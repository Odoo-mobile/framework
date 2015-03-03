package com.odoo.widgets.slider;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.odoo.widgets.slider.R;

public class SliderView extends LinearLayout {

	private Context mContext;
	private SliderHelper mSlider;

	@SuppressLint("NewApi")
	public SliderView(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	@SuppressLint("NewApi")
	public SliderView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public SliderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SliderView(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		mContext = context;
		setOrientation(LinearLayout.VERTICAL);
		addView(LayoutInflater.from(mContext).inflate(
				R.layout.slider_default_view, this, false));
	}

	public void setItems(FragmentManager fragmentManager, List<SliderItem> items) {
		mSlider = (SliderHelper) findViewById(R.id.default_view_helper);
		mSlider.init(fragmentManager, items);
		mSlider.initNavigator((ViewGroup) findViewById(R.id.footer_dot));
	}

}
