/*
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
 */
package com.odoo.auth;

import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import com.odoo.App;
import com.odoo.config.SyncWizardValues;
import com.odoo.support.OUser;
import com.odoo.support.SyncValue;

/**
 * The Class OdooAccountManager.
 */
public class OdooAccountManager {

	/** The Constant PARAM_AUTHTOKEN_TYPE. */
	private static final String PARAM_AUTHTOKEN_TYPE = "com.odoo.auth";

	/**
	 * Fetch all accounts.
	 * 
	 * @param context
	 *            the context
	 * @return the list
	 */
	public static List<OUser> fetchAllAccounts(Context context) {
		List<OUser> userObjs = new ArrayList<OUser>();
		AccountManager accMgr = AccountManager.get(context);
		Account[] accounts = accMgr.getAccountsByType(PARAM_AUTHTOKEN_TYPE);
		if (accounts.length > 0) {
			userObjs = new ArrayList<OUser>();
			for (Account account : accounts) {
				OUser userobj = new OUser();
				userobj.fillFromAccount(accMgr, account);
				userObjs.add(userobj);
			}
		}
		return userObjs;
	}

	/**
	 * hasAccounts
	 * 
	 * checks for availability of any account for Odoo
	 * 
	 * @param context
	 * @return true if there is any account related to type
	 */
	public static boolean hasAccounts(Context context) {
		boolean flag = false;
		AccountManager accMgr = AccountManager.get(context);
		if (accMgr.getAccountsByType(PARAM_AUTHTOKEN_TYPE).length > 0) {
			flag = true;
		}
		return flag;
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
	public static boolean createAccount(Context context, OUser bundleData) {
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

		List<OUser> accounts = OdooAccountManager.fetchAllAccounts(context);
		if (accounts != null) {
			for (OUser user : accounts) {
				if (user.isIsactive()) {
					flag = true;
					break;
				}
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
	public static OUser currentUser(Context context) {
		App app = (App) context.getApplicationContext();
		if (app.getUser() != null) {
			return app.getUser();
		}
		if (OdooAccountManager.isAnyUser(context)) {
			List<OUser> accounts = OdooAccountManager.fetchAllAccounts(context);
			for (OUser user : accounts) {
				if (user.isIsactive()) {
					return user;
				}
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
	public static OUser getAccountDetail(Context context, String username) {

		List<OUser> allAccounts = OdooAccountManager.fetchAllAccounts(context);
		for (OUser user : allAccounts) {
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

			OUser userData = new OUser();
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
		OUser user = OdooAccountManager.getAccountDetail(context, username);
		Account account = OdooAccountManager.getAccount(context,
				user.getAndroidName());
		App app = (App) context.getApplicationContext();
		if (user != null) {
			if (cancelAllSync(account)) {
				AccountManager accMgr = AccountManager.get(context);
				user.setIsactive(false);
				accMgr.setUserData(account, "isactive", "0");
				flag = true;
				app.setUser(null);
			}
		}
		return flag;

	}

	private static boolean cancelAllSync(Account account) {
		SyncWizardValues syncVals = new SyncWizardValues();
		for (SyncValue sync : syncVals.syncValues()) {
			ContentResolver.cancelSync(account, sync.getAuthority());
		}
		return true;
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
	public static OUser loginUser(Context context, String username) {
		OUser userData = null;

		List<OUser> allAccounts = OdooAccountManager.fetchAllAccounts(context);
		for (OUser user : allAccounts) {
			OdooAccountManager.logoutUser(context, user.getAndroidName());
		}

		userData = OdooAccountManager.getAccountDetail(context, username);
		if (userData != null) {
			AccountManager accMgr = AccountManager.get(context);

			accMgr.setUserData(
					OdooAccountManager.getAccount(context,
							userData.getAndroidName()), "isactive", "true");
		}
		App app = (App) context.getApplicationContext();
		app.setUser(userData);
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
		accMgr.removeAccount(OdooAccountManager.getAccount(context, username),
				null, null);
		App app = (App) context.getApplicationContext();
		app.setOdooInstance(null);
		app.setUser(null);
	}

	public static boolean updateAccountDetails(Context context, OUser userObject) {

		boolean flag = false;
		OUser user = OdooAccountManager.getAccountDetail(context,
				userObject.getAndroidName());
		Bundle userBundle = userObject.getAsBundle();
		if (user != null) {
			AccountManager accMgr = AccountManager.get(context);
			for (String key : userBundle.keySet()) {
				accMgr.setUserData(
						OdooAccountManager.getAccount(context,
								user.getAndroidName()), key,
						userBundle.getString(key));
			}

			flag = true;
		}
		return flag;
	}
}
