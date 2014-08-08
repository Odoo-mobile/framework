/*
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
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
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * 
 */
package com.odoo.orm;

import java.util.List;

public class OO2MRecord {
	private OColumn mCol = null;
	private int mRecordId = 0;
	private OModel mDatabase = null;
	private OModel rel_model = null;
	private String mOrderBy = null;

	public OO2MRecord(OModel oModel, OColumn col, int id) {
		mDatabase = oModel;
		mCol = col;
		mRecordId = id;
	}

	public OO2MRecord setOrder(String order_by) {
		mOrderBy = order_by;
		return this;
	}

	public List<Integer> getIds() {
		rel_model = mDatabase.createInstance(mCol.getType());
		return getIds(rel_model);
	}

	public List<Integer> getIds(OModel rel_model) {
		return mDatabase.selecto2MRelIds(mDatabase, rel_model, mRecordId,
				mCol.getRelatedColumn());
	}

	public List<ODataRow> browseEach() {
		rel_model = mDatabase.createInstance(mCol.getType());
		String column = mCol.getRelatedColumn();
		return rel_model.select(column + " = ? ",
				new String[] { mRecordId + "" }, null, null, mOrderBy);
	}

	public ODataRow browseAt(int index) {
		List<ODataRow> list = browseEach();
		if (list.size() == 0) {
			return null;
		}
		return list.get(index);
	}

}
