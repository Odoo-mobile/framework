package com.odoo.addons.trip;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.odoo.App;
import com.odoo.R;
import com.odoo.addons.Equipment.Equipment;
import com.odoo.addons.Equipment.providers.CmmsEquipment;
import com.odoo.addons.intervention.providers.CmmsIntervention;
import com.odoo.addons.preventive.providers.CmmsPreventive;
import com.odoo.addons.trip.providers.CmmsTrips;
import com.odoo.addons.tripdestination.TripDestination;
import com.odoo.addons.tripdestination.providers.CmmsTripDestination;
import com.odoo.base.addons.ir.feature.OFileManager;
import com.odoo.base.addons.res.ResCompany;
import com.odoo.base.addons.res.ResUsers;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.OSQLite;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.support.OdooCompatActivity;
import com.odoo.core.support.list.OCursorListAdapter;
import com.odoo.core.utils.BitmapUtils;
import com.odoo.core.utils.OAppBarUtils;
import com.odoo.core.utils.OCursorUtils;
import com.odoo.core.utils.OStringColorUtil;
import com.odoo.widgets.parallax.ParallaxScrollView;

import java.util.List;

import odoo.controls.OField;
import odoo.controls.OForm;

/**
 * Created by Sylwek on 02/05/2016.
 */
public class TripDetails extends OdooCompatActivity
        implements View.OnClickListener, OField.IOnFieldValueChangeListener {
    public static final String TAG = TripDetails.class.getSimpleName();
    private final String KEY_MODE = "key_edit_mode";
    private final String KEY_NEW_IMAGE = "key_new_image";
    private ActionBar actionBar;
    private Bundle extras;
    private CmmsTrips cmmsTrips;
    private ODataRow record = null;
    private ParallaxScrollView parallaxScrollView;
    private ImageView userImage = null, captureImage = null;
    private TextView mTitleView = null;
    private OForm mForm;
    private App app;
    private Boolean mEditMode = false;
    private CmmsTripDestination cmmsTripDestination;
    private List<ODataRow> tripDestinations;
    private Menu mMenu;
    private OFileManager fileManager;
    private String newImage = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.equipment_detail);
//        cmmsEquipment = new CmmsEquipment(this, null);
//        extras = getIntent().getExtras();
//        record = cmmsEquipment.browse(extras.getInt(OColumn.ROW_ID));
//        OForm mform  = (OForm) findViewById(R.id.equipmentForm);
//       // mform.setEditable(true);
//        mform.initForm(record);
//
//        newImage = "fdsfdsfds";

        super.onCreate(savedInstanceState);

        setContentView(R.layout.trip_details);
        OAppBarUtils.setAppBar(this, false);
        fileManager = new OFileManager(this);
        actionBar = getSupportActionBar();
        actionBar.setTitle("");
        if (savedInstanceState != null) {
            mEditMode = savedInstanceState.getBoolean(KEY_MODE);
            newImage = savedInstanceState.getString(KEY_NEW_IMAGE);
        }
        app = (App) getApplicationContext();
        parallaxScrollView = (ParallaxScrollView) findViewById(com.odoo.R.id.parallaxScrollView);
        parallaxScrollView.setActionBar(actionBar);
        userImage = (ImageView) findViewById(android.R.id.icon);
        mTitleView = (TextView) findViewById(android.R.id.title);
        cmmsTrips = new CmmsTrips(this, null);
        extras = getIntent().getExtras();
        if (extras == null)
            mEditMode = true;
        // mEditMode = true;

       // cmmsTripDestination = new CmmsTripDestination(getApplicationContext(),null);
        setupActionBar();



    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_MODE, mEditMode);
        outState.putString(KEY_NEW_IMAGE, newImage);
    }

    private void setupActionBar() {
        if (extras == null) {
            setMode(mEditMode);
            userImage.setColorFilter(Color.parseColor("#ffffff"));
            mForm.setEditable(mEditMode);
            mForm.initForm(null);
        } else {
            int rowId = extras.getInt(OColumn.ROW_ID);
            record = cmmsTrips.browse(rowId);
            getInterventions();
            // record.put("full_address", resPartner.getAddress(record));
            //  checkControls();
            setMode(mEditMode);
            mForm.setEditable(mEditMode);
            mForm.initForm(record);
        final ListView yourListView = (ListView) findViewById(R.id.list);

// get data from the table by the ListAdapter
        ListAdapter customAdapter = new ListAdapter(this, R.layout.tripdestination_row_item, tripDestinations);

        yourListView.setAdapter(customAdapter);
            yourListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {
                    Toast.makeText(getApplicationContext(),String.valueOf(position),Toast.LENGTH_SHORT);

                }
            });


            mTitleView.setText(record.getString("name"));
