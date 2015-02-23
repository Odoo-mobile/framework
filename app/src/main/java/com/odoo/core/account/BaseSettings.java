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
 * Created on 9/1/15 11:35 AM
 */
package com.odoo.core.account;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.odoo.R;
import com.odoo.core.utils.OPreferenceManager;
import com.odoo.core.utils.OResource;

public class BaseSettings extends PreferenceFragment {
    public static final String TAG = BaseSettings.class.getSimpleName();
    // Keys
    public static final String KEY_NOTIFICATION_RING_TONE = "notification_ringtone";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.base_preference);
    }

    public static Uri getNotificationRingTone(Context context) {
        OPreferenceManager mPref = new OPreferenceManager(context);
        String defaultUri = OResource.string(context, R.string.notification_default_ring_tone);
        return Uri.parse(mPref.getString(KEY_NOTIFICATION_RING_TONE, defaultUri));
    }

}