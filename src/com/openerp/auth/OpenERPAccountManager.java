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
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import com.openerp.App;
import com.openerp.config.SyncWizardValues;
import com.openerp.support.OEUser;
import com.openerp.support.SyncValue;

/**
 * The Class OpenERPAccountManager.
 */
public class OpenERPAccountManager {

	/** The Constant PARAM_AUTHTOKEN_TYPE. */
	private static final String PARAM_AUTHTOKEN_TYPE = "com.openerp.auth";
	public static OEUser current_user = null;

	/**
	 * Fetch all accounts.
	 * 
	 * @param context
	 *            the context
	 * @return the list
	 */
	public static List<OEUser> fetchAllAccounts(Context context) {
		List<OEUser> userObjs = null;
		AccountManager accMgr = AccountManager.get(context);
		Account[] accounts = accMgr.getAccountsByType(PARAM_AUTHTOKEN_TYPE);
		if (accounts.length > 0) {
			userObjs = new ArrayList<OEUser>();
			for (Account account : accounts) {
				OEUser userobj = new OEUser();
				userobj.fillFromAccount(accMgr, account);
				userObjs.add(userobj);
			}
		}
		return userObjs;
	}

	/**
	 * hasAccounts
	 * 
	 * checks for availability of any account for OpenERP
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
	public static boolean createAccount(Context context, OEUser bundleData) {
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
		if (current_user != null) {
			flag = true;
		} else {
			List<OEUser> accounts = OpenERPAccountManager
					.fetchAllAccounts(context);
			if (accounts != null) {
				for (OEUser user : accounts) {
					if (user.isIsactive()) {
						flag = true;
						current_user = user;
						break;
					}
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
	public static OEUser currentUser(Context context) {
		if (current_user != null) {
			return current_user;
		} else {
			if (OpenERPAccountManager.isAnyUser(context)) {
				List<OEUser> accounts = OpenERPAccountManager
						.fetchAllAccounts(context);
				for (OEUser user : accounts) {

					if (user.isIsactive()) {
						return user;
					}
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
	public static OEUser getAccountDetail(Context context, String username) {

		List<OEUser> allAccounts = OpenERPAccountManager
				.fetchAllAccounts(context);
		for (OEUser user : allAccounts) {
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

			OEUser userData = new OEUser();
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
		OEUser user = OpenERPAccountManager.getAccountDetail(context, username);
		Account account = OpenERPAccountManager.getAccount(context,
				user.getAndroidName());
		if (user != null) {
			if (cancelAllSync(account)) {
				AccountManager accMgr = AccountManager.get(context);
				user.setIsactive(false);

				accMgr.setUserData(account, "isactive", "0");
				flag = true;
				current_user = null;
			}
		}
		return flag;

	}

	private static boolean cancelAllSync(Account account) {
		SyncWizardValues syncVals = new SyncWizardValues();
		boolean flag = false;
		for (SyncValue sync : syncVals.syncValues()) {
			ContentResolver.cancelSync(account, sync.getAuthority());
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
	public static OEUser loginUser(Context context, String username) {
		OEUser userData = null;

		List<OEUser> allAccounts = OpenERPAccountManager
				.fetchAllAccounts(context);
		for (OEUser user : allAccounts) {
			OpenERPAccountManager.logoutUser(context, user.getAndroidName());
		}

		userData = OpenERPAccountManager.getAccountDetail(context, username);
		if (userData != null) {
			AccountManager accMgr = AccountManager.get(context);

			accMgr.setUserData(
					OpenERPAccountManager.getAccount(context,
							userData.getAndroidName()), "isactive", "true");
		}
		current_user = userData;
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
		App app = (App) context.getApplicationContext();
		app.setOEInstance(null);
		current_user = null;
	}

	public static boolean updateAccountDetails(Context context,
			OEUser userObject) {

		boolean flag = false;
		OEUser user = OpenERPAccountManager.getAccountDetail(context,
				userObject.getAndroidName());
		Bundle userBundle = userObject.getAsBundle();
		if (user != null) {
			AccountManager accMgr = AccountManager.get(context);
			for (String key : userBundle.keySet()) {
				accMgr.setUserData(
						OpenERPAccountManager.getAccount(context,
								user.getAndroidName()), key,
						userBundle.getString(key));
			}

			flag = true;
		}
		return flag;
	}
}
