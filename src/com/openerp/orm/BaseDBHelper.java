package com.openerp.orm;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class BaseDBHelper extends ORM {

    public ArrayList<Fields> columns = null;
    public String name = "";

    public BaseDBHelper(Context context) {
	super(context);
	// TODO Auto-generated constructor stub
	this.columns = new ArrayList<Fields>();
	this.columns.add(new Fields("id", "Id", Types.integer()));
	this.columns
		.add(new Fields("oea_name", "OpenERP User", Types.varchar(100),
			false,
			"OpenERP Account manager name used for login and filter multiple accounts."));
    }

    public ArrayList<Fields> getColumns() {
	return this.columns;
    }

    public ArrayList<Fields> getServerColumns() {
	ArrayList<Fields> serverCols = new ArrayList<Fields>();
	for (Fields fields : this.columns) {
	    if (fields.isCanSync()) {
		serverCols.add(fields);
	    }
	}
	return serverCols;
    }

    public HashMap<String, Object> getMany2ManyColumns() {
	HashMap<String, Object> list = new HashMap<String, Object>();
	for (Fields field : this.columns) {
	    if (field.getType() instanceof Many2Many) {
		list.put(field.getName(), field.getType());
	    }
	}
	return list;
    }

    public HashMap<String, Object> getMany2OneColumns() {
	HashMap<String, Object> list = new HashMap<String, Object>();
	for (Fields field : this.columns) {
	    if (field.getType() instanceof Many2One) {
		list.put(field.getName(), field.getType());
	    }
	}
	return list;
    }

    public String getModelName() {
	return this.name;
    }

    public String getTableName() {
	return this.name.replaceAll("\\.", "_");
    }

}
