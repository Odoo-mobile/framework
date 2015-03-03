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
 * Created on 17/12/14 6:06 PM
 */
package com.odoo;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.odoo.core.support.OUser;

import java.util.HashMap;
import java.util.List;

import odoo.Odoo;

public class App extends Application {

    public static final String TAG = App.class.getSimpleName();
    private static HashMap<String, Odoo> mOdooInstances = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public Odoo getOdoo(OUser user) {
        if (mOdooInstances.containsKey(user.getAndroidName())) {
            return mOdooInstances.get(user.getAndroidName());
        }
        return null;
    }

    public void setOdoo(Odoo odoo, OUser user) {
        if (user != null)
            mOdooInstances.put(user.getAndroidName(), odoo);
    }

    /**
     * Checks for network availability
     *
     * @return true, if network available
     */
    public boolean inNetwork() {
        boolean isConnected = false;
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = manager.getActiveNetworkInfo();
        if (nInfo != null && nInfo.isConnectedOrConnecting()) {
            isConnected = true;
        }
        return isConnected;
    }

    /**
     * Checks for installed application
     *
     * @param appPackage
     * @return true, if application installed on device
     */
    public boolean appInstalled(String appPackage) {
        boolean mInstalled = false;
        try {
            PackageManager mPackage = getPackageManager();
            mPackage.getPackageInfo(appPackage, PackageManager.GET_ACTIVITIES);
            mInstalled = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mInstalled;
    }

    /**
     * Check for app is on top of screen
     *
     * @return true, if application is running on top
     */
    public boolean meOnTop() {
        boolean meOnTop = false;
        ActivityManager aManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = aManager.getRunningTasks(1);
        ComponentName componentName = taskInfo.get(0).topActivity;
        if (componentName.getPackageName().equalsIgnoreCase(getPackageName())) {
            meOnTop = true;
        }
        return meOnTop;
    }


}
