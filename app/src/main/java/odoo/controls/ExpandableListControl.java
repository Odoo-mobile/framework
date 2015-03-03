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
 * Created on 3/2/15 2:08 PM
 */
package odoo.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class ExpandableListControl extends LinearLayout
        implements ExpandableListOperationListener {
    public static final String TAG = ExpandableListControl.class.getSimpleName();
    private ExpandableListAdapter mAdapter;
    private Context context;

    public ExpandableListControl(Context context) {
        super(context);
        this.context = context;
    }

    public ExpandableListControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public ExpandableListControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @Override
    public void onAdapterDataChange(List<Object> items) {
        removeAllViews();
        for (int i = 0; i < items.size(); i++) {
            View view = mAdapter.getView(i, null, this);
            addView(view);
        }
    }

    public ExpandableListAdapter getAdapter(int resource, List<Object> objects,
                                            final ExpandableListAdapterGetViewListener listener) {
        mAdapter = new ExpandableListAdapter(context, resource, objects) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(context).inflate(getResource(), parent, false);
                }
                if (listener != null) {
                    return listener.getView(position, convertView, parent);
                }
                return convertView;
            }
        };
        mAdapter.setOperationListener(this);
        return mAdapter;
    }


    public abstract static class ExpandableListAdapter {
        private List<Object> objects = new ArrayList<>();
        private Context context;
        private int resource = android.R.layout.simple_list_item_1;
        private ExpandableListOperationListener listener;

        public ExpandableListAdapter(Context context, int resource, List<Object> objects) {
            this.context = context;
            this.objects = objects;
            this.resource = resource;
        }

        public abstract View getView(int position, View convertView, ViewGroup parent);

        public void notifyDataSetChanged(List<Object> items) {
            objects = items;
            listener.onAdapterDataChange(items);
        }

        public Object getItem(int position) {
            return objects.get(position);
        }

        public void setOperationListener(ExpandableListOperationListener listener) {
            this.listener = listener;
        }

        public int getResource() {
            return resource;
        }
    }

    public static interface ExpandableListAdapterGetViewListener {
        public View getView(int position, View view, ViewGroup parent);
    }


}
