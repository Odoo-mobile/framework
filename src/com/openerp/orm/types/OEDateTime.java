package com.openerp.orm.types;

public class OEDateTime extends OETypeHelper {

	public static final String KEY = "VARCHAR";

	public OEDateTime(String dateformat) {
		mType = OEDateTime.KEY;
		mPattern = dateformat;
	}

}
