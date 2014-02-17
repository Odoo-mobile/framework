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
package com.openerp.orm;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import openerp.OEDomain;
import openerp.OEVersionException;
import openerp.OpenERP;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.openerp.base.ir.Ir_model;
import com.openerp.orm.OEFieldsHelper.OERelationData;
import com.openerp.support.OEUser;
import com.openerp.util.logger.OELog;

public class OEHelper extends OpenERP {
	public static final String TAG = "com.openerp.orm.OEHelper";
	Context mContext = null;
	OEDatabase mDatabase = null;
	OEUser mUser = null;

	public OEHelper(SharedPreferences pref) {
		super(pref);
	}

	public OEHelper(Context context, String host)
			throws ClientProtocolException, JSONException, IOException,
			OEVersionException {
		super(host);
		mContext = context;
	}

	public OEHelper(Context context, OEUser data, OEDatabase oeDatabase)
			throws ClientProtocolException, JSONException, IOException,
			OEVersionException {
		super(data.getHost());
		Log.d(TAG, "OEHelper->OEHelper(Context, OEUser, OEDatabase)");
		Log.d(TAG, "Called from OEDatabase->getOEInstance()");
		mContext = context;
		mDatabase = oeDatabase;
		mUser = data;
		/*
		 * Required to login with server.
		 */
		login(mUser.getUsername(), mUser.getPassword(), mUser.getDatabase(),
				mUser.getHost());
	}

	public OEUser login(String username, String password, String database,
			String serverURL) {
		OEUser userObj = null;
		try {
			JSONObject response = this.authenticate(username, password,
					database);
			int userId = 0;
			if (response.get("uid") instanceof Integer) {
				userId = response.getInt("uid");

				OEFieldsHelper fields = new OEFieldsHelper(new String[] {
						"partner_id", "tz", "image", "company_id" });
				OEDomain domain = new OEDomain();
				domain.add("id", "=", userId);
				JSONObject res = search_read("res.users", fields.get(),
						domain.get()).getJSONArray("records").getJSONObject(0);

				userObj = new OEUser();
				userObj.setAvatar(res.getString("image"));

				userObj.setDatabase(database);
				userObj.setHost(serverURL);
				userObj.setIsactive(true);
				userObj.setAndroidName(androidName(username, database));
				userObj.setPartner_id(res.getJSONArray("partner_id").getString(
						0));
				userObj.setTimezone(res.getString("tz"));
				userObj.setUser_id(String.valueOf(userId));
				userObj.setUsername(username);
				userObj.setPassword(password);
				String company_id = new JSONArray(res.getString("company_id"))
						.getString(0);
				userObj.setCompany_id(company_id);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return userObj;
	}

	private String androidName(String username, String database) {
		StringBuffer android_name = new StringBuffer();
		android_name.append(username);
		android_name.append("[");
		android_name.append(database);
		android_name.append("]");
		return android_name.toString();
	}

	public boolean syncWithServer() {
		return syncWithServer(false, null);
	}

	public boolean syncWithServer(boolean twoWay, List<Object> ids) {
		boolean synced = false;
		Log.d(TAG, "OEHelper->syncWithServer()");
		Log.d(TAG, "Model: " + mDatabase.getModelName());
		OEFieldsHelper fields = new OEFieldsHelper(
				mDatabase.getDatabaseColumns());
		try {
			OEDomain domain = new OEDomain();
			if (ids != null) {
				domain.add("id", "in", ids);
			}
			JSONObject result = search_read(mDatabase.getModelName(),
					fields.get(), domain.get(), 0, 30, null, null);
			fields.addAll(result.getJSONArray("records"));
			List<OERelationData> rel_models = fields.getRelationData();
			for (OERelationData rel : rel_models) {
				rel.getDb().getOEInstance().syncWithServer(false, rel.getIds());
			}
			List<Long> result_ids = mDatabase.createORReplace(fields
					.getValues());
			if (result_ids.size() > 0) {
				synced = true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return synced;
	}

	public boolean isModuleInstalled(String model) {
		boolean installed = false;
		Ir_model ir_model = new Ir_model(mContext);
		try {
			OEFieldsHelper fields = new OEFieldsHelper(new String[] { "model" });
			OEDomain domain = new OEDomain();
			domain.add("model", "=", model);
			JSONObject result = search_read(ir_model.getModelName(),
					fields.get(), domain.get());
			if (result.getInt("length") > 0) {
				installed = true;
				JSONObject record = result.getJSONArray("records")
						.getJSONObject(0);
				OEValues values = new OEValues();
				values.put("id", record.getInt("id"));
				values.put("model", record.getString("model"));
				values.put("is_installed", installed);
				int count = ir_model.count("model = ?", new String[] { model });
				if (count > 0)
					ir_model.update(values, "model = ?", new String[] { model });
				else
					ir_model.create(values);
			}

		} catch (Exception e) {
			Log.d(TAG, "OEHelper->isModuleInstalled()");
			Log.e(TAG, e.getMessage() + ". No connection with OpenERP server");
		}
		return installed;
	}
}
