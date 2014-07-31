package com.odoo.addons.partners;

import java.util.ArrayList;
import java.util.List;

import odoo.controls.OList;
import odoo.controls.OList.OnListBottomReachedListener;
import odoo.controls.OList.OnRowClickListener;
import android.content.Context;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.odoo.R;
import com.odoo.addons.partners.providers.partners.PartnersProvider;
import com.odoo.base.res.ResPartner;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OModel;
import com.odoo.receivers.SyncFinishReceiver;
import com.odoo.support.AppScope;
import com.odoo.support.BaseFragment;
import com.odoo.util.OControls;
import com.odoo.util.drawer.DrawerItem;
import com.openerp.OETouchListener;
import com.openerp.OETouchListener.OnPullListener;

public class Partners extends BaseFragment implements OnRowClickListener,
		OnPullListener, OnListBottomReachedListener {

	public static final String TAG = Partners.class.getSimpleName();
	public static final String KEY = "partner_type";

	private View mView = null;
	private List<ODataRow> mListRecords = new ArrayList<ODataRow>();
	private OList mListcontrol = null;
	private OETouchListener mTouchListener = null;
	private DataLoader mDataLoader = null;
	private Integer mLimit = 10;
	private Integer mLastPosition = -1;

	public enum Type {
		Customers, Suppliers, Companies
	}

	private Type mCurrentType = Type.Companies;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		scope = new AppScope(this);
		initArgs();
		mView = inflater.inflate(R.layout.partners_list, container, false);
		init();
		return mView;
	}

	private void initArgs() {
		Bundle bundle = getArguments();
		if (bundle.containsKey(KEY)) {
			mCurrentType = Type.valueOf(bundle.getString(KEY));
		}
	}

	private void init() {
		mListcontrol = (OList) mView.findViewById(R.id.listRecords);
		mListcontrol.setOnRowClickListener(this);
		mListcontrol.setOnListBottomReachedListener(this);
		mTouchListener = scope.main().getTouchAttacher();
		mTouchListener.setPullableView(mListcontrol, this);
		if (mLastPosition == -1) {
			mDataLoader = new DataLoader(0);
			mDataLoader.execute();
		} else {
			showData();
		}
	}

	private void showData() {
		mListcontrol.setCustomView(R.layout.partners_list_item);
		mListcontrol.initListControl(mListRecords);
		OControls.setGone(mView, R.id.loadingProgress);
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
		SearchView mSearchView = (SearchView) menu.findItem(
				R.id.menu_partner_search).getActionView();
		if (mListcontrol != null)
			mSearchView.setOnQueryTextListener(mListcontrol.getQueryListener());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_partner_create:
			PartnersDetail partner = new PartnersDetail();
			startFragment(partner, true);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	class DataLoader extends AsyncTask<Void, Void, Void> {
		Integer mOffset = 0;

		public DataLoader(Integer offset) {
			mOffset = offset;
		}

		@Override
		protected Void doInBackground(Void... params) {
			scope.main().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (db().isEmptyTable()) {
						scope.main().requestSync(PartnersProvider.AUTHORITY);
					}
					OModel model = db();
					model.setOffset(mOffset);
					if (mOffset == 0)
						mListRecords.clear();
					Object[] args = new Object[] { true };
					mListRecords.addAll(model
							.setLimit(mLimit)
							.setOffset(mOffset)
							.select(getWhere(mCurrentType), args, null, null,
									"local_id DESC"));
					mListcontrol.setRecordOffset(model.getNextOffset());
				}
			});
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			showData();
		}
	}

	private String getWhere(Type type) {
		String where = null;
		switch (type) {
		case Companies:
			where = "is_company = ?";
			break;
		case Customers:
			where = "customer = ?";
			break;
		case Suppliers:
			where = "supplier = ?";
			break;
		}
		return where;

	}

	private int count(Context context, Type type) {
		String where = getWhere(type);
		Object[] args = new Object[] { true };
		return new ResPartner(context).count(where, args);
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		List<DrawerItem> menu = new ArrayList<DrawerItem>();
		menu.add(new DrawerItem(TAG, "Partners", true));
		menu.add(new DrawerItem(TAG, "Companies",
				count(context, Type.Companies), 0, object(Type.Companies)));
		menu.add(new DrawerItem(TAG, "Customers",
				count(context, Type.Customers), 0, object(Type.Customers)));
		menu.add(new DrawerItem(TAG, "Suppliers",
				count(context, Type.Suppliers), 0, object(Type.Suppliers)));
		return menu;
	}

	private Fragment object(Type type) {
		Partners partners = new Partners();
		Bundle args = new Bundle();
		args.putString(KEY, type.toString());
		partners.setArguments(args);
		return partners;
	}

	@Override
	public void onPullStarted(View arg0) {
		scope.main().requestSync(PartnersProvider.AUTHORITY);
	}

	@Override
	public void onRowItemClick(int position, View view, ODataRow row) {
		mLastPosition = position;
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
			mDataLoader = new DataLoader(0);
			mDataLoader.execute();
		}
	};

	@Override
	public void onBottomReached(Integer record_limit, Integer record_offset) {
		if (mDataLoader != null) {
			mDataLoader.cancel(true);
		}
		mDataLoader = new DataLoader(record_offset);
		mDataLoader.execute();
	}

	@Override
	public Boolean showLoader() {
		return true;
	}
}
