/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http:www.openerp.com>)
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
 */
package com.openerp.support;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;

import com.openerp.auth.OpenERPAccountManager;

// TODO: Auto-generated Javadoc
/**
 * The Class UserObject.
 */
public class OEUser {

	/** The username. */
	private String username;

	/** The user_id. */
	private int user_id;

	/** The partner_id. */
	private int partner_id;

	/** The timezone. */
	private String timezone;

	/** The isactive. */
	private boolean isactive;

	/** The avatar. */
	private String avatar;

	/** The database. */
	private String database;

	/** The host. */
	private String host;

	/** The android_name. */
	private String android_name;

	/** The password. */
	private String password;

	/** The company_id. */
	private String company_id;

	/**
	 * Gets the data as bundle.
	 * 
	 * @return the as bundle
	 */
	public Bundle getAsBundle() {
		Bundle bundle = new Bundle();
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
		return bundle;
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
	 * @param company_id
	 *            the new company_id
	 */
	public void setCompany_id(String company_id) {
		this.company_id = company_id;
	}

	/**
	 * Sets the data from bundle.
	 * 
	 * @param data
	 *            the new from bundle
	 */
	public void setFromBundle(Bundle data) {
		this.setUsername(data.getString("username"));
		this.setUser_id(Integer.parseInt(data.getString("user_id")));
		this.setPartner_id(Integer.parseInt(data.getString("partner_id")));
		this.setTimezone(data.getString("timezone"));
		this.setIsactive(data.getBoolean("isactive"));
		this.setAvatar(data.getString("avatar"));
		this.setDatabase(data.getString("database"));
		this.setHost(data.getString("host"));
		this.setAndroidName(data.getString("android_name"));
		this.setPassword(data.getString("password"));
		this.setCompany_id(data.getString("company_id"));
	}

	/**
	 * Fill from account.
	 * 
	 * @param accMgr
	 *            the acc mgr
	 * @param account
	 *            the account
	 */
	public void fillFromAccount(AccountManager accMgr, Account account) {
		this.setUsername(accMgr.getUserData(account, "username"));
		this.setUser_id(Integer.parseInt(accMgr.getUserData(account, "user_id")));
		this.setPartner_id(Integer.parseInt(accMgr.getUserData(account,
				"partner_id")));
		this.setTimezone(accMgr.getUserData(account, "timezone"));
		this.setIsactive(Boolean.parseBoolean(accMgr.getUserData(account,
				"isactive")));
		this.setAvatar(accMgr.getUserData(account, "avatar"));
		this.setDatabase(accMgr.getUserData(account, "database"));
		this.setHost(accMgr.getUserData(account, "host"));
		this.setAndroidName(accMgr.getUserData(account, "android_name"));
		this.setPassword(accMgr.getUserData(account, "password"));
		this.setCompany_id(accMgr.getUserData(account, "company_id"));
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
	 * @param password
	 *            the new password
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
	 * @param android_name
	 *            the new android name
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
	 * @param username
	 *            the new username
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
	 * @param user_id
	 *            the new user_id
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
	 * @param partner_id
	 *            the new partner_id
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
	 * @param timezone
	 *            the new timezone
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
	 * @param isactive
	 *            the new isactive
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
	 * @param avatar
	 *            the new avatar
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
	 * @param database
	 *            the new database
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
	 * @param host
	 *            the new host
	 */
	public void setHost(String host) {
		this.host = host;
	}

	public static OEUser current(Context context) {
		return OpenERPAccountManager.currentUser(context);
	}
}
