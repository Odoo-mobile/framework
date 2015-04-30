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
 * Created on 8/1/15 11:39 AM
 */
package com.odoo.core.support.sync;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.odoo.core.support.OUser;

public class SyncUtils {
    public static final String TAG = SyncUtils.class.getSimpleName();

    private OUser mUser;

    public SyncUtils(Context context, OUser user) {
        mUser = (user != null) ? user : OUser.current(context);
    }

    public static SyncUtils get(Context context) {
        return new SyncUtils(context, null);
    }

    public static SyncUtils get(Context context, OUser user) {
        return new SyncUtils(context, user);
    }

    public void setAutoSync(String authority, boolean autoSync) {
        try {
            Account account = mUser.getAccount();
            if (!ContentResolver.isSyncActive(account, authority)) {
                ContentResolver.setSyncAutomatically(account, authority, autoSync);
            }
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public void requestSync(String authority) {
        requestSync(authority, null);
    }

    public void requestSync(String authority, Bundle bundle) {
        Account account = mUser.getAccount();
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        if (bundle != null) {
            settingsBundle.putAll(bundle);
        }
        ContentResolver.requestSync(account, authority, settingsBundle);
    }

    public void setSyncPeriodic(String authority, long interval_in_minute,
                                long seconds_per_minute, long milliseconds_per_second) {
        Account account = mUser.getAccount();
        Bundle extras = new Bundle();
        this.setAutoSync(authority, true);
        ContentResolver.setIsSyncable(account, authority, 1);
        final long sync_interval = interval_in_minute * seconds_per_minute
                * milliseconds_per_second;
        ContentResolver.addPeriodicSync(account, authority, extras,
                sync_interval);

    }

    public void cancelSync(String authority) {
        Account account = mUser.getAccount();
        ContentResolver.cancelSync(account, authority);
    }
}
