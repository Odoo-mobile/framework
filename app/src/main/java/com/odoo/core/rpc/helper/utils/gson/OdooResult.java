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
 * Created on 22/4/15 4:04 PM
 */
package com.odoo.core.rpc.helper.utils.gson;

import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class OdooResult extends TreeMap<String, Object> {

    public int getTotalRecords() {
        return has("length") ? getInt("length") : 0;
    }

    public int getSize() {
        if (has("records")) {
            return getArray("records").size();
        }
        return 0;
    }

    public List<OdooRecord> getRecords() {
        List<OdooRecord> records = new ArrayList<>();
        if (has("records")) {
            records = getArray("records");
        }
        return records;
    }

    public String getString(String key) {
        return get(key).toString();
    }

    public Double getDouble(String key) {
        return (Double) get(key);
    }

    public Integer getInt(String key) {
        return getDouble(key).intValue();
    }

    public Boolean getBoolean(String key) {
        return (Boolean) get(key);
    }

    public boolean has(String key) {
        return containsKey(key);
    }

    public OdooResult getMap(String key) {
        LinkedTreeMap mapTree = (LinkedTreeMap) get(key);
        OdooResult result = new OdooResult();
        result.putAll(mapTree);
        return result;
    }

    public <T> List<T> getArray(String key) {
        return (List<T>) get(key);
    }
}
