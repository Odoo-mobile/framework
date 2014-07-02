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

public class OM2ORecord {
	private OColumn mCol = null;
	private String record_id = "false";
	private OModel base_model = null;

	public OM2ORecord(OModel base, OColumn col, String value) {
		base_model = base;
		mCol = col;
		record_id = value;
	}

	public Object getId() {
		if (record_id == null || record_id.equals("false"))
			return 0;
		return Integer.parseInt(record_id);
	}

	public ODataRow browse() {
		OModel rel = base_model.createInstance(mCol.getType());
		if (record_id == null || record_id.equals("false"))
			return null;
		return rel.select(Integer.parseInt(record_id));
	}
}
