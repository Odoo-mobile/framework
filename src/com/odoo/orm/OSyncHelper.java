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
import com.odoo.base.ir.IrModel;
import com.odoo.orm.ORelationRecordList.ORelationRecord;
import com.odoo.support.OUser;
import com.odoo.util.ODate;
import com.odoo.util.PreferenceManager;

public class OSyncHelper {

	public static final String TAG = OSyncHelper.class.getSimpleName();

	private Context mContext = null;
	private OUser mUser = null;
	private OModel mModel = null;
	private Odoo mOdoo = null;
	private App mApp = null;
	private ORelationRecordList mRelationRecordList = new ORelationRecordList();
	private List<String> mFinishedModels = new ArrayList<String>();
	private List<String> mFinishedRelModels = new ArrayList<String>();
	private PreferenceManager mPref = null;
	private List<Integer> mAffectedIds = new ArrayList<Integer>();

	public OSyncHelper(Context context, OUser user, OModel model) {
		mContext = context;
		mUser = user;
		mModel = model;
		mApp = (App) mContext.getApplicationContext();
		if (mApp.inNetwork()) {
			mOdoo = mApp.getOdoo();
		}
	}

	public boolean syncWithServer() {
		return syncWithServer(null);
	}

	public boolean syncWithServer(ODomain domain) {
		return syncWithServer(mModel, domain);
	}

