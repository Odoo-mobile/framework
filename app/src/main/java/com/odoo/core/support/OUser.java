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

import com.odoo.core.rpc.helper.OdooVersion;
import com.odoo.core.rpc.helper.utils.OBundleUtils;

public class OUser {

    public static final String TAG = OUser.class.getSimpleName();
    public static final int USER_ACCOUNT_VERSION = 2;
    private Account account;
    private String username, name, timezone, avatar, database, host, password;
    private Integer userId, partnerId, companyId;
    private Boolean isActive = false, allowForceConnect = false;
    private OdooVersion odooVersion;

    public static OUser current(Context context) {
        return OdooAccountManager.getActiveUser(context);
    }

    public Boolean isAllowForceConnect() {
        return allowForceConnect;
    }

    public void setAllowForceConnect(Boolean allowForceConnect) {
        this.allowForceConnect = allowForceConnect;
    }

    public Boolean isActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public Integer getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Integer partnerId) {
        this.partnerId = partnerId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getAndroidName() {
        return username + "[" + database + "]";
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public OdooVersion getOdooVersion() {
        return odooVersion;
    }

    public void setOdooVersion(OdooVersion odooVersion) {
        this.odooVersion = odooVersion;
    }

    public Bundle getAsBundle() {
        Bundle data = new Bundle();
        data.putString("username", getUsername());
        data.putString("name", getName());
        data.putString("timezone", getTimezone());
        data.putString("avatar", getAvatar());
        data.putString("database", getDatabase());
        data.putString("host", getHost());
        data.putString("android_name", getAndroidName());
        data.putString("password", getPassword());
        data.putInt("user_id", getUserId());
        data.putInt("partner_id", getPartnerId());
        data.putInt("company_id", getCompanyId());
        data.putBoolean("is_active", isActive());
        data.putBoolean("allow_force_connect", isAllowForceConnect());
        if (odooVersion != null) {
            data.putAll(odooVersion.getAsBundle());
        }
        // Converting each value to string. Account supports only string values
        for (String key : data.keySet()) {
            data.putString(key, data.get(key) + "");
        }
        return data;
    }

    public void fillFromBundle(Bundle data) {
        if (OBundleUtils.hasKey(data, "username"))
            setUsername(data.getString("username"));
        if (OBundleUtils.hasKey(data, "name"))
            setName(data.getString("name"));
        if (OBundleUtils.hasKey(data, "timezone"))
            setTimezone(data.getString("timezone"));
        if (OBundleUtils.hasKey(data, "avatar"))
            setAvatar(data.getString("avatar"));
        if (OBundleUtils.hasKey(data, "database"))
            setDatabase(data.getString("database"));
        if (OBundleUtils.hasKey(data, "host"))
            setHost(data.getString("host"));
        if (OBundleUtils.hasKey(data, "password"))
            setPassword(data.getString("password"));
        if (OBundleUtils.hasKey(data, "user_id"))
            setUserId(data.getInt("user_id"));
        if (OBundleUtils.hasKey(data, "partner_id"))
            setPartnerId(data.getInt("partner_id"));
        if (OBundleUtils.hasKey(data, "company_id"))
            setCompanyId(data.getInt("company_id"));
        if (OBundleUtils.hasKey(data, "is_active"))
            setIsActive(data.getBoolean("is_active"));
        if (OBundleUtils.hasKey(data, "allow_force_connect"))
            setAllowForceConnect(data.getBoolean("allow_force_connect"));
        odooVersion = new OdooVersion();
        odooVersion.fillFromBundle(data);
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

}
