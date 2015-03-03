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
 * Created on 17/12/14 6:19 PM
 */
package com.odoo.core.support;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;

import com.odoo.core.auth.OdooAccountManager;

public class OUser {
    /**
     * The username.
     */
    private String username;

    /**
     * The name.
     */
    private String name;

    /**
     * The user_id.
     */
    private int user_id;

    /**
     * The partner_id.
     */
    private int partner_id;

    /**
     * The timezone.
     */
    private String timezone;

    /**
     * The isactive.
     */
    private boolean isactive;

    /**
     * The avatar.
     */
    private String avatar;

    /**
     * The database.
     */
    private String database;

    /**
     * The host.
     */
    private String host;

    /**
     * The android_name.
     */
    private String android_name;

    /**
     * The password.
     */
    private String password;

    /**
     * The company_id.
     */
    private String company_id;

    /**
     * The allow_self_signed_ssl.
     */
    private boolean allow_self_signed_ssl = false;

    /**
     * The oauth_login.
     */
    private boolean oauth_login = false;

    /**
     * The version_number.
     */
    private Integer version_number = 0;

    /**
     * The version_serie.
     */
    private String version_serie = null;

    // If oauth login
    /**
     * The instance_url.
     */
    private String instance_url = null;

    /**
     * The instance_database.
     */
    private String instance_database = null;

    /**
     * The client_id.
     */
    private String client_id = null;
    /**
     * Account instance
     */
    private Account account = null;

    /**
     * Current.
     *
     * @param context the context
     * @return the o user
     */
    public static OUser current(Context context) {
        return OdooAccountManager.getActiveUser(context);
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the company_id.
     *
     * @return the company_id
     */
    public String getCompany_id() {
        return company_id;
    }

    /**
     * Sets the company_id.
     *
     * @param company_id the new company_id
     */
    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }

