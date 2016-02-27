/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p/>
 * Created on 30/12/14 3:29 PM
 */
package com.odoo.core.support.addons.fragment;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncStatusObserver;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;

import com.odoo.App;
import com.odoo.OdooActivity;
import com.odoo.R;
import com.odoo.core.orm.OModel;
import com.odoo.core.service.receivers.ISyncFinishReceiver;
import com.odoo.core.support.OUser;
import com.odoo.core.utils.OResource;


public abstract class BaseFragment extends Fragment implements IBaseFragment {
    private Context mContext;

    private OModel syncStatusObserverModel = null;
    private String drawerRefreshTag = null;
    private Object mSyncObserverHandle;
    private ISyncStatusObserverListener mSyncStatusObserverListener = null;
    private SwipeRefreshLayout mSwipeRefresh = null;
    private IOnSearchViewChangeListener mOnSearchViewChangeListener;
    private SearchView mSearchView;
    private FloatingActionButton mFab = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        parent().setHasActionBarSpinner(false);
    }

    public void setTitle(String title) {
        getActivity().setTitle(title);
    }

    public OModel db() {
        Class<?> model = database();
        if (model != null) {
            return new OModel(mContext, null, user()).createInstance(model);
        }
        return null;
    }

    public OUser user() {
        if (mContext != null)
            return OUser.current(mContext);
        return null;
    }

    public OdooActivity parent() {
        return (OdooActivity) mContext;
    }

    public String _s(int res_id) {
        return OResource.string(mContext, res_id);
    }

    public int _c(int res_id) {
        return OResource.color(mContext, res_id);
    }

    public int _dim(int res_id) {
        return OResource.dimen(mContext, res_id);
    }

    // Sync Observer
    public void setHasSyncStatusObserver(String drawerRefreshTag, ISyncStatusObserverListener syncStatusObserver,
                                         OModel model) {
        this.drawerRefreshTag = drawerRefreshTag;
        mSyncStatusObserverListener = syncStatusObserver;
        syncStatusObserverModel = model;
    }

    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        /** Callback invoked with the sync adapter status changes. */
        @Override
        public void onStatusChanged(int which) {
            boolean refreshing = false;
            switch (which) {
                case ContentResolver.SYNC_OBSERVER_TYPE_PENDING:
                case ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE:
                    refreshing = true;
            }
            final boolean finalRefreshing = refreshing;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSyncStatusObserverListener.onStatusChange(finalRefreshing);
                }
            });
        }
    };


    // Swipe refresh view
    public void setHasSwipeRefreshView(View parent, int resource_id,
                                       SwipeRefreshLayout.OnRefreshListener listener) {
        mSwipeRefresh = (SwipeRefreshLayout) parent.findViewById(resource_id);
        mSwipeRefresh.setOnRefreshListener(listener);
        mSwipeRefresh.setColorSchemeResources(R.color.android_blue,
                R.color.android_green,
                R.color.android_orange_dark,
                R.color.android_red);
    }

    public void setSwipeRefreshing(boolean refreshing) {
        if (mSwipeRefresh != null)
            mSwipeRefresh.setRefreshing(refreshing);
    }

    public void hideRefreshingProgress() {
        if (mSwipeRefresh != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefresh.setRefreshing(false);
                }
            }, 1000);
        }
    }

    public boolean inNetwork() {
        App app = (App) mContext.getApplicationContext();
        return app.inNetwork();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
            mSyncObserverHandle = null;
        }
        try {
            parent().unregisterReceiver(syncFinishReceiver);
        } catch (Exception e) {
            // Skipping issue related to unregister receiver
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onNavSpinnerDestroy();
    }

    protected void onNavSpinnerDestroy() {
        if (parent().getActionBarSpinner() != null) {
            parent().getActionBarSpinner().setAdapter(null);
            parent().setHasActionBarSpinner(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (OUser.current(getActivity()) == null)
            return;
        if (mSyncStatusObserverListener != null) {
            mSyncStatusObserver.onStatusChanged(0);
            int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING
                    | ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
            mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask,
                    mSyncStatusObserver);
        }
        parent().registerReceiver(syncFinishReceiver,
                new IntentFilter(ISyncFinishReceiver.SYNC_FINISH));
    }

    public void setHasSearchView(IOnSearchViewChangeListener listener,
                                 Menu menu, int menu_id) {
        mOnSearchViewChangeListener = listener;
        mSearchView = (SearchView) MenuItemCompat.getActionView(menu
                .findItem(menu_id));
        if (mSearchView != null) {
            mSearchView.setOnCloseListener(closeListener);
            mSearchView.setOnQueryTextListener(searchViewQueryListener);
            mSearchView.setIconifiedByDefault(true);
        }
    }

    private SearchView.OnCloseListener closeListener = new SearchView.OnCloseListener() {

        @Override
        public boolean onClose() {
            // Restore the SearchView if a query was entered
            if (!TextUtils.isEmpty(mSearchView.getQuery())) {
                mSearchView.setQuery(null, true);
            }
            mOnSearchViewChangeListener.onSearchViewClose();
            return true;
        }
    };

    private SearchView.OnQueryTextListener searchViewQueryListener = new SearchView.OnQueryTextListener() {

        public boolean onQueryTextChange(String newText) {
            String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
            return mOnSearchViewChangeListener
                    .onSearchViewTextChange(newFilter);
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            // Don't care about this.
            return true;
        }
    };

    public void setHasFloatingButton(View view, int res_id, ListView list,
                                     View.OnClickListener clickListener) {
        mFab = (FloatingActionButton) view.findViewById(res_id);
        if (mFab != null) {
//            if (list != null)
//                mFab.listenTo(list);
            mFab.setOnClickListener(clickListener);
        }
    }

    public void hideFab() {
        if (mFab != null) {
            mFab.setVisibility(View.GONE);
        }
    }

    public void showFab() {
        if (mFab != null) {
            mFab.setVisibility(View.VISIBLE);
        }
    }

    private ISyncFinishReceiver syncFinishReceiver = new ISyncFinishReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            hideRefreshingProgress();
            if (mSyncStatusObserverListener != null)
                mSyncStatusObserverListener.onStatusChange(false);
        }
    };

    public void startFragment(Fragment fragment, Boolean addToBackState, Bundle extra) {
        parent().loadFragment(fragment, addToBackState, extra);
    }

    public void startFragment(Fragment fragment, Boolean addToBackState) {
        startFragment(fragment, addToBackState, null);
    }
}
