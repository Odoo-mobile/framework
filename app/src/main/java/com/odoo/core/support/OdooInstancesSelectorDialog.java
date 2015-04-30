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
 * Created on 18/12/14 5:56 PM
 */
package com.odoo.core.support;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.odoo.R;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.OResource;
import com.odoo.core.utils.controls.ExpandableHeightGridView;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import odoo.helper.OdooInstance;


public class OdooInstancesSelectorDialog implements AdapterView.OnItemClickListener {
    private Context mContext;
    private ExpandableHeightGridView mGrid;
    private ArrayAdapter<OdooInstance> mAdapter;
    private List<OdooInstance> instances = new ArrayList<OdooInstance>();
    private AlertDialog dialog;
    private AlertDialog.Builder builder;
    private OnInstanceSelectListener mOnInstanceSelectListener = null;
    private List<ImageLoader> imageLoaderLists = new ArrayList<>();

    public OdooInstancesSelectorDialog(Context context) {
        mContext = context;
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                AbsListView.LayoutParams.WRAP_CONTENT);
        mGrid = new ExpandableHeightGridView(mContext);
        mGrid.setLayoutParams(params);
        mAdapter = new ArrayAdapter<OdooInstance>(mContext, R.layout.base_instance_item, instances) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null)
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.base_instance_item, parent, false);
                generateView(position, convertView, getItem(position));
                return convertView;
            }
        };
        int padd = OResource.dimen(mContext, R.dimen.activity_horizontal_margin);
        mGrid.setPadding(padd, padd, padd, padd);
        mGrid.setNumColumns(2);
        mGrid.setAdapter(mAdapter);
        mGrid.setOnItemClickListener(this);
    }

    public void setInstances(List<OdooInstance> items) {
        instances.clear();
        instances.addAll(items);
        mAdapter.notifyDataSetChanged();
    }

    private void generateView(int position, View view, OdooInstance instance) {
        OControls.setText(view, R.id.txvInstanceUrl, instance.getUrl());
        OControls.setText(view, R.id.txvInstanceName, instance.getCompanyName());
        String imageURL = instance.getUrl() + "/web/binary/company_logo?dbname=" + instance.getDbName();
        ImageLoader imageLoader = new ImageLoader(position, imageURL, R.id.imgInstance);
        imageLoaderLists.add(imageLoader);
        imageLoader.execute();

    }

    public void showDialog() {
        if (dialog != null)
            dialog.dismiss();
        dialog = null;
        builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.label_select_instance);
        builder.setView(mGrid);
        builder.setCancelable(false);
        builder.setNegativeButton(OResource.string(mContext, R.string.label_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mOnInstanceSelectListener != null) {
                            mOnInstanceSelectListener.canceledInstanceSelect();
                        }
                    }
                });
        dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        OdooInstance instance = mAdapter.getItem(position);
        if (dialog != null)
            dialog.dismiss();
        if (mOnInstanceSelectListener != null) {
            for (ImageLoader imageLoader : imageLoaderLists) {
                imageLoader.cancel(true);
            }
            mOnInstanceSelectListener.instanceSelected(instance);
        }
    }

    public void setOnInstanceSelectListener(OnInstanceSelectListener listener) {
        mOnInstanceSelectListener = listener;
    }

    public interface OnInstanceSelectListener {
        public void instanceSelected(OdooInstance instance);

        public void canceledInstanceSelect();
    }

    class ImageLoader extends AsyncTask<Void, Void, Void> {

        String image_url = "";
        int image_view = -1;
        int view_pos = -1;
        Bitmap bmp = null;

        public ImageLoader(int pos, String url, int image_view) {
            view_pos = pos;
            this.image_view = image_view;
            image_url = url;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL(image_url);
                bmp = BitmapFactory.decodeStream(url.openConnection()
                        .getInputStream());
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (bmp != null) {
                View mView = mGrid.getChildAt(view_pos);
                OControls.setImage(mView, image_view, bmp);
            }
        }
    }
}
