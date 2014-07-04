/*
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
 */
package odoo.controls;

import odoo.controls.OManyToOneWidget.ManyToOneItemChangeListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.odoo.R;
import com.odoo.orm.OColumn;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OModel;

/**
 * The Class OField.
 */
public class OField extends LinearLayout implements ManyToOneItemChangeListener {

	/** The Constant KEY_EDITABLE. */
	public static final String KEY_EDITABLE = "editable";

	/** The Constant KEY_WITH_LABEL. */
	public static final String KEY_WITH_LABEL = "with_label";

	/** The Constant KEY_FIELD_NAME. */
	public static final String KEY_FIELD_NAME = "field_name";

	/** The Constant KEY_FIELD_TYPE. */
	public static final String KEY_FIELD_TYPE = "field_type";

	/** The Constant KEY_LABEL_COLOR. */
	public static final String KEY_LABEL_COLOR = "label_color";

	/** The Constant KEY_FIELD_COLOR. */
	public static final String KEY_FIELD_COLOR = "field_color";

	/** The Constant KEY_FIELD_STYLE. */
	public static final String KEY_FIELD_STYLE = "field_style";

	/** The Constant KEY_LABEL_TEXT_APPEARANCE. */
	public static final String KEY_LABEL_TEXT_APPEARANCE = "label_text_appearance";

	/** The Constant KEY_FIELD_TEXT_APPEARANCE. */
	public static final String KEY_FIELD_TEXT_APPEARANCE = "field_text_appearance";

	/**
	 * The Enum OFieldType.
	 */
	enum OFieldType {

		/** The editable. */
		EDITABLE,
		/** The readonly. */
		READONLY
	}

	/**
	 * The Enum OFieldWidget.
	 */
	enum OFieldWidget {

		/** The many to one. */
		MANY_TO_ONE
	}

	/** The context. */
	Context mContext = null;

	/** The typed array. */
	TypedArray mTypedArray = null;

	/** The m field value. */
	Object mFieldValue = null;

	/** The model used with widget controls. */
	OModel mModel = null;

	/** The many to one record object. */
	ODataRow mManyToOneRecord = null;

	/** The column object. */
	OColumn mColumn = null;

	/** The control attributes. */
	OControlAttributes mAttributes = new OControlAttributes();

	/** The control field type. */
	OFieldType mFieldType = OFieldType.READONLY;

	/** The field widget if any. */
	OFieldWidget mFieldWidget = null;

	/** The layout params. */
	LayoutParams mLayoutParams = null;

	/** The field label. */
	OLabel mFieldLabel = null;

	/** The field text view. */
	TextView mFieldTextView = null;

	/** The field edit text. */
	EditText mFieldEditText = null;

	/** The many to one widget. */
	OManyToOneWidget mManyToOne = null;

	/** The display metrics. */
	DisplayMetrics mMetrics = null;

	/** The scale factor. */
	Float mScaleFactor = 0F;

	/**
	 * Instantiates a new field.
	 * 
	 * @param context
	 *            the context
	 */
	public OField(Context context) {
		super(context);
		init(context, null, 0);
	}

