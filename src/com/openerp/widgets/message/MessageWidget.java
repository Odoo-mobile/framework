package com.openerp.widgets.message;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.ListView;
import android.widget.RemoteViews;

import com.openerp.MainActivity;
import com.openerp.R;
import com.openerp.addons.message.MessageComposeActivity;
import com.openerp.support.OEUser;
import com.openerp.support.listview.OEListAdapter;
import com.openerp.widgets.WidgetHelper;

public class MessageWidget extends AppWidgetProvider {

	public static final String TAG = "com.openerp.widgets.MessageWidget";
	public static final String ACTION_MESSAGE_WIDGET_UPDATE = "com.openerp.widgets.ACTION_MESSAGE_WIDGET_UPDATE";
	public static final String ACTION_MESSAGE_WIDGET_CALL = "com.openerp.widgets.ACTION_MESSAGE_WIDGET_CALL";
	public static final int REQUEST_CODE = 112;

	// ListView setup
	ListView mMessageListView = null;
	OEListAdapter mMessageListAdapter = null;
	List<Object> mMessageObjects = new ArrayList<Object>();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "MessageWidget->onReceive()");
		if (intent.getAction().equals(ACTION_MESSAGE_WIDGET_CALL)) {
			Intent intentMain = new Intent(context, MainActivity.class);
			intentMain.setAction(ACTION_MESSAGE_WIDGET_CALL);
			intentMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intentMain.putExtras(intent.getExtras());
			intentMain.putExtra(WidgetHelper.EXTRA_WIDGET_ITEM_KEY,
					"message_detail");
			context.startActivity(intentMain);
		}
		if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
				&& intent.getExtras().getBoolean(
						MessageWidget.ACTION_MESSAGE_WIDGET_UPDATE)) {
			Log.v(TAG, "ACTION_MESSAGE_WIDGET_UPDATE");
			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context.getApplicationContext());
			ComponentName component = new ComponentName(context,
					MessageWidget.class);
			int[] ids = appWidgetManager.getAppWidgetIds(component);
			onUpdate(context, appWidgetManager, ids);
			appWidgetManager.notifyAppWidgetViewDataChanged(ids,
					R.id.widgetMessageList);
		}
		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		Log.d(TAG, "MessageWidget->onUpdate()");
		updateMessageWidget(context, appWidgetManager, appWidgetIds);

	}

	@SuppressLint("InlinedApi")
	private static RemoteViews initMessageWidgetListView(Context context,
			int widgetId) {
		Log.d(TAG, "MessageWidget->initWidgetListView()");
		RemoteViews mView = new RemoteViews(context.getPackageName(),
				R.layout.widget_message_layout);
		Intent svcIntent = new Intent(context, MessageRemoteViewService.class);
		svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		String filter = MessageWidgetConfigure.getPref(context, widgetId,
				"message_filter");
		svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, filter
				.toString().toLowerCase());
		svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
		mView.setRemoteAdapter(R.id.widgetMessageList, svcIntent);
		return mView;
	}

	static void updateMessageWidget(Context context, AppWidgetManager manager,
			int[] widgetIds) {
		if (OEUser.current(context) == null)
			return;
		for (int widget : widgetIds) {

			// Setting title
			String filter = MessageWidgetConfigure.getPref(context, widget,
					"message_filter");

			RemoteViews mView = initMessageWidgetListView(context, widget);

			final Intent onItemClick = new Intent(context, MessageWidget.class);
			onItemClick.setAction(ACTION_MESSAGE_WIDGET_CALL);
			onItemClick.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widget);
			onItemClick.setData(Uri.parse(onItemClick
					.toUri(Intent.URI_INTENT_SCHEME)));
			final PendingIntent onClickPendingIntent = PendingIntent
					.getBroadcast(context, 1, onItemClick,
							PendingIntent.FLAG_UPDATE_CURRENT);
			mView.setPendingIntentTemplate(R.id.widgetMessageList,
					onClickPendingIntent);

			// widget icon and main row click
			Intent messageIntent = new Intent(context, MainActivity.class);
			messageIntent.setAction(ACTION_MESSAGE_WIDGET_CALL);
			messageIntent.putExtra(WidgetHelper.EXTRA_WIDGET_ITEM_KEY,
					"message_main");
			PendingIntent mPedingIntent = PendingIntent.getActivity(context,
					REQUEST_CODE, messageIntent, 0);
			mView.setOnClickPendingIntent(R.id.widgetImgLauncher, mPedingIntent);
			mView.setOnClickPendingIntent(R.id.widgetTopBar, mPedingIntent);

			// compose message
			Intent intent = new Intent(context, MessageComposeActivity.class);
			PendingIntent pIntent = PendingIntent.getActivity(context,
					REQUEST_CODE, intent, 0);
			mView.setOnClickPendingIntent(R.id.imgBtnWidgetCompose, pIntent);

			// setting current user
			mView.setTextViewText(R.id.txvWidgetUserName,
					OEUser.current(context).getAndroidName());

			mView.setTextViewText(R.id.widgetTxvWidgetTitle, filter);
			// Updating widget
			manager.updateAppWidget(widget, mView);
		}
	}
}
