/*
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
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
package odoo.controls;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.odoo.R;
import com.odoo.orm.OColumn;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OModel;
import com.odoo.support.fragment.BaseFragment;
import com.odoo.util.drawer.DrawerItem;

/**
 * The Class OViewPager.
 */
public class OViewPager extends ViewPager implements OViewPagerObjectListener {

	/** The Constant TAG. */
	public static final String TAG = OViewPager.class.getSimpleName();

	/** The Constant KEY_MODEL_NAME. */
	public static final String KEY_MODEL_NAME = "pagger_model_name";

	/** The Constant KEY_VIEW_COLUMN. */
	public static final String KEY_VIEW_COLUMN = "view_column_name";

	/** The Constant KEY_SHOW_TITLE. */
	public static final String KEY_SHOW_TITLE = "show_title";

	/** The m context. */
	private Context mContext = null;

	/** The m typed array. */
	private TypedArray mTypedArray = null;

	/** The m attr. */
	private OControlAttributes mAttr = new OControlAttributes();

	/** The m model. */
	private OModel mModel = null;

	/** The m column. */
	private OColumn mColumn = null;

	/** The m on pagger get view. */
	private OnPaggerGetView mOnPaggerGetView = null;

	/** The m objects. */
	private List<ODataRow> mObjects = new ArrayList<ODataRow>();

	/** The m pagger adapter. */
	private PaggerAdapter mPaggerAdapter = null;

	/** The m zoom out page transformer. */
	private ZoomOutPageTransformer mZoomOutPageTransformer = null;

	/**
	 * Instantiates a new o view pager.
	 * 
	 * @param context
	 *            the context
	 */
	public OViewPager(Context context) {
		super(context);
		init(context, null, 0);
	}

	/**
	 * Instantiates a new o view pager.
	 * 
	 * @param context
	 *            the context
	 * @param attrs
	 *            the attrs
	 */
	public OViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	/**
	 * Inits the.
	 * 
	 * @param context
	 *            the context
	 * @param attrs
	 *            the attrs
	 * @param defStyle
	 *            the def style
	 */
	private void init(Context context, AttributeSet attrs, int defStyle) {
		mContext = context;
		if (attrs != null) {
			mTypedArray = mContext.obtainStyledAttributes(attrs,
					R.styleable.OViewPager);
			initAttributeValues();
			mTypedArray.recycle();
		}
	}

	/**
	 * Inits the attribute values.
	 */
	private void initAttributeValues() {
		mAttr.put(KEY_MODEL_NAME,
				mTypedArray.getString(R.styleable.OViewPager_pager_model));
		String model = mAttr.getString(KEY_MODEL_NAME, null);
		if (model != null) {
			mModel = OModel.get(mContext, model);
		}
		mAttr.put(KEY_VIEW_COLUMN,
				mTypedArray.getString(R.styleable.OViewPager_view_column_name));
		String column = mAttr.getString(KEY_VIEW_COLUMN, null);
		if (column != null)
			mColumn = mModel.getColumn(column);
	}

	/**
	 * Inits the pagger.
	 */
	private void initPagger() {
		Log.v(TAG, "initPagger()");
		mObjects = mModel.select();
		setBackgroundColor(Color.GRAY);
		mPaggerAdapter = new PaggerAdapter(this,
				mOnPaggerGetView.getPaggerFragmentManager(), mOnPaggerGetView,
				this);
		mZoomOutPageTransformer = new ZoomOutPageTransformer();
		setPageTransformer(true, mZoomOutPageTransformer);
		setAdapter(mPaggerAdapter);
	}

	/**
	 * Sets the on pagger get view.
	 * 
	 * @param listener
	 *            the new on pagger get view
	 */
	public void setOnPaggerGetView(OnPaggerGetView listener) {
		mOnPaggerGetView = listener;
		initPagger();
	}

	/**
	 * The Class PaggerAdapter.
	 */
	class PaggerAdapter extends FragmentStatePagerAdapter {

		/** The m pagger get view. */
		OnPaggerGetView mPaggerGetView = null;

		/** The m object listener. */
		OViewPagerObjectListener mObjectListener = null;

		/** The m view pagger. */
		OViewPager mViewPagger = null;

