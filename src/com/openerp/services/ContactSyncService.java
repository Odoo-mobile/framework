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

import org.json.JSONArray;
import org.json.JSONObject;

import android.accounts.Account;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.openerp.auth.OpenERPAccountManager;
import com.openerp.base.res.Res_PartnerDBHelper;
import com.openerp.base.res.Res_PartnerSyncHelper;
import com.openerp.orm.OEHelper;
import com.openerp.receivers.SyncFinishReceiver;
import com.openerp.support.JSONDataHelper;

public class ContactSyncService extends Service {
	private final static String TAG1 = "In this method: ";
	int mStartMode; // indicates how to behave if the service is killed
	IBinder mBinder; // interface for clients that bind
	boolean mAllowRebind; // indicates whether onRebind should be used

	/** The sync broadcast helper. */
	// SyncBroadcastHelper sync_helper = new SyncBroadcastHelper();
	public static final String TAG = "ContactSyncService";
	private static SyncAdapterImpl sSyncAdapter = null;
	static int i = 0;
	Context context = null;

	public ContactSyncService() {
		super();
		this.context = this;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		IBinder ret = null;
		ret = getSyncAdapter().getSyncAdapterBinder();
		return ret;
	}

	public SyncAdapterImpl getSyncAdapter() {
		// TODO Auto-generated method stub
		if (sSyncAdapter == null) {
			sSyncAdapter = new SyncAdapterImpl(this);
		}
		return sSyncAdapter;
	}

	public void performSync(Context context, Account account, Bundle extras,
			String authority, ContentProviderClient provider,
			SyncResult syncResult) {
		// TODO Auto-generated method stub
		try {
			// sync_helper.sendBrodcast(context, authority, "Contacts",
			// "start");
			Intent intent = new Intent();
			intent.setAction(SyncFinishReceiver.SYNC_FINISH);

			int company_id = Integer.parseInt(OpenERPAccountManager
					.currentUser(context).getCompany_id());
			Res_PartnerDBHelper db = new Res_PartnerDBHelper(context);
			JSONObject domain = new JSONObject();
			domain.accumulate(
					"domain",
					new JSONArray("[[\"company_id\", \"=\", "
							+ company_id
							+ "],[\"id\",\"not in\", "
							+ JSONDataHelper.intArrayToJSONArray(db
									.localIds(db)) + "]]"));

			OEHelper oe = db.getOEInstance();
			if (oe.syncWithServer(db, domain, false)) {
				// Sync Done, Next stuff....
				Res_PartnerSyncHelper helper = new Res_PartnerSyncHelper(
						context);
				helper.SyncContect(context, account);
				// sync_helper.sendBrodcast(context, authority, "Contacts",
				// "finish");
			}
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

			// TODO Auto-generated method stub
			if (OpenERPAccountManager.isAnyUser(mContext)) {
				account = OpenERPAccountManager.getAccount(mContext,
						OpenERPAccountManager.currentUser(mContext)
								.getAndroidName());
				Log.i("Sync Service Start", "Syncing Contacts");
				try {
					if (account != null) {
						new ContactSyncService().performSync(mContext, account,
								bundle, str, providerClient, syncResult);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				return;
			}
		}
	}
}
