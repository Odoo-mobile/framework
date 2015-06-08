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
 * Created on 13/3/15 3:35 PM
 */
package com.odoo.news;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.odoo.R;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.support.addons.fragment.BaseFragment;
import com.odoo.core.support.drawer.ODrawerItem;
import com.odoo.core.support.list.OCursorListAdapter;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.OCursorUtils;
import com.odoo.core.utils.StringUtils;
import com.odoo.news.models.OdooNews;

import java.util.List;

public class News extends BaseFragment implements OCursorListAdapter.
        OnViewBindListener, LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {
    public static final String TAG = News.class.getSimpleName();
    private OdooNews news = null;
    private View mView = null;
    private ListView mList = null;
    private OCursorListAdapter mAdapter = null;
    private DataRefreshReceiver dataRefreshReceiver = null;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        news = new OdooNews(getActivity(), null);
        return inflater.inflate(R.layout.news_list, container, false);
    }

    @Override
    public List<ODrawerItem> drawerMenus(Context context) {
        return null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mView = view;
        dataRefreshReceiver = new DataRefreshReceiver();
        initAdapter();
    }

    private void initAdapter() {
        mList = (ListView) mView.findViewById(R.id.newsList);
        mAdapter = new OCursorListAdapter(getActivity(), null, R.layout.odoo_news);
        mAdapter.setOnViewBindListener(this);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Class<OdooNews> database() {
        return OdooNews.class;
    }


    @Override
    public void onViewBind(View view, Cursor cursor, ODataRow row) {
        OControls.setText(view, R.id.subject, row.getString("subject"));
        OControls.setText(view, R.id.message, StringUtils.htmlToString(row.getString("message")));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), db().uri(), null, null, null, OColumn.ROW_ID + " desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    @Override
    public void onResume() {
        super.onResume();
//        parent().registerReceiver(dataRefreshReceiver, new IntentFilter(Odoo.ACTION_ODOO_UPDATES));
        getLoaderManager().restartLoader(0, null, News.this);
    }

    @Override
    public void onPause() {
        super.onPause();
        parent().unregisterReceiver(dataRefreshReceiver);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        NewsDetail nDetail = new NewsDetail();
        ODataRow row = OCursorUtils.toDatarow((Cursor) mAdapter.getItem(position));
        nDetail.setArguments(row.getPrimaryBundleData());
        startFragment(nDetail, true);
    }

    class DataRefreshReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            getLoaderManager().restartLoader(0, null, News.this);
        }
    }
}
