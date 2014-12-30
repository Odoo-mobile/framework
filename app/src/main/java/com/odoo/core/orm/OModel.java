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
 * Created on 30/12/14 3:31 PM
 */
package com.odoo.core.orm;

import android.content.Context;

import com.odoo.core.support.OUser;

public class OModel extends OSQLite {

    public static final String TAG = OModel.class.getSimpleName();

    private Context mContext;
    private String model_name = null;
    private String database_name = null;

    public OModel(Context context, String model_name) {
        super(context, OUser.current(context).getDBName());
        init(context, model_name, OUser.current(context).getDBName());
    }

    public OModel(Context context, String model_name, String db_name) {
        super(context, db_name);
        init(context, model_name, db_name);
    }

    private void init(Context context, String model, String db_name) {
        mContext = context;
        model_name = model;
        database_name = db_name;
    }

    public String getModelName() {
        return model_name;
    }

    public String toString() {
        return getModelName();
    }


}
