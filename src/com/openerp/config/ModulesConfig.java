/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http:www.openerp.com>)
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
package com.openerp.config;

import java.util.ArrayList;

import com.openerp.addons.messages.Message;
import com.openerp.support.Module;

// TODO: Auto-generated Javadoc
/**
 * The Class ModulesConfig.
 */
public class ModulesConfig {

	/**
	 * Application modules.
	 * 
	 * @return the array list
	 */
	public ArrayList<Module> applicationModules() {
		ArrayList<Module> modules = new ArrayList<Module>();

		/* application modules */
		// modules.add(new Module("module_idea", "Idea", new Idea(), 0));
		modules.add(new Module("menu_message", "Message", new Message(), 0,
				true));

		return modules;
	}

}
