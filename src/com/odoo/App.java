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
package com.odoo;

import odoo.OVersionException;
import odoo.Odoo;
import odoo.OdooInstance;
import odoo.OdooVersion;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.odoo.support.OUser;

public class App extends Application {

	private static final String TAG = App.class.getSimpleName();
	private static Odoo mOdooInstance = null;
	private static OUser mUser = null;
	private static OUser mSyncUser = null;

	@Override
	public void onCreate() {
		Log.d(TAG, "App->onCreate()");
		super.onCreate();
	}

	public void setSyncUser(OUser user) {
		mSyncUser = user;
	}

	public OUser getSyncUser() {
		return mSyncUser;
	}

	public void setUser(OUser user) {
		mUser = user;
	}

	public OUser getUser() {
		return mUser;
	}

	public OdooVersion getOdooVersion() {
		if (mOdooInstance != null)
			try {
				return mOdooInstance.getOdooVersion();
			} catch (OVersionException e) {
				e.printStackTrace();
			}
		return null;
	}

	public Odoo createInstance() {
		Odoo odoo = null;
		OUser user = OUser.current(getApplicationContext());
		if (user != null) {
			try {
				if (user.isOAauthLogin()) {
					odoo = new Odoo(user.getInstanceUrl(),
							user.isAllowSelfSignedSSL());
					OdooInstance instance = new OdooInstance();
					instance.setInstanceUrl(user.getInstanceUrl());
					instance.setDatabaseName(user.getInstanceDatabase());
					instance.setClientId(user.getClientId());
					odoo.oauth_authenticate(instance, user.getUsername(),
							user.getPassword());
				} else {
					odoo = new Odoo(user.getHost(), user.isAllowSelfSignedSSL());
					odoo.authenticate(user.getUsername(), user.getPassword(),
							user.getDatabase());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		setOdooInstance(odoo);
		setUser(user);
		return odoo;
	}

	public Odoo getOdoo() {
		Log.d(TAG, "App->getOdooInstance()");
		if (mOdooInstance == null && inNetwork()) {
			mOdooInstance = createInstance();
		}
		return mOdooInstance;
	}

	public void setOdooInstance(Odoo odoo) {
		Log.d(TAG, "App->setOdooInstance()");
		mOdooInstance = odoo;
	}

	public boolean inNetwork() {
		boolean isConnected = false;
		ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo nInfo = conManager.getActiveNetworkInfo();
		if (nInfo != null && nInfo.isConnectedOrConnecting()) {
			isConnected = true;
		}
		return isConnected;
	}
}
