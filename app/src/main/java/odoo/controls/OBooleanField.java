/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 *
 * Created on 7/1/15 5:10 PM
 */
package odoo.controls;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.odoo.core.orm.fields.OColumn;

public class OBooleanField extends LinearLayout implements IOControlData,
        CompoundButton.OnCheckedChangeListener {
    public static final String TAG = OBooleanField.class.getSimpleName();

    private Context mContext;
    private OColumn mColumn;
    private Boolean mEditable = false;
    private String mLabel = null;
    private Boolean mValue = false;
    private OField.WidgetType mWidget = null;
    private ValueUpdateListener mValueUpdateListener = null;
    // Controls
    private TextView txvView = null;
    private CheckBox mCheckbox = null;
    private Switch mSwitch = null;
    private Boolean mReady = false;
    private float textSize = -1;
    private int appearance = -1;
    private int textColor = Color.BLACK;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OBooleanField(Context context, AttributeSet attrs, int defStyleAttr,
                         int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public OBooleanField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    public OBooleanField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public OBooleanField(Context context) {
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

    public void initControl() {
        mReady = false;
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        removeAllViews();
        setOrientation(VERTICAL);
        if (isEditable()) {
            if (mWidget != null) {
                switch (mWidget) {
                    case Switch:
                        mSwitch = new Switch(mContext);
                        mSwitch.setLayoutParams(params);
                        mSwitch.setOnCheckedChangeListener(this);
                        setValue(getValue());
                        if (mLabel != null)
                            mSwitch.setText(mLabel);
                        if (textSize > -1) {
                            mSwitch.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                        }
                        if (appearance > -1) {
                            mSwitch.setTextAppearance(mContext, appearance);
                        }
                        mSwitch.setTextColor(textColor);
                        addView(mSwitch);
                        break;
                    default:
                        break;
                }
            } else {
                mCheckbox = new CheckBox(mContext);
                mCheckbox.setLayoutParams(params);
                mCheckbox.setOnCheckedChangeListener(this);
                if (mLabel != null)
                    mCheckbox.setText(mLabel);
                if (textSize > -1) {
                    mCheckbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                }
                if (appearance > -1) {
                    mCheckbox.setTextAppearance(mContext, appearance);
                }
                mCheckbox.setTextColor(textColor);
                addView(mCheckbox);
            }
        } else {
            txvView = new TextView(mContext);
            txvView.setLayoutParams(params);
            txvView.setText(getCheckBoxLabel());
            if (mLabel != null)
                txvView.setText(mLabel);
            if (textSize > -1) {
                txvView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            }
            if (appearance > -1) {
                txvView.setTextAppearance(mContext, appearance);
            }
            addView(txvView);
        }
    }

    @Override
    public void setValue(Object value) {
        if (value == null)
            return;
        mValue = Boolean.parseBoolean(value.toString());
        if (isEditable()) {
            if (mWidget != null) {
                switch (mWidget) {
                    case Switch:
                        mSwitch.setChecked(Boolean.parseBoolean(getValue()
                                .toString()));
                        break;
                    default:
                        break;
                }
            } else {
                mCheckbox.setChecked(Boolean
                        .parseBoolean(getValue().toString()));
            }
        } else {
            txvView.setText(getCheckBoxLabel());
        }
        if (mValueUpdateListener != null) {
            mValueUpdateListener.onValueUpdate(value);
            if (!isEditable() && mValue == false) {
                mValueUpdateListener.visibleControl(false);
            } else {
                mValueUpdateListener.visibleControl(true);
            }
        }
    }

    @Override
    public View getFieldView() {
        if (isEditable()) {
            if (mWidget != null) {
                switch (mWidget) {
                    case Switch:
                        return mSwitch;
                }
            }
            return mCheckbox;
        } else {
            return txvView;
        }
    }

    @Override
    public void setError(String error) {
        if (error != null)
            Toast.makeText(mContext, error, Toast.LENGTH_LONG).show();
    }

    @Override
    public Object getValue() {
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

    @Override
    public void setLabelText(String label) {
        mLabel = label;
    }

    private String getCheckBoxLabel() {
        String label = "";
        if (getValue() != null && Boolean.parseBoolean(getValue().toString())) {
            label = "✔ ";
        }
        label += getLabel();
        return label;
    }

    @Override
    public String getLabel() {
        if (mLabel != null)
            return mLabel;
        if (mColumn != null)
            return mColumn.getLabel();
        return "unknown";
    }

    @Override
    public void setColumn(OColumn column) {
        mColumn = column;
        if (mLabel == null && mColumn != null)
            mLabel = mColumn.getLabel();
    }

    @Override
    public void setValueUpdateListener(ValueUpdateListener listener) {
        mValueUpdateListener = listener;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setValue(isChecked);
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
        setValue(getValue());
    }

    public void setResource(float textSize, int appearance, int color) {
        this.textSize = textSize;
        this.appearance = appearance;
        this.textColor = color;
    }
}
