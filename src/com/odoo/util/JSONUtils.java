package com.odoo.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

public class JSONUtils {
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
}
