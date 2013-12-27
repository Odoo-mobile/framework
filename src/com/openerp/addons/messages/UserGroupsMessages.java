package com.openerp.addons.messages;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.openerp.PullToRefreshAttacher;
import com.openerp.R;
import com.openerp.orm.BaseDBHelper;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.util.drawer.DrawerItem;

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
		scope = new AppScope(this);
		db = (BaseDBHelper) databaseHelper(scope.context());
		rootView = inflater
				.inflate(R.layout.fragment_message, container, false);
		return rootView;
	}

	private void setupView(int group_id) {

		lstview = (ListView) rootView.findViewById(R.id.lstMessages);

		// Getting Pull To Refresh Attacher from Main Activity
		mPullToRefreshAttacher = scope.main().getPullToRefreshAttacher();

		// Set the Refreshable View to be the ListView and the refresh listener
		// to be this.
		if (mPullToRefreshAttacher != null & lstview != null) {
			mPullToRefreshAttacher.setRefreshableView(lstview, this);
		}
	}

	@Override
	public Object databaseHelper(Context context) {
		return new MessageDBHelper(context);
	}

	@Override
	public void onStart() {
		super.onStart();
		Bundle bundle = getArguments();
		if (bundle != null) {
			int group_id = bundle.getInt("group_id");
			setupView(group_id);
		}
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
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
