package com.odoo.config;

import android.Manifest;

import com.odoo.core.tools.permissions.DevicePermissionHelper;

/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p/>
 * Created on 30/11/15
 */
public class PreRequiredPermissions implements DevicePermissionHelper.DevicePermissionImpl {
    public static final String TAG = PreRequiredPermissions.class.getSimpleName();


    /**
     * Provide permission list used by device, This method invoked only in API23+ devices.
     * Used by runtime permission model of Odoo Mobile to identify default required permissions
     * before start using application.
     *
     * If, user not grant any of this permission; application will not start.
     * @return String[] array of required permissions.
     */
    @Override
    public String[] permissions() {
        return new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
    }
}
