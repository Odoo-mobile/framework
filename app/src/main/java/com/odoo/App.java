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
import android.support.annotation.NonNull;
import android.util.Log;

import com.odoo.core.auth.OdooAccountManager;
import com.odoo.core.support.sync.SyncUtils;
import com.odoo.datas.OConstants;
import com.odoo.utils.AppPrefs;

import org.json.JSONObject;

import java.util.HashMap;

import odoo.Odoo;
import odoo.handler.OdooVersionException;
import odoo.helper.OUser;
import odoo.listeners.IOdooConnectionListener;
import odoo.listeners.IOdooLoginCallback;
import odoo.listeners.OdooError;

public class App extends Application {

    public static final String TAG = App.class.getSimpleName();
    public static String APPLICATION_NAME;
    private static HashMap<String, Odoo> mOdooInstances = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        App.APPLICATION_NAME = getPackageManager().getApplicationLabel(getApplicationInfo()).toString();
        Odoo.REQUEST_TIMEOUT_MS = OConstants.RPC_REQUEST_TIME_OUT;
        Odoo.DEFAULT_MAX_RETRIES = OConstants.RPC_REQUEST_RETRIES;
    }

    public Odoo getOdoo(@NonNull OUser user) {
        Log.d(TAG, "getOdoo() called with: user = [" + user + "]");
        if (mOdooInstances.containsKey(user.getAndroidName())) {
            return mOdooInstances.get(user.getAndroidName());
        }
        return null;
    }

    public void setOdoo(Odoo odoo, @NonNull OUser user) {
        Log.d(TAG, "setOdoo() called with: odoo = [" + odoo + "], user = [" + user + "]");
        if (user != null)
            mOdooInstances.put(user.getAndroidName(), odoo);
    }

    /**
     * checkOdoo()
     */
    public void requestOdoo() {

    }

    /**
     * check for Odoo instance. if Odoo instance is null this will authenticate Odoo instance
     */
    /// Added by Kasim Rangwala
    /// BEGIN
    public void checkOdoo() {
        // This method has written only for login with SelfHostedURL
        // Anyone can edit this for Odoo.com login or OAuth login
        Log.d(TAG, "checkOdoo() called");
        AppPrefs appPrefs = new AppPrefs(this);
        if (appPrefs.isSelfHosted()) {
            final OUser user = user();
            if (user != null) {
                if (getOdoo(user) == null) {
                    /// Please, do not call quickLogin(user, false); for now.
                    quickLogin(user, true);
                }
            }
        }
    }
    /// END

    /// Added by Kasim Rangwala
    /// BEGIN
    private void quickLogin(final OUser user, boolean asynchronous) {
        Log.d(TAG, "quickLogin() called with: user = [" + user + "], asynchronous = [" + asynchronous + "]");
        if (asynchronous) {
            try {
                Odoo.createInstance(this, (user.isOAuthLogin())
                        ? user.getInstanceURL() : user.getHost())
                        .authenticate(user.getUsername(), user.getPassword(), (user.isOAuthLogin()) ?
                                user.getInstanceDatabase() : user.getDatabase(), new IOdooLoginCallback() {
                            @Override
                            public void onLoginSuccess(Odoo odoo, OUser user) {
                                setOdoo(odoo, user);
                            }

                            @Override
                            public void onLoginFail(OdooError odooError) {
                                Log.e(TAG, "onLoginFail() called with: odooError = [" + odooError + "]");
                                webLogin(user);
                            }
                        });

            } catch (Exception e) {
                e.printStackTrace();
                webLogin(user);
            }
        } else {
            /// This code block requires more work.
            /// do not call this right now
            try {
                Odoo odoo = Odoo.createInstance(this, (user.isOAuthLogin())
                        ? user.getInstanceURL() : user.getHost());
                OUser oUser = odoo.authenticate(user.getUsername(), user.getPassword(), (user.isOAuthLogin()) ?
                        user.getInstanceDatabase() : user.getDatabase());
                setOdoo(odoo, oUser);
            } catch (Exception e) {
                e.printStackTrace();
                webLogin(user);
            }
        }
    }
    /// END

    /// Added by Kasim Rangwala
    /// BEGIN
    private void webLogin(final OUser user) {
        Log.d(TAG, "webLogin() called with: user = [" + user + "]");
        try {
            Odoo.createInstance(this, user.getHost()).setOnConnect(new IOdooConnectionListener() {
                @Override
                public void onConnect(Odoo odoo) {
                    odoo.authenticate(
                            user.getUsername(), user.getPassword(),
                            user.getDatabase(), new IOdooLoginCallback() {
                                @Override
                                public void onLoginSuccess(Odoo odoo, OUser oUser) {
                                    setOdoo(odoo, oUser);
                                }

                                @Override
                                public void onLoginFail(OdooError odooError) {
                                    Log.e(TAG, "onLoginFail() called with: odooError = [" + odooError + "]");
                                }
                            }
                    );
                }

                @Override
                public void onError(OdooError odooError) {
                    Log.e(TAG, "onError() called with: odooError = [" + odooError + "]");
                }
            });
        } catch (OdooVersionException e) {
            Log.e(TAG, "webLogin() OdooVersionException:", e);
        }
    }
    /// END

    /// Added by Kasim Rangwala
    /// BEGIN
    public SyncUtils sync() {
        return SyncUtils.get(this);
    }
    /// END

    /// Added by Kasim Rangwala
    /// BEGIN
    public OUser user() {
        try {
            return OdooAccountManager.getUser(this);
        } catch (Exception e) {
            Log.e(TAG, "user() Exception: ", e);
        }
        return null;
    }
    /// END

    /**
     * @param o Object returned by callMethod()
     */
    /// Added by Kasim Rangwala
    /// BEGIN
    public boolean isResponseSuccessful(Object o) {
        try {
            if (new JSONObject(o.toString()).optJSONArray("result") != null) {
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "isResponseSuccessful() Exception:", e);
        }
        return false;
    }
    /// END

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
}
