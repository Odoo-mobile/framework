package com.openerp.util.drawer;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.openerp.R;

public class DrawerIconView extends ImageView {
	private ColorStateList tint;

	public DrawerIconView(Context context) {
		super(context);
	}

	public DrawerIconView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public DrawerIconView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	private void init(Context context, AttributeSet attrs, int defStyle) {

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.DrawerIconView);
		tint = a.getColorStateList(R.styleable.DrawerIconView_tint);
		a.recycle();
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		if (tint != null && tint.isStateful()) {
			updateTintColor();
		}
	}

	public void setColorFilter(ColorStateList tint) {
		this.tint = tint;
		super.setColorFilter(tint.getColorForState(getDrawableState(), 0));
	}

	private void updateTintColor() {
		int color = tint.getColorForState(getDrawableState(), 0);
		setColorFilter(color);
	}
}
