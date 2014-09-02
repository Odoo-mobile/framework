package com.odoo.addons.partners.sevices;

import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;

import com.odoo.App;
import com.odoo.MainActivity;
import com.odoo.R;
import com.odoo.base.res.ResPartner;
import com.odoo.base.res.providers.partners.PartnersProvider;
import com.odoo.support.OUser;
import com.odoo.support.service.OSyncAdapter;
import com.odoo.support.service.OSyncFinishListener;
import com.odoo.support.service.OSyncService;
import com.odoo.util.ONotificationHelper;

public class PartnersService extends OSyncService implements
		OSyncFinishListener {

	public static final String TAG = PartnersService.class.getSimpleName();

	@Override
	public OSyncAdapter getSyncAdapter() {
		return new OSyncAdapter(getApplicationContext(), new ResPartner(
				getApplicationContext()), this, true).syncDataLimit(50)
				.onSyncFinish(this);
	}

	@Override
	public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {
		/**
		 * Getting extras bundle set domain to adapter depends on extras.
		 */
	}

	@Override
	public OSyncAdapter performSync(SyncResult syncResult) {
		App app = (App) getApplicationContext();
		if (!app.appOnTop()) {
			// Notification here...
			int totalAffected = (int) (syncResult.stats.numUpdates
					+ syncResult.stats.numInserts + syncResult.stats.numDeletes);
			if (totalAffected > 0) {
				ONotificationHelper notification = new ONotificationHelper();
				Intent intent = new Intent(getApplicationContext(),
						MainActivity.class);
				notification.setResultIntent(intent, getApplicationContext());
				notification.showNotification(getApplicationContext(),
						"Partners Sync finished", totalAffected
								+ " record affected",
						PartnersProvider.AUTHORITY, R.drawable.ic_odoo_o);
			}
		}
		return null;
	}
}
