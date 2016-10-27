package com.odoo.utils;

import android.content.Context;
import android.support.annotation.NonNull;

public class AppPrefs extends BasePrefs {

    private static final String TAG = AppPrefs.class.getSimpleName();

    /**
     * SharedPreference key for storing username & selfHostedURL
     */
    private static final String UserName = "UserName";
    private static final String SelfHosted = "SelfHosted";

    // decides one activity b/w OdooActivity & Odoo2Activity
    private static final String OdooActivity = "OdooActivity";

    /**
     * @param context context
     */
    public AppPrefs(@NonNull Context context) {
        super(TAG, context);
    }

    public String getUserName() {
        return getString(UserName);
    }

    public void setUserName(String userName) {
        putString(UserName, userName);
    }

    public boolean isSelfHosted() {
        return getBoolean(SelfHosted);
    }

    public void setSelfHosted(boolean selfHosted) {
        putBoolean(SelfHosted, selfHosted);
    }

    public String whichOdooActivity() {
        return getString(OdooActivity);
    }

    public void setOdooActivity(String odooActivity) {
        putString(OdooActivity, odooActivity);
    }
}
