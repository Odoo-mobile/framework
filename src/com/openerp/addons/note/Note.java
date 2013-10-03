/*
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
package com.openerp.addons.note;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.openerp.MainActivity;
import com.openerp.PullToRefreshAttacher;
import com.openerp.R;
import com.openerp.orm.OEHelper;
import com.openerp.providers.note.NoteProvider;
import com.openerp.receivers.SyncFinishReceiver;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.FragmentHandler;
import com.openerp.support.listview.OEListViewAdapter;
import com.openerp.support.listview.OEListViewOnCreateListener;
import com.openerp.support.listview.OEListViewRows;
import com.openerp.support.menu.OEMenu;
import com.openerp.support.menu.OEMenuItems;

public class Note extends BaseFragment implements
		PullToRefreshAttacher.OnRefreshListener {
	String tag_colors[] = new String[] { "#9933CC", "#669900", "#FF8800",
			"#CC0000", "#59A2BE", "#808080", "#192823", "#0099CC", "#218559",
			"#EBB035" };
	static HashMap<String, Integer> stage_colors = new HashMap<String, Integer>();
	OEListViewAdapter listAdapter = null;
	ListView lstNotes = null;
	View rootView = null;
	HashMap<String, String> stages = null;
	private PullToRefreshAttacher mPullAttacher;
	List<OEListViewRows> listRows = null;
	static boolean rawStrikeStatus = false;
	String stage_id = "-1";
	public FragmentHandler fragmentHandler;
	NoteDBHelper db;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		setHasOptionsMenu(true);
		scope = new AppScope(MainActivity.userContext,
				(MainActivity) getActivity());
		db = (NoteDBHelper) getModel();
		rootView = inflater.inflate(R.layout.fragment_note, container, false);
		lstNotes = (ListView) rootView.findViewById(R.id.lstNotes);
		handleArguments((Bundle) getArguments());
		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.menu_fragment_note, menu);
		SearchView searchView = (SearchView) menu.findItem(
				R.id.menu_note_search).getActionView();
		searchView.setOnQueryTextListener(getQueryListener(listAdapter));

		// disabling the menu
		MenuItem item_write = menu.findItem(R.id.menu_note_write);
		item_write.setVisible(false);
		MenuItem item_cancel = menu.findItem(R.id.menu_note_cancel);
		item_cancel.setVisible(false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_note_compose:

			// Create an instance of ComposeNoteFragment
			Fragment fragment = new ComposeNoteFragment();
			scope.context().fragmentHandler.setBackStack(true, null);
			scope.context().fragmentHandler.replaceFragmnet(fragment);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public Object databaseHelper(Context context) {
		// TODO Auto-generated method stub
		return new NoteDBHelper(context);
	}

	@Override
	public void handleArguments(Bundle bundle) {
		// TODO Auto-generated method stub
		if (bundle != null) {
			setNoteStages(scope.context());
			stage_id = bundle.getString("stage");
			if (bundle.containsKey("tag_color")) {
				// current_stage_color = bundle.getInt("tag_color");
				stage_colors.put("stage_" + stage_id,
						bundle.getInt("tag_color"));
			}

			setupListView(stage_id);
		} else {
			setNoteStages(scope.context());
			setupListView("-1");
		}
	}

	@Override
	public OEMenu menuHelper(Context context) {
		// TODO Auto-generated method stub

		OEMenu menu = new OEMenu();
		menu.setId(1);
		menu.setMenuTitle("Notes");
		setNoteStages(context);
		List<OEMenuItems> items = new ArrayList<OEMenuItems>();
		items.add(new OEMenuItems("All", getFragBundle("stage", "-1"),
				getCount("-1", context)));
		items.add(new OEMenuItems("Archive", getFragBundle("stage", "-2"), 0));
		if (stages != null) {
			int i = 0;
			for (String key : stages.keySet()) {
				if (i > tag_colors.length - 1) {
					i = 0;
				}
				OEMenuItems stageMenu = new OEMenuItems(stages.get(key)
						.toString(), getFragBundle("stage", key), getCount(key,
						context));
				stageMenu.setAutoMenuTagColor(true);
				stageMenu.setMenuTagColor(Color.parseColor(tag_colors[i]));
				stage_colors.put("stage_" + key, stageMenu.getMenuTagColor());
				items.add(stageMenu);
				i++;
			}
		}
		menu.setMenuItems(items);
		return menu;

	}

	private Note getFragBundle(String key, String val) {
		Note note = new Note();
		Bundle bundle = new Bundle();
		bundle.putString(key, val);
		note.setArguments(bundle);
		return note;
	}

	private int getCount(String stage_id, Context context) {
		int count = 0;
		db = new NoteDBHelper(context);
		String[] where = null;
		String[] whereArgs = null;
		if (stage_id.equals("-1")) {
			where = new String[] { "open = ?" };
			whereArgs = new String[] { "true" };
		} else {
			where = new String[] { "open = ?", "AND", "stage_id = ? " };
			whereArgs = new String[] { "true", stage_id };
		}
		count = db.count(db, where, whereArgs);
		return count;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		rootView = null; // now cleaning up!
	}

	@Override
	public void onRefreshStarted(View view) {
		// TODO Auto-generated method stub
		scope.context().requestSync(NoteProvider.AUTHORITY);
	}

	void setPullToRefreshAttacher(PullToRefreshAttacher attacher) {
		mPullAttacher = attacher;

	}

	@Override
	public void onResume() {
		super.onResume();
		scope.context().registerReceiver(syncFinishReceiver,
				new IntentFilter(SyncFinishReceiver.SYNC_FINISH));
	}

	@Override
	public void onPause() {
		super.onPause();
		scope.context().unregisterReceiver(syncFinishReceiver);
	}

	private SyncFinishReceiver syncFinishReceiver = new SyncFinishReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mPullAttacher.setRefreshComplete();
			// refreshing list view after synchronisation
			scope.context().refreshMenu(context);
			setupListView(stage_id);
		}
	};

	private void setupListView(String stage_id) {

		String[] from = new String[] { "name", "memo", "stage_id" };
		int[] to = new int[] { R.id.txvNoteListItem, R.id.txvNoteListDetail,
				R.id.txvNoteListTags };
		listRows = new ArrayList<OEListViewRows>();

		if (listRows != null && listRows.size() <= 0) {
			listRows = getListRows(stage_id);
		}

		listAdapter = new OEListViewAdapter(scope.context(),
				R.layout.listview_fragment_note_listitem, listRows, from, to,
				db);
		listAdapter.cleanHtmlToTextOn("memo");
		lstNotes.setAdapter(listAdapter);
		listAdapter.addViewListener(new OEListViewOnCreateListener() {

			@Override
			public View listViewOnCreateListener(int position, View row_view,
					OEListViewRows row_data) {
				View newView = row_view;
				TextView txvTag = (TextView) newView
						.findViewById(R.id.txvNoteListTags);
				try {
					String stageInfo = row_data.getRow_data().get("stage_id")
							.toString();
					if (!stageInfo.equals("false")) {
						JSONArray stage_id = new JSONArray(stageInfo);
						String stageid = stage_id.getJSONArray(0).getString(0);
						if (stage_colors.containsKey("stage_" + stageid)) {
							txvTag.setBackgroundColor(Integer
									.parseInt(stage_colors.get(
											"stage_" + stageid).toString()));
						}
					} else {
						txvTag.setBackgroundColor(Color.parseColor("#ffffff"));
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

				return newView;
			}
		});
		lstNotes.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub
				int rowId = listRows.get(position).getRow_id();
				String rowStatus = listRows.get(position).getRow_data()
						.get("open").toString();
				String stageIds = listRows.get(position).getRow_data()
						.get("stage_id").toString();

				// Create an instance of DetailNoteFragment
				DetailNoteFragment fragment = new DetailNoteFragment();
				Bundle selectedNoteID = new Bundle();
				selectedNoteID.putInt("row_id", rowId);
				selectedNoteID.putString("row_status", rowStatus);
				selectedNoteID.putString("stage_id", stageIds);
				if (!stageIds.equals("false")) {
					try {
						JSONArray stage_id = new JSONArray(stageIds);
						String stageid = stage_id.getJSONArray(0).getString(0);
						if (stage_colors.containsKey("stage_" + stageid)) {
							selectedNoteID.putInt("stage_color",
									stage_colors.get("stage_" + stageid));
						}
					} catch (Exception e) {
					}
				}

				fragment.setArguments(selectedNoteID);
				scope.context().fragmentHandler.setBackStack(true, null);
				scope.context().fragmentHandler.replaceFragmnet(fragment);
			}
		});

		mPullAttacher = scope.context().getPullToRefreshAttacher();
		mPullAttacher.setRefreshableView(lstNotes, this);

		SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(
				lstNotes,
				new SwipeDismissListViewTouchListener.DismissCallbacks() {
					@Override
					public boolean canDismiss(int position) {
						return true;
					}

					@Override
					public void onDismiss(ListView listView,
							int[] reverseSortedPositions) {
						for (int position : reverseSortedPositions) {
							int rowId = listRows.get(position).getRow_id();
							String raw_status = listRows.get(position)
									.getRow_data().get("open").toString();
							if (!rawStrikeStatus) {
								strikeNote(rowId, raw_status, scope.context());
								rawStrikeStatus = true;
							} else {
								strikeNote(rowId, raw_status, scope.context());
								rawStrikeStatus = false;
							}
							listRows.remove(position);
							listAdapter.refresh(listRows);
						}
					}
				});

		lstNotes.setOnTouchListener(touchListener);
		// Setting this scroll listener is required to ensure that during
		// ListView scrolling,
		// we don't look for swipes.
		lstNotes.setOnScrollListener(touchListener.makeScrollListener());

	}

	// will used to do STRIKE/UNSTRIKE the note
	public void strikeNote(int note_id, String open, Context context) {
		try {

			JSONArray args = new JSONArray();
			JSONArray id = new JSONArray();
			id.put(note_id);
			args.put(id);
			OEHelper oe = new OEHelper(context, MainActivity.userContext);
			db = new NoteDBHelper(context);
			ContentValues values = new ContentValues();

			// Update--> Open[true]-->To-->Close[false]
			if (open.equalsIgnoreCase("true")) {
				JSONObject res = oe.call_kw("note.note",
						"onclick_note_is_done", args);
				values.put("open", "false");
				db = new NoteDBHelper(context);
				db.write(db, values, note_id);
				Toast.makeText(context, "Move to Archive", Toast.LENGTH_SHORT)
						.show();
			}
			// Update--> Close[false]-->To-->Open[true]
			else {
				JSONObject res = oe.call_kw("note.note",
						"onclick_note_not_done", args);
				values.put("open", "true");
				db.write(db, values, note_id);
				Toast.makeText(context, "Move to All", Toast.LENGTH_SHORT)
						.show();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List<OEListViewRows> getListRows(String stage_id) {
		List<OEListViewRows> lists = new ArrayList<OEListViewRows>();

		String[] where = null;
		String[] whereArgs = null;
		if (!stage_id.equals("-1") && !stage_id.equals("-2")) {
			where = new String[] { "open = ? ", "AND ", "stage_id = ?" };
			whereArgs = new String[] { "true", stage_id };
		} else if (stage_id.equals("-2")) {
			where = new String[] { "open = ? " };
			whereArgs = new String[] { "false" };

		} else {
			where = new String[] { "open = ? " };
			whereArgs = new String[] { "true" };
		}

		HashMap<String, Object> results = getModel().search(db, where,
				whereArgs);
		int total = Integer.parseInt(results.get("total").toString());
		if (total > 0) {
			@SuppressWarnings("unchecked")
			List<HashMap<String, Object>> rows = (List<HashMap<String, Object>>) results
					.get("records");

			for (HashMap<String, Object> row_data : rows) {
				OEListViewRows row = new OEListViewRows(
						Integer.parseInt(row_data.get("id").toString()),
						row_data);
				lists.add(row);
			}
		} else {
			if (db.isEmptyTable(db)) {
				// request to sync..
				scope.context().requestSync(NoteProvider.AUTHORITY);
			}
		}
		return lists;
	}

	public void setNoteStages(Context context) {
		stages = new HashMap<String, String>();
		db = new NoteDBHelper(context);
		NoteDBHelper.NoteStages stagesobj = db.new NoteStages(context);
		HashMap<String, Object> data = stagesobj.search(stagesobj);

		int total = Integer.parseInt(data.get("total").toString());
		if (total > 0) {
			@SuppressWarnings("unchecked")
			List<HashMap<String, Object>> rows = (List<HashMap<String, Object>>) data
					.get("records");
			for (HashMap<String, Object> row_data : rows) {
				String row_id = row_data.get("id").toString();
				String name = row_data.get("name").toString();
				stages.put(row_id, name);
			}
		}
	}
}
