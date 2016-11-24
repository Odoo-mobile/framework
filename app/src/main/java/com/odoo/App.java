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
 * Created on 17/12/14 6:06 PM
 */
package com.odoo;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.odoo.core.orm.ModelRegistryUtils;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.OSQLite;
import com.odoo.core.rpc.Odoo;
import com.odoo.core.support.OUser;

import java.lang.reflect.Constructor;
import java.util.HashMap;

public class App extends Application {

    public static final String TAG = App.class.getSimpleName();
    public static String APPLICATION_NAME;
    private static HashMap<String, Odoo> mOdooInstances = new HashMap<>();
    private static HashMap<String, OSQLite> mSQLiteObjecs = new HashMap<>();
    private static ModelRegistryUtils modelRegistryUtils = new ModelRegistryUtils();

    @Override
    public void onCreate() {
        super.onCreate();
        App.APPLICATION_NAME = getPackageManager().getApplicationLabel(getApplicationInfo()).toString();
        App.modelRegistryUtils.makeReady(getApplicationContext());
    }

    public static OSQLite getSQLite(String userName) {
        return mSQLiteObjecs.containsKey(userName) ? mSQLiteObjecs.get(userName) : null;
    }

    public static void setSQLite(String userName, OSQLite sqLite) {
        mSQLiteObjecs.put(userName, sqLite);
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

    public static <T> T getModel(Context context, String modelName, OUser user) {
        Class<? extends OModel> modelCls = App.modelRegistryUtils.getModel(modelName);
        if (modelCls != null) {
            try {
                Constructor constructor = modelCls.getConstructor(Context.class, OUser.class);
                return (T) constructor.newInstance(context, user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public ModelRegistryUtils getModelRegistry() {
        return modelRegistryUtils;
    }
}
