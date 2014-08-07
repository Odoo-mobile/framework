/*
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
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
package com.odoo.support.fragment;

import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.SearchView.OnQueryTextListener;

import com.odoo.App;
import com.odoo.orm.OModel;
import com.odoo.support.AppScope;

/**
 * The Class BaseFragment.
 */
public abstract class BaseFragment extends Fragment implements OModuleHelper {

	/** The scope. */
	public AppScope scope;
	private OModel mDb = null;
	/** The list search adapter. */
	private ArrayAdapter<Object> listSearchAdapter;

	/**
	 * Gets the query listener.
	 * 
	 * @param listAdapter
	 *            the list adapter
	 * @return the query listener
	 */
	public OnQueryTextListener getQueryListener(ArrayAdapter<Object> listAdapter) {
		listSearchAdapter = listAdapter;
		return queryListener;
	}

	/** The query listener. */
	final public OnQueryTextListener queryListener = new OnQueryTextListener() {

		private boolean isSearched = false;

		@Override
		public boolean onQueryTextChange(String newText) {

			if (TextUtils.isEmpty(newText)) {
				newText = "";
				if (isSearched && listSearchAdapter != null) {
					listSearchAdapter.getFilter().filter(null);
				}

			} else {
				isSearched = true;
				listSearchAdapter.getFilter().filter(newText);
			}

			return false;
		}

		@Override
		public boolean onQueryTextSubmit(String query) {
			return false;
		}
	};

	public void startFragment(Fragment fragment, Boolean addToBackState) {
		FragmentListener fragmentListener = (FragmentListener) getActivity();
		fragmentListener.startMainFragment(fragment, addToBackState);
	}

	public OModel db() {
		mDb = (OModel) databaseHelper(getActivity());
		return mDb;
	}

	/**
	 * Gets the resource string values
	 * 
	 * @param res_id
	 *            the res_id
	 * @return the string
	 */
	public String _s(int res_id) {
		return getActivity().getResources().getString(res_id);
	}

	/**
	 * gets the strings array from the resource value.
	 * 
	 * @param res_id
	 *            the res_id
	 * @return the string[]
	 */
	public String[] _sArray(int res_id) {
		return getActivity().getResources().getStringArray(res_id);
	}

	/**
	 * gets the drawable from resource values
	 * 
	 * @param res_id
	 *            the res_id
	 * @return the drawable
	 */
	public Drawable _d(int res_id) {
		return getActivity().getResources().getDrawable(res_id);
	}

	/**
	 * gets the integer value from resource values
	 * 
	 * @param res_id
	 *            the res_id
	 * @return the integer
	 */
	public Integer _i(int res_id) {
		return getActivity().getResources().getInteger(res_id);
	}

	/**
	 * gets the integer array from resource values.
	 * 
	 * @param res_id
	 *            the res_id
	 * @return the int[]
	 */
	public int[] _iArray(int res_id) {
		return getActivity().getResources().getIntArray(res_id);
	}

	/**
	 * gets the color from resource values
	 * 
	 * @param res_id
	 *            the res_id
	 * @return the int
	 */
	public int _c(int res_id) {
		return getActivity().getResources().getColor(res_id);
	}

	/**
	 * gets the dimension from resource values
	 * 
	 * @param res_id
	 *            the res_id
	 * @return the float
	 */
	public Float _dim(int res_id) {
		return getActivity().getResources().getDimension(res_id);
	}

	public App app() {
		return (App) getActivity().getApplicationContext();
	}

}
