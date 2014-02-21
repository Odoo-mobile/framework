package com.openerp.orm;

public class OEM2ORecord {
	private OEColumn mCol = null;
	private String mValue = null;

	public OEM2ORecord(OEColumn col, String value) {
		mCol = col;
		mValue = value;
	}

	public OEDataRow browse() {
		OEManyToOne m2o = (OEManyToOne) mCol.getType();
		OEDatabase db = (OEDatabase) m2o.getDBHelper();
		if (mValue.equals("false")) {
			return null;
		}
		return db.select(Integer.parseInt(mValue));
	}
}
