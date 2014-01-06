package com.openerp.orm;

import java.util.HashMap;

import org.json.JSONArray;

public class OEM2MRecord {
	public static HashMap<String, Object> get(String record) {
		HashMap<String, Object> data = new HashMap<String, Object>();
		try {
			JSONArray arr = new JSONArray(record);
			data.put("id", arr.getJSONArray(0).get(0));
			data.put("name", arr.getJSONArray(0).get(1));
		} catch (Exception e) {
			return null;
		}
		return data;
	}
}
