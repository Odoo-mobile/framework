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
 * Created on 12/1/15 5:25 PM
 */
package com.odoo.core.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.odoo.R;

public class OAlert {
    public static final String TAG = OAlert.class.getSimpleName();

    private enum Type {
        Alert, Warning, Error
    }

    public static enum ConfirmType {
        POSITIVE, NEGATIVE
    }

    public static void showAlert(Context context, String message) {
        showAlert(context, message, null);
    }

    public static void showWarning(Context context, String message) {
        showWarning(context, message, null);
    }

    public static void showError(Context context, String message) {
        showError(context, message, null);
    }

    public static void showAlert(Context context, String message, OnAlertDismissListener listener) {
        show(context, message, Type.Alert, listener);
    }

    public static void showWarning(Context context, String message, OnAlertDismissListener listener) {
        show(context, message, Type.Warning, listener);
    }

    public static void showError(Context context, String message, OnAlertDismissListener listener) {
        show(context, message, Type.Error, listener);
    }

    private static void show(Context context, String message, Type type, final OnAlertDismissListener listener) {
        AlertDialog.Builder mBuilder;
        mBuilder = new AlertDialog.Builder(context);
        switch (type) {
            case Alert:
                mBuilder.setTitle(R.string.label_alert);
                break;
            case Error:
                mBuilder.setTitle(R.string.label_error);
                mBuilder.setCancelable(false);
                break;
            case Warning:
                mBuilder.setTitle(R.string.label_warning);
        }
        mBuilder.setMessage(message);
        mBuilder.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (listener != null) {
                    listener.onAlertDismiss();
                }
            }
        });
        mBuilder.create().show();
    }

    public static void showConfirm(Context context, String message, final OnAlertConfirmListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm");
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onConfirmChoiceSelect(ConfirmType.POSITIVE);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onConfirmChoiceSelect(ConfirmType.NEGATIVE);
                }
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (listener != null) {
                    listener.onConfirmChoiceSelect(ConfirmType.NEGATIVE);
                }
            }
        });
        builder.create().show();
    }

    public static void inputDialog(Context context, String title, final OnUserInputListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        int margin = OResource.dimen(context, R.dimen.default_8dp);
        params.setMargins(margin, margin, margin, margin);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(params);
        linearLayout.setPadding(margin, margin, margin, margin);
        final EditText edtInput = new EditText(context);
        edtInput.setLayoutParams(params);
        edtInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        if (listener != null) {
            listener.onViewCreated(edtInput);
        }
        linearLayout.addView(edtInput);
        builder.setView(linearLayout);
        if (title != null)
            builder.setTitle(title);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (TextUtils.isEmpty(edtInput.getText())) {
                    edtInput.setError("Field required");
                    edtInput.requestFocus();
                } else {
                    if (listener != null) {
                        listener.onUserInputted(edtInput.getText());
                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    public static interface OnAlertConfirmListener {
        public void onConfirmChoiceSelect(ConfirmType type);
    }

    public static interface OnAlertDismissListener {
        public void onAlertDismiss();
    }

    public static interface OnUserInputListener {
        public void onViewCreated(EditText inputView);

        public void onUserInputted(Object value);
    }
}
