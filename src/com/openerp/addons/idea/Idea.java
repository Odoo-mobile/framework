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

package com.openerp.addons.idea;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.openerp.R;
import com.openerp.orm.OEDataRow;
import com.openerp.support.BaseFragment;
import com.openerp.support.fragment.FragmentListener;
import com.openerp.util.drawer.DrawerItem;

/**
 * The Class Idea.
 */
public class Idea extends BaseFragment implements OnItemClickListener {

	List<String> mItems = new ArrayList<String>();
	ListView mListView = null;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		View rootView = inflater.inflate(R.layout.fragment_idea, container,
				false);

		if (db().isEmptyTable()) {
			IdeaDemoRecords rec = new IdeaDemoRecords(getActivity());
			rec.createDemoRecords();
		}

		mItems.clear();
		for (OEDataRow row : db().select()) {
			mItems.add(row.getString("name"));
		}

		mListView = (ListView) rootView.findViewById(R.id.listview);
		mListView.setAdapter(new ArrayAdapter<String>(getActivity(),
				R.layout.fragment_idea_list_item, mItems) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View mView = convertView;
				if (mView == null)
					mView = getActivity().getLayoutInflater().inflate(
							R.layout.fragment_idea_list_item, parent, false);
				TextView txv = (TextView) mView.findViewById(R.id.txvIdeaName);
				txv.setText(mItems.get(position));
				return mView;
			}

		});
		mListView.setOnItemClickListener(this);
		return rootView;
	}

	@Override
	public Object databaseHelper(Context context) {
		return new IdeaDBHelper(context);
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		List<DrawerItem> menu = new ArrayList<DrawerItem>();
		menu.add(new DrawerItem("idea_home", "Idea", true));
		Idea idea = new Idea();
		Bundle args = new Bundle();
		args.putString("key", "idea");
		idea.setArguments(args);
		menu.add(new DrawerItem("idea_home", "Idea", 5, 0, idea));
		return menu;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_fragment_account, menu);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		view.setSelected(true);
		IdeaDetail detail = new IdeaDetail();
		Bundle args = new Bundle();
		args.putString("name", mItems.get(position));
		detail.setArguments(args);
		FragmentListener frag = (FragmentListener) getActivity();
		frag.startDetailFragment(detail);
	}
}
