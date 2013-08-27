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
package com.openerp.support.menu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.openerp.MainActivity;
import com.openerp.R;

// TODO: Auto-generated Javadoc
/**
 * The Class OEMenuAdapter.
 */
public class OEMenuAdapter extends ArrayAdapter<OEMenuItems> {

	/** The context. */
	Context context;

	/** The resource_id. */
	int resource_id;

	/** The menus. */
	OEMenuItems[] menus;

	/**
	 * Instantiates a new oE menu adapter.
	 * 
	 * @param context
	 *            the context
	 * @param resource
	 *            the resource
	 * @param objects
	 *            the objects
	 */
	public OEMenuAdapter(Context context, int resource, OEMenuItems[] objects) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.resource_id = resource;
		this.menus = objects;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View viewRow = convertView;
		LayoutInflater inflater = ((MainActivity) context).getLayoutInflater();
		if (viewRow == null) {
			viewRow = inflater
					.inflate(R.layout.drawer_menu_item, parent, false);
		}
		OEMenuItems menu = this.menus[position];
		if (this.menus[position].isGroup()) {
			/* creating group item */
			viewRow = inflater.inflate(R.layout.drawer_menu_group_item, parent,
					false);
			TextView txvGroupTitle = (TextView) viewRow
					.findViewById(R.id.txvMenuGroupTitle);
			txvGroupTitle.setText(menu.getTitle());
		} else {
			/* menu item */
			viewRow = inflater
					.inflate(R.layout.drawer_menu_item, parent, false);
			// menuItemHolder.txvMenuTitle.setText(menu.getTitle());
			ImageView imgIcon = (ImageView) viewRow.findViewById(R.id.imgIcon);
			if (menu.getIcon() != 0) {
				imgIcon.setImageResource(menu.getIcon());
			}
			TextView txvTitle = (TextView) viewRow
					.findViewById(R.id.txvMenuTitle);
			txvTitle.setText(menu.getTitle());
			TextView txvCounter = (TextView) viewRow
					.findViewById(R.id.txvNotificationCounter);
			if (menu.getNotificationCount() != 0) {
				txvCounter.setVisibility(View.VISIBLE);
				txvCounter.setText(String.valueOf(menu.getNotificationCount()));
			} else {
				txvCounter.setVisibility(View.GONE);
			}

		}

		return viewRow;

	}

}
