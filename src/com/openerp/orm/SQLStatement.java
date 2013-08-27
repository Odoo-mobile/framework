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

// TODO: Auto-generated Javadoc
/**
 * The Class SQLStatement.
 */
public class SQLStatement {

	/** The table_name. */
	private String table_name;

	/** The type. */
	private String type;

	/** The statement. */
	private String statement;

	/**
	 * Gets the table_name.
	 * 
	 * @return the table_name
	 */
	public String getTable_name() {
		return table_name;
	}

	/**
	 * Sets the table_name.
	 * 
	 * @param table_name
	 *            the new table_name
	 */
	public void setTable_name(String table_name) {
		this.table_name = table_name;
	}

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type.
	 * 
	 * @param type
	 *            the new type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the statement.
	 * 
	 * @return the statement
	 */
	public String getStatement() {
		return statement;
	}

	/**
	 * Sets the statement.
	 * 
	 * @param statement
	 *            the new statement
	 */
	public void setStatement(String statement) {
		this.statement = statement;
	}
}
