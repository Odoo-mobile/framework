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
import android.app.ActivityManager;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.openerp.MainActivity;
import com.openerp.R;
import com.openerp.addons.messages.MessageDBHelper;
import com.openerp.addons.messages.MessageSyncHelper;
import com.openerp.auth.OpenERPAccountManager;
import com.openerp.receivers.SyncFinishReceiver;
import com.openerp.support.JSONDataHelper;
import com.openerp.support.OEArgsHelper;
import com.openerp.support.OpenERPServerConnection;
import com.openerp.util.OEDate;
import com.openerp.util.OENotificationHelper;
import com.openerp.widget.Mobile_Widget;

// TODO: Auto-generated Javadoc
/**
 * The Class MessageSyncService.
 */
public class MessageSyncService extends Service {

	/** The Constant TAG. */
	public static final String TAG = "MessageSyncService";

	/** The s sync adapter. */
	private static SyncAdapterImpl sSyncAdapter = null;

	/** The i. */
	static int i = 0;

	/** The context. */
	Context context = null;

	/**
	 * Instantiates a new message sync service.
	 */
	public MessageSyncService() {
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
		try {
			MessageDBHelper msgDb = new MessageDBHelper(context);
			Intent intent = new Intent();
			Intent update_widget = new Intent();
			HashMap<String, Object> response = null;
			if (OpenERPServerConnection.isNetworkAvailable(context)) {
				Log.i(TAG + "::performSync()", "Sync with Server Started");
				intent.setAction(SyncFinishReceiver.SYNC_FINISH);
				update_widget.setAction(Mobile_Widget.TAG);
				int user_id = Integer.parseInt(OpenERPAccountManager
						.currentUser(context).getUser_id());

				// Updating User Context for OE-JSON-RPC
				JSONObject newContext = new JSONObject();
				newContext.put("default_model", "res.users");
				newContext.put("default_res_id", user_id);
				newContext.put("search_default_message_unread", true);
				newContext.put("search_disable_custom_filters", true);
				JSONObject dataContext = msgDb.getOEInstance().updateContext(
						newContext);

				// Providing arguments to filter messages from server.
				// Argument for Check Ids not in local database
				OEArgsHelper arg1 = new OEArgsHelper();
				arg1.addArgCondition("id", "not in", JSONDataHelper
						.intArrayToJSONArray(msgDb.localIds(msgDb)));

				// Handling setting argument for sync data limit.
				OEArgsHelper arg_date = new OEArgsHelper();
				SharedPreferences pref = PreferenceManager
						.getDefaultSharedPreferences(context);
				int data_limit = Integer.parseInt(pref.getString(
						"sync_data_limit", "60"));
				arg_date.addArgCondition("create_date", ">=",
						OEDate.getDateBefore(data_limit));

				OEArgsHelper mainArg_2 = new OEArgsHelper();
				mainArg_2.addArg(arg1.getArgs());
				mainArg_2.addArg(arg_date.getArgs());
				if (!extras.containsKey("group_ids")) {

					// Argument for check partner_ids.user_id is current user
					OEArgsHelper arg2 = new OEArgsHelper();
					arg2.addArgCondition("partner_ids.user_ids", "in",
							new JSONArray().put(user_id));

					// Argument for check notification_ids.partner_ids.user_id
					// is
					// current user
					OEArgsHelper arg3 = new OEArgsHelper();
					arg3.addArgCondition(
							"notification_ids.partner_id.user_ids", "in",
							new JSONArray().put(user_id));

					// Argument for check author id is current user
					OEArgsHelper arg4 = new OEArgsHelper();
					arg4.addArgCondition("author_id.user_ids", "in",
							new JSONArray().put(user_id));

					// Combination of arg2, arg3, arg4 with operators
					mainArg_2.addArg("|");
					mainArg_2.addArg(arg2.getArgs());
					mainArg_2.addArg("|");
					mainArg_2.addArg(arg3.getArgs());
					mainArg_2.addArg(arg4.getArgs());
				} else {
					JSONArray group_ids = new JSONArray(
							extras.getString("group_ids"));

					// Argument for group model check
					OEArgsHelper arg2 = new OEArgsHelper();
					arg2.addArgCondition("model", "=", "mail.group");

					// Argument for group model res id
					OEArgsHelper arg3 = new OEArgsHelper();
					arg3.addArgCondition("res_id", "in", group_ids);

					// Combination of arg2, arg3 with main argument
					mainArg_2.addArg(arg2.getArgs());
					mainArg_2.addArg(arg3.getArgs());
				}

				// Generating Full Argument using above arguments
				OEArgsHelper mainArgs = new OEArgsHelper();
				// Param 1 : ids
				mainArgs.addArg(null);
				// Param 2 : domain
				mainArgs.addArg(mainArg_2.getArgs());
				// Param 3 : message_unload_ids
				mainArgs.addArg(new JSONArray());
				// Param 4 : thread_level
				mainArgs.addArg(true);
				// Param 5 : context
				mainArgs.addArg(dataContext);
				// Param 6 : parent_id
				mainArgs.addArg(null);
				// Param 7 : limit
				mainArgs.addArg(50);

				response = new MessageSyncHelper(context).syncWithServer(msgDb,
						mainArgs.getArgs());
				// Sync status updator

				if (Integer.parseInt(response.get("total").toString()) > 0) {
					intent.putExtra("data_new", response.get("new_ids")
							.toString());
					int totalNewMessage = ((JSONArray) response.get("new_ids"))
							.length();
					boolean showNotification = true;

					ActivityManager am = (ActivityManager) context
							.getSystemService(ACTIVITY_SERVICE);
					// get the info from the currently running task
					List<ActivityManager.RunningTaskInfo> taskInfo = am
							.getRunningTasks(1);

					ComponentName componentInfo = taskInfo.get(0).topActivity;
					// if app is running
					if (componentInfo.getPackageName().equalsIgnoreCase(
							"com.openerp")) {
						showNotification = false;
					}

					if (showNotification && totalNewMessage > 0) {
						OENotificationHelper notification = new OENotificationHelper();
						Intent mainActiivty = new Intent(context,
								MainActivity.class);
						notification.setResultIntent(mainActiivty, context);

						notification.showNotification(context, totalNewMessage
								+ " new messages", totalNewMessage
								+ " new message received (OpneERP)", authority,
								R.drawable.ic_oe_notification);
					}
					intent.putExtra("data_update", response.get("update_ids")
							.toString());
					context.sendBroadcast(intent);
					context.sendBroadcast(update_widget);
				} else {
					intent.putExtra("data_new", "false");
					intent.putExtra("data_update", "false");
					context.sendBroadcast(intent);
					context.sendBroadcast(update_widget);
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
						new MessageSyncService().performSync(mContext, account,
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
