/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http://www.openerp.com>)
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * 
 */

package com.openerp.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.openerp.MainActivity;
import com.openerp.orm.OESQLiteHelper;

/**
 * The Class OpenERPAuthenticator.
 */
public class OpenERPAuthenticator extends AbstractAccountAuthenticator {

	/** The m conetext. */
	private Context mContext;

	/**
	 * Instantiates a new open erp authenticator.
	 * 
	 * @param context
	 *            the context
	 */
	public OpenERPAuthenticator(Context context) {
		super(context);
		mContext = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.accounts.AbstractAccountAuthenticator#addAccount(android.accounts
	 * .AccountAuthenticatorResponse, java.lang.String, java.lang.String,
	 * java.lang.String[], android.os.Bundle)
	 */
	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response,
			String accountType, String authTokenType,
			String[] requiredFeatures, Bundle options)
			throws NetworkErrorException {

		final Bundle result;
		final Intent intent;

		intent = new Intent(mContext, MainActivity.class);
		result = new Bundle();
		intent.putExtra("create_new_account", true);
		result.putParcelable(AccountManager.KEY_INTENT, intent);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.accounts.AbstractAccountAuthenticator#confirmCredentials(android
	 * .accounts.AccountAuthenticatorResponse, android.accounts.Account,
	 * android.os.Bundle)
	 */
	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response,
			Account account, Bundle options) throws NetworkErrorException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.accounts.AbstractAccountAuthenticator#editProperties(android.
	 * accounts.AccountAuthenticatorResponse, java.lang.String)
	 */
	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response,
			String accountType) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.accounts.AbstractAccountAuthenticator#getAuthToken(android.accounts
	 * .AccountAuthenticatorResponse, android.accounts.Account,
	 * java.lang.String, android.os.Bundle)
	 */
	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response,
			Account account, String authTokenType, Bundle options)
			throws NetworkErrorException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.accounts.AbstractAccountAuthenticator#getAuthTokenLabel(java.
	 * lang.String)
	 */
	@Override
	public String getAuthTokenLabel(String authTokenType) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.accounts.AbstractAccountAuthenticator#hasFeatures(android.accounts
	 * .AccountAuthenticatorResponse, android.accounts.Account,
	 * java.lang.String[])
	 */
	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response,
			Account account, String[] features) throws NetworkErrorException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.accounts.AbstractAccountAuthenticator#getAccountRemovalAllowed
	 * (android.accounts.AccountAuthenticatorResponse, android.accounts.Account)
	 */
	@Override
	public Bundle getAccountRemovalAllowed(
			AccountAuthenticatorResponse response, Account account)
			throws NetworkErrorException {
		Bundle result = super.getAccountRemovalAllowed(response, account);
		if (result != null
				&& result.containsKey(AccountManager.KEY_BOOLEAN_RESULT)
				&& !result.containsKey(AccountManager.KEY_INTENT)) {
			final boolean removalAllowed = result
					.getBoolean(AccountManager.KEY_BOOLEAN_RESULT);

			if (removalAllowed) {
				OESQLiteHelper sqlite = new OESQLiteHelper(mContext);
				if (sqlite.cleanUserRecords(account.name)) {
					// TODO: next task after cleaning all record reletated to
					// user.
				}
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.accounts.AbstractAccountAuthenticator#updateCredentials(android
	 * .accounts.AccountAuthenticatorResponse, android.accounts.Account,
	 * java.lang.String, android.os.Bundle)
	 */
	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response,
			Account account, String authTokenType, Bundle options)
			throws NetworkErrorException {
		return null;
	}

}
