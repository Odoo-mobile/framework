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
 * Created on 31/12/14 6:54 PM
 */
package com.odoo.core.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.odoo.core.orm.OModel;

public class BaseModelProvider extends ContentProvider {
    public static final String TAG = BaseModelProvider.class.getSimpleName();

    private final int COLLECTION = 1;
    private final int SINGLE_ROW = 2;
    private OModel mModel = null;
    private UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static Uri buildURI(String authority, String path) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.appendPath(path);
        uriBuilder.scheme("content");
        return uriBuilder.build();
    }

    @Override
    public boolean onCreate() {
        return true;
    }


    private void setModel(Uri uri) {
        String authority = uri.getAuthority();
        String path = uri.getPath().replaceAll("\\/", "");
        matcher.addURI(authority, path, COLLECTION);
        matcher.addURI(authority, path + "/#", SINGLE_ROW);
        //TODO: Create model instance from path
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        setModel(uri);
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return uri.toString();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        setModel(uri);
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        setModel(uri);
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        setModel(uri);
        return 0;
    }
}
