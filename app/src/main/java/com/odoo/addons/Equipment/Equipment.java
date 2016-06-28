package com.odoo.addons.Equipment;

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
import android.widget.Toast;

import com.odoo.R;
import com.odoo.addons.Equipment.providers.CmmsEquipment;
import com.odoo.addons.intervention.providers.CmmsIntervention;
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
 * Created by Sylwek on 02/12/2015.
 */
public class Equipment extends BaseFragment implements ISyncStatusObserverListener,
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
    public static final String TAG = CmmsEquipment.class.getSimpleName();
    @Override
    public List<ODrawerItem> drawerMenus(Context context) {
        List<ODrawerItem> menu = new ArrayList<>();
        menu.add(new ODrawerItem(TAG).setTitle("Equipment").setInstance(new Equipment()));
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
    listAdapter = new OCursorListAdapter(getActivity(), null, R.layout.equipment_row_item);
    listAdapter.setOnViewBindListener(this);
 //   listAdapter.setHasSectionIndexers(true, "name");
    mPartnersList.setAdapter(listAdapter);
    mPartnersList.setFastScrollAlwaysVisible(true);
    mPartnersList.setOnItemClickListener(this);
   // setHasFloatingButton(view, R.id.fabButton, mPartnersList, this);
    getLoaderManager().initLoader(0, null, this);
}
    public boolean check_itervention()
    {

    return true;
    }
@Override
public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    listAdapter.changeCursor(data);
    if (data.getCount() > 0) {
        OControls.setGone(mView, R.id.loadingProgress);
        OControls.setVisible(mView, R.id.swipe_container);
        OControls.setGone(mView, R.id.customer_no_items);
        setHasSwipeRefreshView(mView, R.id.swipe_container, Equipment.this);
    } else {
        OControls.setGone(mView, R.id.loadingProgress);
        OControls.setGone(mView, R.id.swipe_container);
        OControls.setVisible(mView, R.id.customer_no_items);
        setHasSwipeRefreshView(mView, R.id.customer_no_items, Equipment.this);
        OControls.setImage(mView, R.id.icon, R.drawable.ic_action_customers);
        OControls.setText(mView, R.id.title,"No Equipment Found");
        OControls.setText(mView, R.id.subTitle, "");
    }
    if (db().isEmptyTable() && !syncRequested) {
        syncRequested = true;
        onRefresh();
        Log.i("db","sync req");
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
    public Class<CmmsEquipment> database() {
        return CmmsEquipment.class;
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
        switch (v.getId()) {
            case R.id.fabButton:
                loadActivity(null);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ODataRow row = OCursorUtils.toDatarow((Cursor) listAdapter.getItem(position));
        loadActivity(row);

    }

    private void loadActivity(ODataRow row) {
        Bundle data = null;
        if (row != null) {
            data = row.getPrimaryBundleData();
        }
        IntentUtils.startActivity(getActivity(), EquipmentDetails.class, data);
    }

    @Override
    public void onRefresh() {
        if (inNetwork()) {
            parent().sync().requestSync(CmmsEquipment.AUTHORITY);
            //FIXME - inNetwork
        }
    }

    @Override
    public void onViewBind(View view, Cursor cursor, ODataRow row) {

//        OControls.setText(view, android.R.id.text1, row.getString("name"));
//        OControls.setText(view, android.R.id.text2, row.getString("type"));
//        try {
//            CmmsIntervention cmmsIntervention = new CmmsIntervention(getContext(), null);
//            List<ODataRow> rows = cmmsIntervention.select(
//                    new
//                            String[]{"name","motif","equipment_id"},
//                    "equipment_id = ?",
//                    new String[]{row.getString("id")}
//            );
//            for (ODataRow row1: rows)
//            {
//                if(row1 == null)
//                    Log.e("Row1 null", "");
//            }
//          //  ODataRow Interrow = cmmsIntervention.
//            if(cmmsIntervention == null)
//            {
//                Toast.makeText(getContext(),"Intervention null" , Toast.LENGTH_LONG).show();
//                Log.e("inter ","Intervention null");
//            }
//            if(rows == null)
//                Log.e("Row null", "");
//            Toast.makeText(getContext(),"Row null" , Toast.LENGTH_LONG).show();
//        }
//        catch (Exception e)
//        {
//            Log.e("exception- " , e.getMessage());
//            Toast.makeText(getContext(),"exception- " + e.getMessage() , Toast.LENGTH_LONG).show();
//        }
        Bitmap img;
        if (row.getString("image_small").equals("false")) {
            img = BitmapUtils.getAlphabetImage(getActivity(), row.getString("name"));
        } else {
            img = BitmapUtils.getBitmapImage(getActivity(), row.getString("image_small"));
        }
        OControls.setImage(view, R.id.image_small, img);
        OControls.setText(view, R.id.name, row.getString("name"));
        if(row.getString("type") != null) {
            OControls.setText(view, R.id.type, row.getString("type"));
        }
        else
            Log.i("TP","Type null");
//        OControls.setText(view, R.id.email, (row.getString("email").equals("false") ? " "
//                : row.getString("email")));
    }
}
