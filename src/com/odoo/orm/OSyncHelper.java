/*
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * 
 */
package com.odoo.orm;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import odoo.OArguments;
import odoo.ODomain;
import odoo.Odoo;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.odoo.App;
import com.odoo.base.ir.Attachments;
import com.odoo.base.ir.IrAttachment;
import com.odoo.base.ir.IrModel;
import com.odoo.orm.ORelationRecordList.ORelationRecords;
import com.odoo.support.OUser;
import com.odoo.util.ODate;
import com.odoo.util.PreferenceManager;

/**
 * The Class OSyncHelper.
 */
public class OSyncHelper {

	/** The Constant TAG. */
	public static final String TAG = OSyncHelper.class.getSimpleName();

	/** The context. */
	private Context mContext = null;

	/** The user. */
	private OUser mUser = null;

	/** The model. */
	private OModel mModel = null;

	/** The odoo. */
	private Odoo mOdoo = null;

	/** The app. */
	private App mApp = null;

	/** The relation record list. */
	private ORelationRecordList mRelationRecordList = new ORelationRecordList();

	/** The finished models. */
	private List<String> mFinishedModels = new ArrayList<String>();

	/** The finished rel models. */
	private List<String> mFinishedRelModels = new ArrayList<String>();

	/** The pref. */
	private PreferenceManager mPref = null;

	/** The affected ids. */
	private List<Integer> mAffectedIds = new ArrayList<Integer>();

	/** The sync data limit. */
	private Integer mSyncDataLimit = 0;

	/**
	 * Instantiates a new o sync helper.
	 * 
	 * @param context
	 *            the context
	 * @param user
	 *            the user
	 * @param model
	 *            the model
	 */
	public OSyncHelper(Context context, OUser user, OModel model) {
		mContext = context;
		mUser = user;
		mModel = model;
		mApp = (App) mContext.getApplicationContext();
		if (mApp.inNetwork()) {
			mOdoo = mApp.getOdoo();
		}
	}

	/**
	 * Sync with server.
	 * 
	 * @return true, if successful
	 */
	public boolean syncWithServer() {
		return syncWithServer(null);
	}

	/**
	 * Sync with server.
	 * 
	 * @param domain
	 *            the domain
	 * @return true, if successful
	 */
	public boolean syncWithServer(ODomain domain) {
		return syncWithServer(mModel, domain);
	}

	/**
	 * Sync with server.
	 * 
	 * @param domain
	 *            the domain
	 * @param checkForWriteCreateDate
	 *            the check for write create date
	 * @return true, if successful
	 */
	public boolean syncWithServer(ODomain domain,
			Boolean checkForWriteCreateDate) {
		return syncWithServer(mModel, domain, checkForWriteCreateDate);
	}

	/**
	 * Sync with server.
	 * 
	 * @param model
	 *            the model
	 * @param domain
	 *            the domain
	 * @return true, if successful
	 */
	public boolean syncWithServer(OModel model, ODomain domain) {
		return syncWithServer(model, domain, true);
	}

