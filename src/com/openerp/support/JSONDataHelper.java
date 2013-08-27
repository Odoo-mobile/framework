package com.openerp.support;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

public class JSONDataHelper {
    public List<String> arrayToStringList(JSONArray array) {
	List<String> list = new ArrayList<String>();
	for (int i = 0; i < array.length(); i++) {
	    try {
		list.add(array.getString(i));
	    } catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	return list;
    }

    public static JSONArray intArrayToJSONArray(int ids[]) {
	JSONArray idsArr = new JSONArray();
	if (ids != null) {
	    for (int id : ids) {
		idsArr.put(id);
	    }
	}
	return idsArr;
    }

    public static int[] jsonArrayTointArray(JSONArray ids) {
	int newIds[] = new int[ids.length()];
	if (ids != null) {
	    for (int i = 0; i < ids.length(); i++) {
		try {
		    newIds[i] = ids.getInt(i);
		} catch (Exception e) {
		}
	    }
	}
	return newIds;
    }
}
