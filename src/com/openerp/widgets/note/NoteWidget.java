package com.openerp.widgets.note;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.openerp.MainActivity;
import com.openerp.R;
import com.openerp.addons.note.NoteComposeActivity;
import com.openerp.addons.note.NoteDB;
import com.openerp.addons.note.NoteDB.NoteStages;
import com.openerp.orm.OEDataRow;
import com.openerp.support.OEUser;
import com.openerp.widgets.WidgetHelper;

public class NoteWidget extends AppWidgetProvider {

	public static final String NOTE_WIDGET = "com.openerp.widgets.NoteWidget.NOTE_WIDGET";
	public static final String TAG = "com.openerp.widgets.NoteWidget";
	public static final String ACTION_NOTE_WIDGET_UPDATE = "com.openerp.widgets.ACTION_NOTE_WIDGET_UPDATE";
	public static final String ACTION_NOTE_WIDGET_CALL = "com.openerp.widgets.ACTION_NOTE_WIDGET_CALL";
	public static final int REQUEST_CODE = 111;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Notewidget->onReceive()");
		if (intent.getAction().equals(ACTION_NOTE_WIDGET_CALL)) {
			Intent intentMain = new Intent(context, MainActivity.class);
			intentMain.setAction(ACTION_NOTE_WIDGET_CALL);
			intentMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intentMain.putExtras(intent.getExtras());
			intentMain.putExtra(WidgetHelper.EXTRA_WIDGET_ITEM_KEY,
					"note_detail");
			context.startActivity(intentMain);
		}
		if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
				&& intent.getExtras().getBoolean(ACTION_NOTE_WIDGET_UPDATE)) {
			Log.v(TAG, "ACTION_NOTE_WIDGET_UPDATE");
			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context.getApplicationContext());
			ComponentName component = new ComponentName(context,
					NoteWidget.class);
			int[] ids = appWidgetManager.getAppWidgetIds(component);
			onUpdate(context, appWidgetManager, ids);
			appWidgetManager.notifyAppWidgetViewDataChanged(ids,
					R.id.widgetNoteList);
		}
		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		updateNoteWidget(context, appWidgetManager, appWidgetIds);
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@SuppressLint("InlinedApi")
	private static RemoteViews initWidgetListView(Context context, int widgetId) {
		Log.d(TAG, "NoteWidget->initWidgetListView()");
		RemoteViews mView = new RemoteViews(context.getPackageName(),
				R.layout.widget_note_layout);
		Intent svcIntent = new Intent(context, NoteRemoteViewService.class);
		svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		int stage_id = NoteWidgetConfigure.getPref(context, widgetId,
				"note_filter");
		svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, stage_id);
		svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
		mView.setRemoteAdapter(R.id.widgetNoteList, svcIntent);
		mView.setEmptyView(R.id.widgetTxvNotesEmptyView, R.id.widgetNoteList);
		return mView;
	}

	static void updateNoteWidget(Context context, AppWidgetManager manager,
			int[] widgetIds) {
		for (int widgetId : widgetIds) {
			RemoteViews mView = initWidgetListView(context, widgetId);

			final Intent onItemClick = new Intent(context, NoteWidget.class);
			onItemClick.setAction(ACTION_NOTE_WIDGET_CALL);
			onItemClick.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
			onItemClick.setData(Uri.parse(onItemClick
					.toUri(Intent.URI_INTENT_SCHEME)));
			final PendingIntent onClickPendingIntent = PendingIntent
					.getBroadcast(context, REQUEST_CODE, onItemClick,
							PendingIntent.FLAG_UPDATE_CURRENT);
			mView.setPendingIntentTemplate(R.id.widgetNoteList,
					onClickPendingIntent);

			int stage_id = NoteWidgetConfigure.getPref(context, widgetId,
					"note_filter");

			String title = "Notes";
			if (stage_id > -1) {
				NoteStages stage = new NoteDB(context).new NoteStages(context);
				OEDataRow stageInfo = stage.select(stage_id);
				title = stageInfo.getString("name");
			}

			// setting current user
			mView.setTextViewText(R.id.widgetNoteTxvUserName,
					OEUser.current(context).getAndroidName());
			mView.setTextViewText(R.id.widgetNoteTxvWidgetTitle, title);

			// widget icon and main row click
			Intent mIntent = new Intent(context, MainActivity.class);
			mIntent.setAction(ACTION_NOTE_WIDGET_CALL);
			mIntent.putExtra(WidgetHelper.EXTRA_WIDGET_ITEM_KEY, "note_main");
			mIntent.putExtra("stage_id", -1);
			PendingIntent mPIntent = PendingIntent.getActivity(context,
					REQUEST_CODE, mIntent, 0);
			mView.setOnClickPendingIntent(R.id.widgetNoteImgLauncher, mPIntent);
			mView.setOnClickPendingIntent(R.id.widgetNoteTopBar, mPIntent);

			// new note
			Intent intent = new Intent(context, NoteComposeActivity.class);
			PendingIntent pIntent = PendingIntent.getActivity(context, 0,
					intent, 0);
			mView.setOnClickPendingIntent(R.id.imgBtnNoteWidgetCompose, pIntent);

			manager.updateAppWidget(widgetId, mView);
		}
	}
}
