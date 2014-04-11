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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TagsView extends MultiTagsTextView {

	HashMap<String, Object> selectedTags = new HashMap<String, Object>();
	Context mContext = null;
	CustomTagViewListener mCustomTagView = null;
	NewTokenCreateListener mNewTokenListener = null;

	public TagsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	@Override
	protected Object defaultObject(String completionText) {
		if (mNewTokenListener != null) {
			return (Object) mNewTokenListener
					.newTokenAddListener(completionText);
		}
		return null;
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

	public List<Object> getSelectedTags() {
		List<Object> items = new ArrayList<Object>();
		for (String key : selectedTags.keySet()) {
			items.add(selectedTags.get(key));
		}
		return items;
	}

	public interface CustomTagViewListener {
		public View getViewForTags(LayoutInflater layoutInflater,
				Object object, ViewGroup tagsViewGroup);
	}

	public interface NewTokenCreateListener {
		public Object newTokenAddListener(String token);
	}
}