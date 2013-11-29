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
package com.openerp.support.listview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.openerp.MainActivity;
import com.openerp.orm.BaseDBHelper;
import com.openerp.support.OpenERPServerConnection;
import com.openerp.util.Base64Helper;
import com.openerp.util.HTMLHelper;
import com.openerp.util.OEDate;

// TODO: Auto-generated Javadoc
/**
 * The Class OEListViewAdapter.
 */
public class OEListViewAdapter extends ArrayAdapter<OEListViewRows> {

	/** The context. */
	Context context;

	/** The resource_id. */
	int resource_id;

	OEListViewOnCreateListener viewListener = null;

	/** The rows. */
	public List<OEListViewRows> rows = null;

	/** The unfiltered_rows. */
	public List<OEListViewRows> unfiltered_rows = null;

	/** The to. */
	int[] to = null;

	/** The from. */
	String[] from = null;

	/** The colors. */
	int[] colors = null;

	/** The can change background. */
	boolean canChangeBackground = false;

	/** The condition key. */
	String conditionKey = null;

	/** The clean column. */
	List<String> cleanColumn = new ArrayList<String>();

	/** The boolean events. */
	List<String> booleanEvents = new ArrayList<String>();

	/** The image cols. */
	List<String> imageCols = new ArrayList<String>();

	/** The background change. */
	HashMap<String, HashMap<String, Integer>> backgroundChange = new HashMap<String, HashMap<String, Integer>>();

	/** The control click handler. */
	HashMap<Integer, ControlClickEventListener> controlClickHandler = new HashMap<Integer, ControlClickEventListener>();

	/** The db helper. */
	BaseDBHelper dbHelper = null;

	/** The rowdata. */
	HashMap<String, Object> rowdata = null;

	/** The binary_flag. */
	int[] binary_flag = new int[2];

	/** The callbacks. */
	HashMap<String, BooleanColumnCallback> callbacks = new HashMap<String, BooleanColumnCallback>();

	/** The parent view. */
	ViewGroup parentView = null;

	/** The view row. */
	View viewRow = null;

	/** The filter. */
	ItemFilter filter = null;

	/** The to html. */
	List<String> toHtml = new ArrayList<String>();

	/** The is flagged. */
	public HashMap<String, String> isFlagged = new HashMap<String, String>();

	/** The row. */
	OEListViewRows row = null;

	/** The datecols. */
	List<String> datecols = new ArrayList<String>();

	/** The timezone. */
	String timezone = null;
	/** The date format */
	String date_format = null;

	HashMap<String, Boolean> webViewControls = new HashMap<String, Boolean>();

