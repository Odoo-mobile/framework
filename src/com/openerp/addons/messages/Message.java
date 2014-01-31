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
package com.openerp.addons.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.openerp.OETouchListener;
import com.openerp.R;
import com.openerp.orm.OEDataRow;
import com.openerp.orm.OEHelper;
import com.openerp.providers.message.MessageProvider;
import com.openerp.receivers.DataSetChangeReceiver;
import com.openerp.receivers.SyncFinishReceiver;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.JSONDataHelper;
import com.openerp.support.OEArgsHelper;
import com.openerp.support.OpenERPServerConnection;
import com.openerp.support.listview.OEListAdapter;
import com.openerp.support.listview.OEListViewAdapter;
import com.openerp.support.listview.OEListViewRow;
import com.openerp.util.HTMLHelper;
import com.openerp.util.OEDate;
import com.openerp.util.controls.OETextView;
import com.openerp.util.drawer.DrawerItem;

public class Message extends BaseFragment implements
		OETouchListener.OnPullListener, OnItemLongClickListener,
		OnItemClickListener {

	public static String TAG = "com.openerp.addons.Message";

	private OETouchListener mTouchAttacher;
	ActionMode mActionMode;
	View rootView = null;
	SearchView searchView = null;

	String type = "inbox";
	MessagesLoader mMessageLoader = null;
	StarredOperation mStarredOperation = null;
	@SuppressLint("UseSparseArrays")
	HashMap<Integer, Boolean> mMultiSelectedRows = new HashMap<Integer, Boolean>();
	ReadUnreadOperation mReadUnreadOperation = null;

	OEListAdapter mListViewAdapter = null;

	ListView lstMessagesView = null;
	OEListViewAdapter listAdapter = null;
	List<Object> mMessageObjects = new ArrayList<Object>();

	HashMap<String, Integer> message_row_indexes = new HashMap<String, Integer>();
	HashMap<String, Integer> message_model_colors = new HashMap<String, Integer>();

	int[] background_res = new int[] {
			R.drawable.message_listview_bg_toread_selector,
			R.drawable.message_listview_bg_tonotread_selector };

	int[] starred_drawables = new int[] { R.drawable.ic_action_starred,
			R.drawable.ic_action_unstarred };

	String[] from = new String[] { "id", "subject", "body", "record_name",
			"type", "to_read", "starred", "author_id", "res_id", "email_from",
			"parent_id", "model", "date" };

	String tag_colors[] = new String[] { "#A4C400", "#00ABA9", "#1BA1E2",
			"#AA00FF", "#D80073", "#A20025", "#FA6800", "#6D8764", "#76608A",
			"#EBB035" };

	int tag_color_count = 0;
	boolean isSynced = false;

	public enum Type {
		INBOX, TODO, TOME, ARCHIVE, GROUP
	}

	private String mGroupId = null;

	Type mType = Type.INBOX;
	public int selectedCounter = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		/*
		 * setHasOptionsMenu()
		 * 
		 * REQUIRED
		 * 
		 * Report that this fragment would like to participate in populating the
		 * options menu by receiving a call to onCreateOptionsMenu(Menu,
		 * MenuInflater) and related methods.
		 */
		setHasOptionsMenu(true);

		/*
		 * scope
		 * 
		 * REQUIRED
		 * 
		 * Initialise application context (MainActivity), Current User Object,
		 * and database object for current fragment
		 * 
		 * @param UserObject
		 * 
		 * @param MainActivity
		 */
		scope = new AppScope(this);

		/*
		 * db
		 * 
		 * REQUIRED
		 * 
		 * Instance of BaseDBHelper Used to handle all local database operations
		 */
		db = (MessageDBHelper) getModel();

		/*
		 * rowView
		 * 
		 * Inflate the layout for this fragment
		 */

		rootView = inflater
				.inflate(R.layout.fragment_message, container, false);
		/*
		 * handleArguments(Bundle)
		 * 
		 * see method for more information about it.
		 */
		scope.main().setAutoSync(MessageProvider.AUTHORITY, true);
		initView();

		return rootView;
	}

	private void initView() {
		lstMessagesView = (ListView) rootView.findViewById(R.id.lstMessages);
		lstMessagesView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		lstMessagesView.setOnItemLongClickListener(this);
		lstMessagesView.setOnItemClickListener(this);
		lstMessagesView
				.setMultiChoiceModeListener(mMessageViewMultiChoiceListener);
		mListViewAdapter = new OEListAdapter(getActivity(),
				R.layout.fragment_message_listview_items, mMessageObjects) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View mView = convertView;
				if (mView == null) {
					mView = getActivity().getLayoutInflater().inflate(
							getResource(), parent, false);
				}
				mView = handleRowView(mView, position);
				return mView;
			}
		};

		lstMessagesView.setAdapter(mListViewAdapter);
		// Getting Pull To Refresh Attacher from Main Activity
		mTouchAttacher = scope.main().getTouchAttacher();

		// Set the Refreshable View to be the ListView and the refresh listener
		// to be this.
		if (mTouchAttacher != null) {
			mTouchAttacher.setPullableView(lstMessagesView, this);
		}
	}

	// Handling each row view
	private View handleRowView(View mView, final int position) {
		final OEListViewRow row = (OEListViewRow) mMessageObjects.get(position);

		boolean to_read = row.getRow_data().getBoolean("to_read");
		mView.setBackgroundResource((to_read) ? background_res[1]
				: background_res[0]);

		TextView txvSubject, txvBody, txvFrom, txvDate, txvTag;
		final ImageView imgStarred = (ImageView) mView
				.findViewById(R.id.imgMessageStarred);

		final boolean starred = row.getRow_data().getBoolean("starred");
		imgStarred.setImageResource((starred) ? starred_drawables[0]
				: starred_drawables[1]);
		imgStarred.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Handling Starred click event
				mMultiSelectedRows.put(position, true);
				mStarredOperation = new StarredOperation((starred) ? false
						: true);
				mStarredOperation.execute();
			}
		});

		txvSubject = (TextView) mView.findViewById(R.id.txvMessageSubject);
		txvBody = (TextView) mView.findViewById(R.id.txvMessageBody);
		txvFrom = (TextView) mView.findViewById(R.id.txvMessageFrom);
		txvDate = (TextView) mView.findViewById(R.id.txvMessageDate);
		txvTag = (TextView) mView.findViewById(R.id.txvMessageTag);

		if (!to_read) {
			txvSubject.setTextColor(Color.BLACK);
			txvFrom.setTextColor(Color.BLACK);
		} else {
			txvSubject.setTextColor(Color.parseColor("#414141"));
			txvFrom.setTextColor(Color.parseColor("#414141"));
		}

		txvSubject.setText(row.getRow_data().getString("subject"));
		txvBody.setText(HTMLHelper.htmlToString(row.getRow_data().getString(
				"body")));
		String date = row.getRow_data().getString("date");
		txvDate.setText(OEDate.getDate(date, TimeZone.getDefault().getID()));

		String from = row.getRow_data().getString("email_from");
		if (from.equals("false")) {
			from = row.getRow_data().getIdName("author_id").getName();
		}
		txvFrom.setText(from);

		String model_name = row.getRow_data().getString("model");
		if (model_name.equals("false")) {
			model_name = capitalizeString(row.getRow_data().getString("type"));
		} else {
			String[] model_parts = TextUtils.split(model_name, "\\.");
			@SuppressWarnings({ "unchecked", "rawtypes" })
			HashSet unique_parts = new HashSet(Arrays.asList(model_parts));
			model_name = capitalizeString(TextUtils.join(" ",
					unique_parts.toArray()));
		}
		int tag_color = 0;
		if (message_model_colors.containsKey(model_name)) {
			tag_color = message_model_colors.get(model_name);
		} else {
			tag_color = Color.parseColor(tag_colors[tag_color_count]);
			message_model_colors.put(model_name, tag_color);
			tag_color_count++;
			if (tag_color_count > tag_colors.length) {
				tag_color_count = 0;
			}
		}
		if (model_name.equals("mail.group")) {
			String res_id = row.getRow_data().getString("res_id");
			if (UserGroups.group_names.containsKey("group_" + res_id)) {
				model_name = UserGroups.group_names.get("group_" + res_id);
				tag_color = UserGroups.menu_color.get("group_" + res_id);
			}
		}
		txvTag.setBackgroundColor(tag_color);
		txvTag.setText(model_name);

		return mView;
	}

	public static String capitalizeString(String string) {
		char[] chars = string.toLowerCase().toCharArray();
		boolean found = false;
		for (int i = 0; i < chars.length; i++) {
			if (!found && Character.isLetter(chars[i])) {
				chars[i] = Character.toUpperCase(chars[i]);
				found = true;
			} else if (Character.isWhitespace(chars[i]) || chars[i] == '.'
					|| chars[i] == '\'') { // You can add other chars here
				found = false;
			}
		}
		return String.valueOf(chars);
	}

	private String updateSubject(String subject, int parent_id) {
		String newSubject = subject;
		if (subject.equals("false")) {
			newSubject = "message";
		}
		int total_child = db.count(db, new String[] { "parent_id = ? " },
				new String[] { String.valueOf(parent_id) });
		if (total_child > 0) {
			newSubject += " (" + total_child + ") ";
		}
		return newSubject;
	}

	int message_resource = 0;

	private void checkMessageStatus() {

		// Fetching parent ids from Child row with order by date desc
		if (mMessageObjects.size() == 0) {
			if (db.isEmptyTable(db) && !isSynced) {
				isSynced = true;
				scope.main().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (rootView.findViewById(R.id.waitingForSyncToStart) != null) {
							rootView.findViewById(R.id.waitingForSyncToStart)
									.setVisibility(View.VISIBLE);
						}
					}
				});

				try {
					Thread.sleep(2000);
					if (mGroupId != null) {
						Bundle group_bundle = new Bundle();
						JSONArray ids = new JSONArray();
						ids.put(mGroupId);
						group_bundle.putString("group_ids", ids.toString());
						scope.main().requestSync(MessageProvider.AUTHORITY,
								group_bundle);
					} else {
						scope.main().requestSync(MessageProvider.AUTHORITY);
					}
				} catch (Exception e) {
				}
			} else {
				scope.main().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						rootView.findViewById(R.id.waitingForSyncToStart)
								.setVisibility(View.GONE);
						OETextView txvMsg = (OETextView) rootView
								.findViewById(R.id.txvMessageAllReadMessage);
						txvMsg.setVisibility(View.VISIBLE);
						txvMsg.setText(message_resource);
					}
				});

			}

		}
	}

	@Override
	public Object databaseHelper(Context context) {
		return new MessageDBHelper(context);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_fragment_message, menu);
		// Associate searchable configuration with the SearchView
		searchView = (SearchView) menu.findItem(R.id.menu_message_search)
				.getActionView();
		Bundle bundle = getArguments();
		if (bundle != null && bundle.containsKey("group_id")) {
			MenuItem compose = menu.findItem(R.id.menu_message_compose);
			compose.setVisible(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection
		switch (item.getItemId()) {
		case R.id.menu_message_compose:
			scope.context().startActivity(
					new Intent(scope.context(), MessageComposeActivty.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

		// Called when the action mode is created; startActionMode() was called
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Inflate a menu resource providing context menu items
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.menu_fragment_message_context, menu);
			return true;
		}

		// Called each time the action mode is shown. Always called after
		// onCreateActionMode, but
		// may be called multiple times if the mode is invalidated.
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false; // Return false if nothing is done
		}

		// Called when the user selects a contextual menu item
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			Log.e(">> CLICKED", "Context Menu Clicked");

			switch (item.getItemId()) {

			default:
				return false;
			}
		}

		// Called when the user exits the action mode
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
		}
	};

	private String[] getWhereClause(Type type) {
		String where[] = null;
		switch (type) {
		case INBOX:
			where = new String[] { "to_read = 'true'", "AND",
					"starred  = 'false'" };
			message_resource = R.string.message_inbox_all_read;
			break;
		case TOME:
			where = new String[] { "res_id = '0' ", "AND", "to_read= 'true'", };
			message_resource = R.string.message_tome_all_read;
			break;
		case TODO:
			where = new String[] { "starred  = 'true' ", "AND",
					"to_read = 'true'" };
			message_resource = R.string.message_todo_all_read;
			break;
		case GROUP:
			where = new String[] { "res_id  =  " + mGroupId, "AND",
					"model = 'mail.group'" };
			message_resource = R.string.message_no_group_message;
			break;
		default:
			break;
		}
		return where;
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		List<DrawerItem> drawerItems = new ArrayList<DrawerItem>();
		db = (MessageDBHelper) databaseHelper(context);
		if (db.getOEInstance().isInstalled("mail.message")) {
			drawerItems.add(new DrawerItem(TAG, "Messages", true));
			drawerItems.add(new DrawerItem(TAG, "Inbox", getCount(Type.INBOX,
					context), R.drawable.ic_action_inbox, getObjectOFClass(
					"type", "inbox")));
			drawerItems.add(new DrawerItem(TAG, "To: me", getCount(Type.TOME,
					context), R.drawable.ic_action_user, getObjectOFClass(
					"type", "to-me")));
			drawerItems.add(new DrawerItem(TAG, "To-do", getCount(Type.TODO,
					context), R.drawable.ic_action_todo, getObjectOFClass(
					"type", "to-do")));
			drawerItems.add(new DrawerItem(TAG, "Archives", 0,
					R.drawable.ic_action_archive, getObjectOFClass("type",
							"archive")));
			return drawerItems;
		} else {
			return null;
		}
	}

	public int getCount(Type type, Context context) {
		db = new MessageDBHelper(context);
		int count = 0;
		String[] where = getWhereClause(type);
		if (where != null) {
			count = db.count(db, where, null);
		}
		return count;
	}

	public Message getObjectOFClass(String key, String value) {
		Message frag = new Message();
		Bundle bundle = new Bundle();
		bundle.putString(key, value);
		frag.setArguments(bundle);
		return frag;
	}

	@Override
	public void onStart() {
		super.onStart();
		Bundle bundle = getArguments();
		if (bundle != null) {
			if (bundle.containsKey("type")) {
				type = bundle.getString("type");
				String title = "Archive";
				if (type.equals("inbox")) {
					// setupListView(Type.INBOX);
					mMessageLoader = new MessagesLoader(Type.INBOX);
					mMessageLoader.execute((Void) null);
					title = "Inbox";
				} else if (type.equals("to-me")) {
					title = "To-Me";
					// setupListView(Type.TOME);
					mMessageLoader = new MessagesLoader(Type.TOME);
					mMessageLoader.execute((Void) null);
				} else if (type.equals("to-do")) {
					// setupListView(Type.TODO);
					title = "To-DO";
					mMessageLoader = new MessagesLoader(Type.TODO);
					mMessageLoader.execute((Void) null);
				} else if (type.equals("archive")) {
					// setupListView(Type.ARCHIVE);
					mMessageLoader = new MessagesLoader(Type.ARCHIVE);
					mMessageLoader.execute((Void) null);

				}
				scope.main().setTitle(title);
			} else {
				if (bundle.containsKey("group_id")) {
					mGroupId = bundle.getString("group_id");
					mMessageLoader = new MessagesLoader(Type.GROUP);
					mMessageLoader.execute((Void) null);
				} else {
					scope.main().setTitle("Inbox");
					mMessageLoader = new MessagesLoader(Type.INBOX);
					mMessageLoader.execute((Void) null);
				}

			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		scope.context().registerReceiver(messageSyncFinish,
				new IntentFilter(SyncFinishReceiver.SYNC_FINISH));
		scope.context().registerReceiver(datasetChangeReceiver,
				new IntentFilter(DataSetChangeReceiver.DATA_CHANGED));
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mMessageLoader != null) {
			mMessageLoader.cancel(true);
		}
		scope.context().unregisterReceiver(messageSyncFinish);
		scope.context().unregisterReceiver(datasetChangeReceiver);
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mMessageLoader != null) {
			mMessageLoader.cancel(true);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mMessageLoader != null) {
			mMessageLoader.cancel(true);
		}
	}

	private HashMap<String, Boolean> datasetReg = new HashMap<String, Boolean>();

	private DataSetChangeReceiver datasetChangeReceiver = new DataSetChangeReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			try {
				rootView.findViewById(R.id.waitingForSyncToStart)
						.setVisibility(View.GONE);

				String id = intent.getExtras().getString("id");
				String parent_id = intent.getExtras().getString("parent_id");
				if (!parent_id.equals("false")) {
					id = parent_id;
				}
				OEListViewRow newRowObj = null;
				List<OEDataRow> newRow = db.search(db, from,
						new String[] { "id = ?" }, new String[] { id }, null,
						null, "date", "DESC");
				newRowObj = new OEListViewRow(Integer.parseInt(id),
						newRow.get(0));
				if (message_row_indexes.containsKey(id)
						&& mMessageObjects.size() > 0) {
					mMessageObjects.remove(Integer.parseInt(message_row_indexes
							.get(id).toString()));
					datasetReg.remove(id);
				}
				if (!datasetReg.containsKey(String.valueOf(newRowObj
						.getRow_id()))) {
					datasetReg.put(String.valueOf(newRowObj.getRow_id()), true);
					mMessageObjects.add(0, newRowObj);
					mListViewAdapter.notifiyDataChange(mMessageObjects);
				}

			} catch (Exception e) {
			}

		}
	};

	/*
	 * Used for Synchronization : Register receiver and unregister receiver
	 * 
	 * SyncFinishReceiver
	 */
	private SyncFinishReceiver messageSyncFinish = new SyncFinishReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Extract data included in the Intent
			try {
				mTouchAttacher.setPullComplete();
				String data_new = intent.getExtras().get("data_new").toString();
				String data_update = intent.getExtras()
						.getString("data_update");
				if (!data_new.equals("false")) {

				}
				if (!data_update.equals("false")) {

				}
				mListViewAdapter.clear();
				mMessageObjects.clear();
				mListViewAdapter.notifiyDataChange(mMessageObjects);

				new MessagesLoader(mType).execute();

			} catch (Exception e) {
			}
			scope.main().refreshDrawer(TAG, getActivity());
			if (mTouchAttacher == null && listAdapter != null) {
				mListViewAdapter.clear();
				mMessageObjects.clear();
				mListViewAdapter.notifiyDataChange(mMessageObjects);
				new MessagesLoader(mType).execute();
			}

		}
	};

	// Pull listview
	// Allow Activity to pass us it's OETouchListener
	void setTouchAttacher(OETouchListener attacher) {
		mTouchAttacher = attacher;
	}

	public class MessagesLoader extends AsyncTask<Void, Void, Boolean> {

		Type messageType = null;

		public MessagesLoader(Type type) {
			messageType = type;
		}

		@Override
		protected void onPreExecute() {
			scope.main().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					rootView.findViewById(R.id.loadingProgress).setVisibility(
							View.VISIBLE);
				}
			});
		}

		@Override
		protected Boolean doInBackground(Void... arg0) {
			String[] where = getWhereClause(messageType);
			mType = messageType;
			List<OEDataRow> result = db.search(db, from, where, null, null,
					null, "date", "DESC");
			HashMap<String, OEListViewRow> parent_list_details = new HashMap<String, OEListViewRow>();
			mMessageObjects.clear();
			if (result.size() > 0) {
				int i = 0;
				for (OEDataRow row : result) {
					boolean isParent = true;
					String key = row.getString("parent_id");
					if (key.equals("false")) {
						key = row.getString("id");
					} else {
						isParent = false;
					}
					if (!parent_list_details.containsKey(key)) {
						// Fetching row parent message
						OEDataRow newRow = null;
						OEListViewRow newRowObj = null;

						if (isParent) {
							newRow = row;
						} else {
							List<OEDataRow> data_row = db.search(db, from,
									new String[] { "id = ?" },
									new String[] { key });
							newRow = data_row.get(0);
						}
						newRow.put(
								"subject",
								updateSubject(newRow.get("subject").toString(),
										Integer.parseInt(key)));
						newRowObj = new OEListViewRow(Integer.parseInt(key),
								newRow);

						parent_list_details.put(key, newRowObj);
						message_row_indexes.put(key, i);
						i++;
						mMessageObjects.add(newRowObj);

					}
				}
			}
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			scope.main().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					try {
						rootView.findViewById(R.id.loadingProgress)
								.setVisibility(View.GONE);
						mListViewAdapter.notifiyDataChange(mMessageObjects);
						searchView
								.setOnQueryTextListener(getQueryListener(mListViewAdapter));
						mMessageLoader = null;
						checkMessageStatus();

					} catch (Exception e) {
					}
				}
			});

		}

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		rootView = null; // now cleaning up!
	}

	@Override
	public void onPullStarted(View arg0) {
		try {
			if (OpenERPServerConnection.isNetworkAvailable(getActivity())) {
				Log.d("MessageFragment", "requesting for sync");
				if (mGroupId != null) {
					Bundle group_bundle = new Bundle();
					JSONArray ids = new JSONArray();
					ids.put(mGroupId);
					group_bundle.putString("group_ids", ids.toString());
					scope.main().requestSync(MessageProvider.AUTHORITY,
							group_bundle);
				} else {
					scope.main().requestSync(MessageProvider.AUTHORITY);
				}
			} else {
				Toast.makeText(getActivity(), "Unable to connect server !",
						Toast.LENGTH_LONG).show();
				mTouchAttacher.setPullComplete();
			}
		} catch (Exception e) {

		}
	}

	// On ListView item Long click listener
	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view,
			int position, long id) {
		view.setSelected(true);
		if (mActionMode != null) {
			return false;
		}
		// Start the CAB using the ActionMode.Callback defined above
		mActionMode = scope.main().startActionMode(mActionModeCallback);
		selectedCounter++;
		view.setBackgroundResource(R.drawable.listitem_pressed);
		return true;
	}

	// Message ListView multiChoiceListener
	MultiChoiceModeListener mMessageViewMultiChoiceListener = new MultiChoiceModeListener() {

		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position,
				long id, boolean checked) {
			mMultiSelectedRows.put(position, checked);
			if (checked) {
				selectedCounter++;
			} else {
				selectedCounter--;
			}
			if (selectedCounter != 0) {
				mode.setTitle(selectedCounter + "");
			}
		}

		@SuppressLint("UseSparseArrays")
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			// Respond to clicks on the actions in the CAB
			switch (item.getItemId()) {
			case R.id.menu_message_mark_unread_selected:
				mReadUnreadOperation = new ReadUnreadOperation(true);
				mReadUnreadOperation.execute();
				mode.finish();
				return true;
			case R.id.menu_message_mark_read_selected:
				mReadUnreadOperation = new ReadUnreadOperation(false);
				mReadUnreadOperation.execute();
				mode.finish();
				return true;
			case R.id.menu_message_more_move_to_archive_selected:
				mReadUnreadOperation = new ReadUnreadOperation(false);
				mReadUnreadOperation.execute();
				mode.finish();
				return true;
			case R.id.menu_message_more_add_star_selected:
				mStarredOperation = new StarredOperation(true);
				mStarredOperation.execute();
				mode.finish();
				return true;
			case R.id.menu_message_more_remove_star_selected:
				mStarredOperation = new StarredOperation(false);
				mStarredOperation.execute();
				mode.finish();
				return true;
			default:
				return false;
			}
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Inflate the menu for the CAB
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.menu_fragment_message_context, menu);
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// Here you can make any necessary updates to the activity when
			// the CAB is removed. By default, selected items are
			// deselected/unchecked.

			selectedCounter = 0;
			lstMessagesView.clearChoices();

		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// Here you can perform updates to the CAB due to
			// an invalidate() request
			return false;
		}
	};

	// On item click listener
	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position,
			long id) {
		MessageDetail messageDetail = new MessageDetail();
		Bundle bundle = new Bundle();
		OEListViewRow row = (OEListViewRow) mMessageObjects.get(position);
		bundle.putInt("message_id", row.getRow_id());
		bundle.putInt("position", position);
		messageDetail.setArguments(bundle);

		scope.main().fragmentHandler.setBackStack(true, null);
		scope.main().fragmentHandler.replaceFragmnet(messageDetail);
	}

	/**
	 * Marking each row starred/unstarred in background
	 */
	public class StarredOperation extends AsyncTask<Void, Void, Boolean> {

		boolean mStarred = false;
		ProgressDialog mProgressDialog = null;
		boolean isConnection = true;

		public StarredOperation(boolean starred) {
			mStarred = starred;
			try {
				if (!OpenERPServerConnection.isNetworkAvailable(getActivity())) {
					isConnection = false;
				}
			} catch (Exception e) {
				isConnection = false;
			}
			mProgressDialog = new ProgressDialog(getActivity());
			mProgressDialog.setMessage("Working...");
			if (isConnection) {
				mProgressDialog.show();
			}
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			if (!isConnection) {
				return false;
			}
			OEArgsHelper messageIds = new OEArgsHelper();
			for (int position : mMultiSelectedRows.keySet()) {
				if (mMultiSelectedRows.get(position)) {
					OEListViewRow row = (OEListViewRow) mMessageObjects
							.get(position);
					messageIds.addArg(row.getRow_id());
				}
			}
			OEHelper openerp = getOEInstance();

			OEArgsHelper args = new OEArgsHelper();

			// Param 1 : message_ids list
			args.addArg(messageIds.getArgs());

			// Param 2 : starred - boolean value
			args.addArg(mStarred);

			// Param 3 : create_missing - If table does not contain any value
			// for
			// this row than create new one
			args.addArg(true);

			// Creating Local Database Requirement Values
			ContentValues values = new ContentValues();
			String value = (mStarred) ? "true" : "false";
			values.put("starred", value);

			boolean response = openerp.callServerMethod(getModel(),
					"set_message_starred", args.getArgs(), values,
					JSONDataHelper.jsonArrayTointArray(messageIds.getArgs()));
			return response;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				for (int position : mMultiSelectedRows.keySet()) {
					OEListViewRow row = (OEListViewRow) mMessageObjects
							.get(position);
					row.getRow_data().put("starred", mStarred);
				}
				mListViewAdapter.notifiyDataChange(mMessageObjects);
				scope.main().refreshDrawer(TAG, getActivity());
			} else {
				Toast.makeText(getActivity(), "No connection",
						Toast.LENGTH_LONG).show();
			}
			mMultiSelectedRows.clear();
			mProgressDialog.dismiss();
		}

	}

	/**
	 * Making message read or unread or Archive
	 */
	public class ReadUnreadOperation extends AsyncTask<Void, Void, Boolean> {

		ProgressDialog mProgressDialog = null;
		boolean mToRead = false;
		boolean isConnection = true;

		public ReadUnreadOperation(boolean toRead) {
			mToRead = toRead;
			try {
				if (!OpenERPServerConnection.isNetworkAvailable(getActivity())) {
					isConnection = false;
				}
			} catch (Exception e) {
				isConnection = false;
			}
			mProgressDialog = new ProgressDialog(getActivity());
			mProgressDialog.setMessage("Working...");
			if (isConnection) {
				mProgressDialog.show();
			}
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			if (!isConnection) {
				return false;
			}
			boolean flag = false;
			OEArgsHelper args = new OEArgsHelper();
			for (int position : mMultiSelectedRows.keySet()) {
				if (mMultiSelectedRows.get(position)) {
					args = new OEArgsHelper();
					OEListViewRow row = (OEListViewRow) mMessageObjects
							.get(position);

					String default_model = "false";
					int parent_id = 0, res_id = 0;
					if (row.getRow_data().getString("parent_id")
							.equals("false")) {
						parent_id = row.getRow_id();
						res_id = row.getRow_data().getInt("res_id");
						default_model = row.getRow_data().getString("model");
					} else {
						parent_id = row.getRow_data().getInt("parent_id");
					}
					List<HashMap<String, Object>> ids = db.executeSQL(
							db.getModelName(),
							new String[] { "id" },
							new String[] { "id = ?", "OR", "parent_id = ?" },
							new String[] { String.valueOf(parent_id),
									String.valueOf(parent_id) });
					for (HashMap<String, Object> id : ids) {
						if (parent_id != Integer.parseInt(id.get("id")
								.toString())) {
							args.addArg(Integer.parseInt(id.get("id")
									.toString()));
						}
					}
					args.addArg(row.getRow_id());
					if (toggleReadUnread(args, default_model, res_id,
							parent_id, mToRead)) {
						flag = true;
					}

				}
			}
			return flag;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				for (int position : mMultiSelectedRows.keySet()) {
					if (!mToRead && !mType.equals(Type.ARCHIVE)) {
						mMessageObjects.remove(position);
					}
				}
				mListViewAdapter.notifiyDataChange(mMessageObjects);
				if (mMessageObjects.size() == 0) {
					OETextView txvMsg = (OETextView) rootView
							.findViewById(R.id.txvMessageAllReadMessage);
					txvMsg.setVisibility(View.VISIBLE);
					txvMsg.setText(message_resource);
				}
				scope.main().refreshDrawer(TAG, getActivity());
			} else {
				Toast.makeText(getActivity(), "No connection",
						Toast.LENGTH_LONG).show();
			}
			mMultiSelectedRows.clear();
			mProgressDialog.dismiss();
		}

	}

	/* Method for Make Message as Read,Unread and Archive */
	private boolean toggleReadUnread(OEArgsHelper idsArg, String default_model,
			int res_id, int parent_id, boolean to_read) {
		boolean flag = false;
		OEHelper openerp = getOEInstance();
		JSONObject newContext = new JSONObject();
		try {
			if (default_model.equals("false")) {
				newContext.put("default_model", false);
			} else {
				newContext.put("default_model", default_model);
			}
			newContext.put("default_res_id", res_id);
			newContext.put("default_parent_id", parent_id);
			OEArgsHelper args = new OEArgsHelper();

			// Param 1 : message_ids list
			args.addArg(idsArg.getArgs());

			// Param 2 : to_read - boolean value
			args.addArg((to_read) ? false : true);

			// Param 3 : create_missing - If table does not contain any value
			// for
			// this row than create new one
			args.addArg(true);

			// Param 4 : context
			args.addArg(newContext);

			// Creating Local Database Requirement Values
			ContentValues values = new ContentValues();
			String value = (to_read) ? "true" : "false";
			values.put("starred", "false");
			values.put("to_read", value);
			flag = openerp.callServerMethod(getModel(), "set_message_read",
					args.getArgs(), values,
					JSONDataHelper.jsonArrayTointArray(idsArg.getArgs()));
			for (int uId : JSONDataHelper.jsonArrayTointArray(idsArg.getArgs())) {
				db.write(db, values, uId, true);
			}
		} catch (Exception e) {
		}
		return flag;
	}
}