	public boolean syncWithServer(OModel model, ODomain domain) {
		Log.v(TAG, "syncWithServer():" + model.getModelName());
		Log.v(TAG, "User : " + mUser.getAndroidName());
		if (!mFinishedModels.contains(model.getModelName())) {
			mFinishedModels.add(model.getModelName());
			try {
				if (domain == null)
					domain = new ODomain();

				// Adding default domain to domain
				domain.append(model.defaultDomain());
				if (model.checkForCreateDate()) {
					// Adding Old data limit
					mPref = new PreferenceManager(mContext);
					int data_limit = mPref.getInt("sync_data_limit", 60);
					domain.add("create_date", ">=",
							ODate.getDateBefore(data_limit));
				}
				// Adding Last sync date comparing with write_date of record
				if (model.checkForWriteDate() && !model.isEmptyTable()) {
					String last_sync_date = getLastSyncDate(model);
					domain.add("write_date", ">", last_sync_date);
				}
				JSONObject result = mOdoo.search_read(model.getModelName(),
						getFields(model), domain.get());
				handleResult(model, checkForLocalLatestUpdate(model, result));
				handleRelationRecords(model);
				createRecordOnserver(model);
				updateToServer(model);
				deleteRecordFromServer(model);
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
				model.delete(id);
			model.checkInActiveRecord(false);

		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	/**
	 * Check for dirty record in local and update to server
	 * 
	 * @param model
	 */
	private void updateToServer(OModel model) {
		Log.v(TAG, "updating to server:" + model.getModelName());
		try {
			for (ODataRow row : model.select("is_dirty = ?",
					new Object[] { true })) {

				Integer recId = row.getInt("id");
				JSONObject values = createJSONValues(model, row);
				if (values != null) {
					mOdoo.updateValues(model.getModelName(), values, recId);
					OValues vals = new OValues();
					vals.put("is_dirty", "false");
					model.update(vals, row.getInt("local_id"), true);
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
	 * @param result
	 * @return records object, which are new on server or latest updated on
	 *         server
	 */
	private JSONObject checkForLocalLatestUpdate(OModel model, JSONObject result) {
		Log.v(TAG,
				"checking for local latest updated record:"
						+ model.getModelName());
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
						// Check for local write date and server write date
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
					JSONObject write_date_list = perm_read(model, mCheckIds);
					JSONArray results = write_date_list.getJSONArray("result");
					if (results.length() > 0) {
						for (int i = 0; i < results.length(); i++) {
							JSONObject obj = results.getJSONObject(i);
							Integer record_id = obj.getInt("id");
							ODataRow record = model.select(record_id);
							String write_date = obj.getString("write_date");
							String local_write_date = record
									.getString("local_write_date");
							Date local_date = ODate.convertToDate(
									local_write_date, ODate.DEFAULT_FORMAT,
									true);
							Date server_date = ODate.convertToDate(write_date,
									ODate.DEFAULT_FORMAT, true);
							if (local_date.compareTo(server_date) > 0) {
								updateToServerRecordList.add(record);
							} else {
								newORUpdateRecords.put(record_list.get("key_"
										+ record_id));
							}
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

	private void updateRecordOnServer(OModel model, List<ODataRow> records) {
		Log.v(TAG,
				"Updating " + records.size() + " record on server:"
						+ model.getModelName());
		try {
			for (ODataRow row : records) {
				JSONObject values = createJSONValues(model, row);
				if (values != null) {
					mOdoo.updateValues(model.getModelName(), values,
							row.getInt("id"));
					OValues vals = new OValues();
					vals.put("is_dirty", "false");
					model.update(vals, row.getInt("id"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void deleteRecordFromServer(OModel model) {
		Log.v(TAG, "deleting records " + model.getModelName());
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

	private void createRecordOnserver(OModel model) {
		Log.v(TAG, "creating record on server:" + model.getModelName());
		try {
			for (ODataRow row : model.select("id = ?", new Object[] { false })) {
				Integer newId = 0;
				JSONObject values = createJSONValues(model, row);
				if (values != null) {
					values.remove("id");
					JSONObject result = mOdoo.createNew(model.getModelName(),
							values);
					newId = result.getInt("result");
					OValues vals = new OValues();
					vals.put("id", newId);
					vals.put("is_dirty", "false");
					model.update(vals, row.getInt("local_id"), true);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JSONObject createJSONValues(OModel model, ODataRow row) {
		JSONObject values = null;
		try {
			values = new JSONObject();
			for (OColumn col : model.getColumns(false)) {
				if (col.getRelationType() == null) {
					Object val = row.get(col.getName());
					if (val.toString().equals("false") || val == null
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
						break;
					case ManyToMany:
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return values;
	}

	public boolean syncWithMethod(String method, OArguments args) {
		Log.v(TAG, "syncWithMethod():" + mModel.getModelName());
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

	private String getLastSyncDate(OModel model) {
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

	private void handleRelationRecords(OModel model) {
		List<String> keys = new ArrayList<String>();
		keys.addAll(mRelationRecordList.keys());
		for (String key : keys) {
			if (!mFinishedRelModels.contains(key)) {
				mFinishedRelModels.add(key);
				ORelationRecord rel = mRelationRecordList.get(key);
				// Related model
				OModel rel_model = rel.getModel();
				ODomain rel_domain = new ODomain();
				if (rel.getIds().size() > 0)
					rel_domain.add("id", "in", rel.getIds());
				syncWithServer(rel_model, rel_domain);
			}
		}
	}

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
						if (record.get(column.getName()) instanceof JSONArray) {
							JSONArray m2oRecord = record.getJSONArray(column
									.getName());
							// Local table contains only id and name so not
							// required
							// to request on server
							if (m2o.getColumns(false).size() == 2) {
								OValues m2oVals = new OValues();
								m2oVals.put("id", m2oRecord.get(0));
								m2oVals.put("name", m2oRecord.get(1));
								m2o.createORReplace(m2oVals);
							} else {
								// Need to create list of ids for model
								ORelationRecord rel_record = mRelationRecordList.new ORelationRecord();
								if (mRelationRecordList.contains(m2o
										.getModelName())) {
									rel_record = mRelationRecordList.get(m2o
											.getModelName());
								} else {
									rel_record.setModel(m2o);
								}
								rel_record.addId(m2oRecord.getInt(0),
										record.getInt("id"));
								rel_record.setType(column.getRelationType());
								mRelationRecordList.add(m2o.getModelName(),
										rel_record);
							}
							values.put(column.getName(), m2oRecord.get(0));
						}
						break;
					case ManyToMany:
						OModel m2m = model.createInstance(column.getType());
						JSONArray ids_list = record.getJSONArray(column
								.getName());
						for (int i = 0; i < ids_list.length(); i++) {
							r_ids.add(ids_list.getInt(i));
						}
						values.put(column.getName(), r_ids);
						ORelationRecord mrel_record = mRelationRecordList.new ORelationRecord();
						if (mRelationRecordList.contains(m2m.getModelName())) {
							mrel_record = mRelationRecordList.get(m2m
									.getModelName());
						} else {
							mrel_record.setModel(m2m);
						}
						mrel_record.addIds(r_ids, record.getInt("id"));
						mrel_record.setType(column.getRelationType());
						mrel_record.setRefColumn(column.getRelatedColumn());
						mRelationRecordList
								.add(m2m.getModelName(), mrel_record);
						break;
					case OneToMany:
						OModel o2m = model.createInstance(column.getType());
						JSONArray o2m_ids_list = record.getJSONArray(column
								.getName());
						r_ids.clear();
						for (int i = 0; i < o2m_ids_list.length(); i++) {
							r_ids.add(o2m_ids_list.getInt(i));
						}
						// Need to create list of ids for model
						ORelationRecord rel_record = mRelationRecordList.new ORelationRecord();
						if (mRelationRecordList.contains(o2m.getModelName())) {
							rel_record = mRelationRecordList.get(o2m
									.getModelName());
						} else {
							rel_record.setModel(o2m);
						}
						rel_record.addIds(r_ids, record.getInt("id"));
						rel_record.setType(column.getRelationType());
						rel_record.setRefColumn(column.getRelatedColumn());
						mRelationRecordList.add(o2m.getModelName(), rel_record);
						break;
					}
				} else {
					// General record
					values.put(column.getName(), record.get(column.getName()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return values;
	}

	private void handleResult(OModel model, JSONObject result) {
		try {
			JSONArray records = (result.has("result")) ? result
					.getJSONArray("result") : result.getJSONArray("records");
			List<OValues> values_list = new ArrayList<OValues>();
			for (int i = 0; i < records.length(); i++) {
				JSONObject record = records.getJSONObject(i);
				values_list.add(createValueRow(model, model.getColumns(false),
						record));
			}
			// Creating new records.
			List<Integer> affectedIds = model.createORReplace(values_list);
			mAffectedIds.clear();
			mAffectedIds.addAll(affectedIds);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<Integer> getAffectedIds() {
		return mAffectedIds;
	}

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

	public JSONObject getContext(JSONObject obj) {
		try {
			return mOdoo.updateContext((obj != null) ? obj : new JSONObject());
		} catch (Exception e) {
			e.printStackTrace();
			return new JSONObject();
		}
	}

	private JSONObject perm_read(OModel model, List<Integer> ids) {
		try {
			return mOdoo.perm_read(model.getModelName(), ids);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
