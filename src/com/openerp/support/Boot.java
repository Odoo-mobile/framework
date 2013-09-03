/*
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
package com.openerp.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.content.Context;

import com.openerp.MainActivity;
import com.openerp.base.ir.AttachmentFragment;
import com.openerp.base.res.ResFragment;
import com.openerp.config.ModulesConfig;
import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.SQLStatement;

// TODO: Auto-generated Javadoc
/**
 * The Class Boot.
 */
public class Boot {

	/** The logger. */
	private final java.util.logging.Logger logger = java.util.logging.Logger
			.getLogger(this.getClass().toString());

	/** The context. */
	private Context context;

	/** The modules. */
	private ArrayList<Module> modules = null;

	/** The statements. */
	private ArrayList<SQLStatement> statements = null;

	/**
	 * Instantiates a new boot.
	 * 
	 * @param context
	 *            the context
	 */
	public Boot(Context context) {

		((MainActivity) context).getActionBar()
				.setDisplayShowTitleEnabled(true);

		this.context = context;
		this.modules = new ModulesConfig().modules();
		this.statements = new ArrayList<SQLStatement>();
		loadBaseModules();
		this.initDatabase();

	}

	/**
	 * Load base modules.
	 */
	private void loadBaseModules() {
		this.modules.add(new Module("base_res", "Res_Partner",
				new ResFragment(), 0));
		this.modules.add(new Module("ir_attachment", "Ir_Attachment",
				new AttachmentFragment(), 0));
	}

	/**
	 * Inits the database.
	 * 
	 * @return true, if successful
	 */
	private boolean initDatabase() {
		for (Module module : this.modules) {
			try {
				Class newClass = Class.forName(module.getModuleInstance()
						.getClass().getName());
				if (newClass.isInstance(module.getModuleInstance())) {
					Object receiver = newClass.newInstance();
					Class params[] = new Class[1];
					params[0] = Context.class;

					Method method = newClass.getDeclaredMethod(
							"databaseHelper", params);

					Object obj = method.invoke(receiver, this.context);
					BaseDBHelper dbInfo = (BaseDBHelper) obj;
					SQLStatement statement = dbInfo.createStatement(dbInfo);
					dbInfo.createTable(statement);
				}

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return false;
	}

	/**
	 * Gets the all statements.
	 * 
	 * @return the all statements
	 */
	public ArrayList<SQLStatement> getAllStatements() {
		return this.statements;
	}

	/**
	 * Gets the modules.
	 * 
	 * @return the modules
	 */
	public ArrayList<Module> getModules() {
		// TODO Auto-generated method stub
		return modules;
	}

}
