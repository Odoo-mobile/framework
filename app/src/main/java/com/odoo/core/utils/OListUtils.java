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
 * Created on 7/1/15 12:43 PM
 */
package com.odoo.core.utils;

import java.util.ArrayList;
import java.util.List;

public class OListUtils {
    public static final String TAG = OListUtils.class.getSimpleName();


    public static List<Integer> doubleToIntList(List<Double> list) {
        List<Integer> vals = new ArrayList<>();
        for (Double val : list) {
            vals.add(val.intValue());
        }
        return vals;
    }

    public static List<String> toStringList(List<Integer> list) {
        List<String> items = new ArrayList<>();
        for (Integer item : list) {
            items.add(item + "");
        }
        return items;
    }
}
