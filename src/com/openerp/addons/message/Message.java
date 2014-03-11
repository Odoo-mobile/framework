package com.openerp.addons.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

import openerp.OEArguments;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
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
import com.openerp.orm.OEValues;
import com.openerp.providers.message.MessageProvider;
import com.openerp.receivers.DataSetChangeReceiver;
import com.openerp.receivers.SyncFinishReceiver;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.fragment.FragmentListener;
import com.openerp.support.listview.OEListAdapter;
import com.openerp.util.HTMLHelper;
import com.openerp.util.OEDate;
import com.openerp.util.StringHelper;
import com.openerp.util.drawer.DrawerItem;
import com.openerp.util.drawer.DrawerListener;

public class Message extends BaseFragment implements
		OETouchListener.OnPullListener, OnItemLongClickListener,
		OnItemClickListener {

	public static final String TAG = "com.openerp.addons.message.Message";

	private enum MType {
		INBOX, TOME, TODO, ARCHIVE, GROUP
	}

	Integer mGroupId = null;
	Integer mSelectedItemPosition = -1;
	Integer selectedCounter = 0;
	MType mType = MType.INBOX;
	String mCurrentType = "inbox";
	View mView = null;
	SearchView mSearchView = null;
	OETouchListener mTouchAttacher;
	ActionMode mActionMode;
	@SuppressLint("UseSparseArrays")
	HashMap<Integer, Boolean> mMultiSelectedRows = new HashMap<Integer, Boolean>();
	OEListAdapter mListViewAdapter = null;
	ListView mListView = null;
	List<Object> mMessageObjects = new ArrayList<Object>();
	Integer tag_color_count = 0;
	Boolean isSynced = false;

	/**
	 * Background data operations
	 */
	MessagesLoader mMessageLoader = null;
	StarredOperation mStarredOperation = null;
	ReadUnreadOperation mReadUnreadOperation = null;

	HashMap<String, Integer> message_row_indexes = new HashMap<String, Integer>();
	HashMap<String, Integer> message_model_colors = new HashMap<String, Integer>();

	int[] background_resources = new int[] {
			R.drawable.message_listview_bg_toread_selector,
			R.drawable.message_listview_bg_tonotread_selector };

	int[] starred_drawables = new int[] { R.drawable.ic_action_starred,
			R.drawable.ic_action_unstarred };

	String tag_colors[] = new String[] { "#A4C400", "#00ABA9", "#1BA1E2",
			"#AA00FF", "#D80073", "#A20025", "#FA6800", "#6D8764", "#76608A",
			"#EBB035" };

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mSelectedItemPosition = savedInstanceState.getInt(
					"mSelectedItemPosition", -1);
		}
		setHasOptionsMenu(true);
		mView = inflater.inflate(R.layout.fragment_message, container, false);
		scope = new AppScope(getActivity());
		init();
		return mView;
	}

	private void init() {
		Log.d(TAG, "Message->init()");
		mListView = (ListView) mView.findViewById(R.id.lstMessages);
		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		mListView.setOnItemLongClickListener(this);
		mListView.setOnItemClickListener(this);
		mListView.setMultiChoiceModeListener(mMessageViewMultiChoiceListener);
		mListViewAdapter = new OEListAdapter(getActivity(),
				R.layout.fragment_message_listview_items, mMessageObjects) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View mView = convertView;
				if (mView == null)
					mView = getActivity().getLayoutInflater().inflate(
							getResource(), parent, false);
				mView = handleRowView(mView, position);
				return mView;
			}
		};
		mListView.setAdapter(mListViewAdapter);
		mTouchAttacher = scope.main().getTouchAttacher();
		mTouchAttacher.setPullableView(mListView, this);
		initData();
	}

	private void initData() {
		Log.d(TAG, "Message->initData()");
		if (mSelectedItemPosition > -1) {
			return;
		}
		Bundle bundle = getArguments();
		if (bundle != null) {
			if (mMessageLoader != null) {
				mMessageLoader.cancel(true);
				mMessageLoader = null;
			}
			if (bundle.containsKey("type")) {
				mCurrentType = bundle.getString("type");
				String title = "Archive";
				if (mCurrentType.equals("inbox")) {
					mMessageLoader = new MessagesLoader(MType.INBOX);
					mMessageLoader.execute((Void) null);
					title = "Inbox";
				} else if (mCurrentType.equals("to-me")) {
					title = "To-Me";
					mMessageLoader = new MessagesLoader(MType.TOME);
					mMessageLoader.execute((Void) null);
				} else if (mCurrentType.equals("to-do")) {
					title = "To-DO";
					mMessageLoader = new MessagesLoader(MType.TODO);
					mMessageLoader.execute((Void) null);
				} else if (mCurrentType.equals("archive")) {
					mMessageLoader = new MessagesLoader(MType.ARCHIVE);
					mMessageLoader.execute((Void) null);

				}
				scope.main().setTitle(title);
			} else {
				if (bundle.containsKey("group_id")) {
					mGroupId = bundle.getInt("group_id");
					mMessageLoader = new MessagesLoader(MType.GROUP);
					mMessageLoader.execute((Void) null);
				} else {
					scope.main().setTitle("Inbox");
					mMessageLoader = new MessagesLoader(MType.INBOX);
					mMessageLoader.execute((Void) null);
				}

			}
		}
	}

	// Handling each row view
	private View handleRowView(View mView, final int position) {
		final OEDataRow row = (OEDataRow) mMessageObjects.get(position);
		boolean to_read = row.getBoolean("to_read");
		mView.setBackgroundResource((to_read) ? background_resources[1]
				: background_resources[0]);

		TextView txvSubject, txvBody, txvFrom, txvDate, txvTag, txvchilds;
		final ImageView imgStarred = (ImageView) mView
				.findViewById(R.id.imgMessageStarred);

		final boolean starred = row.getBoolean("starred");
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
		txvchilds = (TextView) mView.findViewById(R.id.txvChilds);

		if (!to_read) {
			txvSubject.setTextColor(Color.BLACK);
			txvFrom.setTextColor(Color.BLACK);
		} else {
			txvSubject.setTextColor(Color.parseColor("#414141"));
			txvFrom.setTextColor(Color.parseColor("#414141"));
		}
		String subject = row.getString("subject");
		if (subject.equals("false")) {
			subject = row.getString("type");
		}
		if (!row.getString("record_name").equals("false"))
			subject = row.getString("record_name");
		txvSubject.setText(subject);
		if (row.getInt("childs") > 0) {
			txvchilds.setVisibility(View.VISIBLE);
			txvchilds.setText(row.getString("childs") + " reply");
		} else
			txvchilds.setVisibility(View.GONE);

		txvBody.setText(HTMLHelper.htmlToString(row.getString("body")));
		String date = row.getString("date");
		txvDate.setText(OEDate.getDate(date, TimeZone.getDefault().getID()));

		String from = row.getString("email_from");
		if (from.equals("false")) {
			OEDataRow author_id = row.getM2ORecord("author_id").browse();
			if (author_id != null)
				from = row.getM2ORecord("author_id").browse().getString("name");
		}
		txvFrom.setText(from);

		String model_name = row.getString("model");
		if (model_name.equals("false")) {
			model_name = StringHelper.capitalizeString(row.getString("type"));
		} else {
			String[] model_parts = TextUtils.split(model_name, "\\.");
			@SuppressWarnings({ "unchecked", "rawtypes" })
			HashSet unique_parts = new HashSet(Arrays.asList(model_parts));
			model_name = StringHelper.capitalizeString(TextUtils.join(" ",
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
		if (row.getString("model").equals("mail.group")) {
			String res_id = row.getString("res_id");
			if (MailGroup.mMenuGroups.containsKey("group_" + res_id)) {
				OEDataRow grp = (OEDataRow) MailGroup.mMenuGroups.get("group_"
						+ res_id);
				model_name = grp.getString("name");
				tag_color = grp.getInt("tag_color");
			}
		}
		txvTag.setBackgroundColor(tag_color);
		txvTag.setText(model_name);
		return mView;
	}

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
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.menu_fragment_message_context, menu);
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			selectedCounter = 0;
			mListView.clearChoices();
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
	};

	private int getStatusMessage(MType type) {
		switch (type) {
		case INBOX:
			return R.string.message_inbox_all_read;
		case TOME:
			return R.string.message_tome_all_read;
		case TODO:
			return R.string.message_todo_all_read;
		case GROUP:
			return R.string.message_no_group_message;
		default:
			break;
		}
		return 0;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_fragment_message, menu);
		mSearchView = (SearchView) menu.findItem(R.id.menu_message_search)
				.getActionView();
		// Hidning compose menu for group messages.
		Bundle bundle = getArguments();
		if (bundle != null && bundle.containsKey("group_id")) {
			MenuItem compose = menu.findItem(R.id.menu_message_compose);
			compose.setVisible(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_message_compose:
			getActivity().startActivity(
					new Intent(getActivity(), MessageComposeActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public Object databaseHelper(Context context) {
		return new MessageDB(context);
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		List<DrawerItem> drawerItems = new ArrayList<DrawerItem>();
		MessageDB db = new MessageDB(context);
		if (db.isInstalledOnServer()) {
			drawerItems.add(new DrawerItem(TAG, "Messages", true));
			drawerItems
					.add(new DrawerItem(TAG, "Inbox", count(MType.INBOX,
							context), R.drawable.ic_action_inbox,
							getFragment("inbox")));
			drawerItems.add(new DrawerItem(TAG, "To: me", count(MType.TOME,
					context), R.drawable.ic_action_user, getFragment("to-me")));
			drawerItems.add(new DrawerItem(TAG, "To-do", count(MType.TODO,
					context), R.drawable.ic_action_todo, getFragment("to-do")));
			drawerItems.add(new DrawerItem(TAG, "Archives", 0,
					R.drawable.ic_action_archive, getFragment("archive")));
		}
		return drawerItems;
	}

	private int count(MType type, Context context) {
		int count = 0;
		MessageDB db = new MessageDB(context);
		String where = null;
		String whereArgs[] = null;
		HashMap<String, Object> obj = getWhere(type);
		where = (String) obj.get("where");
		whereArgs = (String[]) obj.get("whereArgs");
		count = db.count(where, whereArgs);
		return count;
	}

	public HashMap<String, Object> getWhere(MType type) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		String where = null;
		String[] whereArgs = null;
		switch (type) {
		case INBOX:
			where = "to_read = ? AND starred = ?";
			whereArgs = new String[] { "true", "false" };
			break;
		case TOME:
			where = "res_id = ? AND to_read = ?";
			whereArgs = new String[] { "0", "true" };
			break;
		case TODO:
			where = "to_read = ? AND starred = ?";
			whereArgs = new String[] { "true", "true" };
			break;
		case GROUP:
			where = "res_id = ? AND model = ?";
			whereArgs = new String[] { mGroupId + "", "mail.group" };
			break;
		default:
			where = null;
			whereArgs = null;
			break;
		}
		map.put("where", where);
		map.put("whereArgs", whereArgs);
		return map;
	}

	private BaseFragment getFragment(String value) {
		Message message = new Message();
		Bundle bundle = new Bundle();
		bundle.putString("type", value);
		message.setArguments(bundle);
		return message;

	}

	public class MessagesLoader extends AsyncTask<Void, Void, Boolean> {

		MType messageType = null;

		public MessagesLoader(MType type) {
			messageType = type;
			mView.findViewById(R.id.loadingProgress)
					.setVisibility(View.VISIBLE);
		}

		@Override
		protected Boolean doInBackground(Void... arg0) {
			HashMap<String, Object> map = getWhere(messageType);
			String where = (String) map.get("where");
			String whereArgs[] = (String[]) map.get("whereArgs");
			mType = messageType;
			List<OEDataRow> result = db().select(where, whereArgs, null, null,
					"date DESC");
			HashMap<String, OEDataRow> parent_list_details = new HashMap<String, OEDataRow>();
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

						if (isParent) {
							newRow = row;
						} else {
							newRow = db().select(Integer.parseInt(key));
						}

						int childs = db().count("parent_id = ? ",
								new String[] { key });
						newRow.put("childs", childs);
						parent_list_details.put(key, null);
						message_row_indexes.put(key, i);
						i++;
						mMessageObjects.add(newRow);

					}
				}
			}
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {

			mView.findViewById(R.id.loadingProgress).setVisibility(View.GONE);
			mListViewAdapter.notifiyDataChange(mMessageObjects);
			mSearchView
					.setOnQueryTextListener(getQueryListener(mListViewAdapter));
			mMessageLoader = null;
			checkMessageStatus();

		}

	}

	private void checkMessageStatus() {

		// Fetching parent ids from Child row with order by date desc
		if (mMessageObjects.size() == 0) {
			if (db().isEmptyTable() && !isSynced) {
				isSynced = true;

				if (mView.findViewById(R.id.waitingForSyncToStart) != null) {
					mView.findViewById(R.id.waitingForSyncToStart)
							.setVisibility(View.VISIBLE);
				}
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

				mView.findViewById(R.id.waitingForSyncToStart).setVisibility(
						View.GONE);
				TextView txvMsg = (TextView) mView
						.findViewById(R.id.txvMessageAllReadMessage);
				txvMsg.setVisibility(View.VISIBLE);
				txvMsg.setText(getStatusMessage(mType));
			}

		}
	}

	/**
	 * On message item click
	 */
	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position,
			long id) {
		mSelectedItemPosition = position;
		OEDataRow row = (OEDataRow) mMessageObjects.get(position);
		MessageDetail detail = new MessageDetail();
		Bundle bundle = new Bundle();
		bundle.putInt("message_id", row.getInt("id"));
		bundle.putInt("position", position);
		detail.setArguments(bundle);
		FragmentListener listener = (FragmentListener) getActivity();
		listener.startDetailFragment(detail);
	}

	/**
	 * on message item long press
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view,
			int position, long id) {
		return false;
	}

	/**
	 * on pulled for sync message
	 */
	@Override
	public void onPullStarted(View arg0) {
		scope.main().requestSync(MessageProvider.AUTHORITY);
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
		scope.context().unregisterReceiver(messageSyncFinish);
		scope.context().unregisterReceiver(datasetChangeReceiver);
		Bundle outState = new Bundle();
		outState.putInt("mSelectedItemPosition", mSelectedItemPosition);
		onSaveInstanceState(outState);
	}

	/*
	 * Used for Synchronization : Register receiver and unregister receiver
	 * 
	 * SyncFinishReceiver
	 */
	private SyncFinishReceiver messageSyncFinish = new SyncFinishReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mTouchAttacher.setPullComplete();
			scope.main().refreshDrawer(TAG);
			mListViewAdapter.clear();
			mMessageObjects.clear();
			mListViewAdapter.notifiyDataChange(mMessageObjects);
			new MessagesLoader(mType).execute();

		}
	};
	private DataSetChangeReceiver datasetChangeReceiver = new DataSetChangeReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			try {
				mView.findViewById(R.id.waitingForSyncToStart).setVisibility(
						View.GONE);

				String id = intent.getExtras().getString("id");
				String model = intent.getExtras().getString("model");
				if (model.equals("mail.message")) {
					OEDataRow row = db().select(Integer.parseInt(id));
					if (!row.getString("parent_id").equals("false")) {
						row = db().select(row.getInt("parent_id"));
					}
					row.put("childs", 0);
					int parent_id = row.getInt("id");
					if (message_row_indexes.containsKey(parent_id + "")
							&& mMessageObjects.size() > 0) {
						mMessageObjects.remove(Integer
								.parseInt(message_row_indexes.get(
										parent_id + "").toString()));
					}
					mMessageObjects.add(0, row);
					message_row_indexes.put(parent_id + "", parent_id);
					mListViewAdapter.notifiyDataChange(mMessageObjects);
				}

			} catch (Exception e) {
			}

		}
	};

	/**
	 * Making message read or unread or Archive
	 */
	public class ReadUnreadOperation extends AsyncTask<Void, Void, Boolean> {

		ProgressDialog mProgressDialog = null;
		boolean mToRead = false;
		boolean isConnection = true;
		OEHelper mOE = null;

		public ReadUnreadOperation(boolean toRead) {
			mOE = db().getOEInstance();
			if (mOE == null)
				isConnection = false;
			mToRead = toRead;
			mProgressDialog = new ProgressDialog(getActivity());
			mProgressDialog.setMessage("Working...");
			if (isConnection) {
				mProgressDialog.show();
			}
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			if (!isConnection)
				return false;

			boolean flag = false;
			for (int position : mMultiSelectedRows.keySet()) {
				if (mMultiSelectedRows.get(position)) {

					OEDataRow row = (OEDataRow) mMessageObjects.get(position);

					String default_model = "false";
					JSONArray ids = new JSONArray();
					int parent_id = 0, res_id = 0;
					if (row.getString("parent_id").equals("false")) {
						parent_id = row.getInt("id");
						res_id = row.getInt("res_id");
						default_model = row.getString("model");
					} else {
						parent_id = row.getInt("parent_id");
					}
					ids.put(parent_id);
					for (OEDataRow child : db().select("parent_id = ? ",
							new String[] { parent_id + "" })) {
						ids.put(child.getInt("id"));
					}
					if (toggleReadUnread(mOE, ids, default_model, res_id,
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
				ArrayList<Integer> keys = new ArrayList<Integer>(
						mMultiSelectedRows.keySet());
				for (int position = keys.size() - 1; position >= 0; position--) {
					if (!mToRead && !mType.equals(MType.ARCHIVE)) {
						mMessageObjects.remove(position);
					}
				}
				mListViewAdapter.notifiyDataChange(mMessageObjects);
				if (mMessageObjects.size() == 0) {
					TextView txvMsg = (TextView) mView
							.findViewById(R.id.txvMessageAllReadMessage);
					txvMsg.setVisibility(View.VISIBLE);
					txvMsg.setText(getStatusMessage(mType));
				}
				DrawerListener drawer = (DrawerListener) getActivity();
				drawer.refreshDrawer(TAG);
				drawer.refreshDrawer(MailGroup.TAG);

			} else {
				Toast.makeText(getActivity(), "No connection",
						Toast.LENGTH_LONG).show();
			}
			mMultiSelectedRows.clear();
			mProgressDialog.dismiss();
		}

	}

	/* Method for Make Message as Read,Unread and Archive */
	private boolean toggleReadUnread(OEHelper oe, JSONArray ids,
			String default_model, int res_id, int parent_id, boolean to_read) {
		boolean flag = false;

		JSONObject newContext = new JSONObject();
		OEArguments args = new OEArguments();
		try {
			if (default_model.equals("false")) {
				newContext.put("default_model", false);
			} else {
				newContext.put("default_model", default_model);
			}
			newContext.put("default_res_id", res_id);
			newContext.put("default_parent_id", parent_id);

			// Param 1 : message_ids list
			args.add(ids);

			// Param 2 : to_read - boolean value
			args.add((to_read) ? false : true);

			// Param 3 : create_missing - If table does not contain any value
			// for
			// this row than create new one
			args.add(true);

			// Param 4 : context
			args.add(newContext);

			// Creating Local Database Requirement Values
			OEValues values = new OEValues();
			String value = (to_read) ? "true" : "false";
			values.put("starred", false);
			values.put("to_read", value);
			int result = (Integer) oe.call_kw("set_message_read", args, null);
			if (result > 0) {
				for (int i = 0; i < ids.length(); i++) {
					int id = ids.getInt(i);
					db().update(values, id);
				}
				flag = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}

	/**
	 * Marking each row starred/unstarred in background
	 */
	public class StarredOperation extends AsyncTask<Void, Void, Boolean> {

		boolean mStarred = false;
		ProgressDialog mProgressDialog = null;
		boolean isConnection = true;
		OEHelper mOE = null;

		public StarredOperation(boolean starred) {
			mStarred = starred;
			mOE = db().getOEInstance();
			if (mOE == null)
				isConnection = false;
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
			JSONArray mIds = new JSONArray();
			for (int position : mMultiSelectedRows.keySet()) {
				if (mMultiSelectedRows.get(position)) {
					OEDataRow row = (OEDataRow) mMessageObjects.get(position);
					mIds.put(row.getInt("id"));
				}
			}
			OEArguments args = new OEArguments();
			// Param 1 : message_ids list
			args.add(mIds);

			// Param 2 : starred - boolean value
			args.add(mStarred);

			// Param 3 : create_missing - If table does not contain any value
			// for
			// this row than create new one
			args.add(true);

			// Creating Local Database Requirement Values
			OEValues values = new OEValues();
			String value = (mStarred) ? "true" : "false";
			values.put("starred", value);

			boolean response = (Boolean) mOE.call_kw("set_message_starred",
					args, null);
			response = (!mStarred && !response) ? true : response;
			if (response) {
				try {
					for (int i = 0; i < mIds.length(); i++)
						db().update(values, mIds.getInt(i));
				} catch (Exception e) {
				}
			}
			return response;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				for (int position : mMultiSelectedRows.keySet()) {
					OEDataRow row = (OEDataRow) mMessageObjects.get(position);
					row.put("starred", mStarred);
				}
				mListViewAdapter.notifiyDataChange(mMessageObjects);
				DrawerListener drawer = (DrawerListener) getActivity();
				drawer.refreshDrawer(TAG);
				drawer.refreshDrawer(MailGroup.TAG);
			} else {
				Toast.makeText(getActivity(), "No connection",
						Toast.LENGTH_LONG).show();
			}
			mMultiSelectedRows.clear();
			mProgressDialog.dismiss();
		}

	}
}
