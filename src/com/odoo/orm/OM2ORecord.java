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
	private Integer record_id = 0;
	private OModel base_model = null;
	private OModel rel_model = null;

	public OM2ORecord(OModel base, OColumn col, Integer rec_id) {
		base_model = base;
		mCol = col;
		record_id = rec_id;
	}

	public Integer getId() {
		return record_id;
	}

	public String getName() {
		rel_model = base_model.createInstance(mCol.getType());
		return rel_model.getName(record_id);
	}

	public ODataRow browse(OModel rel_model) {
		if (record_id != null) {
			return rel_model.select(record_id);
		}
		return null;
	}

	public ODataRow browse() {
		rel_model = base_model.createInstance(mCol.getType());
		return browse(rel_model);
	}
}
