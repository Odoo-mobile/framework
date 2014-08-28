package com.odoo.support.service;

import java.util.ArrayList;
import java.util.List;

import odoo.ODomain;
import odoo.Odoo;

import org.json.JSONArray;
import org.json.JSONObject;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.odoo.App;
import com.odoo.auth.OdooAccountManager;
import com.odoo.base.ir.IrModel;
import com.odoo.orm.OColumn;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OModel;
import com.odoo.orm.ORelationRecordList;
import com.odoo.orm.ORelationRecordList.ORelationRecords;
import com.odoo.orm.OSyncHelper;
import com.odoo.orm.OValues;
import com.odoo.orm.types.OBoolean;
import com.odoo.support.OUser;
import com.odoo.util.ODate;
import com.odoo.util.PreferenceManager;

public class OSyncAdapter extends AbstractThreadedSyncAdapter {
	public static final String TAG = OSyncAdapter.class.getSimpleName();
	private final ContentResolver mContentResolver;
	private Context mContext = null;
	private OModel mModel = null;
	private App mApp = null;
	private Odoo mOdoo = null;
	private ODomain mDomain = null;
	private Boolean checkForWriteCreateDate = true;
	/** The relation record list. */
	private ORelationRecordList mRelationRecordList = new ORelationRecordList();
	private OSyncHelper mSync = null;

	/** The finished models. */
	private List<String> mFinishedModels = new ArrayList<String>();

	/** The finished rel models. */
	private List<String> mFinishedRelModels = new ArrayList<String>();
	/** The sync data limit. */
	private Integer mSyncDataLimit = 0;
	private PreferenceManager mPref = null;

