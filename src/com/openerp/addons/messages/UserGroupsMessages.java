package com.openerp.addons.messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;

import com.openerp.MainActivity;
import com.openerp.PullToRefreshAttacher;
import com.openerp.R;
import com.openerp.orm.BaseDBHelper;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.listview.OEListViewRows;
import com.openerp.support.menu.OEMenu;

public class UserGroupsMessages extends BaseFragment implements
		PullToRefreshAttacher.OnRefreshListener {
	View rootView = null;
	ListView lstview = null;
	String[] from = new String[] { "id", "subject", "body", "record_name",
			"type", "to_read", "starred", "author_id" };
	HashMap<String, Object> message_row_indexes = new HashMap<String, Object>();
	private PullToRefreshAttacher mPullToRefreshAttacher;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		scope = new AppScope(MainActivity.userContext,
				(MainActivity) getActivity());
		db = (BaseDBHelper) databaseHelper(scope.context());
		rootView = inflater
				.inflate(R.layout.fragment_message, container, false);
		handleArguments(getArguments());
		return rootView;
	}

	private void setupView(int group_id) {

		lstview = (ListView) rootView.findViewById(R.id.lstMessages);

		List<OEListViewRows> messages = new ArrayList<OEListViewRows>();
		messages = getMessages(group_id);

		
		// Getting Pull To Refresh Attacher from Main Activity
		mPullToRefreshAttacher = scope.context().getPullToRefreshAttacher();

		// Set the Refreshable View to be the ListView and the refresh listener
		// to be this.
		if (mPullToRefreshAttacher != null & lstview != null) {
			mPullToRefreshAttacher.setRefreshableView(lstview, this);
		}
	}

	private List<OEListViewRows> getMessages(int group_id) {
		String[] where = new String[] { "model = ?", "AND", "res_id = ?" };
		String[] whereArgs = new String[] { "mail.group", group_id + "" };
		HashMap<String, Object> result = db.search(db, from, where, whereArgs,
				null, null, "date", "DESC");
		HashMap<String, OEListViewRows> parent_list_details = new HashMap<String, OEListViewRows>();
		ArrayList<OEListViewRows> messages_sorted = new ArrayList<OEListViewRows>();
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
						newRow.put(
								"subject",
								updateSubject(newRow.get("subject").toString(),
										Integer.parseInt(key)));
						newRowObj = new OEListViewRows(Integer.parseInt(key),
								(HashMap<String, Object>) newRow);

					} else {
						newRow = db.search(db, from, new String[] { "id = ?" },
								new String[] { key });
						HashMap<String, Object> temp_row = ((List<HashMap<String, Object>>) newRow
								.get("records")).get(0);
						temp_row.put(
								"subject",
								updateSubject(temp_row.get("subject")
										.toString(), Integer.parseInt(key)));
						newRowObj = new OEListViewRows(Integer.parseInt(key),
								temp_row);
					}

					parent_list_details.put(key, newRowObj);
					message_row_indexes.put(key, i);
					i++;
					messages_sorted.add(newRowObj);

				}
			}
		}

		return messages_sorted;
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

	@Override
	public Object databaseHelper(Context context) {
		return new MessageDBHelper(context);
	}

	@Override
	public void handleArguments(Bundle bundle) {
		if (bundle != null) {
			int group_id = bundle.getInt("group_id");
			setupView(group_id);
		}
	}

	@Override
	public OEMenu menuHelper(Context context) {
		return null;
	}

	@Override
	public void onRefreshStarted(View arg0) {

	}

	// PullToRefresh
	// Allow Activity to pass us it's PullToRefreshAttacher
	void setPullToRefreshAttacher(PullToRefreshAttacher attacher) {
		mPullToRefreshAttacher = attacher;
	}

}
