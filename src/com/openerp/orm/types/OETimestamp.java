package com.openerp.orm.types;

public class OETimestamp extends OETypeHelper {

	public static final String KEY = "VARCHAR";

	public OETimestamp(String dateformat) {
		mType = OETimestamp.KEY;
		mPattern = dateformat;
	}

}
