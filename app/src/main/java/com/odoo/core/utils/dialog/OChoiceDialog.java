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
 * Created on 6/2/15 2:41 PM
 */
package com.odoo.core.utils.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.util.ArrayList;
import java.util.List;

public class OChoiceDialog implements DialogInterface.OnClickListener {
    public static final String TAG = OChoiceDialog.class.getSimpleName();
    private Context mContext;
    private AlertDialog.Builder mBuilder;
    private List<String> options = new ArrayList<>();
    private OnChoiceSelectListener mOnChoiceSelectListener;
    private int defaultSelected = -1;
    private String title = null;

    public OChoiceDialog(Context context) {
        mContext = context;
        mBuilder = new AlertDialog.Builder(mContext);
    }

    public static OChoiceDialog get(Context context) {
        return new OChoiceDialog(context);
    }

    public OChoiceDialog withTitle(String title) {
        this.title = title;
        return this;
    }

    public OChoiceDialog withOptions(List<String> options, int selected) {
        this.options = options;
        defaultSelected = selected;
        return this;
    }

    public void show(OnChoiceSelectListener listener) {
        mOnChoiceSelectListener = listener;
        if (title != null) {
            mBuilder.setTitle(title);
        }
        mBuilder.setSingleChoiceItems(options.toArray(new String[options.size()]), defaultSelected, this);
        mBuilder.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mOnChoiceSelectListener != null) {
            mOnChoiceSelectListener.choiceSelected(which, options.get(which));
        }
        dialog.dismiss();
    }

    public interface OnChoiceSelectListener {
        public void choiceSelected(int position, String value);
    }
}
