/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p/>
 * Created on 1/1/15 3:17 PM
 */
package com.odoo.core.service;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.odoo.App;
import com.odoo.R;
import com.odoo.base.addons.ir.IrModel;
import com.odoo.base.addons.res.ResCompany;
import com.odoo.core.account.About;
import com.odoo.core.auth.OdooAccountManager;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.rpc.Odoo;
import com.odoo.core.rpc.handler.OdooVersionException;
import com.odoo.core.rpc.helper.ODomain;
import com.odoo.core.rpc.helper.ORecordValues;
import com.odoo.core.rpc.helper.OdooFields;
import com.odoo.core.rpc.helper.utils.gson.OdooRecord;
import com.odoo.core.rpc.helper.utils.gson.OdooResult;
import com.odoo.core.support.OUser;
import com.odoo.core.utils.ODateUtils;
import com.odoo.core.utils.OPreferenceManager;
import com.odoo.core.utils.OResource;
import com.odoo.core.utils.OdooRecordUtils;
import com.odoo.core.utils.logger.OLog;
import com.odoo.datas.OConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OSyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String TAG = OSyncAdapter.class.getSimpleName();
    private Context mContext;
    private App app = null;
    private Odoo mOdoo;
    private OModel mModel;
    private OSyncService mService;
    private OSyncDataUtils dataUtils;
    private OPreferenceManager preferenceManager;
    private Class<? extends OModel> mModelClass;
    private Integer mSyncDataLimit = 0;
    private Boolean checkForWriteCreateDate = true;
    private HashMap<String, ODomain> mDomain = new HashMap<>();
    private HashMap<String, ISyncFinishListener> mSyncFinishListeners = new HashMap<>();

    public OSyncAdapter(Context context, Class<? extends OModel> model, OSyncService service,
                        boolean autoInitialize) {
        super(context, autoInitialize);
        init(context, model, service);
    }

    public OSyncAdapter(Context context, Class<? extends OModel> model, OSyncService service,
                        boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        init(context, model, service);
    }

    private void init(Context context, Class<? extends OModel> model, OSyncService service) {
        mContext = context;
        mModelClass = model;
        mService = service;
        preferenceManager = new OPreferenceManager(mContext);
        app = (App) context.getApplicationContext();
    }

    public OSyncAdapter setDomain(ODomain domain) {
        mDomain.put(mModel.getModelName(), domain);
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
        // Creating model Object
        mModel = new OModel(mContext, null, OdooAccountManager.getDetails(mContext, account.name))
                .createInstance(mModelClass);
        OUser mUser = mModel.getUser();
        if (OdooAccountManager.isValidUserObj(mContext, mUser)) {
            // Creating Odoo instance
            mOdoo = createOdooInstance(mContext, mUser);
            if (mOdoo != null) {
                dataUtils = new OSyncDataUtils(mContext, mOdoo);
                Log.i(TAG, "User        : " + mModel.getUser().getAndroidName());
                Log.i(TAG, "Model       : " + mModel.getModelName());
                Log.i(TAG, "Database    : " + mModel.getDatabaseName());
                Log.i(TAG, "Odoo Version: " + mUser.getOdooVersion().getServerSerie());
                // Calling service callback
                if (mService != null)
                    mService.performDataSync(this, extras, mUser);

                //Creating domain
                ODomain domain = (mDomain.containsKey(mModel.getModelName())) ?
                        mDomain.get(mModel.getModelName()) : null;

                // Ready for sync data from server
                syncData(mModel, mUser, domain, syncResult, true, true);
            } else {
                Log.e(TAG, "Unable to connect with Odoo Server.");
            }
        }
    }

    private void syncData(OModel model, OUser user, ODomain domain_filter,
                          SyncResult result, Boolean checkForDataLimit, Boolean createRelationRecords) {
        Log.v(TAG, "Sync for (" + model.getModelName() + ") Started at " + ODateUtils.getDate());
        model.onSyncStarted();
        try {
            ODomain domain = new ODomain();
            domain.append(model.defaultDomain());
            if (domain_filter != null) {
                domain.append(domain_filter);
            }

            if (checkForWriteCreateDate) {
                List<Integer> serverIds = model.getServerIds();
                // Model Create date domain filters
                if (model.checkForCreateDate() && checkForDataLimit) {
                    if (serverIds.size() > 0) {
                        if (model.checkForWriteDate()
                                && !model.isEmptyTable()) {
                            domain.add("|");
                        }
                        if (model.checkForWriteDate() && !model.isEmptyTable()
                                && createRelationRecords && model.getLastSyncDateTime() != null)
                            domain.add("&");
                    }
                    int data_limit = preferenceManager.getInt("sync_data_limit", 60);
                    domain.add("create_date", ">=", ODateUtils.getDateBefore(data_limit));
                    if (serverIds.size() > 0) {
                        domain.add("id", "not in", serverIds);
                    }
                }
                // Model write date domain filters
                if (model.checkForWriteDate() && !model.isEmptyTable() && createRelationRecords) {
                    String last_sync_date = model.getLastSyncDateTime();
                    if (last_sync_date != null) {
                        domain.add("write_date", ">", last_sync_date);
                    }
                }
            }
            // Getting data
            OdooResult response = mOdoo
                    .withRetryPolicy(OConstants.RPC_REQUEST_TIME_OUT, OConstants.RPC_REQUEST_RETRIES)
                    .searchRead(model.getModelName(), getFields(model)
                            , domain, 0, mSyncDataLimit, "create_date DESC");
            if (response == null) {
                // FIXME: Check in library. May be timeout issue with slow network.
                Log.w(TAG, "Response null from server.");
                model.onSyncTimedOut();
                return;
            }
            if (response.containsKey("error")) {
                app.setOdoo(null, user);
                OPreferenceManager pref = new OPreferenceManager(mContext);
                if (pref.getBoolean(About.DEVELOPER_MODE, false)) {
                    OdooResult error = response.getMap("error");
                    OLog.log("ERROR ERROR :(" + error);
                }
                return;
            }

            Log.v(TAG, "Processing " + response.getRecords().size() + " records");
            dataUtils.handleResult(model, user, result, response, createRelationRecords);
            // Updating records on server if local are latest updated.
            // if model allowed update record to server
            if (model.allowUpdateRecordOnServer()) {
                dataUtils.updateRecordsOnServer(this);
            }

            // Creating or updating relation records
            handleRelationRecords(user, dataUtils.getRelationRecordsHashMap(), result);

            // If model allowed to create record on server
            if (model.allowCreateRecordOnServer()) {
                createRecordsOnServer(model);
            }

            // If model allowed to delete record on server
            if (model.allowDeleteRecordOnServer()) {
                removeRecordOnServer(model);
            }

            // If model allowed to delete server removed record from local database
            if (model.allowDeleteRecordInLocal()) {
                removeNonExistRecordFromLocal(model);
            }

            Log.v(TAG, "Sync for (" + model.getModelName() + ") finished at " + ODateUtils.getDate());
            if (createRelationRecords) {
                IrModel irModel = new IrModel(mContext, user);
                irModel.setLastSyncDateTimeToNow(model);
            }
            model.onSyncFinished();
        } catch (Exception e) {
            e.printStackTrace();
            model.onSyncFailed();
        }
        // Performing next sync if any in service
        if (mSyncFinishListeners.containsKey(model.getModelName())) {
            OSyncAdapter adapter = mSyncFinishListeners.get(model.getModelName())
                    .performNextSync(user, result);
            mSyncFinishListeners.remove(model.getModelName());
            if (adapter != null) {
                SyncResult syncResult = new SyncResult();
                OModel syncModel = model.createInstance(adapter.getModelClass());
                ContentProviderClient contentProviderClient =
                        mContext.getContentResolver().acquireContentProviderClient(syncModel.authority());
                adapter.onPerformSync(user.getAccount(), null, syncModel.authority(),
                        contentProviderClient, syncResult);
            }
        }
        model.close();
    }

    private void handleRelationRecords(OUser user,
                                       HashMap<String, OSyncDataUtils.SyncRelationRecords> relationRecords,
                                       SyncResult result) {
        for (String key : relationRecords.keySet()) {
            OSyncDataUtils.SyncRelationRecords record = relationRecords.get(key);
            OModel model = record.getBaseModel();
            OModel rel_model = model.createInstance(record.getRelationModel());
            model.close();

            // Skipping blank sync request if there is no any ids to sync.
            if (!record.getUniqueIds().isEmpty()) {
                ODomain domain = new ODomain();
                domain.add("id", "in", record.getUniqueIds());
                syncData(rel_model, user, domain, result, false, false);
            }
            // Updating manyToOne record with their relation record row_id
            switch (record.getRelationType()) {
                case ManyToOne:
                    // Nothing to do. Already added link with record relation
                    break;
                case OneToMany:
                    // Update related_column with base id's row_id for each of record ids
                    String related_column = record.getRelatedColumn();
                    for (Integer id : record.getUniqueIds()) {
                        OValues values = new OValues();
                        ODataRow rec = rel_model.browse(rel_model.selectRowId(id));
                        values.put(related_column, rec.getInt(related_column));
                        values.put("_is_dirty", "false");
                        rel_model.update(rel_model.selectRowId(id), values);
                    }
                    break;
                case ManyToMany:
                    // Nothing to do. Already added relation records links
                    break;
            }
            rel_model.close();
        }
    }

    public static Odoo createOdooInstance(final Context context, final OUser user) {
        final App app = (App) context.getApplicationContext();
        Odoo odoo = app.getOdoo(user);
        try {
            if (odoo == null) {
                odoo = Odoo.createQuickInstance(context, user.getHost());
                OUser mUser = odoo
                        .withRetryPolicy(OConstants.RPC_REQUEST_TIME_OUT, OConstants.RPC_REQUEST_RETRIES)
                        .authenticate(user.getUsername(), user.getPassword(), user.getDatabase());
                app.setOdoo(odoo, user);
                if (mUser != null) {
                    ResCompany company = new ResCompany(context, user);
                    if (company.count("id = ? ", new String[]{user.getCompanyId() + ""}) <= 0) {
                        ODataRow company_details = new ODataRow();
                        company_details.put("id", user.getCompanyId());
                        company.quickCreateRecord(company_details);
                    }
                } else {
                    // FIXME: Unable to get user object or may be due session destroyed with Odoo Saas (single connection support only)
                    Log.e(TAG, OResource.string(context, R.string.toast_something_gone_wrong));
                }
            }
        } catch (OdooVersionException e) {
            e.printStackTrace();
        }
        return odoo;
    }

    private OdooFields getFields(OModel model) {
        OdooFields fields = new OdooFields();
        List<String> names = new ArrayList<>();
        for (OColumn column : model.getColumns(false)) {
            names.add(column.getSyncColumn());
        }
        fields.addAll(names.toArray(new String[names.size()]));
        return fields;
    }

    /**
     * Creates locally created record on server (id with zero)
     *
     * @param model model object
     */
    private void createRecordsOnServer(OModel model) {
        List<ODataRow> records = model.select(null,
                "(id = ? or id = ?)", new String[]{"0", "false"});
        int counter = 0;
        for (ODataRow record : records) {
            if (validateRelationRecords(model, record)) {
                /*
                 Need to check server id for record.
                 It is possible that record created on server by validating main record.
                 */
                if (model.selectServerId(record.getInt(OColumn.ROW_ID)) == 0) {
                    int id = createOnServer(model, OdooRecordUtils.createRecordValues(model, record));
                    if (id != OModel.INVALID_ROW_ID) {
                        OValues values = new OValues();
                        values.put("id", id);
                        values.put("_is_dirty", "false");
                        values.put("_write_date", ODateUtils.getUTCDate());
                        model.update(record.getInt(OColumn.ROW_ID), values);
                        counter++;
                    } else {
                        Log.e(TAG, "Unable to create record on server.");
                    }
                }
            }
        }
        if (counter == records.size()) {
            Log.i(TAG, counter + " records created on server.");
        }
    }

    /**
     * Validate relation record for the record. And if relation record not created on server.
     * It will be created on server before syncing original record
     *
     * @param model
     * @param row
     * @return updatedRow
     */
    public boolean validateRelationRecords(OModel model, ODataRow row) {
        Log.d(TAG, "Validating relation records for record");
        // Check for relation local record
        for (OColumn column : model.getRelationColumns()) {
            OModel relModel = model.createInstance(column.getType());
            switch (column.getRelationType()) {
                case ManyToOne:
                    if (!row.getString(column.getName()).equals("false")) {
                        ODataRow m2oRec = row.getM2ORecord(column.getName()).browse();
                        if (m2oRec != null && m2oRec.getInt("id") == 0) {
                            int new_id = relModel.getServerDataHelper().createOnServer(
                                    OdooRecordUtils.createRecordValues(relModel, m2oRec));
                            updateRecordServerId(relModel, m2oRec.getInt(OColumn.ROW_ID), new_id);
                        }
                    }
                    break;
                case ManyToMany:
                    List<ODataRow> m2mRecs = row.getM2MRecord(column.getName()).browseEach();
                    if (!m2mRecs.isEmpty()) {
                        for (ODataRow m2mRec : m2mRecs) {
                            if (m2mRec.getInt("id") == 0) {
                                int new_id = relModel.getServerDataHelper().createOnServer(
                                        OdooRecordUtils.createRecordValues(relModel, m2mRec));
                                updateRecordServerId(relModel, m2mRec.getInt(OColumn.ROW_ID), new_id);
                            }
                        }
                    }
                    break;
                case OneToMany:
                    List<ODataRow> o2mRecs = row.getO2MRecord(column.getName()).browseEach();
                    if (!o2mRecs.isEmpty()) {
                        for (ODataRow o2mRec : o2mRecs) {
                            if (o2mRec.getInt("id") == 0) {
                                int new_id = relModel.getServerDataHelper().createOnServer(
                                        OdooRecordUtils.createRecordValues(relModel, o2mRec));
                                updateRecordServerId(relModel, o2mRec.getInt(OColumn.ROW_ID), new_id);
                            }
                        }
                    }
                    break;
            }
        }
        return true;
    }

    /**
     * Updating local record with server id
     *
     * @param model
     * @param row_id
     * @param server_id
     */
    private void updateRecordServerId(OModel model, int row_id, int server_id) {
        OValues values = new OValues();
        values.put("id", server_id);
        values.put("_is_dirty", "false");
        model.update(row_id, values);
    }

    private int createOnServer(OModel model, ORecordValues values) {
        int id = OModel.INVALID_ROW_ID;
        try {
            if (values != null) {
                OdooResult result = mOdoo.createRecord(model.getModelName(), values);
                id = result.getInt("result");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    /**
     * Removes record on server if local record is not active
     *
     * @param model
     */
    private void removeRecordOnServer(OModel model) {
        List<ODataRow> records = model.select(new String[]{},
                "id != ? and _is_active = ?", new String[]{"0", "false"});
        List<Integer> serverIds = new ArrayList<>();
        for (ODataRow record : records) {
            serverIds.add(record.getInt("id"));
        }
        if (serverIds.size() > 0) {
            if (removeRecordsFromServer(model, serverIds)) {
                int counter = model.deleteRecords(serverIds, true);
                Log.i(TAG, counter + " records removed from server and local database");
            } else {
                Log.e(TAG, "Unable to remove records from server");
            }
        }
    }

    private boolean removeRecordsFromServer(OModel model, List<Integer> serverIds) {
        try {
            mOdoo.unlinkRecord(model.getModelName(), serverIds);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Removes non exist record from local database
     *
     * @param model
     */
    private void removeNonExistRecordFromLocal(OModel model) {
        List<Integer> ids = model.getServerIds();
        try {
            ODomain domain = new ODomain();
            domain.add("id", "in", ids);
            OdooFields fields = new OdooFields();
            fields.addAll(new String[]{"id"});
            OdooResult result = mOdoo.searchRead(model.getModelName(), fields, domain, 0, 0, null);
            List<OdooRecord> records = result.getRecords();
            if (!records.isEmpty()) {
                for (OdooRecord record : records) {
                    ids.remove(ids.indexOf(record.getInt("id")));
                }
            }
            int removedCounter = 0;
            if (ids.size() > 0) {
                removedCounter = model.deleteRecords(ids, true);
            }
            Log.i(TAG, removedCounter + " Records removed from local database.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Class<? extends OModel> getModelClass() {
        return mModelClass;
    }

    public void setModel(OModel model) {
        mModel = model;
    }

    public OModel getModel() {
        return mModel;
    }

    public OSyncAdapter onSyncFinish(ISyncFinishListener syncFinish) {
        mSyncFinishListeners.put(mModel.getModelName(), syncFinish);
        return this;
    }
}
