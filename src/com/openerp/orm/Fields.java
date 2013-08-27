package com.openerp.orm;

public class Fields {

    private String name;
    private String title;
    private Object type;
    private String help = "";
    private boolean canSync = true;
    public Fields(String name, String title, Object type) {
	super();
	this.name = name;
	this.title = title;
	this.type = type;
    }

    public Fields(String name, String title, Object type, boolean canSync) {
	super();
	this.name = name;
	this.title = title;
	this.type = type;
	this.canSync = canSync;
    }

    public Fields(String name, String title, Object type, boolean canSync,
	    String help) {
	super();
	this.name = name;
	this.title = title;
	this.type = type;
	this.help = help;
	this.canSync = canSync;
    }

    public boolean isCanSync() {
	return canSync;
    }

    public void setCanSync(boolean canSync) {
	this.canSync = canSync;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getTitle() {
	return title;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    public Object getType() {
	return type;
    }

    public void setType(String type) {
	this.type = type;
    }

    public String getHelp() {
	return help;
    }

    public void setHelp(String help) {
	this.help = help;
    }

}
