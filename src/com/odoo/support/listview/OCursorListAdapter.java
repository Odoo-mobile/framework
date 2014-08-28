package com.odoo.support.listview;

import odoo.controls.OForm;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.odoo.orm.ODataRow;

public class OCursorListAdapter extends CursorAdapter {

	private Integer mLayout = null;
	private LayoutInflater mInflater = null;

	public OCursorListAdapter(Context context, Cursor c, int layout) {
		super(context, c, false);
		mLayout = layout;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		OForm form = (OForm) view;
		final ODataRow row = new ODataRow();
		for (String col : cursor.getColumnNames()) {
			row.put(col, getValue(cursor, col));
		}
		form.initForm(row);
	}

	private Object getValue(Cursor c, String column) {
		Object value = false;
		int index = c.getColumnIndex(column);
		switch (c.getType(index)) {
		case Cursor.FIELD_TYPE_NULL:
			value = false;
			break;
		case Cursor.FIELD_TYPE_BLOB:
		case Cursor.FIELD_TYPE_STRING:
			value = c.getString(index);
			break;
		case Cursor.FIELD_TYPE_FLOAT:
			value = c.getFloat(index);
			break;
		case Cursor.FIELD_TYPE_INTEGER:
			value = c.getInt(index);
			break;
		}
		return value;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
		return mInflater.inflate(mLayout, viewGroup, false);
	}

}
