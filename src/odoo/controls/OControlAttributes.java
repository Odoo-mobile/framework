/*
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
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
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * 
 */
package odoo.controls;

import java.util.HashMap;

/**
 * The Class OControlAttributes.
 */
public class OControlAttributes extends HashMap<String, Object> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Gets the boolean.
	 * 
	 * @param key
	 *            the key
	 * @param defValue
	 *            the def value
	 * @return the boolean
	 */
	public Boolean getBoolean(String key, Boolean defValue) {
		if (containsKey(key))
			return (Boolean) get(key);
		return defValue;
	}

	/**
	 * Gets the string.
	 * 
	 * @param key
	 *            the key
	 * @param defValue
	 *            the def value
	 * @return the string
	 */
	public String getString(String key, String defValue) {
		if (containsKey(key))
			return get(key).toString();
		return defValue;
	}

	/**
	 * Gets the color.
	 * 
	 * @param key
	 *            the key
	 * @param defValue
	 *            the def value
	 * @return the color
	 */
	public Integer getColor(String key, Integer defValue) {
		if (containsKey(key))
			return (Integer) get(key);
		return defValue;
	}

	/**
	 * Gets the resource.
	 * 
	 * @param key
	 *            the key
	 * @param defValue
	 *            the def value
	 * @return the resource
	 */
	public Integer getResource(String key, Integer defValue) {
		if (containsKey(key))
			return (Integer) get(key);
		return defValue;
	}
}
