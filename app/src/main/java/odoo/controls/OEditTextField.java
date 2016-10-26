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
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.utils.ODateUtils;

public class OEditTextField extends LinearLayout implements IOControlData,
        View.OnFocusChangeListener {
    public static final String TAG = OEditTextField.class.getSimpleName();

    private Context mContext;
    private EditText edtText;
    private TextView txvText;
    private Boolean mEditable = false, mReady = false;
    private OField.WidgetType mWidget = null;
    private OColumn mColumn;
    private String mLabel, mHint;
    private ValueUpdateListener mValueUpdateListener = null;
    private float textSize = -1;
    private int appearance = -1;
    private int textColor = Color.BLACK;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OEditTextField(Context context, AttributeSet attrs,
                          int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public OEditTextField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    public OEditTextField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public OEditTextField(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr,
                      int defStyleRes) {
        mContext = context;
        if (attrs != null) {

        }
        mReady = false;
        if (mContext.getClass().getSimpleName().contains("BridgeContext"))
            initControl();
    }

    public void initControl() {
        // Creating control
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        removeAllViews();
        setOrientation(VERTICAL);
        if (mEditable) {
            edtText = new EditText(mContext);
            edtText.setTypeface(OControlHelper.lightFont());
            edtText.setLayoutParams(params);
            edtText.setBackgroundColor(Color.TRANSPARENT);
            edtText.setPadding(0, 10, 10, 10);
            edtText.setHint(getLabel());
            edtText.setOnFocusChangeListener(this);
            if (textSize > -1) {
                edtText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            }
            if (appearance > -1) {
                edtText.setTextAppearance(mContext, appearance);
            }
            edtText.setTextColor(textColor);
            addView(edtText);
        } else {
            txvText = new TextView(mContext);
            txvText.setTypeface(OControlHelper.lightFont());
            txvText.setLayoutParams(params);
            txvText.setBackgroundColor(Color.TRANSPARENT);
            txvText.setPadding(0, 10, 10, 10);
            if (textSize > -1) {
                txvText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            }
            if (appearance > -1) {
                txvText.setTextAppearance(mContext, appearance);
            }
            txvText.setTextColor(textColor);
            addView(txvText);
        }
    }

    public void setWidgetType(OField.WidgetType type) {
        mWidget = type;
        initControl();
    }

    @Override
    public void setValue(Object value) {
        if (value == null)
            return;
        if (value.toString().equals("false")) {
            value = "";
        } else if (mWidget == OField.WidgetType.Duration) {
            value = ODateUtils.floatToDuration(value.toString());
        }
        if (mEditable) {
            edtText.setText(value.toString());
        } else {
            txvText.setText(value.toString());
        }
        if (mValueUpdateListener != null) {
            mValueUpdateListener.onValueUpdate(value);
        }
    }

    @Override
    public View getFieldView() {
        if (mEditable)
            return edtText;
        return txvText;
    }


    @Override
    public void setError(String error) {
        if (mEditable) {
            edtText.setError(error);
        }
    }

    @Override
    public Object getValue() {
        Object value = null;
        if (mEditable)
            value = edtText.getText().toString();
        else if (txvText != null)
            value = txvText.getText().toString();
        if ((value != null || !value.toString().equals("false")) && mWidget == OField.WidgetType.Duration) {
            value = ODateUtils.durationToFloat(value.toString());
        }
        return value;
    }

    @Override
    public void setEditable(Boolean editable) {
        if (mEditable != editable) {
            mEditable = editable;
        }
    }

    @Override
    public Boolean isEditable() {
        return mEditable;
    }

    public void setHint(String hint) {
        mHint = hint;
    }

    @Override
    public void setLabelText(String label) {
        mLabel = label;
    }

    @Override
    public void setColumn(OColumn column) {
        mColumn = column;
    }

    @Override
    public String getLabel() {
        if (mLabel != null)
            return mLabel;
        if (mColumn != null)
            return mColumn.getLabel();
        if (mHint != null)
            return mHint;
        return "unknown";
    }

    @Override
    public void setValueUpdateListener(ValueUpdateListener listener) {
        mValueUpdateListener = listener;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus && edtText.getText().length() > 0) {
            setValue(getValue());
        }
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
