/*
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
 */

package com.odoo;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.view.MenuItem;

import com.odoo.auth.OdooAccountManager;
import com.odoo.base.about.About;
import com.odoo.support.OUser;
import com.odoo.util.Base64Helper;

public class BaseSettings extends PreferenceActivity implements
		OnPreferenceClickListener {

	public static final String ACTION_GENERAL_CONFIG = "com.odoo.ACTION_ACCOUNT_GENERAL_CONFIG";
	public static final String ACTION_ABOUT = "com.odoo.ACTION_ABOUT";
	public static final String KEY_ADD_ACCOUNT = "add_account";

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionbar = getActionBar();
		actionbar.setHomeButtonEnabled(true);
		actionbar.setSubtitle(R.string.title_application_settings);
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setIcon(R.drawable.ic_action_settings);
		addPreferencesFromResource(R.xml.base_preference);
	}

	private void populateAccounts() {
		@SuppressWarnings("deprecation")
		PreferenceCategory account_settings = (PreferenceCategory) getPreferenceScreen()
				.findPreference("account_settings");
		account_settings.removeAll();
		for (OUser user : OdooAccountManager.fetchAllAccounts(this)) {
			Preference pref = new Preference(this);
			Bitmap bitmap = null;
			if (!user.getAvatar().equals("false")) {
				bitmap = Base64Helper.getBitmapImage(this, user.getAvatar());
			} else {
				bitmap = BitmapFactory.decodeResource(getResources(),
						R.drawable.avatar);
			}
			pref.setIcon(new BitmapDrawable(getResources(), Base64Helper
					.getRoundedCornerBitmap(this, bitmap, true)));

			pref.setTitle(user.getAndroidName());
			pref.setSummary((user.isOAauthLogin()) ? user.getInstanceUrl()
					: user.getHost());
			account_settings.addPreference(pref);
		}

		// Add account
		Preference pref = new Preference(this);
		// pref.setKey(KEY_ADD_ACCOUNT);
		pref.setTitle(R.string.add_account);
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra("create_new_account", true);
		pref.setIntent(intent);
		account_settings.addPreference(pref);
	}

	@Override
	public void startActivity(Intent intent) {
		if (intent.getAction() != null) {
			if (intent.getAction().equals(ACTION_GENERAL_CONFIG)) {
				intent = new Intent(this, GeneralSettings.class);
				startActivity(intent);
			} else if (intent.getAction().equals(ACTION_ABOUT)) {
				intent = new Intent(this, About.class);
				startActivity(intent);
			}
		} else {
			super.startActivity(intent);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		populateAccounts();
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		return false;
	}
}
