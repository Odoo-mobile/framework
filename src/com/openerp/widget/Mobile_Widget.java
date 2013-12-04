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
package com.openerp.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import com.openerp.R;
import com.openerp.MainActivity;
import com.openerp.addons.messages.Message;
import com.openerp.addons.messages.MessageComposeActivty;
import com.openerp.addons.messages.MessageDBHelper;
import com.openerp.addons.note.ComposeNoteActivity;
import com.openerp.addons.note.Note;
import com.openerp.addons.note.NoteDBHelper;
import com.openerp.auth.OpenERPAccountManager;
import com.openerp.base.res.Res_PartnerDBHelper;
import com.openerp.orm.OEHelper;
import com.openerp.util.drawer.DrawerItem;

public class Mobile_Widget extends AppWidgetProvider {
	public static final String TAG = "android.appwidget.action.APPWIDGET_UPDATE";
	public static final String CONSTANT = "ACTION.WIDGET.UPDATE.FROM.ACTIVITY";

	int total_Notes = 0;
	int total_unreadMessges = 0;
	ComponentName openerpWidget = null;
	int[] allWidgetIds = null;
	AppWidgetManager appWidgetManager = null;
	RemoteViews remoteViews = null;

	@Override
	public void onReceive(Context context, Intent intent) {

		try {
			appWidgetManager = AppWidgetManager.getInstance(context
					.getApplicationContext());
			openerpWidget = new ComponentName(context.getApplicationContext(),
					Mobile_Widget.class);
			allWidgetIds = appWidgetManager.getAppWidgetIds(openerpWidget);

			if (allWidgetIds != null && allWidgetIds.length > 0) {
				onUpdate(context, appWidgetManager, allWidgetIds);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		for (int widgetId : allWidgetIds) {
			try {
				// Fetching Notes From Account
				Note note = new Note();
				total_Notes = note.getCount("-1", context);
				if (String.valueOf(total_Notes) == null
						&& String.valueOf(total_Notes)
								.equalsIgnoreCase("false")) {
					total_Notes = 0;
				}

				// Fetching UNREAD Messages From Account
				Message message = new Message();
				total_unreadMessges = message.getCount(Message.TYPE.INBOX,
						context);

				if (String.valueOf(total_unreadMessges) == null
						&& String.valueOf(total_unreadMessges)
								.equalsIgnoreCase("false")) {
					total_unreadMessges = 0;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.activity_widget);

			// Updating NOTES Counter
			remoteViews.setTextViewText(R.id.widget_notification_counter_notes,
					String.valueOf(total_Notes));

			// Updating MESSAGE Counter
			remoteViews.setTextViewText(
					R.id.widget_notification_counter_messages,
					String.valueOf(total_unreadMessges));

			// Click on Notes
			// Openening OpenERP Mobile NOTE[All] Module
			Intent configIntent = new Intent(context, MainActivity.class);
			configIntent.setAction("NOTES");
			PendingIntent configPendingIntent = PendingIntent.getActivity(
					context, 0, configIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.widget_label_notes_layout,
					configPendingIntent);

			// Click on Messages
			// Openening OpenERP Mobile MESSAGE[Inbox] Module
			Intent messageIntent = new Intent(context, MainActivity.class);
			messageIntent.setAction("MESSAGE");
			PendingIntent messagePendingIntent = PendingIntent.getActivity(
					context, 0, messageIntent, 0);
			remoteViews.setOnClickPendingIntent(
					R.id.widget_label_messages_layout, messagePendingIntent);

			// Click on Compose Messages
			// Openening Screen to COMPOSE MESSAGE
			MessageDBHelper db = new MessageDBHelper(context);
			Intent composeMessage = null;
			if (db.getOEInstance().isInstalled("mail.message")) {
				composeMessage = new Intent(context,
						MessageComposeActivty.class);
			} else {
				composeMessage = new Intent(context, MainActivity.class);
			}
			PendingIntent composeMessagePendingIntent = PendingIntent
					.getActivity(context, 0, composeMessage, 0);
			remoteViews.setOnClickPendingIntent(
					R.id.widget_notification_compose_messages,
					composeMessagePendingIntent);

			// Click on Compose Note
			// Openening Screen to COMPOSE NOTE
			Intent composeNote = null;
			if (db.getOEInstance().isInstalled("note.note")) {
				composeNote = new Intent(context, ComposeNoteActivity.class);
			} else {
				composeNote = new Intent(context, MainActivity.class);
			}
			PendingIntent composeNotePendingIntent = PendingIntent.getActivity(
					context, 0, composeNote, 0);
			remoteViews.setOnClickPendingIntent(
					R.id.widget_notification_compose__notes,
					composeNotePendingIntent);

			// Click on ICON
			// Openening Application
			Intent openApp = new Intent(context, MainActivity.class);
			PendingIntent openAppPendingIntent = PendingIntent.getActivity(
					context, 0, openApp, 0);
			remoteViews.setOnClickPendingIntent(R.id.widget_icon_logo_layout,
					openAppPendingIntent);

			// Updating the widget
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
	}
}
