package com.openerp.services;

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
import com.openerp.addons.note.NoteDBHelper;
import com.openerp.auth.OpenERPAccountManager;
import com.openerp.orm.OEHelper;
import com.openerp.receivers.SyncFinishReceiver;
import com.openerp.util.SyncBroadcastHelper;

public class NoteSyncService extends Service {
	/** The sync broadcast helper. */
	SyncBroadcastHelper sync_helper = new SyncBroadcastHelper();
	public static final String TAG = "NoteSyncService";
	private static SyncAdapterImpl sSyncAdapter = null;
	static int i = 0;
	Context context = null;

	public NoteSyncService() {
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
			sync_helper.sendBrodcast(context, authority, "Note", "start");
			Intent intent = new Intent();
			intent.setAction(SyncFinishReceiver.SYNC_FINISH);

			NoteDBHelper db = new NoteDBHelper(context);
			OEHelper oe = new OEHelper(context, MainActivity.userContext);

			if (oe.syncWithServer(db)) {
				// Sync Done, Next stuff....
				context.sendBroadcast(intent);
				sync_helper.sendBrodcast(context, authority, "Note", "finish");
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

				Log.i("Sync Service Start", "Syncing Notes");

				try {
					if (account != null) {
						new NoteSyncService().performSync(mContext, account,
								bundle, str, providerClient, syncResult);
					}
				} catch (Exception e) {

				}
			} else {
				return;
			}
		}
	}

}
