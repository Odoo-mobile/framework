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
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.utils.BitmapUtils;

public class OBlobField extends LinearLayout implements IOControlData {
    public static final String TAG = OBlobField.class.getSimpleName();

    private Context mContext;
    private Boolean mReady = false, isEditable = false;
    private ValueUpdateListener mValueUpdateListener = null;
    private String mLabel;
    private OColumn mCol;
    private Object mValue;
    private BezelImageView imgView;
    private OField.WidgetType mWidget = OField.WidgetType.Image;
    private float imageSize = -1;
    private Integer defaultImage = -1;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OBlobField(Context context, AttributeSet attrs, int defStyleAttr,
                      int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public OBlobField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    public OBlobField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public OBlobField(Context context) {
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

    @Override
    public void initControl() {
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        if (imageSize > -1) {
            params = new LayoutParams((int) imageSize, (int) imageSize);
        }
        removeAllViews();
        setOrientation(VERTICAL);
        imgView = new BezelImageView(mContext);
        imgView.setLayoutParams(params);
        switch (mWidget) {
            case ImageCircle:
                imgView.autoSetMaskDrawable();
            case Image:
                break;
        }
        addView(imgView);
    }

    @Override
    public void setValue(Object value) {
        mValue = value;
        if (mValue != null && imgView != null) {
            if (!mValue.equals("false")) {
                imgView.setImageBitmap(BitmapUtils.getBitmapImage(mContext, mValue.toString()));
            } else if (defaultImage > -1) {
                imgView.setImageResource(defaultImage);
            }
        }
    }


    @Override
    public void setError(String error) {

    }

    @Override
    public View getFieldView() {
        return imgView;
    }

    @Override
    public Object getValue() {
        return mValue;
    }

    @Override
    public void setEditable(Boolean editable) {
        isEditable = editable;
    }

    @Override
    public Boolean isEditable() {
        return isEditable;
    }

    @Override
    public void setColumn(OColumn column) {
        mCol = column;
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

    @Override
    public void setValueUpdateListener(ValueUpdateListener listener) {
        mValueUpdateListener = listener;
    }

    @Override
    public Boolean isControlReady() {
        return mReady;
    }

    @Override
    public void resetData() {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mReady = true;
    }

    public void setWidgetType(OField.WidgetType type) {
        if (type != null) {
            mWidget = type;
            initControl();
        }
    }

    public void setImageSize(float size) {
        imageSize = size;
    }

    public void setDefaultImage(int image) {
        defaultImage = image;
    }
}
