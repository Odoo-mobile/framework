package com.openerp.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
	SharedPreferences mPref = null;

	public PreferenceManager(Context context) {
		mPref = android.preference.PreferenceManager
				.getDefaultSharedPreferences(context);
	}

	public int getInt(String key, int default_value) {
		return Integer.parseInt(mPref.getString(key, default_value + ""));
	}
}
