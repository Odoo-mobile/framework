package com.odoo.orm;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

public class OContentResolver {
	private OModel mModel = null;
	private Context mContext;

	public OContentResolver(OModel model, Context context) {
		mModel = model;
		mContext = context;
	}

	public void delete(int id) {
		mContext.getContentResolver().delete(mModel.uri(),
				OColumn.ROW_ID + " = ? ", new String[] { id + "" });
	}

	public int insert(OValues values) {
		ContentValues vals = values.toContentValues();
		if (!vals.containsKey("id"))
			vals.put("id", "0");
		if (!vals.containsKey("odoo_name"))
			vals.put("odoo_name", mModel.getUser().getAndroidName());
		Uri uri = mContext.getContentResolver().insert(mModel.uri(), vals);
		return Integer.parseInt(uri.getLastPathSegment());
	}

	public void update(Integer id, OValues values) {
		ContentValues vals = values.toContentValues();
		if (!vals.containsKey("odoo_name"))
			vals.put("odoo_name", mModel.getUser().getAndroidName());
		Uri uri = mModel.uri().buildUpon().appendPath(id + "").build();
		mContext.getContentResolver().update(uri, vals, null, null);

	}
}
