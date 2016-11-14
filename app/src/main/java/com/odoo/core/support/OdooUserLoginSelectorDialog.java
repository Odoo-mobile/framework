/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p>
 * Created on 19/12/14 1:49 PM
 */
package com.odoo.core.support;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.odoo.R;
import com.odoo.core.account.OdooUserAskPassword;
import com.odoo.core.auth.OdooAccountManager;
import com.odoo.core.utils.BitmapUtils;
import com.odoo.core.utils.OAlert;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.OResource;
import com.odoo.core.utils.controls.ExpandableHeightGridView;

import java.util.List;

import odoo.controls.BezelImageView;

public class OdooUserLoginSelectorDialog implements AdapterView.OnItemClickListener {
    private Context mContext;
    private ExpandableHeightGridView mGrid;
    private ArrayAdapter<OUser> mAdapter;
    private AlertDialog dialog;
    private AlertDialog.Builder builder;
    private OUser mUser;
    private IUserLoginSelectListener mIUserLoginSelectListener = null;
    private OdooUserAskPassword askPassword = null;

    public OdooUserLoginSelectorDialog(Context context) {
        mContext = context;
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                AbsListView.LayoutParams.WRAP_CONTENT);
        mGrid = new ExpandableHeightGridView(mContext);
        mGrid.setLayoutParams(params);
        List<OUser> accounts = OdooAccountManager.getAllAccounts(mContext);
        mAdapter = new ArrayAdapter<OUser>(mContext, R.layout.base_instance_item, accounts) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null)
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.base_instance_item, parent, false);
                generateView(position, convertView, getItem(position));
                return convertView;
            }
        };
        int padding = OResource.dimen(mContext, R.dimen.activity_horizontal_margin);
        mGrid.setPadding(padding, padding, padding, padding);
        if (accounts.size() > 1)
            mGrid.setNumColumns(2);
        mGrid.setAdapter(mAdapter);
        mGrid.setOnItemClickListener(this);
    }

    private void generateView(int position, View view, OUser user) {
        BezelImageView imgView = (BezelImageView) view.findViewById(R.id.imgInstance);
        if (user.getAvatar().equals("false")) {
            imgView.setImageResource(R.drawable.avatar);
        } else {
            imgView.setImageBitmap(BitmapUtils.getBitmapImage(mContext, user.getAvatar()));
        }
        imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imgView.autoSetMaskDrawable();
        OControls.setText(view, R.id.txvInstanceName, user.getName());
        OControls.setText(view, R.id.txvServerURL, user.getHost());

    }

    public void setUserLoginSelectListener(IUserLoginSelectListener listener) {
        mIUserLoginSelectListener = listener;
    }


    public void show() {
        if (dialog != null)
            dialog.dismiss();
        dialog = null;
        builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.label_select_user);
        builder.setView(mGrid);
        builder.setCancelable(false);
        builder.setPositiveButton(OResource.string(mContext, R.string.label_drawer_account_add_account),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mIUserLoginSelectListener != null) {
                            mIUserLoginSelectListener.onNewAccountRequest();
                        }
                        dialog.dismiss();
                    }
                });
        builder.setNegativeButton(OResource.string(mContext, R.string.label_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mIUserLoginSelectListener != null) {
                            mIUserLoginSelectListener.onCancelSelect();
                        }
                        dialog.dismiss();
                    }
                });
        dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final OUser user = mAdapter.getItem(position);
        dialog.dismiss();
        if (mIUserLoginSelectListener != null) {
            // Ask for password of account
            askPassword = OdooUserAskPassword.get(mContext, user);
            askPassword.setOnUserPasswordValidateListener(new OdooUserAskPassword.OnUserPasswordValidateListener() {
                @Override
                public void onSuccess() {
                    mIUserLoginSelectListener.onUserSelected(user);
                }

                @Override
                public void onCancel() {
                    mIUserLoginSelectListener.onRequestAccountSelect();
                }

                @Override
                public void onFail() {
                    OAlert.showError(mContext, OResource.string(mContext,
                            R.string.error_invalid_password), new OAlert.OnAlertDismissListener() {
                        @Override
                        public void onAlertDismiss() {
                            onCancel();
                        }
                    });
                }
            });
            askPassword.show();
        }
    }


    public interface IUserLoginSelectListener {
        public void onUserSelected(OUser user);

        public void onNewAccountRequest();

        public void onCancelSelect();

        public void onRequestAccountSelect();
    }
}
