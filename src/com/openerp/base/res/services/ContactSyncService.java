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
package com.openerp.base.res.services;

import openerp.OEDomain;
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
import com.openerp.base.res.ResPartnerDB;
import com.openerp.orm.OEHelper;
import com.openerp.support.OEUser;
import com.openerp.support.contact.OEContact;

public class ContactSyncService extends Service {

	public static final String TAG = "com.openerp.base.res.services.ContactSyncService";

	private static SyncAdapterImpl sSyncAdapter = null;
	Context mContext = null;

	public ContactSyncService() {
		this.mContext = this;
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
			OEUser user = OpenERPAccountManager.getAccountDetail(context,
					account.name);

			ResPartnerDB db = new ResPartnerDB(context);
			OEHelper oe = db.getOEInstance();

			OEContact contact = new OEContact(context, user);

			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(context);
			boolean syncServerContacts = settings.getBoolean(
					"server_contact_sync", false);

			if (syncServerContacts && oe != null) {
				Log.v(TAG, "Contact sync with server");
				int company_id = Integer.parseInt(user.getCompany_id());
				OEDomain domain = new OEDomain();
				domain.add("company_id", "=", company_id);
				oe.syncWithServer(domain, false);
			}
			contact.createContacts(db.select());
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

			new ContactSyncService().performSync(mContext, account, bundle,
					str, providerClient, syncResult);

		}
	}
}
