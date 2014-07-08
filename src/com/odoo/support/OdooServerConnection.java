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
package com.odoo.support;

import javax.net.ssl.SSLPeerUnverifiedException;

import odoo.OVersionException;
import odoo.Odoo;

import org.json.JSONArray;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.odoo.auth.OdooAccountManager;

/**
 * The Class OdooServerConnection.
 */
public class OdooServerConnection {

	public static final String TAG = "com.odoo.support.OdooServerConnection";
	public Odoo odoo = null;
	JSONArray mDbLists = null;
	boolean mAllowSelfSignedSSL = false;

	public OdooServerConnection() {
		mAllowSelfSignedSSL = false;
	}

	public OdooServerConnection(boolean allowSelfSignedSSL) {
		mAllowSelfSignedSSL = allowSelfSignedSSL;
	}

	/**
	 * Test connection.
	 * 
	 * @param context
	 *            the context
	 * @param serverURL
	 *            the server url
	 * @param mForceConnect
	 * @return true, if successful
	 * @throws OVersionException
	 * @throws SSLPeerUnverifiedException
	 */
	public boolean testConnection(Context context, String serverURL)
			throws OVersionException, SSLPeerUnverifiedException {
		Log.d(TAG, "OdooServerConnection->testConnection()");
		if (TextUtils.isEmpty(serverURL)) {
			return false;
		}
		try {
			odoo = new Odoo(serverURL, mAllowSelfSignedSSL);
			mDbLists = odoo.getDatabaseList();
			if (mDbLists == null) {
				mDbLists = new JSONArray();
				if (odoo.getDatabaseName() != null)
					mDbLists.put(odoo.getDatabaseName());
			}
		} catch (SSLPeerUnverifiedException ssl) {
			Log.d(TAG, "Throw SSLPeerUnverifiedException ");
			throw new SSLPeerUnverifiedException(ssl.getMessage());
		} catch (OVersionException version) {
			throw new OVersionException(version.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public String[] getDatabases() {
		String[] dbs = new String[mDbLists.length()];
		try {
			for (int i = 0; i < mDbLists.length(); i++)
				dbs[i] = mDbLists.getString(i);
		} catch (Exception e) {
		}
		return dbs;
	}

	/**
	 * Checks if is network available.
	 * 
	 * @param context
	 *            the context
	 * @return true, if is network available
	 * @throws OVersionException
	 * @throws SSLPeerUnverifiedException
	 */
	public static boolean isNetworkAvailable(Context context)
			throws OVersionException, SSLPeerUnverifiedException {
		boolean outcome = false;

		OdooServerConnection osc = new OdooServerConnection();
		outcome = osc.testConnection(context,
				OdooAccountManager.currentUser(context).getHost());

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
	 * @throws OVersionException
	 * @throws SSLPeerUnverifiedException
	 */
	public static boolean isNetworkAvailable(Context context, String url)
			throws OVersionException, SSLPeerUnverifiedException {
		boolean outcome = false;

		OdooServerConnection osc = new OdooServerConnection();
		outcome = osc.testConnection(context, url);

		return outcome;
	}
}
