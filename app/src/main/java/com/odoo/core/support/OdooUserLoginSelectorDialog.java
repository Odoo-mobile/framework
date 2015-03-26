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
 * Created on 19/12/14 1:49 PM
 */
package com.odoo.core.support;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.odoo.R;
import com.odoo.core.account.OdooLogin;
import com.odoo.core.auth.OdooAccountManager;
import com.odoo.core.utils.BitmapUtils;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.OResource;
import com.odoo.core.utils.controls.ExpandableHeightGridView;

import java.util.List;

import odoo.controls.BezelImageView;

import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;

public class OdooUserLoginSelectorDialog implements AdapterView.OnItemClickListener {
    private Context mContext;
    private ExpandableHeightGridView mGrid;
    private ArrayAdapter<OUser> mAdapter;
    private AlertDialog login_dialog, passwordDialog;
    private AlertDialog.Builder builder, builder_password;
    private IUserLoginSelectListener mIUserLoginSelectListener = null;

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
        OControls.setText(view, R.id.txvInstanceUrl, (user.isOAauthLogin()) ? user.getInstanceUrl() : user.getHost());

    }

    public void setUserLoginSelectListener(IUserLoginSelectListener listener) {
        mIUserLoginSelectListener = listener;
    }


    public void show() {
        if (login_dialog != null)
            login_dialog.dismiss();
        if (passwordDialog != null)
            passwordDialog.dismiss();
        login_dialog = null;
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
        login_dialog = builder.create();
        login_dialog.show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final OdooUserLoginSelectorDialog lDialog = new OdooUserLoginSelectorDialog(mContext);
        if (login_dialog != null)
            login_dialog.dismiss();
        passwordDialog = null;
        final OUser user = mAdapter.getItem(position);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                AbsListView.LayoutParams.WRAP_CONTENT);
        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setLayoutParams(params);
        linearLayout.setPadding(10, 10, 10, 10);
        final EditText edt_password = new EditText(mContext);
        edt_password.setLayoutParams(params);
        edt_password.setHint(OResource.string(mContext, R.string.label_password));
        edt_password.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD);
        linearLayout.addView(edt_password);
        builder_password = new AlertDialog.Builder(mContext);
        builder_password.setTitle(R.string.label_enter_password);
        builder_password.setView(linearLayout);
        builder_password.setCancelable(false);
        builder_password.setPositiveButton(OResource.string(mContext, R.string.label_login), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!TextUtils.isEmpty(edt_password.getText())) {
                    if (edt_password.getText().toString().equals(user.getPassword())) {
                        if (mIUserLoginSelectListener != null) {
                            mIUserLoginSelectListener.onUserSelected(user);
                        }
                    } else {
                        Toast.makeText(mContext, OResource.string(mContext, R.string.toast_invalid_password), Toast.LENGTH_LONG).show();
                        lDialog.show();
                    }
                } else {
                    Toast.makeText(mContext, OResource.string(mContext, R.string.error_provide_password), Toast.LENGTH_LONG).show();
                    lDialog.setUserLoginSelectListener(new OdooLogin());
                    lDialog.show();
                }
                dialog.dismiss();
            }
        });
        builder_password.setNegativeButton(OResource.string(mContext, R.string.label_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                lDialog.show();
            }
        });
        passwordDialog = builder_password.create();
        passwordDialog.show();
    }

    public interface IUserLoginSelectListener {
        public void onUserSelected(OUser user);

        public void onNewAccountRequest();

        public void onCancelSelect();
    }
}
