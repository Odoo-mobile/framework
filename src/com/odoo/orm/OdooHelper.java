package com.odoo.orm;

import java.util.ArrayList;
import java.util.List;

import odoo.OArguments;
import odoo.ODomain;
import odoo.Odoo;
import odoo.OdooAccountExpireException;
import odoo.OdooInstance;
import odoo.OdooVersion;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.odoo.App;
import com.odoo.support.OUser;

public class OdooHelper {

	public static final String TAG = OdooHelper.class.getSimpleName();

	Context mContext = null;
	Boolean mForceConnect = false;
	Odoo mOdoo = null;
	App mApp = null;

	public OdooHelper(Context context) {
		mContext = context;
		mApp = (App) context.getApplicationContext();
	}

	public OdooHelper(Context context, Boolean forceConnect) {
		mContext = context;
		mForceConnect = forceConnect;
		mApp = (App) context.getApplicationContext();
	}

	public OUser login(String username, String password, String database,
			String serverURL) {
		Log.d(TAG, "OHelper->login()");
		OUser userObj = null;
		try {
			mOdoo = new Odoo(serverURL, mForceConnect);
			JSONObject response = mOdoo.authenticate(username, password,
					database);
			int userId = 0;
			if (response.get("uid") instanceof Integer) {
				mApp.setOdooInstance(mOdoo);
				userId = response.getInt("uid");
				ODomain domain = new ODomain();
				domain.add("id", "=", userId);
				userObj = getUserDetail(domain, database, username, password,
						serverURL, userId, mForceConnect, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return userObj;
	}

	public OUser instance_login(OdooInstance instance, String username,
			String password) throws OdooAccountExpireException {
		Log.d(TAG, "OHelper->instance_login()");
		OUser userObj = null;
		try {
			mOdoo = mApp.getOdoo();
			String odooServer = mOdoo.getServerURL();
			String odooDB = mOdoo.getDatabaseName();
			JSONObject response = mOdoo.oauth_authenticate(instance, username,
					password);
			int userId = 0;
			if (response.get("uid") instanceof Integer) {
				mApp.setOdooInstance(mOdoo);
				userId = response.getInt("uid");
				ODomain domain = new ODomain();
				domain.add("id", "=", userId);
				userObj = getUserDetail(domain, odooDB, username, password,
						odooServer, userId, false, instance);
			}
		} catch (OdooAccountExpireException e) {
			throw new OdooAccountExpireException(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return userObj;
	}

	private OUser getUserDetail(ODomain domain, String database,
			String username, String password, String url, int userId,
			boolean mForceConnect, OdooInstance instance)
			throws OdooAccountExpireException {
		OUser userObj = null;
		try {
			OFieldsHelper fields = new OFieldsHelper(new String[] { "name",
					"partner_id", "tz", "image", "company_id" });
			JSONObject res = mOdoo
					.search_read("res.users", fields.get(), domain.get())
					.getJSONArray("records").getJSONObject(0);
			userObj = new OUser();
			userObj.setAvatar(res.getString("image"));
			userObj.setName(res.getString("name"));
			userObj.setDatabase(database);
			userObj.setHost(url);
			userObj.setIsactive(true);
			userObj.setAndroidName(generateOdooName(username,
					(instance != null) ? instance.getDatabaseName() : database));
			userObj.setPartner_id(res.getJSONArray("partner_id").getInt(0));
			userObj.setTimezone(res.getString("tz"));
			userObj.setUser_id(userId);
			userObj.setUsername(username);
			userObj.setPassword(password);
			userObj.setAllowSelfSignedSSL(mForceConnect);
			String company_id = new JSONArray(res.getString("company_id"))
					.getString(0);
			userObj.setCompany_id(company_id);
			userObj.setOAuthLogin(false);
			if (instance != null) {
				userObj.setOAuthLogin(true);
				userObj.setInstanceDatabase(instance.getDatabaseName());
				userObj.setInstanceUrl(instance.getInstanceUrl());
				userObj.setClientId(instance.getClientId());
			}
			OdooVersion odooVersion = mApp.getOdooVersion();
			userObj.setVersion_number(odooVersion.getVersion_number());
			userObj.setVersion_serie(odooVersion.getServer_serie());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return userObj;
	}

	public List<OdooInstance> getUserInstances(OUser user) {
		List<OdooInstance> list = new ArrayList<OdooInstance>();
		mOdoo = mApp.getOdoo();
		try {
			OArguments args = new OArguments();
			for (OdooInstance instance : mOdoo.get_instances(args.get())) {
				if (instance.isSaas()) {
					list.add(instance);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	private String generateOdooName(String username, String database) {
		return username + "[" + database + "]";
	}
}
