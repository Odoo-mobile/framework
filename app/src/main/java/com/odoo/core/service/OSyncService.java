/**
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
 * Created on 1/1/15 3:16 PM
 */
package com.odoo.core.service;

import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.odoo.core.service.receivers.ISyncFinishReceiver;
import com.odoo.core.support.OUser;

public abstract class OSyncService extends Service {
    public static final String TAG = OSyncService.class.getSimpleName();
    private static final Object sSyncAdapterLock = new Object();
    private AbstractThreadedSyncAdapter sSyncAdapter = null;

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
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroyed");
        Intent intent = new Intent();
        intent.setAction(ISyncFinishReceiver.SYNC_FINISH);
        getApplicationContext().sendBroadcast(intent);
    }

    public String getModelName() {
        ComponentName myService = new ComponentName(this, this.getClass());
        try {
            Bundle data = getPackageManager().getServiceInfo(myService, PackageManager.GET_META_DATA).metaData;
            return data.getString("model");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        throw new NullPointerException("Unable to find model meta data in your service." + getClass().getName());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }

    public abstract OSyncAdapter getSyncAdapter();

    public abstract void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user);
}
