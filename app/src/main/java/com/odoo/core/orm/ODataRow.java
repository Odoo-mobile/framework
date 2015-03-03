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
 * Created on 31/12/14 6:47 PM
 */
package com.odoo.core.orm;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.odoo.core.orm.fields.OColumn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ODataRow implements Parcelable {
    public static final String TAG = ODataRow.class.getSimpleName();

    HashMap<String, Object> _data = new HashMap<>();

    public void put(String key, Object value) {
        _data.put(key, value);
    }

    public Object get(String key) {
        return _data.get(key);
    }

    public Integer getInt(String key) {
        if (_data.get(key).toString().equals("false"))
            return 0;
        else
            return Integer.parseInt(_data.get(key).toString());
    }

    public Float getFloat(String key) {
        return Float.parseFloat(_data.get(key).toString());
    }

    public String getString(String key) {
        if (_data.containsKey(key) && _data.get(key) != null)
            return _data.get(key).toString();
        else
            return "false";
    }

    public Boolean getBoolean(String key) {
        return Boolean.parseBoolean(_data.get(key).toString());
    }


    public OM2ORecord getM2ORecord(String key) {
        return (OM2ORecord) _data.get(key);
    }

    public OM2MRecord getM2MRecord(String key) {
        return (OM2MRecord) _data.get(key);
    }

    public OO2MRecord getO2MRecord(String key) {
        return (OO2MRecord) _data.get(key);
    }

    public List<Object> values() {
        List<Object> values = new ArrayList<>();
        values.addAll(_data.values());
        return values;
    }

    public List<String> keys() {
        List<String> list = new ArrayList<>();
        list.addAll(_data.keySet());
        return list;
    }

    public boolean contains(String key) {
        return _data.containsKey(key);
    }

    public int size() {
        return _data.size();
    }

    @Override
    public String toString() {
        return _data.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    public class IdName {
        Integer id;
        String name;

        public IdName(Integer id, String name) {
            super();
            this.id = id;
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    public void addAll(HashMap<String, Object> data) {
        _data.putAll(data);
    }

    public void addAll(ODataRow row) {
        _data.putAll(row.getAll());
    }

    public HashMap<String, Object> getAll() {
        return _data;
    }

    public OValues toValues() {
        OValues values = new OValues();
        values.addAll(getAll());
        return values;
    }

    public Bundle getPrimaryBundleData() {
        Bundle bundle = new Bundle();
        bundle.putInt("id", getInt("id"));
        bundle.putInt(OColumn.ROW_ID, getInt(OColumn.ROW_ID));
        return bundle;
    }

}
