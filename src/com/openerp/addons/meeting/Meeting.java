package com.openerp.addons.meeting;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;

import com.openerp.support.BaseFragment;
import com.openerp.support.menu.OEMenu;
import com.openerp.support.menu.OEMenuItems;

public class Meeting extends BaseFragment {

	@Override
	public Object databaseHelper(Context context) {
		// TODO Auto-generated method stub
		return new MeetingDBHelper(context);
	}

	@Override
	public void handleArguments(Bundle bundle) {
		// TODO Auto-generated method stub

	}

	@Override
	public OEMenu menuHelper(Context context) {
		// TODO Auto-generated method stub
		OEMenu menu = new OEMenu();
		menu.setId(1);
		menu.setMenuTitle("Meetings");
		List<OEMenuItems> items = new ArrayList<OEMenuItems>();
		items.add(new OEMenuItems("Note", new Meeting(), 0));
		menu.setMenuItems(items);
		return menu;
	}

}