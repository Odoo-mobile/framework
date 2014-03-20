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
