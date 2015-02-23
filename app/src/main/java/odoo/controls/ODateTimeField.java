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
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.utils.ODateUtils;

import java.util.Date;

import static odoo.controls.OField.FieldType;

public class ODateTimeField extends LinearLayout implements IOControlData,
        DateTimePicker.PickerCallBack {
    public static final String TAG = ODateTimeField.class.getSimpleName();


    private Context mContext;
    private Boolean mEditable = false;
    private OColumn mColumn;
    private String mLabel, mHint;
    private ValueUpdateListener mValueUpdateListener = null;
    private FieldType mFieldType;
    private TextView txvText;
    private Object mValue;
    private String mParsePattern = ODateUtils.DEFAULT_DATE_FORMAT;
    private DateTimePicker.Builder builder = null;
    private String mDate;
    private Boolean mReady = false;
    private float textSize = -1;
    private int appearance = -1;
    private int textColor = Color.BLACK;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ODateTimeField(Context context, AttributeSet attrs,
                          int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public ODateTimeField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    public ODateTimeField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public ODateTimeField(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr,
                      int defStyleRes) {
        mContext = context;
        if (attrs != null) {

        }
        mReady = false;
        initControl();
    }

    public void setFieldType(FieldType type) {
        mFieldType = type;
        if (mFieldType == FieldType.DateTime) {
            mParsePattern = ODateUtils.DEFAULT_FORMAT;
        }
    }

    @Override
    public void initControl() {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        removeAllViews();
        setOrientation(VERTICAL);
        txvText = new TextView(mContext);
        txvText.setLayoutParams(params);
        txvText.setOnClickListener(null);
        if (isEditable()) {
            txvText.setOnClickListener(onClick);
        }
        if (mValue != null && !mValue.toString().equals("false")) {
            txvText.setText(getDate(mValue.toString(), mParsePattern));
        }
        if (textSize > -1) {
            txvText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        }
        if (appearance > -1) {
            txvText.setTextAppearance(mContext, appearance);
        }
        txvText.setTextColor(textColor);
        addView(txvText);
    }

    @Override
    public void setValue(Object value) {
        mValue = value;
        if (value == null || value.toString().equals("false")) {
            txvText.setText("No Value");
            return;
        }
        txvText.setText(getDate(mValue.toString(), mParsePattern));
        if (mValueUpdateListener != null) {
            mValueUpdateListener.onValueUpdate(value);
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

    @Override
    public Object getValue() {
        if (mValue != null && !TextUtils.isEmpty(mValue.toString())) {
            if (mFieldType == FieldType.Date)
                return mValue.toString().replaceAll(" 00:00:00", "");
            return mValue;
        }
        return null;
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

    View.OnClickListener onClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            builder = new DateTimePicker.Builder(mContext);
            if (mFieldType == FieldType.Date) {
                if (getValue() != null && !getValue().toString().equals("false"))
                    builder.setDate(getValue().toString());
                builder.setType(DateTimePicker.Type.Date);
            } else if (mFieldType == FieldType.Time) {
                if (getValue() != null && !getValue().toString().equals("false"))
                    builder.setTime(getValue().toString());
                builder.setType(DateTimePicker.Type.Time);
            } else {
                if (getValue() != null && !getValue().toString().equals("false"))
                    builder.setDateTime(getValue().toString());
                builder.setType(DateTimePicker.Type.DateTime);
            }
            builder.setCallBack(ODateTimeField.this);
            builder.build().show();
        }
    };

    private String getDate(String date, String format) {
        if (date.contains("now()") || date.contains("NOW()")) {
            mValue = ODateUtils.getUTCDate((mFieldType == FieldType.Date) ? ODateUtils.DEFAULT_DATE_FORMAT
                    : (mFieldType == FieldType.Time) ? ODateUtils.DEFAULT_TIME_FORMAT : ODateUtils.DEFAULT_FORMAT);
            return ODateUtils.getDate(format);
        } else {
            if (mFieldType == FieldType.Date) {
                date += " 00:00:00";
            }
            String defaultFormat = ODateUtils.DEFAULT_FORMAT;
            if (mFieldType == FieldType.Time) {
                defaultFormat = ODateUtils.DEFAULT_TIME_FORMAT;
            }
            return ODateUtils.convertToDefault(date, defaultFormat, format);
        }
    }

    @Override
    public void onDatePick(String date) {
        mDate = date;
        if (mFieldType == FieldType.Date) {
            setValue(mDate + " 00:00:00");
        }
    }

    @Override
    public void onTimePick(String time) {
        String date;
        String format;
        if (mFieldType == FieldType.Time) {
            date = time;
            format = ODateUtils.DEFAULT_TIME_FORMAT;
        } else {
            date = mDate + " " + time;
            format = ODateUtils.DEFAULT_FORMAT;
        }
        Date dt = ODateUtils.createDateObject(date, format, true);
        String utc_date = ODateUtils.getUTCDate(dt, format);
        setValue(utc_date);
    }

    public void setParsePattern(String parsePattern) {
        if (parsePattern != null)
            mParsePattern = parsePattern;
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
