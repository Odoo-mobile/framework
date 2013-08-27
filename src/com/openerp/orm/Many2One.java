package com.openerp.orm;

public class Many2One {
    private String model_name = null;
    private BaseDBHelper m2oObject = null;

    public Many2One(String model_name) {
	super();
	this.model_name = model_name;
    }

    public String getModelName() {
	return model_name;
    }

    public Many2One(BaseDBHelper m2oObject) {
	super();
	this.m2oObject = m2oObject;
    }

    public boolean isM2OObject() {
	if (this.m2oObject != null) {
	    return true;
	}
	return false;
    }

    public BaseDBHelper getM2OObject() {
	return m2oObject;
    }

    public boolean isModleName() {
	if (this.model_name != null) {
	    return true;
	}
	return false;
    }
}
