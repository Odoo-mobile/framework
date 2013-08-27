package com.openerp.support;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class OEArgsHelper {
	private JSONArray args = null;

	public OEArgsHelper() {
		args = new JSONArray();
	}

	public void addArg(Object value) {
		this.args.put(value);
		if (value != null) {
		}
	}

	public void addArg(Object value, String operator) {
		JSONArray domain = new JSONArray();
		if (value instanceof JSONArray) {
			domain.put((JSONArray) value);
		} else if (value instanceof JSONObject) {
			domain.put((JSONObject) value);
		} else {
			domain.put(value);
		}
		domain.put(operator);

		this.args.put(domain);
	}

	public void addArgCondition(String col, String operator, Object value) {

		this.args.put(col);
		this.args.put(operator);

		if (value instanceof JSONArray) {
			this.args.put((JSONArray) value);
		} else if (value instanceof JSONObject) {
			this.args.put((JSONObject) value);
		} else {
			this.args.put(value);
		}

	}

	public JSONArray getArgs() {
		return this.args;
	}
}
