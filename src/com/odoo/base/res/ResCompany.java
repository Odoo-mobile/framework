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

import android.content.Context;

import com.odoo.orm.OColumn;
import com.odoo.orm.OModel;
import com.odoo.orm.types.OVarchar;

/**
 * The Class ResCompany.
 */
public class ResCompany extends OModel {

	OColumn name = new OColumn("Name", OVarchar.class, 100);

	public ResCompany(Context context) {
		super(context, "res.company");
	}

}