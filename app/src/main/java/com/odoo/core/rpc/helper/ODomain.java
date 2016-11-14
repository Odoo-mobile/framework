/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p>
 * Created on 21/4/15 4:05 PM
 */
package com.odoo.core.rpc.helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Generate domain for RPC-Call
 */
public class ODomain extends ODomainArgsHelper<ODomain> {
    public static final String TAG = ODomain.class.getSimpleName();

    /**
     * Add Domain with column, operator and it's condition value
     *
     * @param column column name
     * @param operator conditional operator
     * @param value condition value
     * @return self object
     */
    public ODomain add(String column, String operator, Object value) {
        JSONArray domain = new JSONArray();
        domain.put(column);
        domain.put(operator);
        if (value instanceof List) {
            domain.put(listToArray(value));
        } else {
            domain.put(value);
        }
        add(domain);
        return this;
    }

    /**
     * Gets JSONObject with domain key
     *
     * @return JSONObject with domain key
     */
    public JSONObject get() {
        JSONObject result = new JSONObject();
        try {
            result.put("domain", getArray());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
