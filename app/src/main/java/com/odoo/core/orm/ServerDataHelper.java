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
 * Created on 7/1/15 6:23 PM
 */
package com.odoo.core.orm;

import android.content.Context;

import com.google.gson.internal.LinkedTreeMap;
import com.odoo.App;
import com.odoo.core.rpc.Odoo;
import com.odoo.core.rpc.helper.OArguments;
import com.odoo.core.rpc.helper.ODomain;
import com.odoo.core.rpc.helper.ORecordValues;
import com.odoo.core.rpc.helper.OdooFields;
import com.odoo.core.rpc.helper.utils.gson.OdooRecord;
import com.odoo.core.rpc.helper.utils.gson.OdooResult;
import com.odoo.core.service.OSyncAdapter;
import com.odoo.core.support.OUser;
import com.odoo.core.utils.OdooRecordUtils;
import com.odoo.datas.OConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServerDataHelper {
    public static final String TAG = ServerDataHelper.class.getSimpleName();
    private OModel mModel;
    private Context mContext;
    private Odoo mOdoo;
    private App mApp;

    public ServerDataHelper(Context context, OModel model, OUser user) {
        mContext = context;
        mModel = model;
        mApp = (App) mContext.getApplicationContext();
        mOdoo = mApp.getOdoo(user);
        if (mOdoo == null)
            mOdoo = OSyncAdapter.createOdooInstance(mContext, model.getUser());
    }

    public List<ODataRow> nameSearch(String name, ODomain domain, int limit) {
        List<ODataRow> items = new ArrayList<>();
        if (mApp.inNetwork()) {
            OdooResult result = mOdoo
                    .withRetryPolicy(OConstants.RPC_REQUEST_TIME_OUT, OConstants.RPC_REQUEST_RETRIES)
                    .nameSearch(mModel.getModelName(), name, domain, limit);
            if (!result.getRecords().isEmpty()) {
                for (OdooRecord record : result.getRecords()) {
                    // FIXME : What response i'll get. I DON'T KNOW YET
                        /*ODataRow row = new ODataRow();
                        row.put("id", record.getInt("id"));
                        row.put(mModel.getDefaultNameColumn(), record.getString("name"));
                        items.add(row); */
                }
            }
        }
        return items;
    }

    public OdooResult read(com.odoo.core.rpc.helper.OdooFields fields, int id) {
        if (mApp.inNetwork()) {
            OdooResult result = mOdoo
                    .withRetryPolicy(OConstants.RPC_REQUEST_TIME_OUT, OConstants.RPC_REQUEST_RETRIES)
                    .read(mModel.getModelName(), id, fields);
            if (mOdoo.getVersion().getVersionNumber() >= 10) {
                if (result.containsKey("result") && result.get("result") instanceof ArrayList) {
                    LinkedTreeMap record = (LinkedTreeMap) result.getArray("result").get(0);
                    OdooResult odooResult = new OdooResult();
                    odooResult.putAll(record);
                    return odooResult;
                }
            } else {
                return result;
            }
        }
        return null;
    }

    public List<ODataRow> searchRecords(OdooFields fields, ODomain domain, int limit) {
        List<ODataRow> items = new ArrayList<>();
        if (mApp.inNetwork()) {
            OdooResult result = mOdoo
                    .withRetryPolicy(OConstants.RPC_REQUEST_TIME_OUT, OConstants.RPC_REQUEST_RETRIES)
                    .searchRead(mModel.getModelName(), fields, domain, 0, limit, null);
            if (result != null && !result.getRecords().isEmpty()) {
                for (OdooRecord record : result.getRecords()) {
                    items.add(OdooRecordUtils.toDataRow(record));
                }
            }
        }
        return items;
    }

    public Odoo getOdoo() {
        return mOdoo;
    }

    public Object callMethod(String method, OArguments args) {
        return callMethod(method, args, null, null);
    }

    public Object callMethod(String method, OArguments args, HashMap<String, Object> context) {
        return callMethod(mModel.getModelName(), method, args, context, null);
    }

    public Object callMethod(String method, OArguments args,
                             HashMap<String, Object> context, HashMap<String, Object> kwargs) {
        return callMethod(mModel.getModelName(), method, args, context, kwargs);
    }

    public Object callMethod(String model, String method, OArguments args,
                             HashMap<String, Object> context, HashMap<String, Object> kwargs) {
        if (context != null) {
            args.add(mOdoo.updateContext(context));
        }
        OdooResult result = mOdoo
                .withRetryPolicy(OConstants.RPC_REQUEST_TIME_OUT, OConstants.RPC_REQUEST_RETRIES)
                .callMethod(model, method, args, kwargs, context);
        if (result.has("result")) {
            return result.get("result");
        }
        return false;
    }


    public int createOnServer(ORecordValues data) {
        OdooResult result = mOdoo.createRecord(mModel.getModelName(), data);
        return result.getInt("result");
    }

    public int updateOnServer(ORecordValues data, Integer id) {
        mOdoo.updateRecord(mModel.getModelName(), data, id);
        return mModel.selectRowId(id);
    }

    public OdooResult executeWorkFlow(int server_id, String signal) {
        return mOdoo.executeWorkFlow(mModel.getModelName(), server_id, signal);
    }
}
