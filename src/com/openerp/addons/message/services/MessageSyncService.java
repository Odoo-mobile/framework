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
package com.openerp.addons.message.services;

import java.util.ArrayList;
import java.util.List;

import openerp.OEArguments;
import openerp.OEDomain;

import org.json.JSONArray;
import org.json.JSONObject;

import android.accounts.Account;
import android.app.ActivityManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.openerp.MainActivity;
import com.openerp.R;
import com.openerp.addons.message.MessageDB;
import com.openerp.addons.message.widgets.MessageWidget;
import com.openerp.auth.OpenERPAccountManager;
import com.openerp.orm.OEDataRow;
import com.openerp.orm.OEHelper;
import com.openerp.orm.OEValues;
import com.openerp.receivers.SyncFinishReceiver;
import com.openerp.support.OEUser;
import com.openerp.util.OEDate;
import com.openerp.util.OENotificationHelper;
import com.openerp.util.PreferenceManager;

/**
 * The Class MessageSyncService.
 */
public class MessageSyncService extends Service {

	/** The Constant TAG. */
	public static final String TAG = "com.openerp.addons.message.services.MessageSyncService";

	/** The s sync adapter. */
	private static SyncAdapterImpl sSyncAdapter = null;

	/** The i. */
	static int i = 0;

	/** The context. */
	Context mContext = null;

