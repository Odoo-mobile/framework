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
 * Created on 18/12/14 4:23 PM
 */
package com.odoo.core.support;

import com.odoo.core.orm.fields.OColumn;

import java.util.ArrayList;
import java.util.List;

public class OdooFields extends odoo.helper.OdooFields {

    public OdooFields(List<OColumn> columns) {
        List<String> fields = new ArrayList<>();
        if (columns != null) {
            for (OColumn column : columns) {
                if (!column.isLocal() && !column.isFunctionalColumn()) {
                    fields.add(column.getName());
                }
            }
        }
        addAll(fields.toArray(new String[fields.size()]));
    }

    public OdooFields(String[] fields) {
        addAll(fields);
    }
}
