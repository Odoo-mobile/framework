package com.openerp.support.listview;

import java.util.HashMap;

public class OEListViewRows {
	private int row_id;
	private HashMap<String, Object> row_data;

	public OEListViewRows(int row_id, HashMap<String, Object> row_data) {
		super();
		this.row_id = row_id;
		this.row_data = row_data;
	}

	public int getRow_id() {
		return row_id;
	}

	public void setRow_id(int row_id) {
		this.row_id = row_id;
	}

	public HashMap<String, Object> getRow_data() {
		return row_data;
	}

	public void setRow_data(HashMap<String, Object> row_data) {
		this.row_data = row_data;
	}

}
