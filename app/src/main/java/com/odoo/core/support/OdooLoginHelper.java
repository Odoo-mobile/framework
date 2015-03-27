/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
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
 * Created on 18/12/14 3:15 PM
 */
package com.odoo.core.support;

import android.content.Context;

import com.odoo.App;
import com.odoo.datas.OConstants;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import odoo.OArguments;
import odoo.ODomain;
import odoo.Odoo;
import odoo.OdooAccountExpireException;
import odoo.OdooInstance;
import odoo.OdooVersion;

public class OdooLoginHelper {
    private App mApp;
    private Odoo mOdoo;
    private Context mContext;

    public OdooLoginHelper(Context context) {
        mContext = context;
        mApp = (App) mContext.getApplicationContext();
    }

    public OUser login(String username, String password, String database, String serverURL, Boolean forceConnect) {
        try {
            mOdoo = new Odoo(mContext, serverURL, forceConnect);
            JSONObject res = mOdoo.authenticate(username, password, database);
            int user_id;
            if (res.get("uid") instanceof Integer) {
                user_id = res.getInt("uid");
                OUser user = new OUser();
                user.setOAuthLogin(false);
                user.setPassword(password);
                user.setUsername(username);
                user.setAllowSelfSignedSSL(forceConnect);
                user.setHost(serverURL);
                user.setUser_id(user_id);
                user.setDatabase(database);

                ODomain domain = new ODomain();
                domain.add("id", "=", user_id);
                user = getUserDetails(domain, user, null);
                mApp.setOdoo(mOdoo, user);
                return user;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public OUser instanceLogin(OdooInstance instance, OUser uData) throws OdooAccountExpireException {
        try {
            JSONObject res = mOdoo.oauth_authenticate(instance, uData.getUsername(), uData.getPassword());
            int user_id;
            if (res.get("uid") instanceof Integer) {
                user_id = res.getInt("uid");
                ODomain domain = new ODomain();
                domain.add("id", "=", user_id);
                uData = getUserDetails(domain, uData, instance);
                mApp.setOdoo(mOdoo, uData);
                return uData;
            }
        } catch (OdooAccountExpireException e) {
            throw new OdooAccountExpireException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private OUser getUserDetails(ODomain domain, OUser data, OdooInstance instance) {
        OUser user = data;
        try {
            OdooFields fields = new OdooFields(new String[]{
                    "name", "partner_id", "tz", "image_medium", "company_id"
            });
            JSONObject res = mOdoo.search_read("res.users", fields.get(), domain.get());
            JSONObject userData = res.getJSONArray("records").getJSONObject(0);
            String database = user.getDatabase();
            if (instance != null) {
                user.setOAuthLogin(true);
                database = instance.getDatabaseName();
                user.setInstanceDatabase(instance.getDatabaseName());
                user.setInstanceUrl(instance.getInstanceUrl());
                user.setClientId(instance.getClientId());
            }
            user.setUser_id(userData.getInt("id"));
            user.setName(userData.getString("name"));
            user.setAvatar(userData.getString("image_medium"));
            user.setIsactive(true);
            user.setAndroidName(androidName(user.getUsername(), database));
            user.setPartner_id(userData.getJSONArray("partner_id").getInt(0));
            user.setTimezone(userData.getString("tz"));
            user.setCompany_id(userData.getJSONArray("company_id").getInt(0) + "");

            OdooVersion version = mOdoo.getOdooVersion();
            user.setVersion_number(version.getVersion_number());
            user.setVersion_serie(version.getServer_serie());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public String androidName(String username, String database) {
        return username + "[" + database + "]";
    }

    public List<OdooInstance> getOdooInstances(OUser user) {
        List<OdooInstance> instances = new ArrayList<OdooInstance>();
        //Default Instance (www.odoo.com)
        OdooInstance oInstance = new OdooInstance();
        oInstance.setCompanyName(OConstants.ODOO_COMPANY_NAME);
        oInstance.setInstanceUrl(OConstants.URL_ODOO);
        instances.add(oInstance);

        //Getting user instances
        try {
            for (OdooInstance instance : mOdoo.get_instances(new OArguments().get())) {
                if (instance.isSaas()) {
                    instances.add(instance);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instances;
    }
}
