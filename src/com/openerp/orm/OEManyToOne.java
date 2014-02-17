package com.openerp.orm;

public class OEManyToOne {
	OEDBHelper mDb = null;

	public OEManyToOne(OEDBHelper db) {
		mDb = db;
	}

	public OEDBHelper getDBHelper() {
		return mDb;
	}
}
