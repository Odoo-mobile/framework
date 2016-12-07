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
 * Created on 7/1/15 5:11 PM
 */
package odoo.controls;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OM2ORecord;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OSelection;
import com.odoo.core.utils.OControls;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class OSelectionField extends LinearLayout implements IOControlData,
        AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener,
        RadioGroup.OnCheckedChangeListener {
    public static final String TAG = OSelectionField.class.getSimpleName();

    private Context mContext;
    private Object mValue = null;
    private Boolean mEditable = false;
    private OField.WidgetType mWidget = null;
    private Integer mResourceArray = null;
    private OColumn mCol;
    private String mLabel;
    private OModel mModel;
    private List<ODataRow> items = new ArrayList<>();
    private ValueUpdateListener mValueUpdateListener = null;
    // Controls
    private Spinner mSpinner = null;
    private SpinnerAdapter mAdapter;
    private RadioGroup mRadioGroup = null;
    private TextView txvView = null;
    private Boolean mReady = false;
    private float textSize = -1;
    private int appearance = -1;
    private int textColor = Color.BLACK;
    private OForm formView;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OSelectionField(Context context, AttributeSet attrs,
                           int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public OSelectionField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    public OSelectionField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public OSelectionField(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr,
                      int defStyleRes) {
        mContext = context;
        if (attrs != null) {

        }
        if (mContext.getClass().getSimpleName().contains("BridgeContext"))
            initControl();
    }

    private void createRadioGroup() {
        final LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        if (mRadioGroup == null) {
            mRadioGroup = new RadioGroup(mContext);
            mRadioGroup.setLayoutParams(params);
        } else {
            removeView(mRadioGroup);
        }
        mRadioGroup.removeAllViews();
        mRadioGroup.setOnCheckedChangeListener(this);
        for (ODataRow label : items) {
            RadioButton rdoBtn = new RadioButton(mContext);
            rdoBtn.setLayoutParams(params);
            rdoBtn.setText(label.getString(mModel.getDefaultNameColumn()));
            if (textSize > -1) {
                rdoBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            }
            if (appearance > -1) {
                rdoBtn.setTextAppearance(mContext, appearance);
            }
            rdoBtn.setTextColor(textColor);
            mRadioGroup.addView(rdoBtn);
        }
        addView(mRadioGroup);
    }

    @Override
    public void initControl() {
        final LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        removeAllViews();
        setOrientation(VERTICAL);
        createItems();
        if (isEditable()) {
            if (mWidget != null) {
                switch (mWidget) {
                    case RadioGroup:
                        createRadioGroup();
                        return;
                    case SelectionDialog:
                        txvView = new TextView(mContext);
                        txvView.setLayoutParams(params);
                        mAdapter = new SpinnerAdapter(mContext,
                                android.R.layout.simple_list_item_1, items);
                        setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View v) {

                                AlertDialog dialog = createSelectionDialog(
                                        getPos(), items, params);
                                txvView.setTag(dialog);
                                dialog.show();
                            }
                        });
                        if (textSize > -1) {
                            txvView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                        }
                        if (appearance > -1) {
                            txvView.setTextAppearance(mContext, appearance);
                        }
                        txvView.setTextColor(textColor);
                        addView(txvView);
                        return;
                    case Searchable:
                    case SearchableLive:
                        txvView = new TextView(mContext);
                        txvView.setLayoutParams(params);
                        setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                startSearchableActivity();
                            }
                        });
                        if (textSize > -1) {
                            txvView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                        }
                        if (appearance > -1) {
                            txvView.setTextAppearance(mContext, appearance);
                        }
                        txvView.setTextColor(textColor);
                        addView(txvView);
                        return;
                    default:
                        break;
                }
            }

            // Default View
            mSpinner = new Spinner(mContext);
            mSpinner.setLayoutParams(params);
            mAdapter = new SpinnerAdapter(mContext,
                    android.R.layout.simple_list_item_1, items);
            mSpinner.setAdapter(mAdapter);
            mSpinner.setOnItemSelectedListener(this);
            addView(mSpinner);
        } else {
            setOnClickListener(null);
            txvView = new TextView(mContext);
            if (textSize > -1) {
                txvView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            }
            if (appearance > -1) {
                txvView.setTextAppearance(mContext, appearance);
            }
            txvView.setTextColor(textColor);
            addView(txvView);
        }
    }

    private void startSearchableActivity() {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext);
        Intent intent = new Intent(mContext, SearchableItemActivity.class);
        intent.putExtra("resource_id", mResourceArray);
        intent.putExtra("selected_position", getPos());
        intent.putExtra(OColumn.ROW_ID, getPos());
        intent.putExtra("search_hint", getLabel());
        if (mCol != null) {
            intent.putExtra("column_name", mCol.getName());
            if (mCol.hasDomainFilterColumn()) {
                OValues formValues = formView.getControlValues();
                Bundle formData = formValues != null ?
                        formValues.toFilterColumnsBundle(mModel, mCol) : new Bundle();
                intent.putExtra("form_data", formData);
            }
        }
        intent.putExtra("model", mModel.getModelName());
        intent.putExtra("live_search", (mWidget == OField.WidgetType.SearchableLive));
        broadcastManager.registerReceiver(valueReceiver, new IntentFilter("searchable_value_select"));
        mContext.startActivity(intent);
    }

    private void createItems() {
        items.clear();
        if (!mContext.getClass().getSimpleName().contains("BridgeContext")) {
            if (mResourceArray != null && mResourceArray != -1) {
                String[] items_list = mContext.getResources().getStringArray(
                        mResourceArray);
                ODataRow row = new ODataRow();
                row.put(OColumn.ROW_ID, -1);
                row.put(mModel.getDefaultNameColumn(), "Nothing Selected");
                items.add(row);
                for (int i = 0; i < items_list.length; i++) {
                    row = new ODataRow();
                    row.put(OColumn.ROW_ID, i);
                    row.put(mModel.getDefaultNameColumn(), items_list[i]);
                    items.add(row);
                }
            } else if (mCol.getType().isAssignableFrom(OSelection.class)) {
                List<ODataRow> rows = new ArrayList<>();
                Object defaultVal = mCol.getDefaultValue();
                for (String key : mCol.getSelectionMap().keySet()) {
                    String val = mCol.getSelectionMap().get(key);
                    ODataRow row = new ODataRow();
                    row.put("key", key);
                    row.put("name", val);
                    if (defaultVal != null && defaultVal.toString().equals(val)) {
                        rows.add(0, row);
                    } else {
                        rows.add(row);
                    }
                }
                items.addAll(rows);
            } else {
                OValues formValues = formView.getControlValues();
                Bundle data = formValues != null ? formValues
                        .toFilterColumnsBundle(mModel, mCol) : new Bundle();
                items.addAll(getRecordItems(mModel, mCol, data));
            }
        }
    }

    private int getPos() {
        if (mResourceArray != -1 && mValue != null) {
            return Integer.parseInt(mValue.toString());
        } else if (mCol.getType().isAssignableFrom(OSelection.class)) {
            if (items.size() <= 0) {
                createItems();
            }
            for (ODataRow item : items) {
                int index = items.indexOf(item);
                if (item.getString("key").equals(mValue.toString())) {
                    return index;
                }
            }
        } else {
            ODataRow rec = getValueForM2O();
            if (rec != null) {
                return rec.getInt(OColumn.ROW_ID);
            }
        }
        return -1;
    }

    @Override
    public void setValue(Object value) {
        mValue = value;
        if (mValue == null || mValue.toString().equals("false")) {
            mValue = -1;
        }
        ODataRow row = new ODataRow();
        if (isEditable()) {
            if (mWidget != null) {
                switch (mWidget) {
                    case RadioGroup:
                        if (mResourceArray != -1) {
                            ((RadioButton) mRadioGroup.getChildAt(getPos()))
                                    .setChecked(true);
                            row = items.get(getPos());
                        } else {
                            Integer row_id;
                            if (mValue instanceof OM2ORecord) {
                                row = ((OM2ORecord) mValue).browse();
                                row_id = row.getInt(OColumn.ROW_ID);
                            } else
                                row_id = (Integer) mValue;
                            int index = 0;
                            for (int i = 0; i < items.size(); i++) {
                                if (items.get(i).getInt(OColumn.ROW_ID) == row_id) {
                                    index = i;
                                    break;
                                }
                            }
                            row = items.get(index);
                            ((RadioButton) mRadioGroup.getChildAt(index))
                                    .setChecked(true);
                        }
                        break;
                    case Searchable:
                    case SearchableLive:
                    case SelectionDialog:
                        if (mResourceArray != -1) {
                            row = items.get(getPos());
                        } else {
                            if (mValue instanceof OM2ORecord)
                                row = ((OM2ORecord) mValue).browse();
                            else if (mValue instanceof Integer)
                                row = getRecordData((Integer) mValue);
                        }
                        if (row != null)
                            txvView.setText(row.getString(mModel.getDefaultNameColumn()));
                        if (txvView.getTag() != null) {
                            AlertDialog dialog = (AlertDialog) txvView.getTag();
                            dialog.dismiss();
                        }
                        break;
                    default:
                        break;
                }
            } else {
                if (mResourceArray != -1) {
                    mSpinner.setSelection(getPos());
                    row = items.get(getPos());
                } else if (mCol.getType().isAssignableFrom(OSelection.class)) {
                    int pos = getPos();
                    mSpinner.setSelection(pos);
                    if (pos != -1) {
                        row = mAdapter.getItem(pos);
                    }
                } else {
                    Integer row_id = null;
                    if (mValue instanceof OM2ORecord) {
                        row = ((OM2ORecord) mValue).browse();
                        row_id = row.getInt(OColumn.ROW_ID);
                    } else if (mValue instanceof Integer)
                        row_id = (Integer) mValue;
                    int index = 0;
                    for (int i = 0; i < items.size(); i++) {
                        if (items.get(i).getInt(OColumn.ROW_ID) == row_id) {
                            index = i;
                            break;
                        }
                    }
                    row = items.get(index);
                    mSpinner.setSelection(index);
                }
            }
        } else {
            if (mResourceArray != -1 || mCol.getType().isAssignableFrom(OSelection.class)) {
                int position = getPos();
                // Ignoring if default value not set for field.
                if (position != -1) row = items.get(position);
            } else {
                if (mValue instanceof OM2ORecord) {
                    row = ((OM2ORecord) mValue).browse();
                    if (row == null) {
                        row = new ODataRow();
                    }
                } else {
                    if (!(mValue instanceof Boolean) && mValue != null && !mValue.toString().equals("false")) {
                        int row_id = (Integer) mValue;
                        row = getRecordData(row_id);
                    } else {
                        row = new ODataRow();
                        row.put(mModel.getDefaultNameColumn(), "No " + mCol.getLabel() + " selected");
                    }
                }
            }
            if (!row.getString(mModel.getDefaultNameColumn()).equals("false"))
                txvView.setText(row.getString(mModel.getDefaultNameColumn()));
        }
        if (isEditable() && mValueUpdateListener != null) {
            if (mValue instanceof Integer && (int) mValue == -1) return;
            mValueUpdateListener.onValueUpdate(row);
        }
    }

    @Override
    public View getFieldView() {
        return null;
    }


    @Override
    public void setError(String error) {
        if (error != null)
            Toast.makeText(mContext, error, Toast.LENGTH_LONG).show();
    }

    private ODataRow getValueForM2O() {
        if (getValue() != null) {
            if (getValue() instanceof OM2ORecord)
                return ((OM2ORecord) getValue()).browse();
            else if (getValue() instanceof Integer)
                return getRecordData((Integer) getValue());
        }
        return null;
    }

    @Override
    public Object getValue() {
        if (mValue instanceof OM2ORecord) {
            return ((OM2ORecord) mValue).getId();
        }
        return mValue;
    }

    @Override
    public void setEditable(Boolean editable) {
        mEditable = editable;
        initControl();
    }

    @Override
    public Boolean isEditable() {
        return mEditable;
    }

    public void setWidgetType(OField.WidgetType type) {
        mWidget = type;
        initControl();
    }

    public void setArrayResourceId(int res_id) {
        mResourceArray = res_id;
    }

    public void setColumn(OColumn col) {
        mCol = col;
        if (mCol != null && mLabel == null) {
            mLabel = mCol.getLabel();
        }
    }

    private ODataRow getRecordData(int row_id) {
        ODataRow row;
        if (row_id > 0) {
            OModel rel_model = mModel.createInstance(mCol.getType());
            row = rel_model.browse(row_id);
        } else {
            row = items.get(0);
        }
        return row;
    }

    public void setFormView(OForm formView) {
        this.formView = formView;
    }

    private class SpinnerAdapter extends ArrayAdapter<ODataRow> {

        public SpinnerAdapter(Context context, int resource,
                              List<ODataRow> objects) {
            super(context, resource, objects);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return generateView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            return generateView(position, convertView, parent);
        }

        private View generateView(int position, View convertView,
                                  ViewGroup parent) {
            View v = convertView;
            if (v == null)
                v = LayoutInflater.from(mContext).inflate(
                        android.R.layout.simple_list_item_1, parent, false);
            ODataRow row = getItem(position);
            OControls.setText(v, android.R.id.text1, row.getString(mModel.getDefaultNameColumn()));
            return v;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
        if (mResourceArray != -1) {
            mValue = position;
        } else if (mCol.getType().isAssignableFrom(OSelection.class)) {
            ODataRow row = mAdapter.getItem(position);
            mValue = row.getString("key");
        } else {
            mValue = items.get(position).get(OColumn.ROW_ID);
        }
        setValue(mValue);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        mValue = null;
    }

    @Override
    public void setLabelText(String label) {
        mLabel = label;
    }

    @Override
    public String getLabel() {
        if (mLabel != null)
            return mLabel;
        if (mCol != null)
            return mCol.getLabel();
        return "unknown";
    }

    private AlertDialog createSelectionDialog(final int selected_position,
                                              final List<ODataRow> items, LayoutParams params) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        ListView dialogView = new ListView(mContext);
        dialogView.setAdapter(mAdapter);
        dialogView.setOnItemClickListener(this);
        dialogView.setLayoutParams(params);
        builder.setView(dialogView);
        return builder.create();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        setValue(position);
    }

    BroadcastReceiver valueReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mCol.getName().equals(intent.getStringExtra("column_name"))) {
                setValue(intent.getIntExtra("selected_position", -1));
                LocalBroadcastManager.getInstance(mContext).unregisterReceiver(valueReceiver);
            }
        }
    };

    public void setModel(OModel model) {
        mModel = model;
    }

    public static List<ODataRow> getRecordItems(OModel model, OColumn column, Bundle formData) {
        List<ODataRow> items = new ArrayList<>();

        OModel rel_model = model.createInstance(column.getType());
        StringBuilder whr = new StringBuilder();
        List<Object> args_list = new ArrayList<>();

        LinkedHashMap<String, OColumn.ColumnDomain> domains = new LinkedHashMap<>();
        domains.putAll(column.getDomains());
        if (column.hasDomainFilterColumn()) {
            domains.putAll(column.getDomainFilterParser(model).getDomain(formData));
        }
        for (String key : domains.keySet()) {
            OColumn.ColumnDomain domain = domains.get(key);
            if (domain.getConditionalOperator() != null) {
                whr.append(domain.getConditionalOperator());
            } else {
                whr.append(" ");
                whr.append(domain.getColumn());
                whr.append(" ");
                whr.append(domain.getOperator());
                whr.append(" ? ");
                args_list.add(domain.getValue() + "");
            }
        }
        String where = null;
        String[] args = null;
        if (args_list.size() > 0) {
            where = whr.toString();
            args = args_list.toArray(new String[args_list.size()]);
        }
        List<ODataRow> rows = rel_model.select(new String[]{rel_model.getDefaultNameColumn()}, where,
                args, rel_model.getDefaultNameColumn());
        ODataRow row = new ODataRow();
        row.put(OColumn.ROW_ID, -1);
        row.put(rel_model.getDefaultNameColumn(), "No " + column.getLabel() + " selected");
        items.add(row);
        items.addAll(rows);
        return items;
    }

    @Override
    public void setValueUpdateListener(ValueUpdateListener listener) {
        mValueUpdateListener = listener;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int index = mRadioGroup.indexOfChild(group.findViewById(checkedId));
        ODataRow row = items.get(index);
        setValue(row.getInt(OColumn.ROW_ID));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mReady = true;
    }

    @Override
    public Boolean isControlReady() {
        return mReady;
    }

    @Override
    public void resetData() {
        if (isEditable()) {
            if (mWidget == null) {
                if (mAdapter != null) {
                    createItems();
                    mAdapter.notifyDataSetChanged();
                }
            } else {
                switch (mWidget) {
                    case SelectionDialog:
                        createItems();
                        break;
                    case RadioGroup:
                        createItems();
                        createRadioGroup();
                        break;
                    case Searchable:
                    case SearchableLive:
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void setResource(float textSize, int appearance, int color) {
        this.textSize = textSize;
        this.appearance = appearance;
        this.textColor = color;
    }
}
