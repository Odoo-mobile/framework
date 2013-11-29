/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http:www.openerp.com>)
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
package com.openerp.services;

import java.util.HashMap;
import java.util.List;

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

import com.openerp.addons.meeting.MeetingDBHelper;
import com.openerp.auth.OpenERPAccountManager;
import com.openerp.orm.OEHelper;
import com.openerp.support.JSONDataHelper;
import com.openerp.support.calendar.OECalendar;

public class MeetingSyncService extends Service {
	/** The sync broadcast helper. */
	private static SyncAdapterImpl sSyncAdapter = null;
	Context context = null;
	MeetingDBHelper db = null;
	OECalendar calendar = null;

	public MeetingSyncService() {
		super();
		// TODO Auto-generated constructor stub
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
			// creating a object of MeetingDBHelper to handle database
			db = new MeetingDBHelper(context);
			int user_id = Integer.parseInt(OpenERPAccountManager.currentUser(
					context).getUser_id());
			JSONObject domain = new JSONObject();
			domain.accumulate(
					"domain",
					new JSONArray("[[\"user_id\", \"=\", "
							+ user_id
							+ "],[\"id\",\"not in\", "
							+ JSONDataHelper.intArrayToJSONArray(db
									.localIds(db)) + "]]"));

			// start sync service to fetch new Records from OpenERP Server to
			// localDB
			// first delete records from localdb which are no more in OpenERP
			// Server
			// second add new records from OpenERP Server to localdb which are
			// not in localdb
			// update localdb with OpenERP Server
			OEHelper oe = db.getOEInstance();
			if (oe.syncWithServer(db, domain)) {
				// Sync Done, Next stuff....
				// initilizing com.openerp.support.calendar obejct to delete
				// event from OpenERP mobile calendar which are no more in
				// OpenERP Server and local db
				calendar = new OECalendar(context);

				// contains records which are deleted from OpenERP Server and
				// local db
				HashMap<String, List<HashMap<String, Object>>> deleted_ids = oe
						.getDeletedRows();

				// check whether any deleted record foud for crm.meeting
				if (deleted_ids.containsKey("crm.meeting")) {
					// contains all the deleted records for crm.meeting module
					List<HashMap<String, Object>> ids = deleted_ids
							.get("crm.meeting");

					// deleting events from OpenERP mobile calendar by id
					for (int i = 0; i < ids.size(); i++) {
						// fetching whole row which are deleted
						@SuppressWarnings("unchecked")
						HashMap<String, Object> row = ((List<HashMap<String, Object>>) ids
								.get(i).get("records")).get(0);
						// fetching calendar_event_id from localdb to and
						// deleting events by event_id[calendar_event_id]from
						// OpenERP mobile calendar
						calendar.delete_CalendarEvent(Integer.parseInt(row.get(
								"calendar_event_id").toString()));
					}
				}

				// start syncing crm.meetings With OpenERP mobile calandar
				// creating OpenERP mobile calendar if not exist
				// registering event under OpenERP mobile calendar if not exist
				// sync manully created events in OpenERP mobile calendar to
				// OpenERP Server
				calendar.sync_Event_TOServer(account, db);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class SyncAdapterImpl extends AbstractThreadedSyncAdapter {

		// declaring conctext CONSTANT
		private Context mContext;

		public SyncAdapterImpl(Context context) {
			super(context, true);

			// initilizing mContext with context value
			mContext = context;
		}

		@Override
		public void onPerformSync(Account account, Bundle bundle, String str,
				ContentProviderClient providerClient, SyncResult syncResult) {
			// TODO Auto-generated method stub

			if (OpenERPAccountManager.isAnyUser(mContext)) {
				// retriving user account name
				account = OpenERPAccountManager.getAccount(mContext,
						OpenERPAccountManager.currentUser(mContext)
								.getAndroidName());

				try {
					db = new MeetingDBHelper(mContext);
					// checking whether crm.meeting module installed or not
					if (db.getOEInstance().isInstalled("note.note")) {
						// checking whether user exist with this account name
						if (account != null) {
							// creating object to call performing sync operation
							new MeetingSyncService().performSync(mContext,
									account, bundle, str, providerClient,
									syncResult);
						}
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
