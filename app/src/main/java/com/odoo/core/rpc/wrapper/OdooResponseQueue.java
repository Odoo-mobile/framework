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
 * Created on 22/4/15 12:48 PM
 */
package com.odoo.core.rpc.wrapper;

import com.odoo.core.rpc.listeners.IOdooResponse;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class OdooResponseQueue {
    public static final String TAG = OdooResponseQueue.class.getSimpleName();
    private Map<String, Queue<IOdooResponse>> mOdooResponseQueue = new HashMap<>();

    public void add(int id, IOdooResponse callback) {
        if (!mOdooResponseQueue.containsKey("queue_" + id)) {
            Queue<IOdooResponse> response = new LinkedList<>();
            response.add(callback);
            mOdooResponseQueue.put("queue_" + id, response);
        }
    }

    public IOdooResponse get(int id) {
        if (mOdooResponseQueue.containsKey("queue_" + id)) {
            return mOdooResponseQueue.get("queue_" + id).poll();
        }
        return null;
    }

    public void remove(int id) {
        if (mOdooResponseQueue.containsKey("queue_" + id)) {
            mOdooResponseQueue.remove("queue_" + id);
        }
    }

    public boolean contain(int id) {
        return mOdooResponseQueue.containsKey("queue_" + id);
    }
}
