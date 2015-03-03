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
 * Created on 15/1/15 12:55 PM
 */
package com.odoo.core.utils;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;

import com.odoo.R;

import java.util.Locale;

public class OStringColorUtil {
    public static final String TAG = OStringColorUtil.class.getSimpleName();

    public static int getStringColor(Context context, String content) {
        Resources res = context.getResources();
        TypedArray mColors = res.obtainTypedArray(R.array.letter_tile_colors);
        int MAX_COLORS = mColors.length();
        int firstCharAsc = content.toUpperCase(Locale.getDefault()).charAt(0);
        int index = (firstCharAsc % MAX_COLORS);
        if (index > MAX_COLORS - 1) {
            index = index / 2;
        }
        int color = mColors.getColor(index, Color.WHITE);
        mColors.recycle();
        return color;
    }
}
