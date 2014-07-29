package com.odoo.addons.idea.services;

import android.accounts.Account;
import android.app.Service;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.odoo.addons.idea.model.BookBook;
import com.odoo.addons.idea.model.BookBook.BookAuthor;
import com.odoo.orm.OSyncHelper;
import com.odoo.receivers.SyncFinishReceiver;
import com.odoo.support.OUser;
import com.odoo.support.service.OService;

public class LibraryService extends OService {

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
			OUser user = OUser.current(context);
			Intent intent = new Intent();
			intent.setAction(SyncFinishReceiver.SYNC_FINISH);
			BookBook db = new BookBook(context);
			db.setUser(user);
			OSyncHelper sync = db.getSyncHelper();
			sync.syncDataLimit(10);
			if (sync.syncWithServer()) {
				BookAuthor author = new BookAuthor(context);
				author.setUser(user);
				if (author.getSyncHelper().syncWithServer()) {
					context.sendBroadcast(intent);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
