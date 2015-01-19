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
import java.util.Arrays;
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
				
		// h4k1m: get modules from fields
		Field[] fields = getClass().getDeclaredFields();
		OModule[] modules = new OModule[fields.length];
		
		for (int i = 0; i < fields.length; i++) {
		    fields[i].setAccessible(true);

		    try {
				modules[i] = (OModule) fields[i].get(this);	
		    } catch (Exception e) {
				e.printStackTrace();
		    }
 		}
		
		// h4k1m: sort menu modules array
		Arrays.sort(modules);

		// h4k1m: iterate over sorted modules instead of fields
		for (OModule module : modules) {
		    if (module.isDefault()) {
				mDefaultModule = module;
				mModules.add(0, module);
		    } else {
				mModules.add(module);
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
