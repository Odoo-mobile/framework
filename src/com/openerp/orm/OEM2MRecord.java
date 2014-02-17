package com.openerp.orm;

import java.util.List;

public class OEM2MRecord {
	OEColumn mCol = null;
	int mId = 0;
	OEDatabase mDatabase = null;

	public OEM2MRecord(OEDatabase oeDatabase, OEColumn col, int id) {
		mDatabase = oeDatabase;
		mCol = col;
		mId = id;
	}

	public List<OEDataRow> browseEach() {
		OEManyToMany m2o = (OEManyToMany) mCol.getType();
		return mDatabase.selectM2M(m2o.getDBHelper(), mDatabase.tableName()
				+ "_id = ?", new String[] { mId + "" });
	}

	public OEDataRow browseAt(int index) {
		List<OEDataRow> list = browseEach();
		if (list.size() == 0) {
			return null;
		}
		return list.get(index);
	}

}
