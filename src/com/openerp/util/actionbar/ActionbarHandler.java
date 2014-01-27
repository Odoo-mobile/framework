package com.openerp.util.actionbar;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.TextView;

public class ActionbarHandler {
	Context mContext = null;
	ActionBar mActionbar = null;

	public ActionbarHandler(Context context, ActionBar actionbar) {
		mContext = context;
		mActionbar = actionbar;
	}

	public void applyCustomFonts(Typeface tf) {
		// Updating Title fonts
		setTitleFonts(tf);
	}

	private void setTitleFonts(Typeface tf) {
		int actionBarTitle = Resources.getSystem().getIdentifier(
				"action_bar_title", "id", "android");
		int actionBarSubTitle = Resources.getSystem().getIdentifier(
				"action_bar_subtitle", "id", "android");
		TextView title = (TextView) ((Activity) mContext).getWindow()
				.findViewById(actionBarTitle);
		TextView subTitle = (TextView) ((Activity) mContext).getWindow()
				.findViewById(actionBarSubTitle);
		title.setTypeface(tf);
		title.setTextColor(Color.parseColor("#414141"));
		subTitle.setTypeface(tf);
		subTitle.setTextColor(Color.parseColor("#414141"));
	}

}
