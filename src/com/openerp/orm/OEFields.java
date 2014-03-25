/**
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

/**
 * The Class OEFields. Different database data types. Also support many2many and
 * many2one
 */
public class OEFields {

	/**
	 * Varchar.
	 * 
	 * @param size
	 *            the size
	 * @return the string
	 */
	public static String varchar(int size) {
		return " VARCHAR(" + size + ") ";
	}

	/**
	 * Integer.
	 * 
	 * @return the string
	 */
	public static String integer() {
		return " INTEGER ";
	}

	/**
	 * Integer.
	 * 
	 * @param size
	 *            the size
	 * @return the string
	 */
	public static String integer(int size) {
		return " INTEGER(" + size + ") ";
	}

	/**
	 * Text.
	 * 
	 * @return the string
	 */
	public static String text() {
		return " TEXT ";
	}

	/**
	 * Blob.
	 * 
	 * @return the string
	 */
	public static String blob() {
		return " BLOB ";
	}

	/**
	 * Many to many.
	 * 
	 * @param db
	 *            the db
	 * @return the many to many object
	 */
	public static OEManyToMany manyToMany(Object db) {
		return new OEManyToMany((OEDBHelper) db);
	}

	/**
	 * Many to one.
	 * 
	 * @param db
	 *            the db
	 * @return the many to one object
	 */
	public static OEManyToOne manyToOne(Object db) {
		return new OEManyToOne((OEDBHelper) db);
	}

	/**
	 * One to many.
	 * 
	 * @param db
	 *            the db
	 * @return the one to many object
	 */
	public static OEOneToMany oneToMany(Object db) {
		return new OEOneToMany((OEDBHelper) db);
	}

}
