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
 * The Class Fields.
 */
public class Fields {

	/** The name. */
	private String name;

	/** The title. */
	private String title;

	/** The type. */
	private Object type;

	/** The help. */
	private String help = "";

	/** The can sync. */
	private boolean canSync = true;

	/**
	 * Instantiates a new fields.
	 * 
	 * @param name
	 *            the name
	 * @param title
	 *            the title
	 * @param type
	 *            the type
	 */
	public Fields(String name, String title, Object type) {
		super();
		this.name = name;
		this.title = title;
		this.type = type;
	}

	/**
	 * Instantiates a new fields.
	 * 
	 * @param name
	 *            the name
	 * @param title
	 *            the title
	 * @param type
	 *            the type
	 * @param canSync
	 *            the can sync
	 */
	public Fields(String name, String title, Object type, boolean canSync) {
		super();
		this.name = name;
		this.title = title;
		this.type = type;
		this.canSync = canSync;
	}

	/**
	 * Instantiates a new fields.
	 * 
	 * @param name
	 *            the name
	 * @param title
	 *            the title
	 * @param type
	 *            the type
	 * @param canSync
	 *            the can sync
	 * @param help
	 *            the help
	 */
	public Fields(String name, String title, Object type, boolean canSync,
			String help) {
		super();
		this.name = name;
		this.title = title;
		this.type = type;
		this.help = help;
		this.canSync = canSync;
	}

	/**
	 * Checks if is can sync.
	 * 
	 * @return true, if is can sync
	 */
	public boolean isCanSync() {
		return canSync;
	}

	/**
	 * Sets the can sync.
	 * 
	 * @param canSync
	 *            the new can sync
	 */
	public void setCanSync(boolean canSync) {
		this.canSync = canSync;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the title.
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the title.
	 * 
	 * @param title
	 *            the new title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public Object getType() {
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
	 * Gets the help.
	 * 
	 * @return the help
	 */
	public String getHelp() {
		return help;
	}

	/**
	 * Sets the help.
	 * 
	 * @param help
	 *            the new help
	 */
	public void setHelp(String help) {
		this.help = help;
	}

}
