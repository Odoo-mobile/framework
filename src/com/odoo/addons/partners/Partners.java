package com.odoo.addons.partners;

import java.util.ArrayList;
import java.util.List;

import odoo.controls.OList;
import odoo.controls.OList.OnRowClickListener;
import android.content.Context;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.odoo.R;
import com.odoo.addons.partners.providers.partners.PartnersProvider;
import com.odoo.base.res.ResPartner;
import com.odoo.orm.ODataRow;
import com.odoo.receivers.SyncFinishReceiver;
import com.odoo.support.AppScope;
import com.odoo.support.BaseFragment;
import com.odoo.util.OControls;
import com.odoo.util.drawer.DrawerItem;
import com.openerp.OETouchListener;
import com.openerp.OETouchListener.OnPullListener;

public class Partners extends BaseFragment implements OnRowClickListener,
		OnPullListener {

	public static final String TAG = Partners.class.getSimpleName();

	View mView = null;
	List<ODataRow> mListRecords = new ArrayList<ODataRow>();
	OList mListcontrol = null;
	OETouchListener mTouchListener = null;
	DataLoader mDataLoader = null;
	Bundle arg = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		scope = new AppScope(this);
		mView = inflater.inflate(R.layout.partners_list, container, false);
		init();
		return mView;
	}

	public void init() {
		mListcontrol = (OList) mView.findViewById(R.id.listRecords);
		mListcontrol.setOnRowClickListener(this);
		mDataLoader = new DataLoader();
		mTouchListener = scope.main().getTouchAttacher();
		mTouchListener.setPullableView(mListcontrol, this);
		mDataLoader.execute();
	}

	@Override
	public Object databaseHelper(Context context) {
		return new ResPartner(context);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
		inflater.inflate(R.menu.menu_partners, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		PartnersDetail partner = new PartnersDetail();
		startFragment(partner, true);
		return super.onOptionsItemSelected(item);
	}

	class DataLoader extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			scope.main().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (db().isEmptyTable()) {
						scope.main().requestSync(PartnersProvider.AUTHORITY);
					}
					mListRecords.clear();
					mListRecords.addAll(db().select());
				}
			});
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mListcontrol.setCustomView(R.layout.partners_list_item);
			mListcontrol.initListControl(mListRecords);
			Log.e("Size", mListRecords.size() + "");
			OControls.setGone(mView, R.id.loadingProgress);
		}
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		List<DrawerItem> menu = new ArrayList<DrawerItem>();
		menu.add(new DrawerItem(TAG, "Detail", true));
		menu.add(new DrawerItem(TAG, "Partner", 0, 0, object("partners")));
		return menu;
	}

	private Fragment object(String key) {
		Partners partners = new Partners();
		Bundle args = new Bundle();
		args.putString("partners", key);
		partners.setArguments(args);
		return partners;
	}

	@Override
	public void onPullStarted(View arg0) {
		scope.main().requestSync(PartnersProvider.AUTHORITY);
	}

	@Override
	public void onRowItemClick(int position, View view, ODataRow row) {
		PartnersDetail partner = new PartnersDetail();
		Bundle arg = new Bundle();
		arg.putAll(row.getPrimaryBundleData());
		partner.setArguments(arg);
		startFragment(partner, true);
	}

	@Override
	public void onResume() {
		super.onResume();
		scope.main().registerReceiver(mSyncFinishReceiver,
				new IntentFilter(SyncFinishReceiver.SYNC_FINISH));
	}

	@Override
	public void onPause() {
		super.onPause();
		scope.main().unregisterReceiver(mSyncFinishReceiver);
	}

	SyncFinishReceiver mSyncFinishReceiver = new SyncFinishReceiver() {
		@Override
		public void onReceive(Context context, android.content.Intent intent) {
			scope.main().refreshDrawer(TAG);
			mTouchListener.setPullComplete();
			if (mDataLoader != null) {
				mDataLoader.cancel(true);
			}
			mDataLoader = new DataLoader();
			mDataLoader.execute();
		}
	};
}
