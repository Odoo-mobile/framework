package com.openerp.util.tags;

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

public class CustomAdapter extends ArrayAdapter<TagsItems> implements
		Filterable {
	private final Object mLock = new Object();
	List<TagsItems> lists = null;
	List<TagsItems> listsArray = null;
	View rootView = null;
	Context context = null;
	int resource_id = 0;
	ItemFilter filter = null;

	public CustomAdapter(Context context, int resource, List<TagsItems> objects) {
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
//		TagsItems item = lists.get(position);
//		TextView txvSubject = (TextView) rootView.findViewById(R.id.txvSubject);
//		TextView txvSubSubject = (TextView) rootView
//				.findViewById(R.id.txvSubSubject);
//		txvSubject.setText(item.getSubject());
//		txvSubSubject.setText(item.getSub_subject());

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
					listsArray = new ArrayList<TagsItems>(lists);
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
				final List<TagsItems> items = lists;
				final int count = items.size();
				final List<TagsItems> newItems = new ArrayList<TagsItems>(count);
				for (int i = 0; i < count; i++) {
					final TagsItems item = items.get(i);
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

		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			if (results.count > 0) {
				clear();
				lists = (List<TagsItems>) results.values;
				addAll(lists);
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}

		}
	}
}
