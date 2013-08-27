package com.openerp.support.menu;

public class OEMenuItems {

	private int icon;
	private String title;
	private Object fragmentInstance;
	private int notificationCount;
	private boolean isGroup;

	public OEMenuItems() {

	}

	public OEMenuItems(int icon, String title, Object fragmentInstance,
			int notificationCount, boolean isGroup) {
		super();
		this.icon = icon;
		this.title = title;
		this.fragmentInstance = fragmentInstance;
		this.notificationCount = notificationCount;
		this.isGroup = isGroup;
	}

	public OEMenuItems(String title, Object fragmentInstance,
			int notificationCount) {
		super();
		this.icon = 0;
		this.title = title;
		this.fragmentInstance = fragmentInstance;
		this.notificationCount = notificationCount;
		this.isGroup = false;
	}

	public OEMenuItems(String title, Object fragmentInstance,
			int notificationCount, boolean isGroup) {
		super();
		this.icon = 0;
		this.title = title;
		this.fragmentInstance = fragmentInstance;
		this.notificationCount = notificationCount;
		this.isGroup = isGroup;
	}

	public OEMenuItems(int icon, String string, Object objectOFClass, int i) {
		// TODO Auto-generated constructor stub
		this.icon = icon;
		this.title = string;
		this.fragmentInstance = objectOFClass;
		this.notificationCount = i;
	}

	public boolean isGroup() {
		return this.isGroup;
	}

	public void setGroup(boolean isGroup) {
		this.isGroup = isGroup;
	}

	public int getIcon() {
		return this.icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Object getFragmentInstance() {
		return this.fragmentInstance;
	}

	public void setFragmentInstance(Object fragmentInstance) {
		this.fragmentInstance = fragmentInstance;
	}

	public int getNotificationCount() {
		return this.notificationCount;
	}

	public void setNotificationCount(int notificationCount) {
		this.notificationCount = notificationCount;
	}

}
