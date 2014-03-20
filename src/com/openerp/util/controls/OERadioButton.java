/**
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http://www.openerp.com>)
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * 
 */
package com.openerp.util.controls;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RadioButton;

public class OERadioButton extends RadioButton {
	Context mContext = null;

	public OERadioButton(Context context) {
		super(context);
		mContext = context;
		setTypeFace("light");
	}

	public OERadioButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		setTypeFace(getTextStyle(attrs));
	}

	public OERadioButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		setTypeFace(getTextStyle(attrs));
	}

	public void setTypeFace(String textStyle) {

		Typeface typeFace = null;
		if (textStyle.equals("light")) {
			typeFace = Typeface.createFromAsset(getResources().getAssets(),
					"fonts/RobotoSlab-Light.ttf");
		}
		if (textStyle.equals("bold")) {
			typeFace = Typeface.createFromAsset(getResources().getAssets(),
					"fonts/RobotoSlab-Bold.ttf");
		}

		if (textStyle.equals("italic")) {
			typeFace = Typeface.createFromAsset(getResources().getAssets(),
					"fonts/RobotoSlab-Regular.ttf");
		}

		setTypeface(typeFace);
	}

	private String getTextStyle(AttributeSet attrs) {
		String textStyle = "light";
		for (int i = 0; i < attrs.getAttributeCount(); i++) {
			String attr = attrs.getAttributeName(i);
			if (attr.equals("textStyle")) {
				textStyle = attrs.getAttributeValue(i);
				if (textStyle.equals("0x1")) {
					textStyle = "bold";
				}
				if (textStyle.equals("0x2")) {
					textStyle = "italic";
				}
			}
		}
		return textStyle;
	}

	public void log(String str) {
		Log.e("OETextView: ", str);
	}
}
