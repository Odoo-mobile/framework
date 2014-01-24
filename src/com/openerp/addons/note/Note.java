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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.openerp.MainActivity;
import com.openerp.OESwipeListener.DismissCallbacks;
import com.openerp.OETouchListener;
import com.openerp.R;
import com.openerp.addons.note.NoteDBHelper.NoteFollowers;
import com.openerp.auth.OpenERPAccountManager;
import com.openerp.orm.OEDataRow;
import com.openerp.orm.OEHelper;
import com.openerp.providers.note.NoteProvider;
import com.openerp.receivers.SyncFinishReceiver;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.FragmentHandler;
import com.openerp.support.JSONDataHelper;
import com.openerp.support.OEUser;
import com.openerp.support.listview.OEListViewAdapter;
import com.openerp.support.listview.OEListViewRow;
import com.openerp.util.HTMLHelper;
import com.openerp.util.controls.OEEditText;
import com.openerp.util.controls.OETextView;
import com.openerp.util.drawer.DrawerItem;
import com.openerp.util.logger.OELog;
import com.openerp.util.tags.TagsItem;
import com.openerp.widget.Mobile_Widget;

public class Note extends BaseFragment implements
		OETouchListener.OnPullListener, DismissCallbacks {

	public static String TAG = "com.openerp.addons.Note";
	public FragmentHandler fragmentHandler;
	private OETouchListener mPullAttacher;
	View rootView = null;
	OETextView emptyNotesText;
	GridView notesGrid = null;
	OEListViewAdapter listAdapter = null;
	List<OEListViewRow> listRows = null;
	NoteDBHelper db = null;
	JSONObject res = null;
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
		notesGrid = (GridView) rootView.findViewById(R.id.noteGridView);
		emptyNotesText = (OETextView) rootView
				.findViewById(R.id.txvNoteAllArchive);

		update_widget = new Intent();
		update_widget.setAction(Mobile_Widget.TAG);
		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Setting required menu for the action bar
		inflater.inflate(R.menu.menu_fragment_note, menu);
		SearchView searchView = (SearchView) menu.findItem(
				R.id.menu_note_search).getActionView();
		searchView.setOnQueryTextListener(getQueryListener(listAdapter));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
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
				OEDataRow newRow = db.search(db, new String[] { "id = ?" },
						new String[] { new_id + "" }).get(0);
				OEListViewRow listRow = new OEListViewRow(new_id, newRow);
				listRows.add(listRow);
				// checking if list view is empty ? if not then
				// Hiding text message of empty list view
				emptyNotesText.setVisibility(View.GONE);
			}
			break;
		}
	}

	@Override
	public Object databaseHelper(Context context) {
		NoteDBHelper noteDb = new NoteDBHelper(context);
		NoteFollowers noteFollowerDb = noteDb.new NoteFollowers(context);
		noteFollowerDb.createTable(noteFollowerDb
				.createStatement(noteFollowerDb));
		return noteDb;
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
		rootView.findViewById(R.id.imgBtnCreateQuickNote).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent composeNote = new Intent(scope.context(),
								ComposeNoteActivity.class);
						OEEditText edtTitle = (OEEditText) rootView
								.findViewById(R.id.edtNoteQuickTitle);
						composeNote.putExtra("note_title", edtTitle.getText()
								.toString());
						startActivityForResult(composeNote, NOTE_ID);
						edtTitle.setText(null);
					}
				});

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

	// Pull To sync
	// Allow Activity to pass us it's OETouchListener
	void setPullToRefreshAttacher(OETouchListener attacher) {
		mPullAttacher = attacher;
	}

	@Override
	public void onResume() {
		super.onResume();
		scope.context().registerReceiver(syncFinishReceiver,
				new IntentFilter(SyncFinishReceiver.SYNC_FINISH));
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
			mPullAttacher.setPullComplete();
			// Refreshing Menulist [counter] after synchronisation complete
			scope.main().refreshDrawer(TAG, context);
			setupListView(stage_id);
		}
	};

	/* Method for setting list view for Notes according stages */
	private void setupListView(final String stage_id) {

		listRows = new ArrayList<OEListViewRow>();
		if (listRows != null && listRows.size() <= 0) {
			listRows = getListRows(stage_id);
		}
		listAdapter = new OEListViewAdapter(scope.context(),
				R.layout.fragment_note_grid_custom_layout, listRows, null,
				null, db) {
			View mView = null;

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				mView = convertView;
				if (convertView == null) {
					LayoutInflater inflater = getActivity().getLayoutInflater();
					mView = inflater.inflate(
							R.layout.fragment_note_grid_custom_layout, parent,
							false);
				}
				TextView txvTitle = (TextView) mView
						.findViewById(R.id.txvNoteTitle);
				TextView txvDesc = (TextView) mView
						.findViewById(R.id.txvNoteDescription);
				TextView txvStage = (TextView) mView
						.findViewById(R.id.txvNoteStage);
				OEListViewRow note = listRows.get(position);
				txvTitle.setText(note.getRow_data().getString("name"));
				txvDesc.setText(HTMLHelper.htmlToString(note.getRow_data()
						.getString("memo")));
				String stageInfo = note.getRow_data().getString("stage_id")
						.toString();
				int color = Color.parseColor("#ffffff");
				String stage_name = "New";
				if (!stageInfo.equals("false")) {
					try {
						JSONArray stage_id = new JSONArray(stageInfo);
						String stageid = stage_id.getJSONArray(0).getString(0);
						stage_name = stage_id.getJSONArray(0).getString(1);
						if (stage_colors.containsKey("stage_" + stageid)) {
							color = Integer.parseInt(stage_colors.get(
									"stage_" + stageid).toString());
						}
					} catch (Exception e) {
					}
				}
				mView.findViewById(R.id.noteGridClildView).setBackgroundColor(
						color);
				txvStage.setText(stage_name);
				txvStage.setTextColor(color);

				return mView;
			}
		};
		notesGrid.setAdapter(listAdapter);
		// Setting item click listner for Detail view of selected Note.
		notesGrid.setOnItemClickListener(new OnItemClickListener() {
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
				selectedNoteID.putInt("note_id", rowId);
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
				scope.main().fragmentHandler.setBackStack(true, null);
				scope.main().fragmentHandler.replaceFragmnet(fragment);
			}
		});

		// important to write
		mPullAttacher = scope.main().getTouchAttacher();
		mPullAttacher.setPullableView(notesGrid, this);
		mPullAttacher.setSwipeableView(notesGrid, this);
		notesGrid.setOnScrollListener(mPullAttacher.makeScrollListener());

	}

	/* Method for handling STRIKE/UNSTRIKE functionality of notes */
	public void strikeNote(int note_id, String open, AppScope scope) {
		try {
			JSONArray args = new JSONArray();
			JSONArray id = new JSONArray();
			id.put(note_id);
			args.put(id);
			OEHelper oe = new OEHelper(scope.context(), OEUser.current(scope
					.context()));
			db = new NoteDBHelper(scope.context());
			ContentValues values = new ContentValues();

			// Update--> Open[true]-->To-->Close[false]
			if (open.equalsIgnoreCase("true")) {
				res = oe.call_kw("note.note", "onclick_note_is_done", args);
				values.put("open", "false");
				db.write(db, values, note_id);
				Toast.makeText(scope.context(), "Moved to Archive.",
						Toast.LENGTH_LONG).show();
			}
			// Update--> Close[false]-->To-->Open[true]
			else {
				res = oe.call_kw("note.note", "onclick_note_not_done", args);
				values.put("open", "true");
				db.write(db, values, note_id);
				Toast.makeText(scope.context(), "Moved to Active notes.",
						Toast.LENGTH_LONG).show();
			}

			// Refreshing list view after synchronisation
			// complete
			scope.main().refreshDrawer(TAG, scope.context());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			update_widget = new Intent();
			update_widget.setAction(Mobile_Widget.TAG);
			scope.context().sendBroadcast(update_widget);
		}
	}

	/* Method for fetching rows of notes by stage_id */
	private List<OEListViewRow> getListRows(String stage_id) {

		List<OEListViewRow> lists = new ArrayList<OEListViewRow>();
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

		List<OEDataRow> results = getModel().search(db, where, whereArgs);
		int total = results.size();

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
			for (OEDataRow row_data : results) {
				OEListViewRow row = new OEListViewRow(row_data.getInt("id"),
						row_data);
				lists.add(row);
			}
		} else {
			if (db.isEmptyTable(db) && !isSynced) {
				isSynced = true;
				// Hiding text message of empty list view
				// due to visibility of sync process message
				emptyNotesText.setVisibility(View.GONE);

				// requesting to sync..
				scope.main().requestSync(NoteProvider.AUTHORITY);
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

			List<OEDataRow> data = stagesobj.search(stagesobj, null, null,
					null, null, null, "id", "ASC");
			int total = data.size();

			if (total > 0) {
				for (OEDataRow row_data : data) {
					String row_id = row_data.get("id").toString();
					String name = row_data.get("name").toString();
					stages.put(row_id, name);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<TagsItem> getNoteTags(String note_note_id, Context context) {

		String oea_name = OpenERPAccountManager.currentUser(
				MainActivity.context).getAndroidName();
		db = new NoteDBHelper(context);
		List<TagsItem> note_tags = new ArrayList<TagsItem>();
		List<OEDataRow> records = db
				.executeSQL(
						"SELECT id,name,oea_name FROM note_tag where id in (select note_tag_id from note_note_note_tag_rel where note_note_id = ? and oea_name = ?) and oea_name = ?",
						new String[] { note_note_id, oea_name, oea_name });

		if (records.size() > 0) {
			for (OEDataRow row : records) {
				note_tags.add(new TagsItem(row.getInt("id"), row
						.getString("name"), null));
			}
		}
		return note_tags;
	}

	@Override
	public void onPullStarted(View arg0) {
		scope.main().requestSync(NoteProvider.AUTHORITY);
	}

	@Override
	public boolean canDismiss(int arg0) {
		return true;
	}

	@Override
	public void onDismiss(View view, int[] reverseSortedPositions) {
		for (int position : reverseSortedPositions) {
			int rowId = listRows.get(position).getRow_id();
			String raw_status = listRows.get(position).getRow_data()
					.get("open").toString();

			// Handling functionality to change note status
			// open --> close OR close --> open
			if (!rawStrikeStatus) {
				strikeNote(rowId, raw_status, scope);
				rawStrikeStatus = true;
			} else {
				strikeNote(rowId, raw_status, scope);
				rawStrikeStatus = false;
			}
			listRows.remove(position);

			// Checking whether list view is empty !

			if (listAdapter.isEmpty()) {
				// Setting text for empty archive list view
				if (stage_id.equalsIgnoreCase("-2")) {
					emptyNotesText
							.setText("You don't have any archived notes right now.");
				} // Displaying text message of empty list view
				emptyNotesText.setVisibility(View.VISIBLE);
			} else { // Hiding text message of empty list view
				emptyNotesText.setVisibility(View.GONE);
			}
			listAdapter.refresh(listRows);
		}
	}
}
