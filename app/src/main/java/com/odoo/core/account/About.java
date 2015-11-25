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
 * Created on 9/1/15 11:33 AM
 */
package com.odoo.core.account;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.odoo.R;
import com.odoo.base.addons.ir.IrModel;
import com.odoo.core.utils.IntentUtils;
import com.odoo.core.utils.OAppBarUtils;
import com.odoo.core.utils.OPreferenceManager;
import com.odoo.datas.OConstants;

public class About extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = About.class.getSimpleName();
    public final static String DEVELOPER_MODE = "developer_mode";
    private Handler handler = null;
    private int click_count = 0;
    private Runnable runnable = null;
    private OPreferenceManager pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_about);
        OAppBarUtils.setAppBar(this, true);
        pref = new OPreferenceManager(this);
        setTitle("");
        findViewById(R.id.abtus_header).setOnClickListener(this);
        TextView versionName, versionCode, aboutLine2, aboutLine3, aboutLine4;
        versionName = (TextView) findViewById(R.id.txvVersionName);
        versionCode = (TextView) findViewById(R.id.txvVersionCode);
        handler = new Handler();
        try {
            PackageManager packageManager = getPackageManager();
            // setting version name from manifest file
            String version = packageManager.getPackageInfo(
                    getPackageName(), 0).versionName;
            String versionCodeName = packageManager.getPackageInfo(
                    getPackageName(), 0).versionCode + "";
            versionName.setText(getResources()
                    .getString(R.string.label_version) + " " + version);
            versionCode.setText(getResources()
                    .getString(R.string.label_version_build) + " " + versionCodeName);

            // setting link in textView
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
        OPreferenceManager pref = new OPreferenceManager(this);
        if (pref.getBoolean(DEVELOPER_MODE, false)) {
            menu.findItem(R.id.menu_developer_mode).setVisible(true);
        }
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
            case R.id.menu_export_db:
                IrModel model = new IrModel(this, null);
                model.exportDB();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        if (!pref.getBoolean(DEVELOPER_MODE, false)) {
            if (runnable == null) {
                runnable = new Runnable() {
                    public void run() {
                        click_count = 0;
                    }
                };
                handler.postDelayed(runnable, 7000);
            }
            click_count = click_count + 1;
            if (click_count == 3) {
                Toast.makeText(this, R.string.developer_2_tap, Toast.LENGTH_SHORT).show();
            }
            if (click_count == 5) {
                pref.setBoolean(DEVELOPER_MODE, true);
                Toast.makeText(this, R.string.developer_5_tap, Toast.LENGTH_SHORT).show();
                finish();
                startActivity(new Intent(this, About.class));
            }
        }
    }
}
