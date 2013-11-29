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

import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

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

import com.openerp.addons.messages.MailFollowerDb;
import com.openerp.addons.messages.UserGroupsDb;
import com.openerp.auth.OpenERPAccountManager;
import com.openerp.orm.OEHelper;
import com.openerp.providers.message.MessageProvider;
import com.openerp.receivers.SyncFinishReceiver;
import com.openerp.support.OpenERPServerConnection;

/**
 * The Class UserGroupsSyncService.
 */
public class UserGroupsSyncService extends Service {

	/** The Constant TAG. */
	public static final String TAG = "UserGroupsSyncService";

	/** The s sync adapter. */
	private static SyncAdapterImpl sSyncAdapter = null;

	/** The i. */
	static int i = 0;

	/** The context. */
	Context context = null;

	/**
	 * Instantiates a new message sync service.
	 */
	public UserGroupsSyncService() {
		super();
		this.context = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		IBinder ret = null;
		ret = getSyncAdapter().getSyncAdapterBinder();
		return ret;
	}

	/**
	 * Gets the sync adapter.
	 * 
	 * @return the sync adapter
	 */
	public SyncAdapterImpl getSyncAdapter() {
		// TODO Auto-generated method stub
		if (sSyncAdapter == null) {
			sSyncAdapter = new SyncAdapterImpl(this);
		}
		return sSyncAdapter;
	}

	/**
	 * Perform sync.
	 * 
	 * @param context
	 *            the context
	 * @param account
	 *            the account
	 * @param extras
	 *            the extras
	 * @param authority
	 *            the authority
	 * @param provider
	 *            the provider
	 * @param syncResult
	 *            the sync result
	 */
	public void performSync(Context context, Account account, Bundle extras,
			String authority, ContentProviderClient provider,
			SyncResult syncResult) {
		// TODO Auto-generated method stub
		try {
			UserGroupsDb usergroups = new UserGroupsDb(context);
			Intent intent = new Intent();
			intent.setAction(SyncFinishReceiver.SYNC_FINISH);
			if (OpenERPServerConnection.isNetworkAvailable(context)) {
				Log.i(TAG + "::performSync()", "Sync with Server Started");
				OEHelper oe = usergroups.getOEInstance();
				if (oe.syncWithServer(usergroups, null, false, false)) {
					MailFollowerDb group_follower = new MailFollowerDb(context);
					OEHelper oe_1 = group_follower.getOEInstance();
					JSONObject domain = new JSONObject();
					int partner_id = Integer.parseInt(OpenERPAccountManager
							.currentUser(context).getPartner_id());
					domain.accumulate("domain",
							new JSONArray("[[\"partner_id\", \"=\", "
									+ partner_id + "],[\"res_model\",\"=\", \""
									+ usergroups.getModelName() + "\"]]"));

					if (oe_1.syncWithServer(group_follower, domain, false,
							false)) {
						Log.i(TAG, "UserGroups Sync Finished");
						MailFollowerDb follower = new MailFollowerDb(context);
						List<HashMap<String, Object>> user_groups = follower
								.executeSQL(follower.getModelName(),
										new String[] { "res_id" },
										new String[] { "partner_id = ?", "AND",
												"res_model = ?" },
										new String[] { partner_id + "",
												"mail.group" });
						JSONArray group_ids = new JSONArray();
						if (user_groups.size() > 0) {
							for (HashMap<String, Object> row : user_groups) {
								group_ids.put(Integer.parseInt(row
										.get("res_id").toString()));
							}
						}
						context.sendBroadcast(intent);
						Bundle bundle = new Bundle();
						bundle.putString("group_ids", group_ids.toString());
						bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL,
								true);
						bundle.putBoolean(
								ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
						ContentResolver.requestSync(account,
								MessageProvider.AUTHORITY, bundle);
					}

				}

			} else {
				Log.e("OpenERPServerConnection",
						"Unable to Connect with server");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * The Class SyncAdapterImpl.
	 */
	public class SyncAdapterImpl extends AbstractThreadedSyncAdapter {

		/** The m context. */
		private Context mContext;

		/**
		 * Instantiates a new sync adapter impl.
		 * 
		 * @param context
		 *            the context
		 */
		public SyncAdapterImpl(Context context) {
			super(context, true);
			mContext = context;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.content.AbstractThreadedSyncAdapter#onPerformSync(android
		 * .accounts.Account, android.os.Bundle, java.lang.String,
		 * android.content.ContentProviderClient, android.content.SyncResult)
		 */
		@Override
		public void onPerformSync(Account account, Bundle bundle, String str,
				ContentProviderClient providerClient, SyncResult syncResult) {
			// TODO Auto-generated method stub
			if (OpenERPAccountManager.isAnyUser(mContext)) {
				account = OpenERPAccountManager.getAccount(mContext,
						OpenERPAccountManager.currentUser(context)
								.getAndroidName());
				try {
					if (account != null) {
						new UserGroupsSyncService().performSync(mContext,
								account, bundle, str, providerClient,
								syncResult);
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
