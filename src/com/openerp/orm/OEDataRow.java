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
