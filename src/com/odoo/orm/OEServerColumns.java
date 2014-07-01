package com.odoo.orm;

public class OEServerColumns {
	String[] mColumns = null;

	public OEServerColumns(String[] columns) {
		mColumns = columns;
	}

	public String[] getServerColumns() {
		return mColumns;
	}
}
