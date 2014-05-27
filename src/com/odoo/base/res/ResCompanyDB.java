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
package com.odoo.base.res;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.odoo.orm.OEColumn;
import com.odoo.orm.OEDatabase;
import com.odoo.orm.OEFields;

/**
 * The Class Res_Company.
 */
public class ResCompanyDB extends OEDatabase {
	public ResCompanyDB(Context context) {
		super(context);
	}

	@Override
	public String getModelName() {
		return "res.company";
	}

	@Override
	public List<OEColumn> getModelColumns() {
		List<OEColumn> columns = new ArrayList<OEColumn>();
		columns.add(new OEColumn("name", "Name", OEFields.varchar(100)));
		return columns;
	}

}