		/**
		 * Instantiates a new pagger adapter.
		 * 
		 * @param pagger
		 *            the pagger
		 * @param fm
		 *            the fm
		 * @param view_listener
		 *            the view_listener
		 * @param object_listener
		 *            the object_listener
		 */
		public PaggerAdapter(OViewPager pagger, FragmentManager fm,
				OnPaggerGetView view_listener,
				OViewPagerObjectListener object_listener) {
			super(fm);
			mViewPagger = pagger;
			mPaggerGetView = view_listener;
			mObjectListener = object_listener;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.support.v4.view.PagerAdapter#getPageTitle(int)
		 */
		@Override
		public CharSequence getPageTitle(int position) {
			return mObjectListener.getObject(position).getString(
					mObjectListener.getColumn().getName());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.support.v4.app.FragmentStatePagerAdapter#getItem(int)
		 */
		@Override
		public Fragment getItem(int position) {
			ScreenPageFragment fragment = new ScreenPageFragment(mViewPagger,
					position, mPaggerGetView, mObjectListener);
			return fragment;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.support.v4.view.PagerAdapter#getCount()
		 */
		@Override
		public int getCount() {
			return mObjectListener.objectCount();
		}
	}

	/**
	 * The Class ScreenPageFragment.
	 */
	public static class ScreenPageFragment extends BaseFragment {

		/** The m pagger get view. */
		OnPaggerGetView mPaggerGetView = null;

		/** The m object listener. */
		OViewPagerObjectListener mObjectListener = null;

		/** The m position. */
		Integer mPosition = -1;

		/** The m view pagger. */
		OViewPager mViewPagger = null;

		View mView = null;

		/**
		 * Instantiates a new screen page fragment.
		 */
		public ScreenPageFragment() {
		}

		/**
		 * Instantiates a new screen page fragment.
		 * 
		 * @param pagger
		 *            the pagger
		 * @param position
		 *            the position
		 * @param view_listener
		 *            the view_listener
		 * @param listener
		 *            the listener
		 */
		public ScreenPageFragment(OViewPager pagger, int position,
				OnPaggerGetView view_listener, OViewPagerObjectListener listener) {
			mViewPagger = pagger;
			mPosition = position;
			mPaggerGetView = view_listener;
			mObjectListener = listener;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater
		 * , android.view.ViewGroup, android.os.Bundle)
		 */
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			setRetainInstance(true);
			if (mPaggerGetView != null) {
				init();
				if (mView != null)
					return mView;
			}
			mView = super.onCreateView(inflater, container, savedInstanceState);
			return mView;
		}

		private void init() {
			mView = mPaggerGetView.paggerGetView(getActivity(), mViewPagger,
					mObjectListener.getObject(mPosition), mPosition);
		}

		@Override
		public void onResume() {
			super.onResume();
			init();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.odoo.support.fragment.OModuleHelper#databaseHelper(android.content
		 * .Context)
		 */
		@Override
		public Object databaseHelper(Context context) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.odoo.support.fragment.OModuleHelper#drawerMenus(android.content
		 * .Context)
		 */
		@Override
		public List<DrawerItem> drawerMenus(Context context) {
			return null;
		}

	}

	/**
	 * The Class ZoomOutPageTransformer.
	 */
	class ZoomOutPageTransformer implements ViewPager.PageTransformer {

		/** The Constant MIN_SCALE. */
		private static final float MIN_SCALE = 0.85f;

		/** The Constant MIN_ALPHA. */
		private static final float MIN_ALPHA = 0.5f;

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.support.v4.view.ViewPager.PageTransformer#transformPage(android
		 * .view.View, float)
		 */
		public void transformPage(View view, float position) {
			int pageWidth = view.getWidth();
			int pageHeight = view.getHeight();

			if (position < -1) { // [-Infinity,-1)
				// This page is way off-screen to the left.
				view.setAlpha(0);

			} else if (position <= 1) { // [-1,1]
				// Modify the default slide transition to shrink the page as
				// well
				float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
				float vertMargin = pageHeight * (1 - scaleFactor) / 2;
				float horzMargin = pageWidth * (1 - scaleFactor) / 2;
				if (position < 0) {
					view.setTranslationX(horzMargin - vertMargin / 2);
				} else {
					view.setTranslationX(-horzMargin + vertMargin / 2);
				}

				// Scale the page down (between MIN_SCALE and 1)
				view.setScaleX(scaleFactor);
				view.setScaleY(scaleFactor);

				// Fade the page relative to its size.
				view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE)
						/ (1 - MIN_SCALE) * (1 - MIN_ALPHA));

			} else { // (1,+Infinity]
				// This page is way off-screen to the right.
				view.setAlpha(0);
			}
		}
	}

	/**
	 * The Interface OnPaggerGetView.
	 */
	public interface OnPaggerGetView {

		/**
		 * Pagger get view.
		 * 
		 * @param context
		 *            the context
		 * @param view
		 *            the view
		 * @param object
		 *            the object
		 * @param position
		 *            the position
		 * @return the view
		 */
		public View paggerGetView(Context context, View view, ODataRow object,
				int position);

		/**
		 * Gets the pagger fragment manager.
		 * 
		 * @return the pagger fragment manager
		 */
		public FragmentManager getPaggerFragmentManager();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odoo.controls.OViewPagerObjectListener#getObject(int)
	 */
	@Override
	public ODataRow getObject(int position) {
		if (position > mObjects.size() - 1) {
			return null;
		}
		return mObjects.get(position);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odoo.controls.OViewPagerObjectListener#getColumn()
	 */
	@Override
	public OColumn getColumn() {
		return mColumn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see odoo.controls.OViewPagerObjectListener#objectCount()
	 */
	@Override
	public int objectCount() {
		return mObjects.size();
	}
}
