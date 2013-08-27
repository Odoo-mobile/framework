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
 * The Class Many2Many.
 */
public class Many2Many {

	/** The model_name. */
	private String model_name = null;

	/** The m2m object. */
	private BaseDBHelper m2mObject = null;

	/**
	 * Instantiates a new many2 many.
	 * 
	 * @param model
	 *            the model
	 */
	public Many2Many(String model) {
		// TODO Auto-generated constructor stub
		this.model_name = model;
	}

	/**
	 * Instantiates a new many2 many.
	 * 
	 * @param obj
	 *            the obj
	 */
	public Many2Many(BaseDBHelper obj) {
		// TODO Auto-generated constructor stub
		this.m2mObject = obj;
	}

	/**
	 * Gets the model_name.
	 * 
	 * @return the model_name
	 */
	public String getModel_name() {
		return model_name;
	}

	/**
	 * Gets the m2m object.
	 * 
	 * @return the m2m object
	 */
	public BaseDBHelper getM2mObject() {
		return m2mObject;
	}

	/**
	 * Checks if is m2 m object.
	 * 
	 * @return true, if is m2 m object
	 */
	public boolean isM2MObject() {
		if (this.m2mObject != null) {
			return true;
		}
		return false;
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
