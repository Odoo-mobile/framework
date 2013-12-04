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
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
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
import com.openerp.auth.OpenERPAccountManager;
import com.openerp.orm.OEHelper;
import com.openerp.providers.note.NoteProvider;
import com.openerp.receivers.SyncFinishReceiver;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.FragmentHandler;
import com.openerp.support.JSONDataHelper;
import com.openerp.support.OEUser;
import com.openerp.support.listview.OEListViewAdapter;
import com.openerp.support.listview.OEListViewOnCreateListener;
import com.openerp.support.listview.OEListViewRows;
import com.openerp.util.drawer.DrawerItem;
import com.openerp.util.tags.TagsView;
import com.openerp.widget.Mobile_Widget;

public class Note extends BaseFragment implements
		PullToRefreshAttacher.OnRefreshListener {

	public static String isStateExist = null;
	public static String TAG = "com.openerp.addons.Note";
	public FragmentHandler fragmentHandler;
	private PullToRefreshAttacher mPullAttacher;
	View rootView = null;
	TextView noteSyncProcessText, emptyNotesText;
	ListView lstNotes = null;
	OEListViewAdapter listAdapter = null;
	List<OEListViewRows> listRows = null;
	NoteDBHelper db = null;
	JSONObject res = null;
	SwipeDismissListViewTouchListener touchListener = null;
	static HashMap<String, Integer> stage_colors = new HashMap<String, Integer>();
	LinkedHashMap<String, String> stages = null;
	private static final int NOTE_ID = 0;
	String[] from = new String[] { "name", "memo", "stage_id" };
	String stage_id = "-1";
	static boolean rawStrikeStatus = false;
	boolean isSynced = false;
	Intent update_widget = null;
	String tag_colors[] = new String[] { "#9933CC", "#669900", "#FF8800",
			"#CC0000", "#59A2BE", "#808080", "#192823", "#0099CC", "#218559",
			"#EBB035" };

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		scope = new AppScope(this);
		db = (NoteDBHelper) getModel();
		setHasOptionsMenu(true);
		rootView = inflater.inflate(R.layout.fragment_note, container, false);
		lstNotes = (ListView) rootView.findViewById(R.id.lstNotes);
		emptyNotesText = (TextView) rootView
				.findViewById(R.id.txvNoteAllArchive);

		update_widget = new Intent();
		update_widget.setAction(Mobile_Widget.TAG);

		if (isStateExist == null) {
			isStateExist = String.valueOf(db.isPadExist());
		}
		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Setting required menu for the action bar
		inflater.inflate(R.menu.menu_fragment_note, menu);
		SearchView searchView = (SearchView) menu.findItem(
				R.id.menu_note_search).getActionView();
		searchView.setOnQueryTextListener(getQueryListener(listAdapter));

		// Hiding unnecesary Menu from action bar
		MenuItem item_write = menu.findItem(R.id.menu_note_write);
		item_write.setVisible(false);
		MenuItem item_cancel = menu.findItem(R.id.menu_note_cancel);
		item_cancel.setVisible(false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_note_compose:
			Intent composeNote = new Intent(scope.context(),
					ComposeNoteActivity.class);
			startActivityForResult(composeNote, NOTE_ID);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case NOTE_ID:
			if (resultCode == Activity.RESULT_OK) {
				int new_id = data.getExtras().getInt("result");
				@SuppressWarnings("unchecked")
				HashMap<String, Object> newRow = ((List<HashMap<String, Object>>) db
						.search(db, new String[] { "id = ?" },
								new String[] { new_id + "" }).get("records"))
						.get(0);
				OEListViewRows listRow = new OEListViewRows(new_id, newRow);
				listRows.add(listRow);
				listAdapter.refresh(listRows);
				lstNotes.setOnTouchListener(touchListener);
				lstNotes.setOnScrollListener(touchListener.makeScrollListener());

				// checking if list view is empty ? if not then
				// Hiding text message of empty list view
				emptyNotesText.setVisibility(View.GONE);
			}
			break;
		}
	}

	@Override
	public Object databaseHelper(Context context) {
		return new NoteDBHelper(context);
	}

	@Override
	public void onStart() {
		super.onStart();
		Bundle bundle = getArguments();
		if (bundle != null) {
			setNoteStages(scope.context());
			stage_id = bundle.getString("stage");
			if (bundle.containsKey("tag_color")) {
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
	public List<DrawerItem> drawerMenus(Context context) {
		List<DrawerItem> drawerItems = new ArrayList<DrawerItem>();
		db = (NoteDBHelper) databaseHelper(context);
		if (db.getOEInstance().isInstalled("note.note")) {
			drawerItems.add(new DrawerItem(TAG, "Notes", true));
			setNoteStages(context);
			// Setting list of stages under Note in Drawable menu
			drawerItems.add(new DrawerItem(TAG, "Notes",
					getCount("-1", context), R.drawable.ic_action_notes,
					getFragBundle("stage", "-1")));
			drawerItems
					.add(new DrawerItem(TAG, "Archive", 0,
							R.drawable.ic_action_archive, getFragBundle(
									"stage", "-2")));
			if (stages != null) {
				int i = 0;
				for (String key : stages.keySet()) {
					if (i > tag_colors.length - 1) {
						i = 0;
					}
					drawerItems.add(new DrawerItem(TAG, stages.get(key)
							.toString(), getCount(key, context), tag_colors[i],
							getFragBundle("stage", key)));
					stage_colors.put("stage_" + key,
							Color.parseColor(tag_colors[i]));
					i++;
				}
			}
			return drawerItems;
		} else {
			return null;
		}
	}

	private Note getFragBundle(String key, String val) {

		Note note = new Note();
		Bundle bundle = new Bundle();
		bundle.putString(key, val);
		note.setArguments(bundle);
		return note;
	}

	/* Method For Calculating Notes According Stages */
	public int getCount(String stage_id, Context context) {

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
		scope.context().requestSync(NoteProvider.AUTHORITY);
	}

	// PullToRefresh
	// Allow Activity to pass us it's PullToRefreshAttacher
	void setPullToRefreshAttacher(PullToRefreshAttacher attacher) {
		mPullAttacher = attacher;
	}

	@Override
	public void onResume() {
		super.onResume();
		scope.context().registerReceiver(syncFinishReceiver,
				new IntentFilter(SyncFinishReceiver.SYNC_FINISH));
		rootView.findViewById(R.id.noteSyncWaiter).setVisibility(View.GONE);
		scope.context().sendBroadcast(update_widget);
	}

	@Override
	public void onPause() {
		super.onPause();
		scope.context().unregisterReceiver(syncFinishReceiver);
	}

	/*
	 * Used for Synchronization : Register receiver and unregister receiver
	 * 
	 * SyncFinishReceiver
	 */
	private SyncFinishReceiver syncFinishReceiver = new SyncFinishReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			// Hiding the NoteSyncWaiter view
			rootView.findViewById(R.id.noteSyncWaiter).setVisibility(View.GONE);
			mPullAttacher.setRefreshComplete();

			// Refreshing Menulist [counter] after synchronisation complete
			scope.context().refreshDrawer(TAG, context);
			setupListView(stage_id);
		}
	};

	/* Method for setting list view for Notes according stages */
	private void setupListView(final String stage_id) {

		int[] to = new int[] { R.id.txvNoteListItem, R.id.txvNoteListDetail,
				R.id.txvNoteListTags };
		listRows = new ArrayList<OEListViewRows>();
		if (listRows != null && listRows.size() <= 0) {
			listRows = getListRows(stage_id);
		}
		// Creating instance for listAdapter
		listAdapter = new OEListViewAdapter(scope.context(),
				R.layout.listview_fragment_note_listitem, listRows, from, to,
				db);
		// Telling adapter to clean HTML text for key value
		listAdapter.cleanHtmlToTextOn("memo");
		lstNotes.setAdapter(listAdapter);
		listAdapter.addViewListener(new OEListViewOnCreateListener() {

			@Override
			public View listViewOnCreateListener(int position, View row_view,
					OEListViewRows row_data) {

				View newView = row_view;
				TextView txvTag = (TextView) newView
						.findViewById(R.id.txvNoteListTags);
				TagsView noteTags = (TagsView) newView
						.findViewById(R.id.txv_detailNote_Tags);

				try {
					// String noteID =
					// row_data.getRow_data().get("id").toString();
					// String[] note_tags_items = getNoteTags(
					// String.valueOf(noteID), scope.context());
					// noteTags.showImage(false);
					// for (String tag : note_tags_items) {
					// noteTags.addObject(new TagsItems(0, tag, ""));
					// }
					// if (note_tags_items.length <= 0) {
					noteTags.setVisibility(View.GONE);
					// }
					// Fetching Note Stage and Setting Background color for that
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
				}
				return newView;
			}
		});

		// Setting item click listner for Detail view of selected Note.
		lstNotes.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {

				int rowId = listRows.get(position).getRow_id();
				String rowStatus = listRows.get(position).getRow_data()
						.get("open").toString();
				String stageIds = listRows.get(position).getRow_data()
						.get("stage_id").toString();

				// Creating an instance of DetailNoteFragment
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
						e.printStackTrace();
					}
				}

				fragment.setArguments(selectedNoteID);
				scope.context().fragmentHandler.setBackStack(true, null);
				scope.context().fragmentHandler.replaceFragmnet(fragment);
			}
		});

		// important to write
		mPullAttacher = scope.context().getPullToRefreshAttacher();
		mPullAttacher.setRefreshableView(lstNotes, this);

		// Setting touch listner for swapping the list rows.
		touchListener = new SwipeDismissListViewTouchListener(lstNotes,
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

							// Handling functionality to change note status
							// open --> close OR close --> open
							if (!rawStrikeStatus) {
								strikeNote(rowId, raw_status, scope.context());
								rawStrikeStatus = true;
							} else {
								strikeNote(rowId, raw_status, scope.context());
								rawStrikeStatus = false;
							}

							listRows.remove(position);
							listAdapter.refresh(listRows);

							// Checking whether list view is empty !
							if (listAdapter.isEmpty()) {
								// Setting text for empty archive list view
								if (stage_id.equalsIgnoreCase("-2")) {
									emptyNotesText
											.setText("You don't have any archived notes right now.");
								}
								// Displaying text message of empty list view
								emptyNotesText.setVisibility(View.VISIBLE);
							} else {
								// Hiding text message of empty list view
								emptyNotesText.setVisibility(View.GONE);
							}
						}
					}
				});

		lstNotes.setOnTouchListener(touchListener);
		// Setting this scroll listener is required to ensure that during
		// ListView scrolling,
		// we don't look for swipes.
		lstNotes.setOnScrollListener(touchListener.makeScrollListener());

	}

	/* Method for handling STRIKE/UNSTRIKE functionality of notes */
	public void strikeNote(int note_id, String open, Context context) {
		try {
			JSONArray args = new JSONArray();
			JSONArray id = new JSONArray();
			id.put(note_id);
			args.put(id);
			OEHelper oe = new OEHelper(context, OEUser.current(context));
			db = new NoteDBHelper(context);
			ContentValues values = new ContentValues();

			// Update--> Open[true]-->To-->Close[false]
			if (open.equalsIgnoreCase("true")) {
				res = oe.call_kw("note.note", "onclick_note_is_done", args);
				values.put("open", "false");
				db.write(db, values, note_id);
				Toast.makeText(context, "Moved to Archive.", Toast.LENGTH_LONG)
						.show();
			}
			// Update--> Close[false]-->To-->Open[true]
			else {
				res = oe.call_kw("note.note", "onclick_note_not_done", args);
				values.put("open", "true");
				db.write(db, values, note_id);
				Toast.makeText(context, "Moved to Active notes.",
						Toast.LENGTH_LONG).show();
			}

			// Refreshing list view after synchronisation
			// complete
			scope.context().refreshDrawer(TAG, scope.context());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			scope.context().sendBroadcast(update_widget);
		}
	}

	/* Method for fetching rows of notes by stage_id */
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

		// Handling text message of empty list view
		// exa.
		// "You don't have any notes right now. / You don't have any active notes right now."
		if (total == 0) {
			// Setting text for empty archive list view
			if (stage_id.equalsIgnoreCase("-2")) {
				emptyNotesText
						.setText("You don't have any archived notes right now.");
			}
			emptyNotesText.setVisibility(View.VISIBLE);
		} else {
			emptyNotesText.setVisibility(View.GONE);
		}

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
			if (db.isEmptyTable(db) && !isSynced) {
				isSynced = true;

				// Hiding text message of empty list view
				// due to visibility of sync process message
				emptyNotesText.setVisibility(View.GONE);

				// Handling text message for sync process on startup
				noteSyncProcessText = (TextView) rootView
						.findViewById(R.id.txvMessageHeaderSubtitle);
				noteSyncProcessText.setText("Your notes will appear shortly");
				rootView.findViewById(R.id.noteSyncWaiter).setVisibility(
						View.VISIBLE);

				// requesting to sync..
				scope.context().requestSync(NoteProvider.AUTHORITY);
			}
		}
		return lists;
	}

	/* Method for fetching stages of notes */
	public void setNoteStages(Context context) {

		stages = new LinkedHashMap<String, String>();

		try {
			OEHelper oe = new OEHelper(context,
					OpenERPAccountManager.currentUser(context));
			db = new NoteDBHelper(context);
			NoteDBHelper.NoteStages stagesobj = db.new NoteStages(context);
			int user_id = Integer.parseInt(OpenERPAccountManager.currentUser(
					context).getUser_id());

			JSONObject domain = new JSONObject();
			domain.accumulate(
					"domain",
					new JSONArray("[[\"user_id\",\"=\","
							+ user_id
							+ "],[\"id\",\"not in\","
							+ JSONDataHelper.intArrayToJSONArray(stagesobj
									.localIds(stagesobj)) + "]]"));

			if (stagesobj.isEmptyTable(stagesobj)) {
				oe.syncWithServer(stagesobj, domain);
			}

			HashMap<String, Object> data = stagesobj.search(stagesobj, null,
					null, null, null, null, "id", "ASC");
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String[] getNoteTags(String note_note_id, Context context) {

		String oea_name = OpenERPAccountManager.currentUser(
				MainActivity.context).getAndroidName();
		db = new NoteDBHelper(context);
		List<String> note_tags = new ArrayList<String>();
		List<HashMap<String, Object>> records = db
				.executeSQL(
						"SELECT id,name,oea_name FROM note_tag where id in (select note_tag_id from note_note_note_tag_rel where note_note_id = ? and oea_name = ?) and oea_name = ?",
						new String[] { note_note_id, oea_name, oea_name });

		if (records.size() > 0) {
			for (HashMap<String, Object> row : records) {
				note_tags.add(row.get("name").toString());
			}
		}
		return note_tags.toArray(new String[note_tags.size()]);
	}
}
