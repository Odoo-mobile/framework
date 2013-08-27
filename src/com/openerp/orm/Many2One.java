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
 * The Class Many2One.
 */
public class Many2One {

	/** The model_name. */
	private String model_name = null;

	/** The m2o object. */
	private BaseDBHelper m2oObject = null;

	/**
	 * Instantiates a new many2 one.
	 * 
	 * @param model_name
	 *            the model_name
	 */
	public Many2One(String model_name) {
		super();
		this.model_name = model_name;
	}

	/**
	 * Gets the model name.
	 * 
	 * @return the model name
	 */
	public String getModelName() {
		return model_name;
	}

	/**
	 * Instantiates a new many2 one.
	 * 
	 * @param m2oObject
	 *            the m2o object
	 */
	public Many2One(BaseDBHelper m2oObject) {
		super();
		this.m2oObject = m2oObject;
	}

	/**
	 * Checks if is m2 o object.
	 * 
	 * @return true, if is m2 o object
	 */
	public boolean isM2OObject() {
		if (this.m2oObject != null) {
			return true;
		}
		return false;
	}

	/**
	 * Gets the m2 o object.
	 * 
	 * @return the m2 o object
	 */
	public BaseDBHelper getM2OObject() {
		return m2oObject;
	}

	/**
	 * Checks if is modle name.
	 * 
	 * @return true, if is modle name
	 */
	public boolean isModleName() {
		if (this.model_name != null) {
			return true;
		}
		return false;
	}
}
