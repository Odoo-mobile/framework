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
 * Created on 21/4/15 4:37 PM
 */
package com.odoo.core.rpc.helper;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class ODomainArgsHelper<T> {
    public static final String TAG = ODomainArgsHelper.class.getSimpleName();
    protected List<Object> mObjects = new ArrayList<>();

    /**
     * Add domain condition, array list
     *
     * @param data
     * @return self object
     */
    public T add(Object data) {
        mObjects.add(data);
        return (T) this;
    }


    /**
     * Append another object values to current object value
     *
     * @param domain
     * @return updated domain
     */
    public T append(ODomain domain) {
        if (domain != null) {
            for (Object obj : domain.getObject()) {
                add(obj);
            }
        }
        return (T) this;
    }

    /**
     * Get domain as list object
     *
     * @return list of objects
     */
    public List<Object> getObject() {
        return mObjects;
    }

    /**
     * Gets domain objects as array
     *
     * @return JSON Array of objects
     */
    public JSONArray getArray() {
        JSONArray result = new JSONArray();
        for (Object obj : mObjects) {
            result.put(obj);
        }
        return result;
    }

    public List<Object> getAsList() {
        return mObjects;
    }

    public JSONArray listToArray(Object collection) {
        JSONArray array = new JSONArray();
        List<Object> list = (List<Object>) collection;
        try {
            for (Object data : list) {
                array.put(data);
            }
        } catch (Exception e) {

        }
        return array;
    }
}
