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
package com.odoo.support;

import android.content.Context;

import com.odoo.orm.OModel;
import com.odoo.support.fragment.OModuleHelper;

public class OModule {

	Class<?> module = null;
	Boolean isDefault = false;

	public OModule(Class<?> module) {
		super();
		this.module = module;
	}

	public OModule setDefault() {
		isDefault = true;
		return this;
	}

	public Object newInstance() {
		try {
			return module.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public OModel getModel(Context context) {
		OModuleHelper module = (OModuleHelper) newInstance();
		return (OModel) module.databaseHelper(context);
	}
}
