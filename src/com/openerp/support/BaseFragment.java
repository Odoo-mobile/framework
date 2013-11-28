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
package com.openerp.support;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView.OnQueryTextListener;

import com.openerp.auth.OpenERPAccountManager;
import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.OEHelper;
import com.openerp.support.listview.OEListViewAdapter;
import com.openerp.util.drawer.DrawerItem;

/**
 * The Class BaseFragment.
 */
public abstract class BaseFragment extends Fragment implements FragmentHelper {

	/** The scope. */
	public AppScope scope;

	/** The db. */
	public BaseDBHelper db;

	/** The list search adapter. */
	private OEListViewAdapter listSearchAdapter;

	/**
	 * Gets the oE instance.
	 * 
	 * @return the oE instance
	 */
	public OEHelper getOEInstance() {
		OEHelper openerp = null;
		try {
			openerp = new OEHelper(scope.context(),
					OpenERPAccountManager.currentUser(scope.context()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		return openerp;
	}

	/**
	 * Gets the model.
	 * 
	 * @return the model
	 */
	public BaseDBHelper getModel() {
		return (BaseDBHelper) databaseHelper(scope.context());
	}

	/**
	 * Gets the query listener.
	 * 
	 * @param listAdapter
	 *            the list adapter
	 * @return the query listener
	 */
	public OnQueryTextListener getQueryListener(OEListViewAdapter listAdapter) {
		listSearchAdapter = listAdapter;
		return queryListener;
	}

	/* list search handler */
	/** The grid_current query. */
	private String grid_currentQuery = null; // holds the current query...

	/** The query listener. */
	final public OnQueryTextListener queryListener = new OnQueryTextListener() {

		private boolean isSearched = false;

		@Override
		public boolean onQueryTextChange(String newText) {

			if (TextUtils.isEmpty(newText)) {
				grid_currentQuery = null;
				newText = "";
				if (isSearched && listSearchAdapter != null) {
					listSearchAdapter.getFilter().filter(null);
				}

			} else {
				isSearched = true;
				grid_currentQuery = newText;
				listSearchAdapter.getFilter().filter(newText);
			}

			return false;
		}

		@Override
		public boolean onQueryTextSubmit(String query) {
			return false;
		}
	};

}

interface FragmentHelper {
	public Object databaseHelper(Context context);

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState);

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater);

	public List<DrawerItem> drawerMenus(Context context);
}
