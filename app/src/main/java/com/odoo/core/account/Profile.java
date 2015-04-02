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
 * Created on 9/1/15 11:54 AM
 */
package com.odoo.core.account;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.odoo.R;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.support.OUser;
import com.odoo.core.utils.BitmapUtils;
import com.odoo.core.utils.OActionBarUtils;
import com.odoo.core.utils.OStringColorUtil;
import com.odoo.widgets.parallax.ParallaxScrollView;

import odoo.controls.OField;
import odoo.controls.OForm;

public class Profile extends ActionBarActivity implements View.OnClickListener {
    public static final String TAG = Profile.class.getSimpleName();
    private OUser user;
    private OForm form;
    private ParallaxScrollView parallaxScrollView;
    private TextView title;
    private Handler handler = null;
    private int click_count = 0;
    public static String CONNECT_WITH_ODOO = "enable_connect_with_odoo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_profile);
        OActionBarUtils.setActionBar(this, false);
        user = OUser.current(this);
        form = (OForm) findViewById(R.id.profileDetails);
        OField user_login = (OField) findViewById(R.id.user_login);
        parallaxScrollView = (ParallaxScrollView) findViewById(R.id.parallaxScrollView);
        parallaxScrollView.setActionBar(getSupportActionBar());
        setTitle("");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable((Color.parseColor("#00000000"))));
        title = (TextView) findViewById(android.R.id.title);
        int color = OStringColorUtil.getStringColor(this, user.getName());
        parallaxScrollView.setParallaxOverLayColor(color);
        form.setIconTintColor(color);
        ODataRow userData = new ODataRow();
        userData.put("name", user.getName());
        userData.put("user_login", user.getUsername());
        userData.put("server_url", (user.isOAauthLogin()) ? user.getInstanceUrl() : user.getHost());
        userData.put("database", (user.isOAauthLogin()) ? user.getInstanceDatabase() : user.getDatabase());
        userData.put("version", user.getVersion_serie());
        userData.put("timezone", user.getTimezone());
        form.initForm(userData);
        title.setText(userData.getString("name"));

        Bitmap avatar;
        if (user.getAvatar().equals("false")) {
            avatar = BitmapUtils.getAlphabetImage(this, user.getName());
        } else {
            avatar = BitmapUtils.getBitmapImage(this, user.getAvatar());
        }
        ImageView imageView = (ImageView) findViewById(android.R.id.icon);
        imageView.setImageBitmap(avatar);
        user_login.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
//        FIXME:
//
//          handler = getWindow().getDecorView().getHandler();
//        click_count = click_count + 1;
//        Runnable r = new Runnable() {
//            public void run() {
//                click_count = 0;
//            }
//        };
//        handler.postDelayed(r, 7000);
//
//        if (click_count == 3) {
//            Toast.makeText(this, "Need 2 Tap to connect with odoo", Toast.LENGTH_SHORT).show();
//        }
//        if (click_count == 5) {
//            OPreferenceManager pref = new OPreferenceManager(this);
//            pref.setBoolean(CONNECT_WITH_ODOO, true);
//        }
    }
}

