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

public class OField extends LinearLayout implements ManyToOneItemChangeListener {

	public static final String KEY_EDITABLE = "editable";
	public static final String KEY_WITH_LABEL = "with_label";
	public static final String KEY_FIELD_NAME = "field_name";
	public static final String KEY_FIELD_TYPE = "field_type";
	public static final String KEY_LABEL_COLOR = "label_color";
	public static final String KEY_FIELD_COLOR = "field_color";
	public static final String KEY_FIELD_STYLE = "field_style";
	public static final String KEY_LABEL_TEXT_APPEARANCE = "label_text_appearance";
	public static final String KEY_FIELD_TEXT_APPEARANCE = "field_text_appearance";

	enum OFieldType {
		EDITABLE, READONLY
	}

	enum OFieldWidget {
		MANY_TO_ONE
	}

	Context mContext = null;
	TypedArray mTypedArray = null;

	/*
	 * Field value
	 */
	Object mFieldValue = null;

	/*
	 * Widgets
	 */
	OModel mModel = null;
	ODataRow mManyToOneRecord = null;
	OColumn mColumn = null;

	/*
	 * Attributes
	 */
	OControlAttributes mAttributes = new OControlAttributes();
	OFieldType mFieldType = OFieldType.READONLY;
	OFieldWidget mFieldWidget = null;

	/*
	 * Ofield required controls
	 */
	LayoutParams mLayoutParams = null;
	OLabel mFieldLabel = null;
	TextView mFieldTextView = null;
	EditText mFieldEditText = null;
	OManyToOneWidget mManyToOne = null;

	DisplayMetrics mMetrics = null;
	Float mScaleFactor = 0F;

	public OField(Context context) {
		super(context);
		init(context, null, 0);
	}

	public OField(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public OField(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

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

	public void reInit() {
		initControls();
	}

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

	private void createWidget(OFieldWidget fieldWidget) {
		switch (fieldWidget) {
		case MANY_TO_ONE:
			createManyToOneWidget();
			break;
		}
	}

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
				ODataRow row = mManyToOneRecord
						.getM2ORecord(mColumn.getName()).browse();
				if (row != null)
					setText(row.getString("name"));
				else
					setText("No " + mColumn.getLabel());

			}
		}
	}

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
				mFieldLabel.setTextAppearnce(mAttrLabelTextAppearnce);
			mFieldLabel.setColor(mAttributes.getColor(KEY_LABEL_COLOR,
					Color.BLACK));
			addView(mFieldLabel);
		}
		mLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		if (mAttributes.getBoolean(KEY_WITH_LABEL, true))
			mLayoutParams.setMargins(0, 5, 0, 5);
	}

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

	public String getText() {
		if (mAttributes.getBoolean(KEY_EDITABLE, false)) {
			return mFieldEditText.getText().toString();
		} else {
			return mFieldTextView.getText().toString();
		}
	}

	public boolean isEmpty() {
		if (mFieldWidget != null) {
			return ((Integer) mFieldValue == 0);
		} else {
			return TextUtils.isEmpty(getValue().toString());
		}
	}

	@SuppressLint("DefaultLocale")
	public String getFieldTag() {
		boolean editable = mAttributes.getBoolean(KEY_EDITABLE, false);
		String tag = "odoo_field_" + mAttributes.getString(KEY_FIELD_NAME, "")
				+ ((editable) ? "_editable" : "_readonly");
		mFieldType = (editable) ? OFieldType.EDITABLE : OFieldType.READONLY;
		return tag;
	}

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

	public String getFieldName() {
		return mAttributes.getString(KEY_FIELD_NAME, null);
	}

	public void setEditable(boolean editable) {
		OFieldType field_type = (editable) ? OFieldType.EDITABLE
				: OFieldType.READONLY;
		if (field_type != mFieldType) {
			mFieldType = (editable) ? OFieldType.EDITABLE : OFieldType.READONLY;
			mAttributes.put(KEY_EDITABLE, editable);
			initControls();
		}
	}

	public OFieldType getType() {
		return mFieldType;
	}

	public void setText(String text) {
		text = (text.equals("false")) ? "" : text;
		if (mAttributes.getBoolean(KEY_EDITABLE, false)) {
			mFieldEditText.setText(text);
		} else {
			mFieldTextView.setText(text);
		}
	}

	public void setLabel(String label) {
		if (mAttributes.getBoolean(KEY_WITH_LABEL, true)) {
			mFieldLabel.setLabel(label);
			if (mAttributes.getBoolean(KEY_EDITABLE, false)
					&& mFieldEditText != null) {
				mFieldEditText.setHint(label);
			}
		}
	}

	public void setTextStyle(float size) {
		size = size * mScaleFactor;
		if (mAttributes.getBoolean(KEY_EDITABLE, false)) {
			mFieldEditText.setTextSize(size);
		} else {
			mFieldTextView.setTextSize(size);
		}
	}

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

	public void setFieldName(String name) {
		mAttributes.put(KEY_FIELD_NAME, name);
	}

	public void showLabel(boolean withLabel) {
		mAttributes.put(KEY_WITH_LABEL, withLabel);
	}

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

	public Object getValue() {
		if (mFieldWidget != null) {
			return mFieldValue;
		} else {
			return getText();
		}
	}

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
