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
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.openerp.auth.OpenERPAccountManager;
import com.openerp.base.res.Res_PartnerDBHelper;
import com.openerp.base.res.Res_PartnerSyncHelper;
import com.openerp.orm.OEHelper;
import com.openerp.receivers.SyncFinishReceiver;
import com.openerp.support.JSONDataHelper;
import com.openerp.util.logger.OELog;

public class ContactSyncService extends Service {
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

			String saasURL1 = "https://openerp.my.openerp.com";
			String saasURL2 = "https://accounts.openerp.com";
			Res_PartnerDBHelper db = new Res_PartnerDBHelper(context);
			OEHelper oe = db.getOEInstance();
			Res_PartnerSyncHelper helper = new Res_PartnerSyncHelper(context);

			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(context);
			boolean syncServerContacts = settings.getBoolean(
					"server_contact_sync", false);

			if (OpenERPAccountManager.currentUser(context).getHost().toString()
					.contains(saasURL1)
					|| OpenERPAccountManager.currentUser(context).getHost()
							.toString().contains(saasURL2)) {
				helper.syncContacts(context, account);

			} else {
				if (syncServerContacts) {
					int company_id = Integer.parseInt(OpenERPAccountManager
							.currentUser(context).getCompany_id());

					JSONObject domain = new JSONObject();
					domain.accumulate("domain", new JSONArray(
							"[[\"company_id\", \"=\", " + company_id + "]]"));
					if (oe.syncWithServer(db, domain, false)) {
						helper.syncContacts(context, account);
					}
				} else {
					helper.syncContacts(context, account);
				}
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
