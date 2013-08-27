package com.openerp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SyncFinishReceiver extends BroadcastReceiver {
	public static final String SYNC_FINISH = "com.openerp.SYNC_FINISH";

	@Override
	public void onReceive(Context arg0, Intent intent) {
		// TODO Auto-generated method stub
		// String tag = intent.getExtras().getString("service");
		Log.i("SyncFinish", "Sync finished, should refresh nao!!");
	}

}
