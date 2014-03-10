package com.openerp.util;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.View.MeasureSpec;

import com.openerp.util.controls.OETextView;

public class TextViewTags {

	String mColor = null;
	String mTextColor = null;
	Context mContext;
	List<String> mTokens = null;
	int mTextSize = 14;

	public TextViewTags(Context context, List<String> tokens,
			String backgroundHexColor, String textColor, int textSize) {
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
			OETextView txvTag = createTokenTextView(" " + tag + " ");
			BitmapDrawable bd = (BitmapDrawable) convertViewToDrawable(txvTag);
			bd.setBounds(0, 0, bd.getIntrinsicWidth(), bd.getIntrinsicHeight());
			ssb.setSpan(new ImageSpan(bd), pos, pos + tag.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			pos = pos + tag.length() + 1;
		}
		return ssb;
	}

	private OETextView createTokenTextView(String text) {
		OETextView tv = new OETextView(mContext);
		tv.setText(text);
		tv.setTextSize(mTextSize);
		return tv;
	}

	@SuppressWarnings("deprecation")
	private Object convertViewToDrawable(OETextView view) {
		view.setBackgroundColor(Color.parseColor(mColor));
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
