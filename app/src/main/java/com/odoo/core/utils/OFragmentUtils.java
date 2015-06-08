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
 * Created on 30/12/14 5:44 PM
 */
package com.odoo.core.utils;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.odoo.R;

public class OFragmentUtils {
    public static final String TAG = OFragmentUtils.class.getSimpleName();

    private AppCompatActivity mActivity;
    private Context mContext;
    private Bundle savedInstance = null;
    private FragmentManager fragmentManager;

    public OFragmentUtils(AppCompatActivity activity, Bundle savedInstance) {
        mActivity = activity;
        mContext = activity;
        fragmentManager = mActivity.getSupportFragmentManager();
    }

    public static OFragmentUtils get(AppCompatActivity activity, Bundle savedInstance) {
        return new OFragmentUtils(activity, savedInstance);
    }

    public void startFragment(Fragment fragment, boolean addToBackState, Bundle extra) {
        Bundle extra_data = fragment.getArguments();
        if (extra_data == null)
            extra_data = new Bundle();
        if (extra != null)
            extra_data.putAll(extra);
        fragment.setArguments(extra_data);
        loadFragment(fragment, addToBackState);
    }

    private void loadFragment(Fragment fragment, Boolean addToBackState) {
        String tag = fragment.getClass().getCanonicalName();
        if (fragmentManager.findFragmentByTag(tag) != null && savedInstance != null) {
            fragmentManager.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        if (savedInstance == null) {
            Log.i(TAG, "Fragment Loaded (" + tag + ")");
            FragmentTransaction tran = fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment, tag);
            if (addToBackState)
                tran.addToBackStack(tag);
            tran.commitAllowingStateLoss();
        }
    }

}
