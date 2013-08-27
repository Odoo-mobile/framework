package com.openerp.support.menu;

import java.util.List;

import com.openerp.support.menu.OEMenuItems;

public class OEMenu {

	private int id;
	private String menuTitle;
	private List<OEMenuItems> menuItems;
	private int icon;

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMenuTitle() {
		return this.menuTitle;
	}

	public void setMenuTitle(String menuTitle) {
		this.menuTitle = menuTitle;
	}

	public List<OEMenuItems> getMenuItems() {
		return this.menuItems;
	}

	public void setMenuItems(List<OEMenuItems> menuItems) {
		this.menuItems = menuItems;
	}

}
