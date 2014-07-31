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

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import odoo.controls.OManyToOneWidget.ManyToOneItemChangeListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.odoo.R;
import com.odoo.orm.OColumn;
import com.odoo.orm.OColumn.RelationType;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OModel;
import com.odoo.orm.types.ODateTime;
import com.odoo.util.Base64Helper;
import com.odoo.util.ODate;
import com.odoo.util.StringUtils;

/**
 * The Class OField.
 */
public class OField extends LinearLayout implements ManyToOneItemChangeListener {
	/** The Constant TAG. */
	public static final String TAG = OField.class.getSimpleName();

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

	/** The Constant KEY_SINGLE_LINE. */
	public static final String KEY_SINGLE_LINE = "singleLine";

	/** The Constant KEY_BINARY_TYPE. */
	public static final String KEY_BINARY_TYPE = "binaryType";

	/** The Constant KEY_DEFAULT_IMAGE. */
	public static final String KEY_DEFAULT_IMAGE = "defaultImage";

	/** The Constant KEY_BOOLEAN_WIDGET. */
	public static final String KEY_BOOLEAN_WIDGET = "booleanWidget";

	/** The Constant KEY_CUSTOM_LAYOUT. */
	public static final String KEY_CUSTOM_LAYOUT = "customLayout";

	/** The Constant KEY_BOTTOM_BORDER_HEIGHT. */
	public static final String KEY_BOTTOM_BORDER_HEIGHT = "bottom_border_height";

	/** The Constant KEY_TEXT_LINES. */
	public static final String KEY_TEXT_LINES = "textLines";

	/** The Constant KEY_REF_COLUMN. */
	public static final String KEY_REF_COLUMN = "ref_column";

	/** The Constant KEY_SHOW_AS_TEXT. */
	public static final String KEY_SHOW_AS_TEXT = "showAsText";

	/** The Constant KEY_DISPLAY_PATTERN. */
	public static final String KEY_DISPLAY_PATTERN = "displayPattern";

	/** The Constant KEY_ROUND_IMAGE_WIDTH_HEIGHT. */
	public static final String KEY_ROUND_IMAGE_WIDTH_HEIGHT = "imageWidthHeight";

	/** The Constant KEY_CUSTOM_LAYOUT_ORIANTATION. */
	public static final String KEY_CUSTOM_LAYOUT_ORIANTATION = "customLayoutOriantation";

	/**
	 * The Enum OFieldMode.
	 */
	enum OFieldMode {

		/** The editable. */
		EDITABLE,
		/** The readonly. */
		READONLY
	}

	/**
	 * The Enum OFieldType.
	 */
	enum OFieldType {

		/** The many to one. */
		MANY_TO_ONE,
		/** The many to many tags. */
		MANY_TO_MANY_TAGS,
		/** The binary. */
		BINARY,
		/** The binary image. */
		BINARY_IMAGE,
		/** The binary rounded image. */
		BINARY_ROUND_IMAGE,
		/** The binary file. */
		BINARY_FILE,
		/** The boolean widget. */
		BOOLEAN_WIDGET,
		/** The boolean checkbox. */
		BOOLEAN_CHECKBOX,
		/** The boolean switch. */
		BOOLEAN_SWITCH,
		/** The boolean radio. */
		BOOLEAN_RADIO,
		/** The web view. */
		WEB_VIEW

	}

	/** The context. */
	Context mContext = null;

	/** The typed array. */
	TypedArray mTypedArray = null;

	/** The m field value. */
	Object mFieldValue = null;

	/** The model used with widget controls. */
	OModel mModel = null;

	/** The m control record. */
	ODataRow mControlRecord = null;

	/** The column object. */
	OColumn mColumn = null;

	/** The control attributes. */
	OControlAttributes mAttributes = new OControlAttributes();

	/** The control field type. */
	OFieldMode mFieldType = OFieldMode.READONLY;

