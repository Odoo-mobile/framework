/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p>
 * Created on 17/12/14 6:21 PM
 */
package com.odoo.core.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Build;
import android.support.annotation.BoolRes;
import android.support.design.widget.BottomSheetDialog;
import android.util.Log;

import com.odoo.App;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.OSQLite;
import com.odoo.core.support.OUser;
import com.odoo.core.utils.OPreferenceManager;
import com.odoo.core.utils.sys.OCacheUtils;

import java.io.IOException;
import java.net.Authenticator;
import java.util.ArrayList;
import java.util.List;

public class OdooAccountManager {

    public static final String TAG = OdooAccountManager.class.getSimpleName();
    public static final String KEY_ACCOUNT_TYPE = "com.odoo.auth";
    public static final String KEY_USER_ACCOUNT_VERSION = "key_user_account_version";

    /**
     * Gets all the account related Odoo Auth
     *
     * @param context
     * @return List of OUser instances if any
     */
    public static List<OUser> getAllAccounts(Context context) {
        List<OUser> users = new ArrayList<>();
        AccountManager aManager = AccountManager.get(context);
        for (Account account : aManager.getAccountsByType(KEY_ACCOUNT_TYPE)) {
            OUser user = new OUser();
            user.fillFromAccount(aManager, account);
            user.setAccount(account);
            users.add(user);
        }
        return users;
    }

    /**
     * Check for any account availability
     *
     * @param context
     * @return true, if any account found
     */
    public static boolean hasAnyAccount(Context context) {
        if (getAllAccounts(context).size() > 0)
            return true;
        return false;
    }

    public static boolean isValidUserObj(Context context, OUser user) {
        OPreferenceManager pref = new OPreferenceManager(context);
        int version = pref.getInt(userObjectKEY(user), 0);
        if (version == 0) {
            updateUserData(context, user, user);
            version = OUser.USER_ACCOUNT_VERSION;
        }
        return (version == OUser.USER_ACCOUNT_VERSION);
    }

    /**
     * Creates Odoo account for app
     *
     * @param context
     * @param user    user instance (OUser)
     * @return true, if account created successfully
     */

    public static boolean createAccount(Context context, OUser user) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = new Account(user.getAndroidName(), KEY_ACCOUNT_TYPE);
        if (accountManager.addAccountExplicitly(account, String.valueOf(user.getPassword()),
                user.getAsBundle())) {
            OPreferenceManager pref = new OPreferenceManager(context);
            if (pref.getInt(userObjectKEY(user), 0) != OUser.USER_ACCOUNT_VERSION) {
                pref.putInt(userObjectKEY(user), OUser.USER_ACCOUNT_VERSION);
            }
            return true;
        }
        return false;
    }

    public static String userObjectKEY(OUser user) {
        return KEY_USER_ACCOUNT_VERSION + "_" + user.getAndroidName();
    }

    /**
     * Remove account from device
     *
     * @param context
     * @param username
     * @return true, if account removed successfully
     */
    public static boolean removeAccount(Context context, String username) {
        OUser user = getDetails(context, username);
        if (user != null) {
            AccountManager accountManager = AccountManager.get(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (accountManager.removeAccountExplicitly(user.getAccount())) {
                    dropDatabase(user);
                }
                return true;
            } else {
                try {
                    AccountManagerFuture<Boolean> result = accountManager.
                            removeAccount(user.getAccount(), null, null);
                    if (result.getResult()) {
                        dropDatabase(user);
                    }
                    return true;
                } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public static void dropDatabase(OUser user) {
        OSQLite sqLite = App.getSQLite(user.getAndroidName());
        if (sqLite != null) {
            sqLite.dropDatabase();
            App.setSQLite(user.getAndroidName(), null);
        }
    }

    public static OUser updateUserData(Context context, OUser user, OUser newData) {
        if (user != null) {
            AccountManager accountManager = AccountManager.get(context);
            for (String key : newData.getAsBundle().keySet()) {
                accountManager.setUserData(user.getAccount(), key, newData.getAsBundle().get(key) + "");
            }
            OPreferenceManager pref = new OPreferenceManager(context);
            if (pref.getInt(userObjectKEY(user), 0) != OUser.USER_ACCOUNT_VERSION) {
                pref.putInt(userObjectKEY(user), OUser.USER_ACCOUNT_VERSION);
            }
        }
        return getDetails(context, newData.getAndroidName());
    }

    /**
     * Updates user bundle data in accounts
     *
     * @param context
     * @param newData instance of OUser class
     * @return new user object with updated values
     */
    public static OUser updateUserData(Context context, OUser newData) {
        OUser user = getDetails(context, newData.getAndroidName());
        return updateUserData(context, user, newData);
    }

    /**
     * Finds any active user for application
     *
     * @param context
     * @return true, if there is any active user for app
     */
    public static boolean anyActiveUser(Context context) {
        for (OUser user : getAllAccounts(context)) {
            if (user.isActive()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets active user object
     *
     * @param context
     * @return user object (Instance of OUser class)
     */
    public static OUser getActiveUser(Context context) {
        for (OUser user : getAllAccounts(context)) {
            if (user.isActive()) {
                return user;
            }
        }
        return null;
    }

    /**
     * Returns OUser object with username
     *
     * @param context
     * @param username
     * @return instance for OUser class or null
     */
    public static OUser getDetails(Context context, String username) {
        for (OUser user : getAllAccounts(context))
            if (user.getAndroidName().equals(username)) {
                return user;
            }
        return null;
    }

    /**
     * Login to user account. changes active state for user.
     * Other users will be automatically logged out
     *
     * @param context
     * @param username
     * @return new user object
     */
    public static OUser login(Context context, String username) {

        // Setting odoo instance to null
        App app = (App) context.getApplicationContext();
        app.setOdoo(null, null);
        OUser activeUser = getActiveUser(context);
        // Logging out user if any
        if (activeUser != null) {
            logout(context, activeUser.getAndroidName());
        }

        OUser newUser = getDetails(context, username);
        if (newUser != null) {
            AccountManager accountManager = AccountManager.get(context);
            accountManager.setUserData(newUser.getAccount(), "isactive", "true");
            Log.i(TAG, newUser.getName() + " Logged in successfully");
            return newUser;
        }
        // Clearing old cache of the system
        OCacheUtils.clearSystemCache(context);
        return null;
    }

    /**
     * Logout user
     *
     * @param context
     * @param username
     * @return true, if successfully logged out
     */
    public static boolean logout(Context context, String username) {
        OUser user = getDetails(context, username);
        if (user != null) {
            if (cancelUserSync(user.getAccount())) {
                AccountManager accountManager = AccountManager.get(context);
                accountManager.setUserData(user.getAccount(), "isactive", "false");
                Log.i(TAG, user.getName() + " Logged out successfully");
                return true;
            }
        }
        return false;
    }

    private static boolean cancelUserSync(Account account) {
        //TODO: Cancel user's sync services. if any.
        return true;
    }
}