	/**
	 * Instantiates a new message sync service.
	 */
	public MessageSyncService() {
		super();
		mContext = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
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
		Intent intent = new Intent();
		Intent updateWidgetIntent = new Intent();
		updateWidgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		updateWidgetIntent.putExtra(MessageWidget.ACTION_MESSAGE_WIDGET_UPDATE,
				true);
		intent.setAction(SyncFinishReceiver.SYNC_FINISH);
		OEUser user = OpenERPAccountManager.getAccountDetail(context,
				account.name);
		try {
			MessageDB msgDb = new MessageDB(context);
			msgDb.setAccountUser(user);
			OEHelper oe = msgDb.getOEInstance();
			if (oe == null) {
				return;
			}
			int user_id = user.getUser_id();

			// Updating User Context for OE-JSON-RPC
			JSONObject newContext = new JSONObject();
			newContext.put("default_model", "res.users");
			newContext.put("default_res_id", user_id);

			OEArguments arguments = new OEArguments();
			// Param 1 : ids
			arguments.addNull();
			// Param 2 : domain
			OEDomain domain = new OEDomain();

			// Data limit.
			PreferenceManager mPref = new PreferenceManager(context);
			int data_limit = mPref.getInt("sync_data_limit", 60);
			domain.add("create_date", ">=", OEDate.getDateBefore(data_limit));

			if (!extras.containsKey("group_ids")) {
				// Last id
				JSONArray msgIds = new JSONArray();
				for (OEDataRow row : msgDb.select()) {
					msgIds.put(row.getInt("id"));
				}
				domain.add("id", "not in", msgIds);

				domain.add("|");
				// Argument for check partner_ids.user_id is current user
				domain.add("partner_ids.user_ids", "in",
						new JSONArray().put(user_id));

				domain.add("|");
				// Argument for check notification_ids.partner_ids.user_id
				// is
				// current user
				domain.add("notification_ids.partner_id.user_ids", "in",
						new JSONArray().put(user_id));

				// Argument for check author id is current user
				domain.add("author_id.user_ids", "in",
						new JSONArray().put(user_id));

			} else {
				JSONArray group_ids = new JSONArray(
						extras.getString("group_ids"));

				// Argument for group model check
				domain.add("model", "=", "mail.group");

				// Argument for group model res id
				domain.add("res_id", "in", group_ids);
			}

			arguments.add(domain.getArray());
			// Param 3 : message_unload_ids
			arguments.add(new JSONArray());
			// Param 4 : thread_level
			arguments.add(true);
			// Param 5 : context
			arguments.add(oe.updateContext(newContext));
			// Param 6 : parent_id
			arguments.addNull();
			// Param 7 : limit
			arguments.add(50);
			List<Integer> ids = msgDb.ids();
			if (oe.syncWithMethod("message_read", arguments)) {
				int affected_rows = oe.getAffectedRows();
				List<Integer> affected_ids = oe.getAffectedIds();
				boolean notification = true;
				ActivityManager am = (ActivityManager) context
						.getSystemService(ACTIVITY_SERVICE);
				List<ActivityManager.RunningTaskInfo> taskInfo = am
						.getRunningTasks(1);
				ComponentName componentInfo = taskInfo.get(0).topActivity;
				if (componentInfo.getPackageName().equalsIgnoreCase(
						"com.openerp")) {
					notification = false;
				}
				if (notification && affected_rows > 0) {
					OENotificationHelper mNotification = new OENotificationHelper();
					Intent mainActiivty = new Intent(context,
							MainActivity.class);
					mNotification.setResultIntent(mainActiivty, context);
					mNotification.showNotification(context, affected_rows
							+ " new messages", affected_rows
							+ " new message received (OpneERP)", authority,
							R.drawable.ic_oe_notification);
				}
				intent.putIntegerArrayListExtra("new_ids",
						(ArrayList<Integer>) affected_ids);
			}
			List<Integer> updated_ids = updateOldMessages(msgDb, oe, user, ids);
			intent.putIntegerArrayListExtra("updated_ids",
					(ArrayList<Integer>) updated_ids);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (user.getAndroidName().equals(account.name)) {
			context.sendBroadcast(intent);
			context.sendBroadcast(updateWidgetIntent);
		}
	}

	private List<Integer> updateOldMessages(MessageDB db, OEHelper oe,
			OEUser user, List<Integer> ids) {
		Log.d(TAG, "MessageSyncServide->updateOldMessages()");
		List<Integer> updated_ids = new ArrayList<Integer>();
		try {
			JSONArray ids_array = new JSONArray();
			for (int id : ids)
				ids_array.put(id);
			JSONObject fields = new JSONObject();

			fields.accumulate("fields", "read");
			fields.accumulate("fields", "starred");
			fields.accumulate("fields", "partner_id");
			fields.accumulate("fields", "message_id");

			OEDomain domain = new OEDomain();
			domain.add("message_id", "in", ids_array);
			domain.add("partner_id", "=", user.getPartner_id());
			JSONObject result = oe.search_read("mail.notification", fields,
					domain.get());
			for (int j = 0; j < result.getJSONArray("records").length(); j++) {
				JSONObject objRes = result.getJSONArray("records")
						.getJSONObject(j);
				int message_id = objRes.getJSONArray("message_id").getInt(0);
				boolean read = objRes.getBoolean("read");
				boolean starred = objRes.getBoolean("starred");
				OEValues values = new OEValues();
				values.put("starred", starred);
				values.put("to_read", !read);
				db.update(values, message_id);
				updated_ids.add(message_id);
			}
			updateMessageVotes(db, oe, user, ids_array);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return updated_ids;
	}

	private void updateMessageVotes(MessageDB db, OEHelper oe, OEUser user,
			JSONArray ids_array) {
		Log.d(TAG, "MessageSyncServide->updateMessageVotes()");
		try {
			JSONObject vote_fields = new JSONObject();
			vote_fields.accumulate("fields", "vote_user_ids");

			OEDomain domain = new OEDomain();
			domain.add("id", "in", ids_array);
			JSONObject vote_detail = oe.search_read("mail.message",
					vote_fields, domain.get(), 0, 0, null, null);
			for (int j = 0; j < vote_detail.getJSONArray("records").length(); j++) {
				JSONObject obj_vote = vote_detail.getJSONArray("records")
						.getJSONObject(j);
				JSONArray voted_user_ids = obj_vote
						.getJSONArray("vote_user_ids");
				OEValues values = new OEValues();
				for (int i = 0; i < voted_user_ids.length(); i++) {
					if (voted_user_ids.getInt(i) == user.getUser_id()) {
						values.put("has_voted", true);
						break;
					} else {
						values.put("has_voted", false);
					}
				}
				int total_votes = voted_user_ids.length();
				int message_id = obj_vote.getInt("id");
				values.put("vote_nb", total_votes);
				db.update(values, message_id);
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

		@Override
		public void onPerformSync(Account account, Bundle bundle, String str,
				ContentProviderClient providerClient, SyncResult syncResult) {
			if (account != null) {
				new MessageSyncService().performSync(mContext, account, bundle,
						str, providerClient, syncResult);
			}

		}

	}

}
