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

}
