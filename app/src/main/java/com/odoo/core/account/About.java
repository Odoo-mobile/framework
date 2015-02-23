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
 * Created on 9/1/15 11:33 AM
 */
package com.odoo.core.account;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.odoo.core.utils.IntentUtils;
import com.odoo.core.utils.OActionBarUtils;
import com.odoo.R;
import com.odoo.datas.OConstants;

public class About extends ActionBarActivity {
    public static final String TAG = About.class.getSimpleName();

    private TextView versionName = null, aboutLine2 = null, aboutLine3 = null,
            aboutLine4 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_about);
        OActionBarUtils.setActionBar(this, true);
        setTitle("");
        versionName = (TextView) findViewById(R.id.txvVersionName);
        try {
            // setting version name from manifest file
            String version = getPackageManager().getPackageInfo(
                    getPackageName(), 0).versionName;
            versionName.setText(getResources()
                    .getString(R.string.label_version) + " " + version);

            // setting link in textview
            aboutLine2 = (TextView) findViewById(R.id.line2);
            if (aboutLine2 != null) {
                aboutLine2.setMovementMethod(LinkMovementMethod.getInstance());
            }
            aboutLine3 = (TextView) findViewById(R.id.line3);
            if (aboutLine3 != null) {
                aboutLine3.setMovementMethod(LinkMovementMethod.getInstance());
            }
            aboutLine4 = (TextView) findViewById(R.id.line4);
            if (aboutLine4 != null) {
                aboutLine4.setMovementMethod(LinkMovementMethod.getInstance());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                finish();
                return true;
            case R.id.menu_about_our_apps:
                IntentUtils.openURLInBrowser(this, OConstants.URL_ODOO_APPS_ON_PLAY_STORE);
                return true;
            case R.id.menu_about_github:
                IntentUtils.openURLInBrowser(this, OConstants.URL_ODOO_MOBILE_GIT_HUB);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
