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

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

// TODO: Auto-generated Javadoc
/**
 * The Class OEDomainHelper.
 */
public class OEDomainHelper {

	/** The _key. */
	private String _key;

	/** The domains. */
	JSONObject domains = null;

	/**
	 * Instantiates a new oE domain helper.
	 * 
	 * @param key
	 *            the key
	 */
	public OEDomainHelper(String key) {
		this._key = key;
		domains = new JSONObject();
	}

	/**
	 * Adds the domain.
	 * 
	 * @param field
	 *            the field
	 * @param operator
	 *            the operator
	 * @param value
	 *            the value
	 */
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
