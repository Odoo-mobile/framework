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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.odoo.R;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OBlob;
import com.odoo.core.orm.fields.types.OBoolean;
import com.odoo.core.orm.fields.types.ODate;
import com.odoo.core.orm.fields.types.ODateTime;
import com.odoo.core.orm.fields.types.OFloat;
import com.odoo.core.orm.fields.types.OHtml;
import com.odoo.core.orm.fields.types.OInteger;
import com.odoo.core.orm.fields.types.OSelection;
import com.odoo.core.orm.fields.types.OText;
import com.odoo.core.orm.fields.types.OTimestamp;
import com.odoo.core.orm.fields.types.OVarchar;

public class OField extends LinearLayout implements IOControlData.ValueUpdateListener {
    public static final String TAG = OField.class.getSimpleName();
    private Context mContext = null;
    private FieldType mType = FieldType.Text;
    private OColumn mColumn = null;
    private OModel mModel = null;
    private String mLabel, mField_name;
    private Object mValue = null;
    private boolean mEditable = false, showIcon = true, show_label = true;
    private TextView label_view = null;
    private int resId, tint_color = Color.BLACK, mValueArrayId = -1;
    private ImageView img_icon = null;
    private ViewGroup container = null;
    private Boolean with_bottom_padding = true, with_top_padding = true;
    private WidgetType mWidgetType = null;
    private String mParsePattern = null;
    private IOnChangeCallback mOnChangeCallback = null;
    private IOnDomainFilterCallbacks mOnDomainFilterCallbacks = null;
    private float mWidgetImageSize = -1;
    private Boolean withPadding = true;
    // Controls
    private OForm parentForm;
    private IOControlData mControlData = null;
    private Boolean useTemplate = true;
    private Integer defaultImage = -1;
    // Appearance
    private int textColor = Color.BLACK;
    private int labelColor = Color.DKGRAY;
    private int textAppearance = -1;
    private int labelAppearance = -1;
    private float textSize = -1;
    private float labelSize = -1;
    private IOnFieldValueChangeListener mValueUpdateListener = null;

    public enum WidgetType {
        Switch, RadioGroup, SelectionDialog, Searchable, SearchableLive, Image, ImageCircle, Duration;

        public static WidgetType getWidgetType(int widget) {
            switch (widget) {
                case 0:
                    return WidgetType.Switch;
                case 1:
                    return WidgetType.RadioGroup;
                case 2:
                    return WidgetType.SelectionDialog;
                case 3:
                    return WidgetType.Searchable;
                case 4:
                    return WidgetType.SearchableLive;
                case 5:
                    return WidgetType.Image;
                case 6:
                    return WidgetType.ImageCircle;
                case 7:
                    return WidgetType.Duration;
            }
            return null;
        }
    }

    public enum FieldType {
        Text, Boolean, ManyToOne, Chips, Selection, Date, Time, DateTime, Blob, RelationType;

        public static FieldType getTypeValue(int type_val) {
            switch (type_val) {
                case 0:
                    return FieldType.Text;
                case 1:
                    return FieldType.Boolean;
                case 2:
                    return FieldType.ManyToOne;
                case 3:
                    return FieldType.Chips;
                case 4:
                    return FieldType.Selection;
                case 5:
                    return FieldType.Date;
                case 6:
                    return FieldType.DateTime;
                case 7:
                    return FieldType.Blob;
                case 8:
                    return FieldType.Time;
            }
            return FieldType.Text;
        }
    }

