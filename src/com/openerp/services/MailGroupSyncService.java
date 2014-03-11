/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http://www.openerp.com>)
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * 
 */
package com.openerp.services;

import openerp.OEDomain;

import org.json.JSONArray;

import android.accounts.Account;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.openerp.addons.message.MailGroupDB;
import com.openerp.auth.OpenERPAccountManager;
import com.openerp.base.mail.MailFollowers;
import com.openerp.orm.OEDataRow;
import com.openerp.orm.OEHelper;
import com.openerp.providers.message.MessageProvider;
import com.openerp.receivers.SyncFinishReceiver;

public class MailGroupSyncService extends Service {
	public static final String TAG = "com.openerp.services.MailGroupSyncService";
	private static SyncAdapterImpl sSyncAdapter = null;
	static int i = 0;
	Context mContext = null;

	public MailGroupSyncService() {
		mContext = this;
	}

	@Override
	public IBinder onBind(Intent intent) {
		IBinder ret = null;
		ret = getSyncAdapter().getSyncAdapterBinder();
		return ret;
	}

	public SyncAdapterImpl getSyncAdapter() {

		if (sSyncAdapter == null) {
			sSyncAdapter = new SyncAdapterImpl(this);
		}
		return sSyncAdapter;
	}

	public void performSync(Context context, Account account, Bundle extras,
			String authority, ContentProviderClient provider,
			SyncResult syncResult) {

		try {
			Intent intent = new Intent();
			intent.setAction(SyncFinishReceiver.SYNC_FINISH);

			MailGroupDB db = new MailGroupDB(context);
			db.setAccountUser(OpenERPAccountManager.getAccountDetail(context,
					account.name));
			OEHelper oe = db.getOEInstance();
			if (oe != null && oe.syncWithServer(true)) {
				MailFollowers followers = new MailFollowers(context);

				OEDomain domain = new OEDomain();
				domain.add("partner_id", "=", oe.getUser().getPartner_id());
				domain.add("res_model", "=", db.getModelName());

				if (followers.getOEInstance().syncWithServer(domain, true)) {
					// syncing group messages
					JSONArray group_ids = new JSONArray();
					for (OEDataRow grp : followers.select(
							"res_model = ? AND partner_id = ?", new String[] {
									db.getModelName(),
									oe.getUser().getPartner_id() + "" })) {
						group_ids.put(grp.getInt("res_id"));
					}
					Bundle messageBundle = new Bundle();
					messageBundle.putString("group_ids", group_ids.toString());
					messageBundle.putBoolean(
							ContentResolver.SYNC_EXTRAS_MANUAL, true);
					messageBundle.putBoolean(
							ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
					ContentResolver.requestSync(account,
							MessageProvider.AUTHORITY, messageBundle);

				}
			}
			if (OpenERPAccountManager.currentUser(context).getAndroidName()
					.equals(account.name))
				context.sendBroadcast(intent);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class SyncAdapterImpl extends AbstractThreadedSyncAdapter {
		private Context mContext;

		public SyncAdapterImpl(Context context) {
			super(context, true);
			mContext = context;
		}

		@Override
		public void onPerformSync(Account account, Bundle bundle, String str,
				ContentProviderClient providerClient, SyncResult syncResult) {
			Log.d(TAG, "Mail Group sync service started");
			try {
				if (account != null) {
					new MailGroupSyncService().performSync(mContext, account,
							bundle, str, providerClient, syncResult);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
}
