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

public class OEMenuAdapter extends ArrayAdapter<OEMenuItems> {

	Context context;
	int resource_id;
	OEMenuItems[] menus;

	public OEMenuAdapter(Context context, int resource, OEMenuItems[] objects) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.resource_id = resource;
		this.menus = objects;
	}

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