//            setCustomerImage();
//            if (record.getInt("id") != 0 && record.getString("large_image").equals("false")) {
//                BigImageLoader bigImageLoader = new BigImageLoader();
//                bigImageLoader.execute(record.getInt("id"));
//            }
          //  TripDestination tripDestination = new TripDestination();

        }
    }

    private void getInterventions()
    {
        cmmsTripDestination = new CmmsTripDestination(getBaseContext(),null);
        List<ODataRow> test;
        OModel oModelDestination = new OModel(getApplicationContext(),cmmsTripDestination.getModelName(),null);
        String stest = record.getString("_id");
        try {
            tripDestinations = oModelDestination.query("select * from " + cmmsTripDestination.getTableName()+ " where trip = " + stest);
           // tripDestinations = oModelDestination.
        } catch (Exception e)
        {
            Log.e("error",e.getMessage());
        }
     //   ResUsers resUsers = new ResUsers(getApplicationContext(),null);
       // ODataRow oDataRow = resUsers.browse(record.getInt(record.getString("user1")));



//        CmmsIntervention cmmsIntervention = new CmmsIntervention(getApplicationContext(),null);
//        CmmsEquipment cmmsEquipment = new CmmsEquipment(getApplicationContext(),null);
//        CmmsPreventive cmmsPreventive = new CmmsPreventive(getApplicationContext(),null);
//        ResCompany resCompany = new ResCompany(getApplicationContext(),null);
//        OModel oModelEquipment = new OModel(getApplicationContext(),cmmsEquipment.getModelName(),null);
//        OModel oModelIntervention = new OModel(getApplicationContext(),cmmsIntervention.getModelName(),null);
//        OModel oModelCompany = new OModel(getApplicationContext(),resCompany.getModelName(),null);
//        OModel oModelPreventive = new OModel(getApplicationContext(), cmmsPreventive.getModelName(),null);
//
//        List<ODataRow> dataRowsIntervention = oModelIntervention.query("select equipment_id,priority from "+ cmmsIntervention.getTableName()+ " where state = 'draft'");
//        String[] equipment_ids = new String[dataRowsIntervention.size()];
//      for(int i = 0;i < dataRowsIntervention.size();i++) {
//          equipment_ids[i] = dataRowsIntervention.get(i).get("equipment_id").toString();
//         // oModelEquipment.
//
//      }
//        //List<ODataRow> dataRowsEquipment = oModelEquipment.
//        List<ODataRow> dataRows = oModelEquipment.query("select id,name,type,customer,active from " + cmmsEquipment.getTableName() );
//       // mTitleView.setText(dataRows.size());
//        cmmsEquipment = null;


    }
    private void setMode(Boolean edit) {
//        if (mMenu != null) {
//            mMenu.findItem(com.odoo.R.id.menu_equipment_detail_more).setVisible(!edit);
//            mMenu.findItem(com.odoo.R.id.menu_equipment_edit).setVisible(!edit);
//            mMenu.findItem(com.odoo.R.id.menu_equipment_save).setVisible(edit);
//            mMenu.findItem(com.odoo.R.id.menu_equipment_cancel).setVisible(edit);
//        }
        int color = Color.DKGRAY;
        if (record != null) {
            color = OStringColorUtil.getStringColor(this, record.getString("name"));
        }
        if (edit) {
//            if (extras != null)
//                actionBar.setTitle(com.odoo.R.string.label_edit);
//            else
//                actionBar.setTitle(com.odoo.R.string.label_new);
//            actionBar.setBackgroundDrawable(new ColorDrawable(color));
//            mForm = (OForm) findViewById(R.id.tripForm);
//            captureImage = (ImageView) findViewById(com.odoo.R.id.captureImage);
//            captureImage.setOnClickListener(this);
//            userImage = (ImageView) findViewById(android.R.id.icon1);
//            findViewById(com.odoo.R.id.parallaxScrollView).setVisibility(View.GONE);
//            findViewById(com.odoo.R.id.equipmentScrollViewEdit).setVisibility(View.VISIBLE);
            // OField is_company = (OField) findViewById(R.id.is_company_edit);
            //  is_company.setOnValueChangeListener(this);
        } else {
            actionBar.setBackgroundDrawable(getResources().getDrawable(com.odoo.R.drawable.action_bar_shade));
            userImage = (ImageView) findViewById(android.R.id.icon);
            mForm = (OForm) findViewById(R.id.tripForm);
         //   findViewById(com.odoo.R.id.equipmentScrollViewEdit).setVisibility(View.GONE);
         //   findViewById(com.odoo.R.id.parallaxScrollView).setVisibility(View.VISIBLE);
        }
       // setColor(color);
    }
