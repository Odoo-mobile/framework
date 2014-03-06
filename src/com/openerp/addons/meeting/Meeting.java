package com.openerp.addons.meeting;

import java.util.List;

import android.content.Context;

import com.openerp.support.BaseFragment;
import com.openerp.util.drawer.DrawerItem;

public class Meeting extends BaseFragment {

	@Override
	public Object databaseHelper(Context context) {
		return new MeetingDB(context);
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		return null;
	}

}
