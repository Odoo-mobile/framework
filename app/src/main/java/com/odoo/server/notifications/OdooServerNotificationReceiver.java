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
 * Created on 1/4/15 7:38 PM
 */
package com.odoo.server.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

//import odoo.OdooServerNotification;

public class OdooServerNotificationReceiver extends BroadcastReceiver {
    public static final String TAG = OdooServerNotificationReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data = intent.getExtras();
//        int message_id = Integer.parseInt(data.getString(OdooServerNotification.KEY_MESSAGE_ID));
//        ONotificationBuilder builder = new ONotificationBuilder(context,
//                message_id);
//        builder.setTitle(data.getString(OdooServerNotification.KEY_MESSAGE_AUTHOR_NAME));
//        builder.setBigText(data.getString(OdooServerNotification.KEY_MESSAGE_BODY));
//        builder.build().show();
    }
}
