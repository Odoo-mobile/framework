package com.odoo.addons.trip;

import android.content.Context;
import android.content.Intent;
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
import com.odoo.addons.trip.providers.CmmsTrips;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.support.addons.fragment.BaseFragment;
import com.odoo.core.support.addons.fragment.IOnSearchViewChangeListener;
import com.odoo.core.support.addons.fragment.ISyncStatusObserverListener;
import com.odoo.core.support.drawer.ODrawerItem;
import com.odoo.core.support.list.OCursorListAdapter;
import com.odoo.core.utils.IntentUtils;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.OCursorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sylwek on 01/05/2016.
 */
public class Trip extends BaseFragment implements ISyncStatusObserverListener,
        LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener, IOnSearchViewChangeListener, View.OnClickListener,
        AdapterView.OnItemClickListener,OCursorListAdapter.OnViewBindListener {
    ///////////////////////////////////////
    public static final String TAG = CmmsTrips.class.getSimpleName();
    //private View mView;
    private String mCurFilter = null;
    private ListView mPartnersList = null;
    private boolean syncRequested = false;
    private View mView;
    private ListView listView;
    private OCursorListAdapter listAdapter;
    private ODataRow record = null;
    private Bundle extra;

    @Override
    public List<ODrawerItem> drawerMenus(Context context) {
        List<ODrawerItem> menu = new ArrayList<>();
        menu.add(new ODrawerItem(TAG).setTitle("Trips").setInstance(new Trip()));
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


        setHasSyncStatusObserver(TAG, this, db());
//    getLoaderManager().initLoader(0, null, this);
        setHasSwipeRefreshView(view, R.id.swipe_container, this);
        mView = view;
        // mType = Type.valueOf(getArguments().getString(EXTRA_KEY_TYPE));
        mPartnersList = (ListView) view.findViewById(R.id.listview);
        listAdapter = new OCursorListAdapter(getActivity(), null, R.layout.trip_row_item);
        listAdapter.setOnViewBindListener(this);
        //   listAdapter.setHasSectionIndexers(true, "name");
        mPartnersList.setAdapter(listAdapter);
        mPartnersList.setFastScrollAlwaysVisible(true);
        mPartnersList.setOnItemClickListener(this);
        // setHasFloatingButton(view, R.id.fabButton, mPartnersList, this);
        getLoaderManager().initLoader(0, null, this);
        // Log.i("oVC", "intervention OnViewCreated");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i("oLF", "on Load Finished started inervention");
        listAdapter.changeCursor(data);
        if (data.getCount() > 0) {
            OControls.setGone(mView, R.id.loadingProgress);
            OControls.setVisible(mView, R.id.swipe_container);
            OControls.setGone(mView, R.id.customer_no_items);
            setHasSwipeRefreshView(mView, R.id.swipe_container, Trip.this);
        } else {
            OControls.setGone(mView, R.id.loadingProgress);
            OControls.setGone(mView, R.id.swipe_container);
            OControls.setVisible(mView, R.id.customer_no_items);
            setHasSwipeRefreshView(mView, R.id.customer_no_items, Trip.this);
            OControls.setImage(mView, R.id.icon, R.drawable.ic_action_customers);
            OControls.setText(mView, R.id.title, "no Trips found");
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
    public Class<CmmsTrips> database() {
        Log.i("cc","Class cmms trips");
        return CmmsTrips.class;
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
        if (changed) {
             getLoaderManager().restartLoader(0, null, this);
        }
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i("OC", "started onCreateLoader");
        return new CursorLoader(getActivity(), db().uri(), null, null, null, null);
    }




    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i("oLR","on Load Reset started trips");
        listAdapter.changeCursor(null);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ODataRow row = OCursorUtils.toDatarow((Cursor) listAdapter.getItem(position));
        //  loadActivity(row);

        //RecyclerViewActivity.startActivity(getContext());
        Intent trip = new Intent(getContext(), RecyclerViewActivity.class);
        trip.putExtra("trip_id", row.getString("_id"));
        startActivity(trip);
    }
    private void loadActivity(ODataRow row) {
        Bundle data = null;
        if (row != null) {
            data = row.getPrimaryBundleData();
        }
        IntentUtils.startActivity(getActivity(), TripDetails.class, data);
    }
    @Override
    public void onRefresh() {
        if (inNetwork()) {
            parent().sync().requestSync(CmmsTrips.AUTHORITY);
            //FIXME - inNetwork
        }
    }

    @Override
    public void onViewBind(View view, Cursor cursor, ODataRow row) {
        try {


      Log.i("oVB", "on View Bind started trips");
        if(row.getString("name") != null)
        OControls.setText(view, R.id.name, row.getString("name"));
        if(row.getString("state") != null) {
            OControls.setText(view, R.id.type, row.getString("state"));
        }
        } catch (Exception e) { Log.i("TP", "null");
        }
    }
}
