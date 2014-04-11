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
package com.openerp.util;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.View.MeasureSpec;
import android.widget.TextView;

public class TextViewTags {

	int mColor = 0;
	String mTextColor = null;
	Context mContext;
	List<String> mTokens = null;
	int mTextSize = 14;

	public TextViewTags(Context context, List<String> tokens,
			String backgroundHexColor, String textColor, int textSize) {
		mContext = context;
		mColor = Color.parseColor(backgroundHexColor);
		mTextColor = textColor;
		mTokens = tokens;
		mTextSize = textSize;
	}

	public TextViewTags(Context context, List<String> tokens,
			int backgroundHexColor, String textColor, int textSize) {
		mContext = context;
		mColor = backgroundHexColor;
		mTextColor = textColor;
		mTokens = tokens;
		mTextSize = textSize;
	}

	public CharSequence generate() {
		SpannableStringBuilder ssb = new SpannableStringBuilder(TextUtils.join(
				" ", mTokens));
		Paint p = new Paint();
		p.setColor(Color.WHITE);
		int pos = 0;
		for (String tag : mTokens) {
			TextView txvTag = createTokenTextView(" " + tag + " ");
			BitmapDrawable bd = (BitmapDrawable) convertViewToDrawable(txvTag);
			bd.setBounds(0, 0, bd.getIntrinsicWidth(), bd.getIntrinsicHeight());
			ssb.setSpan(new ImageSpan(bd), pos, pos + tag.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			pos = pos + tag.length() + 1;
		}
		return ssb;
	}

	private TextView createTokenTextView(String text) {
		Typeface tf = Typeface.create("sans-serif-light", 0);
		TextView tv = new TextView(mContext);
		tv.setText(text);
		tv.setTextSize(mTextSize);
		tv.setTypeface(tf);
		tv.setPadding(3, 3, 3, 3);
		return tv;
	}

	@SuppressWarnings("deprecation")
	private Object convertViewToDrawable(TextView view) {
		view.setBackgroundColor(mColor);
		view.setTextColor(Color.parseColor(mTextColor));
		view.setPadding(2, 2, 2, 2);
		int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		view.measure(spec, spec);
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		Bitmap b = Bitmap.createBitmap(view.getMeasuredWidth(),
				view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		c.translate(-view.getScrollX(), -view.getScrollY());
		view.draw(c);
		view.setDrawingCacheEnabled(true);
		Bitmap cacheBmp = view.getDrawingCache();
		Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
		view.destroyDrawingCache();
		return new BitmapDrawable(viewBmp);
	}
}
