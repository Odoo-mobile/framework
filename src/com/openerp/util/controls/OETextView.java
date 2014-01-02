package com.openerp.util.controls;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class OETextView extends TextView {
	Context mContext = null;

	public OETextView(Context context) {
		super(context);
		mContext = context;
		setTypeFace("light");
	}

	public OETextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		setTypeFace(getTextStyle(attrs));
	}

	public OETextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		setTypeFace(getTextStyle(attrs));
	}

	private void setTypeFace(String textStyle) {

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