	/**
	 * Sync with server.
	 * 
	 * @param model
	 *            the model
	 * @param domain_filter
	 *            the domain_filter
	 * @param checkForCreateWriteDate
	 *            the check for create write date
	 * @return true, if successful
	 */
	public boolean syncWithServer(OModel model, ODomain domain_filter,
			Boolean checkForCreateWriteDate) {
		Log.v(TAG, "syncWithServer():" + model.getModelName());
		Log.v(TAG, "User : " + mUser.getAndroidName());
		if (!mFinishedModels.contains(model.getModelName())
				|| !checkForCreateWriteDate) {
			mFinishedModels.add(model.getModelName());
			try {

				ODomain domain = new ODomain();
				// Adding default domain to domain
				domain.append(model.defaultDomain());
				if (checkForCreateWriteDate) {
					if (model.checkForCreateDate()) {
						// Adding Old data limit
						mPref = new PreferenceManager(mContext);
						int data_limit = mPref.getInt("sync_data_limit", 60);
						List<Integer> ids = model.ids();
						if (ids.size() > 0 && model.checkForWriteDate()
								&& !model.isEmptyTable())
							domain.add("|");
						if (ids.size() > 0)
							domain.add("&");
						domain.add("create_date", ">=",
								ODate.getDateBefore(data_limit));
						if (ids.size() > 0)
							domain.add("id", "not in",
									new JSONArray(ids.toString()));
					}
					// Adding Last sync date comparing with write_date of record
					if (model.checkForWriteDate() && !model.isEmptyTable()) {
						String last_sync_date = getLastSyncDate(model);
						domain.add("write_date", ">", last_sync_date);
					}
				}
				if (domain_filter != null)
					domain.append(domain_filter);
				JSONObject result = mOdoo.search_read(model.getModelName(),
						getFields(model), domain.get(), 0, mSyncDataLimit,
						null, null);
				if (checkForCreateWriteDate
						&& model.checkForLocalLatestUpdate()) {
					handleResult(model,
							checkForLocalLatestUpdate(model, result));
				} else {
					handleResult(model, result);
				}
				handleRelationRecords(model);
				// Creating record on server if model allows true
				if (model.canCreateOnServer())
					createRecordOnserver(model);
				// Updating dirty record on server if model allows true
				if (model.canUpdateToServer())
					updateToServer(model);
				// Deleting record from server if model allows true
				if (model.canDeleteFromServer())
					deleteRecordFromServer(model);
				// Deleting record from local if model allows true
				if (model.canDeleteFromLocal())
					deleteRecordInLocal(model);
				return syncFinish(model);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return true;
		}
		return false;
	}

	/**
	 * Delete record in local.
	 * 
	 * @param model
	 *            the model
	 */
	private void deleteRecordInLocal(OModel model) {
		try {
			List<Integer> ids = model.ids();
			ODomain domain = new ODomain();
			domain.add("id", "in", new JSONArray(ids.toString()));
			JSONObject result = mOdoo.search_read(model.getModelName(),
					new JSONObject(), domain.get());
			JSONArray records = result.getJSONArray("records");
			if (records.length() > 0) {
				for (int i = 0; i < records.length(); i++) {
					Integer server_id = records.getJSONObject(i).getInt("id");
					ids.remove(ids.indexOf(server_id));
				}
			}
			model.checkInActiveRecord(true);
			for (Integer id : ids)
				model.delete("id = ? ", new Object[] { id });
			model.checkInActiveRecord(false);

		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	/**
	 * Check for dirty record in local and update to server.
	 * 
	 * @param model
	 *            the model
	 */
	private void updateToServer(OModel model) {
		try {
			for (ODataRow row : model.select("is_dirty = ?",
					new Object[] { true })) {
				Integer recId = row.getInt("id");
				JSONObject values = createJSONValues(model, row);
				if (values != null) {
					mOdoo.updateValues(model.getModelName(), values, recId);
					OValues vals = new OValues();
					vals.put("is_dirty", "false");
					model.update(vals, row.getInt("local_id"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Check for local latest updated records. If result is from call_kw than
	 * ignoring it.
	 * 
	 * @param model
	 *            the model
	 * @param result
	 *            the result
	 * @return records object, which are new on server or latest updated on
	 *         server
	 */
	public JSONObject checkForLocalLatestUpdate(OModel model, JSONObject result) {
		JSONObject newResult = result;
		try {
			if (result.has("records")
					&& result.getJSONArray("records").length() > 0) {
				newResult = new JSONObject();
				JSONArray newORUpdateRecords = new JSONArray();
				JSONArray records = result.getJSONArray("records");
				List<Integer> mCheckIds = new ArrayList<Integer>();
				HashMap<String, JSONObject> record_list = new HashMap<String, JSONObject>();
				model.checkInActiveRecord(true);
				for (int i = 0; i < records.length(); i++) {
					JSONObject record = records.getJSONObject(i);
					if (model.hasRecord(record.getInt("id"))) {
						mCheckIds.add(record.getInt("id"));
						record_list.put("key_" + record.getInt("id"), record);
					} else {
						// New record.
						newORUpdateRecords.put(record);
					}
				}
				List<ODataRow> updateToServerRecordList = new ArrayList<ODataRow>();
				if (mCheckIds.size() > 0) {
					// Getting write_date for records
					HashMap<String, String> write_dates = getWriteDate(model,
							mCheckIds);
					for (Integer id : mCheckIds) {
						String key = "KEY_" + id;
						String write_date = write_dates.get(key);
						ODataRow record = model.select("id = ? ",
								new Object[] { id }).get(0);
						String local_write_date = record
								.getString("local_write_date");

						Date local_date = ODate.convertToDate(local_write_date,
								ODate.DEFAULT_FORMAT, true);
						Date server_date = ODate.convertToDate(write_date,
								ODate.DEFAULT_FORMAT, true);

						if (local_date.compareTo(server_date) > 0) {
							updateToServerRecordList.add(record);
						} else {
							newORUpdateRecords
									.put(record_list.get("key_" + id));
						}
					}
				}
				if (updateToServerRecordList.size() > 0) {
					updateRecordOnServer(model, updateToServerRecordList);
				}
				newResult.accumulate("records", newORUpdateRecords);
				model.checkInActiveRecord(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newResult;
	}

	/**
	 * Update record on server.
	 * 
	 * @param model
	 *            the model
	 * @param records
	 *            the records
	 */
	private void updateRecordOnServer(OModel model, List<ODataRow> records) {
		try {
			for (ODataRow row : records) {
				JSONObject values = createJSONValues(model, row);
				if (values != null) {
					mOdoo.updateValues(model.getModelName(), values,
							row.getInt("id"));
					OValues vals = new OValues();
					if (row.getBoolean("is_active")) {
						vals.put("is_dirty", "false");
					}
					model.update(vals, row.getInt(OColumn.ROW_ID));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Delete record from server.
	 * 
	 * @param model
	 *            the model
	 */
	private void deleteRecordFromServer(OModel model) {
		try {
			model.checkInActiveRecord(true);
			for (ODataRow row : model.select("is_active = ? AND is_dirty = ?",
					new Object[] { false, true })) {
				if (mOdoo.unlink(model.getModelName(), row.getInt("id"))) {
					model.delete("id = ?", new Object[] { row.getInt("id") },
							true);
				}
			}
			model.checkInActiveRecord(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates the record on server.
	 * 
	 * @param model
	 *            the model
	 */
	private void createRecordOnserver(OModel model) {
		try {
			for (ODataRow row : model.select("id = ? ", new Object[] { 0 })) {
				create(model, row);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates record on server
	 * 
	 * @param data_row
	 *            the data_row
	 */
	public void create(ODataRow data_row) {
		create(mModel, data_row);
	}

	/**
	 * Creates record on server
	 * 
	 * @param model
	 *            the model
	 * @param data_row
	 *            the data_row
	 */
	public Integer create(OModel model, ODataRow data_row) {
		try {
			JSONObject values = createJSONValues(model, data_row);
			if (model.getModelName().equals(
					new IrAttachment(mContext).getModelName())) {
				if (data_row.contains(Attachments.KEY_DB_DATAS))
					values.put(Attachments.KEY_DB_DATAS,
							data_row.get(Attachments.KEY_DB_DATAS));
				if (data_row.contains(Attachments.KEY_TYPE))
					values.put(Attachments.KEY_TYPE,
							data_row.get(Attachments.KEY_TYPE));
			}
			if (values != null) {
				Integer newId = 0;
				values.remove("id");
				JSONObject result = mOdoo.createNew(model.getModelName(),
						values);
				newId = result.getInt("result");
				OValues vals = new OValues();
				vals.put("id", newId);
				vals.put("is_dirty", "false");
				model.update(vals, data_row.getInt(OColumn.ROW_ID));
				return newId;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Creates the json values.
	 * 
	 * @param model
	 *            the model
	 * @param row
	 *            the row
	 * @return the JSON object
	 */
	private JSONObject createJSONValues(OModel model, ODataRow row) {
		JSONObject values = null;
		try {
			values = new JSONObject();
			for (OColumn col : model.getColumns(false)) {
				if (col.getRelationType() == null) {
					Object val = row.get(col.getName());
					if (val == null || val.toString().equals("false")
							|| TextUtils.isEmpty(val.toString()))
						val = false;
					values.put(col.getName(), val);
				} else {
					// Relation columns
					switch (col.getRelationType()) {
					case ManyToOne:
						ODataRow m2o = row.getM2ORecord(col.getName()).browse();
						if (m2o != null)
							values.put(col.getName(), m2o.getInt("id"));
						break;
					case OneToMany:
						JSONArray o2mRecords = new JSONArray();
						List<ODataRow> o2mRecordList = row.getO2MRecord(
								col.getName()).browseEach();
						if (o2mRecordList.size() > 0) {
							JSONArray rec_ids = new JSONArray();
							for (ODataRow o2mR : o2mRecordList) {
								if (o2mR.getInt("id") != 0)
									rec_ids.put(o2mR.getInt("id"));
							}
							o2mRecords.put(6);
							o2mRecords.put(false);
							o2mRecords.put(rec_ids);
							values.put(col.getName(),
									new JSONArray().put(o2mRecords));
						}
						break;
					case ManyToMany:
						JSONArray m2mRecords = new JSONArray();
						List<ODataRow> m2mRecordList = row.getM2MRecord(
								col.getName()).browseEach();
						if (m2mRecordList.size() > 0) {
							JSONArray rec_ids = new JSONArray();
							for (ODataRow o2mR : m2mRecordList) {
								if (o2mR.getInt("id") != 0)
									rec_ids.put(o2mR.getInt("id"));
							}
							m2mRecords.put(6);
							m2mRecords.put(false);
							m2mRecords.put(rec_ids);
							values.put(col.getName(),
									new JSONArray().put(m2mRecords));
						}
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return values;
	}

	/**
	 * Sync with method.
	 * 
	 * @param method
	 *            the method
	 * @param args
	 *            the args
	 * @return true, if successful
	 */
	public boolean syncWithMethod(String method, OArguments args) {
		boolean synced = false;
		try {
			JSONObject result = mOdoo.call_kw(mModel.getModelName(), method,
					args.getArray());
			handleResult(mModel, result);
			handleRelationRecords(mModel);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return synced;
	}

	/**
	 * Gets the last sync date.
	 * 
	 * @param model
	 *            the model
	 * @return the last sync date
	 */
	public String getLastSyncDate(OModel model) {
		String last_sync_date = "false";
		IrModel irModel = new IrModel(mContext);
		List<ODataRow> records = irModel.select("model = ?",
				new Object[] { model.getModelName() });
		if (records.size() > 0) {
			last_sync_date = records.get(0).getString("last_synced");
		}
		if (last_sync_date.equals("false"))
			last_sync_date = ODate.getDate();
		return last_sync_date;

	}

	/**
	 * Handle relation records.
	 * 
	 * @param model
	 *            the model
	 */
	private void handleRelationRecords(OModel model) {

		List<String> keys = new ArrayList<String>();
		keys.addAll(mRelationRecordList.keys());
		for (String key : keys) {
			if (!mFinishedRelModels.contains(key)) {
				mFinishedRelModels.add(key);
				ORelationRecords rel = mRelationRecordList.get(key);
				OModel base_model = rel.getBaseModel();
				base_model.setSyncingDataFlag(true);
				OModel rel_model = rel.getRelModel();
				rel_model.setSyncingDataFlag(true);
				ODomain rel_domain = new ODomain();
				if (rel.getRelIds().size() > 0) {
					rel_domain.add("id", "in", rel.getRelIds());
					syncWithServer(rel_model, rel_domain, false);
				}
			}
		}
	}

	/**
	 * Sync finish.
	 * 
	 * @param model
	 *            the model
	 * @return true, if successful
	 */
	private boolean syncFinish(OModel model) {
		String finish_date_time = ODate.getDate();
		Log.v(TAG, model.getModelName() + " sync finished at "
				+ finish_date_time + " (UTC)");
		IrModel irmodel = new IrModel(mContext);
		OValues values = new OValues();
		values.put("last_synced", finish_date_time);
		irmodel.update(values, "model = ?",
				new Object[] { model.getModelName() });
		return true;
	}

	/**
	 * Creates the value row.
	 * 
	 * @param model
	 *            the model
	 * @param columns
	 *            the columns
	 * @param original_record
	 *            the original_record
	 * @return the o values
	 */
	private OValues createValueRow(OModel model, List<OColumn> columns,
			JSONObject original_record) {
		OValues values = new OValues();
		try {
			List<Integer> r_ids = new ArrayList<Integer>();
			for (OColumn column : columns) {
				JSONObject record = model.beforeCreateRow(column,
						original_record);
				if (column.getRelationType() != null) {
					// Relation records
					switch (column.getRelationType()) {
					case ManyToOne:
						/*
						 * Handling ManyToOne records
						 */
						OModel m2o = model.createInstance(column.getType());
						String rel_key = m2o.getTableName() + "_"
								+ column.getName();
						if (record.get(column.getName()) instanceof JSONArray) {
							JSONArray m2oRecord = record.getJSONArray(column
									.getName());
							// Local table contains only id and name so not
							// required
							// to request on server
							if (m2o.getColumns(false).size() > 2
									|| (m2o.getColumns(false).size() > 4 && model
											.getOdooVersion()
											.getVersion_number() > 7)) {
								// Need to create list of ids for model
								ORelationRecords rel_record = mRelationRecordList.new ORelationRecords();
								if (mRelationRecordList.contains(rel_key)) {
									rel_record = mRelationRecordList
											.get(rel_key);
								} else {
									rel_record.setRelModel(m2o);
									rel_record.setBaseModel(model);
								}
								rel_record.setRefColumn(column.getName());
								rel_record.setRelationType(column
										.getRelationType());
								rel_record.addBaseRelId(record.getInt("id"),
										m2oRecord.getInt(0));

								// Creating relation ids list for relation model
								mRelationRecordList.add(rel_key, rel_record);
							}
							OValues m2oVals = new OValues();
							m2oVals.put("id", m2oRecord.get(0));
							m2oVals.put("name", m2oRecord.get(1));
							m2oVals.put("is_dirty", false);
							Integer row_id = m2o.createORReplace(m2oVals);
							// Replacing original id with row_id to maintain
							// relation for local
							m2oRecord.put(0, row_id);
							record.put(column.getName(), m2oRecord);
							values.put(column.getName(), m2oRecord.get(0));
						}
						break;
					case ManyToMany:
						r_ids.clear();
						OModel m2m = model.createInstance(column.getType());
						rel_key = m2m.getTableName() + "_" + column.getName();
						JSONArray ids_list = record.getJSONArray(column
								.getName());
						int len = ids_list.length();
						// limiting sync limit for many to many
						int record_len = column.getRecordSyncLimit();
						if (record_len != -1 && len > record_len)
							len = record_len;
						List<Integer> row_ids = new ArrayList<Integer>();
						for (int i = 0; i < len; i++) {
							int server_id = ids_list.getInt(i);
							r_ids.add(server_id);
							OValues vals = new OValues();
							vals.put("id", server_id);
							int row_id = m2m.createORReplace(vals);
							row_ids.add(row_id);
						}
						values.put(column.getName(), row_ids);
						ORelationRecords mrel_record = mRelationRecordList.new ORelationRecords();
						if (mRelationRecordList.contains(rel_key)) {
							mrel_record = mRelationRecordList.get(rel_key);
						} else {
							mrel_record.setRelModel(m2m);
							mrel_record.setBaseModel(model);
						}
						mrel_record.addBaseRelId(record.getInt("id"), r_ids);
						mrel_record.setRefColumn(column.getName());
						mrel_record.setRelationType(column.getRelationType());
						// Creating relation ids list for relation model
						mRelationRecordList.add(rel_key, mrel_record);
						break;
					case OneToMany:
						OModel o2m = model.createInstance(column.getType());
						rel_key = o2m.getTableName() + "_" + column.getName();
						JSONArray o2m_ids_list = record.getJSONArray(column
								.getName());
						r_ids.clear();
						for (int i = 0; i < o2m_ids_list.length(); i++) {
							r_ids.add(o2m_ids_list.getInt(i));
						}
						// Need to create list of ids for model
						ORelationRecords rel_record = mRelationRecordList.new ORelationRecords();
						if (mRelationRecordList.contains(rel_key)) {
							rel_record = mRelationRecordList.get(rel_key);
						} else {
							rel_record.setRelModel(o2m);
							rel_record.setBaseModel(model);
						}
						rel_record.addBaseRelId(record.getInt("id"), r_ids);
						rel_record.setRefColumn(column.getName());
						rel_record.setRelationType(column.getRelationType());
						// Creating relation ids list for relation model
						mRelationRecordList.add(rel_key, rel_record);
						break;
					}
				} else {
					// General record
					values.put(column.getName(), record.get(column.getName()));
				}
			}
			values.put("is_dirty", false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return values;
	}

	/**
	 * Handle result.
	 * 
	 * @param model
	 *            the model
	 * @param result
	 *            the result
	 */
	public void handleResult(OModel model, JSONObject result) {
		try {
			JSONArray records = (result.has("result")) ? result
					.getJSONArray("result") : result.getJSONArray("records");
			List<OValues> values_list = new ArrayList<OValues>();
			for (int i = 0; i < records.length(); i++) {
				JSONObject record = records.getJSONObject(i);
				OValues vals = createValueRow(model, model.getColumns(false),
						record);
				values_list.add(vals);
			}
			// Creating new records.
			List<Integer> affectedIds = model.createORReplace(values_list);
			mAffectedIds.clear();
			mAffectedIds.addAll(affectedIds);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the affected ids.
	 * 
	 * @return the affected ids
	 */
	public List<Integer> getAffectedIds() {
		return mAffectedIds;
	}

	/**
	 * Gets the fields.
	 * 
	 * @param model
	 *            the model
	 * @return the fields
	 */
	public JSONObject getFields(OModel model) {
		JSONObject fields = new JSONObject();
		try {
			for (OColumn column : model.getColumns(false))
				fields.accumulate("fields", column.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fields;
	}

	/**
	 * Model info.
	 * 
	 * @param models
	 *            the models
	 * @return the list
	 */
	public List<OValues> modelInfo(List<String> models) {
		List<OValues> models_list = new ArrayList<OValues>();
		try {
			ODomain domain = new ODomain();
			domain.add("name", "in", new JSONArray(models.toString()));
			JSONObject result = mOdoo.search_read("ir.module.module",
					getFields(mModel), domain.get());
			JSONArray records = result.getJSONArray("records");
			for (int i = 0; i < records.length(); i++) {
				JSONObject record = records.getJSONObject(i);
				OValues values = createValueRow(mModel,
						mModel.getColumns(false), record);
				models_list.add(values);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return models_list;
	}

	/**
	 * Gets the context.
	 * 
	 * @param obj
	 *            the obj
	 * @return the context
	 */
	public JSONObject getContext(JSONObject obj) {
		try {
			return mOdoo.updateContext((obj != null) ? obj : new JSONObject());
		} catch (Exception e) {
			e.printStackTrace();
			return new JSONObject();
		}
	}

	/**
	 * Gets the write date.
	 * 
	 * @param model
	 *            the model
	 * @param ids
	 *            the ids
	 * @return the write date
	 */
	private HashMap<String, String> getWriteDate(OModel model, List<Integer> ids) {
		HashMap<String, String> map = new HashMap<String, String>();
		try {
			JSONArray results = new JSONArray();
			if (model.getPermReadColumn("write_date") != null) {
				JSONObject fields = new JSONObject();
				fields.accumulate("fields", "write_date");
				ODomain domain = new ODomain();
				domain.add("id", "in", ids);
				JSONObject result = mOdoo.search_read(model.getModelName(),
						fields, domain.get());
				results = result.getJSONArray("records");
			} else {
				JSONObject write_date_list = perm_read(model, ids);
				results = write_date_list.getJSONArray("result");
			}
			if (results.length() > 0) {
				for (int i = 0; i < results.length(); i++) {
					JSONObject obj = results.getJSONObject(i);
					map.put("KEY_" + obj.getInt("id"),
							obj.getString("write_date"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	/**
	 * Sync data limit.
	 * 
	 * @param dataLimit
	 *            the data limit
	 * @return the o sync helper
	 */
	public OSyncHelper syncDataLimit(Integer dataLimit) {
		mSyncDataLimit = dataLimit;
		return this;
	}

	/**
	 * Perm_read.
	 * 
	 * @param model
	 *            the model
	 * @param ids
	 *            the ids
	 * @return the JSON object
	 */
	private JSONObject perm_read(OModel model, List<Integer> ids) {
		try {
			return mOdoo.perm_read(model.getModelName(), ids);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Call method.
	 * 
	 * @param method
	 *            the method
	 * @param args
	 *            the args
	 * @return the object
	 */
	public Object callMethod(String method, OArguments args) {
		return callMethod(method, args, null, null);
	}

	/**
	 * Call method.
	 * 
	 * @param method
	 *            the method
	 * @param args
	 *            the args
	 * @param context
	 *            the context
	 * @return the object
	 */
	public Object callMethod(String method, OArguments args, JSONObject context) {
		return callMethod(mModel.getModelName(), method, args, context, null);
	}

	public Object callMethod(String method, OArguments args,
			JSONObject context, JSONObject kwargs) {
		return callMethod(mModel.getModelName(), method, args, context, kwargs);
	}

	/**
	 * Call method.
	 * 
	 * @param method
	 *            the method
	 * @param args
	 *            the args
	 * @param context
	 *            the context
	 * @param kwargs
	 *            the kwargs
	 * @return the object
	 */
	public Object callMethod(String model, String method, OArguments args,
			JSONObject context, JSONObject kwargs) {
		try {
			if (kwargs == null)
				kwargs = new JSONObject();
			if (context != null) {
				args.add(mOdoo.updateContext(context));
			}
			JSONObject result = mOdoo.call_kw(model, method, args.getArray(),
					kwargs);
			if (result.has("result")) {
				return result.get("result");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