	/** The field widget if any. */
	OFieldType mFieldWidget = null;

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
	RadioGroup mRadioGroup = null;
	RadioButton mTrueRadioButton = null;
	RadioButton mFalseRadioButton = null;
	CheckBox mCheckBox = null;
	Switch mSwitch = null;
	WebView mWebView = null;
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
	private void createWidget(OFieldType fieldWidget) {
		switch (fieldWidget) {
		case MANY_TO_ONE:
			createManyToOneWidget();
			break;
		case BINARY_FILE:
		case BINARY_IMAGE:
			createBinaryControl(fieldWidget, false);
			break;
		case BINARY_ROUND_IMAGE:
			createBinaryControl(fieldWidget, true);
			break;
		case BOOLEAN_WIDGET:
		case BOOLEAN_CHECKBOX:
		case BOOLEAN_RADIO:
		case BOOLEAN_SWITCH:
			createBooleanControl(fieldWidget);
			break;
		case WEB_VIEW:
			createWebView();
			break;
		case MANY_TO_MANY_TAGS:
			createManyToManyTags();
			break;
		default:
			break;
		}
	}

	private void createManyToManyTags() {
		if (mAttributes.getBoolean(KEY_EDITABLE, false)) {

		} else {
			if (mControlRecord != null) {

				List<ODataRow> records = new ArrayList<ODataRow>();
				if (mColumn.getRelationType() == RelationType.OneToMany) {
					records.addAll(mControlRecord.getO2MRecord(
							mColumn.getName()).browseEach());
				} else {
					records.addAll(mControlRecord.getM2MRecord(
							mColumn.getName()).browseEach());
				}
				int customLayoutOriantation = mAttributes.getResource(
						KEY_CUSTOM_LAYOUT_ORIANTATION, -1);
				mLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.WRAP_CONTENT);
				LinearLayout mlayout = new LinearLayout(mContext);
				mlayout.setLayoutParams(mLayoutParams);
				if (customLayoutOriantation == 1)
					mlayout.setOrientation(LinearLayout.VERTICAL);
				else
					mlayout.setOrientation(LinearLayout.HORIZONTAL);
				if (records.size() > 0) {
					int custom_layout = mAttributes.getResource(
							KEY_CUSTOM_LAYOUT, -1);
					for (ODataRow row : records) {
						if (custom_layout > -1) {
							LayoutInflater inflater = LayoutInflater
									.from(mContext);
							OForm form = (OForm) inflater.inflate(
									custom_layout, null);
							form.initForm(row);
							mlayout.addView(form);
						} else {
							TextView mtag = new TextView(mContext);
							mLayoutParams = new LayoutParams(
									LayoutParams.WRAP_CONTENT,
									LayoutParams.WRAP_CONTENT);
							mLayoutParams.setMargins(5, 0, 5, 5);
							mtag.setLayoutParams(mLayoutParams);
							mtag.setPadding(5, 8, 5, 8);
							mtag.setSingleLine(true);
							String ref_column = mAttributes.getString(
									KEY_REF_COLUMN, "name");
							mtag.setText(row.getString(ref_column));
							mtag.setBackgroundColor(Color.LTGRAY);
							mtag.setTypeface(OControlHelper.boldFont());
							mlayout.addView(mtag);
						}
					}
					switch (customLayoutOriantation) {
					case 1: // vertical
						LinearLayout mParentLayout = new LinearLayout(mContext);
						mParentLayout.setLayoutParams(mLayoutParams);
						mParentLayout.setOrientation(LinearLayout.VERTICAL);
						mParentLayout.addView(mlayout);
						addView(mParentLayout);
						break;
					case 0: // horizontal
					default:
						HorizontalScrollView mHScroll = new HorizontalScrollView(
								mContext);
						mHScroll.setLayoutParams(mLayoutParams);
						mHScroll.setHorizontalScrollBarEnabled(false);
						mHScroll.addView(mlayout);
						addView(mHScroll);
						break;
					}

				} else {
					// No any record.
					createTextViewControl();
					setText("No " + mColumn.getLabel());
				}
			}
		}
	}

	private void createWebView() {
		boolean showAsText = mAttributes.getBoolean(KEY_SHOW_AS_TEXT, false);
		if (mAttributes.getBoolean(KEY_EDITABLE, false)) {
			createTextViewControl();
			if (mControlRecord != null) {
				setText(mControlRecord.getString(mColumn.getName()));
			}
		} else {
			if (mControlRecord != null) {
				if (!showAsText) {
					mLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT);
					mWebView = new WebView(mContext);
					mWebView.setLayoutParams(mLayoutParams);
					mWebView.loadData(
							mControlRecord.getString(mColumn.getName()),
							"text/html; charset=UTF-8", "UTF-8");
					mWebView.getSettings().setTextZoom(90);
					mWebView.setBackgroundColor(Color.TRANSPARENT);
					addView(mWebView);
				} else {
					createTextViewControl();
					setText(StringUtils.htmlToString(mControlRecord
							.getString(mColumn.getName())));
				}
			}
		}
	}

	/**
	 * Creates the boolean control.
	 * 
	 * @param fieldWidget
	 *            the field widget
	 */
	private void createBooleanControl(OFieldType fieldWidget) {
		if (mAttributes.getBoolean(KEY_EDITABLE, false)) {
			mLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			Boolean val = (mControlRecord != null) ? mControlRecord
					.getBoolean(mColumn.getName()) : false;
			switch (fieldWidget) {
			case BOOLEAN_CHECKBOX:
				mCheckBox = new CheckBox(mContext);
				mCheckBox.setLayoutParams(mLayoutParams);
				mCheckBox.setText(mColumn.getLabel());
				mCheckBox.setChecked(val);
				addView(mCheckBox);
				break;
			case BOOLEAN_RADIO:
				mRadioGroup = new RadioGroup(mContext);
				mRadioGroup.setLayoutParams(mLayoutParams);
				mRadioGroup.setOrientation(RadioGroup.HORIZONTAL);
				mLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT);
				mTrueRadioButton = new RadioButton(mContext);
				mTrueRadioButton.setLayoutParams(mLayoutParams);
				mTrueRadioButton.setText("True");
				mTrueRadioButton.setId(0x1234 + (int) Math.random());
				if (val)
					mTrueRadioButton.setChecked(val);
				mTrueRadioButton.setTag("true_widget");
				mRadioGroup.addView(mTrueRadioButton);
				mFalseRadioButton = new RadioButton(mContext);
				mFalseRadioButton.setLayoutParams(mLayoutParams);
				mFalseRadioButton.setText("False");
				mFalseRadioButton.setId(0x4321 + (int) Math.random());
				if (!val)
					mFalseRadioButton.setChecked(!val);
				mFalseRadioButton.setTag("false_widget");
				mRadioGroup.addView(mFalseRadioButton);
				addView(mRadioGroup);
				break;
			case BOOLEAN_SWITCH:
				mLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT);
				mLayoutParams.setMargins(0, 8, 0, 8);
				mSwitch = new Switch(mContext);
				mSwitch.setLayoutParams(mLayoutParams);
				mSwitch.setTextOn("True");
				mSwitch.setTextOff("False");
				mSwitch.setChecked(val);
				addView(mSwitch);
				break;
			default:
				break;
			}
		} else {
			createTextViewControl();
			if (mControlRecord != null) {
				String value = mControlRecord.getString(mColumn.getName());
				setText(value);
			}

		}
	}

	/**
	 * Creates the binary control.
	 * 
	 * @param binary_type
	 *            the binary_type
	 * @param roundedImage
	 */
	private void createBinaryControl(OFieldType binary_type,
			boolean roundedImage) {

		ODataRow record = null;
		String column_name = getRefColumn();
		if (mControlRecord != null) {
			if (column_name != null) {
				if (mColumn.getRelationType() == RelationType.ManyToOne) {
					record = mControlRecord.getM2ORecord(mColumn.getName())
							.browse();
				}
			} else {
				record = mControlRecord;
				column_name = mColumn.getName();
			}
		}

		ImageView imgBinary = new ImageView(mContext);
		int heightWidth = mAttributes.getResource(KEY_ROUND_IMAGE_WIDTH_HEIGHT,
				-1);
		if (heightWidth > -1) {
			heightWidth = (int) (heightWidth * mScaleFactor);
			mLayoutParams = new LayoutParams(heightWidth, heightWidth);
			imgBinary.setLayoutParams(mLayoutParams);
		}
		int default_image = mAttributes.getResource(KEY_DEFAULT_IMAGE,
				R.drawable.attachment);
		switch (binary_type) {
		case BINARY_FILE:
			imgBinary
					.setImageResource((default_image < 0) ? R.drawable.attachment
							: default_image);
			break;
		case BINARY_ROUND_IMAGE:
		case BINARY_IMAGE:

			Bitmap binary_image = BitmapFactory
					.decodeResource(mContext.getResources(),
							(default_image < 0) ? R.drawable.attachment
									: default_image);
			if (record != null
					&& !record.getString(column_name).equals("false")
					&& !record.getString(column_name).equals("null")) {
				binary_image = Base64Helper.getBitmapImage(mContext,
						record.getString(column_name));
				if (!roundedImage)
					imgBinary.setScaleType(ScaleType.CENTER_CROP);
			}
			if (roundedImage)
				imgBinary.setImageBitmap(Base64Helper.getRoundedCornerBitmap(
						mContext, binary_image, true));
			else
				imgBinary.setImageBitmap(binary_image);
			break;
		default:
			break;
		}
		addView(imgBinary);
	}

	/**
	 * Creates the many to one widget.
	 */
	private void createManyToOneWidget() {
		String ref_column = mAttributes.getString(KEY_REF_COLUMN, "name");
		if (mAttributes.getBoolean(KEY_EDITABLE, false)) {
			mManyToOne = new OManyToOneWidget(mContext);
			mLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			mManyToOne.setLayoutParams(mLayoutParams);
			mManyToOne.setModel(mModel, ref_column, mColumn.getDomains());
			if (mControlRecord != null)
				mManyToOne.setRecordId((Integer) mControlRecord.getM2ORecord(
						mColumn.getName()).getId());
			mManyToOne.reInit();
			addView(mManyToOne);
			mManyToOne.setOnManyToOneItemChangeListener(this);
		} else {
			createTextViewControl();
			if (mControlRecord != null) {
				ODataRow row = mControlRecord.getM2ORecord(mColumn.getName())
						.browse();
				if (row != null)
					setText(row.getString(ref_column));
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
			mFieldLabel.setBottomBorderHeight(mAttributes.getResource(
					KEY_BOTTOM_BORDER_HEIGHT, 2));
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

	public void setColumn(OColumn column) {
		mColumn = column;
	}

	/**
	 * Creates the text view control.
	 */
	private void createTextViewControl() {
		Integer mAttrFieldTextAppearnce = mAttributes.getResource(
				KEY_FIELD_TEXT_APPEARANCE, 0);
		Integer mAttrFieldStyle = mAttributes.getResource(KEY_FIELD_STYLE, 0);
		Boolean mSingleLine = mAttributes.getBoolean(KEY_SINGLE_LINE, false);
		if (mColumn != null && mColumn.isFunctionalColumn()) {
			mAttributes.put(KEY_EDITABLE, false);
		}
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
			if (mSingleLine) {
				mFieldEditText.setSingleLine(true);
			}
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
			if (mSingleLine) {
				mFieldTextView.setSingleLine(true);
				mFieldTextView.setEllipsize(TruncateAt.END);
			}
			Integer textLines = mAttributes.getResource(KEY_TEXT_LINES, -1);
			if (textLines > 0) {
				mFieldTextView.setLines(textLines);
			}
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
		mFieldType = (editable) ? OFieldMode.EDITABLE : OFieldMode.READONLY;
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
		mAttributes.put(KEY_SINGLE_LINE,
				mTypedArray.getBoolean(R.styleable.OField_singleLine, false));
		mAttributes.put(KEY_BINARY_TYPE,
				mTypedArray.getInt(R.styleable.OField_binaryType, -1));
		int binaryType = mAttributes.getResource(KEY_BINARY_TYPE, -1);
		if (binaryType > -1) {
			mFieldWidget = (binaryType == 0 || binaryType == 3) ? (binaryType == 0) ? OFieldType.BINARY_IMAGE
					: OFieldType.BINARY_ROUND_IMAGE
					: OFieldType.BINARY_FILE;
		}

		mAttributes.put(KEY_DEFAULT_IMAGE,
				mTypedArray.getResourceId(R.styleable.OField_defaultImage, -1));
		int booleanType = mTypedArray.getInt(R.styleable.OField_booleanWidget,
				-1);
		if (booleanType > -1) {
			switch (booleanType) {
			case 0:
				mFieldWidget = OFieldType.BOOLEAN_SWITCH;
				break;
			case 1:
				mFieldWidget = OFieldType.BOOLEAN_RADIO;
				break;
			case 2:
				mFieldWidget = OFieldType.BOOLEAN_CHECKBOX;
				break;
			}
		}
		mAttributes.put(KEY_BOOLEAN_WIDGET, booleanType);
		mAttributes.put(KEY_CUSTOM_LAYOUT,
				mTypedArray.getResourceId(R.styleable.OField_customLayout, -1));
		mAttributes.put(KEY_BOTTOM_BORDER_HEIGHT, mTypedArray.getInt(
				R.styleable.OField_label_bottom_border_height, 2));
		mAttributes.put(KEY_TEXT_LINES,
				mTypedArray.getInt(R.styleable.OField_textLines, -1));
		mAttributes.put(KEY_REF_COLUMN,
				mTypedArray.getString(R.styleable.OField_ref_column));
		mAttributes.put(KEY_SHOW_AS_TEXT,
				mTypedArray.getBoolean(R.styleable.OField_showAsText, false));
		mAttributes.put(KEY_DISPLAY_PATTERN,
				mTypedArray.getString(R.styleable.OField_displayPattern));
		mAttributes.put(KEY_ROUND_IMAGE_WIDTH_HEIGHT,
				mTypedArray.getInt(R.styleable.OField_imageWidthHeight, -1));
		mAttributes.put(KEY_CUSTOM_LAYOUT_ORIANTATION, mTypedArray.getInt(
				R.styleable.OField_customLayoutOriantation, -1));
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
		OFieldMode field_type = (editable) ? OFieldMode.EDITABLE
				: OFieldMode.READONLY;
		if (field_type != mFieldType) {
			mFieldType = (editable) ? OFieldMode.EDITABLE : OFieldMode.READONLY;
			mAttributes.put(KEY_EDITABLE, editable);
			initControls();
		}
	}

	/**
	 * Gets the field type.
	 * 
	 * @return the type
	 */
	public OFieldMode getType() {
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
			String displayPattern = mAttributes.getString(KEY_DISPLAY_PATTERN,
					null);
			if (displayPattern != null
					&& mColumn.getType().isAssignableFrom(ODateTime.class)) {
				text = ODate.getDate(mContext, text, TimeZone.getDefault()
						.getID(), displayPattern);
			}
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
	 * Creates the control.
	 * 
	 * @param type
	 *            the type
	 * @param column
	 *            the column
	 * @param record
	 *            the record
	 */
	public void createControl(OFieldType type, OColumn column, ODataRow record) {
		if (type != OFieldType.BINARY && type != OFieldType.BOOLEAN_WIDGET
				&& type != OFieldType.WEB_VIEW) {
			mFieldWidget = type;
			mModel = new OModel(mContext, null)
					.createInstance(column.getType());
		}
		if (type == OFieldType.WEB_VIEW) {
			mFieldWidget = type;
		}
		mColumn = column;
		mControlRecord = record;
		initControls();
	}

	/**
	 * Gets the value.
	 * 
	 * @return the value
	 */
	public Object getValue() {
		if (mFieldWidget != null) {
			switch (mFieldWidget) {
			case BOOLEAN_CHECKBOX:
				return mCheckBox.isChecked();
			case BOOLEAN_RADIO:
				return mTrueRadioButton.isChecked();
			case BOOLEAN_SWITCH:
				return mSwitch.isChecked();
			case MANY_TO_ONE:
				return mFieldValue;
			default:
				return getText();
			}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see odoo.controls.OManyToOneWidget.ManyToOneItemChangeListener#
	 * onManyToOneItemChangeListener(com.odoo.orm.OColumn,
	 * com.odoo.orm.ODataRow)
	 */
	@Override
	public void onManyToOneItemChangeListener(OColumn column, ODataRow row) {
		mFieldValue = row.get(OColumn.ROW_ID);
	}

	public String getRefColumn() {
		return mAttributes.getString(KEY_REF_COLUMN, null);
	}
}