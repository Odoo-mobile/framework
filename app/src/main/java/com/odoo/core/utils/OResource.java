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
 * Created on 18/12/14 11:25 AM
 */
package com.odoo.core.utils;

import android.content.Context;

public class OResource {
    public static String string(Context context, int res_id) {
        return context.getResources().getString(res_id);
    }

    public static Integer dimen(Context context, int res_id) {
        return (int) context.getResources().getDimension(res_id);
    }

    public static int color(Context context, int res_id) {
        return context.getResources().getColor(res_id);
    }
}
