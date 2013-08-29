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
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.Toast;

import com.openerp.MainActivity;
import com.openerp.R;
import com.openerp.orm.OEHelper;
import com.openerp.providers.message.MessageProvider;
import com.openerp.receivers.DataSetChangeReceiver;
import com.openerp.receivers.SyncFinishReceiver;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.JSONDataHelper;
import com.openerp.support.OEArgsHelper;
import com.openerp.support.OEDialog;
import com.openerp.support.listview.BooleanColumnCallback;
import com.openerp.support.listview.OEListViewAdapter;
import com.openerp.support.listview.OEListViewRows;
import com.openerp.support.menu.OEMenu;
import com.openerp.support.menu.OEMenuItems;

public class Message extends BaseFragment implements
		PullToRefreshAttacher.OnRefreshListener {

	PerformOperation markasTodoTask = null;
	PerformReadUnreadArchiveOperation readunreadoperation = null;
	ActionMode mActionMode;
	private PullToRefreshAttacher mPullToRefreshAttacher;
	ListView lstview = null;
	List<OEListViewRows> list = new ArrayList<OEListViewRows>();
	OEListViewAdapter listAdapter = null;
	List<OEListViewRows> messages_sorted = null;
	HashMap<String, Integer> message_row_indexes = new HashMap<String, Integer>();
	View rootView = null;
	String[] from = new String[] { "id", "subject", "body", "record_name",
			"type", "to_read", "starred", "author_id" };

	private enum TYPE {
		INBOX, TODO, TOME, ARCHIVE
	}

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
		scope = new AppScope(MainActivity.userContext,
				(MainActivity) getActivity());

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
		handleArguments((Bundle) getArguments());

		/*
		 * Your required code below.
		 */

		// Sync Service with 40 seconds delayed.
		// scope.context().setSyncPeriodic(MessageProvider.AUTHORITY, 1L, 40L,
		// 1L);
		scope.context().setAutoSync(MessageProvider.AUTHORITY, true);

		// setupListView(TYPE.INBOX);
		return rootView;
	}

	/*
	 * setupListView()
	 * 
	 * Setting up listview for messages to load.
	 */
	private void setupListView(TYPE type) {
		// Destroying pre-loaded instance and going to create new one
		lstview = null;

		// Fetching required messages for listview by filtering of requrement
		if (list != null && list.size() <= 0) {
			list = getMessages(type);
		} else {
			rootView.findViewById(R.id.messageSyncWaiter).setVisibility(
					View.GONE);
		}

		// Handling List View controls and keys
		String[] from = new String[] { "subject|type", "body", "starred",
				"author_id|email_from", "date" };
		int[] to = new int[] { R.id.txvMessageSubject, R.id.txvMessageBody,
				R.id.imgMessageStarred, R.id.txvMessageFrom,
				R.id.txvMessageDate };

		// Creating instance for listAdapter
		listAdapter = new OEListViewAdapter(scope.context(),
				R.layout.message_listview_items, list, from, to, db, true,
				new int[] { R.drawable.message_listview_bg_toread_selector,
						R.drawable.message_listview_bg_tonotread_selector },
				"to_read");

		// Telling adapter to clean HTML text for key value
		listAdapter.cleanHtmlToTextOn("body");
		listAdapter.cleanDate("date", scope.User().getTimezone());
		// Setting callback handler for boolean field value change.
		listAdapter.setBooleanEventOperation("starred",
				R.drawable.ic_action_starred, R.drawable.ic_action_unstarred,
				updateStarred);
		// Creating instance for listview control
		lstview = (ListView) rootView.findViewById(R.id.lstMessages);
		// Providing adapter to listview
		lstview.setAdapter(listAdapter);
		// Setting listview choice mode to multiple model
		lstview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		// Seeting item long click listern to activate action mode.
		lstview.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View view,
					int index, long arg3) {
				// TODO Auto-generated method stub

				OEListViewRows data = (OEListViewRows) lstview.getAdapter()
						.getItem(index);

				Toast.makeText(scope.context(),
						data.getRow_id() + " id clicked", Toast.LENGTH_LONG)
						.show();
				view.setSelected(true);
				if (mActionMode != null) {
					return false;
				}
				// Start the CAB using the ActionMode.Callback defined above
				mActionMode = scope.context().startActionMode(
						mActionModeCallback);
				selectedCounter++;
				view.setBackgroundResource(R.drawable.listitem_pressed);
				// lstview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
				return true;

			}
		});

		// Setting multi choice selection listener
		lstview.setMultiChoiceModeListener(new MultiChoiceModeListener() {
			HashMap<Integer, Boolean> selectedList = new HashMap<Integer, Boolean>();

			@Override
			public void onItemCheckedStateChanged(ActionMode mode,
					int position, long id, boolean checked) {
				// Here you can do something when items are
				// selected/de-selected,
				// such as update the title in the CAB
				selectedList.put(position, checked);
				if (checked) {
					selectedCounter++;
				} else {
					selectedCounter--;
				}
				if (selectedCounter != 0) {
					mode.setTitle(selectedCounter + "");
				}

			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				// Respond to clicks on the actions in the CAB
				HashMap<Integer, Integer> msg_pos = new HashMap<Integer, Integer>();
				OEDialog dialog = null;
				switch (item.getItemId()) {
				case R.id.menu_message_mark_unread_selected:
					Log.e("menu_message_context", "Mark as Unread");
					for (int pos : selectedList.keySet()) {
						msg_pos.put(list.get(pos).getRow_id(), pos);
					}
					readunreadoperation = new PerformReadUnreadArchiveOperation(
							msg_pos, false);
					readunreadoperation.execute((Void) null);
					mode.finish();
					return true;
				case R.id.menu_message_mark_read_selected:
					Log.e("menu_message_context", "Mark as Read");
					for (int pos : selectedList.keySet()) {
						msg_pos.put(list.get(pos).getRow_id(), pos);
					}
					readunreadoperation = new PerformReadUnreadArchiveOperation(
							msg_pos, true);
					readunreadoperation.execute((Void) null);
					mode.finish();
					return true;
				case R.id.menu_message_more_move_to_archive_selected:
					Log.e("menu_message_context", "Archive");
					for (int pos : selectedList.keySet()) {
						msg_pos.put(list.get(pos).getRow_id(), pos);
					}
					readunreadoperation = new PerformReadUnreadArchiveOperation(
							msg_pos, false);
					readunreadoperation.execute((Void) null);
					mode.finish();
					return true;
				case R.id.menu_message_more_add_star_selected:
					for (int pos : selectedList.keySet()) {
						msg_pos.put(list.get(pos).getRow_id(), pos);
					}

					markasTodoTask = new PerformOperation(msg_pos, true);
					markasTodoTask.execute((Void) null);

					mode.finish();

					return true;
				case R.id.menu_message_more_remove_star_selected:
					for (int pos : selectedList.keySet()) {
						msg_pos.put(list.get(pos).getRow_id(), pos);
					}

					markasTodoTask = new PerformOperation(msg_pos, false);
					markasTodoTask.execute((Void) null);
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

				/*
				 * Perform Operation on Selected Ids.
				 * 
				 * row_ids are list of selected message Ids.
				 */

				selectedList.clear();
				selectedCounter = 0;
				lstview.clearChoices();

			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				// Here you can perform updates to the CAB due to
				// an invalidate() request
				return false;
			}
		});
		lstview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index,
					long id) {
				// TODO Auto-generated method stub
				MessageDetail messageDetail = new MessageDetail();
				Bundle bundle = new Bundle();
				bundle.putInt("message_id", list.get(index).getRow_id());
				messageDetail.setArguments(bundle);
				scope.context().fragmentHandler.setBackStack(true, null);
				scope.context().fragmentHandler.replaceFragmnet(messageDetail);
			}
		});

		// Getting Pull To Refresh Attacher from Main Activity
		mPullToRefreshAttacher = scope.context().getPullToRefreshAttacher();

		// Set the Refreshable View to be the ListView and the refresh listener
		// to be this.
		mPullToRefreshAttacher.setRefreshableView(lstview, this);
	}

	private List<OEListViewRows> getMessages(TYPE type) {

		String[] where = null;
		String[] whereArgs = null;
		switch (type) {
		case INBOX:
			where = new String[] { "to_read = ?", "AND", "starred  = ?" };
			whereArgs = new String[] { "true", "false" };
			break;
		case TOME:
			where = new String[] { "res_id = ? ", "AND", "to_read= ?", "AND",
					"starred=?" };
			whereArgs = new String[] { "0", "true", "false" };
			break;
		case TODO:
			Log.e("Loading ", "TODO List");
			where = new String[] { "starred  = ? " };
			whereArgs = new String[] { "true" };
			break;
		}

		// Fetching parent ids from Child row with order by date desc
		HashMap<String, Object> result = db.search(db, where, whereArgs, null,
				null, "date", "DESC");

		HashMap<String, OEListViewRows> parent_list_details = new HashMap<String, OEListViewRows>();
		messages_sorted = new ArrayList<OEListViewRows>();
		if (Integer.parseInt(result.get("total").toString()) > 0) {
			int i = 0;
			for (HashMap<String, Object> row : (List<HashMap<String, Object>>) result
					.get("records")) {

				boolean isParent = true;
				String key = row.get("parent_id").toString();
				if (key.equals("false")) {
					key = row.get("id").toString();
				} else {
					isParent = false;
				}
				if (!parent_list_details.containsKey(key)) {
					// Fetching row parent message
					HashMap<String, Object> newRow = null;
					OEListViewRows newRowObj = null;
					if (isParent) {
						newRow = row;
						newRowObj = new OEListViewRows(Integer.parseInt(key),
								(HashMap<String, Object>) newRow);
					} else {
						newRow = db.search(db, new String[] { "id = ?" },
								new String[] { key });

						newRowObj = new OEListViewRows(Integer.parseInt(key),
								((List<HashMap<String, Object>>) newRow
										.get("records")).get(0));
					}

					// OEListViewRows data_row = new OEListViewRows(
					// Integer.valueOf((String) key),
					// newRowObj.getRow_data());
					parent_list_details.put(key, newRowObj);
					message_row_indexes.put(key, i);
					i++;
					messages_sorted.add(newRowObj);

				}
			}
			rootView.findViewById(R.id.messageSyncWaiter).setVisibility(
					View.GONE);
		} else {
			try {
				Thread.sleep(2000);
				scope.context().requestSync(MessageProvider.AUTHORITY);
			} catch (Exception e) {
				// TODO: handle exception
			}

		}
		return messages_sorted;

	}

	@Override
	public Object databaseHelper(Context context) {
		// TODO Auto-generated method stub
		return new MessageDBHelper(context);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.menu_fragment_message, menu);
		// Associate searchable configuration with the SearchView
		SearchView searchView = (SearchView) menu.findItem(
				R.id.menu_message_search).getActionView();
		searchView.setOnQueryTextListener(getQueryListener(listAdapter));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
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

	@Override
	public OEMenu menuHelper(Context context) {
		// TODO Auto-generated method stub
		OEMenu menu = new OEMenu();
		List<OEMenuItems> menuitems = new ArrayList<OEMenuItems>();
		menu.setId(1);
		menu.setMenuTitle("Message");

		menuitems.add(new OEMenuItems(R.drawable.ic_action_inbox, "Inbox", this
				.getObjectOFClass("type", "inbox"), getCount(TYPE.INBOX,
				context)));
		menuitems.add(new OEMenuItems(R.drawable.ic_action_user, "To:Me", this
				.getObjectOFClass("type", "to-me"),
				getCount(TYPE.TOME, context) - 1));
		menuitems.add(new OEMenuItems(R.drawable.ic_action_todo, "To-Do", this
				.getObjectOFClass("type", "to-do"),
				getCount(TYPE.TODO, context)));
		menuitems.add(new OEMenuItems(R.drawable.ic_action_archive, "Archive",
				this.getObjectOFClass("type", "archive"), 0));

		menu.setMenuItems(menuitems);
		return menu;
	}

	private int getCount(TYPE type, Context context) {
		db = new MessageDBHelper(context);
		int count = 0;
		String[] where = null;
		String[] whereArgs = null;
		switch (type) {
		case INBOX:
			where = new String[] { "to_read = ?", "AND", "starred = ?" };
			whereArgs = new String[] { "true", "false" };
			break;
		case TOME:
			where = new String[] { "to_read = ?", "AND", "res_id = ?", "AND",
					"starred= ?" };
			whereArgs = new String[] { "true", "0", "false" };
			break;
		case TODO:
			where = new String[] { "to_read = ?", "AND", "starred = ?" };
			whereArgs = new String[] { "true", "true" };
			break;
		default:
			break;
		}
		if (where != null) {
			count = db.count(db, where, whereArgs);

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
	public void handleArguments(Bundle bundle) {
		// TODO Auto-generated method stub

		if (bundle != null) {
			if (bundle.containsKey("type")) {
				String type = bundle.getString("type");
				String title = "Archive";
				if (type.equals("inbox")) {
					setupListView(TYPE.INBOX);
					title = "Inbox";
				} else if (type.equals("to-me")) {
					title = "To-Me";
					setupListView(TYPE.TOME);
				} else if (type.equals("to-do")) {
					setupListView(TYPE.TODO);
					title = "To-DO";

				} else if (type.equals("archive")) {
					setupListView(TYPE.ARCHIVE);

				}
				scope.context().setTitle(title);
			} else {
				scope.context().setTitle("Inbox");
				setupListView(TYPE.INBOX);

			}
		}
	}

	/*
	 * Used for Synchronization : Register receiver and unregister receiver
	 * 
	 * SyncFinishReceiver
	 */
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
		scope.context().unregisterReceiver(messageSyncFinish);
		scope.context().unregisterReceiver(datasetChangeReceiver);
	}

	private HashMap<String, Boolean> datasetReg = new HashMap<String, Boolean>();

	private DataSetChangeReceiver datasetChangeReceiver = new DataSetChangeReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			rootView.findViewById(R.id.messageSyncWaiter).setVisibility(
					View.GONE);

			String id = intent.getExtras().getString("id");
			String parent_id = intent.getExtras().getString("parent_id");

			if (!parent_id.equals("false")) {
				id = parent_id;
			}
			try {

				OEListViewRows newRowObj = null;
				HashMap<String, Object> newRow = db.search(db,
						new String[] { "id = ?" }, new String[] { id });
				newRowObj = new OEListViewRows(Integer.parseInt(id),
						((List<HashMap<String, Object>>) newRow.get("records"))
								.get(0));

				HashMap<String, Object> row = newRowObj.getRow_data();
				//
				if (message_row_indexes.containsKey(id) && list.size() > 0) {
					list.remove(Integer.parseInt(message_row_indexes.get(id)
							.toString()));
					datasetReg.remove(id);
				}
				if (!datasetReg.containsKey(String.valueOf(newRowObj
						.getRow_id()))) {
					datasetReg.put(String.valueOf(newRowObj.getRow_id()), true);
					list.add(0, newRowObj);
					listAdapter.refresh(list);
				}

			} catch (Exception e) {
			}

		}
	};

	private OEListViewRows getRowForMessage(int id) {
		HashMap<String, Object> newRow = db.search(db,
				new String[] { "id = ?" }, new String[] { String.valueOf(id) });
		OEListViewRows newRowObj = new OEListViewRows(id,
				((List<HashMap<String, Object>>) newRow.get("records")).get(0));

		return newRowObj;
	}

	private SyncFinishReceiver messageSyncFinish = new SyncFinishReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Extract data included in the Intent
			try {
				mPullToRefreshAttacher.setRefreshComplete();
				String data = intent.getExtras().get("data").toString();
				if (!data.equals("false")) {

				}
				scope.context().refreshMenu(getActivity());

				// int[] ids = intent.getExtras().getIntArray("new_ids");
				// String total = intent.getExtras().getString("total");
				// if (Integer.parseInt(total) > 0) {
				// rootView.findViewById(R.id.messageNewCounter)
				// .setVisibility(View.VISIBLE);
				// TextView txvHeaderTitle = (TextView) rootView
				// .findViewById(R.id.txvMessageHeaderTitle);
				// TextView txvCounter = (TextView) rootView
				// .findViewById(R.id.txvMessageHeaderCounter);
				// rootView.findViewById(R.id.txvMessageHeaderSubtitle)
				// .setVisibility(View.GONE);
				// txvHeaderTitle.setText(title);
				// txvCounter.setText(total + " New");
				// }

			} catch (Exception e) {
				e.printStackTrace();
			}
			Log.d("Message::syncFinishReceiver::onReceive()",
					"Resetting listview messages to INBOX");
			setupListView(TYPE.INBOX);

		}
	};

	@Override
	public void onRefreshStarted(View view) {
		// TODO Auto-generated method stub
		Log.d("MessageFragment", "requesting for sync");
		scope.context().requestSync(MessageProvider.AUTHORITY);

	}

	// PullToRefresh
	// Allow Activity to pass us it's PullToRefreshAttacher
	void setPullToRefreshAttacher(PullToRefreshAttacher attacher) {
		mPullToRefreshAttacher = attacher;
	}

	// Callback when user press on starred button from listview row
	BooleanColumnCallback updateStarred = new BooleanColumnCallback() {

		@Override
		public OEListViewRows updateFlagValues(OEListViewRows row, View view) {
			// TODO Auto-generated method stub
			HashMap<String, Object> rowData = (HashMap<String, Object>) row
					.getRow_data();
			boolean flag = false;
			ImageView img = (ImageView) view;
			if (rowData.get("starred").toString().equals("false")) {
				flag = true;
				img.setImageResource(R.drawable.ic_action_starred);
			} else {
				img.setImageResource(R.drawable.ic_action_unstarred);
			}
			OEArgsHelper messageIds = new OEArgsHelper();
			messageIds.addArg(row.getRow_id());
			if (markAsTodo(messageIds, flag)) {
				rowData.put("starred", flag);
			} else {
				Log.e("Unable to mark as todo", "Operation Fail");
			}

			return row;
		}
	};

	/* Method for Make Message as Read,Unread and Archive */
	private boolean markAsReadUnreadArchive(OEArgsHelper messageIds,
			String default_model, int res_id, int parent_id, boolean markFlag) {
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
			args.addArg(messageIds.getArgs());

			// Param 2 : starred - boolean value
			args.addArg(markFlag);

			// Param 3 : create_missing - If table does not contain any value
			// for
			// this row than create new one
			args.addArg(true);

			// Param 4 : context
			args.addArg(newContext);

			// Creating Local Database Requirement Values
			ContentValues values = new ContentValues();
			String value = (markFlag) ? "false" : "true";
			values.put("starred", "false");
			values.put("to_read", value);
			flag = openerp.callServerMethod(getModel(), "set_message_read",
					args.getArgs(), values,
					JSONDataHelper.jsonArrayTointArray(messageIds.getArgs()));
			for (int uId : JSONDataHelper.jsonArrayTointArray(messageIds
					.getArgs())) {
				db.write(db, values, uId, true);
			}
		} catch (Exception e) {
		}
		return flag;
	}

	/* Method for mark multiple message as Read, Unread, Archive */
	private boolean markAsReadUnreadArchive(HashMap<Integer, Integer> msg_pos,
			final boolean flag) {
		boolean res = false;
		OEArgsHelper args = new OEArgsHelper();
		int parent_id = 0;
		int res_id = 0;
		String default_model = "false";
		for (int key : msg_pos.keySet()) {
			final int pos = msg_pos.get(key);
			OEListViewRows rowInfo = list.get(pos);
			if (rowInfo.getRow_data().get("parent_id").equals("false")) {
				parent_id = rowInfo.getRow_id();
				res_id = Integer.parseInt(rowInfo.getRow_data().get("res_id")
						.toString());
				default_model = rowInfo.getRow_data().get("model").toString();
			} else {
				parent_id = Integer.parseInt(rowInfo.getRow_data()
						.get("parent_id").toString());
			}
			List<HashMap<String, Object>> ids = db.executeSQL(
					db.getModelName(),
					new String[] { "id" },
					new String[] { "id = ?", "OR", "parent_id = ?" },
					new String[] { String.valueOf(parent_id),
							String.valueOf(parent_id) });
			for (HashMap<String, Object> id : ids) {
				Log.e(">>>>>>>>>>>>>>>> ", id.get("id").toString());
				if (parent_id != Integer.parseInt(id.get("id").toString())) {

					args.addArg(Integer.parseInt(id.get("id").toString()));
				}
			}
			args.addArg(key);
			Log.e(">>>>>>>>>>>>>>>> args.", args.getArgs().toString());

		}

		if (markAsReadUnreadArchive(args, default_model, res_id, parent_id,
				flag)) {
			for (int key : msg_pos.keySet()) {
				final int pos = msg_pos.get(key);
				scope.context().runOnUiThread(new Runnable() {
					public void run() {
						list.remove(pos);
						listAdapter.refresh(list);
					}
				});
			}
		}
		return res;
	}

	/* Method for Make Message as TODO */
	private boolean markAsTodo(OEArgsHelper messageIds, boolean markFlag) {
		boolean flag = false;
		OEHelper openerp = getOEInstance();

		OEArgsHelper args = new OEArgsHelper();

		// Param 1 : message_ids list
		args.addArg(messageIds.getArgs());

		// Param 2 : starred - boolean value
		args.addArg(markFlag);

		// Param 3 : create_missing - If table does not contain any value for
		// this row than create new one
		args.addArg(true);

		// Creating Local Database Requirement Values
		ContentValues values = new ContentValues();
		String value = (markFlag) ? "true" : "false";
		values.put("starred", value);

		flag = openerp.callServerMethod(getModel(), "set_message_starred",
				args.getArgs(), values,
				JSONDataHelper.jsonArrayTointArray(messageIds.getArgs()));
		Log.d("Marking as TODO", messageIds.getArgs().toString());
		return flag;
	}

	/* Method for mark multiple message as TODO */
	private boolean markAsTodo(HashMap<Integer, Integer> msg_pos,
			final boolean flag) {
		boolean res = false;
		final int img[] = new int[] { R.drawable.ic_action_unstarred,
				R.drawable.ic_action_starred };
		for (int key : msg_pos.keySet()) {

			final int pos = msg_pos.get(key);

			OEArgsHelper args = new OEArgsHelper();
			args.addArg(key);
			if (markAsTodo(args, flag)) {
				listAdapter.updateRows(getRowForMessage(key), pos, "starred");
				scope.context().runOnUiThread(new Runnable() {
					public void run() {

						ImageView imgStarred = (ImageView) lstview.getChildAt(
								pos).findViewById(R.id.imgMessageStarred);
						imgStarred.setImageResource((flag) ? img[1] : img[0]);
						imgStarred.invalidate();

					}
				});
			}
		}

		return res;
	}

	public class PerformOperation extends AsyncTask<Void, Void, Boolean> {

		OEDialog pdialog = null;
		String errorMsg = "";
		HashMap<Integer, Integer> msg_pos;
		boolean setFlag = false;

		public PerformOperation(HashMap<Integer, Integer> msg_pos, boolean bool) {
			// TODO Auto-generated constructor stub
			this.msg_pos = msg_pos;
			this.setFlag = bool;
			pdialog = new OEDialog(scope.context(), true,
					"Performing Operation...");
		}

		@Override
		protected void onPreExecute() {

			pdialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return markAsTodo(msg_pos, setFlag);
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			pdialog.hide();
		}

	}

	public class PerformReadUnreadArchiveOperation extends
			AsyncTask<Void, Void, Boolean> {

		OEDialog pdialog = null;
		String errorMsg = "";
		HashMap<Integer, Integer> msg_pos;
		boolean setFlag = false;

		public PerformReadUnreadArchiveOperation(
				HashMap<Integer, Integer> msg_pos, boolean bool) {
			// TODO Auto-generated constructor stub
			this.msg_pos = msg_pos;
			this.setFlag = bool;
			pdialog = new OEDialog(scope.context(), true,
					"Performing Operation...");
		}

		@Override
		protected void onPreExecute() {

			pdialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return markAsReadUnreadArchive(msg_pos, setFlag);
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			pdialog.hide();
		}

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		rootView = null; // now cleaning up!
	}

}
