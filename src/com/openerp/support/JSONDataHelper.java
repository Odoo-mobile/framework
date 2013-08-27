/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http://www.openerp.com>)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * 
 */
package com.openerp.support;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

// TODO: Auto-generated Javadoc
/**
 * The Class JSONDataHelper.
 */
public class JSONDataHelper {

	/**
	 * Array to string list.
	 * 
	 * @param array
	 *            the array
	 * @return the list
	 */
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

	/**
	 * Int array to json array.
	 * 
	 * @param ids
	 *            the ids
	 * @return the jSON array
	 */
	public static JSONArray intArrayToJSONArray(int ids[]) {
		JSONArray idsArr = new JSONArray();
		if (ids != null) {
			for (int id : ids) {
				idsArr.put(id);
			}
		}
		return idsArr;
	}

	/**
	 * Json array toint array.
	 * 
	 * @param ids
	 *            the ids
	 * @return the int[]
	 */
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
