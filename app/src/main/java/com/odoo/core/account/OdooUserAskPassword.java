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
 * Created on 8/4/15 12:06 PM
 */
package com.odoo.core.account;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.odoo.R;
import com.odoo.core.support.OUser;
import com.odoo.core.utils.BitmapUtils;
import com.odoo.core.utils.OControls;

public class OdooUserAskPassword {

    public static final String TAG = OdooUserAskPassword.class.getSimpleName();
    private Context mContext;
    private OUser mUser;
    private OnUserPasswordValidateListener mOnUserPasswordValidateListener;
    private AlertDialog.Builder builder;
    private EditText edtPassword;

    public OdooUserAskPassword(Context context, OUser user) {
        mContext = context;
        mUser = user;
        builder = new AlertDialog.Builder(context);
    }

    public static OdooUserAskPassword get(Context context, OUser user) {
        return new OdooUserAskPassword(context, user);
    }

    public void show() {
        builder.setView(getView());
        builder.setPositiveButton(R.string.label_login, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String password = edtPassword.getText().toString();
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(mContext, R.string.error_provide_password, Toast.LENGTH_LONG).show();
                    show();
                } else {
                    if (password.equals(mUser.getPassword())) {
                        mOnUserPasswordValidateListener.onSuccess();
                    } else {
                        mOnUserPasswordValidateListener.onFail();
                    }
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mOnUserPasswordValidateListener.onCancel();
            }
        });
        builder.create().show();
    }

    private View getView() {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.base_account_ask_pass, null, false);
        String avatar = mUser.getAvatar();
        Bitmap bitmap;
        if (avatar.equals("false")) {
            bitmap = BitmapUtils.getAlphabetImage(mContext, mUser.getName());
        } else {
            bitmap = BitmapUtils.getBitmapImage(mContext, avatar);
        }
        OControls.setImage(view, R.id.userAvatar, bitmap);
        OControls.setText(view, R.id.txvUsername, mUser.getName());
        edtPassword = (EditText) view.findViewById(R.id.edtPassword);
        return view;
    }

    public OdooUserAskPassword setOnUserPasswordValidateListener(OnUserPasswordValidateListener listener) {
        mOnUserPasswordValidateListener = listener;
        return this;
    }

    public interface OnUserPasswordValidateListener {
        public void onSuccess();

        public void onCancel();

        public void onFail();
    }

}
