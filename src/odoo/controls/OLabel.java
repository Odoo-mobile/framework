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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.odoo.R;

/**
 * The Class OLabel.
 */
public class OLabel extends LinearLayout {

	/** The context. */
	Context mContext = null;

	/** The typed array. */
	TypedArray mTypedArray = null;

	/** The bottom line view. */
	View mBottomLineView = null;

	/** The label text view. */
	TextView mLabelTextView = null;

	/** The layout params. */
	LayoutParams mLayoutParams = null;

	/** The attr label. */
	String mAttrLabel = null;

	/** The attr color. */
	Integer mAttrColor = Color.BLACK;

	/** The attr text appearance. */
	Integer mAttrTextAppearance = 0;

	/** The attr bottom border height. */
	Integer mAttrBottomBorderHeight = 1;

	/**
	 * Instantiates a new label.
	 * 
	 * @param context
	 *            the context
	 */
	public OLabel(Context context) {
		super(context);
		init(context, null, 0);
	}

	/**
	 * Instantiates a new label.
	 * 
	 * @param context
	 *            the context
	 * @param attrs
	 *            the attrs
	 */
	public OLabel(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	/**
	 * Instantiates a new label.
	 * 
	 * @param context
	 *            the context
	 * @param attrs
	 *            the attrs
	 * @param defStyle
	 *            the def style
	 */
	public OLabel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	/**
	 * Inits the label control.
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
		if (attrs != null) {
			mTypedArray = mContext.obtainStyledAttributes(attrs,
					R.styleable.OLabel);
			initAttributeValues();
			mTypedArray.recycle();
		}
		initControls();
	}

	/**
	 * Inits the controls.
	 */
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

	/**
	 * Inits the attribute values.
	 */
	private void initAttributeValues() {
		mAttrLabel = mTypedArray.getString(R.styleable.OLabel_label);
		mAttrColor = mTypedArray
				.getColor(R.styleable.OLabel_color, Color.BLACK);
		mAttrTextAppearance = mTypedArray.getResourceId(
				R.styleable.OLabel_textAppearance, 0);
		mAttrBottomBorderHeight = mTypedArray.getInteger(
				R.styleable.OLabel_bottom_border_height, 1);
	}

	/**
	 * Sets the label.
	 * 
	 * @param label
	 *            the new label
	 */
	public void setLabel(String label) {
		mAttrLabel = label;
		mLabelTextView.setText(label);
	}

	/**
	 * Gets the label.
	 * 
	 * @return the label
	 */
	public String getLabel() {
		return mAttrLabel;
	}

	/**
	 * Sets the color.
	 * 
	 * @param color
	 *            the new color
	 */
	public void setColor(int color) {
		mAttrColor = color;
		mLabelTextView.setTextColor(color);
		mBottomLineView.setBackgroundColor(color);
	}

	/**
	 * Sets the text appearance.
	 * 
	 * @param textAppearance
	 *            the new text appearance
	 */
	public void setTextAppearance(int textAppearance) {
		mAttrTextAppearance = textAppearance;
		mLabelTextView.setTextAppearance(mContext, textAppearance);
	}

	/**
	 * Sets the bottom border height.
	 * 
	 * @param height
	 *            the new bottom border height
	 */
	public void setBottomBorderHeight(int height) {
		mAttrBottomBorderHeight = height;
		mLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, height);
		mBottomLineView.setLayoutParams(mLayoutParams);
	}

}
