package com.odoo.util;

import android.database.Cursor;

import com.odoo.orm.ODataRow;

public class CursorUtil {
	public static ODataRow toDatarow(Cursor cr) {
		ODataRow row = new ODataRow();
		for (String col : cr.getColumnNames()) {
			row.put(col, CursorUtil.cursorValue(col, cr));
		}
		return row;
	}

	public static Object cursorValue(String column, Cursor cr) {
		Object value = false;
		int index = cr.getColumnIndex(column);
		switch (cr.getType(index)) {
		case Cursor.FIELD_TYPE_NULL:
			value = false;
			break;
		case Cursor.FIELD_TYPE_STRING:
			value = cr.getString(index);
			break;
		case Cursor.FIELD_TYPE_INTEGER:
			value = cr.getInt(index);
			break;
		case Cursor.FIELD_TYPE_FLOAT:
			value = cr.getFloat(index);
			break;
		case Cursor.FIELD_TYPE_BLOB:
			value = cr.getBlob(index);
			break;
		}
		return value;
	}
}
