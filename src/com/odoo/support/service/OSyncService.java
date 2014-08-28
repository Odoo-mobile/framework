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
package com.odoo.support.service;

import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.odoo.receivers.SyncFinishReceiver;

public abstract class OSyncService extends Service implements
		OSyncServiceListener {
	private static final String TAG = "OSyncService";
	private static final Object sSyncAdapterLock = new Object();
	private AbstractThreadedSyncAdapter sSyncAdapter = null;

	/**
	 * Thread-safe constructor, creates static {@link SyncAdapter} instance.
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Service created");
		synchronized (sSyncAdapterLock) {
			if (sSyncAdapter == null) {
				sSyncAdapter = getSyncAdapter();
			}
		}
	}

	@Override
	/**
	 * Logging-only destructor.
	 */
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Service destroyed");
		Intent intent = new Intent();
		intent.setAction(SyncFinishReceiver.SYNC_FINISH);
		getApplicationContext().sendBroadcast(intent);
	}

	/**
	 * Return Binder handle for IPC communication with {@link SyncAdapter}.
	 * 
	 * <p>
	 * New sync requests will be sent directly to the SyncAdapter using this
	 * channel.
	 * 
	 * @param intent
	 *            Calling intent
	 * @return Binder handle for {@link SyncAdapter}
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return sSyncAdapter.getSyncAdapterBinder();
	}
}
