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
package com.openerp.orm;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;

// TODO: Auto-generated Javadoc
/**
 * The Class BaseDBHelper.
 */
public class BaseDBHelper extends ORM {

	/** The columns. */
	public ArrayList<Fields> columns = null;

	/** The name. */
	public String name = "";

	/**
	 * Instantiates a new base db helper.
	 * 
	 * @param context
	 *            the context
	 */
	public BaseDBHelper(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.columns = new ArrayList<Fields>();
		this.columns.add(new Fields("id", "Id", Types.integer()));
		this.columns
				.add(new Fields("oea_name", "OpenERP User", Types.varchar(100),
						false,
						"OpenERP Account manager name used for login and filter multiple accounts."));
	}

	/**
	 * Gets the columns.
	 * 
	 * @return the columns
	 */
	public ArrayList<Fields> getColumns() {
		return this.columns;
	}

	/**
	 * Gets the server columns.
	 * 
	 * @return the server columns
	 */
	public ArrayList<Fields> getServerColumns() {
		ArrayList<Fields> serverCols = new ArrayList<Fields>();
		for (Fields fields : this.columns) {
			if (fields.isCanSync()) {
				serverCols.add(fields);
			}
		}
		return serverCols;
	}

	/**
	 * Gets the many2 many columns.
	 * 
	 * @return the many2 many columns
	 */
	public HashMap<String, Object> getMany2ManyColumns() {
		HashMap<String, Object> list = new HashMap<String, Object>();
		for (Fields field : this.columns) {
			if (field.getType() instanceof Many2Many) {
				list.put(field.getName(), field.getType());
			}
		}
		return list;
	}

	/**
	 * Gets the many2 one columns.
	 * 
	 * @return the many2 one columns
	 */
	public HashMap<String, Object> getMany2OneColumns() {
		HashMap<String, Object> list = new HashMap<String, Object>();
		for (Fields field : this.columns) {
			if (field.getType() instanceof Many2One) {
				list.put(field.getName(), field.getType());
			}
		}
		return list;
	}

	/**
	 * Gets the model name.
	 * 
	 * @return the model name
	 */
	public String getModelName() {
		return this.name;
	}

	/**
	 * Gets the table name.
	 * 
	 * @return the table name
	 */
	public String getTableName() {
		return this.name.replaceAll("\\.", "_");
	}

}
