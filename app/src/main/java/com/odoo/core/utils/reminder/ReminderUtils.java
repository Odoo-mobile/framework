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
 * Created on 9/1/15 6:12 PM
 */
package com.odoo.core.utils.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.odoo.core.orm.fields.OColumn;

import java.util.Date;

public class ReminderUtils {
    public static final String TAG = ReminderUtils.class.getSimpleName();
    public static final String KEY_REMINDER_TYPE = "key_reminder_type";
    private Context mContext;

    public ReminderUtils(Context context) {
        mContext = context;
    }

    public static ReminderUtils get(Context context) {
        return new ReminderUtils(context);
    }

    public boolean setReminder(Date date, Bundle extra) {
        Intent myIntent = new Intent(mContext, ReminderReceiver.class);
        myIntent.putExtras(extra);
        int row_id = extra.getInt(OColumn.ROW_ID);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, row_id, myIntent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, date.getTime(), pendingIntent);
        return true;
    }

    public boolean resetReminder(Date date, Bundle extra) {
        if (cancelReminder(date, extra)) {
            setReminder(date, extra);
        }
        return true;
    }

    public boolean cancelReminder(Date date, Bundle extra) {
        Intent myIntent = new Intent(mContext, ReminderReceiver.class);
        myIntent.putExtras(extra);
        int row_id = extra.getInt(OColumn.ROW_ID);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, row_id, myIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);
        return true;
    }
}
