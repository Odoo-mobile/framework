package com.openerp.orm.types;

abstract public class OETypeHelper {
	String mType = "";
	String mPattern = "";
	int mSize = 0;

	public boolean equals(String key) {
		return mType.equals(key);
	}

	public String getType() {
		String type = mType;
		if (mSize > 0)
			type += " (" + mSize + ")";
		return type;
	}

	public String getPattern() {
		return mPattern;
	}
}
