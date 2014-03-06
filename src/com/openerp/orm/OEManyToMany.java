package com.openerp.orm;

public class OEManyToMany {
	OEDBHelper mDb = null;

	public OEManyToMany(OEDBHelper db) {
		mDb = db;
	}

	public OEDBHelper getDBHelper() {
		return mDb;
	}
}
