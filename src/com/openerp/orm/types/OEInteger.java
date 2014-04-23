package com.openerp.orm.types;

public class OEInteger extends OETypeHelper {

	public static final String KEY = "INTEGER";

	public OEInteger(int size) {
		mType = OEInteger.KEY;
		mSize = size;
	}

}
