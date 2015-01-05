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
import com.odoo.core.utils.ODateUtils;
import com.odoo.core.utils.OPreferenceManager;

import org.json.JSONArray;
import org.json.JSONObject;

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
            // TODO: Update record on server
            // dataUtils.updateRecordsOnServer();

            // Creating relation records
            handleRelationRecords(user, dataUtils.getRelationRecordsHashMap(), result);
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
}