	public OSyncAdapter(Context context, OModel model, boolean autoInitialize) {
		super(context, autoInitialize);
		mContext = context;
		mModel = model;
		mContentResolver = context.getContentResolver();
		mApp = (App) mContext.getApplicationContext();
		init();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public OSyncAdapter(Context context, OModel model, boolean autoInitialize,
			boolean allowParallelSyncs) {
		super(context, autoInitialize, allowParallelSyncs);
		mContext = context;
		mModel = model;
		mContentResolver = context.getContentResolver();
		init();
	}

	private void init() {
		mPref = new PreferenceManager(mContext);
		mSync = mModel.getSyncHelper();
		mOdoo = mApp.getOdoo();
	}

	public OSyncAdapter setDomain(ODomain domain) {
		mDomain = domain;
		return this;
	}

	public OSyncAdapter checkForWriteCreateDate(Boolean check) {
		checkForWriteCreateDate = check;
		return this;
	}

	public OSyncAdapter syncDataLimit(Integer dataLimit) {
		mSyncDataLimit = dataLimit;
		return this;
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		final OUser user = OdooAccountManager.getAccountDetail(mContext,
				account.name);
		Log.v(TAG, "Performing sync for :" + mModel.getModelName());
		Log.v(TAG, "User : " + user.getAndroidName());
		Log.v(TAG, mModel.uri().toString());
		mModel.setUser(user);
		mApp.setSyncUser(user);
		performSync(mModel, mDomain, account, syncResult, true);
	}

	private void performSync(OModel model, ODomain domain, Account account,
			SyncResult syncResult, Boolean dataCheck) {
		if (!mFinishedModels.contains(model.getModelName())
				|| !checkForWriteCreateDate) {
			mFinishedModels.add(model.getModelName());
			try {

				/**
				 * Preparing domain
				 */
				if (domain == null)
					domain = new ODomain();
				// Adding default domain to domain
				domain.append(model.defaultDomain());
				// checking for write/create date
				if (checkForWriteCreateDate && dataCheck) {
					if (model.checkForCreateDate()) {
						// Adding Old data limit
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
						String last_sync_date = mSync.getLastSyncDate(model);
						domain.add("write_date", ">", last_sync_date);
					}
				}

				/**
				 * Getting data from server
				 */
				JSONObject result = mOdoo.search_read(model.getModelName(),
						mSync.getFields(model), domain.get(), 0,
						mSyncDataLimit, null, null);
				if (checkForWriteCreateDate
						&& model.checkForLocalLatestUpdate() && dataCheck) {
					handleResult(account, syncResult, model,
							mSync.checkForLocalLatestUpdate(model, result));
				} else {
					handleResult(account, syncResult, model, result);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void handleResult(Account account, SyncResult syncResult,
			OModel model, JSONObject result) {
		ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
		try {
			JSONArray records = (result.has("result")) ? result
					.getJSONArray("result") : result.getJSONArray("records");
			for (int i = 0; i < records.length(); i++) {
				JSONObject record = records.getJSONObject(i);
				batch.clear();
				batch.add(createBatch(account, model, record));
				mContentResolver.applyBatch(model.authority(), batch);
			}
			// mContentResolver.applyBatch(model.authority(), batch);
			// Updating relation records for master record
			updateRelationRecords(account, syncResult);
			// Creating record on server if model allows true
			if (model.canCreateOnServer())
				createRecordOnserver(account, model, syncResult);
			// Deleting record from server if model allows true
			if (model.canDeleteFromServer())
				deleteRecordFromServer(account, model, syncResult);
			// Deleting record from local if model allows true
			if (model.canDeleteFromLocal())
				deleteRecordInLocal(model, syncResult);
			// Updating dirty record on server if model allows true
			if (model.canUpdateToServer())
				updateToServer(account, model, syncResult);
			syncFinish(model, syncResult);
			mContentResolver.notifyChange(model.uri(), null, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean syncFinish(OModel model, SyncResult syncResult) {
		String finish_date_time = ODate.getDate();
		Log.v(TAG, model.getModelName() + " sync finished at "
				+ finish_date_time + " (UTC)");
		IrModel irmodel = new IrModel(mContext);
		OValues values = new OValues();
		values.put("last_synced", finish_date_time);
		irmodel.update(values, "model = ?",
				new Object[] { model.getModelName() });
		mApp.setSyncUser(null);
		return true;
	}

	private void updateRelationRecords(Account account, SyncResult syncResult) {
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
					// Removing from finished sync to sync new data
					int model_index = mFinishedModels.indexOf(rel_model
							.getModelName());
					if (model_index > -1)
						mFinishedModels.remove(model_index);
					performSync(rel_model, rel_domain, account, syncResult,
							false);
				}
			}
		}
	}

	private void deleteRecordInLocal(OModel model, SyncResult syncResult) {
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
			ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
			for (Integer id : ids) {
				Integer localId = model.selectRowId(id);
				ContentProviderOperation.Builder builder = ContentProviderOperation
						.newDelete(model.uri().buildUpon()
								.appendPath(Integer.toString(localId)).build());
				batch.add(builder.build());
			}
			mContentResolver.applyBatch(model.authority(), batch);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void deleteRecordFromServer(Account account, OModel model,
			SyncResult syncResult) {
		Cursor c = mContentResolver.query(model.uri(), model.projection(),
				"is_active = ? or is_active = 0 and is_dirty = ? or is_dirty = 0 and odoo_name = ?",
				new String[] { "false", "true", account.name }, null);
		assert c != null;
		Log.i(TAG, "Found " + c.getCount()
				+ " local entries for delete on server");
		while (c.moveToNext()) {
			Integer recId = c.getInt(c.getColumnIndex("id"));
			Integer localId = c.getInt(c.getColumnIndex(OColumn.ROW_ID));
			try {
				if (mOdoo.unlink(model.getModelName(), recId)) {
					ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
					ContentProviderOperation.Builder builder = ContentProviderOperation
							.newDelete(model.uri().buildUpon()
									.appendPath(Integer.toString(localId))
									.build());
					batch.add(builder.build());
					mContentResolver.applyBatch(model.authority(), batch);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		c.close();
	}

	private void updateToServer(Account account, OModel model,
			SyncResult syncResult) {
		Cursor c = mContentResolver.query(model.uri(), model.projection(),
				"is_dirty = ? and odoo_name = ?", new String[] { "true",
						account.name }, null);
		assert c != null;
		Log.i(TAG, "Found " + c.getCount()
				+ " local dirty entries for upload to server");
		while (c.moveToNext()) {
			Integer recId = c.getInt(c.getColumnIndex("id"));
			Integer localId = c.getInt(c.getColumnIndex(OColumn.ROW_ID));
			JSONObject values = createJSONValues(model, c);
			try {
				if (values != null) {
					mOdoo.updateValues(model.getModelName(), values, recId);
					ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
					ContentProviderOperation.Builder builder = ContentProviderOperation
							.newUpdate(model.uri().buildUpon()
									.appendPath(Integer.toString(localId))
									.build());
					builder.withValue("is_dirty", false);
					batch.add(builder.build());
					mContentResolver.applyBatch(model.authority(), batch);
					mContentResolver.notifyChange(model.uri(), null, false);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		c.close();
	}

	private void createRecordOnserver(Account account, OModel model,
			SyncResult syncResult) {
		Cursor c = mContentResolver.query(model.uri(), model.projection(),
				"id = ? and odoo_name = ?", new String[] { "0", account.name },
				null);
		assert c != null;
		Log.i(TAG, "Found " + c.getCount()
				+ " local entries for upload to server");
		while (c.moveToNext()) {
			create(model, c);
		}
		c.close();
	}

	public void create(OModel model, Cursor c) {
		try {
			Integer local_id = c.getInt(c.getColumnIndex(OColumn.ROW_ID));
			JSONObject values = createJSONValues(model, c);
			if (values != null) {
				Integer newId = 0;
				values.remove("id");
				JSONObject result = mOdoo.createNew(model.getModelName(),
						values);
				newId = result.getInt("result");
				ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
				ContentProviderOperation.Builder builder = ContentProviderOperation
						.newUpdate(model.uri().buildUpon()
								.appendPath(Integer.toString(local_id)).build());
				builder.withValue("id", newId);
				builder.withValue("is_dirty", false);
				batch.add(builder.build());
				mContentResolver.applyBatch(model.authority(), batch);
				mContentResolver.notifyChange(model.uri(), null, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JSONObject createJSONValues(OModel model, Cursor c) {
		JSONObject values = null;
		try {
			values = new JSONObject();
			for (OColumn col : model.getColumns(false)) {
				if (col.getRelationType() == null) {
					Object val = model.createRecordRow(col, c);
					if (val.toString().equals("false") || val == null
							|| TextUtils.isEmpty(val.toString()))
						val = false;
					if (val.toString().equals("true"))
						val = true;
					values.put(col.getName(), val);
				} else {
					// Relation columns
					switch (col.getRelationType()) {
					case ManyToOne:
						OModel rel_model = model.createInstance(col.getType());
						Object val = model.createRecordRow(col, c);
						if (val instanceof Integer) {
							val = rel_model.selectServerId((Integer) val);
						}
						values.put(col.getName(), val);
						break;
					case OneToMany:
						rel_model = model.createInstance(col.getType());
						JSONArray o2mRecords = new JSONArray();
						List<ODataRow> o2mRecordList = rel_model.select(
								col.getRelatedColumn() + " = ?",
								new Object[] { c.getInt(c
										.getColumnIndex(OColumn.ROW_ID)) });
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
						rel_model = model.createInstance(col.getType());
						JSONArray m2mRecords = new JSONArray();
						List<ODataRow> m2mRecordList = model.selectM2MRecords(
								model, rel_model,
								c.getInt(c.getColumnIndex(OColumn.ROW_ID)));
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

	private ContentProviderOperation createBatch(Account account, OModel model,
			JSONObject original_record) {
		ContentProviderOperation.Builder batch = null;
		try {
			int id = original_record.getInt("id");
			boolean update = model.hasRecord(id);
			batch = (update) ? ContentProviderOperation.newUpdate(model.uri()
					.buildUpon()
					.appendPath(Integer.toString(model.selectRowId(id)))
					.build()) : ContentProviderOperation.newInsert(model.uri());
			List<Integer> r_ids = new ArrayList<Integer>();
			for (OColumn column : model.getColumns(false)) {
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
							if (column.canSyncMasterRecord()
									&& (m2o.getColumns(false).size() > 2 || (m2o
											.getColumns(false).size() > 4 && model
											.getOdooVersion()
											.getVersion_number() > 7))) {
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
							batch.withValue(column.getName(), m2oRecord.get(0));
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
						// FIXME: What with many to many record in
						// ContentProvider ???
						// values.put(column.getName(), row_ids);
						// batch.withValue("xyz", row_ids);
						batch.withValue(column.getName(), row_ids.toString());
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
					// Simple columns
					Object value = record.get(column.getName());
					if (column.getType().isAssignableFrom(OBoolean.class))
						value = value.toString();
					if (value.toString().equals("false"))
						value = value.toString();
					batch.withValue(column.getName(), value);
				}
			}

			for (OColumn col : model.getFunctionalColumns()) {
				if (col.canFunctionalStore()) {
					OValues values = new OValues();
					if (!col.isLocal())
						values.put(col.getName(),
								original_record.get(col.getName()));
					for (String dCol : col.getFunctionalStoreDepends()) {
						Object data = original_record.get(dCol);
						values.put(dCol, data);
					}
					Object value = model.getFunctionalMethodValue(col, values);
					batch.withValue(col.getName(), value);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		batch.withValue("is_dirty", "false");
		batch.withValue("local_write_date", ODate.getDate());
		batch.withValue("odoo_name", account.name);
		return batch.build();
	}
}
