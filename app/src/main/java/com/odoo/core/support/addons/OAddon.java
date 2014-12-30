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
 * Created on 30/12/14 3:13 PM
 */
package com.odoo.core.support.addons;

import android.util.Log;

public class OAddon {
    public static final String TAG = OAddon.class.getSimpleName();
    private Class<?> addon = null;
    private Boolean isDefault = false;

    public OAddon(Class<?> addon_class) {
        addon = addon_class;
    }

    public OAddon setDefault() {
        isDefault = true;
        return this;
    }

    public Object get() {
        try {
            return addon.newInstance();
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }
        return null;
    }

    public Boolean isDefault() {
        return isDefault;
    }
}
