package com.openerp.orm.types;

public class OEVarchar extends OETypeHelper {

	public static final String KEY = "VARCHAR";

	public OEVarchar(int size) {
		mType = OEVarchar.KEY;
		mSize = size;

	}

}
