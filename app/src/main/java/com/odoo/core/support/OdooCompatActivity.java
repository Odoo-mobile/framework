package com.odoo.core.support;

import android.support.v7.app.AppCompatActivity;

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
 * Created on 26/11/15
 */
public abstract class OdooCompatActivity extends AppCompatActivity {
    public static final String TAG = OdooCompatActivity.class.getSimpleName();
    private DevicePermissionResultListener mDevicePermissionResultListener = null;

    // API23+ Permission model helper methods
    public void setOnDevicePermissionResultListener(DevicePermissionResultListener callback) {
        mDevicePermissionResultListener = callback;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (mDevicePermissionResultListener != null) {
            mDevicePermissionResultListener.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public interface DevicePermissionResultListener {
        void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);
    }

}
