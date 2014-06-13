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
package com.odoo.orm;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import com.odoo.orm.OEM2MIds.Operation;

public class OEO2MIds {

	/** The operation. */
	Operation mOperation = null;

	/** The List<Integer> ids. */
	private List<Integer> mIds = new ArrayList<Integer>();

	public OEO2MIds(Operation operation, List<Integer> ids) {
		mIds.clear();
		mOperation = operation;
		mIds.addAll(ids);
	}

	public Operation getOperation() {
		return mOperation;
	}

	public void setOperation(Operation mOperation) {
		this.mOperation = mOperation;
	}

	public List<Integer> getIds() {
		return mIds;
	}

	public JSONArray getJSONIds() {
		JSONArray ids = new JSONArray();
		try {
			for (int id : mIds) {
				ids.put(id);
			}
		} catch (Exception e) {
		}
		return ids;
	}

	public void setIds(List<Integer> ids) {
		mIds.clear();
		mIds.addAll(ids);
	}

}
