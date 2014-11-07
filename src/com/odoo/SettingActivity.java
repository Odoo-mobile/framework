package com.odoo;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.odoo.base.about.About;

public class SettingActivity extends ActionBarActivity {
	public static final String ACTION_ABOUT = "com.odoo.ACTION_ABOUT";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.base_setting_activity);
		getSupportActionBar().setBackgroundDrawable(
				new ColorDrawable(getResources()
						.getColor(R.color.theme_primary)));
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
}
