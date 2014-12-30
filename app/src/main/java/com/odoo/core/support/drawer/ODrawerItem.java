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
 * Created on 30/12/14 4:09 PM
 */
package com.odoo.core.support.drawer;

import android.os.Bundle;

import java.io.Serializable;
import java.util.Locale;

public class ODrawerItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private String key = null, title = null;
    private String unique_key = null;
    private Integer counter = 0, icon = 0;
    private Object instance = null;
    private Boolean mGroupTitle = false;
    private Bundle mBundle = null;

    public ODrawerItem(String key) {
        this.key = key;

    }

    public ODrawerItem setTitle(String title) {
        this.title = title;
        unique_key = key.toLowerCase(Locale.getDefault()) + "_" + title.replaceAll(" ", "_")
                .toLowerCase(Locale.getDefault());
        return this;
    }

    public String getTitle() {
        return title;
    }

    public ODrawerItem setIcon(int icon) {
        this.icon = icon;
        return this;
    }

    public Integer getIcon() {
        return icon;
    }

    public ODrawerItem setCounter(int counter) {
        this.counter = counter;
        return this;
    }

    public Integer getCounter() {
        return counter;
    }

    public ODrawerItem setInstance(Object instance) {
        this.instance = instance;
        return this;
    }

    public Object getInstance() {
        return instance;
    }

    public ODrawerItem setGroupTitle() {
        mGroupTitle = true;
        return this;
    }

    public Boolean isGroupTitle() {
        return mGroupTitle;
    }

    public ODrawerItem setExtra(Bundle bundle) {
        mBundle = bundle;
        return this;
    }

    public Bundle getExtra() {
        return mBundle;
    }

    public String getKey() {
        return unique_key;
    }
}
