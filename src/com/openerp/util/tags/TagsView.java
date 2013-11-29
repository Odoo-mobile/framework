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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.openerp.R;
import com.openerp.util.Base64Helper;

public class TagsView extends MultiTagsTextView implements
		MultiTagsTextView.TokenListener {

	HashMap<String, TagsItems> selectedTags = new HashMap<String, TagsItems>();
	Context mContext = null;
	private boolean showImage = true;

	public TagsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setTokenListener(this);
		mContext = context;
	}

	@Override
	protected Object defaultObject(String completionText) {
		// Stupid simple example of guessing if we have an email or not
		int index = completionText.indexOf('@');
		if (index == -1) {
			return new TagsItems(0, completionText, completionText.replace(" ",
					"") + "@example.com");
		} else {
			return new TagsItems(0, completionText.substring(0, index),
					completionText);
		}
	}

	@Override
	protected View getViewForObject(Object object) {
		TagsItems item = (TagsItems) object;
		LayoutInflater l = (LayoutInflater) getContext().getSystemService(
				Activity.LAYOUT_INFLATER_SERVICE);
		LinearLayout view = (LinearLayout) l.inflate(
				R.layout.message_receipient_tag_layout,
				(ViewGroup) TagsView.this.getParent(), false);
		((TextView) view.findViewById(R.id.txvTagSubject)).setText(item
				.getSubject());
		if (!this.showImage) {
			view.findViewById(R.id.imgTagImage).setVisibility(View.GONE);
		}
		if (this.showImage && item.getImage() != null
				&& !item.getImage().equals("false")) {
			((ImageView) view.findViewById(R.id.imgTagImage))
					.setImageBitmap(Base64Helper.getBitmapImage(mContext,
							item.getImage()));
		}
		return view;
	}

	@Override
	public void onTokenAdded(Object obj, View view) {
		final TagsItems item = (TagsItems) obj;
		selectedTags.put("id_" + item.getId(), item);
	}

	@Override
	public void onTokenRemoved(Object arg0) {
		TagsItems item = (TagsItems) arg0;
		if (selectedTags.containsKey("id_" + item.getId())) {
			selectedTags.remove("id_" + item.getId());
		}
	}

	public List<TagsItems> getSelectedTags() {
		List<TagsItems> items = new ArrayList<TagsItems>();
		for (String key : selectedTags.keySet()) {
			items.add(selectedTags.get(key));
		}
		return items;
	}

	public void showImage(boolean flag) {
		this.showImage = flag;
	}

	@Override
	public void onTokenSelected(Object token, View view) {
	}
}