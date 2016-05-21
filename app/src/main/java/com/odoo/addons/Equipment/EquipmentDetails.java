package com.odoo.addons.Equipment;

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
import com.odoo.base.addons.ir.feature.OFileManager;
import com.odoo.addons.Equipment.providers.CmmsEquipment;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.support.OdooCompatActivity;
import com.odoo.core.utils.BitmapUtils;
import com.odoo.core.utils.IntentUtils;
import com.odoo.core.utils.OAppBarUtils;
import com.odoo.core.utils.OStringColorUtil;
import com.odoo.widgets.parallax.ParallaxScrollView;

import odoo.controls.OField;
import odoo.controls.OForm;
import odoo.controls.OSelectionField;

/**
 * Created by Sylwek on 05/12/2015.
 */
public class EquipmentDetails extends OdooCompatActivity
        implements View.OnClickListener, OField.IOnFieldValueChangeListener {
    public static final String TAG = EquipmentDetails.class.getSimpleName();
    private final String KEY_MODE = "key_edit_mode";
    private final String KEY_NEW_IMAGE = "key_new_image";
    private ActionBar actionBar;
    private Bundle extras;
    private CmmsEquipment cmmsEquipment;
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
//        cmmsEquipment = new CmmsEquipment(this, null);
//        extras = getIntent().getExtras();
//        record = cmmsEquipment.browse(extras.getInt(OColumn.ROW_ID));
//        OForm mform  = (OForm) findViewById(R.id.equipmentForm);
//       // mform.setEditable(true);
//        mform.initForm(record);
//
//        newImage = "fdsfdsfds";

        super.onCreate(savedInstanceState);
        setContentView(com.odoo.R.layout.equipment_detail);
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
        cmmsEquipment = new CmmsEquipment(this, null);
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
            userImage.setColorFilter(Color.parseColor("#ffffff"));
            mForm.setEditable(mEditMode);
            mForm.initForm(null);
        } else {
            int rowId = extras.getInt(OColumn.ROW_ID);
            record = cmmsEquipment.browse(rowId);
            // record.put("full_address", resPartner.getAddress(record));
            //  checkControls();
            setMode(mEditMode);
            mForm.setEditable(mEditMode);
            mForm.initForm(record);
            mTitleView.setText(record.getString("name"));
//            setCustomerImage();
//            if (record.getInt("id") != 0 && record.getString("large_image").equals("false")) {
//                BigImageLoader bigImageLoader = new BigImageLoader();
//                bigImageLoader.execute(record.getInt("id"));
//            }
        }
    }
    private void setMode(Boolean edit) {
        if (mMenu != null) {
            mMenu.findItem(com.odoo.R.id.menu_equipment_detail_more).setVisible(!edit);
            mMenu.findItem(com.odoo.R.id.menu_equipment_edit).setVisible(!edit);
            mMenu.findItem(com.odoo.R.id.menu_equipment_save).setVisible(edit);
            mMenu.findItem(com.odoo.R.id.menu_equipment_cancel).setVisible(edit);
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
            mForm = (OForm) findViewById(com.odoo.R.id.equipmentFormEdit);
            captureImage = (ImageView) findViewById(com.odoo.R.id.captureImage);
            captureImage.setOnClickListener(this);
            userImage = (ImageView) findViewById(android.R.id.icon1);
            findViewById(com.odoo.R.id.parallaxScrollView).setVisibility(View.GONE);
            findViewById(com.odoo.R.id.equipmentScrollViewEdit).setVisibility(View.VISIBLE);
            // OField is_company = (OField) findViewById(R.id.is_company_edit);
            //  is_company.setOnValueChangeListener(this);
        } else {
            actionBar.setBackgroundDrawable(getResources().getDrawable(com.odoo.R.drawable.action_bar_shade));
            userImage = (ImageView) findViewById(android.R.id.icon);
            mForm = (OForm) findViewById(com.odoo.R.id.equipmentForm);
            findViewById(com.odoo.R.id.equipmentScrollViewEdit).setVisibility(View.GONE);
            findViewById(com.odoo.R.id.parallaxScrollView).setVisibility(View.VISIBLE);
        }
        setColor(color);
    }
    private void setColor(int color) {
        FrameLayout frameLayout = (FrameLayout) findViewById(com.odoo.R.id.parallax_view);
        frameLayout.setBackgroundColor(color);
        parallaxScrollView.setParallaxOverLayColor(color);
        parallaxScrollView.setBackgroundColor(color);
        mForm.setIconTintColor(color);
        findViewById(com.odoo.R.id.parallax_view).setBackgroundColor(color);
        findViewById(com.odoo.R.id.parallax_view_edit).setBackgroundColor(color);
        findViewById(com.odoo.R.id.equipmentScrollViewEdit).setBackgroundColor(color);
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
            case com.odoo.R.id.menu_equipment_save:
                OValues values = mForm.getValues();
                if (values != null) {
//                    if (newImage != null) {
//                        values.put("image_small", newImage);
//                        values.put("large_image", newImage);
//                    }
                    if (record != null) {
                        cmmsEquipment.update(record.getInt(OColumn.ROW_ID), values);
                        Toast.makeText(this, com.odoo.R.string.toast_information_saved, Toast.LENGTH_LONG).show();
                        mEditMode = !mEditMode;
                        setupActionBar();
                    } else {
                        // values.put("customer", "true");
                        final int row_id = cmmsEquipment.insert(values);
                        if (row_id != OModel.INVALID_ROW_ID) {
                            finish();
                        }
                    }
                }
                break;
            case com.odoo.R.id.menu_equipment_cancel:
                if (record == null) {
                    finish();
                    return true;
                }
            case com.odoo.R.id.menu_equipment_edit:
                mEditMode = !mEditMode;
                setMode(mEditMode);
                mForm.setEditable(mEditMode);
                mForm.initForm(record);
                setCustomerImage();
                break;
            case com.odoo.R.id.menu_equipment_share:
                // ShareUtil.shareContact(this, record, true);
                break;
            case com.odoo.R.id.menu_equipment_import:
                //  ShareUtil.shareContact(this, record, false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case com.odoo.R.id.captureImage:
                fileManager.requestForFile(OFileManager.RequestType.IMAGE_OR_CAPTURE_IMAGE);
                break;
        }
    }
    private void setCustomerImage() {
        if (!record.getString("photo").equals("false")) {
            userImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            userImage.setColorFilter(null);
            String base64 = newImage;
//            if (newImage == null) {
//                if (!record.getString("large_image").equals("false")) {
//                    base64 = record.getString("large_image");
//                } else {
//                    base64 = record.getString("image_small");
//                }
//            }
            userImage.setImageBitmap(BitmapUtils.getBitmapImage(this, base64));
        } else {
            userImage.setColorFilter(Color.parseColor("#ffffff"));
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.odoo.R.menu.menu_equipment_detail, menu);
        mMenu = menu;
        setMode(mEditMode);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        OValues values = fileManager.handleResult(requestCode, resultCode, data);
        if (values != null && !values.contains("size_limit_exceed")) {
            newImage = values.getString("datas");
            userImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            userImage.setColorFilter(null);
            userImage.setImageBitmap(BitmapUtils.getBitmapImage(this, newImage));
        } else if (values != null) {
            Toast.makeText(this, com.odoo.R.string.toast_image_size_too_large, Toast.LENGTH_LONG).show();
        }
    }
}
