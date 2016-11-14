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
 * Created on 27/4/15 3:14 PM
 */
package com.odoo.core.rpc.helper;

import android.content.ContentValues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ORecordValues extends HashMap<String, Object> {

    public long getLong(String key) {
        if (get(key).toString().equals("false")) {
            return -1;
        }
        return Long.parseLong(get(key).toString());
    }

    public Integer getInt(String key) {
        if (get(key).toString().equals("false")) {
            return -1;
        }
        return Integer.parseInt(get(key).toString());
    }

    public String getString(String key) {
        return get(key).toString();
    }

    public Boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key).toString());
    }

    public boolean contains(String key) {
        return containsKey(key);
    }

    public List<String> keys() {
        List<String> list = new ArrayList<>();
        list.addAll(keySet());
        return list;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        for (String key : keySet()) {
            Object val = get(key);
            val = (val == null) ? "false" : val;
            values.put(key, val.toString());
        }
        return values;
    }

    public void addAll(HashMap<String, Object> data) {
        putAll(data);
    }
}