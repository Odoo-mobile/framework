package com.odoo.addons.idea.services;

import android.accounts.Account;
import android.app.Service;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.odoo.addons.idea.BooksDB;
import com.odoo.auth.OdooAccountManager;
import com.odoo.orm.OEHelper;
import com.odoo.receivers.SyncFinishReceiver;
import com.odoo.support.service.OEService;

public class LibraryService extends OEService {

	public static final String TAG = LibraryService.class.getSimpleName();

	@Override
	public Service getService() {
		return this;
	}

	@Override
	public void performSync(Context context, Account account, Bundle extras,
			String authority, ContentProviderClient provider,
			SyncResult syncResult) {
		Log.v(TAG, "LibraryService:performSync()");
		try {
			Intent intent = new Intent();
			intent.setAction(SyncFinishReceiver.SYNC_FINISH);
			BooksDB db = new BooksDB(context);
			db.setAccountUser(OdooAccountManager.getAccountDetail(context,
					account.name));
			OEHelper oe = db.getOEInstance();
			if (oe != null && oe.syncWithServer(true)) {
				context.sendBroadcast(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
