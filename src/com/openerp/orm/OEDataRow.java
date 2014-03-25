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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;

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

	public Float getFloat(String key) {
		return Float.parseFloat(_data.get(key).toString());
	}

	public String getString(String key) {
		if (_data.containsKey(key) && _data.get(key) != null)
			return _data.get(key).toString();
		else
			return "false";
	}

	public Boolean getBoolean(String key) {
		return Boolean.parseBoolean(_data.get(key).toString());
	}

	public IdName getIdName(String key) {
		String data = getString(key);
		IdName val = null;
		try {
			JSONArray arr = new JSONArray(data);
			if (arr.get(0) instanceof JSONArray) {
				if (arr.getJSONArray(0).length() == 2) {
					val = new IdName(Integer.parseInt(arr.getJSONArray(0)
							.getString(0)), arr.getJSONArray(0).getString(1));
				}
			} else {
				if (arr.length() == 2) {
					val = new IdName(Integer.parseInt(arr.getString(0)),
							arr.getString(1));
				}
			}
		} catch (Exception e) {
		}
		return val;
	}

	public OEM2ORecord getM2ORecord(String key) {
		return (OEM2ORecord) _data.get(key);
	}

	public OEM2MRecord getM2MRecord(String key) {
		return (OEM2MRecord) _data.get(key);
	}

	public OEO2MRecord getO2MRecord(String key) {
		return (OEO2MRecord) _data.get(key);
	}

	public List<String> keys() {
		List<String> list = new ArrayList<String>();
		list.addAll(_data.keySet());
		return list;
	}

	@Override
	public String toString() {
		return _data.toString();
	}

	public class IdName {
		Integer id;
		String name;

		public IdName(Integer id, String name) {
			super();
			this.id = id;
			this.name = name;
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}

}
