package com.odoo.addons.partners;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import android.widgets.SwipeRefreshLayout.OnRefreshListener;

import com.odoo.R;
import com.odoo.base.res.ResPartner;
import com.odoo.base.res.providers.partners.PartnersProvider;
import com.odoo.orm.OColumn;
import com.odoo.support.fragment.BaseFragment;
import com.odoo.support.fragment.OnSearchViewChangeListener;
import com.odoo.support.fragment.SyncStatusObserverListener;
import com.odoo.support.listview.OCursorListAdapter;
import com.odoo.support.listview.OCursorListAdapter.OnRowViewClickListener;
import com.odoo.util.OControls;
import com.odoo.util.drawer.DrawerItem;
import com.odoo.util.logger.OLog;

public class PartnersCursorLoader extends BaseFragment implements
		OnRefreshListener, LoaderManager.LoaderCallbacks<Cursor>,
		SyncStatusObserverListener, OnSearchViewChangeListener,
		OnItemClickListener, OnRowViewClickListener {
	public static final String TAG = Partners.class.getSimpleName();
	public static final String KEY = "partner_type";
	private Type mCurrentType = Type.Companies;
	private View mView = null;

	public enum Type {
		Customers, Suppliers, Companies
	}

	/**
	 * cursor loader
	 */
	private OCursorListAdapter mAdapter;
	private ListView listView = null;
	// If non-null, this is the current filter the user has provided.
	private String mCurFilter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		setHasSyncStatusObserver(TAG, this, db());
		initArgs();
		return inflater.inflate(R.layout.partners_listview, container, false);
	}

	private void initArgs() {
		Bundle bundle = getArguments();
		if (bundle.containsKey(KEY)) {
			mCurrentType = Type.valueOf(bundle.getString(KEY));
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mView = view;
		setHasSwipeRefreshView(view, R.id.swipe_container, this);
		listView = (ListView) view.findViewById(R.id.partners_listView);
		mAdapter = new OCursorListAdapter(getActivity(), null,
				R.layout.partners_list_item);
		mAdapter.setOnRowViewClickListener(R.id.imgUserProfilePicture, this);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(this);
		scope.main().setActionbarAutoHide(listView);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
		inflater.inflate(R.menu.menu_partners, menu);
		setHasSearchView(this, menu, R.id.menu_partner_search);
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

	@Override
	public Object databaseHelper(Context context) {
		return new ResPartner(context);
	}

	private int count(Context context, Type type) {
		Object[] args = new Object[] { true };
		return new ResPartner(context).count(getWhere(type) + " = ? ", args);
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		List<DrawerItem> menu = new ArrayList<DrawerItem>();
		menu.add(new DrawerItem(TAG, "Companies",
				count(context, Type.Companies), R.drawable.ic_action_company,
				object(Type.Companies)));
		menu.add(new DrawerItem(TAG, "Customers",
				count(context, Type.Customers), R.drawable.ic_action_user,
				object(Type.Customers)));
		menu.add(new DrawerItem(TAG, "Suppliers",
				count(context, Type.Suppliers), R.drawable.ic_action_suppliers,
				object(Type.Suppliers)));
		return menu;
	}

	private Fragment object(Type type) {
		PartnersCursorLoader partners = new PartnersCursorLoader();
		Bundle args = new Bundle();
		args.putString(KEY, type.toString());
		partners.setArguments(args);
		return partners;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		if (db().isEmptyTable()) {
			scope.main().requestSync(PartnersProvider.AUTHORITY);
			setSwipeRefreshing(true);
		}
		String selection = null;
		String[] args = null;
		if (mCurFilter != null) {
			selection = " name like ? and " + getWhere(mCurrentType) + " = ?";
			args = new String[] { "%" + mCurFilter + "%", "true" };
		} else {
			selection = getWhere(mCurrentType) + " = ?";
			args = new String[] { "true" };
		}
		return new CursorLoader(getActivity(), db().uri(), new String[] {
				"image_small", "name", "email", "city", "country_id.name" },
				selection, args, OColumn.ROW_ID + " DESC");
	}

	private String getWhere(Type type) {
		String where = null;
		switch (type) {
		case Companies:
			where = "is_company";
			break;
		case Customers:
			where = "customer";
			break;
		case Suppliers:
			where = "supplier";
			break;
		}
		return where;

	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		mAdapter.changeCursor(cursor);
		OControls.setGone(mView, R.id.loadingProgress);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.changeCursor(null);
	}

	@Override
	public void onRefresh() {
		if (app().inNetwork()) {
			scope.main().requestSync(PartnersProvider.AUTHORITY);
		} else {
			hideRefreshingProgress();
			Toast.makeText(getActivity(), "No Connection", Toast.LENGTH_LONG)
					.show();
		}
	}

	@Override
	public void onStatusChange(Boolean refreshing) {
		if (!refreshing) {
			hideRefreshingProgress();
		}
	}

	@Override
	public boolean onSearchViewTextChange(String newFilter) {

		if (mCurFilter == null && newFilter == null)
			return false;
		if (mCurFilter != null && mCurFilter.equals(newFilter))
			return false;

		mCurFilter = newFilter;
		getLoaderManager().restartLoader(0, null, this);
		return true;
	}

	@Override
	public void onSearchViewClose() {
		// nothing to do
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Cursor c = (Cursor) mAdapter.getItem(position);
		Bundle bundle = new Bundle();
		bundle.putInt(OColumn.ROW_ID,
				c.getInt(c.getColumnIndex(OColumn.ROW_ID)));
		bundle.putInt("id", c.getInt(c.getColumnIndex("id")));
		PartnersDetail partner = new PartnersDetail();
		partner.setArguments(bundle);
		startFragment(partner, true);
	}

	@Override
	public void onRowViewClick(int position, Cursor cursor, View view,
			View parent) {
		OLog.log(cursor.getString(cursor.getColumnIndex("name")));
	}
}