    public OField(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OField(Context context, AttributeSet attrs, int defStyleAttr,
                  int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public OField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    public OField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr,
                      int defStyleRes) {
        mContext = context;
        if (attrs != null) {
            TypedArray types = mContext.obtainStyledAttributes(attrs,
                    R.styleable.OField);
            mField_name = types.getString(R.styleable.OField_fieldName);
            resId = types.getResourceId(R.styleable.OField_iconResource, 0);
            showIcon = types.getBoolean(R.styleable.OField_showIcon, true);
            tint_color = types.getColor(R.styleable.OField_iconTint, 0);
            show_label = types.getBoolean(R.styleable.OField_showLabel, true);
            int type_value = types.getInt(R.styleable.OField_fieldType, 0);
            mType = FieldType.getTypeValue(type_value);

            with_bottom_padding = types.getBoolean(
                    R.styleable.OField_withBottomPadding, true);
            with_top_padding = types.getBoolean(
                    R.styleable.OField_withTopPadding, true);
            mLabel = types.getString(R.styleable.OField_controlLabel);
            mValue = types.getString(R.styleable.OField_defaultFieldValue);
            mParsePattern = types.getString(R.styleable.OField_parsePattern);
            mValueArrayId = types.getResourceId(
                    R.styleable.OField_valueArray, -1);
            mWidgetType = WidgetType.getWidgetType(types.getInt(
                    R.styleable.OField_widgetType, -1));
            mWidgetImageSize = types.getDimension(R.styleable.OField_widgetImageSize, -1);
            withPadding = types.getBoolean(R.styleable.OField_withOutSidePadding, true);

            textColor = types.getColor(R.styleable.OField_fieldTextColor, Color.BLACK);
            labelColor = types.getColor(R.styleable.OField_fieldLabelColor, Color.DKGRAY);
            textAppearance = types.getResourceId(R.styleable.OField_fieldTextAppearance, -1);
            labelAppearance = types.getResourceId(R.styleable.OField_fieldLabelTextAppearance, -1);
            textSize = types.getDimension(R.styleable.OField_fieldTextSize, -1);
            labelSize = types.getDimension(R.styleable.OField_fieldLabelSize, -1);
            defaultImage = types.getResourceId(R.styleable.OField_defaultImage, -1);
            types.recycle();
        }
        if (mContext.getClass().getSimpleName().contains("BridgeContext"))
            initControl();
    }

    public void setFormView(OForm formView) {
        parentForm = formView;
    }

    public void useTemplate(Boolean withTemplate) {
        useTemplate = withTemplate;
    }

    private void initLayout() {
        removeAllViews();
        if (useTemplate) {
            View layout = LayoutInflater.from(mContext).inflate(
                    R.layout.base_control_template, this, false);

            if (withPadding) {
                int top_padding = layout.getPaddingTop();
                int right_padding = layout.getPaddingRight();
                int bottom_padding = layout.getPaddingBottom();
                int left_padding = layout.getPaddingLeft();
                if (!with_bottom_padding) {
                    layout.setPadding(left_padding, top_padding, right_padding, 0);
                }
                if (!with_top_padding) {
                    layout.setPadding(left_padding, 0, right_padding, bottom_padding);
                }
            } else {
                layout.setPadding(0, 0, 0, 0);
            }
            addView(layout);
            container = (ViewGroup) findViewById(R.id.control_container);
            img_icon = (ImageView) findViewById(android.R.id.icon);
            img_icon.setColorFilter(tint_color);
            setImageIcon();
        } else {
            container = this;
        }
    }

    public void initControl() {
        initLayout();
        View controlView = null;
        if (show_label) {
            label_view = getLabelView();
            container.addView(label_view);
        }
        switch (mType) {
            case Text:
                controlView = initTextControl();
                break;
            case Boolean:
                controlView = initBooleanControl();
                break;
            case Chips:
                break;
            case ManyToOne:
            case Selection:
                controlView = initSelectionWidget();
                break;
            case Date:
            case Time:
            case DateTime:
                controlView = initDateTimeControl(mType);
                break;
            case Blob:
                controlView = initBlobControl();
                break;
            default:
                return;
        }
        mControlData.setValueUpdateListener(this);
        mControlData.setEditable(getEditable());
        mControlData.initControl();
        mControlData.setValue(mValue);
        container.addView(controlView);
    }

    public <T> T getFieldView() {
        return (T) mControlData.getFieldView();
    }

    public void setIconTintColor(int color) {
        tint_color = color;
        if (img_icon != null) {
            img_icon.setColorFilter(tint_color);
        }
    }

    private void setImageIcon() {
        if (showIcon) {
            if (resId != 0)
                img_icon.setImageResource(resId);
            if (tint_color != 0)
                img_icon.setColorFilter(tint_color);
        } else
            img_icon.setVisibility(View.GONE);
    }

    public <T> void setColumn(OColumn column) {
        mColumn = column;
        mType = getType(column.getType());
        if (label_view != null) {
            label_view.setText(getLabelText());
        }
        if (mControlData != null) {
            mControlData.setColumn(mColumn);
        }
    }

    private <T> FieldType getType(Class<T> type_class) {
        try {
            // Varchar
            if (type_class.isAssignableFrom(OVarchar.class)
                    || type_class.isAssignableFrom(OInteger.class)
                    || type_class.isAssignableFrom(OFloat.class)) {
                return FieldType.Text;
            }
            // boolean
            if (type_class.isAssignableFrom(OBoolean.class)) {
                return FieldType.Boolean;
            }

            // Blob
            if (type_class.isAssignableFrom(OBlob.class)) {
                return FieldType.Blob;
            }
            // DateTime
            if (type_class.isAssignableFrom(ODateTime.class)
                    || type_class.isAssignableFrom(OTimestamp.class)) {
                return FieldType.DateTime;
            }
            // Date
            if (type_class.isAssignableFrom(ODate.class)) {
                return FieldType.Date;
            }
            // Text
            if (type_class.isAssignableFrom(OText.class)) {
                return FieldType.Text;
            }
            // FIXME: WebView type
            if (type_class.isAssignableFrom(OHtml.class)) {
                return FieldType.Text;
            }
            if (type_class.isAssignableFrom(OSelection.class)) {
                return FieldType.Selection;
            }
            // ManyToOne
            if (mColumn.getRelationType() != null
                    && mColumn.getRelationType() == OColumn.RelationType.ManyToOne) {
                return FieldType.ManyToOne;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getLabelText() {
        if (mLabel != null)
            return mLabel;
        if (mColumn != null)
            return mColumn.getLabel();
        if (mControlData != null)
            return mControlData.getLabel();
        return getFieldName();
    }

    public void setValue(Object value) {
        mValue = value;
        if (mValue != null && mControlData != null) {
            mControlData.setValue(mValue);
        }
    }

    public Object getValue() {
        if (mControlData != null)
            return mControlData.getValue();
        return null;
    }

    public void setEditable(Boolean editable) {
        mEditable = editable;
        if (mControlData != null) {
            Object value = getValue();
            mControlData.setEditable(editable);
            mControlData.initControl();
            if (value != null)
                mControlData.setValue(value);
        }
    }

    public boolean getEditable() {
        return mEditable;
    }

    public String getFieldName() {
        return mField_name;
    }

    // EditText control (TextView, EditText)
    private View initTextControl() {
        setOrientation(VERTICAL);
        OEditTextField edt = new OEditTextField(mContext);
        edt.setWidgetType(mWidgetType);
        mControlData = edt;
        edt.setResource(textSize, textAppearance, textColor);
        edt.setColumn(mColumn);
        edt.setHint(mLabel);
        return edt;
    }

    // Boolean Control (Checkbox, W-Switch)
    private View initBooleanControl() {
        OBooleanField bool = new OBooleanField(mContext);
        mControlData = bool;
        bool.setResource(textSize, textAppearance, textColor);
        bool.setColumn(mColumn);
        bool.setEditable(getEditable());
        bool.setLabelText(getLabelText());
        bool.setWidgetType(mWidgetType);
        return bool;
    }

    // Selection, Searchable, SearchableLive
    private View initSelectionWidget() {
        OSelectionField selection = new OSelectionField(mContext);
        selection.setFormView(parentForm);
        mControlData = selection;
        selection.setResource(textSize, textAppearance, textColor);
        selection.setLabelText(getLabelText());
        selection.setModel(mModel);
        selection.setArrayResourceId(mValueArrayId);
        selection.setColumn(mColumn);
        selection.setWidgetType(mWidgetType);
        return selection;
    }

    // Datetime (dialog with date or date time)
    private View initDateTimeControl(FieldType type) {
        ODateTimeField datetime = new ODateTimeField(mContext);
        mControlData = datetime;
        datetime.setResource(textSize, textAppearance, textColor);
        datetime.setFieldType(type);
        datetime.setParsePattern(mParsePattern);
        datetime.setLabelText(getLabelText());
        datetime.setColumn(mColumn);
        return datetime;
    }

    // Blob (file contents)
    private View initBlobControl() {
        OBlobField blob = new OBlobField(mContext);
        mControlData = blob;
        blob.setDefaultImage(defaultImage);
        blob.setImageSize(mWidgetImageSize);
        blob.setLabelText(getLabelText());
        blob.setColumn(mColumn);
        blob.setWidgetType(mWidgetType);
        return blob;
    }

    private TextView getLabelView() {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        TextView label = new TextView(mContext);
        if (labelSize > -1) {
            label.setTextSize(TypedValue.COMPLEX_UNIT_PX, labelSize);
        }
        if (labelAppearance > -1) {
            label.setTextAppearance(mContext, labelAppearance);
        }
        label.setTextColor(labelColor);
        label.setLayoutParams(params);
        label.setGravity(Gravity.LEFT);
        label.setText(getLabelText());
        label.setAllCaps(true);
        return label;
    }

    public void setIcon(int resourceId) {
        img_icon.setImageResource(resourceId);
    }

    public void setError(String error) {
        mControlData.setError(error);
    }

    public int getIcon() {
        return resId;
    }

    public void setModel(OModel model) {
        mModel = model;
    }

    public OModel getModel() {
        return mModel;
    }

    public OColumn getColumn() {
        return mColumn;
    }

    public void resetData() {
        mControlData.resetData();
    }

    @Override
    public void onValueUpdate(Object value) {
        mValue = value;
        if (mValueUpdateListener != null) {
            mValueUpdateListener.onFieldValueChange(this, value);
        }
        if (value instanceof ODataRow) {
            mValue = ((ODataRow) value).get(OColumn.ROW_ID);
        }
        if (mEditable) {
            if (mControlData.isControlReady()) {
                ODataRow row = new ODataRow();
                if (mOnChangeCallback != null
                        || mOnDomainFilterCallbacks != null) {
                    if (!(value instanceof ODataRow)) {
                        row.put(mColumn.getName(), value);
                    } else {
                        row = (ODataRow) value;
                    }
                }
                if (mOnChangeCallback != null) {
                    mOnChangeCallback.onValueChange(row);
                }
                if (mOnDomainFilterCallbacks != null) {
                    mOnDomainFilterCallbacks.onFieldValueChanged(row.getInt(OColumn.ROW_ID));
                }
            }
        }
    }

    public void setOnValueChangeListener(IOnFieldValueChangeListener listener) {
        mValueUpdateListener = listener;
    }

    @Override
    public void visibleControl(boolean isVisible) {
        if (isVisible) {
            setVisibility(View.VISIBLE);
        } else {
            setVisibility(View.GONE);
        }
    }

    /**
     * OnChange CallBack for column
     *
     * @param callback
     */
    public void setOnChangeCallbackListener(IOnChangeCallback callback) {
        mOnChangeCallback = callback;
    }

    /**
     * Domain Filters
     *
     * @param callback
     */
    public void setOnFilterDomainCallBack(IOnDomainFilterCallbacks callback) {
        mOnDomainFilterCallbacks = callback;
    }

    public interface IOnFieldValueChangeListener {
        void onFieldValueChange(OField field, Object value);
    }
}
