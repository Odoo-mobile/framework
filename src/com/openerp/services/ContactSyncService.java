/*******************************************************************************
 * Copyright 2010 Sam Steele 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
import com.openerp.util.SyncBroadcastHelper;

public class ContactSyncService extends Service {
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
			JSONObject domain = new JSONObject();
			domain.accumulate("domain", new JSONArray(
					"[[\"company_id\", \"=\", " + company_id + "]]"));

			Res_PartnerDBHelper db = new Res_PartnerDBHelper(context);
			OEHelper oe = db.getOEInstance();
			if (oe.syncWithServer(db, domain)) {
				// Sync Done, Next stuff....
				Res_PartnerSyncHelper helper = new Res_PartnerSyncHelper(
						context);
				helper.SyncContect(context, account);
				context.sendBroadcast(intent);
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
