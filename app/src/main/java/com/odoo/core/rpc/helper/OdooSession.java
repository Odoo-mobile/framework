/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p/>
 * Created on 22/4/15 11:56 AM
 */
package com.odoo.core.rpc.helper;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.odoo.core.rpc.helper.utils.OdooLog;
import com.odoo.core.rpc.helper.utils.gson.OdooResult;

public class OdooSession {
    public static final String TAG = OdooSession.class.getSimpleName();
    private String username, db, sessionId;
    private OdooResult userContext;
    private Integer companyId, uid;

    /* For Odoo 10.0+ */
    private List<OdooUserCurrency> currencies = new ArrayList<>();
    private String warning_level = "user";
    private Boolean is_admin = false;
    private String server_version = "false";
    private String web_base_url = "false";
    private String expiration_reason = "false";
    private String expiration_date = "false";
    private Boolean is_superuser = false;

    public static OdooSession parseSessionInfo(OdooResult result) {
        OdooLog.v("Parsing Session :" + result);
        OdooSession session = new OdooSession();
        if (!result.containsKey("error")) {
            if (result.get("username") != null)
                session.setUsername(result.getString("username"));
            session.setUserContext(result.getMap("user_context"));
            if (result.get("uid") != null && !result.getString("uid").equals("false"))
                session.setUid(result.getInt("uid"));
            if (result.get("company_id") != null && !result.getString("company_id").equals("false"))
                session.setCompanyId(result.getInt("company_id"));
            if (result.get("db") != null)
                session.setDb(result.getString("db"));
            session.setSessionId(result.getString("session_id"));
        }
        return session;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public OdooResult userContext() {
        return userContext;
    }

    public JSONObject getUserContext() {
        Gson gson = new Gson();
        try {
            return new JSONObject(gson.toJson(userContext));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public void setUserContext(OdooResult userContext) {
        this.userContext = userContext;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public List<OdooUserCurrency> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<OdooUserCurrency> currencies) {
        this.currencies = currencies;
    }


    public String getWarning_level() {
        return warning_level;
    }

    public void setWarning_level(String warning_level) {
        this.warning_level = warning_level;
    }

    public Boolean getIs_admin() {
        return is_admin;
    }

    public void setIs_admin(Boolean is_admin) {
        this.is_admin = is_admin;
    }

    public String getServer_version() {
        return server_version;
    }

    public void setServer_version(String server_version) {
        this.server_version = server_version;
    }

    public String getWeb_base_url() {
        return web_base_url;
    }

    public void setWeb_base_url(String web_base_url) {
        this.web_base_url = web_base_url;
    }

    public String getExpiration_reason() {
        return expiration_reason;
    }

    public void setExpiration_reason(String expiration_reason) {
        this.expiration_reason = expiration_reason;
    }

    public String getExpiration_date() {
        return expiration_date;
    }

    public void setExpiration_date(String expiration_date) {
        this.expiration_date = expiration_date;
    }

    public Boolean getIs_superuser() {
        return is_superuser;
    }

    public void setIs_superuser(Boolean is_superuser) {
        this.is_superuser = is_superuser;
    }

    @Override
    public String toString() {
        return "OdooSession{" +
                "username='" + username + '\'' +
                ", db='" + db + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", userContext=" + userContext +
                ", companyId=" + companyId +
                ", uid=" + uid +
                ", currencies=" + currencies +
                ", warning_level='" + warning_level + '\'' +
                ", is_admin=" + is_admin +
                ", server_version='" + server_version + '\'' +
                ", web_base_url='" + web_base_url + '\'' +
                ", expiration_reason='" + expiration_reason + '\'' +
                ", expiration_date='" + expiration_date + '\'' +
                ", is_superuser=" + is_superuser +
                '}';
    }
}
