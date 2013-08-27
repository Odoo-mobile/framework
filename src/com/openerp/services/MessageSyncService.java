package com.openerp.services;

import java.util.HashMap;

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

import com.openerp.MainActivity;
import com.openerp.addons.messages.MessageDBHelper;
import com.openerp.addons.messages.MessageSyncHelper;
import com.openerp.auth.OpenERPAccountManager;
import com.openerp.orm.OEHelper;
import com.openerp.receivers.SyncFinishReceiver;
import com.openerp.support.JSONDataHelper;
import com.openerp.support.OEArgsHelper;
import com.openerp.support.OpenERPServerConnection;

public class MessageSyncService extends Service {
    public static final String TAG = "MessageSyncService";
    private static SyncAdapterImpl sSyncAdapter = null;
    static int i = 0;
    Context context = null;

    public MessageSyncService() {
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
	Log.d("performSync for Message", "Started");
	try {

	    Intent intent = new Intent();
	    HashMap<String, Object> response = null;
	    if (OpenERPServerConnection.isNetworkAvailable(context)) {
		Log.d(TAG + "::performSync()", "Sync with Server Started");
		intent.setAction(SyncFinishReceiver.SYNC_FINISH);
		MainActivity.openerp = new OEHelper(context,
			OpenERPAccountManager.currentUser(context));
		int user_id = Integer.parseInt(MainActivity.userContext
			.getUser_id());

		// Updating User Context for OE-JSON-RPC
		JSONObject newContext = new JSONObject();
		newContext.put("default_model", "res.users");
		newContext.put("default_res_id", user_id);
		newContext.put("search_default_message_unread", true);
		newContext.put("search_disable_custom_filters", true);
		JSONObject dataContext = MainActivity.openerp
			.updateContext(newContext);

		// Providing arguments to filter messages from server.
		// Argument for Check Ids not in local database
		OEArgsHelper arg1 = new OEArgsHelper();
		arg1.addArgCondition("id", "not in", JSONDataHelper
			.intArrayToJSONArray(MainActivity.openerp
				.getAllIds(new MessageDBHelper(context))));

		// Argument for check partner_ids.user_id is current user
		OEArgsHelper arg2 = new OEArgsHelper();
		arg2.addArgCondition("partner_ids.user_ids", "in",
			new JSONArray().put(user_id));

		// Argument for check notification_ids.partner_ids.user_id is
		// current user
		OEArgsHelper arg3 = new OEArgsHelper();
		arg3.addArgCondition("notification_ids.partner_id.user_ids",
			"in", new JSONArray().put(user_id));

		// Argument for check author id is current user
		OEArgsHelper arg4 = new OEArgsHelper();
		arg4.addArgCondition("author_id.user_ids", "in",
			new JSONArray().put(user_id));

		// Combination of arg1, arg2, arg3, arg4 with operators
		OEArgsHelper mainArg_2 = new OEArgsHelper();
		mainArg_2.addArg(arg1.getArgs());
		mainArg_2.addArg("|");
		mainArg_2.addArg(arg2.getArgs());
		mainArg_2.addArg("|");
		mainArg_2.addArg(arg3.getArgs());
		mainArg_2.addArg(arg4.getArgs());

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

		response = new MessageSyncHelper(context).syncWithServer(
			new MessageDBHelper(context), mainArgs.getArgs());

		if (Integer.parseInt(response.get("total").toString()) > 0) {
		    intent.putExtra("data", response.get("new_ids").toString());
		    Log.d("MessageSyncService",
			    "sending sync finish broadcast.");
		    context.sendBroadcast(intent);
		} else {
		    intent.putExtra("data", "false");
		    context.sendBroadcast(intent);
		}

	    } else {
		Log.e("OpenERPServerConnection",
			"Unable to Connect with server");
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
			MainActivity.userContext.getAndroidName());
		try {
		    if (account != null) {
			new MessageSyncService().performSync(mContext, account,
				bundle, str, providerClient, syncResult);
		    }
		} catch (Exception e) {

		}
	    } else {
		return;
	    }
	}

	// private Uri asSyncAdapter(Uri uri, String account, String
	// accountType) {
	//
	// return uri
	// .buildUpon()
	// .appendQueryParameter(
	// CalendarContract.CALLER_IS_SYNCADAPTER, "true")
	// .appendQueryParameter(Calendars.ACCOUNT_NAME, account)
	// .appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType)
	// .build();
	// }
    }

}
