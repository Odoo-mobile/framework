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
 * Created on 31/12/14 5:39 PM
 */
package com.odoo.core.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OPreferenceManager {
    public static final String TAG = OPreferenceManager.class.getSimpleName();
    private SharedPreferences mPref = null;

    public OPreferenceManager(Context context) {
        mPref = android.preference.PreferenceManager
                .getDefaultSharedPreferences(context);
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public boolean putStringSet(String key, List<String> values) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putStringSet(key, new HashSet<>(values));
        return editor.commit();
    }

    public List<String> getStringSet(String key) {
        List<String> list = new ArrayList<>();
        Set<String> vals = mPref.getStringSet(key, null);
        if (vals != null)
            list.addAll(vals);
        return list;
    }

    public String getString(String key, String default_value) {
        return mPref.getString(key, default_value);
    }

    public boolean putInt(String key, int value) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    public int getInt(String key, int default_value) {
        if (mPref.getAll().get(key) instanceof String) {
            try {
                return Integer.parseInt(mPref.getString(key, default_value + ""));
            } catch (NumberFormatException e) {
                Log.d(TAG, e.getMessage());
                return default_value;
            }
        }
        return mPref.getInt(key, default_value);
    }

    public void setBoolean(String key, Boolean value) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public boolean getBoolean(String key, boolean defValue) {
        return mPref.getBoolean(key, defValue);
    }

}
