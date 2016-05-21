package com.odoo.addons.tripdestination;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.odoo.App;
import com.odoo.R;
import com.odoo.addons.tripdestination.providers.CmmsTripDestination;
import com.odoo.base.addons.ir.feature.OFileManager;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.support.OdooCompatActivity;
import com.odoo.core.utils.BitmapUtils;
import com.odoo.core.utils.OAppBarUtils;
import com.odoo.core.utils.OStringColorUtil;
import com.odoo.widgets.parallax.ParallaxScrollView;

import odoo.controls.OField;
import odoo.controls.OForm;

/**
 * Created by Sylwek on 20/05/2016.
 */
public class TripDestinationDetails extends OdooCompatActivity
        implements View.OnClickListener, OField.IOnFieldValueChangeListener {
    public static final String TAG = TripDestinationDetails.class.getSimpleName();
    private final String KEY_MODE = "key_edit_mode";
    private final String KEY_NEW_IMAGE = "key_new_image";
    private ActionBar actionBar;
    private Bundle extras;
  //  private CmmsEquipment cmmsTripDestination;
    private CmmsTripDestination cmmsTripDestination;
    private ODataRow record = null;
    private ParallaxScrollView parallaxScrollView;
    private ImageView userImage = null, captureImage = null;
    private TextView mTitleView = null;
    private OForm mForm;
    private App app;
    private Boolean mEditMode = false;
    private Menu mMenu;
    private OFileManager fileManager;
    private String newImage = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.equipment_detail);
//        cmmsTripDestination = new CmmsEquipment(this, null);
//        extras = getIntent().getExtras();
//        record = cmmsTripDestination.browse(extras.getInt(OColumn.ROW_ID));
//        OForm mform  = (OForm) findViewById(R.id.equipmentForm);
//       // mform.setEditable(true);
//        mform.initForm(record);
//
//        newImage = "fdsfdsfds";

        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_destination_details);
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
       // userImage = (ImageView) findViewById(android.R.id.icon);
        mTitleView = (TextView) findViewById(android.R.id.title);
        cmmsTripDestination = new CmmsTripDestination(this, null);
        extras = getIntent().getExtras();
        if (extras == null)
            mEditMode = true;
        // mEditMode = true;
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
           // userImage.setColorFilter(Color.parseColor("#ffffff"));
            mForm.setEditable(mEditMode);
            mForm.initForm(null);
        } else {
            int rowId = extras.getInt(OColumn.ROW_ID);
            record = cmmsTripDestination.browse(rowId);
            // record.put("full_address", resPartner.getAddress(record));
            //  checkControls();
            setMode(mEditMode);
            mForm.setEditable(mEditMode);
            mForm.initForm(record);
           // mTitleView.setText(record.getString("name"));
//            setCustomerImage();
//            if (record.getInt("id") != 0 && record.getString("large_image").equals("false")) {
//                BigImageLoader bigImageLoader = new BigImageLoader();
//                bigImageLoader.execute(record.getInt("id"));
//            }
        }
    }
    private void setMode(Boolean edit) {
        if (mMenu != null) {
            mMenu.findItem(R.id.menu_trip_destination_detail_more).setVisible(!edit);
            mMenu.findItem(com.odoo.R.id.menu_trip_destination_edit).setVisible(!edit);
            mMenu.findItem(com.odoo.R.id.menu_trip_destination_save).setVisible(edit);
            mMenu.findItem(com.odoo.R.id.menu_trip_destination_cancel).setVisible(edit);
        }
        int color = Color.DKGRAY;
        if (record != null) {
            color = OStringColorUtil.getStringColor(this, record.getString("name"));
        }
        if (edit) {
            if (extras != null)
                actionBar.setTitle(com.odoo.R.string.label_edit);
            else
                actionBar.setTitle(com.odoo.R.string.label_new);
            actionBar.setBackgroundDrawable(new ColorDrawable(color));
            mForm = (OForm) findViewById(R.id.tripDestinationsForm);
           // captureImage = (ImageView) findViewById(com.odoo.R.id.captureImage);
            //captureImage.setOnClickListener(this);
           // userImage = (ImageView) findViewById(android.R.id.icon1);
//            findViewById(com.odoo.R.id.parallaxScrollView).setVisibility(View.GONE);
//            findViewById(com.odoo.R.id.equipmentScrollViewEdit).setVisibility(View.VISIBLE);
            // OField is_company = (OField) findViewById(R.id.is_company_edit);
            //  is_company.setOnValueChangeListener(this);
        } else {
            actionBar.setBackgroundDrawable(getResources().getDrawable(com.odoo.R.drawable.action_bar_shade));
          //  userImage = (ImageView) findViewById(android.R.id.icon);
            mForm = (OForm) findViewById(R.id.tripDestinationsForm);
//            findViewById(com.odoo.R.id.equipmentScrollViewEdit).setVisibility(View.GONE);
//            findViewById(com.odoo.R.id.parallaxScrollView).setVisibility(View.VISIBLE);
        }
       // setColor(color);
    }
    private void setColor(int color) {
        FrameLayout frameLayout = (FrameLayout) findViewById(com.odoo.R.id.parallax_view);
        frameLayout.setBackgroundColor(color);
        parallaxScrollView.setParallaxOverLayColor(color);
        parallaxScrollView.setBackgroundColor(color);
        mForm.setIconTintColor(color);
        findViewById(com.odoo.R.id.parallax_view).setBackgroundColor(color);
        findViewById(com.odoo.R.id.parallax_view_edit).setBackgroundColor(color);
       // findViewById(com.odoo.R.id.tripScrollViewEdit).setBackgroundColor(color);
        if (captureImage != null) {
            GradientDrawable shapeDrawable =
                    (GradientDrawable) getResources().getDrawable(com.odoo.R.drawable.circle_mask_primary);
            shapeDrawable.setColor(color);
            captureImage.setBackgroundDrawable(shapeDrawable);
        }
    }
    @Override
    public void onFieldValueChange(OField field, Object value) {

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_trip_destination_save:
                OValues values = mForm.getValues();
                if (values != null) {
//                    if (newImage != null) {
//                        values.put("image_small", newImage);
//                        values.put("large_image", newImage);
//                    }
                    if (record != null) {
                        cmmsTripDestination.update(record.getInt(OColumn.ROW_ID), values);
                        Toast.makeText(this, com.odoo.R.string.toast_information_saved, Toast.LENGTH_LONG).show();
                        mEditMode = !mEditMode;
                        setupActionBar();
                    } else {
                        // values.put("customer", "true");
                        final int row_id = cmmsTripDestination.insert(values);
                        if (row_id != OModel.INVALID_ROW_ID) {
                            finish();
                        }
                    }
                }
                break;
            case R.id.menu_trip_destination_cancel:
                if (record == null) {
                    finish();
                    return true;
                }
            case R.id.menu_trip_destination_edit:
                mEditMode = !mEditMode;
                setMode(mEditMode);
                mForm.setEditable(mEditMode);
                mForm.initForm(record);
                //setCustomerImage();
                break;
            case R.id.menu_trip_destination_share:
                // ShareUtil.shareContact(this, record, true);
                break;
            case R.id.menu_trip_destination_import:
                //  ShareUtil.shareContact(this, record, false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_trip_destination_details, menu);
        mMenu = menu;
        setMode(mEditMode);
        return true;
    }


}

