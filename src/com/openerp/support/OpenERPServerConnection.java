package com.openerp.support;

import java.io.IOException;

import openerp.OpenERP;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.text.TextUtils;

import com.openerp.auth.OpenERPAccountManager;

public class OpenERPServerConnection {

	public OpenERP openerp = null;

	public boolean testConnection(Context context, String serverURL) {
		if (TextUtils.isEmpty(serverURL)) {
			return false;
		}
		try {
			openerp = new OpenERP(serverURL);
			openerp.getDatabaseList();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return false;
		} catch (RuntimeException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public JSONArray getDatabases(Context context, String serverURL) {
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

	public static boolean isNetworkAvailable(Context context) {
		boolean outcome = false;

		OpenERPServerConnection osc = new OpenERPServerConnection();
		outcome = osc.testConnection(context, OpenERPAccountManager
				.currentUser(context).getHost());

		return outcome;
	}

	public static boolean isNetworkAvailable(Context context, String url) {
		boolean outcome = false;

		OpenERPServerConnection osc = new OpenERPServerConnection();
		outcome = osc.testConnection(context, url);

		return outcome;
	}
}
