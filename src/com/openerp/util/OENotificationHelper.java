/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http:www.openerp.com>)
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
package com.openerp.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class OENotificationHelper {
	NotificationCompat.Builder mBuilder;
	NotificationManager mNotifyManager;
	Notification notification;

	Intent notificationIntent = null;
	PendingIntent pendingIntent = null;

	@SuppressWarnings("deprecation")
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
