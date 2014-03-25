/**
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http://www.openerp.com>)
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * 
 */
package com.openerp.addons.note.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import com.openerp.R;
import com.openerp.addons.note.NoteDB;
import com.openerp.orm.OEDataRow;
import com.openerp.util.HTMLHelper;
import com.openerp.widgets.WidgetHelper;

public class NoteRemoteViewFactory implements RemoteViewsFactory {

	Context mContext = null;
	List<Object> mNoteItems = new ArrayList<Object>();

	String mTagColors[] = new String[] { "#9933CC", "#669900", "#FF8800",
			"#CC0000", "#59A2BE", "#808080", "#192823", "#0099CC", "#218559",
			"#EBB035" };

	HashMap<String, Integer> mStageColors = new HashMap<String, Integer>();
	int stage_id = -1;

	@SuppressLint("InlinedApi")
	public NoteRemoteViewFactory(Context context, Intent intent) {
		mContext = context;
		stage_id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS,
				-1);
	}

	@Override
	public int getCount() {
		return mNoteItems.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public RemoteViews getLoadingView() {
		RemoteViews mView = new RemoteViews(mContext.getPackageName(),
				R.layout.listview_data_loading_progress);
		return mView;
	}

	@Override
	public RemoteViews getViewAt(int position) {
		OEDataRow note = (OEDataRow) mNoteItems.get(position);
		RemoteViews mView = new RemoteViews(mContext.getPackageName(),
				R.layout.widget_note_item_layout);
		mView.setTextViewText(R.id.widgetTxvNoteName, note.getString("name"));
		mView.setTextViewText(R.id.widgetTxvNoteMemo,
				HTMLHelper.htmlToString(note.getString("memo")));
		OEDataRow stage = note.getM2ORecord("stage_id").browse();
		String stage_name = "New";
		if (stage != null) {
			stage_name = stage.getString("name");
			mView.setTextViewText(R.id.widgetTxvNoteStage, stage_name);
			if (mStageColors.containsKey("key_" + stage.getString("id"))) {
				mView.setTextColor(R.id.widgetTxvNoteStage,
						mStageColors.get("key_" + stage.getString("id")));
				mView.setInt(R.id.widgetTxvViewColor, "setBackgroundColor",
						mStageColors.get("key_" + stage.getString("id")));
			}
		}
		final Intent fillInIntent = new Intent();
		fillInIntent.setAction(NoteWidget.ACTION_NOTE_WIDGET_CALL);
		final Bundle bundle = new Bundle();
		bundle.putInt(WidgetHelper.EXTRA_WIDGET_DATA_VALUE, note.getInt("id"));
		fillInIntent.putExtras(bundle);
		mView.setOnClickFillInIntent(R.id.widgetNoteGridClildView, fillInIntent);
		return mView;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public void onCreate() {
		initData();
	}

	@Override
	public void onDataSetChanged() {
		initData();
	}

	private void initData() {
		mNoteItems.clear();
		NoteDB note = new NoteDB(mContext);
		int i = 0;
		String where = "";
		String[] whereArgs = null;
		if (stage_id > -1) {
			where = "open = ? AND stage_id = ?";
			whereArgs = new String[] { "true", stage_id + "" };
		} else {
			where = "open = ?";
			whereArgs = new String[] { "true" };
		}
		for (OEDataRow row : note.select(where, whereArgs, null, null,
				"id DESC")) {
			OEDataRow stage = row.getM2ORecord("stage_id").browse();
			if (stage != null) {
				if (i == mTagColors.length)
					i = 0;
				if (!mStageColors.containsKey("key_" + stage.getString("id"))) {
					mStageColors.put("key_" + stage.getString("id"),
							Color.parseColor(mTagColors[i]));
					i++;
				}
			}
			mNoteItems.add(row);
		}
	}

	@Override
	public void onDestroy() {

	}

}
