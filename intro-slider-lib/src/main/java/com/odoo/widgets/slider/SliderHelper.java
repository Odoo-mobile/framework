package com.odoo.widgets.slider;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.odoo.widget.slider.navigator.PagerNavigatorAdapter;

import java.util.ArrayList;
import java.util.List;

public class SliderHelper extends ViewPager {

    private Context mContext;
    private List<SliderItem> mItems = new ArrayList<SliderItem>();
    private SliderPagerAdapter mPagerAdapter = null;
    private PagerNavigatorAdapter mPagerNavigatorAdapter;

    public SliderHelper(Context context) {
        super(context);
        mContext = context;
        _init(context);
    }

    public SliderHelper(Context context, AttributeSet attrs) {
        super(context, attrs);
        _init(context);
    }

    private void _init(Context context) {
        mContext = context;
        mPagerNavigatorAdapter = new PagerNavigatorAdapter(mContext);
    }

    public void init(FragmentManager fragmentManager, List<SliderItem> items) {
        mItems.clear();
        mItems.addAll(items);
        mPagerAdapter = new SliderPagerAdapter(mContext, fragmentManager);
        mPagerAdapter.initPager(mContext, items);
        setAdapter(mPagerAdapter);

        setOnPageChangeListener(mPageChangeListener);

        setPageTransformer(true, new ZoomOutPageTransformer() );
    }

    OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            mPagerNavigatorAdapter.focusOnPagerDot(position);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    public void initNavigator(ViewGroup parent) {
        mPagerNavigatorAdapter.navigator(mPagerAdapter.getCount(), parent);
    }

    public class ZoomOutPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as well
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
                view.setAlpha(MIN_ALPHA +
                        (scaleFactor - MIN_SCALE) /
                                (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
}
