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
 * Created on 13/3/15 2:29 PM
 */
package com.odoo.news;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.odoo.core.orm.OValues;
import com.odoo.news.models.OdooNews;

/**
 * Odoo News Receiver
 */
public class OdooNewsReceiver extends BroadcastReceiver {
    public static final String TAG = OdooNewsReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // Use Keys: subject, from, message
        OdooNews news = new OdooNews(context, null);
        OValues values = new OValues();
        values.put("sender", intent.getExtras().getString("from"));
        values.put("subject", intent.getExtras().getString("subject"));
        values.put("message", intent.getExtras().getString("message"));
        news.insert(values);
    }
}
