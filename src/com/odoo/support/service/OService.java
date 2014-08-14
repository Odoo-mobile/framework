/*
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
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
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * 
 */
package com.odoo.support.service;

import com.odoo.auth.OdooAccountManager;
import com.odoo.support.OUser;

import android.accounts.Account;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;

public abstract class OService extends Service implements OServiceListener {
	private SyncAdapterImpl sSyncAdapter = null;
	OServiceListener mService = null;
	Context mServiceContext = null;

	public OService() {
		mServiceContext = this;
	}

	@Override
	public IBinder onBind(Intent intent) {
		if (sSyncAdapter == null) {
			sSyncAdapter = new SyncAdapterImpl(this);
		}
		return sSyncAdapter.getSyncAdapterBinder();
	}

	private class SyncAdapterImpl extends AbstractThreadedSyncAdapter {
		Context mContext = null;

		public SyncAdapterImpl(Context context) {
			super(context, true);
			mContext = context;
		}

		@Override
		public void onPerformSync(Account account, Bundle extras,
				String authority, ContentProviderClient provider,
				SyncResult syncResult) {
			mService = (OServiceListener) mServiceContext;
			OUser user = OdooAccountManager.getAccountDetail(mContext,
					account.name);
			mService.performSync(mContext, user, account, extras, authority,
					provider, syncResult);
		}
	}
}
