/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p/>
 * Created on 30/12/14 3:28 PM
 */
package com.odoo.addons.customers;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.odoo.R;
import com.odoo.base.addons.res.ResPartner;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.support.addons.fragment.BaseFragment;
import com.odoo.core.support.addons.fragment.IOnSearchViewChangeListener;
import com.odoo.core.support.addons.fragment.ISyncStatusObserverListener;
import com.odoo.core.support.drawer.ODrawerItem;
import com.odoo.core.support.list.OCursorListAdapter;
import com.odoo.core.utils.BitmapUtils;
import com.odoo.core.utils.IntentUtils;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.OCursorUtils;

import java.util.ArrayList;
import java.util.List;

public class Customers extends BaseFragment implements ISyncStatusObserverListener,
        LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener,
        OCursorListAdapter.OnViewBindListener, IOnSearchViewChangeListener, View.OnClickListener,
        AdapterView.OnItemClickListener {

    public static final String KEY = Customers.class.getSimpleName();
    public static final String EXTRA_KEY_TYPE = "extra_key_type";
    private View mView;
    private String mCurFilter = null;
    private OCursorListAdapter mAdapter = null;
    private boolean syncRequested = false;

    public enum Type {
        Customer, Supplier, Company
    }

    private Type mType = Type.Customer;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        setHasSyncStatusObserver(KEY, this, db());
        return inflater.inflate(R.layout.common_listview, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasSwipeRefreshView(view, R.id.swipe_container, this);
        mView = view;
        mType = Type.valueOf(getArguments().getString(EXTRA_KEY_TYPE));
        ListView mPartnersList = (ListView) view.findViewById(R.id.listview);
        mAdapter = new OCursorListAdapter(getActivity(), null, R.layout.customer_row_item);
        mAdapter.setOnViewBindListener(this);
        mAdapter.setHasSectionIndexers(true, "name");
        mPartnersList.setAdapter(mAdapter);
        mPartnersList.setFastScrollAlwaysVisible(true);
        mPartnersList.setOnItemClickListener(this);
        setHasFloatingButton(view, R.id.fabButton, mPartnersList, this);
        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    public void onViewBind(View view, Cursor cursor, ODataRow row) {
        Bitmap img;
        if (row.getString("image_small").equals("false")) {
            img = BitmapUtils.getAlphabetImage(getActivity(), row.getString("name"));
        } else {
            img = BitmapUtils.getBitmapImage(getActivity(), row.getString("image_small"));
        }
        OControls.setImage(view, R.id.image_small, img);
        OControls.setText(view, R.id.name, row.getString("name"));
        OControls.setText(view, R.id.company_name, (row.getString("company_name").equals("false"))
                ? "" : row.getString("company_name"));
        OControls.setText(view, R.id.email, (row.getString("email").equals("false") ? " "
                : row.getString("email")));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle data) {
        String where = "";
        List<String> args = new ArrayList<>();
        switch (mType) {
            case Customer:
                where = "customer = ?";
                break;
            case Supplier:
                where = "supplier = ?";
                break;
            case Company:
                where = "is_company = ?";
                break;
        }
        args.add("true");
        if (mCurFilter != null) {
            where += " and name like ? ";
            args.add(mCurFilter + "%");
        }
        String selection = (args.size() > 0) ? where : null;
        String[] selectionArgs = (args.size() > 0) ? args.toArray(new String[args.size()]) : null;
        return new CursorLoader(getActivity(), db().uri(),
                null, selection, selectionArgs, "name");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);
        if (data.getCount() > 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    OControls.setGone(mView, R.id.loadingProgress);
                    OControls.setVisible(mView, R.id.swipe_container);
                    OControls.setGone(mView, R.id.data_list_no_item);
                    setHasSwipeRefreshView(mView, R.id.swipe_container, Customers.this);
                }
            }, 500);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    OControls.setGone(mView, R.id.loadingProgress);
                    OControls.setGone(mView, R.id.swipe_container);
                    OControls.setVisible(mView, R.id.data_list_no_item);
                    setHasSwipeRefreshView(mView, R.id.data_list_no_item, Customers.this);
                    OControls.setImage(mView, R.id.icon, R.drawable.ic_action_customers);
                    OControls.setText(mView, R.id.title, _s(R.string.label_no_customer_found));
                    OControls.setText(mView, R.id.subTitle, "");
                }
            }, 500);
            if (db().isEmptyTable() && !syncRequested) {
                syncRequested = true;
                onRefresh();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    @Override
    public Class<ResPartner> database() {
        return ResPartner.class;
    }

    @Override
    public List<ODrawerItem> drawerMenus(Context context) {
        List<ODrawerItem> items = new ArrayList<>();
        items.add(new ODrawerItem(KEY).setTitle("Customers")
                .setIcon(R.drawable.ic_action_customers)
                .setExtra(extra(Type.Customer))
                .setInstance(new Customers()));
        items.add(new ODrawerItem(KEY).setTitle("Suppliers")
                .setIcon(R.drawable.ic_action_suppliers)
                .setExtra(extra(Type.Supplier))
                .setInstance(new Customers()));
        items.add(new ODrawerItem(KEY).setTitle("Companies")
                .setIcon(R.drawable.ic_action_company)
                .setExtra(extra(Type.Company))
                .setInstance(new Customers()));
        return items;
    }

    public Bundle extra(Type type) {
        Bundle extra = new Bundle();
        extra.putString(EXTRA_KEY_TYPE, type.toString());
        return extra;
    }


    @Override
    public void onStatusChange(Boolean refreshing) {
        // Sync Status
        getLoaderManager().restartLoader(0, null, this);
    }


    @Override
    public void onRefresh() {
        if (inNetwork()) {
            parent().sync().requestSync(ResPartner.AUTHORITY);
            setSwipeRefreshing(true);
        } else {
            hideRefreshingProgress();
            Toast.makeText(getActivity(), _s(R.string.toast_network_required), Toast.LENGTH_LONG)
                    .show();
        }
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSearchViewTextChange(String newFilter) {
        mCurFilter = newFilter;
        getLoaderManager().restartLoader(0, null, this);
        return true;
    }

    @Override
    public void onSearchViewClose() {
        // nothing to do
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fabButton:
                loadActivity(null);
                break;
        }
    }

    private void loadActivity(ODataRow row) {
        Bundle data = new Bundle();
        if (row != null) {
            data = row.getPrimaryBundleData();
        }
        data.putString(CustomerDetails.KEY_PARTNER_TYPE, mType.toString());
        IntentUtils.startActivity(getActivity(), CustomerDetails.class, data);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ODataRow row = OCursorUtils.toDatarow((Cursor) mAdapter.getItem(position));
        loadActivity(row);
    }
}
