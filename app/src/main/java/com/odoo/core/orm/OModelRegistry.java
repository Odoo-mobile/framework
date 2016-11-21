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
 * Created on 20/1/15 3:49 PM
 */
package com.odoo.core.orm;

import java.util.HashMap;

public class OModelRegistry {
    public static final String TAG = OModelRegistry.class.getSimpleName();
    private HashMap<String, OModel> modelRegistry = new HashMap<>();

    public void register(OModel model) {
        if (model != null && model.getModelName() != null) {
            modelRegistry.put(getKey(model), model);
        }
    }

    public OModel getModel(String model, String user) {
        if (modelRegistry.containsKey(model)) {
            String key = model + "_" + user;
            return modelRegistry.get(key);
        }
        return null;
    }

    public void unRegister(String model, String user) {
        if (modelRegistry.containsKey(model)) {
            String key = model + "_" + user;
            modelRegistry.remove(key);
        }
    }

    public void clearAll() {
        modelRegistry.clear();
    }

    public int count() {
        return modelRegistry.size();
    }

    private String getKey(OModel model) {
        return model.getModelName() + "_" + model.getUser().getAndroidName();
    }
}
