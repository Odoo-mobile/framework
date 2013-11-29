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

import java.io.IOException;

import openerp.OEVersionException;
import openerp.OpenERP;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.text.TextUtils;

import com.openerp.auth.OpenERPAccountManager;

// TODO: Auto-generated Javadoc
/**
 * The Class OpenERPServerConnection.
 */
public class OpenERPServerConnection {

	/** The openerp. */
	public OpenERP openerp = null;

	/**
	 * Test connection.
	 * 
	 * @param context
	 *            the context
	 * @param serverURL
	 *            the server url
	 * @return true, if successful
	 * @throws OEVersionException
	 */
	public boolean testConnection(Context context, String serverURL)
			throws OEVersionException {
		if (TextUtils.isEmpty(serverURL)) {
			return false;
		}
		try {
			openerp = new OpenERP(serverURL);
			openerp.getDatabaseList();
		} catch (OEVersionException version) {
			throw new OEVersionException(version.getMessage());
		} catch (Exception e) {
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
	 */
	public JSONArray getDatabases(Context context, String serverURL)
			throws OEVersionException {
		JSONArray dbList = null;
		if (this.testConnection(context, serverURL)) {
			try {
				dbList = openerp.getDatabaseList();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
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
	 */
	public static boolean isNetworkAvailable(Context context)
			throws OEVersionException {
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
	 */
	public static boolean isNetworkAvailable(Context context, String url)
			throws OEVersionException {
		boolean outcome = false;

		OpenERPServerConnection osc = new OpenERPServerConnection();
		outcome = osc.testConnection(context, url);

		return outcome;
	}
}
