package com.openerp.support.listview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.openerp.MainActivity;
import com.openerp.orm.BaseDBHelper;
import com.openerp.support.OpenERPServerConnection;
import com.openerp.support.UserObject;
import com.openerp.util.Base64Helper;
import com.openerp.util.HTMLHelper;
import com.openerp.util.OEDate;

public class OEListViewAdapter extends ArrayAdapter<OEListViewRows> {
    Context context;
    int resource_id;
    public List<OEListViewRows> rows = null;
    public List<OEListViewRows> unfiltered_rows = null;
    int[] to = null;
    String[] from = null;
    int[] colors = null;
    boolean canChangeBackground = false;
    String conditionKey = null;
    List<String> cleanColumn = new ArrayList<String>();
    List<String> booleanEvents = new ArrayList<String>();
    List<String> imageCols = new ArrayList<String>();
    HashMap<String, HashMap<String, Integer>> backgroundChange = new HashMap<String, HashMap<String, Integer>>();
    HashMap<Integer, ControlClickEventListener> controlClickHandler = new HashMap<Integer, ControlClickEventListener>();
    BaseDBHelper dbHelper = null;
    HashMap<String, Object> rowdata = null;
    int[] binary_flag = new int[2];
    HashMap<String, BooleanColumnCallback> callbacks = new HashMap<String, BooleanColumnCallback>();
    ViewGroup parentView = null;
    View viewRow = null;
    ItemFilter filter = null;
    List<String> toHtml = new ArrayList<String>();
    public HashMap<String, String> isFlagged = new HashMap<String, String>();
    OEListViewRows row = null;
    List<String> datecols = new ArrayList<String>();

    String timezone = null;

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
			    // TODO Auto-generated method stub
			    controlClickHandler.get(control_id).controlClicked(
				    rows.get(position), viewRow);

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
		    imgView.setImageBitmap(Base64Helper.getBitmapImage(
			    context, data));
		}
	    } else {
		TextView txvObj = (TextView) viewRow.findViewById(this.to[i]);
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
		    data = OEDate.getDate(data, timezone);
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
			    txvObj.setText(HTMLHelper.stringToHtml(data));
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

	return viewRow;
    }

    public void setBooleanEventOperation(String column, int true_flag,
	    int false_flag, BooleanColumnCallback callback) {
	booleanEvents.add(column);
	this.binary_flag[0] = false_flag;
	this.binary_flag[1] = true_flag;
	this.callbacks.put(column, callback);
    }

    public void setItemClickListener(int control_id,
	    ControlClickEventListener itemClick) {
	controlClickHandler.put(control_id, itemClick);
    }

    public void cleanHtmlToTextOn(String column) {
	cleanColumn.add(column);
    }

    public void cleanDate(String column, String timezone) {
	datecols.add(column);
	this.timezone = timezone;
    }

    public void addImageColumn(String column) {
	imageCols.add(column);
    }

    public void layoutBackgroundColor(String column, int true_color,
	    int false_color) {
	HashMap<String, Integer> colors = new HashMap<String, Integer>();
	colors.put("true", true_color);
	colors.put("false", false_color);
	backgroundChange.put(column, colors);
    }

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
		// TODO Auto-generated method stub

		if (OpenERPServerConnection.isNetworkAvailable(context)) {
		    OEListViewRows newRow = callbacks.get(key)
			    .updateFlagValues(rows.get(position), booleanView);
		    rowdata = newRow.getRow_data();
		    rows.get(position).setRow_data(newRow.getRow_data());

		    isFlagged.put(String.valueOf(position), rowdata.get(key)
			    .toString());

		} else {
		    Toast.makeText(context,
			    "Please Check your connection to server.",
			    Toast.LENGTH_LONG).show();
		}

	    }
	});

    }

    public void updateRows(OEListViewRows newRowVal, int position, String column) {
	rows.get(position).setRow_data(newRowVal.getRow_data());
	rowdata = newRowVal.getRow_data();
	isFlagged.put(String.valueOf(position), rowdata.get(column).toString());
	Log.e("UpdateRows", "ListView Adapter");
    }

    @Override
    public Filter getFilter() {
	if (filter == null) {
	    filter = new ItemFilter();
	}
	return filter;
    }

    class ItemFilter extends Filter {

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

    public void refresh(List<OEListViewRows> items) {
	this.rows = items;
	this.unfiltered_rows = items;
	notifyDataSetChanged();
    }

    public void toHTML(String column) {
	toHtml.add(column);
    }
}
