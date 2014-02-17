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
package com.openerp.support;


/**
 * The Class Module. Handles Property for module information
 */
public class Module {

	/** The key id. */
	private String keyId;

	/** The module name. */
	private String moduleName;

	/** The module instance. */
	private Object moduleInstance;

	/** The icon. */
	private int icon;

	/** The load default. */
	private boolean loadDefault = false;

	/**
	 * Instantiates a new module.
	 * 
	 * @param keyId
	 *            the key id
	 * @param moduleName
	 *            the module name
	 * @param moduleInstance
	 *            the module instance
	 */
	public Module(String keyId, String moduleName, Object moduleInstance) {
		super();
		this.keyId = keyId;
		this.moduleName = moduleName;
		this.moduleInstance = moduleInstance;
		this.icon = 0;
	}

	/**
	 * Instantiates a new module.
	 * 
	 * @param keyId
	 *            the key id
	 * @param moduleName
	 *            the module name
	 * @param moduleInstance
	 *            the module instance
	 * @param icon
	 *            the icon
	 */
	public Module(String keyId, String moduleName, Object moduleInstance,
			int icon) {
		super();
		this.keyId = keyId;
		this.moduleName = moduleName;
		this.moduleInstance = moduleInstance;
		this.icon = icon;
	}

	/**
	 * Gets the key id.
	 * 
	 * @return the key id
	 */
	public String getKeyId() {
		return keyId;
	}

	/**
	 * Sets the key id.
	 * 
	 * @param keyId
	 *            the new key id
	 */
	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}

	/**
	 * Gets the module name.
	 * 
	 * @return the module name
	 */
	public String getModuleName() {
		return moduleName;
	}

	/**
	 * Sets the module name.
	 * 
	 * @param moduleName
	 *            the new module name
	 */
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	/**
	 * Gets the module instance.
	 * 
	 * @return the module instance
	 */
	public Object getModuleInstance() {
		return moduleInstance;
	}

	/**
	 * Sets the module instance.
	 * 
	 * @param moduleInstance
	 *            the new module instance
	 */
	public void setModuleInstance(Object moduleInstance) {
		this.moduleInstance = moduleInstance;
	}

	/**
	 * Gets the icon.
	 * 
	 * @return the icon
	 */
	public int getIcon() {
		return icon;
	}

	/**
	 * Sets the icon.
	 * 
	 * @param icon
	 *            the new icon
	 */
	public void setIcon(int icon) {
		this.icon = icon;
	}

	/**
	 * Checks if is load default.
	 * 
	 * @return true, if is load default
	 */
	public boolean isLoadDefault() {
		return loadDefault;
	}

	/**
	 * Sets the load default.
	 * 
	 * @param loadDefault
	 *            the new load default
	 */
	public void setLoadDefault(boolean loadDefault) {
		this.loadDefault = loadDefault;
	}
}
