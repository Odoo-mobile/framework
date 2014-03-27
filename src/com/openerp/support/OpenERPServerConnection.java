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
package com.openerp.support;

import javax.net.ssl.SSLPeerUnverifiedException;

import openerp.OEVersionException;
import openerp.OpenERP;

import org.json.JSONArray;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.openerp.auth.OpenERPAccountManager;

/**
 * The Class OpenERPServerConnection.
 */
public class OpenERPServerConnection {

	public static final String TAG = "com.openerp.support.OpenERPServerConnection";
	/** The openerp. */
	public OpenERP openerp = null;

	/**
	 * Test connection.
	 * 
	 * @param context
	 *            the context
	 * @param serverURL
	 *            the server url
	 * @param mForceConnect
	 * @return true, if successful
	 * @throws OEVersionException
	 * @throws SSLPeerUnverifiedException
	 */
	public boolean testConnection(Context context, String serverURL)
			throws OEVersionException, SSLPeerUnverifiedException {
		Log.d(TAG, "OpenERPServerConnection->testConnection()");
		if (TextUtils.isEmpty(serverURL)) {
			return false;
		}
		try {
			openerp = new OpenERP(serverURL);
			openerp.getDatabaseList();
		} catch (SSLPeerUnverifiedException ssl) {
			Log.d(TAG, "Throw SSLPeerUnverifiedException ");
			throw new SSLPeerUnverifiedException(ssl.getMessage());
		} catch (OEVersionException version) {
			throw new OEVersionException(version.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Gets the databases.
	 * 
	 * @param context
	 *            the context
	 * @param serverURL
	 *            the server url
	 * @return the databases
	 * @throws OEVersionException
	 * @throws SSLPeerUnverifiedException
	 */
	public JSONArray getDatabases(Context context, String serverURL)
			throws OEVersionException, SSLPeerUnverifiedException {
		JSONArray dbList = null;
		if (this.testConnection(context, serverURL)) {
			try {
				dbList = openerp.getDatabaseList();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return dbList;
	}

	/**
	 * Checks if is network available.
	 * 
	 * @param context
	 *            the context
	 * @return true, if is network available
	 * @throws OEVersionException
	 * @throws SSLPeerUnverifiedException
	 */
	public static boolean isNetworkAvailable(Context context)
			throws OEVersionException, SSLPeerUnverifiedException {
		boolean outcome = false;

		OpenERPServerConnection osc = new OpenERPServerConnection();
		outcome = osc.testConnection(context, OpenERPAccountManager
				.currentUser(context).getHost());

		return outcome;
	}

	/**
	 * Checks if is network available.
	 * 
	 * @param context
	 *            the context
	 * @param url
	 *            the url
	 * @return true, if is network available
	 * @throws OEVersionException
	 * @throws SSLPeerUnverifiedException
	 */
	public static boolean isNetworkAvailable(Context context, String url)
			throws OEVersionException, SSLPeerUnverifiedException {
		boolean outcome = false;

		OpenERPServerConnection osc = new OpenERPServerConnection();
		outcome = osc.testConnection(context, url);

		return outcome;
	}
}
