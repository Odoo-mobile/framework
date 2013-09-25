package com.openerp.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class OENotificationHelper {
	private static final String TAG = "OENotificationHelper";
	NotificationCompat.Builder mBuilder;
	NotificationManager mNotifyManager;
	Notification notification;

	Intent notificationIntent = null;
	PendingIntent pendingIntent = null;

	public void showNotification(Context context, String title,
			String subtitle, String authority, int icon) {
		mNotifyManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notification = new Notification(icon, title, System.currentTimeMillis());
		if (notificationIntent != null) {
			notification.defaults |= Notification.DEFAULT_VIBRATE;
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notification.setLatestEventInfo(context, title, subtitle,
					pendingIntent);
		}
		mNotifyManager.notify(0, notification);
	}

	public void stopSyncNotification(Context context) {
		mNotifyManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotifyManager.cancelAll();
	}

	public void setResultIntent(Intent intent, Context context) {
		this.notificationIntent = intent;
		pendingIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
						| Notification.FLAG_AUTO_CANCEL);
	}
}
