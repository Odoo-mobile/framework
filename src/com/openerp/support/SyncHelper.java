package com.openerp.support;

import java.util.HashMap;

import org.json.JSONArray;

import com.openerp.orm.BaseDBHelper;

public interface SyncHelper {

	public HashMap<String, Object> syncWithServer(BaseDBHelper db, JSONArray args);
}
