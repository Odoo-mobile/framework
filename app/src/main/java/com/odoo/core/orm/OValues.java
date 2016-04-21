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
 * Created on 31/12/14 6:49 PM
 */
package com.odoo.core.orm;

import android.content.ContentValues;

import com.odoo.core.utils.OObjectUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OValues implements Serializable {
    public static final String TAG = OValues.class.getSimpleName();
    private HashMap<String, Object> _values = new HashMap<>();

    public OValues() {
        _values.clear();
        _values = new HashMap<>();
    }

    public void put(String key, Object value) {
        _values.put(key, value);
    }

    /**
     * Used for adding chained records (ids or OValues) for relation column M2M and O2M
     *
     * @param values Relation records
     */
    public void put(String key, RelValues values) {
        _values.put(key, values);
    }

    public Object get(String key) {
        return _values.get(key);
    }

    public long getLong(String key) {
        if (_values.get(key).toString().equals("false")) {
            return -1;
        }
        return Long.parseLong(_values.get(key).toString());
    }

    public Integer getInt(String key) {
        if (_values.get(key).toString().equals("false")) {
            return -1;
        }
        return Integer.parseInt(_values.get(key).toString());
    }

    public String getString(String key) {
        return _values.get(key).toString();
    }

    public Boolean getBoolean(String key) {
        return Boolean.parseBoolean(_values.get(key).toString());
    }

    public boolean contains(String key) {
        return _values.containsKey(key);
    }

    public List<String> keys() {
        List<String> list = new ArrayList<>();
        list.addAll(_values.keySet());
        return list;
    }

    public void setAll(OValues values) {
        for (String key : values.keys())
            _values.put(key, values.get(key));
    }

    public int size() {
        return _values.size();
    }

    @Override
    public String toString() {
        return _values.toString();
    }

    public ODataRow toDataRow() {
        ODataRow row = new ODataRow();
        row.addAll(_values);
        return row;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        for (String key : _values.keySet()) {
            Object val = _values.get(key);
            val = (val == null) ? "false" : val;
            if (val instanceof OValues || val instanceof RelValues) {
                // Converting values to byte so we can pass it to ContentValues
                try {
                    // Possible values: record (M2O) or list of records (M2M, O2M).
                    val = OObjectUtils.objectToByte(val);
                    values.put(key, (byte[]) val);
                } catch (IOException e) {
                    e.printStackTrace();
                    values.put(key, "false");
                }
            } else if (val instanceof byte[]) {
                values.put(key, (byte[]) val);
            } else {
                values.put(key, val.toString());
            }
        }
        return values;
    }

    public void addAll(HashMap<String, Object> data) {
        _values.putAll(data);
    }

    public static OValues from(ContentValues contentValues) {
        OValues values = new OValues();
        for (String key : contentValues.keySet()) {
            values.put(key, contentValues.get(key));
        }
        return values;
    }

}
