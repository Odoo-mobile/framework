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
 * Created on 27/2/15 6:55 PM
 */
package com.odoo.base.addons.mail.widget;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

public class MessageObserver extends ContentObserver {
    public static final String TAG = MessageObserver.class.getSimpleName();
    private OnDataSetChangeListener mOnDataSetChangeListener;

    public MessageObserver() {
        super(new Handler());
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        if (mOnDataSetChangeListener != null) {
            mOnDataSetChangeListener.onDataSetChange();
        }
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        if (mOnDataSetChangeListener != null) {
            mOnDataSetChangeListener.onDataSetChange();
        }
    }

    public void setOnDataSetChangeListener(OnDataSetChangeListener listener) {
        mOnDataSetChangeListener = listener;
    }

    public interface OnDataSetChangeListener {
        public void onDataSetChange();
    }

}
