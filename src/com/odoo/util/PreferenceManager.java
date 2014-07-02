/*
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
 */
package com.odoo.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferenceManager {
	SharedPreferences mPref = null;

	public PreferenceManager(Context context) {
		mPref = android.preference.PreferenceManager
				.getDefaultSharedPreferences(context);
	}

	public void putString(String key, String value) {
		Editor editor = mPref.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public void putStringSet(String key, List<String> values) {
		Editor editor = mPref.edit();
		Set<String> vals = new HashSet<String>(values);
		editor.putStringSet(key, vals);
		editor.commit();
	}

	public List<String> getStringSet(String key) {
		List<String> list = new ArrayList<String>();
		Set<String> vals = mPref.getStringSet(key, null);
		if (vals != null)
			list.addAll(vals);
		return list;
	}

	public String getString(String key, String default_value) {
		return mPref.getString(key, default_value);
	}

	public int getInt(String key, int default_value) {
		return Integer.parseInt(mPref.getString(key, default_value + ""));
	}

}
