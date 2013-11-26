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
package com.openerp.support.menu;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class OEMenu.
 */
public class OEMenu {

	/** The id. */
	private int id;

	/** The menu title. */
	private String menuTitle;

	/** The menu items. */
	private List<OEMenuItems> menuItems;

	/** The icon. */
	private int icon;

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
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * Sets the id.
	 * 
	 * @param id
	 *            the new id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the menu title.
	 * 
	 * @return the menu title
	 */
	public String getMenuTitle() {
		return this.menuTitle;
	}

	/**
	 * Sets the menu title.
	 * 
	 * @param menuTitle
	 *            the new menu title
	 */
	public void setMenuTitle(String menuTitle) {
		this.menuTitle = menuTitle;
	}

	/**
	 * Gets the menu items.
	 * 
	 * @return the menu items
	 */
	public List<OEMenuItems> getMenuItems() {
		return this.menuItems;
	}

	/**
	 * Sets the menu items.
	 * 
	 * @param menuItems
	 *            the new menu items
	 */
	public void setMenuItems(List<OEMenuItems> menuItems) {
		this.menuItems = menuItems;
	}

}
