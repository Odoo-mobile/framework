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
 * Created on 13/3/15 5:29 PM
 */
package com.odoo.news;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.odoo.R;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.support.addons.fragment.BaseFragment;
import com.odoo.core.support.drawer.ODrawerItem;
import com.odoo.core.utils.OControls;
import com.odoo.news.models.OdooNews;

import java.util.List;

public class NewsDetail extends BaseFragment {
    public static final String TAG = NewsDetail.class.getSimpleName();
    private int id = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.news_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        OdooNews news = new OdooNews(getActivity(), null);
        getArgument();
        ODataRow row = news.browse(id);
        OControls.setText(view, R.id.detailSubject, row.getString("subject"));
        WebView wvMessage = (WebView) view.findViewById(R.id.detailMessage);
        wvMessage.loadData(row.getString("message"), "text/html;UTF-8", "UTF-8");

    }

    private void getArgument() {
        Bundle b = getArguments();
        id = b.getInt(OColumn.ROW_ID);
    }

    @Override
    public List<ODrawerItem> drawerMenus(Context context) {
        return null;
    }

    @Override
    public <T> Class<T> database() {
        return null;
    }
}
