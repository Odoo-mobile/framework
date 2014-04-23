package com.openerp.orm.types;

public class OEReal extends OETypeHelper {

	public static final String KEY = "REAL";

	public OEReal(int size) {
		mType = OEReal.KEY;
		mSize = size;
	}

}
