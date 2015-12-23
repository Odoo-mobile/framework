package com.odoo.core.tools.permissions;

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import com.odoo.core.support.OdooCompatActivity;
import com.odoo.core.support.addons.fragment.BaseFragment;

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
public class DevicePermissionHelper implements OdooCompatActivity.DevicePermissionResultListener {
    public static final String TAG = DevicePermissionHelper.class.getSimpleName();
    public static final int REQUEST_PERMISSION = 7;
    public static final int REQUEST_PERMISSIONS = 8;
    private OdooCompatActivity mActivity;
    private PermissionGrantListener mPermissionGrantListener;

    public DevicePermissionHelper(OdooCompatActivity activity) {
        mActivity = activity;
        mActivity.setOnDevicePermissionResultListener(this);
    }

    public DevicePermissionHelper(BaseFragment fragment) {
        this(fragment.parent());
    }

    public boolean hasPermission(String permission) {
        int permissionCheck = ActivityCompat.checkSelfPermission(mActivity, permission);
        switch (permissionCheck) {
            case PackageManager.PERMISSION_GRANTED:
                return true;
            case PackageManager.PERMISSION_DENIED:
                return false;
        }
        return false;
    }

    public void requestToGrantPermission(PermissionGrantListener callback, String permission) {
        mPermissionGrantListener = callback;
        if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)) {
            if (callback != null) callback.onPermissionRationale();
        } else {
            ActivityCompat.requestPermissions(mActivity, new String[]{permission}, REQUEST_PERMISSION);
        }
    }

    public void requestPermissions(PermissionGrantListener callback, String[] permissions) {
        mPermissionGrantListener = callback;
        ActivityCompat.requestPermissions(mActivity, permissions, REQUEST_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mPermissionGrantListener != null)
                    mPermissionGrantListener.onPermissionGranted();
            } else {
                if (mPermissionGrantListener != null)
                    mPermissionGrantListener.onPermissionDenied();
            }
        }
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mPermissionGrantListener != null)
                    mPermissionGrantListener.onPermissionGranted();
            } else {
                if (mPermissionGrantListener != null)
                    mPermissionGrantListener.onPermissionDenied();
            }
        }
    }

    public interface PermissionGrantListener {
        void onPermissionGranted();

        void onPermissionDenied();

        void onPermissionRationale();
    }

    public interface DevicePermissionImpl {
        String[] permissions();
    }
}
