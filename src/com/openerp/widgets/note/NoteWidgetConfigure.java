package com.openerp.widgets.note;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.openerp.R;
import com.openerp.addons.note.NoteDB;
import com.openerp.addons.note.NoteDB.NoteStages;
import com.openerp.orm.OEDataRow;
import com.openerp.support.OEUser;
import com.openerp.support.listview.OEListAdapter;

public class NoteWidgetConfigure extends Activity implements
		OnItemClickListener {

	private static final String PREFS_NAME = "com.openerp.widgetsWidgetProvider";
	List<Object> mOptionsList = new ArrayList<Object>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.widget_note_configure_layout);
		setTitle("Widget Configure");
		setResult(RESULT_CANCELED);

		NoteDB note = new NoteDB(this);
		NoteStages stages = note.new NoteStages(this);
		OEDataRow all = new OEDataRow();
		all.put("id", -1);
		all.put("name", "All Notes");
		mOptionsList.add(all);
		mOptionsList.addAll(stages.select());
		initListView();
		if (OEUser.current(this) == null) {
			Toast.makeText(this, "No account found", Toast.LENGTH_LONG).show();
			finish();
		}
	}

	private void initListView() {
		ListView lstOptions = (ListView) findViewById(R.id.widgetNoteConfigure);
		OEListAdapter adapter = new OEListAdapter(this,
				android.R.layout.simple_list_item_1, mOptionsList) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View mView = convertView;
				if (mView == null)
					mView = getLayoutInflater().inflate(getResource(), parent,
							false);
				OEDataRow item = (OEDataRow) mOptionsList.get(position);
				TextView txvTitle = (TextView) mView
						.findViewById(android.R.id.text1);
				txvTitle.setText(item.getString("name"));
				return mView;
			}
		};
		lstOptions.setAdapter(adapter);
		lstOptions.setOnItemClickListener(this);
	}

	static void savePref(Context context, int appWidgetId, String key,
			int stage_id) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(
				PREFS_NAME, 0).edit();
		prefs.putInt(key + "_" + appWidgetId, stage_id);
		prefs.commit();
	}

	static int getPref(Context context, int appWidgetId, String key) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		int value = prefs.getInt(key + "_" + appWidgetId, -1);
		return value;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		int mAppWidgetId = 0;
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		OEDataRow row = (OEDataRow) mOptionsList.get(position);
		savePref(this, mAppWidgetId, "note_filter", row.getInt("id"));
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		NoteWidget.updateNoteWidget(this, appWidgetManager,
				new int[] { mAppWidgetId });

		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		setResult(RESULT_OK, resultValue);
		finish();
	}
}
