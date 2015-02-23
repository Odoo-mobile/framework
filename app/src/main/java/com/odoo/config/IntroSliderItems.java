/**
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
 * Created on 13/2/15 4:16 PM
 */
package com.odoo.config;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.odoo.R;
import com.odoo.core.utils.OControls;
import com.odoo.widgets.slider.SliderItem;
import com.odoo.widgets.slider.SliderPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class IntroSliderItems implements SliderPagerAdapter.SliderBuilderListener {
    public static final String TAG = IntroSliderItems.class.getSimpleName();

    public List<SliderItem> getItems() {
        List<SliderItem> items = new ArrayList<>();
        return items;
    }

    @Override
    public View getCustomView(Context context, SliderItem item, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.base_intro_slider_view, parent, false);
        OControls.setText(view, R.id.big_title, item.getTitle());
        OControls.setImage(view, R.id.slider_image, item.getImagePath());
        OControls.setText(view, R.id.sub_title, item.getExtras().get("sub_title").toString());
        OControls.setText(view, R.id.description, item.getContent());
        return view;

    }
}
