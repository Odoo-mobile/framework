package com.openerp.support;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class OEDomainHelper {
	private String _key;
	JSONObject domains = null;

	public OEDomainHelper(String key) {
		this._key = key;
		domains = new JSONObject();
	}

	public void addDomain(String field, String operator, Object value) {
		JSONArray domain = new JSONArray();
		domain.put(field);
		domain.put(operator);
		if (value instanceof JSONArray) {
			domain.put((JSONArray) value);
		}

		Log.d("New Domain Added : ", domain.toString());
	}

}
