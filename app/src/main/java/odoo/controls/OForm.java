/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p>
 * Created on 7/1/15 5:10 PM
 */
package odoo.controls;

import android.animation.LayoutTransition;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.odoo.R;
import com.odoo.base.addons.mail.widget.MailChatterView;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.utils.DomainFilterParser;
import com.odoo.core.utils.OResource;

import java.util.HashMap;

public class OForm extends LinearLayout {
    public static final String TAG = OForm.class.getSimpleName();
    private Boolean mEditable = false;
    private String mModel;
    private OModel model = null;
    private HashMap<String, OField> mFormFieldControls = new HashMap<>();
    private Context mContext = null;
    private ODataRow mRecord = null;
    private Boolean autoUIGenerate = true;
    private int icon_tint_color = 0;
    private Boolean mFirstModeChange = true;
    private OValues extraValues = new OValues();
    private MailChatterView chatterView = null;
    private Boolean loadChatter = true;

    public OForm(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OForm(Context context, AttributeSet attrs, int defStyleAttr,
                 int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public OForm(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    public OForm(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public void setEditable(Boolean editable) {
        mEditable = editable;
        if (mEditable) {
            mFirstModeChange = true;
        }
        for (String key : mFormFieldControls.keySet()) {
            OField control = mFormFieldControls.get(key);
            control.setEditable(editable);
        }
        mFirstModeChange = false;
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr,
                      int defStyleRes) {
        mFirstModeChange = true;
        mContext = context;
        if (attrs != null) {
            TypedArray types = mContext.obtainStyledAttributes(attrs,
                    R.styleable.OForm);
            mModel = types.getString(R.styleable.OForm_modelName);
            mEditable = types.getBoolean(R.styleable.OForm_editableMode,
                    false);
            autoUIGenerate = types.getBoolean(R.styleable.OForm_autoUIGenerate, true);
            icon_tint_color = types.getColor(R.styleable.OForm_controlIconTint, -1);
            types.recycle();
        }
        initForm();
        LayoutTransition transition = new LayoutTransition();
        setLayoutTransition(transition);
    }

    public boolean getEditable() {
        return mEditable;
    }

    public void setModel(String model) {
        mModel = model;
    }

    public String getModel() {
        return mModel;
    }

    public void setData(ODataRow record) {
        initForm(record);
    }

    public ODataRow getData() {
        return mRecord;
    }

    public void initForm(ODataRow record) {
        mRecord = new ODataRow();
        mRecord = record;
        initForm();
    }

    public void setIconTintColor(int color) {
        icon_tint_color = color;
    }

    private void initForm() {
        findAllFields(this);
        model = OModel.get(mContext, mModel, null);
        setOrientation(VERTICAL);
        for (String key : mFormFieldControls.keySet()) {
            View v = mFormFieldControls.get(key);
            if (v instanceof OField) {
                OField c = (OField) v;
                c.setFormView(this);
                c.setEditable(mEditable);
                c.useTemplate(autoUIGenerate);
                c.setModel(model);
                OColumn column = model.getColumn(c.getFieldName());
                if (column != null) {
                    c.setColumn(column);
                    // Setting OnChange Event
                    if (column.hasOnChange()) {
                        setOnChangeForControl(column, c);
                    }

                    // Setting domain Filter for column
                    if (column.hasDomainFilterColumn()) {
                        setOnDomainFilterCallBack(column, c);
                    }
                }
                c.initControl();
                Object val = c.getValue();
                if (mRecord != null) {
                    if (mRecord.contains(c.getFieldName()))
                        val = mRecord.get(c.getFieldName());
                }
                if (val != null)
                    c.setValue(val);
                if (icon_tint_color != -1) {
                    c.setIconTintColor(icon_tint_color);
                }
            }
        }

        // Adding chatter view if model requested
        if (loadChatter) {
            if (!mEditable) {
                if (model != null && model.hasMailChatter()
                        && mRecord != null && mRecord.getInt("id") != 0) {
                    if (chatterView == null) {
                        chatterView = (MailChatterView) LayoutInflater.from(mContext)
                                .inflate(R.layout.base_mail_chatter, this, false);
                        chatterView.setModelName(model.getModelName());
                        chatterView.setRecordServerId(mRecord.getInt("id"));
                        chatterView.generateView();
                        addView(chatterView);
                    }
                }
            }
        }
    }

    public void loadChatter(boolean loadChatter) {
        this.loadChatter = loadChatter;
    }

    private void findAllFields(ViewGroup view) {
        int child = view.getChildCount();
        for (int i = 0; i < child; i++) {
            View v = view.getChildAt(i);
            if (v instanceof LinearLayout || v instanceof RelativeLayout) {
                if (v.getVisibility() == View.VISIBLE)
                    findAllFields((ViewGroup) v);
            }
            if (v instanceof OField) {
                OField field = (OField) v;
                if (field.getVisibility() == View.VISIBLE)
                    mFormFieldControls.put(field.getFieldName(), field);
            }
        }
    }

    public OValues getControlValues() {
        OValues values = getValues(false);
        if (mRecord != null && values != null) {
            for (String key : values.keys()) {
                if (values.get(key).toString().equals("false") &&
                        !mRecord.get(key).toString().equals("false")) {
                    values.put(key, mRecord.get(key));
                }
            }
        }
        return values;
    }

    public OValues getValues() {
        return getValues(true);
    }

    private OValues getValues(boolean validateData) {
        OValues values = new OValues();
        for (String key : mFormFieldControls.keySet()) {
            OField control = mFormFieldControls.get(key);
            Object val = control.getValue();
            OColumn column = control.getColumn();
            if (val == null || TextUtils.isEmpty(val.toString())
                    || val.toString().equals("-1")) {
                val = false;
            }

            if (column != null && validateData && column.isRequired()) {
                if (val.toString().equals("false")) {
                    control.setError(column.getLabel() +
                            " " + OResource.string(mContext, R.string.label_required));
                    return null;
                } else {
                    control.setError(null);
                }
            }
            values.put(key, val);
        }
        values.addAll(extraValues.toDataRow().getAll());
        return values;
    }

    private void setOnDomainFilterCallBack(final OColumn column, final OField baseField) {
        DomainFilterParser domainFilter = column.getDomainFilterParser(model);
        for (String key : domainFilter.getFilterColumns()) {
            DomainFilterParser.FilterDomain filterDomain = domainFilter.getFilter(key);
            if (filterDomain.operator_value == null &&
                    mFormFieldControls.containsKey(filterDomain.valueColumn)) {
                final OField columnField = mFormFieldControls.get(filterDomain.valueColumn);
                columnField.setOnFilterDomainCallBack(new IOnDomainFilterCallbacks() {
                    @Override
                    public void onFieldValueChanged(Object value) {
                        baseField.resetData();
                    }
                });
            }
        }
    }

    // OnChange event for control column
    private void setOnChangeForControl(final OColumn column, OField field) {
        field.setOnChangeCallbackListener(new IOnChangeCallback() {

            @Override
            public void onValueChange(final ODataRow row) {
                if (!mFirstModeChange) {
                    if (!column.isOnChangeBGProcess()) {
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                Object value = model.getOnChangeMethodValue(column, row);
                                if (value instanceof ODataRow)
                                    fillOnChangeData((ODataRow) value);
                            }
                        }, 300);
                    } else {
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                OnChangeBackground bgProcess = new OnChangeBackground(
                                        column);
                                bgProcess.execute(row);
                            }
                        }, 300);
                    }
                }
                if (mFirstModeChange) {
                    mFirstModeChange = false;
                }
            }

        });
    }

    private void fillOnChangeData(ODataRow values) {
        if (values != null) {
            for (String key : values.keys()) {
                if (mFormFieldControls.containsKey(key)) {
                    OField fld = mFormFieldControls.get(key);
                    fld.setValue(values.get(key));
                } else {
                    extraValues.put(key, values.get(key));
                }
            }
        }
    }

    private class OnChangeBackground extends
            AsyncTask<ODataRow, Void, ODataRow> {
        private ProgressDialog mDialog;

        private OColumn mCol;

        public OnChangeBackground(OColumn col) {
            mCol = col;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = new ProgressDialog(mContext);
            mDialog.setTitle(mContext.getString(R.string.title_working));
            mDialog.setMessage(mContext.getString(R.string.title_please_wait));
            mDialog.setCancelable(false);
            mDialog.show();
        }

        @Override
        protected ODataRow doInBackground(ODataRow... params) {
            try {
                Thread.sleep(300);
                return (ODataRow) model.getOnChangeMethodValue(mCol, params[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ODataRow result) {
            super.onPostExecute(result);
            if (result != null) {
                fillOnChangeData(result);
            }
            mDialog.dismiss();
        }
    }
}
