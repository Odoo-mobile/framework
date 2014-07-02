package com.odoo.orm;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class OFieldsHelper {
	public static final String TAG = "com.odoo.orm.OEFieldsHelper";
	JSONObject mFields = new JSONObject();
	List<OValues> mValues = new ArrayList<OValues>();
	List<OColumn> mColumns = new ArrayList<OColumn>();

	public OFieldsHelper(String[] fields) {
		addAll(fields);
	}

	public OFieldsHelper(List<OColumn> cols) {
		addAll(cols);
		mColumns.addAll(cols);
	}

	public void addAll(String[] fields) {
		try {
			for (int i = 0; i < fields.length; i++) {
				mFields.accumulate("fields", fields[i]);
			}
			if (fields.length == 1) {
				mFields.accumulate("fields", fields[0]);
			}
		} catch (Exception e) {
		}
	}

	public void addAll(List<OColumn> cols) {
		try {
			for (OColumn col : cols) {
				mFields.accumulate("fields", col.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public JSONObject get() {
		return mFields;
	}

	public List<OValues> getValues() {
		return mValues;
	}

}
