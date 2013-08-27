package com.openerp;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

public class AppSettingsActivity extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setSubtitle("Application Settings");
		getActionBar().setDisplayHomeAsUpEnabled(true);

		getActionBar().setIcon(R.drawable.ic_action_settings);

		addPreferencesFromResource(R.xml.app_settings);

	}

    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {

	case android.R.id.home:
	    // app icon in action bar clicked; go home
	    finish();
	    return true;

	default:
	    return super.onOptionsItemSelected(item);
	}
    }
}
