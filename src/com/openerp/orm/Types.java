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
package com.openerp.orm;


// TODO: Auto-generated Javadoc
/**
 * The Class Types. Handles SQLite Datatypes requied to handle database
 */
public class Types {

	/**
	 * Integer.
	 * 
	 * @return the string
	 */
	public static String integer() {
		return "INTEGER";
	}

	/**
	 * Varchar.
	 * 
	 * @param size
	 *            the size
	 * @return the string
	 */
	public static String varchar(int size) {
		return "VARCHAR(" + String.valueOf(size) + ")";
	}

	/**
	 * Text.
	 * 
	 * @return the string
	 */
	public static String text() {
		return "TEXT";
	}

	/**
	 * Blob.
	 * 
	 * @return the string
	 */
	public static String blob() {
		return "BLOB";
	}

	/**
	 * many2one.
	 * 
	 * @param model
	 *            the model
	 * @return the many2 many
	 */
	public static Many2Many many2Many(String model) {
		return new Many2Many(model);
	}

	/**
	 * Many2 one.
	 * 
	 * @param model
	 *            the model
	 * @return the many2 one
	 */
	public static Many2One many2One(String model) {
		return new Many2One(model);
	}

	/**
	 * Many2 many.
	 * 
	 * @param m2mObj
	 *            the m2m obj
	 * @return the many2 many
	 */
	public static Many2Many many2Many(BaseDBHelper m2mObj) {
		// TODO Auto-generated method stub
		return new Many2Many(m2mObj);
	}

	/**
	 * Many2 one.
	 * 
	 * @param m2oObj
	 *            the m2o obj
	 * @return the object
	 */
	public static Object many2One(BaseDBHelper m2oObj) {
		// TODO Auto-generated method stub
		return new Many2One(m2oObj);
	}

}
