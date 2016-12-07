package com.odoo.core.rpc.wrapper;

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
 * Created on 21/4/15 4:01 PM
 */

import android.content.Context;
import android.net.Uri;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.odoo.core.rpc.Odoo;
import com.odoo.core.rpc.handler.OdooVersionException;
import com.odoo.core.rpc.helper.OArguments;
import com.odoo.core.rpc.helper.ODomain;
import com.odoo.core.rpc.helper.ORecordValues;
import com.odoo.core.rpc.helper.OdooFields;
import com.odoo.core.rpc.helper.OdooSession;
import com.odoo.core.rpc.helper.OdooUserCurrency;
import com.odoo.core.rpc.helper.OdooVersion;
import com.odoo.core.rpc.helper.utils.OdooLog;
import com.odoo.core.rpc.helper.utils.gson.OdooRecord;
import com.odoo.core.rpc.helper.utils.gson.OdooResponse;
import com.odoo.core.rpc.helper.utils.gson.OdooResult;
import com.odoo.core.rpc.http.OdooSafeClient;
import com.odoo.core.rpc.listeners.IDatabaseListListener;
import com.odoo.core.rpc.listeners.IModuleInstallListener;
import com.odoo.core.rpc.listeners.IOdooConnectionListener;
import com.odoo.core.rpc.listeners.IOdooLoginCallback;
import com.odoo.core.rpc.listeners.IOdooResponse;
import com.odoo.core.rpc.listeners.OdooError;
import com.odoo.core.rpc.listeners.OdooSyncResponse;
import com.odoo.core.support.OUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OdooWrapper<T> implements Response.Listener<JSONObject> {
    public static final String TAG = OdooWrapper.class.getName();

    protected String serverURL;
    protected OdooVersion mVersion = new OdooVersion();
    private RequestQueue requestQueue;
    private OdooResponseQueue responseQueue;
    protected IOdooConnectionListener mIOdooConnectionListener;
    protected OdooSession odooSession = new OdooSession();
    protected OUser user;
    protected Gson gson;
    private Odoo mOdoo;
    private HashMap<String, Object> tempContext = null;

    private Integer new_request_timeout = Odoo.REQUEST_TIMEOUT_MS;
    private Integer new_request_max_retry = Odoo.DEFAULT_MAX_RETRIES;

    public OdooWrapper(Context context, String baseURL) {
        serverURL = stripURL(baseURL);
        gson = new Gson();
        responseQueue = new OdooResponseQueue();
        requestQueue = Volley.newRequestQueue(context,
                new HttpClientStack(OdooSafeClient.getSafeClient(true)));
    }

    @SuppressWarnings("unchecked")
    protected T connect(boolean synchronizedTask) throws OdooVersionException {
        mOdoo = (Odoo) this;
        if (serverURL != null) {
            OdooLog.v("Connecting to " + serverURL);
            if (!synchronizedTask) {
                getVersionInfo(new IOdooResponse() {
                    @Override
                    public void onResponse(OdooResult response) {
                        if (mVersion.getVersionNumber() < 10) {
                            getSessionInfo(new IOdooResponse() {
                                @Override
                                public void onResponse(OdooResult response) {
                                    if (mIOdooConnectionListener != null) {
                                        mIOdooConnectionListener.onConnect(mOdoo);
                                    }
                                }

                                @Override
                                public void onError(OdooError error) {
                                    if (mIOdooConnectionListener != null) {
                                        mIOdooConnectionListener.onError(error);
                                    }
                                }
                            });
                        } else {
                            if (mIOdooConnectionListener != null) {
                                mIOdooConnectionListener.onConnect(mOdoo);
                            }
                        }
                    }

                    @Override
                    public void onError(OdooError error) {
                        if (mIOdooConnectionListener != null) {
                            mIOdooConnectionListener.onError(error);
                        }
                    }
                });
            } else {
                getVersionInfo();
                getSessionInfo();
            }
        }
        return (T) this;
    }

    public T withRetryPolicy(Integer request_timeout, Integer max_retry) {
        new_request_timeout = request_timeout;
        new_request_max_retry = max_retry;
        return (T) this;
    }

    private void newJSONPOSTRequest(final String url, JSONObject params,
                                    IOdooResponse odooResponse, OdooSyncResponse backResponse) {
        OdooLog.d("REQUEST URL : " + url);
        final JSONObject postData = createRequestWrapper(params, odooResponse);
        OdooLog.d("POST DATA: " + postData);
        RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();
        if (backResponse == null) {
            Response.ErrorListener errorListener = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    OdooLog.d("ERROR RESPONSE : " + error.getMessage());
                    String message = error.getMessage();
                    int responseCode = -1;
                    if (error.networkResponse != null) {
                        message = "Server Error :" + error.networkResponse.statusCode;
                        switch (error.networkResponse.statusCode) {
                            case 400:
                                responseCode = Odoo.ErrorCode.OdooServerError.get();
                                break;
                            case 404:
                                responseCode = Odoo.ErrorCode.InvalidURL.get();
                                break;
                            default:
                                responseCode = Odoo.ErrorCode.UnknownError.get();
                        }
                    }
                    OdooError odooError = new OdooError(message, error);
                    odooError.setResponseCode(responseCode);
                    if (error instanceof TimeoutError) {
                        odooError.setMessage("Request Time out");
                        odooError.setServerTrace("Requested too many records. \n\n" +
                                "You can update values before requesting data:\n" +
                                "Odoo.REQUEST_TIMEOUT_MS\n" +
                                "Odoo.DEFAULT_MAX_RETRIES");
                    }
                    try {
                        IOdooResponse response = responseQueue.get(postData.getInt("id"));
                        if (response != null) {
                            response.onError(odooError);
                            responseQueue.remove(postData.getInt("id"));
                        }
                    } catch (JSONException e) {
                        OdooLog.e(e, e.getMessage());
                    }
                }
            };
            JsonObjectRequest request = new JsonObjectRequest(url, postData, OdooWrapper.this, errorListener);
            request.setRetryPolicy(new DefaultRetryPolicy(new_request_timeout, new_request_max_retry,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(request);
        } else {
            JsonObjectRequest request = new JsonObjectRequest(url, postData, requestFuture, requestFuture);
            request.setRetryPolicy(new DefaultRetryPolicy(new_request_timeout, new_request_max_retry,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(request);
            try {
                backResponse.setResponse(parseToResponse(requestFuture.get()));
            } catch (Exception e) {
                OdooLog.e(e);
            }
        }
        new_request_timeout = Odoo.REQUEST_TIMEOUT_MS;
        new_request_max_retry = Odoo.DEFAULT_MAX_RETRIES;
    }

    public void requestController(String fullURL, JSONObject data, IOdooResponse callback) {
        requestController(fullURL, data, callback, null);
    }

    public OdooResult requestController(String fullURL, JSONObject data) {
        OdooSyncResponse response = new OdooSyncResponse();
        requestController(fullURL, data, null, response);
        return validateResult(response);
    }

    private void requestController(String fullURL, JSONObject data, IOdooResponse callback,
                                   OdooSyncResponse backResponse) {
        newJSONPOSTRequest(fullURL, data, callback, backResponse);
    }

    private JSONObject createRequestWrapper(JSONObject params, IOdooResponse callback) {
        JSONObject requestData = new JSONObject();
        try {
            int randomId = getRequestID();
            JSONObject newParams = params;
            if (newParams == null) {
                newParams = new JSONObject();
            }
            if (mVersion != null && mVersion.getVersionNumber() == 7) {
                newParams.put("session_id", odooSession.getSessionId());
            }
            requestData.put("jsonrpc", "2.0");
            requestData.put("method", "call");
            requestData.put("params", newParams);
            requestData.put("id", randomId);
            if (callback != null)
                responseQueue.add(randomId, callback);
        } catch (Exception e) {
            OdooLog.e(e, e.getMessage());
        }
        return requestData;
    }

    private int getRequestID() {
        return Math.abs(new Random().nextInt(9999));
    }

    public OdooResult getVersionInfo() throws OdooVersionException {
        OdooSyncResponse response = new OdooSyncResponse();
        getVersionInfo(null, response);
        OdooResult version = validateResult(response);
        mVersion = OdooVersion.parseVersion(version);
        return version;
    }

    public void getVersionInfo(IOdooResponse res) {
        getVersionInfo(res, null);
    }

    private void getVersionInfo(final IOdooResponse res, OdooSyncResponse backResponse) {
        String url = serverURL + "/web/webclient/version_info";
        newJSONPOSTRequest(url, null, new IOdooResponse() {
            @Override
            public void onResponse(OdooResult response) {
                try {
                    mVersion = OdooVersion.parseVersion(response);
                    res.onResponse(response);
                } catch (OdooVersionException e) {
                    OdooError error = new OdooError(e.getMessage(), null);
                    error.setResponseCode(Odoo.ErrorCode.OdooVersionError.get());
                    res.onError(error);
                }
            }

            @Override
            public void onError(OdooError error) {
                res.onError(error);
            }
        }, backResponse);
    }

    public OdooResult getSessionInfo() {
        OdooSyncResponse response = new OdooSyncResponse();
        getSessionInfo(null, response);
        OdooResult session = validateResult(response);
        if (session != null)
            odooSession = OdooSession.parseSessionInfo(session);
        return session;
    }

    public void getSessionInfo(IOdooResponse callback) {
        getSessionInfo(callback, null);
    }

    private void getSessionInfo(final IOdooResponse callback, OdooSyncResponse backResponse) {
        String url = serverURL + "/web/session/get_session_info";
        newJSONPOSTRequest(url, null, new IOdooResponse() {
            @Override
            public void onResponse(OdooResult response) {
                odooSession = OdooSession.parseSessionInfo(response);
                if (callback != null)
                    callback.onResponse(response);
            }

            @Override
            public void onError(OdooError error) {
                if (callback != null)
                    callback.onError(error);
            }
        }, backResponse);
    }


    public List<String> getDatabaseList() {
        OdooSyncResponse response = new OdooSyncResponse();
        getDatabaseList(null, response);
        return response.get().result.getArray("result");
    }

    public void getDatabaseList(IDatabaseListListener callback) {
        getDatabaseList(callback, null);
    }

    private void getDatabaseList(final IDatabaseListListener callback, OdooSyncResponse backResponse) {

        try {
            if (odooSession.getDb() != null && !odooSession.getDb().equals("false")) {
                // Ignoring if server is odoo.com
                if (callback != null) {
                    callback.onDatabasesLoad(Collections.singletonList(odooSession.getDb()));
                } else {
                    OdooResponse response = new OdooResponse();
                    OdooResult result = new OdooResult();
                    result.put("result", Collections.singletonList(odooSession.getDb()));
                    response.result = result;
                    backResponse.setResponse(response);
                }
            } else {
                Uri uri = Uri.parse(serverURL);
                if (!isRunbotURL(serverURL)
                        && uri.getHost().endsWith(".odoo.com") && mVersion.getVersionNumber() >= 10) {
                    String[] parts = uri.getHost().split("\\.");
                    if (callback != null) {
                        callback.onDatabasesLoad(Collections.singletonList(parts[0]));
                    } else {
                        OdooResponse response = new OdooResponse();
                        OdooResult result = new OdooResult();
                        result.put("result", Collections.singletonList(parts[0]));
                        response.result = result;
                        backResponse.setResponse(response);
                    }
                } else {
                    String url = serverURL;
                    JSONObject params = new JSONObject();
                    // Fix for listing databases. Removed get_list from odoo 9.0+
                    if (mVersion.getVersionNumber() == 9) {
                        url += "/jsonrpc";
                        params.put("method", "list");
                        params.put("service", "db");
                        params.put("args", new JSONArray());
                    } else if (mVersion.getVersionNumber() >= 10) {
                        url += "/web/database/list";
                        params.put("context", new JSONObject());
                    } else {
                        url += "/web/database/get_list";
                        params.put("context", new JSONObject());
                    }
                    newJSONPOSTRequest(url, params, new IOdooResponse() {
                        @Override
                        public void onResponse(OdooResult response) {
                            List<String> dbs = response.getArray("result");
                            List<String> dbNames = new ArrayList<>();
                            dbNames.addAll(dbs);
                            // Fix for runtbot databases. Filtering from host prefix
                            if (isRunbotURL(serverURL)) {
                                String dbPrefix = getDBPrefix(serverURL);
                                dbNames.clear();
                                for (String db : dbs) {
                                    if (db.contains(dbPrefix)) {
                                        dbNames.add(db);
                                    }
                                }
                            }
                            callback.onDatabasesLoad(dbNames);
                        }

                        @Override
                        public void onError(OdooError error) {
                            if (mIOdooConnectionListener != null) {
                                mIOdooConnectionListener.onError(error);
                            }
                        }
                    }, backResponse);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public OUser authenticate(String username, String password, String database) {
        OdooSyncResponse response = new OdooSyncResponse();
        authenticate(username, password, database, null, response);
        OdooResult userResult = response.get().result;
        if (!(userResult.get("uid") instanceof Boolean)) {
            userResult.put("username", username);
            bindOdooSession(userResult);
            generateUserObject(username, password, database, null, response);
            if (response.getObject() != null)
                return (OUser) response.getObject();
        }
        return null;
    }

    public void authenticate(String username, String password, String database, IOdooLoginCallback callback) {
        authenticate(username, password, database, callback, null);
    }


    /**
     * Authenticate user
     *
     * @param username Username
     * @param password Password
     * @param database Database
     * @param callback Callback Response
     */
    private void authenticate(final String username, final String password, final String database,
                              final IOdooLoginCallback callback,
                              final OdooSyncResponse backResponse) {
        try {
            String url = serverURL + "/web/session/authenticate";
            JSONObject params = new JSONObject();
            params.put("db", database);
            params.put("login", username);
            params.put("password", password);
            params.put("context", new JSONObject());
            newJSONPOSTRequest(url, params, new IOdooResponse() {
                @Override
                public void onResponse(OdooResult res) {
                    if (res.get("uid") instanceof Boolean) {
                        OdooError error = new OdooError("Authentication Fail", null);
                        error.setResponseCode(Odoo.ErrorCode.AuthenticationFail.get());
                        callback.onLoginFail(error);
                    } else {
                        if (!(res.get("uid") instanceof Boolean)) {
                            res.put("username", username); // FIX for 10.0+
                            bindOdooSession(res);
                            generateUserObject(username, password, database, callback,
                                    backResponse);
                        }
                    }
                }

                @Override
                public void onError(OdooError error) {
                    callback.onLoginFail(error);
                }
            }, backResponse);
        } catch (Exception e) {
            OdooLog.e(e, e.getMessage());
        }
    }

    private void bindOdooSession(OdooResult response) {
        if (odooSession == null) odooSession = new OdooSession();
        if (mVersion.getVersionNumber() > 7 && response.containsKey("company_id"))
            odooSession.setCompanyId(response.getInt("company_id"));
        odooSession.setDb(response.getString("db"));
        odooSession.setSessionId(response.getString("session_id"));
        odooSession.setUid(response.getInt("uid"));
        odooSession.setUserContext(response.getMap("user_context"));
        odooSession.setUsername(response.getString("username"));

        if (mVersion.getVersionNumber() >= 10) {
            OdooResult currencies = response.getMap("currencies");
            List<OdooUserCurrency> currencyList = new ArrayList<>();
            for (String key : currencies.keySet()) {
                OdooResult map = currencies.getMap(key);
                OdooUserCurrency currency = new OdooUserCurrency();
                List<Double> values = map.getArray("digits");
                currency.id = Integer.parseInt(key);
                currency.digits = new Integer[]{values.get(0).intValue(), values.get(1).intValue()};
                currency.position = map.getString("position");
                currency.symbol = map.getString("symbol");
                currencyList.add(currency);
            }
            odooSession.setCurrencies(currencyList);
            if (mVersion.isEnterprise()) {
                if (response.containsKey("expiration_date"))
                    odooSession.setExpiration_date(response.getString("expiration_date"));
                if (response.containsKey("expiration_reason"))
                    odooSession.setExpiration_reason(response.getString("expiration_reason"));
                if (response.containsKey("warning"))
                    odooSession.setWarning_level(response.getString("warning"));
            }
            odooSession.setIs_admin(response.getBoolean("is_admin"));
            odooSession.setIs_superuser(response.getBoolean("is_superuser"));
            odooSession.setServer_version(response.getString("server_version"));
            odooSession.setWeb_base_url(response.getString("web.base.url"));
        }
    }

    public T withContext(HashMap<String, Object> context) {
        if (context != null) {
            tempContext = new HashMap<>();
            tempContext.putAll(context);
        }
        return (T) this;
    }

    public void nameSearch(String model, String query, ODomain domain, int limit, IOdooResponse callback) {
        nameSearch(model, query, domain, limit, callback, null);
    }

    public OdooResult nameSearch(String model, String query, ODomain domain, int limit) {
        OdooSyncResponse response = new OdooSyncResponse();
        nameSearch(model, query, domain, limit, null, response);
        return validateResult(response);
    }

    private void nameSearch(String model, String query, ODomain domain, int limit, IOdooResponse callback
            , OdooSyncResponse backResponse) {
        try {
            HashMap<String, Object> kwargs = new HashMap<>();
            kwargs.put("name", query);
            kwargs.put("args", (domain != null) ? domain.getAsList() : new ArrayList<>());
            kwargs.put("operator", "ilike");
            kwargs.put("limit", limit);
            callMethod(model, "name_search", new OArguments(), kwargs, null, callback, backResponse);
        } catch (Exception e) {
            OdooLog.e(e, e.getMessage());
        }
    }

    public int searchCount(String model, ODomain domain) {
        OdooSyncResponse response = new OdooSyncResponse();
        searchCount(model, domain, null, response);
        OdooResult result = validateResult(response);
        return result.getInt("result");
    }

    public void searchCount(String model, ODomain domain, IOdooResponse callback) {
        searchCount(model, domain, callback, null);
    }

    private void searchCount(String model, ODomain domain, IOdooResponse callback,
                             OdooSyncResponse backResponse) {
        try {
            JSONObject params = new JSONObject();
            params.put("model", model);
            params.put("method", "search_count");
            OArguments args = new OArguments();
            args.add(domain.getArray());
            callMethod(model, "search_count", args, new HashMap<String, Object>(), new HashMap<String, Object>(),
                    callback, backResponse);
        } catch (Exception e) {
            OdooLog.e(e, e.getMessage());
        }
    }

    public OdooResult searchRead(String model, OdooFields fields, ODomain domain,
                                 int offset, int limit, String sort) {
        OdooSyncResponse response = new OdooSyncResponse();
        searchRead(model, fields, domain, offset, limit, sort, null, response);
        return validateResult(response);
    }

    public void searchRead(String model, OdooFields fields, ODomain domain,
                           int offset, int limit, String sort, final IOdooResponse callback) {
        searchRead(model, fields, domain, offset, limit, sort, callback, null);
    }

    private void searchRead(String model, OdooFields fields, ODomain domain,
                            int offset, int limit, String sort, final IOdooResponse callback,
                            OdooSyncResponse backResponse) {
        try {
            String url = serverURL + "/web/dataset/search_read";
            JSONObject params = new JSONObject();
            params.put("model", model);
            if (fields == null) {
                fields = new OdooFields();
            }
            params.put("fields", fields.get().getJSONArray("fields"));
            if (domain == null) {
                domain = new ODomain();
            }
            params.put("domain", domain.getArray());
            JSONObject context = updateCTX(odooSession.getUserContext());
            params.put("context", context);
            params.put("offset", offset);
            params.put("limit", limit);
            params.put("sort", (sort == null) ? "" : sort);
            newJSONPOSTRequest(url, params, callback, backResponse);
        } catch (Exception e) {
            OdooLog.e(e, e.getMessage());
        }
    }

    public OdooResult executeWorkFlow(String model, int id, String signal) {
        OdooSyncResponse response = new OdooSyncResponse();
        executeWorkFlow(model, id, signal, null, response);
        return validateResult(response);
    }

    public void executeWorkFlow(String model, int id, String signal, IOdooResponse callback) {
        executeWorkFlow(model, id, signal, callback, null);
    }

    private void executeWorkFlow(String model, int id, String signal, IOdooResponse callback,
                                 OdooSyncResponse backResponse) {
        String url = serverURL + "/web/dataset/exec_workflow";
        try {
            JSONObject params = new JSONObject();
            params.put("model", model);
            params.put("id", id);
            params.put("signal", signal);
            newJSONPOSTRequest(url, params, callback, backResponse);
        } catch (Exception e) {
            OdooLog.e(e, e.getMessage());
        }
    }

    private JSONObject updateCTX(JSONObject context) {
        try {
            if (tempContext != null) {
                for (String key : tempContext.keySet()) {
                    context.put(key, tempContext.get(key));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return context;
    }

    public OdooResult read(String model, int id, OdooFields fields) {
        OdooSyncResponse response = new OdooSyncResponse();
        read(model, id, fields, null, response);
        return validateResult(response);
    }

    public void read(String model, int id, OdooFields fields, IOdooResponse callback) {
        read(model, id, fields, callback, null);
    }

    private void read(String model, int id, OdooFields fields, final IOdooResponse callback,
                      OdooSyncResponse backResponse) {
        String url = serverURL + "/web/dataset/call_kw/" + model + "/read";
        try {
            JSONObject params = new JSONObject();
            params.put("model", model);
            params.put("method", "read");
            OArguments args = new OArguments();
            args.add(id);
            if (fields == null) {
                fields = new OdooFields();
            }
            args.add(fields.getArray());
            params.put("args", args.getArray());
            JSONObject kwargs = new JSONObject();
            kwargs.put("context", odooSession.getUserContext());
            params.put("kwargs", kwargs);
            newJSONPOSTRequest(url, params, callback, backResponse);
        } catch (Exception e) {
            OdooLog.e(e, e.getMessage());
        }
    }

    public OdooResult getModelFields(String model) {
        OdooSyncResponse response = new OdooSyncResponse();
        getModelFields(model, null, response);
        return validateResult(response);
    }

    public void getModelFields(String model, IOdooResponse callback) {
        getModelFields(model, callback, null);
    }

    private void getModelFields(String model, IOdooResponse callback, OdooSyncResponse backResponse) {
        OdooFields fields = new OdooFields();
        fields.addAll(new String[]{"name", "field_description", "ttype", "model_id"});
        ODomain domain = new ODomain();
        domain.add("model_id", "=", model);
        searchRead("ir.model.fields", fields, domain, 0, 0, null, callback, backResponse);
    }

    public void callMethod(String model, String method, OArguments arguments,
                           HashMap<String, Object> kwargs, IOdooResponse callback) {
        callMethod(model, method, arguments, kwargs, null, callback, null);
    }

    public OdooResult callMethod(String model, String method, OArguments arguments,
                                 HashMap<String, Object> kwargs) {
        OdooSyncResponse response = new OdooSyncResponse();
        callMethod(model, method, arguments, kwargs, null, null, response);
        return validateResult(response);
    }

    public void callMethod(String model, String method, OArguments arguments,
                           HashMap<String, Object> kwargs, HashMap<String, Object> context,
                           IOdooResponse callback) {
        callMethod(model, method, arguments, kwargs, context, callback, null);
    }

    public OdooResult callMethod(String model, String method, OArguments arguments,
                                 HashMap<String, Object> kwargs, HashMap<String, Object> context) {
        OdooSyncResponse response = new OdooSyncResponse();
        callMethod(model, method, arguments, kwargs, context, null, response);
        return validateResult(response);
    }

    private void callMethod(String model, String method, OArguments arguments,
                            HashMap<String, Object> kwargs, HashMap<String, Object> context,
                            IOdooResponse callback, OdooSyncResponse backResponse) {
        String url = serverURL + "/web/dataset/call_kw";
        try {
            JSONObject params = new JSONObject();
            params.put("model", model);
            params.put("method", method);
            params.put("args", arguments.getArray());
            params.put("kwargs", (kwargs != null)
                    ? new JSONObject(gson.toJson(kwargs)) : new JSONObject());
            params.put("context", (context != null) ?
                    new JSONObject(gson.toJson(updateContext(context)))
                    : odooSession.getUserContext());
            newJSONPOSTRequest(url, params, callback, backResponse);
        } catch (Exception e) {
            OdooLog.e(e, e.getMessage());
        }
    }

    public HashMap<String, Object> updateContext(HashMap<String, Object> newContext) {
        newContext.putAll(odooSession.userContext());
        return newContext;
    }

    public OdooResult permRead(String model, List<Integer> ids) {
        OdooSyncResponse response = new OdooSyncResponse();
        permRead(model, ids, null, response);
        return validateResult(response);
    }

    public void permRead(String model, List<Integer> ids, IOdooResponse callback) {
        permRead(model, ids, callback, null);
    }

    private void permRead(String model, List<Integer> ids, IOdooResponse callback, OdooSyncResponse backResponse) {
        try {
            OArguments args = new OArguments();
            args.add(new JSONArray(ids.toString()));
            callMethod(model, "perm_read", args, null, null, callback, backResponse);
        } catch (JSONException e) {
            OdooLog.e(e, e.getMessage());
        }
    }

    public OdooResult createRecord(String model, ORecordValues values) {
        OdooSyncResponse response = new OdooSyncResponse();
        createRecord(model, values, null, response);
        return validateResult(response);
    }

    public void createRecord(String model, ORecordValues values, IOdooResponse callback) {
        createRecord(model, values, callback, null);
    }

    private void createRecord(String model, ORecordValues values, IOdooResponse callback, OdooSyncResponse backResponse) {
        try {
            OArguments args = new OArguments();
            args.add(new JSONObject(gson.toJson(values)));
            HashMap<String, Object> map = new HashMap<>();
            map.put("context", gson.fromJson(odooSession.getUserContext() + "",
                    HashMap.class));
            callMethod(model, "create", args, map, null, callback, backResponse);
        } catch (Exception e) {
            OdooLog.e(e, e.getMessage());
        }
    }


    public OdooResult updateRecord(String model, ORecordValues values, int id) {
        OdooSyncResponse response = new OdooSyncResponse();
        List<Integer> ids = new ArrayList<>();
        ids.add(id);
        updateRecord(model, values, ids, null, response);
        return validateResult(response);
    }

    public OdooResult updateRecord(String model, ORecordValues values, List<Integer> ids) {
        OdooSyncResponse response = new OdooSyncResponse();
        updateRecord(model, values, ids, null, response);
        return validateResult(response);
    }

    public void updateRecord(String model, ORecordValues values, int id, IOdooResponse callback) {
        List<Integer> ids = new ArrayList<>();
        ids.add(id);
        updateRecord(model, values, ids, callback, null);
    }

    public void updateRecord(String model, ORecordValues values, List<Integer> ids, IOdooResponse callback) {
        updateRecord(model, values, ids, callback, null);
    }

    private void updateRecord(String model, ORecordValues values, List<Integer> ids, IOdooResponse callback,
                              OdooSyncResponse backResponse) {
        try {
            OArguments args = new OArguments();
            args.add(new JSONArray(ids.toString()));
            args.add(new JSONObject(gson.toJson(values)));
            HashMap<String, Object> map = new HashMap<>();
            map.put("context", gson.fromJson(odooSession.getUserContext() + "",
                    HashMap.class));
            callMethod(model, "write", args, map, null, callback, backResponse);
        } catch (Exception e) {
            OdooLog.e(e, e.getMessage());
        }
    }


    public OdooResult unlinkRecord(String model, int id) {
        List<Integer> ids = new ArrayList<>();
        ids.add(id);
        OdooSyncResponse response = new OdooSyncResponse();
        unlinkRecord(model, ids, null, response);
        return validateResult(response);
    }

    public OdooResult unlinkRecord(String model, List<Integer> ids) {
        OdooSyncResponse response = new OdooSyncResponse();
        unlinkRecord(model, ids, null, response);
        return validateResult(response);
    }

    public void unlinkRecord(String model, int id, IOdooResponse callback) {
        List<Integer> ids = new ArrayList<>();
        ids.add(id);
        unlinkRecord(model, ids, callback, null);
    }

    public void unlinkRecord(String model, List<Integer> ids, IOdooResponse callback) {
        unlinkRecord(model, ids, callback, null);
    }

    private void unlinkRecord(String model, List<Integer> ids, IOdooResponse callback,
                              OdooSyncResponse backResponse) {
        try {
            OArguments args = new OArguments();
            args.add(new JSONArray(ids.toString()));
            HashMap<String, Object> map = new HashMap<>();
            map.put("context", gson.fromJson(odooSession.getUserContext() + "",
                    HashMap.class));
            callMethod(model, "unlink", args, map, null, callback, backResponse);
        } catch (Exception e) {
            OdooLog.e(e, e.getMessage());
        }
    }

    public boolean installedOnServer(String moduleName) {
        OdooSyncResponse response = new OdooSyncResponse();
        installedOnServer(moduleName, null, response);
        OdooResult results = validateResult(response);
        return results != null && results.getRecords().size() > 0
                && results.getRecords().get(0).getString("state").equals("installed");
    }

    public void installedOnServer(String moduleName, IModuleInstallListener callback) {
        installedOnServer(moduleName, callback, null);
    }

    private void installedOnServer(String moduleName, final IModuleInstallListener callback,
                                   OdooSyncResponse backResponse) {
        OdooFields fields = new OdooFields();
        fields.addAll(new String[]{"state", "name"});
        ODomain domain = new ODomain();
        domain.add("name", "=", moduleName);
        searchRead("ir.module.module", fields, domain, 0, 0, null, new IOdooResponse() {
            @Override
            public void onResponse(OdooResult response) {
                if (response.getRecords().size() > 0) {
                    OdooRecord rec = response.getRecords().get(0);
                    callback.installedOnServer(rec.getString("state").equals("installed"));
                }
            }

            @Override
            public void onError(OdooError error) {
                OdooLog.e(error);
                callback.installedOnServer(false);
            }
        }, backResponse);
    }

    private void generateUserObject(String username, String password,
                                    String db, final IOdooLoginCallback callback,
                                    final OdooSyncResponse backResponse) {
        final OUser[] users = new OUser[1];
        users[0] = new OUser();
        users[0].setUsername(username);
        users[0].setPassword(password);
        users[0].setDatabase(db);
        users[0].setOdooVersion(mVersion);
        users[0].setUserId(odooSession.getUid());
        users[0].setCompanyId(odooSession.getCompanyId());
        users[0].setHost(serverURL);
        OdooFields fields = new OdooFields();
        fields.addAll(new String[]{"name", "partner_id", "tz", "image_medium", "company_id"});
        ODomain domain = new ODomain();
        domain.add("id", "=", users[0].getUserId());

        if (backResponse != null) {
            OdooResult result = read("res.users", users[0].getUserId(), fields);
            users[0] = parseUserObject(users[0], result);
            backResponse.setObject(users[0]);
        } else {
            read("res.users", users[0].getUserId(), fields, new IOdooResponse() {
                @Override
                public void onResponse(OdooResult response) {
                    users[0] = parseUserObject(users[0], response);
                    callback.onLoginSuccess(mOdoo, users[0]);
                }

                @Override
                public void onError(OdooError error) {
                    callback.onLoginFail(error);
                }
            });
        }
    }

    private OUser parseUserObject(OUser user, OdooResult result) {
        // Odoo 10.0+ returns array of object in read method
        if (result.containsKey("result") && result.get("result") instanceof ArrayList) {
            List<LinkedTreeMap> items = (List<LinkedTreeMap>) result.get("result");
            result = new OdooResult();
            result.putAll(items.get(0));
        }
        user.setName(result.getString("name"));
        user.setAvatar(result.getString("image_medium"));
        user.setTimezone(result.getString("tz"));
        Double partner_id = (Double) result.getArray("partner_id").get(0);
        Double company_id = (Double) result.getArray("company_id").get(0);
        user.setPartnerId(partner_id.intValue());
        user.setCompanyId(company_id.intValue());
        if (mVersion.getVersionNumber() == 7) {
            //FIX: Odoo 7 Not returning company id with user login details
            odooSession.setCompanyId(company_id.intValue());
        }
        return user;
    }

    private String stripURL(String url) {
        if (url != null) {
            String newUrl;
            if (url.endsWith("/")) {
                newUrl = url.substring(0, url.lastIndexOf("/"));
            } else {
                newUrl = url;
            }
            return newUrl;
        }
        return null;
    }

    /**
     * Handling response queue callbacks
     *
     * @param response request response
     */
    @Override
    public void onResponse(JSONObject response) {
        OdooLog.d("RESPONSE:" + response);
        OdooResponse responseMap = parseToResponse(response);
        if (responseMap != null) {
            int id = responseMap.id;
            IOdooResponse odooResponse = responseQueue.get(id);
            if (odooResponse != null) {
                if (responseMap.error != null) {
                    OdooError error = OdooError.parse(responseMap.error);
                    error.setResponseCode(Odoo.ErrorCode.OdooServerError.get());
                    odooResponse.onError(error);
                } else {
                    odooResponse.onResponse(responseMap.result);
                }
                responseQueue.remove(id);
            }
        }
    }

    private OdooResult validateResult(OdooSyncResponse response) {
        OdooLog.d("SYNC_RESPONSE:" + response);
        if (response.get() == null) {
            return null;
        } else {
            if (response.get().result != null) {
                return response.get().result;
            } else {
                OdooResult error = new OdooResult();
                error.put("error", response.get().error);
                return error;
            }
        }
    }

    private OdooResponse parseToResponse(JSONObject response) {
        try {
            // Fixed for direct array in result
            // It will add one more key to result:(result:[] become result:{"result": []});
            if (response.has("result")) {
                if (!(response.get("result") instanceof JSONObject)) {
                    JSONObject obj = new JSONObject();
                    obj.put("result", response.get("result"));
                    response.put("result", obj);
                }
            } else if (!response.has("error")) {
                JSONObject result = response;
                response = new JSONObject();
                response.put("result", result);
            }
        } catch (Exception e) {
            OdooLog.e(e, e.getMessage());
        }
        return gson.fromJson(response.toString(), OdooResponse.class);
    }

    private String getDBPrefix(String host) {
        Pattern pattern = Pattern.compile(".runbot[1-9]{1,2}..odoo.com?(.+?)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(host);
        return matcher.replaceAll("").replaceAll("http://", "").replaceAll("https://", "");
    }

    private boolean isRunbotURL(String host) {
        Pattern pattern = Pattern.compile(".runbot[1-9]{1,2}..odoo.com?(.+?)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(host);
        return matcher.find();
    }
}
