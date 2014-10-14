package com.odoo.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.odoo.orm.ODataRow;

public class JSONUtils {
	@SuppressWarnings("unchecked")
	public static <T> List<T> toList(JSONArray array) {
		List<T> list = new ArrayList<T>();
		try {
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					list.add((T) array.get(i));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public static <T> JSONArray toArray(List<T> list) {
		JSONArray array = new JSONArray();
		try {
			for (T obj : list)
				array.put(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return array;
	}

	public static ODataRow toDataRow(JSONObject json) {
		ODataRow row = new ODataRow();
		try {
			@SuppressWarnings("unchecked")
			Iterator<String> keys = json.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				row.put(key, json.get(key));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return row;
	}

	public static JSONObject toJSONObject(ODataRow row) {
		JSONObject json = new JSONObject();
		try {
			for (String key : row.keys()) {
				json.put(key, row.get(key));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return json;
	}
}