    /**
     * Gets the data as bundle.
     *
     * @return the as bundle
     */
    public Bundle getAsBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("name", this.getName());
        bundle.putString("username", this.getUsername());
        bundle.putString("user_id", this.getUser_id() + "");
        bundle.putString("partner_id", this.getPartner_id() + "");
        bundle.putString("timezone", this.getTimezone());
        bundle.putString("isactive", String.valueOf(this.isIsactive()));
        bundle.putString("avatar", this.getAvatar());
        bundle.putString("database", this.getDatabase());
        bundle.putString("host", this.getHost());
        bundle.putString("android_name", this.getAndroidName());
        bundle.putString("password", this.getPassword());
        bundle.putString("company_id", this.getCompany_id());
        bundle.putString("allow_self_signed_ssl",
                String.valueOf(this.isAllowSelfSignedSSL()));
        bundle.putString("instance_database", this.getInstanceDatabase());
        bundle.putString("instance_url", this.getInstanceUrl());
        bundle.putString("oauth_login", this.isOAauthLogin() + "");
        bundle.putString("client_id", this.getClientId());
        bundle.putString("odoo_version_number", getVersion_number() + "");
        bundle.putString("odoo_version_serie", getVersion_serie());
        return bundle;
    }

    /**
     * Sets the data from bundle.
     *
     * @param data the new from bundle
     */
    public void setFromBundle(Bundle data) {
        setName(data.getString("name"));
        setUsername(data.getString("username"));
        setUser_id(Integer.parseInt(data.getString("user_id")));
        setPartner_id(Integer.parseInt(data.getString("partner_id")));
        setTimezone(data.getString("timezone"));
        setIsactive(Boolean.parseBoolean(data.getString("isactive")));
        setAvatar(data.getString("avatar"));
        setDatabase(data.getString("database"));
        setHost(data.getString("host"));
        setAndroidName(data.getString("android_name"));
        setPassword(data.getString("password"));
        setCompany_id(data.getString("company_id"));
        setAllowSelfSignedSSL(Boolean.parseBoolean(data
                .getString("allow_self_signed_ssl")));
        setVersion_number(Integer.parseInt(data
                .getString("odoo_version_number")));
        setVersion_serie(data.getString("odoo_version_serie"));
        // If oAuth Login
        setInstanceDatabase(data.getString("instance_database"));
        setInstanceUrl(data.getString("instance_url"));
        setOAuthLogin(Boolean.parseBoolean(data.getString("oauth_login")));
        setClientId(data.getString("client_id"));
    }

    /**
     * Fill from account.
     *
     * @param accMgr  the acc mgr
     * @param account the account
     */
    public void fillFromAccount(AccountManager accMgr, Account account) {

        setName(accMgr.getUserData(account, "name"));
        setUsername(accMgr.getUserData(account, "username"));
        setUser_id(Integer.parseInt(accMgr.getUserData(account, "user_id")));
        setPartner_id(Integer.parseInt(accMgr
                .getUserData(account, "partner_id")));
        setTimezone(accMgr.getUserData(account, "timezone"));
        setIsactive(Boolean.parseBoolean(accMgr
                .getUserData(account, "isactive")));
        try {
            setAvatar(accMgr.getUserData(account, "avatar"));
        } catch (Exception e) {
            setAvatar("false");
        }
        setDatabase(accMgr.getUserData(account, "database"));
        setHost(accMgr.getUserData(account, "host"));
        setAndroidName(accMgr.getUserData(account, "android_name"));
        setPassword(accMgr.getUserData(account, "password"));
        setCompany_id(accMgr.getUserData(account, "company_id"));
        setAllowSelfSignedSSL(Boolean.parseBoolean(accMgr.getUserData(account, "allow_self_signed_ssl")));
        setVersion_number(Integer.parseInt(accMgr.getUserData(account,
                "odoo_version_number")));
        setVersion_serie(accMgr.getUserData(account, "odoo_version_serie"));
        // If oAuth login
        setOAuthLogin(Boolean.parseBoolean(accMgr.getUserData(account,
                "oauth_login")));
        setInstanceDatabase(accMgr.getUserData(account, "instance_database"));
        setInstanceUrl(accMgr.getUserData(account, "instance_url"));
        setClientId(accMgr.getUserData(account, "client_id"));

    }


    /**
     * Gets the password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password the new password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the android name.
     *
     * @return the android name
     */
    public String getAndroidName() {
        return this.android_name;
    }

    /**
     * Sets the android name.
     *
     * @param android_name the new android name
     */
    public void setAndroidName(String android_name) {
        this.android_name = android_name;
    }

    /**
     * Gets the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username the new username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the user_id.
     *
     * @return the user_id
     */
    public int getUser_id() {
        return user_id;
    }

    /**
     * Sets the user_id.
     *
     * @param user_id the new user_id
     */
    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    /**
     * Gets the partner_id.
     *
     * @return the partner_id
     */
    public int getPartner_id() {
        return partner_id;
    }

    /**
     * Sets the partner_id.
     *
     * @param partner_id the new partner_id
     */
    public void setPartner_id(int partner_id) {
        this.partner_id = partner_id;
    }

    /**
     * Gets the timezone.
     *
     * @return the timezone
     */
    public String getTimezone() {
        return timezone;
    }

    /**
     * Sets the timezone.
     *
     * @param timezone the new timezone
     */
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    /**
     * Checks if is isactive.
     *
     * @return true, if is isactive
     */
    public boolean isIsactive() {
        return isactive;
    }

    /**
     * Sets the isactive.
     *
     * @param isactive the new isactive
     */
    public void setIsactive(boolean isactive) {
        this.isactive = isactive;
    }

    /**
     * Gets the avatar.
     *
     * @return the avatar
     */
    public String getAvatar() {
        return avatar;
    }

    /**
     * Sets the avatar.
     *
     * @param avatar the new avatar
     */
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    /**
     * Gets the database.
     *
     * @return the database
     */
    public String getDatabase() {
        return database;
    }

    /**
     * Sets the database.
     *
     * @param database the new database
     */
    public void setDatabase(String database) {
        this.database = database;
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the host.
     *
     * @param host the new host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Checks if is allow self signed ssl.
     *
     * @return true, if is allow self signed ssl
     */
    public boolean isAllowSelfSignedSSL() {
        return allow_self_signed_ssl;
    }

    /**
     * Sets the allow self signed ssl.
     *
     * @param allow_self_signed_ssl the new allow self signed ssl
     */
    public void setAllowSelfSignedSSL(boolean allow_self_signed_ssl) {
        this.allow_self_signed_ssl = allow_self_signed_ssl;
    }

    /**
     * Checks if is o aauth login.
     *
     * @return true, if is o aauth login
     */
    public boolean isOAauthLogin() {
        return oauth_login;
    }

    /**
     * Sets the o auth login.
     *
     * @param oauth_login the new o auth login
     */
    public void setOAuthLogin(boolean oauth_login) {
        this.oauth_login = oauth_login;
    }

    /**
     * Gets the instance url.
     *
     * @return the instance url
     */
    public String getInstanceUrl() {
        return instance_url;
    }

    /**
     * Sets the instance url.
     *
     * @param instnace_url the new instance url
     */
    public void setInstanceUrl(String instnace_url) {
        this.instance_url = instnace_url;
    }

    /**
     * Gets the instance database.
     *
     * @return the instance database
     */
    public String getInstanceDatabase() {
        return instance_database;
    }

    /**
     * Sets the instance database.
     *
     * @param instance_database the new instance database
     */
    public void setInstanceDatabase(String instance_database) {
        this.instance_database = instance_database;
    }

    /**
     * Gets the client id.
     *
     * @return the client id
     */
    public String getClientId() {
        return client_id;
    }

    /**
     * Sets the client id.
     *
     * @param client_id the new client id
     */
    public void setClientId(String client_id) {
        this.client_id = client_id;
    }

    public Integer getVersion_number() {
        return version_number;
    }

    public void setVersion_number(Integer version_number) {
        this.version_number = version_number;
    }

    public String getVersion_serie() {
        return version_serie;
    }

    public void setVersion_serie(String version_serie) {
        this.version_serie = version_serie;
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
}
