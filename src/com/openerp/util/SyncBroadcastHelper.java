package com.openerp.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.openerp.R;
import com.openerp.receivers.SyncProgressReceiver;

public class SyncBroadcastHelper {
	private static final String TAG = "SyncBroadcastHelper";
	NotificationCompat.Builder mBuilder;
	NotificationManager mNotifyManager;
	Notification notification;
	Thread sync_thread = null;

	public void startSyncNotification(Context context, String name,
			String authority) {
		Log.d(TAG, "startSyncNotification");
		mNotifyManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat.Builder(context);
		notification = mBuilder.build();
		mBuilder.setContentTitle("OpenERP Sync")
				.setContentText(name + " sync in progress")
				.setSmallIcon(R.drawable.ic_stat_refresh);

		sync_thread = new Thread(new Runnable() {

			@Override
			public void run() {
				mBuilder.setProgress(0, 0, true);
				mNotifyManager.notify(0, mBuilder.build());
			}
		});
		sync_thread.start();
	}

	public void stopSyncNotification(Context context) {
		Log.d(TAG, "stopSyncNotification");
		mNotifyManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotifyManager.cancelAll();
	}

	public void sendBrodcast(Context context, String authority, String name,
			String status) {
		Intent syncStatus = new Intent();
		syncStatus.setAction(SyncProgressReceiver.SYNC_PROGRESS);
		Bundle sync_service = new Bundle();
		sync_service.putString("name", name);
		sync_service.putString("authority", authority);
		sync_service.putString("status", status);
		syncStatus.putExtra("sync_service", sync_service);
		context.sendBroadcast(syncStatus);

	}
}
