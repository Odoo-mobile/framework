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
 * Created on 15/1/15 5:09 PM
 */
package com.odoo.core.utils;

import android.os.Environment;

import java.io.File;

public class OStorageUtils {
    public static final String TAG = OStorageUtils.class.getSimpleName();

    public static String getDirectoryPath(String file_type) {
        File externalStorage = Environment.getExternalStorageDirectory();
        String path = externalStorage.getAbsolutePath() + "/Odoo";
        File baseDir = new File(path);
        if (!baseDir.isDirectory()) {
            baseDir.mkdir();
        }
        if (file_type == null) {
            file_type = "file";
        }
        if (file_type.contains("image")) {
            path += "/Images";
        } else if (file_type.contains("audio")) {
            path += "/Audio";
        } else {
            path += "/Files";
        }
        File fileDir = new File(path);
        if (!fileDir.isDirectory()) {
            fileDir.mkdir();
        }
        return path;
    }
}