//    private void setColor(int color) {
//        FrameLayout frameLayout = (FrameLayout) findViewById(com.odoo.R.id.parallax_view);
//        frameLayout.setBackgroundColor(color);
//        parallaxScrollView.setParallaxOverLayColor(color);
//        parallaxScrollView.setBackgroundColor(color);
//        mForm.setIconTintColor(color);
//        findViewById(com.odoo.R.id.parallax_view).setBackgroundColor(color);
//        findViewById(com.odoo.R.id.parallax_view_edit).setBackgroundColor(color);
//        findViewById(com.odoo.R.id.equipmentScrollViewEdit).setBackgroundColor(color);
//        if (captureImage != null) {
//            GradientDrawable shapeDrawable =
//                    (GradientDrawable) getResources().getDrawable(com.odoo.R.drawable.circle_mask_primary);
//            shapeDrawable.setColor(color);
//            captureImage.setBackgroundDrawable(shapeDrawable);
//        }
//    }
    @Override
    public void onFieldValueChange(OField field, Object value) {

    }

    @Override
    public void onClick(View v) {

    }
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case com.odoo.R.id.menu_equipment_save:
//                OValues values = mForm.getValues();
//                if (values != null) {
////                    if (newImage != null) {
////                        values.put("image_small", newImage);
////                        values.put("large_image", newImage);
////                    }
//                    if (record != null) {
//                        cmmsTrips.update(record.getInt(OColumn.ROW_ID), values);
//                        Toast.makeText(this, com.odoo.R.string.toast_information_saved, Toast.LENGTH_LONG).show();
//                        mEditMode = !mEditMode;
//                        setupActionBar();
//                    } else {
//                        // values.put("customer", "true");
//                        final int row_id = cmmsTrips.insert(values);
//                        if (row_id != OModel.INVALID_ROW_ID) {
//                            finish();
//                        }
//                    }
//                }
//                break;
//            case com.odoo.R.id.menu_equipment_cancel:
//                if (record == null) {
//                    finish();
//                    return true;
//                }
//            case com.odoo.R.id.menu_equipment_edit:
//                mEditMode = !mEditMode;
//                setMode(mEditMode);
//                mForm.setEditable(mEditMode);
//                mForm.initForm(record);
//                setCustomerImage();
//                break;
//            case com.odoo.R.id.menu_equipment_share:
//                // ShareUtil.shareContact(this, record, true);
//                break;
//            case com.odoo.R.id.menu_equipment_import:
//                //  ShareUtil.shareContact(this, record, false);
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//
//            case com.odoo.R.id.captureImage:
//                fileManager.requestForFile(OFileManager.RequestType.IMAGE_OR_CAPTURE_IMAGE);
//                break;
//        }
//    }
//    private void setCustomerImage() {
//        if (!record.getString("photo").equals("false")) {
//            userImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            userImage.setColorFilter(null);
//            String base64 = newImage;
////            if (newImage == null) {
////                if (!record.getString("large_image").equals("false")) {
////                    base64 = record.getString("large_image");
////                } else {
////                    base64 = record.getString("image_small");
////                }
////            }
//            userImage.setImageBitmap(BitmapUtils.getBitmapImage(this, base64));
//        } else {
//            userImage.setColorFilter(Color.parseColor("#ffffff"));
//        }
//    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(com.odoo.R.menu.menu_equipment_detail, menu);
//        mMenu = menu;
//        setMode(mEditMode);
//        return true;
//    }

    public class ListAdapter extends ArrayAdapter<ODataRow> {

        public ListAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        public ListAdapter(Context context, int resource, List<ODataRow> items) {
            super(context, resource, items);
        }
        private String changeState(String currentState)
        {
            switch (currentState) {
                case "1":
                    return "2";
                case "2":
                    return "3";
                case "3":
                    return "4";
                case "4":
                    return "5";
                case "":
                    return "1";
            }
            return "";
        }
        private String reverseState(String currentState)
        {
            switch (currentState) {
                case "5":
                    return "4";
                case "4":
                    return "3";
                case "3":
                    return "2";
                case "2":
                    return "1";
            }
            return "";
        }
        private void setStateImage(ODataRow row )
        {

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.tripdestination_row_item, null);
            }

            final ODataRow row = getItem(position);


            if (row != null) {
                TextView name = (TextView) v.findViewById(R.id.item_txtName);
                TextView type = (TextView) v.findViewById(R.id.item_type);
//                TextView distance = (TextView) v.findViewById(R.id.item_distance);
//                TextView time = (TextView) v.findViewById(R.id.item_time);

                ImageView t = (ImageView) v.findViewById(R.id.image_small_loler);
                ImageView i = (ImageView) v.findViewById(R.id.image_small_install);
                ImageView a = (ImageView) v.findViewById(R.id.image_small_action);
                ImageView l = (ImageView) v.findViewById(R.id.image_small_loler);
                final ImageView state = (ImageView) v.findViewById(R.id.imageState);

                state.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Log.i("State img", "State onclick");
                        CmmsTripDestination cmmsTripDestination = new CmmsTripDestination(getContext(), null);
                        OModel oModelTripDest = new OModel(getContext(), cmmsTripDestination.getModelName(), null);
                      //  ODataRow oDataRow = oModelTripDest.query("select * from " + cmmsTripDestination.getTableName() + " where _id = " + row.getString("_id")).get(0);
                         ODataRow oDataRow = cmmsTripDestination.browse(row.getInt(OColumn.ROW_ID));
                        if(!oDataRow.getString("state").equals("5")) {

                       // ODataRow oDataRowDest = oModelTripDest.query("select * from " + cmmsTripDestination.getTableName() + " where _id = " + row.getString("_id")).get(0);
                        OValues oValues = oDataRow.toValues();
                        String updatedState = changeState(oDataRow.getString("state"));
                        oValues.put("state", updatedState);

                        boolean t = cmmsTripDestination.update(oDataRow.getInt(OColumn.ROW_ID), oValues);
                            Toast.makeText(getContext(),String.valueOf(t),Toast.LENGTH_SHORT).show();

                           // String current_state = row.getString("state");
                            switch(updatedState){
                                case "1":
                                    state.setImageDrawable(getResources().getDrawable(R.drawable.notstarted));
                                    break;
                                case "2":
                                    state.setImageDrawable(getResources().getDrawable(R.drawable.driving));
                                    break;
                                case "3":
                                    state.setImageDrawable(getResources().getDrawable(R.drawable.working));
                                    break;
                                case "4":
                                    state.setImageDrawable(getResources().getDrawable(R.drawable.complete));
                                    break;
                                case "5":
                                    state.setImageDrawable(getResources().getDrawable(R.drawable.incomplete));

                            }
                            state.postInvalidate();
                        // oModelTripDest.
                    }
                    }
                });
                state.setOnLongClickListener(new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        Log.i("State img", "State onLongClick");
                        CmmsTripDestination cmmsTripDestination = new CmmsTripDestination(getApplicationContext(), null);
                       // OModel oModelTripDest = new OModel(getApplicationContext(), cmmsTripDestination.getModelName(), null);
                       // ODataRow oDataRow = oModelTripDest.query("select * from " + cmmsTripDestination.getTableName() + " where _id = " + row.getString("_id")).get(0);
                        ODataRow oDataRow = cmmsTripDestination.browse(row.getInt(OColumn.ROW_ID));
                        if(!oDataRow.getString("state").equals("1")) {

                            // ODataRow oDataRowDest = oModelTripDest.query("select * from " + cmmsTripDestination.getTableName() + " where _id = " + row.getString("_id")).get(0);
                            OValues oValues = oDataRow.toValues();
                            String updatedState = reverseState(oDataRow.getString("state"));
                            oValues.put("state", updatedState);
                            cmmsTripDestination.update(row.getInt(OColumn.ROW_ID), oValues);
                            switch(updatedState){
                                case "1":
                                    state.setImageDrawable(getResources().getDrawable(R.drawable.notstarted));
                                    break;
                                case "2":
                                    state.setImageDrawable(getResources().getDrawable(R.drawable.driving));
                                    break;
                                case "3":
                                    state.setImageDrawable(getResources().getDrawable(R.drawable.working));
                                    break;
                                case "4":
                                    state.setImageDrawable(getResources().getDrawable(R.drawable.complete));
                                    break;
                                case "5":
                                    state.setImageDrawable(getResources().getDrawable(R.drawable.incomplete));

                            }
                            state.postInvalidate();
                            // oModelTripDest.
                        }
                        return true;
                    }
                });
                CmmsEquipment cmmsEquipment = new CmmsEquipment(getContext(),null);
                ODataRow oDataRowEquipment = cmmsEquipment.select(
                        new String[]{"name","type"},
                        "_id = ?",
                        new String[]{row.getString("equipment_id")}
                ).get(0); // always first element (should be only one)

                name.setText(oDataRowEquipment.getString("name"));
                type.setText(oDataRowEquipment.getString("type"));
