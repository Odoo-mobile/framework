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

import java.util.List;

public class SyncValue {
	String title;
	String authority;
	Boolean isGroup = false;
	List<SyncValue> radioGroups = null;

	public enum Type {
		CHECKBOX, RADIO
	};

	Type type = Type.CHECKBOX;

	public SyncValue(String title) {
		this.title = title;
		this.isGroup = true;
	}

	public SyncValue(String title, String authority, Type type) {
		this.title = title;
		this.authority = authority;
		this.type = type;
	}

	public SyncValue(List<SyncValue> radioGroups) {
		this.radioGroups = radioGroups;
		this.type = Type.RADIO;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

	public Boolean getIsGroup() {
		return isGroup;
	}

	public void setIsGroup(Boolean isGroup) {
		this.isGroup = isGroup;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public List<SyncValue> getRadioGroups() {
		return radioGroups;
	}

	public void setRadioGroups(List<SyncValue> radioGroups) {
		this.radioGroups = radioGroups;
	}

}
