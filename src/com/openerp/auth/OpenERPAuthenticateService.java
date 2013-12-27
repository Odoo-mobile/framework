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

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * The Class OpenERPAuthenticateService.
 */
public class OpenERPAuthenticateService extends Service {

	/** The Constant TAG. */
	@SuppressWarnings("unused")
	private static final String TAG = "AccountAuthenticatorService";

	/** The oe account authenticator. */
	private static OpenERPAuthenticator oeAccountAuthenticator = null;

	/**
	 * Instantiates a new open erp authenticate service.
	 */
	public OpenERPAuthenticateService() {

		super();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		IBinder ret = null;
		if (intent.getAction().equals(
				android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT)) {
			ret = new OpenERPAuthenticator(this).getIBinder();
		}
		return ret;
	}

	/**
	 * Gets the authenticator.
	 * 
	 * @return the authenticator
	 */
	@SuppressWarnings("unused")
	private OpenERPAuthenticator getAuthenticator() {
		if (oeAccountAuthenticator == null) {
			oeAccountAuthenticator = new OpenERPAuthenticator(this);
		}
		return oeAccountAuthenticator;

	}

}
