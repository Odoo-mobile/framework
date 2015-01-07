/**
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

import com.odoo.core.auth.OdooAccountManager;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.support.OUser;
import com.odoo.core.utils.JSONUtils;
import com.odoo.core.utils.ODateUtils;
import com.odoo.core.utils.OPreferenceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import odoo.ODomain;
import odoo.Odoo;

public class OSyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String TAG = OSyncAdapter.class.getSimpleName();

    private Context mContext;
    private Class<? extends OModel> mModelClass;
    private OModel mModel;
    private OSyncService mService;
    private OUser mUser;
    private Boolean checkForWriteCreateDate = true;
    private Integer mSyncDataLimit = 0;
    private HashMap<String, ODomain> mDomain = new HashMap<>();
    private OPreferenceManager preferenceManager;
    private Odoo mOdoo;
    private HashMap<String, List<Integer>> relationDataIds = new HashMap<>();

    public OSyncAdapter(Context context, Class<? extends OModel> model, OSyncService service, boolean autoInitialize) {
        super(context, autoInitialize);
        init(context, model, service);
    }

    public OSyncAdapter(Context context, Class<? extends OModel> model, OSyncService service, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        init(context, model, service);
    }

    private void init(Context context, Class<? extends OModel> model, OSyncService service) {
        mContext = context;
        mModelClass = model;
        mService = service;
        preferenceManager = new OPreferenceManager(mContext);
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
        mUser = mModel.getUser();
        // Creating Odoo instance
        mOdoo = createOdooInstance(mUser);
        Log.i(TAG, "User        : " + mModel.getUser().getAndroidName());
        Log.i(TAG, "Model       : " + mModel.getModelName());
        Log.i(TAG, "Database    : " + mModel.getDatabaseName());
        Log.i(TAG, "Odoo Version: " + mUser.getVersion_number());
        // Calling service callback
        mService.performDataSync(this, extras, mUser);

        //Creating domain
        ODomain domain = (mDomain.containsKey(mModel.getModelName())) ?
                mDomain.get(mModel.getModelName()) : null;

        // Ready for sync data from server
        syncData(mModel, mUser, domain, syncResult, true, true);
    }

    private void syncData(OModel model, OUser user, ODomain domain,
                          SyncResult result, Boolean checkForDataLimit, Boolean createRelationRecords) {
        Log.v(TAG, "Sync for (" + model.getModelName() + ") Started at " + ODateUtils.getDate());
        try {

            if (domain == null) {
                domain = new ODomain();
            }
            domain.append(model.defaultDomain());

            if (checkForWriteCreateDate) {
                // Model Create date domain filters
                if (model.checkForCreateDate() && checkForDataLimit) {
                    List<Integer> serverIds = model.getServerIds();
                    if (serverIds.size() > 0) {
                        if (model.checkForWriteDate()
                                && !model.isEmptyTable()) {
                            domain.add("|");
                        }
                        if (createRelationRecords)
                            domain.add("&");
                    }
                    int data_limit = preferenceManager.getInt("sync_data_limit", 60);
                    domain.add("create_date", ">=", ODateUtils.getDateBefore(data_limit));
                    if (serverIds.size() > 0) {
                        domain.add("id", "not in", new JSONArray(serverIds.toString()));
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
            JSONObject response = mOdoo.search_read(model.getModelName(),
                    getFields(model), domain.get(), 0, mSyncDataLimit, "create_date", "DESC");
            OSyncDataUtils dataUtils = new OSyncDataUtils(mContext, mOdoo, model, user, response,
                    result, createRelationRecords);
            // Updating records on server if local are latest updated.
            // if model allowed update record to server
            if (model.allowUpdateRecordOnServer()) {
                dataUtils.updateRecordsOnServer();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.v(TAG, "Sync for (" + model.getModelName() + ") finished at " + ODateUtils.getDate());
        model.setLastSyncDateTimeToNow();
    }


    private void handleRelationRecords(OUser user,
                                       HashMap<String, OSyncDataUtils.SyncRelationRecords> relationRecords,
                                       SyncResult result) {
        for (String key : relationRecords.keySet()) {
            OSyncDataUtils.SyncRelationRecords record = relationRecords.get(key);
            OModel model = record.getBaseModel();
            OModel rel_model = record.getRelationModel();
            ODomain domain = new ODomain();
            domain.add("id", "in", record.getUniqueIds());
            syncData(rel_model, user, domain, result, false, false);
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
        }
    }

    private Odoo createOdooInstance(OUser user) {
        try {
            Odoo odoo = new Odoo(mContext, user.isOAauthLogin() ? user.getInstanceUrl() : user.getHost()
                    , user.isAllowSelfSignedSSL());
            odoo.authenticate(user.getUsername(), user.getPassword(), user.getDatabase());
            return odoo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private JSONObject getFields(OModel model) {
        JSONObject fields = new JSONObject();
        try {
            for (OColumn column : model.getColumns(false)) {
                fields.accumulate("fields", column.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            int id = createOnServer(model, JSONUtils.createJSONValues(model, record));
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
        if (counter == records.size()) {
            Log.i(TAG, counter + " records created on server.");
        }
    }

    private int createOnServer(OModel model, JSONObject values) {
        int id = OModel.INVALID_ROW_ID;
        try {
            if (values != null) {
                JSONObject response = mOdoo.createNew(model.getModelName(), values);
                id = response.getInt("result");
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
        if (removeRecordsFromServer(model, serverIds)) {
            int counter = model.deleteRecords(serverIds, true);
            Log.i(TAG, counter + " records removed from server and local database");
        } else {
            Log.e(TAG, "Unable to remove records from server");
        }
    }

    private boolean removeRecordsFromServer(OModel model, List<Integer> serverIds) {
        try {
            mOdoo.unlink(model.getModelName(), serverIds);
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
            domain.add("id", "in", new JSONArray(ids.toString()));
            JSONObject result = mOdoo.search_read(model.getModelName(),
                    new JSONObject(), domain.get());
            JSONArray records = result.getJSONArray("records");
            if (records.length() > 0) {
                for (int i = 0; i < records.length(); i++) {
                    JSONObject record = records.getJSONObject(i);
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
}
