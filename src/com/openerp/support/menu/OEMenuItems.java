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

import android.graphics.Color;

// TODO: Auto-generated Javadoc
/**
 * The Class OEMenuItems.
 */
public class OEMenuItems {

	/** The icon. */
	private int icon;

	/** The title. */
	private String title;

	/** The fragment instance. */
	private Object fragmentInstance;

	/** The notification count. */
	private int notificationCount;

	/** The is group. */
	private boolean isGroup;

	/** The Menu Tag color */
	private boolean set_menu_tag_color = false;

	private int menu_color = 0;

	/**
	 * Instantiates a new oE menu items.
	 */
	public OEMenuItems() {

	}

	/**
	 * Instantiates a new oE menu items.
	 * 
	 * @param icon
	 *            the icon
	 * @param title
	 *            the title
	 * @param fragmentInstance
	 *            the fragment instance
	 * @param notificationCount
	 *            the notification count
	 * @param isGroup
	 *            the is group
	 */
	public OEMenuItems(int icon, String title, Object fragmentInstance,
			int notificationCount, boolean isGroup) {
		super();
		this.icon = icon;
		this.title = title;
		this.fragmentInstance = fragmentInstance;
		this.notificationCount = notificationCount;
		this.isGroup = isGroup;
	}

	/**
	 * Instantiates a new oE menu items.
	 * 
	 * @param title
	 *            the title
	 * @param fragmentInstance
	 *            the fragment instance
	 * @param notificationCount
	 *            the notification count
	 */
	public OEMenuItems(String title, Object fragmentInstance,
			int notificationCount) {
		super();
		this.icon = 0;
		this.title = title;
		this.fragmentInstance = fragmentInstance;
		this.notificationCount = notificationCount;
		this.isGroup = false;
	}

	/**
	 * Instantiates a new oE menu items.
	 * 
	 * @param title
	 *            the title
	 * @param fragmentInstance
	 *            the fragment instance
	 * @param notificationCount
	 *            the notification count
	 * @param isGroup
	 *            the is group
	 */
	public OEMenuItems(String title, Object fragmentInstance,
			int notificationCount, boolean isGroup) {
		super();
		this.icon = 0;
		this.title = title;
		this.fragmentInstance = fragmentInstance;
		this.notificationCount = notificationCount;
		this.isGroup = isGroup;
	}

	/**
	 * Instantiates a new oE menu items.
	 * 
	 * @param icon
	 *            the icon
	 * @param string
	 *            the string
	 * @param objectOFClass
	 *            the object of class
	 * @param i
	 *            the i
	 */
	public OEMenuItems(int icon, String string, Object objectOFClass, int i) {
		// TODO Auto-generated constructor stub
		this.icon = icon;
		this.title = string;
		this.fragmentInstance = objectOFClass;
		this.notificationCount = i;
	}

	/**
	 * Checks if is group.
	 * 
	 * @return true, if is group
	 */
	public boolean isGroup() {
		return this.isGroup;
	}

	/**
	 * Sets the group.
	 * 
	 * @param isGroup
	 *            the new group
	 */
	public void setGroup(boolean isGroup) {
		this.isGroup = isGroup;
	}

	/**
	 * Gets the icon.
	 * 
	 * @return the icon
	 */
	public int getIcon() {
		return this.icon;
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
	 * Gets the title.
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return this.title;
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
	 * Gets the fragment instance.
	 * 
	 * @return the fragment instance
	 */
	public Object getFragmentInstance() {
		return this.fragmentInstance;
	}

	/**
	 * Sets the fragment instance.
	 * 
	 * @param fragmentInstance
	 *            the new fragment instance
	 */
	public void setFragmentInstance(Object fragmentInstance) {
		this.fragmentInstance = fragmentInstance;
	}

	/**
	 * Gets the notification count.
	 * 
	 * @return the notification count
	 */
	public int getNotificationCount() {
		return this.notificationCount;
	}

	/**
	 * Sets the notification count.
	 * 
	 * @param notificationCount
	 *            the new notification count
	 */
	public void setNotificationCount(int notificationCount) {
		this.notificationCount = notificationCount;
	}

	public void setAutoMenuTagColor(boolean set_tag_color) {
		this.set_menu_tag_color = set_tag_color;
	}

	public boolean hasMenuTagColor() {
		return this.set_menu_tag_color;
	}

	public void setMenuTagColor(int color_code) {
		this.menu_color = color_code;
	}

	public int getMenuTagColor() {
		return this.menu_color;
	}
}
