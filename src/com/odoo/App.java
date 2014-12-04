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

import com.odoo.support.OEUser;

import odoo.Odoo;
import android.app.Application;
import android.util.Log;

public class App extends Application implements
		MainActivity.OnOdooInstanceCreateListener {

	public static final String TAG = App.class.getSimpleName();
	public static Odoo mOEInstance = null;

	@Override
	public void onCreate() {
		Log.d(TAG, "App->onCreate()");
		super.onCreate();
	}

	public Odoo createInstance() {
		Odoo odoo = null;
		OEUser user = OEUser.current(getApplicationContext());
		if (user != null) {
			try {
				odoo = new Odoo(this, user.getHost(),
						user.isAllowSelfSignedSSL());
				odoo.authenticate(user.getUsername(), user.getPassword(),
						user.getDatabase());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return odoo;
	}

	public Odoo getOEInstance() {
		Log.d(TAG, "App->getOEInstance()");
		if (mOEInstance == null) {
			mOEInstance = createInstance();
		}
		return mOEInstance;
	}

	public void setOEInstance(Odoo odoo) {
		mOEInstance = odoo;
	}

	@Override
	public void onOdooInstanceCreated(Odoo odoo) {
		setOEInstance(odoo);
	}
}
