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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class OModulesHelper.
 */
public class OModulesHelper {

	/** The modules. */
	List<OModule> mModules = new ArrayList<OModule>();

	/** The default module. */
	OModule mDefaultModule = null;

	/**
	 * Gets the modules.
	 * 
	 * @return the modules
	 */
	public List<OModule> getModules() {
		if (mModules.size() <= 0)
			prepareModuleList();
		return mModules;
	}

	/**
	 * Prepare module list.
	 */
	private void prepareModuleList() {
		mModules.clear();
		for (Field module_col : getClass().getDeclaredFields()) {
			if (module_col.getType().isAssignableFrom(OModule.class)) {
				module_col.setAccessible(true);
				try {
					OModule module = (OModule) module_col.get(this);
					if (module.isDefault()) {
						mDefaultModule = module;
						mModules.add(0, module);
					} else {
						mModules.add(module);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Gets the default module.
	 * 
	 * @return the default module
	 */
	public OModule getDefaultModule() {
		return mDefaultModule;
	}
}
