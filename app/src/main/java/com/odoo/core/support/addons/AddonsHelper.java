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
 * Created on 30/12/14 3:12 PM
 */
package com.odoo.core.support.addons;

import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddonsHelper {
    public static final String TAG = AddonsHelper.class.getSimpleName();
    private List<OAddon> addons = new ArrayList<OAddon>();
    private OAddon defaultAddon = null;


    public List<OAddon> getAddons() {
        if (addons.size() <= 0) {
            prepareAddons();
        }
        Collections.sort(addons);
        return addons;
    }

    private void prepareAddons() {
        addons.clear();
        for (Field addon : getClass().getDeclaredFields()) {
            if (addon.getType().isAssignableFrom(OAddon.class)) {
                addon.setAccessible(true);
                try {
                    OAddon mAddon = (OAddon) addon.get(this);
                    if (mAddon.isDefault()) {
                        defaultAddon = mAddon;
                        addons.add(0, defaultAddon);
                    } else {
                        addons.add(mAddon);
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }

    public OAddon getDefaultAddon() {
        prepareAddons();
        return defaultAddon;
    }

}
