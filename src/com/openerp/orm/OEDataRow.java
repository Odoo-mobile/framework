/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http:www.openerp.com>)
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
package com.openerp.orm;

import java.util.HashMap;

public class OEDataRow {
	HashMap<String, Object> _data = new HashMap<String, Object>();

	public void put(String key, Object value) {
		_data.put(key, value);
	}

	public Object get(String key) {
		return _data.get(key);
	}

	public Integer getInt(String key) {
		return Integer.parseInt(_data.get(key).toString());
	}

	public String getString(String key) {
		return _data.get(key).toString();
	}

	public Boolean getBoolean(String key) {
		return Boolean.parseBoolean(_data.get(key).toString());
	}

	@Override
	public String toString() {
		return _data.toString();
	}

}
