package com.odoo.widgets.slider;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SliderPagerAdapter extends FragmentStatePagerAdapter {

    public static final String KEY_POSITION = "key_pos";
    Context mContext = null;
    List<SliderItem> mItems = new ArrayList<SliderItem>();

    public SliderPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        PageFragment frag = new PageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_POSITION, position);
        frag.setArguments(bundle);
        return frag;

    }

    public void initPager(Context context, List<SliderItem> items) {
        mContext = context;
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    class PageFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View layout = null;
            int pos = getPosition();
            SliderItem item = mItems.get(pos);
            if (item.getSliderCustomViewListener() != null) {
                layout = item.getSliderCustomViewListener().getCustomView(
                        mContext, item, container);
            } else {
                layout = (LinearLayout) inflater.inflate(R.layout.default_ui,
                        container, false);
            }
            return layout;
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            int pos = getPosition();
            SliderItem item = mItems.get(pos);
            if (item.getSliderCustomViewListener() == null) {
                ImageView imgPic = (ImageView) view
                        .findViewById(R.id.view_image);
                TextView txvTitle, txvContent;
                txvTitle = (TextView) view.findViewById(R.id.view_title);
                txvContent = (TextView) view.findViewById(R.id.view_content);
                imgPic.setImageResource(item.getImagePath());
                txvTitle.setText(item.getTitle());
                txvContent.setText(item.getContent());
            }
        }

        private int getPosition() {
            return getArguments().getInt(KEY_POSITION);
        }

    }

    public interface SliderBuilderListener {
        public View getCustomView(Context context, SliderItem item,
                                  ViewGroup parent);
    }

}
