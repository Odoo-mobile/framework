package odoo.controls;

import java.util.HashMap;

public class OControlAttributes extends HashMap<String, Object> {

	private static final long serialVersionUID = 1L;

	public Boolean getBoolean(String key, Boolean defValue) {
		if (containsKey(key))
			return (Boolean) get(key);
		return defValue;
	}

	public String getString(String key, String defValue) {
		if (containsKey(key))
			return get(key).toString();
		return defValue;
	}

	public Integer getColor(String key, Integer defValue) {
		if (containsKey(key))
			return (Integer) get(key);
		return defValue;
	}

	public Integer getResource(String key, Integer defValue) {
		if (containsKey(key))
			return (Integer) get(key);
		return defValue;
	}
}
