/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p>
 * Created on 22/4/15 2:20 PM
 */
package com.odoo.core.rpc.helper;

import android.util.Log;

import com.odoo.core.orm.fields.OColumn;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OdooFields {
    public static final String TAG = OdooFields.class.getSimpleName();
    private JSONObject jFields = new JSONObject();

    public OdooFields(List<OColumn> columns) {
        List<String> fields = new ArrayList<>();
        if (columns != null) {
            for (OColumn column : columns) {
                if (!column.isLocal() && !column.isFunctionalColumn()) {
                    fields.add(column.getName());
                }
            }
        }
        addAll(fields.toArray(new String[fields.size()]));
    }

    public OdooFields(String... fields) {
        if (fields != null && fields.length > 0)
            addAll(fields);
    }

    public void addAll(String[] fields) {
        try {
            for (String field : fields) {
                jFields.accumulate("fields", field);
            }
            if (fields.length == 1) {
                jFields.accumulate("fields", fields[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSONArray getArray() {
        if (jFields.length() != 0) {
            try {
                return jFields.getJSONArray("fields");
            } catch (JSONException e) {
                Log.d(TAG, e.getMessage());
            }
        }
        return new JSONArray();
    }

    public JSONObject get() {
        if (jFields.length() == 0) {
            try {
                jFields.put("fields", new JSONArray());
            } catch (JSONException e) {
                Log.d(TAG, e.getMessage());
            }
        }
        return jFields;
    }

}
