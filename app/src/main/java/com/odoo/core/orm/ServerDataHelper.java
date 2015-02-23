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
 * Created on 7/1/15 6:23 PM
 */
package com.odoo.core.orm;

import android.content.Context;

import com.odoo.App;
import com.odoo.core.service.OSyncAdapter;
import com.odoo.core.support.OUser;
import com.odoo.core.support.OdooFields;
import com.odoo.core.utils.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import odoo.OArguments;
import odoo.ODomain;
import odoo.Odoo;

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
        try {
            if (mApp.inNetwork()) {
                JSONObject kwargs = new JSONObject();
                kwargs.put("name", name);
                kwargs.put("args", domain.getArray());
                kwargs.put("operator", "ilike");
                JSONArray records = (JSONArray) callMethod("name_search", new OArguments(),
                        null, kwargs);
                if (records.length() > 0) {
                    for (int i = 0; i < records.length(); i++) {
                        ODataRow row = new ODataRow();
                        JSONArray record = records.getJSONArray(i);
                        row.put("id", record.get(0));
                        row.put(mModel.getDefaultNameColumn(), record.get(1));
                        items.add(row);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    public List<ODataRow> searchRecords(OdooFields fields, ODomain domain, int limit) {
        List<ODataRow> items = new ArrayList<>();
        try {
            if (mApp.inNetwork()) {
                JSONObject result = mOdoo.search_read(mModel.getModelName(),
                        fields.get(), domain.get(), 0, limit, null, null);
                JSONArray records = result.getJSONArray("records");
                if (records.length() > 0) {
                    for (int i = 0; i < records.length(); i++) {
                        items.add(JSONUtils.toDataRow(records.getJSONObject(i)));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    public Odoo getOdoo() {
        return mOdoo;
    }

    public Object executeWorkFlow(int server_id, String signal) {
        try {
            return mOdoo.exec_workflow(mModel.getModelName(), server_id, signal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object callMethod(String method, OArguments args) {
        return callMethod(method, args, null, null);
    }

    public Object callMethod(String method, OArguments args, JSONObject context) {
        return callMethod(mModel.getModelName(), method, args, context, null);
    }

    public Object callMethod(String method, OArguments args,
                             JSONObject context, JSONObject kwargs) {
        return callMethod(mModel.getModelName(), method, args, context, kwargs);
    }

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


    public int createOnServer(JSONObject data) {
        try {
            JSONObject result = mOdoo.createNew(mModel.getModelName(), data);
            return result.getInt("result");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return OModel.INVALID_ROW_ID;
    }

    public int updateOnServer(JSONObject data, Integer id) {
        try {
            if (mOdoo.updateValues(mModel.getModelName(), data, id)) {
                return mModel.selectRowId(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return OModel.INVALID_ROW_ID;
    }
}
