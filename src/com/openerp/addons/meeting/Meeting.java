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
		items.add(new OEMenuItems("Calendar", new Meeting(), 0));
		menu.setMenuItems(items);
		return menu;
	}

}