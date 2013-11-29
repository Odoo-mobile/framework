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

// TODO: Auto-generated Javadoc
/**
 * The Class OEArgsHelper.
 */
public class OEArgsHelper {

	/** The args. */
	private JSONArray args = null;

	/**
	 * Instantiates a new oE args helper.
	 */
	public OEArgsHelper() {
		args = new JSONArray();
	}

	/**
	 * Adds the arg.
	 * 
	 * @param value
	 *            the value
	 */
	public void addArg(Object value) {
		this.args.put(value);
		if (value != null) {
		}
	}

	/**
	 * Adds the arg.
	 * 
	 * @param value
	 *            the value
	 * @param operator
	 *            the operator
	 */
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

	/**
	 * Adds the arg condition.
	 * 
	 * @param col
	 *            the col
	 * @param operator
	 *            the operator
	 * @param value
	 *            the value
	 */
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

	/**
	 * Gets the args.
	 * 
	 * @return the args
	 */
	public JSONArray getArgs() {
		return this.args;
	}
}
