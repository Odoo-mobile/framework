package odoo.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.odoo.R;

public class OLabel extends LinearLayout {

	Context mContext = null;
	TypedArray mTypedArray = null;

	/*
	 * Label base control views
	 */
	View mBottomLineView = null;
	TextView mLabelTextView = null;
	LayoutParams mLayoutParams = null;

	/*
	 * Attributes
	 */
	String mAttrLabel = null;
	Integer mAttrColor = Color.BLACK;
	Integer mAttrTextAppearance = 0;
	Integer mAttrBottomBorderHeight = 1;

	public OLabel(Context context) {
		super(context);
		init(context, null, 0);
	}

	public OLabel(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public OLabel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	private void init(Context context, AttributeSet attrs, int defStyle) {
		mContext = context;
		if (attrs != null) {
			mTypedArray = mContext.obtainStyledAttributes(attrs,
					R.styleable.OLabel);
			initAttributeValues();
			mTypedArray.recycle();
		}
		initControls();
	}

	private void initControls() {
		setOrientation(LinearLayout.VERTICAL);

		// Creating label text view
		mLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		mLabelTextView = new TextView(mContext);
		mLabelTextView.setLayoutParams(mLayoutParams);
		mLabelTextView.setPadding(5, 5, 5, 5);
		mLabelTextView.setText(mAttrLabel);
		mLabelTextView.setAllCaps(true);
		mLabelTextView.setTypeface(OControlHelper.lightFont(), Typeface.BOLD);
		if (mAttrTextAppearance != 0)
			mLabelTextView.setTextAppearance(mContext, mAttrTextAppearance);
		mLabelTextView.setTextColor(mAttrColor);
		// Adding label textview
		addView(mLabelTextView);

		// Creating bottom line
		mLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				mAttrBottomBorderHeight);
		mBottomLineView = new View(mContext);
		mBottomLineView.setLayoutParams(mLayoutParams);
		mBottomLineView.setBackgroundColor(mAttrColor);

		// Adding bottom line
		addView(mBottomLineView);
	}

	private void initAttributeValues() {
		mAttrLabel = mTypedArray.getString(R.styleable.OLabel_label);
		mAttrColor = mTypedArray
				.getColor(R.styleable.OLabel_color, Color.BLACK);
		mAttrTextAppearance = mTypedArray.getResourceId(
				R.styleable.OLabel_textAppearance, 0);
		mAttrBottomBorderHeight = mTypedArray.getInteger(
				R.styleable.OLabel_bottom_border_height, 1);
	}

	public void setLabel(String label) {
		mAttrLabel = label;
		mLabelTextView.setText(label);
	}

	public String getLabel() {
		return mAttrLabel;
	}

	public void setColor(int color) {
		mAttrColor = color;
		mLabelTextView.setTextColor(color);
		mBottomLineView.setBackgroundColor(color);
	}

	public void setTextAppearnce(int textAppearance) {
		mAttrTextAppearance = textAppearance;
		mLabelTextView.setTextAppearance(mContext, textAppearance);
	}

	public void setBottomBorderHeight(int height) {
		mAttrBottomBorderHeight = height;
		mLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, height);
		mBottomLineView.setLayoutParams(mLayoutParams);
	}

}
