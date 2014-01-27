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

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.openerp.OETouchListener;
import com.openerp.R;
import com.openerp.orm.BaseDBHelper;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.util.drawer.DrawerItem;

public class UserGroupsMessages extends BaseFragment implements
		OETouchListener.OnPullListener {
	View rootView = null;
	ListView lstview = null;
	String[] from = new String[] { "id", "subject", "body", "record_name",
			"type", "to_read", "starred", "author_id" };
	HashMap<String, Object> message_row_indexes = new HashMap<String, Object>();
	private OETouchListener mTouchAttacher;

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
		mTouchAttacher = scope.main().getTouchAttacher();

		// Set the Refreshable View to be the ListView and the refresh listener
		// to be this.
		if (mTouchAttacher != null & lstview != null) {
			mTouchAttacher.setPullableView(lstview, this);
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

	// pull to sync
	// Allow Activity to pass us it's OETouchListener
	void setPullToRefreshAttacher(OETouchListener attacher) {
		mTouchAttacher = attacher;
	}

	@Override
	public void onPullStarted(View arg0) {

	}

}