	/**
	 * Instantiates a new field.
	 * 
	 * @param context
	 *            the context
	 * @param attrs
	 *            the attrs
	 */
	public OField(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	/**
	 * Instantiates a new field.
	 * 
	 * @param context
	 *            the context
	 * @param attrs
	 *            the attrs
	 * @param defStyle
	 *            the def style
	 */
	public OField(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	/**
	 * Inits the control.
	 * 
	 * @param context
	 *            the context
	 * @param attrs
	 *            the attrs
	 * @param defStyle
	 *            the def style
	 */
	private void init(Context context, AttributeSet attrs, int defStyle) {
		mContext = context;
		mMetrics = getResources().getDisplayMetrics();
		mScaleFactor = mMetrics.density;
		if (attrs != null) {
			mTypedArray = mContext.obtainStyledAttributes(attrs,
					R.styleable.OField);
			initAttributeValues();
			mTypedArray.recycle();
		}
		if (mAttributes.size() > 0)
			initControls();
	}

	/**
	 * Re init the control.
	 */
	public void reInit() {
		initControls();
	}

	/**
	 * Inits the controls.
	 */
	private void initControls() {
		removeAllViews();
		setOrientation(LinearLayout.VERTICAL);
		setTag("field_tag_" + mAttributes.getString(KEY_FIELD_NAME, ""));
		createLabel();
		if (mFieldWidget == null)
			createTextViewControl();
		else
			createWidget(mFieldWidget);
	}

	/**
	 * Creates the widget.
	 * 
	 * @param fieldWidget
	 *            the field widget
	 */
	private void createWidget(OFieldWidget fieldWidget) {
		switch (fieldWidget) {
		case MANY_TO_ONE:
			createManyToOneWidget();
			break;
		}
	}

	/**
	 * Creates the many to one widget.
	 */
	private void createManyToOneWidget() {
		if (mAttributes.getBoolean(KEY_EDITABLE, false)) {
			mManyToOne = new OManyToOneWidget(mContext);
			mLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			mManyToOne.setLayoutParams(mLayoutParams);
			mManyToOne.setModel(mModel, "name");
			if (mManyToOneRecord != null)
				mManyToOne.setRecordId((Integer) mManyToOneRecord.getM2ORecord(
						mColumn.getName()).getId());
			mManyToOne.reInit();
			addView(mManyToOne);
		} else {
			createTextViewControl();
			if (mManyToOneRecord != null) {
				ODataRow row = mManyToOneRecord.getM2ORecord(mColumn.getName())
						.browse();
				if (row != null)
					setText(row.getString("name"));
				else
					setText("No " + mColumn.getLabel());

			}
		}
	}

	/**
	 * Creates the label.
	 */
	private void createLabel() {
		mLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		if (mAttributes.getBoolean(KEY_WITH_LABEL, true)) {
			mFieldLabel = new OLabel(mContext);
			mFieldLabel.setLayoutParams(mLayoutParams);
			mFieldLabel.setBottomBorderHeight(2);
			mFieldLabel.setLabel(mAttributes.getString(KEY_FIELD_NAME, ""));
			Integer mAttrLabelTextAppearnce = mAttributes.getResource(
					KEY_LABEL_TEXT_APPEARANCE, 0);
			if (mAttrLabelTextAppearnce != 0)
				mFieldLabel.setTextAppearance(mAttrLabelTextAppearnce);
			mFieldLabel.setColor(mAttributes.getColor(KEY_LABEL_COLOR,
					Color.BLACK));
			addView(mFieldLabel);
		}
		mLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		if (mAttributes.getBoolean(KEY_WITH_LABEL, true))
			mLayoutParams.setMargins(0, 5, 0, 5);
	}

	/**
	 * Creates the text view control.
	 */
	private void createTextViewControl() {
		Integer mAttrFieldTextAppearnce = mAttributes.getResource(
				KEY_FIELD_TEXT_APPEARANCE, 0);
		Integer mAttrFieldStyle = mAttributes.getResource(KEY_FIELD_STYLE, 0);
		if (mAttributes.getBoolean(KEY_EDITABLE, false)) {
			mFieldEditText = new EditText(mContext);
			mFieldEditText.setLayoutParams(mLayoutParams);
			if (mAttrFieldTextAppearnce != 0) {
				mFieldEditText.setTextAppearance(mContext,
						mAttrFieldTextAppearnce);
			}
			mFieldEditText.setTextColor(mAttributes.getColor(KEY_FIELD_COLOR,
					Color.BLACK));
			mFieldEditText.setHint(mAttributes.getString(KEY_FIELD_NAME, ""));
			if (mAttrFieldStyle == Typeface.BOLD
					|| mAttrFieldStyle == Typeface.BOLD_ITALIC) {
				mFieldEditText.setTypeface(OControlHelper.boldFont(),
						mAttrFieldStyle);
			} else {
				mFieldEditText.setTypeface(OControlHelper.lightFont(),
						mAttrFieldStyle);
			}
			mFieldEditText.setTag(getFieldTag());
			addView(mFieldEditText);
		} else {
			mFieldTextView = new TextView(mContext);
			mFieldTextView.setLayoutParams(mLayoutParams);
			if (mAttrFieldTextAppearnce != 0) {
				mFieldTextView.setTextAppearance(mContext,
						mAttrFieldTextAppearnce);
			}
			mFieldTextView.setTextColor(mAttributes.getColor(KEY_FIELD_COLOR,
					Color.BLACK));
			mFieldTextView.setText(mAttributes.getString(KEY_FIELD_NAME, ""));
			mFieldTextView.setPadding(5, 5, 5, 5);
			if (mAttrFieldStyle == Typeface.BOLD
					|| mAttrFieldStyle == Typeface.BOLD_ITALIC) {
				mFieldTextView.setTypeface(OControlHelper.boldFont(),
						mAttrFieldStyle);
			} else {
				mFieldTextView.setTypeface(OControlHelper.lightFont(),
						mAttrFieldStyle);
			}
			mFieldTextView.setTag(getFieldTag());
			addView(mFieldTextView);
		}
	}

	/**
	 * Gets the text.
	 * 
	 * @return the text
	 */
	public String getText() {
		if (mAttributes.getBoolean(KEY_EDITABLE, false)) {
			return mFieldEditText.getText().toString();
		} else {
			return mFieldTextView.getText().toString();
		}
	}

	/**
	 * Checks if control value empty.
	 * 
	 * @return true, if is empty
	 */
	public boolean isEmpty() {
		if (mFieldWidget != null) {
			return ((Integer) mFieldValue == 0);
		} else {
			return TextUtils.isEmpty(getValue().toString());
		}
	}

	/**
	 * Gets the field tag.
	 * 
	 * @return the field tag
	 */
	@SuppressLint("DefaultLocale")
	public String getFieldTag() {
		boolean editable = mAttributes.getBoolean(KEY_EDITABLE, false);
		String tag = "odoo_field_" + mAttributes.getString(KEY_FIELD_NAME, "")
				+ ((editable) ? "_editable" : "_readonly");
		mFieldType = (editable) ? OFieldType.EDITABLE : OFieldType.READONLY;
		return tag;
	}

	/**
	 * Inits the attribute values.
	 */
	private void initAttributeValues() {
		mAttributes.put(KEY_FIELD_NAME,
				mTypedArray.getString(R.styleable.OField_field_name));
		mAttributes.put(KEY_EDITABLE,
				mTypedArray.getBoolean(R.styleable.OField_editable, false));
		mAttributes.put(KEY_WITH_LABEL,
				mTypedArray.getBoolean(R.styleable.OField_with_label, true));
		mAttributes.put(KEY_LABEL_TEXT_APPEARANCE, mTypedArray.getResourceId(
				R.styleable.OField_label_textAppearance, 0));
		mAttributes.put(KEY_LABEL_COLOR, mTypedArray.getColor(
				R.styleable.OField_label_color, Color.BLACK));
		mAttributes.put(KEY_FIELD_COLOR, mTypedArray.getColor(
				R.styleable.OField_field_color, Color.BLACK));
		mAttributes.put(KEY_FIELD_TEXT_APPEARANCE, mTypedArray.getResourceId(
				R.styleable.OField_field_textAppearance, 0));
		String fieldStyle = mTypedArray
				.getString(R.styleable.OField_fieldTextStyle);
		if (fieldStyle != null && fieldStyle.contains("bold")) {
			if (fieldStyle.contains("italic")) {
				mAttributes.put(KEY_FIELD_STYLE, Typeface.BOLD_ITALIC);
			} else {
				mAttributes.put(KEY_FIELD_STYLE, Typeface.BOLD);
			}
		} else {
			mAttributes.put(KEY_FIELD_STYLE, Typeface.NORMAL);
		}
	}

	/**
	 * Gets the field name.
	 * 
	 * @return the field name
	 */
	public String getFieldName() {
		return mAttributes.getString(KEY_FIELD_NAME, null);
	}

	/**
	 * Sets the editable.
	 * 
	 * @param editable
	 *            the new editable
	 */
	public void setEditable(boolean editable) {
		OFieldType field_type = (editable) ? OFieldType.EDITABLE
				: OFieldType.READONLY;
		if (field_type != mFieldType) {
			mFieldType = (editable) ? OFieldType.EDITABLE : OFieldType.READONLY;
			mAttributes.put(KEY_EDITABLE, editable);
			initControls();
		}
	}

	/**
	 * Gets the field type.
	 * 
	 * @return the type
	 */
	public OFieldType getType() {
		return mFieldType;
	}

	/**
	 * Sets the text.
	 * 
	 * @param text
	 *            the new text
	 */
	public void setText(String text) {
		text = (text.equals("false")) ? "" : text;
		if (mAttributes.getBoolean(KEY_EDITABLE, false)) {
			mFieldEditText.setText(text);
		} else {
			mFieldTextView.setText(text);
		}
	}

	/**
	 * Sets the label.
	 * 
	 * @param label
	 *            the new label
	 */
	public void setLabel(String label) {
		if (mAttributes.getBoolean(KEY_WITH_LABEL, true)) {
			mFieldLabel.setLabel(label);
			if (mAttributes.getBoolean(KEY_EDITABLE, false)
					&& mFieldEditText != null) {
				mFieldEditText.setHint(label);
			}
		}
	}

	/**
	 * Sets the text style.
	 * 
	 * @param size
	 *            the new text style
	 */
	public void setTextStyle(float size) {
		size = size * mScaleFactor;
		if (mAttributes.getBoolean(KEY_EDITABLE, false)) {
			mFieldEditText.setTextSize(size);
		} else {
			mFieldTextView.setTextSize(size);
		}
	}

	/**
	 * Sets the error.
	 * 
	 * @param error
	 *            the new error
	 */
	public void setError(String error) {
		if (error == null)
			return;
		if (mFieldWidget == null) {
			mFieldEditText.setError(error);
			mFieldEditText.requestFocus();
		} else {
			Toast.makeText(mContext, error, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Sets the field name.
	 * 
	 * @param name
	 *            the new field name
	 */
	public void setFieldName(String name) {
		mAttributes.put(KEY_FIELD_NAME, name);
	}

	/**
	 * Show label.
	 * 
	 * @param withLabel
	 *            the with label
	 */
	public void showLabel(boolean withLabel) {
		mAttributes.put(KEY_WITH_LABEL, withLabel);
	}

	/**
	 * Show as many to one widget.
	 * 
	 * @param column
	 *            the column
	 * @param record
	 *            the record
	 */
	public void showAsManyToOne(OColumn column, ODataRow record) {
		mFieldWidget = OFieldWidget.MANY_TO_ONE;
		mColumn = column;
		mModel = new OModel(mContext, null).createInstance(column.getType());
		mManyToOneRecord = record;
		initControls();
		if (mManyToOne != null) {
			mManyToOne.setOnManyToOneItemChangeListener(this);
		}
	}

	/**
	 * Gets the value.
	 * 
	 * @return the value
	 */
	public Object getValue() {
		if (mFieldWidget != null) {
			return mFieldValue;
		} else {
			return getText();
		}
	}

	/**
	 * Checks if is dirty field.
	 * 
	 * @return the boolean
	 */
	public Boolean isDirtyField() {
		if (mFieldWidget != null) {
			return mManyToOne.isDirty();
		} else {
			if (mAttributes.getBoolean(KEY_EDITABLE, false)) {
				mFieldEditText.isDirty();
			}
		}
		return false;
	}

	@Override
	public void onManyToOneItemChangeListener(OColumn column, ODataRow row) {
		mFieldValue = row.get("id");
	}
}
