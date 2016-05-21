package com.odoo.addons.failure;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.odoo.R;
import com.odoo.base.addons.res.CmmsFailure;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.support.addons.fragment.BaseFragment;
import com.odoo.core.support.addons.fragment.IOnSearchViewChangeListener;
import com.odoo.core.support.addons.fragment.ISyncStatusObserverListener;
import com.odoo.core.support.drawer.ODrawerItem;
import com.odoo.core.support.list.OCursorListAdapter;
import com.odoo.core.utils.OControls;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sylwek on 05/12/2015.
 */
public class Failure extends BaseFragment implements ISyncStatusObserverListener,
        LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener, IOnSearchViewChangeListener, View.OnClickListener,
        AdapterView.OnItemClickListener,OCursorListAdapter.OnViewBindListener {
    //private View mView;
    private String mCurFilter = null;
    private ListView mPartnersList = null;
    private boolean syncRequested = false;
    private View mView;
    private ListView listView;
    private OCursorListAdapter listAdapter;

    ///////////////////////////////////////
    public static final String TAG = CmmsFailure.class.getSimpleName();
    @Override
    public List<ODrawerItem> drawerMenus(Context context) {
        List<ODrawerItem> menu = new ArrayList<>();
        menu.add(new ODrawerItem(TAG).setTitle("Failure").setInstance(new Failure()));
        return menu;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        setHasSyncStatusObserver(TAG, this, db());
        return inflater.inflate(R.layout.common_listview, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//    mView = view;
//    listView = (ListView) mView.findViewById(R.id.listview);
//    listAdapter = new OCursorListAdapter(getActivity(), null, android.R.layout.simple_list_item_1);
//    listView.setAdapter(listAdapter);
//    getLoaderManager().initLoader(0, null, this);
//    listAdapter.setOnViewBindListener(this);
//
//
//
        setHasSyncStatusObserver(TAG, this, db());
//    getLoaderManager().initLoader(0, null, this);
        setHasSwipeRefreshView(view, R.id.swipe_container, this);
        mView = view;
        // mType = Type.valueOf(getArguments().getString(EXTRA_KEY_TYPE));
        mPartnersList = (ListView) view.findViewById(R.id.listview);
        listAdapter = new OCursorListAdapter(getActivity(), null, R.layout.base_simple_list_item_1);
        listAdapter.setOnViewBindListener(this);
        //   listAdapter.setHasSectionIndexers(true, "name");
        mPartnersList.setAdapter(listAdapter);
        mPartnersList.setFastScrollAlwaysVisible(true);
        mPartnersList.setOnItemClickListener(this);
        setHasFloatingButton(view, R.id.fabButton, mPartnersList, this);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        listAdapter.changeCursor(data);
        if (data.getCount() > 0) {
            OControls.setGone(mView, R.id.loadingProgress);
            OControls.setVisible(mView, R.id.swipe_container);
            OControls.setGone(mView, R.id.customer_no_items);
            setHasSwipeRefreshView(mView, R.id.swipe_container, Failure.this);
        } else {
            OControls.setGone(mView, R.id.loadingProgress);
            OControls.setGone(mView, R.id.swipe_container);
            OControls.setVisible(mView, R.id.customer_no_items);
            setHasSwipeRefreshView(mView, R.id.customer_no_items, Failure.this);
            OControls.setImage(mView, R.id.icon, R.drawable.ic_action_customers);
            OControls.setText(mView, R.id.title,"no equipment found");
            OControls.setText(mView, R.id.subTitle, "");
        }
        if (db().isEmptyTable() && !syncRequested) {
            syncRequested = true;
            onRefresh();
            Log.i("db", "sync req");
        }
    }
    //    private void loadActivity(ODataRow row) {
//        Bundle data = null;
//        if (row != null) {
//            data = row.getPrimaryBundleData();
//        }
//        IntentUtils.startActivity(getActivity(), CmmsEquipment.class, data);
//    }
    @Override
    public Class<CmmsFailure> database() {
        return CmmsFailure.class;
    }

    @Override
    public boolean onSearchViewTextChange(String newFilter) {
        return false;
    }

    @Override
    public void onSearchViewClose() {

    }

    @Override
    public void onStatusChange(Boolean changed) {
        if(changed){
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.i("OC", "started onCreateLoader");
        return new CursorLoader(getActivity(), db().uri(), null, null, null,"name");
        //  Log.i("OC", "finishing onCreateLoader");

    }



    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        listAdapter.changeCursor(null);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onRefresh() {
        if (inNetwork()) {
            parent().sync().requestSync(CmmsFailure.AUTHORITY);
        }
    }

    @Override
    public void onViewBind(View view, Cursor cursor, ODataRow row) {

       OControls.setText(view, android.R.id.text1, row.getString("name"));
    }
}
