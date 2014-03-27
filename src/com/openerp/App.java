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
				mOEInstance = new OpenERP(user.getHost());
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
