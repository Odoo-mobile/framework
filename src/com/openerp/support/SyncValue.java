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
