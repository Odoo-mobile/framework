package com.openerp.util.drawer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.openerp.R;

public class DrawerAdatper extends ArrayAdapter<DrawerItem> {

	List<DrawerItem> mObjects = null;
	Context mContext = null;
	int mResource = 0;
	int mGroupResource = 0;

	public DrawerAdatper(Context context, int item_resource,
			int item_group_resource, List<DrawerItem> objects) {
		super(context, item_resource, objects);
		mObjects = new ArrayList<DrawerItem>(objects);
		mContext = context;
		mResource = item_resource;
		mGroupResource = item_group_resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View mView = convertView;
		DrawerItem item = mObjects.get(position);
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (item.isGroupTitle()) {
			mView = inflater.inflate(mGroupResource, parent, false);
		} else {
			mView = inflater.inflate(mResource, parent, false);
		}

		// Setting title
		TextView txvTitle = (TextView) mView
				.findViewById(R.id.txvDrawerItemTitle);
		txvTitle.setText(item.getTitle());

		if (!item.isGroupTitle()) {
			// setting counter
			TextView txvCounter = (TextView) mView
					.findViewById(R.id.txvDrawerItemCounter);
			if (item.getCounter() > 0) {
				String counter_string = (item.getCounter() > 99) ? "99+" : item
						.getCounter() + "";
				txvCounter.setText(counter_string);
			} else {
				txvCounter.setVisibility(View.GONE);
			}

			DrawerIconView imgItemIcon = (DrawerIconView) mView
					.findViewById(R.id.imgDrawerItemIcon);
			if (item.getIcon() != 0) {
				imgItemIcon.setVisibility(View.VISIBLE);
				imgItemIcon.setImageDrawable(mContext.getResources()
						.getDrawable(item.getIcon()));
			}

			View tagColorview = (View) mView.findViewById(R.id.drawerTagColor);
			if (item.getTagColor() != null) {
				tagColorview.setBackgroundColor(Color.parseColor(item
						.getTagColor()));
				tagColorview.setVisibility(View.VISIBLE);
			} else {
				tagColorview.setVisibility(View.GONE);
			}

		}
		return mView;
	}

	public void updateDrawerItem(int position, DrawerItem item) {
		mObjects.set(position, item);
		notifyDataSetChanged();
	}

}
