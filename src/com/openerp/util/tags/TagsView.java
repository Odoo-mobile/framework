/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http:www.openerp.com>)
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
package com.openerp.util.tags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TagsView extends MultiTagsTextView implements
		MultiTagsTextView.TokenListener {

	HashMap<String, TagsItem> selectedTags = new HashMap<String, TagsItem>();
	Context mContext = null;
	CustomTagViewListener mCustomTagView = null;
	NewTokenCreateListener mNewTokenListener = null;

	public TagsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setTokenListener(this);
		mContext = context;
		setTypeFace(getTextStyle(attrs));
	}

	@Override
	protected Object defaultObject(String completionText) {
		if (mNewTokenListener != null) {
			return (TagsItem) mNewTokenListener
					.newTokenAddListener(completionText);
		}
		return null;
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

	@Override
	protected View getViewForObject(Object object) {
		View view = null;
		ViewGroup tagsParentView = (ViewGroup) TagsView.this.getParent();
		LayoutInflater l = (LayoutInflater) getContext().getSystemService(
				Activity.LAYOUT_INFLATER_SERVICE);
		if (mCustomTagView != null) {
			view = mCustomTagView.getViewForTags(l, object, tagsParentView);
		}
		return view;
	}

	public void setCustomTagView(CustomTagViewListener customTagView) {
		mCustomTagView = customTagView;
	}

	public void setNewTokenCreateListener(
			NewTokenCreateListener newTokenListener) {
		mNewTokenListener = newTokenListener;
	}

	@Override
	public void onTokenAdded(Object obj, View view) {
		final TagsItem item = (TagsItem) obj;
		selectedTags.put("id_" + item.getId(), item);
	}

	@Override
	public void onTokenRemoved(Object arg0) {
		TagsItem item = (TagsItem) arg0;
		if (selectedTags.containsKey("id_" + item.getId())) {
			selectedTags.remove("id_" + item.getId());
		}
	}

	public List<TagsItem> getSelectedTags() {
		List<TagsItem> items = new ArrayList<TagsItem>();
		for (String key : selectedTags.keySet()) {
			items.add(selectedTags.get(key));
		}
		return items;
	}

	@Override
	public void onTokenSelected(Object token, View view) {
	}

	public interface CustomTagViewListener {
		public View getViewForTags(LayoutInflater layoutInflater,
				Object object, ViewGroup tagsViewGroup);
	}

	public interface NewTokenCreateListener {
		public TagsItem newTokenAddListener(String token);
	}
}