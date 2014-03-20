/**
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http://www.openerp.com>)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * 
 */
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
