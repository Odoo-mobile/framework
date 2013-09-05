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

package com.openerp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.openerp.util.SyncBroadcastHelper;

// TODO: Auto-generated Javadoc
/**
 * The Class SyncProgressReceiver.
 */
public class SyncProgressReceiver extends BroadcastReceiver {

	/** The sync_broadcast. */
	SyncBroadcastHelper sync_broadcast = new SyncBroadcastHelper();

	/** The Constant SYNC_FINISH. */
	public static final String SYNC_PROGRESS = "com.openerp.SYNC_PROGRESS";

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle serviceInfo = new Bundle();
		if (intent.hasExtra("sync_service")) {
			serviceInfo = (Bundle) intent.getExtras().get("sync_service");
			String name = serviceInfo.getString("name");
			String authority = serviceInfo.getString("authority");
			String status = serviceInfo.getString("status");
			if (status.equals("start")) {
				sync_broadcast.startSyncNotification(context, name, authority);
			} else {
				sync_broadcast.stopSyncNotification(context);
			}
		}

	}

}
