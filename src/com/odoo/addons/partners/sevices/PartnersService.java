package com.odoo.addons.partners.sevices;

import android.accounts.Account;
import android.app.Service;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.odoo.base.res.ResPartner;
import com.odoo.orm.OSyncHelper;
import com.odoo.receivers.SyncFinishReceiver;
import com.odoo.support.OUser;
import com.odoo.support.service.OService;

public class PartnersService extends OService {

	public static final String TAG = PartnersService.class.getSimpleName();

	@Override
	public Service getService() {
		return this;
	}

	@Override
	public void performSync(Context context, OUser user, Account account,
			Bundle extras, String authority, ContentProviderClient provider,
			SyncResult syncResult) {
		Log.v(TAG, "PartnersService:performSync()");
		try {
			OSyncHelper sync = null;
			Intent intent = new Intent();
			intent.setAction(SyncFinishReceiver.SYNC_FINISH);
			ResPartner resPartner = new ResPartner(context);
			resPartner.setUser(user);
			sync = resPartner.getSyncHelper().syncDataLimit(30);
			if (sync.syncWithServer()) {
				context.sendBroadcast(intent);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
