package com.odoo.addons.tripdestination;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import com.odoo.addons.Equipment.providers.CmmsEquipment;
import com.odoo.addons.tripdestination.providers.CmmsTripDestination;
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

/**
 * Created by Sylwek on 01/05/2016.
 */
public class TripDestination extends BaseFragment implements ISyncStatusObserverListener,
        LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener, IOnSearchViewChangeListener, View.OnClickListener,
        AdapterView.OnItemClickListener,OCursorListAdapter.OnViewBindListener {
    //private View mView;
    public static final String KEY = TripDestination.class.getSimpleName();
    ///////////////////////////////////////
    public static final String TAG = TripDestination.class.getSimpleName();
    private String mCurFilter = null;
    private ListView mPartnersList = null;
    private boolean syncRequested = false;
    private View mView;
    private ListView listView;
    private OCursorListAdapter listAdapter;
    private ODataRow record = null;
    //private CmmsIntervention cmmsIntervention;
    private Bundle extra;

    @Override
    public List<ODrawerItem> drawerMenus(Context context) {
        List<ODrawerItem> menu = new ArrayList<>();
        menu.add(new ODrawerItem(TAG).setTitle("Trip Destination").setInstance(new TripDestination()));
        return menu;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        hideFab();
        setHasOptionsMenu(true);
        setHasSyncStatusObserver(TAG, this, db());
        return inflater.inflate(R.layout.common_listview, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasSyncStatusObserver(TAG, this, db());


        setHasSwipeRefreshView(view, R.id.swipe_container, this);
        mView = view;
        // mType = Type.valueOf(getArguments().getString(EXTRA_KEY_TYPE));
        mPartnersList = (ListView) view.findViewById(R.id.listview);
        listAdapter = new OCursorListAdapter(getActivity(), null, R.layout.tripdestination_row_item);
        listAdapter.setOnViewBindListener(this);
        //   listAdapter.setHasSectionIndexers(true, "name");
        mPartnersList.setAdapter(listAdapter);
        mPartnersList.setFastScrollAlwaysVisible(true);
        mPartnersList.setOnItemClickListener(this);
        setHasSyncStatusObserver(KEY, this, db());
        // setHasFloatingButton(view, R.id.fabButton, mPartnersList, this);
        getLoaderManager().initLoader(0, null, this);
        hideFab();
        // Log.i("oVC", "intervention OnViewCreated");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i("oLF", "on Load Finished started trip dest");
        listAdapter.changeCursor(data);
        if (data.getCount() > 0) {
            OControls.setGone(mView, R.id.loadingProgress);
            OControls.setVisible(mView, R.id.swipe_container);
            OControls.setGone(mView, R.id.customer_no_items);
            setHasSwipeRefreshView(mView, R.id.swipe_container, TripDestination.this);
        } else {
            OControls.setGone(mView, R.id.loadingProgress);
            OControls.setGone(mView, R.id.swipe_container);
            OControls.setVisible(mView, R.id.customer_no_items);
            setHasSwipeRefreshView(mView, R.id.customer_no_items,  TripDestination.this);
            OControls.setImage(mView, R.id.icon, R.drawable.ic_action_customers);
            OControls.setText(mView, R.id.title, "no  Trip Destinations found");
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
    public Class<CmmsTripDestination> database() {
        Log.i("cc","Class cmms  TripDestination");
        return CmmsTripDestination.class;
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
            // getLoaderManager().restartLoader(0, null, this);
        }
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i("OC", "started onCreateLoader");
        return new CursorLoader(getActivity(), db().uri(), null, null, null, "order1");
    }

    private void loadActivity(ODataRow row) {
        Bundle data = null;
        if (row != null) {
            data = row.getPrimaryBundleData();
        }
        IntentUtils.startActivity(getActivity(), TripDestinationDetails.class, data);
    }



    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i("oLR","on Load Reset started  TripDestination");
        listAdapter.changeCursor(null);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ODataRow row = OCursorUtils.toDatarow((Cursor) listAdapter.getItem(position));
        loadActivity(row);
    }

    @Override
    public void onRefresh() {
        if (inNetwork()) {
            parent().sync().requestSync(CmmsTripDestination.AUTHORITY);
        }
    }

    @Override
    public void onViewBind(View view, Cursor cursor, ODataRow row) {
try{
//        Log.i("oVB","on View Bind started inervention");
    //int id = getResources().getIdentifier("com.odoo:drawable/complete.png" , null, null);
    CmmsEquipment cmmsEquipment = new CmmsEquipment(getContext(),null);
    ODataRow oDataRowEquipment = cmmsEquipment.browse(row.getInt("equipment_id"));


//    String distanceraw= row.getString("distance");
//    String timeRaw = row.getString("driving_time");

    OControls.setText(view, R.id.item_txtName, oDataRowEquipment.get("name"));
    OControls.setText(view, R.id.item_type, oDataRowEquipment.get("type"));
//    OControls.setText(view, R.id.item_distance, distanceraw);
//    OControls.setText(view, R.id.item_time, timeRaw);


}
catch (Exception e ){}
            Bitmap img;

            if (!row.getString("action").equals("false")) {
                img = BitmapUtils.getAlphabetImage(getActivity(), "A");
                OControls.setImage(view, R.id.image_small_action, img);
            }
            Bitmap img1;
            if (!row.getString("installation").equals("false")) {
                img1 = BitmapUtils.getAlphabetImage(getActivity(), "I");
                OControls.setImage(view, R.id.image_small_install, img1);
            }
            Bitmap img2;
            if (!row.getString("training").equals("false")) {
                img2 = BitmapUtils.getAlphabetImage(getActivity(), "T");
                OControls.setImage(view, R.id.image_small_training, img2);
            }
            Bitmap img3;
            if (!row.getString("loler").equals("false")) {
                img3 = BitmapUtils.getAlphabetImage(getActivity(), "L");
                OControls.setImage(view, R.id.image_small_loler, img3);
            }
        Bitmap img4;
        if (!row.getString("pick_up").equals("false")) {
            img4 = BitmapUtils.getAlphabetImage(getActivity(), "P");
            OControls.setImage(view, R.id.image_small_pick_up, img4);
        }
        Bitmap img5;
        if (!row.getString("replacement").equals("false")) {
            img5 = BitmapUtils.getAlphabetImage(getActivity(), "R");
            OControls.setImage(view, R.id.image_small_replacement, img5);
        }


        String current_state = row.getString("state");
        switch(current_state){
            case "1":
                OControls.setImage(view,R.id.imageState,R.drawable.notstarted);
                break;
            case "2":
                OControls.setImage(view,R.id.imageState,R.drawable.driving);
                break;
            case "3":
                OControls.setImage(view,R.id.imageState,R.drawable.working);
                break;
            case "4":
                OControls.setImage(view,R.id.imageState,R.drawable.complete);
                break;
            case "5":
                OControls.setImage(view,R.id.imageState,R.drawable.incomplete);

        }

    }
}

