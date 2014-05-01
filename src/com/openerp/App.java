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
package com.openerp;

import openerp.OpenERP;
import android.app.Application;
import android.util.Log;

import com.openerp.auth.OpenERPAccountManager;
import com.openerp.support.OEUser;

public class App extends Application {

	public static final String TAG = App.class.getSimpleName();
	public static OpenERP mOEInstance = null;

	@Override
	public void onCreate() {
		Log.d(TAG, "App->onCreate()");
		super.onCreate();
		OEUser user = OEUser.current(getApplicationContext());
		if (user != null) {
			try {
				mOEInstance = new OpenERP(user.getHost(),
						user.isAllowSelfSignedSSL());
				mOEInstance.authenticate(user.getUsername(),
						user.getPassword(), user.getDatabase());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!OpenERPAccountManager.isAnyUser(getApplicationContext())) {
			mOEInstance = null;
		}
	}

	public OpenERP getOEInstance() {
		Log.d(TAG, "App->getOEInstance()");
		return mOEInstance;
	}

	public void setOEInstance(OpenERP openERP) {
		mOEInstance = openERP;
	}
}
