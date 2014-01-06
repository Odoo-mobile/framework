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
package com.openerp.addons.messages;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.openerp.R;
import com.openerp.util.Base64Helper;
import com.openerp.util.tags.TagsItem;

public class ReceipientsTagsCustomAdapter extends ArrayAdapter<TagsItem>
		implements Filterable {
	private final Object mLock = new Object();
	List<TagsItem> lists = null;
	List<TagsItem> listsArray = null;
	View rootView = null;
	Context context = null;
	int resource_id = 0;
	ItemFilter filter = null;

	public ReceipientsTagsCustomAdapter(Context context, int resource,
			List<TagsItem> objects) {
		super(context, resource, objects);
		this.lists = objects;
		this.context = context;
		this.resource_id = resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// return super.getView(position, convertView, parent);
		rootView = convertView;
		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		if (rootView == null) {
			rootView = inflater.inflate(this.resource_id, parent, false);
		}
		TagsItem item = lists.get(position);
		TextView txvSubject = (TextView) rootView.findViewById(R.id.txvSubject);
		TextView txvSubSubject = (TextView) rootView
				.findViewById(R.id.txvSubSubject);
		ImageView imgPic = (ImageView) rootView
				.findViewById(R.id.imgReceipientPic);
		txvSubject.setText(item.getSubject());
		if (!item.getSub_subject().equals("false")) {
			txvSubSubject.setText(item.getSub_subject());
		} else {
			txvSubSubject.setText("No email");
		}
		if (item.getImage() != null && !item.getImage().equals("false")) {
			imgPic.setImageBitmap(Base64Helper.getBitmapImage(context,
					item.getImage()));
		}

		return rootView;
	}

	@Override
	public Filter getFilter() {

		if (filter == null) {
			filter = new ItemFilter();
		}
		return filter;

	}

	/**
	 * The Class ItemFilter.
	 */
	class ItemFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			// Initiate our results object
			FilterResults result = new FilterResults();
			// If the adapter array is empty, check the actual items array and
			// use it
			if (listsArray == null) {
				synchronized (mLock) { // Notice the declaration above
					listsArray = new ArrayList<TagsItem>(lists);
				}
			}
			// No prefix is sent to filter by so we're going to send back the
			// original array
			if (prefix == null || prefix.length() == 0) {
				synchronized (mLock) {
					result.values = listsArray;
					result.count = listsArray.size();
				}
			} else {
				// Compare lower case strings
				String prefixString = prefix.toString().toLowerCase();
				// Local to here so we're not changing actual array
				final List<TagsItem> items = lists;
				final int count = items.size();
				final List<TagsItem> newItems = new ArrayList<TagsItem>(count);
				for (int i = 0; i < count; i++) {
					final TagsItem item = items.get(i);
					final String itemName = item.getSubject().toLowerCase();
					// First match against the whole, non-splitted value
					if (itemName.toLowerCase().contains(prefixString)) {
						newItems.add(item);
					}
				}
				// Set and return
				result.values = newItems;
				result.count = newItems.size();
			}

			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			if (results.count > 0) {
				clear();
				lists = (List<TagsItem>) results.values;
				addAll(lists);
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}

		}
	}
}