	/**
	 * Instantiates a new oE list view adapter.
	 * 
	 * @param context
	 *            the context
	 * @param resource
	 *            the resource
	 * @param objects
	 *            the objects
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param db
	 *            the db
	 */
	public OEListViewAdapter(Context context, int resource,
			List<OEListViewRows> objects, String[] from, int[] to,
			BaseDBHelper db) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.resource_id = resource;
		this.rows = new ArrayList<OEListViewRows>(objects);
		this.to = to;
		this.unfiltered_rows = new ArrayList<OEListViewRows>(objects);
		this.from = from;
		this.dbHelper = db;
	}

	/**
	 * Instantiates a new oE list view adapter.
	 * 
	 * @param context
	 *            the context
	 * @param resource
	 *            the resource
	 * @param objects
	 *            the objects
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param db
	 *            the db
	 * @param changeBackground
	 *            the change background
	 * @param colors
	 *            the colors
	 * @param conditionKey
	 *            the condition key
	 */
	public OEListViewAdapter(Context context, int resource,
			List<OEListViewRows> objects, String[] from, int[] to,
			BaseDBHelper db, boolean changeBackground, int[] colors,
			String conditionKey) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.resource_id = resource;
		this.rows = new ArrayList<OEListViewRows>(objects);
		this.unfiltered_rows = new ArrayList<OEListViewRows>(objects);
		this.to = to;
		this.from = from;
		this.colors = colors;
		this.canChangeBackground = changeBackground;
		this.conditionKey = conditionKey;
		this.dbHelper = db;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		viewRow = convertView;
		parentView = parent;
		LayoutInflater inflater = ((MainActivity) context).getLayoutInflater();
		if (viewRow == null) {
			viewRow = inflater.inflate(this.resource_id, parent, false);
		}
		row = this.rows.get(position);
		rowdata = row.getRow_data();
		for (final Integer control_id : controlClickHandler.keySet()) {
			viewRow.findViewById(control_id).setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							controlClickHandler.get(control_id).controlClicked(
									position, rows.get(position), viewRow);
						}
					});
		}

		for (int i = 0; i < this.to.length; i++) {
			final String key = from[i];
			if (booleanEvents.contains(from[i])) {
				handleBinaryBackground(row.getRow_id(), key, to[i], viewRow,
						position);
			} else if (backgroundChange.containsKey(key)) {
				String backFlag = rowdata.get(key).toString();
				if (!backFlag.equals("false")) {
					backFlag = "true";
				}
				int color = backgroundChange.get(key).get(backFlag);
				viewRow.findViewById(this.to[i]).setBackgroundColor(color);
				continue;
			} else if (imageCols.contains(from[i])) {
				String data = rowdata.get(from[i]).toString();
				if (!data.equals("false")) {
					ImageView imgView = (ImageView) viewRow
							.findViewById(this.to[i]);
					imgView.setImageBitmap(Base64Helper.getBitmapImage(context,
							data));
				}
			} else {
				TextView txvObj = null;
				WebView webview = null;
				if (!webViewControls.containsKey(this.from[i])) {
					txvObj = (TextView) viewRow.findViewById(this.to[i]);
				} else {
					if (webViewControls.get(this.from[i])) {
						webview = (WebView) viewRow.findViewById(this.to[i]);
						webview.getSettings().setJavaScriptEnabled(true);
						webview.getSettings().setBuiltInZoomControls(true);
					} else {
						txvObj = (TextView) viewRow.findViewById(this.to[i]);
					}
				}

				String key_col = this.from[i];
				String alt_key_col = key_col;
				if (key_col.contains("|")) {
					String[] splits = key_col.split("\\|");
					key_col = splits[0];
					alt_key_col = splits[1];
				}
				String data = rowdata.get(key_col).toString();
				if (data.equals("false") || TextUtils.isEmpty(data)) {
					data = rowdata.get(alt_key_col).toString();
				}
				if (this.cleanColumn.contains(key_col)) {
					data = HTMLHelper.htmlToString(data);
				}

				if (datecols.contains(key_col)) {
					if (date_format != null) {
						data = OEDate.getDate(data, TimeZone.getDefault()
								.getID(), date_format);
					} else {
						data = OEDate.getDate(data, TimeZone.getDefault()
								.getID());
					}
				}

				if (!data.equals("false")) {
					try {
						StringBuffer inputdata = new StringBuffer();
						JSONArray tmpData = new JSONArray(data);
						for (int k = 0; k < tmpData.length(); k++) {
							if (tmpData.get(k) instanceof JSONArray) {
								if (tmpData.getJSONArray(k).length() == 2) {
									inputdata.append(tmpData.getJSONArray(k)
											.getString(1));
									inputdata.append(",");
								}
							} else {
								inputdata.append(tmpData.getString(0));
								inputdata.append(",");
							}
						}
						int index = inputdata.lastIndexOf(",");
						if (index > 0) {
							inputdata.deleteCharAt(index);
						}
						txvObj.setText(inputdata.toString());
					} catch (Exception e) {
						if (this.toHtml.contains(key_col)) {
							if (webViewControls.get(this.from[i])) {
								String customHtml = data;
								webview.loadData(customHtml, "text/html",
										"UTF-8");
							} else {
								txvObj.setText(HTMLHelper.stringToHtml(data));
							}
						} else {
							txvObj.setText(data);
						}

					}

				} else {
					txvObj.setText("");
				}
			}
		}
		if (this.canChangeBackground && !viewRow.isSelected()) {
			boolean flag = Boolean.parseBoolean(rowdata.get(conditionKey)
					.toString());
			if (flag) {
				viewRow.setBackgroundResource(colors[1]);

			} else {
				viewRow.setBackgroundResource(colors[0]);
			}
		}
		if (viewListener != null) {
			viewRow = viewListener.listViewOnCreateListener(position, viewRow,
					this.rows.get(position));
		}

		return viewRow;
	}

	/**
	 * Sets the boolean event operation.
	 * 
	 * @param column
	 *            the column
	 * @param true_flag
	 *            the true_flag
	 * @param false_flag
	 *            the false_flag
	 * @param callback
	 *            the callback
	 */
	public void setBooleanEventOperation(String column, int true_flag,
			int false_flag, BooleanColumnCallback callback) {
		booleanEvents.add(column);
		this.binary_flag[0] = false_flag;
		this.binary_flag[1] = true_flag;
		this.callbacks.put(column, callback);
	}

	/**
	 * Sets the item click listener.
	 * 
	 * @param control_id
	 *            the control_id
	 * @param itemClick
	 *            the item click
	 */
	public void setItemClickListener(int control_id,
			ControlClickEventListener itemClick) {
		controlClickHandler.put(control_id, itemClick);
	}

	/**
	 * Clean html to text on.
	 * 
	 * @param column
	 *            the column
	 */
	public void cleanHtmlToTextOn(String column) {
		cleanColumn.add(column);
	}

	/**
	 * Clean date.
	 * 
	 * @param column
	 *            the column
	 * @param timezone
	 *            the timezone
	 */
	public void cleanDate(String column, String timezone) {
		datecols.add(column);
		this.timezone = timezone;
		this.date_format = null;
	}

	public void cleanDate(String column, String timezone, String format) {
		datecols.add(column);
		this.timezone = timezone;
		this.date_format = format;
	}

	/**
	 * Adds the image column.
	 * 
	 * @param column
	 *            the column
	 */
	public void addImageColumn(String column) {
		imageCols.add(column);
	}

	/**
	 * Layout background color.
	 * 
	 * @param column
	 *            the column
	 * @param true_color
	 *            the true_color
	 * @param false_color
	 *            the false_color
	 */
	public void layoutBackgroundColor(String column, int true_color,
			int false_color) {
		HashMap<String, Integer> colors = new HashMap<String, Integer>();
		colors.put("true", true_color);
		colors.put("false", false_color);
		backgroundChange.put(column, colors);
	}

	/**
	 * Handle binary background.
	 * 
	 * @param row_id
	 *            the row_id
	 * @param key
	 *            the key
	 * @param resource
	 *            the resource
	 * @param viewRow
	 *            the view row
	 * @param position
	 *            the position
	 */
	private void handleBinaryBackground(final int row_id, final String key,
			int resource, View viewRow, final int position) {
		final ImageView booleanView = (ImageView) viewRow
				.findViewById(resource);
		int flag = 0;
		String rowKeyVal = rows.get(position).getRow_data().get(key).toString();
		if (isFlagged.containsKey(String.valueOf(position))) {
			if (isFlagged.get(String.valueOf(position)).toString()
					.equals("true")) {
				flag = 1;
			}
		} else {
			if (rowKeyVal.equals("true")) {
				isFlagged.put(String.valueOf(position), "true");
				flag = 1;
			}
		}

		booleanView.setImageResource(binary_flag[flag]);
		booleanView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				try {
					if (OpenERPServerConnection.isNetworkAvailable(context)) {
						OEListViewRows newRow = callbacks.get(key)
								.updateFlagValues(rows.get(position),
										booleanView);
						rowdata = newRow.getRow_data();
						rows.get(position).setRow_data(newRow.getRow_data());

						isFlagged.put(String.valueOf(position), rowdata
								.get(key).toString());

					} else {
						Toast.makeText(context,
								"Please Check your connection to server.",
								Toast.LENGTH_LONG).show();
					}
				} catch (Exception e) {

				}
			}
		});

	}

	/**
	 * Update rows.
	 * 
	 * @param newRowVal
	 *            the new row val
	 * @param position
	 *            the position
	 * @param column
	 *            the column
	 */
	public void updateRows(OEListViewRows newRowVal, int position, String column) {
		rows.get(position).setRow_data(newRowVal.getRow_data());
		rowdata = newRowVal.getRow_data();
		isFlagged.put(String.valueOf(position), rowdata.get(column).toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getFilter()
	 */
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Filter#performFiltering(java.lang.CharSequence)
		 */
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			// TODO Auto-generated method stub
			FilterResults result = new FilterResults();
			if (constraint != null && constraint.toString().length() > 0) {
				constraint = constraint.toString().toLowerCase();
				ArrayList<OEListViewRows> filteredItems = new ArrayList<OEListViewRows>();
				for (int i = 0; i < unfiltered_rows.size(); i++) {
					OEListViewRows p = unfiltered_rows.get(i);
					HashMap<String, Object> data = p.getRow_data();
					if (data.toString().toLowerCase().contains(constraint)) {
						filteredItems.add(p);
					}
				}
				result.count = filteredItems.size();
				result.values = filteredItems;
			} else {
				synchronized (this) {
					result.values = unfiltered_rows;
					result.count = unfiltered_rows.size();
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
			// TODO Auto-generated method stub

			if (results.count > 0) {
				clear();
				rows = (List<OEListViewRows>) results.values;
				addAll(rows);
				notifyDataSetChanged();

			} else {
				notifyDataSetInvalidated();
			}

		}

	}

	/**
	 * Refresh.
	 * 
	 * @param items
	 *            the items
	 */
	public void refresh(List<OEListViewRows> items) {
		this.rows = items;
		this.unfiltered_rows = items;
		notifyDataSetChanged();
	}

	public void updateRow(int position, OEListViewRows row) {
		rows.remove(position);
		rows.add(position, row);
		unfiltered_rows.remove(position);
		unfiltered_rows.add(position, row);
		notifyDataSetChanged();
	}

	/**
	 * To html.
	 * 
	 * @param column
	 *            the column
	 */
	public void toHTML(String column) {
		toHtml.add(column);
		webViewControls.put(column, false);
	}

	public void toHTML(String column, boolean isWebView) {
		toHtml.add(column);
		webViewControls.put(column, isWebView);
	}

	public void addViewListener(OEListViewOnCreateListener viewListener) {
		this.viewListener = viewListener;
	}
}
