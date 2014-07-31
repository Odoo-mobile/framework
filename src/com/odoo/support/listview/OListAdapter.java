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
package com.odoo.support.listview;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;

/**
 * The Class OListAdapter.
 */
public class OListAdapter extends ArrayAdapter<Object> {

	/** The Constant TAG. */
	public static final String TAG = "com.odoo.support.listview.OListAdapter";

	/** The context. */
	private Context mContext = null;

	/** The objects. */
	private List<Object> mObjects = null;

	/** The all objects. */
	private List<Object> mAllObjects = null;

	/** The filter. */
	private RowFilter mFilter = null;

	/** The resource id. */
	private int mResourceId = 0;

	/** The row filter text listener. */
	private RowFilterTextListener mRowFilterTextListener = null;

	/** The on search change. */
	private OnSearchChange mOnSearchChange = null;

	/**
	 * Instantiates a new o list adapter.
	 * 
	 * @param context
	 *            the context
	 * @param resource
	 *            the resource
	 * @param objects
	 *            the objects
	 */
	public OListAdapter(Context context, int resource, List<Object> objects) {
		super(context, resource, objects);
		Log.d(TAG, "OListAdapter->constructor()");
		mContext = context;
		mObjects = new ArrayList<Object>(objects);
		mAllObjects = new ArrayList<Object>(objects);
		mResourceId = resource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getFilter()
	 */
	@Override
	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new RowFilter();
		}
		return mFilter;
	}

	/**
	 * Gets the resource.
	 * 
	 * @return the resource
	 */
	public int getResource() {
		return mResourceId;
	}

	/**
	 * Replace object at position.
	 * 
	 * @param position
	 *            the position
	 * @param object
	 *            the object
	 */
	public void replaceObjectAtPosition(int position, Object object) {
		mAllObjects.remove(position);
		mAllObjects.add(position, object);
		mObjects.remove(position);
		mObjects.add(position, object);
	}

	/**
	 * Notifiy data change.
	 * 
	 * @param objects
	 *            the objects
	 */
	public void notifiyDataChange(List<Object> objects) {
		Log.d(TAG, "OListAdapter->notifiyDataChange()");
		mAllObjects.clear();
		mObjects.clear();
		mAllObjects.addAll(objects);
		mObjects.addAll(objects);
		notifyDataSetChanged();
	}

	/**
	 * The Class RowFilter.
	 */
	class RowFilter extends Filter {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Filter#performFiltering(java.lang.CharSequence)
		 */
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults result = new FilterResults();
			if (!TextUtils.isEmpty(constraint)) {
				String searchingStr = constraint.toString().toLowerCase();
				List<Object> filteredItems = new ArrayList<Object>();
				for (Object item : mAllObjects) {
					String filterText = "";
					if (mRowFilterTextListener != null) {
						filterText = mRowFilterTextListener.filterCompareWith(
								item).toLowerCase();
					} else {
						filterText = item.toString().toLowerCase();
					}
					if (filterText.contains(searchingStr)) {
						filteredItems.add(item);
					}
				}
				result.count = filteredItems.size();
				result.values = filteredItems;

			} else {
				synchronized (this) {
					result.count = mAllObjects.size();
					result.values = mAllObjects;
				}
			}
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Filter#publishResults(java.lang.CharSequence,
		 * android.widget.Filter.FilterResults)
		 */
		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			clear();
			mObjects = (List<Object>) results.values;
			addAll(mObjects);
			notifyDataSetChanged();
			if (mOnSearchChange != null) {
				mOnSearchChange.onSearchChange(mObjects);
			}
		}
	}

	/**
	 * Sets the on search change.
	 * 
	 * @param callback
	 *            the new on search change
	 */
	public void setOnSearchChange(OnSearchChange callback) {
		mOnSearchChange = callback;
	}

	/**
	 * Sets the row filter text listener.
	 * 
	 * @param listener
	 *            the new row filter text listener
	 */
	public void setRowFilterTextListener(RowFilterTextListener listener) {
		mRowFilterTextListener = listener;
	}

	/**
	 * The listener interface for receiving rowFilterText events. The class that
	 * is interested in processing a rowFilterText event implements this
	 * interface, and the object created with that class is registered with a
	 * component using the component's
	 * <code>addRowFilterTextListener<code> method. When
	 * the rowFilterText event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see RowFilterTextEvent
	 */
	public interface RowFilterTextListener {

		/**
		 * Filter compare with.
		 * 
		 * @param object
		 *            the object
		 * @return the string
		 */
		public String filterCompareWith(Object object);
	}

	/**
	 * The Interface OnSearchChange.
	 */
	public interface OnSearchChange {

		/**
		 * On search change.
		 * 
		 * @param newRecords
		 *            the new records
		 */
		public void onSearchChange(List<Object> newRecords);
	}

}
