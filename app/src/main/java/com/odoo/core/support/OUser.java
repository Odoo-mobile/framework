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
 * Created on 17/12/14 6:19 PM
 */
package com.odoo.core.support;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.odoo.core.auth.OdooAccountManager;

import odoo.helper.OdooVersion;

public class OUser extends odoo.helper.OUser {

    public static final int USER_ACCOUNT_VERSION = 2;
    private Account account;
    private OdooVersion odooVersion;

    public static OUser current(Context context) {
        return OdooAccountManager.getActiveUser(context);
    }

    @Override
    public Bundle getAsBundle() {
        Bundle data = super.getAsBundle();
        // Converting each value to string. Account supports only string values
        for (String key : data.keySet()) {
            data.putString(key, data.get(key) + "");
        }
        return data;
    }

    public void setFromBundle(Bundle data) {
        fillFromBundle(data);
    }

    public void fillFromAccount(AccountManager accMgr, Account account) {

        setName(accMgr.getUserData(account, "name"));
        setUsername(accMgr.getUserData(account, "username"));
        setUserId(Integer.parseInt(accMgr.getUserData(account, "user_id")));
        setPartnerId(Integer.parseInt(accMgr.getUserData(account, "partner_id")));
        setTimezone(accMgr.getUserData(account, "timezone"));
        setIsActive(Boolean.parseBoolean(accMgr.getUserData(account, "isactive")));
        setAvatar(accMgr.getUserData(account, "avatar"));
        setDatabase(accMgr.getUserData(account, "database"));
        setHost(accMgr.getUserData(account, "host"));
        setAndroidName(accMgr.getUserData(account, "android_name"));
        setPassword(accMgr.getUserData(account, "password"));
        setCompanyId(Integer.parseInt(accMgr.getUserData(account, "company_id")));
        setAllowForceConnect(Boolean.parseBoolean(accMgr.getUserData(account, "allow_self_signed_ssl")));
        try {
            OdooVersion version = new OdooVersion();
            version.setServerSerie(accMgr.getUserData(account, "server_serie"));
            version.setVersionType(accMgr.getUserData(account, "version_type"));
            version.setVersionRelease(accMgr.getUserData(account, "version_release"));
            version.setVersionNumber(Integer.parseInt(accMgr.getUserData(account, "version_number")));
            version.setVersionTypeNumber(Integer.parseInt(accMgr.getUserData(account, "version_type_number")));
            version.setServerVersion(accMgr.getUserData(account, "server_version"));
            setOdooVersion(version);
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, e.getMessage());
        }
        // If oAuth login
        setOAuthLogin(Boolean.parseBoolean(accMgr.getUserData(account, "oauth_login")));
        setInstanceDatabase(accMgr.getUserData(account, "instance_database"));
        setInstanceURL(accMgr.getUserData(account, "instance_url"));
        setClientId(accMgr.getUserData(account, "client_id"));
    }

    public String getDBName() {
        String db_name = "OdooSQLite";
        db_name += "_" + getUsername();
        db_name += "_" + getDatabase();
        return db_name + ".db";
    }

    @Override
    public String toString() {
        return getAndroidName();
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    @Override
    public OdooVersion getOdooVersion() {
        return odooVersion;
    }

    @Override
    public void setOdooVersion(OdooVersion odooVersion) {
        this.odooVersion = odooVersion;
    }
}
