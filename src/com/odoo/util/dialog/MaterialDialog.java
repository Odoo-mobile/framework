package com.odoo.util.dialog;

import odoo.controls.OControlHelper;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.odoo.R;

public class MaterialDialog extends AlertDialog {

	private Context mContext;
	private TextView mTitle;
	private TextView mContent;
	private Button mPositive;
	private Button mNegative;
	private FrameLayout mCustomContainer;
	private ScrollView mScrollText;

	private String title;
	private String contentText;
	private View customView;
	private Integer customResId;

	private Button.OnClickListener mPositiveClickListener;
	private Button.OnClickListener mNegativeClickListener;

	private String positiveText;
	private String negativeText;
	private boolean canDismiss = true;

	public MaterialDialog(Context context) {
		super(context);
		this.mContext = context;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.base_dialog_layout);
		mTitle = (TextView) findViewById(android.R.id.text1);
		mContent = (TextView) findViewById(android.R.id.text2);
		mCustomContainer = (FrameLayout) findViewById(R.id.content);
		mPositive = (Button) findViewById(android.R.id.button2);
		mNegative = (Button) findViewById(android.R.id.button1);
		mScrollText = (ScrollView) findViewById(R.id.scrolltext);

		mTitle.setTypeface(OControlHelper.boldFont());
		mContent.setTypeface(OControlHelper.lightFont());
	}

	@Override
	public void onStart() {
		super.onStart();
		if (title != null) {
			mTitle.setText(title);
		} else {
			mTitle.setVisibility(View.GONE);
		}

		if (contentText != null) {
			mContent.setText(contentText);
		} else {
			mScrollText.setVisibility(View.GONE);
		}

		if (customView != null && customResId == null) {
			mCustomContainer.addView(customView);
		} else if (customView == null && customResId != null) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			customView = inflater.inflate(customResId, null, false);
			mCustomContainer.addView(customView);
		} else if (customView == null && customResId == null) {
			mContent.setVisibility(View.GONE);
		}

		if (positiveText != null && mPositiveClickListener != null) {
			mPositive.setText(positiveText);
			mPositive.setOnClickListener(mPositiveClickListener);
		} else {
			mPositive.setVisibility(View.GONE);
		}

		if (negativeText != null && mNegativeClickListener != null) {
			mNegative.setText(negativeText);
			mNegative.setOnClickListener(mNegativeClickListener);
		} else {
			mNegative.setVisibility(View.GONE);
		}
		this.setCanceledOnTouchOutside(canDismiss);
		this.getWindow().clearFlags(
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

	}

	public void setTitle(int res_id) {
		this.title = mContext.getResources().getString(res_id);
	}

	public MaterialDialog setTitle(String t) {
		this.title = t;
		return this;
	}

	@Override
	public void setMessage(CharSequence message) {
		super.setMessage(message);
		this.contentText = message.toString();
	}

	public MaterialDialog setMessage(int res_id) {
		this.contentText = mContext.getResources().getString(res_id);
		return this;
	}

	public MaterialDialog setMessage(String m) {
		this.contentText = m;
		return this;
	}

	public MaterialDialog setupPositiveButton(int res_id,
			Button.OnClickListener listener) {
		return setupPositiveButton(mContext.getResources().getString(res_id),
				listener);
	}

	public MaterialDialog setupPositiveButton(String text,
			Button.OnClickListener listener) {
		this.positiveText = text;
		this.mPositiveClickListener = listener;
		return this;
	}

	public MaterialDialog setupNegativeButton(int res_id,
			Button.OnClickListener listener) {
		return setupNegativeButton(mContext.getResources().getString(res_id),
				listener);
	}

	public MaterialDialog setupNegativeButton(String text,
			Button.OnClickListener listener) {
		this.negativeText = text;
		this.mNegativeClickListener = listener;
		return this;
	}

	public MaterialDialog setCustomView(View v) {
		this.customView = v;
		return this;
	}

	public MaterialDialog setCustomViewResource(int ResId) {
		this.customResId = ResId;
		return this;
	}

	public MaterialDialog dismissOnTouchOutside(boolean dismiss) {
		this.canDismiss = dismiss;
		return this;
	}

	public View getCustomView() {
		return this.customView;
	}

}