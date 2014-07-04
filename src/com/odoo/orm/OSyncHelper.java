package com.odoo.orm;

import java.util.ArrayList;
import java.util.List;

import odoo.OEDomain;
import odoo.Odoo;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.odoo.App;
import com.odoo.base.ir.IrModel;
import com.odoo.orm.ORelationRecordList.ORelationRecord;
import com.odoo.support.OUser;
import com.odoo.util.ODate;
import com.odoo.util.PreferenceManager;

public class OSyncHelper {

	public static final String TAG = OSyncHelper.class.getSimpleName();

	Context mContext = null;
	OUser mUser = null;
	OModel mModel = null;
	Odoo mOdoo = null;
	Boolean isConnection = false;
	App mApp = null;
	ORelationRecordList mRelationRecordList = new ORelationRecordList();
	List<String> mFinishedModels = new ArrayList<String>();
	List<String> mFinishedRelModels = new ArrayList<String>();
	PreferenceManager mPref = null;

	Boolean mCheckForWriteDate = true, mCheckForCreateDate = true;

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

	public boolean syncWithServer(OEDomain domain) {
		return syncWithServer(mModel, domain);
	}

	public OSyncHelper noCheckForWriteDate() {
		mCheckForWriteDate = false;
		return this;
	}

	public OSyncHelper noCheckForCreateDate() {
		mCheckForCreateDate = false;
		return this;
	}

	public boolean syncWithServer(OModel model, OEDomain domain) {
		Log.v(TAG, "syncWithServer():" + model.getModelName());
		if (!mFinishedModels.contains(model.getModelName())) {
			mFinishedModels.add(model.getModelName());
			try {
				if (domain == null)
					domain = new OEDomain();

				// Adding default domain to domain
				domain.append(model.defaultDomain());
				if (mCheckForCreateDate) {
					// Adding Old data limit
					mPref = new PreferenceManager(mContext);
					int data_limit = mPref.getInt("sync_data_limit", 60);
					domain.add("create_date", ">=",
							ODate.getDateBefore(data_limit));
				}
				// Adding Last sync date comparing with write_date of record
				if (mCheckForWriteDate && !model.isEmptyTable()) {
					String last_sync_date = getLastSyncDate(model);
					domain.add("write_date", ">", last_sync_date);
				}
				JSONObject result = mOdoo.search_read(model.getModelName(),
						getFields(model), domain.get());
				handleResult(model, result);
				handleRelationRecords(model);
				return syncFinish(model);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return true;
		}
		return false;
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
				OEDomain rel_domain = new OEDomain();
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
			JSONObject record) {
		OValues values = new OValues();
		try {
			List<Integer> r_ids = new ArrayList<Integer>();
			for (OColumn column : columns) {
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
			JSONArray records = result.getJSONArray("records");
			List<OValues> values_list = new ArrayList<OValues>();
			for (int i = 0; i < records.length(); i++) {
				JSONObject record = records.getJSONObject(i);
				values_list.add(createValueRow(model, model.getColumns(false),
						record));
			}
			// Creating new records.
			List<Integer> affectedIds = model.createORReplace(values_list);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			OEDomain domain = new OEDomain();
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

}
