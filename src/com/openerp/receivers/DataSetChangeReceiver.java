package com.openerp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DataSetChangeReceiver extends BroadcastReceiver {
	public static final String DATA_CHANGED = "com.openerp.DATA_CHANGED";

	@Override
	public void onReceive(Context arg0, Intent intent) {
		// TODO Auto-generated method stub
		// String tag = intent.getExtras().getString("service");
		Log.i("DATASET Changed", "Dataset Changed Receiver");
	}

}
