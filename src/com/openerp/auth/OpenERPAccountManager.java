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
package com.openerp.auth;

import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.openerp.support.UserObject;

// TODO: Auto-generated Javadoc
/**
 * The Class OpenERPAccountManager.
 */
public class OpenERPAccountManager {

	/** The Constant PARAM_AUTHTOKEN_TYPE. */
	private static final String PARAM_AUTHTOKEN_TYPE = "com.openerp.auth";

	/**
	 * Fetch all accounts.
	 * 
	 * @param context
	 *            the context
	 * @return the list
	 */
	public static List<UserObject> fetchAllAccounts(Context context) {
		List<UserObject> userObjs = null;

		AccountManager accMgr = AccountManager.get(context);
		Account[] accounts = accMgr.getAccountsByType(PARAM_AUTHTOKEN_TYPE);
		if (accounts.length > 0) {
			userObjs = new ArrayList<UserObject>();
			for (Account account : accounts) {
				UserObject userobj = new UserObject();
				userobj.fillFromAccount(accMgr, account);
				userObjs.add(userobj);
			}
		}
		return userObjs;
	}

	/**
	 * Creates the account.
	 * 
	 * @param context
	 *            the context
	 * @param bundleData
	 *            the bundle data
	 * @return true, if successful
	 */
	public static boolean createAccount(Context context, UserObject bundleData) {
		AccountManager accMgr = null;
		accMgr = AccountManager.get(context);
		String accountType = PARAM_AUTHTOKEN_TYPE;
		String password = String.valueOf(bundleData.getPassword());
		String accountName = bundleData.getAndroidName();
		Account account = new Account(accountName, accountType);
		Bundle bundle = bundleData.getAsBundle();
		return accMgr.addAccountExplicitly(account, password, bundle);
	}

	/**
	 * Checks if is any user.
	 * 
	 * @param context
	 *            the context
	 * @return true, if is any user
	 */
	public static boolean isAnyUser(Context context) {
		boolean flag = false;

		List<UserObject> accounts = OpenERPAccountManager
				.fetchAllAccounts(context);
		for (UserObject user : accounts) {
			if (user.isIsactive()) {
				flag = true;
				break;
			}
		}

		return flag;
	}

	/**
	 * Current user.
	 * 
	 * @param context
	 *            the context
	 * @return the user object
	 */
	public static UserObject currentUser(Context context) {
		List<UserObject> accounts = OpenERPAccountManager
				.fetchAllAccounts(context);
		for (UserObject user : accounts) {

			if (user.isIsactive()) {
				return user;
			}
		}

		return null;
	}

	/**
	 * Gets the account detail.
	 * 
	 * @param context
	 *            the context
	 * @param username
	 *            the username
	 * @return the account detail
	 */
	public static UserObject getAccountDetail(Context context, String username) {

		List<UserObject> allAccounts = OpenERPAccountManager
				.fetchAllAccounts(context);
		for (UserObject user : allAccounts) {
			if (user.getAndroidName().equals(username)) {
				return user;
			}
		}
		return null;
	}

	/**
	 * Gets the account.
	 * 
	 * @param context
	 *            the context
	 * @param username
	 *            the username
	 * @return the account
	 */
	public static Account getAccount(Context context, String username) {
		AccountManager accMgr = AccountManager.get(context);
		Account[] accounts = accMgr.getAccountsByType(PARAM_AUTHTOKEN_TYPE);

		Account userAc = null;
		for (Account account : accounts) {

			UserObject userData = new UserObject();
			userData.fillFromAccount(accMgr, account);

			if (userData != null) {
				if (userData.getAndroidName().equals(username)) {
					userAc = account;
				}
			}
		}
		return userAc;

	}

	/**
	 * Logout user.
	 * 
	 * @param context
	 *            the context
	 * @param username
	 *            the username
	 * @return true, if successful
	 */
	public static boolean logoutUser(Context context, String username) {
		boolean flag = false;
		UserObject user = OpenERPAccountManager.getAccountDetail(context,
				username);
		if (user != null) {
			AccountManager accMgr = AccountManager.get(context);
			user.setIsactive(false);

			accMgr.setUserData(
					OpenERPAccountManager.getAccount(context,
							user.getAndroidName()), "isactive", "0");
			flag = true;
		}
		return flag;

	}

	/**
	 * Login user.
	 * 
	 * @param context
	 *            the context
	 * @param username
	 *            the username
	 * @return the user object
	 */
	public static UserObject loginUser(Context context, String username) {
		UserObject userData = null;

		List<UserObject> allAccounts = OpenERPAccountManager
				.fetchAllAccounts(context);
		for (UserObject user : allAccounts) {
			OpenERPAccountManager.logoutUser(context, user.getAndroidName());
		}

		userData = OpenERPAccountManager.getAccountDetail(context, username);
		if (userData != null) {
			AccountManager accMgr = AccountManager.get(context);

			accMgr.setUserData(
					OpenERPAccountManager.getAccount(context,
							userData.getAndroidName()), "isactive", "true");
		}
		return userData;
	}

	/**
	 * Removes the account.
	 * 
	 * @param context
	 *            the context
	 * @param username
	 *            the username
	 */
	public static void removeAccount(Context context, String username) {
		AccountManager accMgr = AccountManager.get(context);
		accMgr.removeAccount(
				OpenERPAccountManager.getAccount(context, username), null, null);

	}
}
