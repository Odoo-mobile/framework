package com.odoo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

public class BasePrefs {

    private final String TAG;
    private Context mContext;
    private SharedPreferences mPreferences;

    /**
     * @param TAG     name for SharedPreferences
     * @param context context
     */
    public BasePrefs(@NonNull final String TAG, @NonNull final Context context) {
        this.TAG = TAG;
        mContext = context;
        mPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
    }

    public String getTAG() {
        return TAG;
    }

    public Context getContext() {
        return mContext;
    }

    protected boolean getBoolean(String key) {
        return mPreferences.getBoolean(key, false);
    }

    protected boolean getBoolean(String key, boolean defValue) {
        return mPreferences.getBoolean(key, defValue);
    }

    protected void putBoolean(String key, boolean value) {
        mPreferences.edit().putBoolean(key, value).apply();
    }

    protected int getInt(String key) {
        return mPreferences.getInt(key, -1);
    }

    protected int getInt(String key, int defValue) {
        return mPreferences.getInt(key, defValue);
    }

    protected void putInt(String key, int value) {
        mPreferences.edit().putInt(key, value).apply();
    }

    protected String getString(String key) {
        return mPreferences.getString(key, null);
    }

    protected String getString(String key, String defValue) {
        return mPreferences.getString(key, defValue);
    }

    protected void putString(String key, String value) {
        mPreferences.edit().putString(key, value).apply();
    }

    public void clear() {
        mPreferences.edit().clear().apply();
    }
}
