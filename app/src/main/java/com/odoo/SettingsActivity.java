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
 * Created on 9/1/15 11:32 AM
 */
package com.odoo;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.odoo.core.account.About;
import com.odoo.core.account.OdooLogin;
import com.odoo.core.support.OUser;
import com.odoo.core.support.sync.SyncUtils;
import com.odoo.core.utils.OAppBarUtils;
import com.odoo.core.utils.OPreferenceManager;
import com.odoo.core.utils.OResource;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    public static final String TAG = SettingsActivity.class.getSimpleName();
    public static final String ACTION_ABOUT = "com.odoo.ACTION_ABOUT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_setting_activity);
        OAppBarUtils.setAppBar(this, true);
        ActionBar actionbar = getSupportActionBar();
        if(actionbar!=null) {
            actionbar.setHomeButtonEnabled(true);
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setTitle(R.string.title_application_settings);
        }
    }

    @Override
    public void startActivity(Intent intent) {
        if (intent.getAction() != null
                && intent.getAction().equals(ACTION_ABOUT)) {
            Intent about = new Intent(this, About.class);
            super.startActivity(about);
            return;
        }
        super.startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        settingUpdated();
    }

    private void settingUpdated() {
        OUser user = OUser.current(this);
        if (user == null) {
            Intent loginActivity = new Intent(this, OdooLogin.class);
            loginActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginActivity);
            finish();
        } else {
            Account mAccount = user.getAccount();
            OPreferenceManager mPref = new OPreferenceManager(this);
            int sync_interval = mPref.getInt("sync_interval", 1440);
            List<String> default_authorities = new ArrayList<>();
            default_authorities.add("com.android.calendar");
            default_authorities.add("com.android.contacts");
            SyncAdapterType[] list = ContentResolver.getSyncAdapterTypes();
            for (SyncAdapterType lst : list) {
                if (lst.authority.contains("com.odoo")
                        && lst.authority.contains("providers")) {
                    default_authorities.add(lst.authority);
                }
            }
            for (String authority : default_authorities) {
                boolean isSyncActive = ContentResolver.getSyncAutomatically(
                        mAccount, authority);
                if (isSyncActive) {
                    SyncUtils.get(this).setSyncPeriodic(authority, sync_interval, 60, 1);
                }
            }
            Toast.makeText(this, OResource.string(this, R.string.toast_setting_saved),
                    Toast.LENGTH_LONG).show();
        }
    }
}