//                distance.setText(row.getString("distance"));
//                time.setText(row.getString("time"));

                if (!row.getString("action").equals("false")) {
                   a.setImageBitmap(BitmapUtils.getAlphabetImage(getApplicationContext(), "A"));
                }

                if (!row.getString("installation").equals("false")) {
                    i.setImageBitmap(BitmapUtils.getAlphabetImage(getApplicationContext(), "I"));
                }
                if (!row.getString("training").equals("false")) {
                   t.setImageBitmap(BitmapUtils.getAlphabetImage(getApplicationContext(), "T"));
                }
                if (!row.getString("loler").equals("false")) {
                    l.setImageBitmap(BitmapUtils.getAlphabetImage(getApplicationContext(), "L"));
                }

                String current_state = row.getString("state");
                switch(current_state){
                    case "1":
                        state.setImageDrawable(getResources().getDrawable(R.drawable.notstarted));
                        break;
                    case "2":
                        state.setImageDrawable(getResources().getDrawable(R.drawable.driving));
                        break;
                    case "3":
                        state.setImageDrawable(getResources().getDrawable(R.drawable.working));
                        break;
                    case "4":
                        state.setImageDrawable(getResources().getDrawable(R.drawable.complete));
                        break;
                    case "5":
                        state.setImageDrawable(getResources().getDrawable(R.drawable.incomplete));

                }

            }

            return v;
        }

    }



}

