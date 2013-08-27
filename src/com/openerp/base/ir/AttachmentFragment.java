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
package com.openerp.base.ir;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.openerp.support.BaseFragment;
import com.openerp.support.menu.OEMenu;

public class AttachmentFragment extends BaseFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		return null;
	}

	@Override
	public Object databaseHelper(Context context) {
		// TODO Auto-generated method stub
		return new Ir_AttachmentDBHelper(context);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		// handle item selection

		switch (item.getItemId()) {

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public OEMenu menuHelper(Context context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleArguments(Bundle bundle) {
		// TODO Auto-generated method stub

	}

}
