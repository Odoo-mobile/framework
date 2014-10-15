package com.odoo.orm;

import java.util.ArrayList;
import java.util.List;

import odoo.ODomain;
import odoo.Odoo;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.odoo.App;
import com.odoo.util.JSONUtils;

public class ServerDataHelper {
	private Context mContext;
	private OModel mModel;
	private App mApp;
	private Odoo mOdoo;

	public ServerDataHelper(Context context, OModel model, Odoo odoo) {
		mContext = context;
		mModel = model;
		mApp = (App) mContext.getApplicationContext();
		mOdoo = odoo;
	}

	public List<ODataRow> searchRecords(OFieldsHelper fields, ODomain domain,
			int limit) {
		List<ODataRow> items = new ArrayList<ODataRow>();
		try {
			if (mApp.inNetwork()) {
				JSONObject result = mOdoo.search_read(mModel.getModelName(),
						fields.get(), domain.get(), 0, limit, null, null);
				JSONArray records = result.getJSONArray("records");
				if (records.length() > 0) {
					for (int i = 0; i < records.length(); i++) {
						items.add(JSONUtils.toDataRow(records.getJSONObject(i)));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return items;
	}

	public void quickCreateLocalRecord(ODataRow mRecord) {
		mModel.getSyncHelper().quickStoreRecords(mRecord);
	}
}
