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
 * Created on 31/12/14 6:54 PM
 */
package com.odoo.core.orm.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.odoo.core.auth.OdooAccountManager;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.RelValues;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.support.OUser;
import com.odoo.core.utils.ODateUtils;
import com.odoo.core.utils.OObjectUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

public class BaseModelProvider extends ContentProvider {
    public static final String TAG = BaseModelProvider.class.getSimpleName();
    public final static String KEY_MODEL = "key_model";
    public final static String KEY_USERNAME = "key_username";
    private final int COLLECTION = 1;
    private final int SINGLE_ROW = 2;
    public UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static Uri buildURI(String authority, String model, String username) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.appendPath(model);
        uriBuilder.appendQueryParameter(KEY_MODEL, model);
        uriBuilder.appendQueryParameter(KEY_USERNAME, username);
        uriBuilder.scheme("content");
        return uriBuilder.build();
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    public String authority() {
        return null;
    }

    public OUser getUser(Uri uri) {
        String username = uri.getQueryParameter(KEY_USERNAME);
        return OdooAccountManager.getDetails(getContext(), username);
    }

    public OModel getModel(Uri uri) {
        String path = uri.getQueryParameter(KEY_MODEL);
        String username = uri.getQueryParameter(KEY_USERNAME);
        return OModel.get(getContext(), path, username);
    }

    private void setMatcher(OModel model, Uri uri) {
        String authority = (authority() != null) ? authority() : uri.getAuthority();
        matcher.addURI(authority, model.getModelName(), COLLECTION);
        matcher.addURI(authority, model.getModelName() + "/#", SINGLE_ROW);
    }

    @Override
    public Cursor query(Uri uri, String[] base_projection, String selection, String[] selectionArgs, String sortOrder) {
        OModel model = getModel(uri);
        setMatcher(model, uri);
        if (model == null)
            return null;
        String[] projection = removeRelationColumns(model, base_projection);
        int match = matcher.match(uri);
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(model.getTableName());

        // If selection not null and does not contain _is_active
        if ((selection != null && !selection.contains("_is_active")) || selection == null) {
            builder.appendWhere("_is_active = 'true'");
        }
        Cursor cr = null;
        switch (match) {
            case COLLECTION:
                cr = builder.query(model.getReadableDatabase(), projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case SINGLE_ROW:
                int row_id = Integer.parseInt(uri.getLastPathSegment());
                cr = builder.query(model.getReadableDatabase(), projection,
                        OColumn.ROW_ID + " = ? ", new String[]{row_id + ""}, null, null, null);
            case UriMatcher.NO_MATCH:
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context ctx = getContext();
        if (cr != null && ctx != null)
            cr.setNotificationUri(ctx.getContentResolver(), uri);
        return cr;
    }

    private String[] removeRelationColumns(OModel model, String[] projection) {
        HashSet<String> columns = new HashSet<>();
        if (projection != null && projection.length > 0 && model != null) {
            for (String key : projection) {
                OColumn column = model.getColumn(key);
                if (column != null && column.getRelationType() == null) {
                    columns.add(key);
                } else if (column != null && column.getRelationType() == OColumn.RelationType.ManyToOne) {
                    columns.add(key);
                }
            }
            columns.addAll(Arrays.asList(OColumn.ROW_ID, "id", "_is_active", "_write_date"));
            return columns.toArray(new String[columns.size()]);
        }
        return null;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return uri.toString();
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues all_values) {
        OModel model = getModel(uri);
        setMatcher(model, uri);
        ContentValues[] values = generateValues(model, all_values);
        ContentValues value_to_insert = values[0];
        value_to_insert.put("_write_date", ODateUtils.getUTCDate());
        if (!value_to_insert.containsKey("_is_active"))
            value_to_insert.put("_is_active", "true");
        if (!value_to_insert.containsKey("_is_dirty"))
            value_to_insert.put("_is_dirty", "false");
        int match = matcher.match(uri);
        switch (match) {
            case COLLECTION:
                SQLiteDatabase db = model.getWritableDatabase();
                long new_id = db.insert(model.getTableName(), null, value_to_insert);
                // Updating relation columns for record
                if (values[1].size() > 0) {
                    storeUpdateRelationRecords(model, values[1], OColumn.ROW_ID + "  = ?",
                            new String[]{new_id + ""});
                }
                return Uri.withAppendedPath(uri, new_id + "");
            case SINGLE_ROW:
                throw new UnsupportedOperationException(
                        "Insert not supported on URI: " + uri);
            case UriMatcher.NO_MATCH:
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        notifyDataChange(uri);
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        OModel model = getModel(uri);
        setMatcher(model, uri);
        int match = matcher.match(uri);
        switch (match) {
            case COLLECTION:
                SQLiteDatabase db = model.getWritableDatabase();
                count = db.delete(model.getTableName(), selection, selectionArgs);
                break;
            case SINGLE_ROW:
                db = model.getWritableDatabase();
                String row_id = uri.getLastPathSegment();
                count = db.delete(model.getTableName(), OColumn.ROW_ID + "  = ?", new String[]{row_id});
                break;
            case UriMatcher.NO_MATCH:
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        notifyDataChange(uri);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues all_values, String selection, String[] selectionArgs) {
        OModel model = getModel(uri);
        setMatcher(model, uri);
        ContentValues[] values = generateValues(model, all_values);
        ContentValues value_to_update = values[0];
        if (!value_to_update.containsKey("_write_date")) {
            value_to_update.put("_write_date", ODateUtils.getUTCDate());
        }
        if (!value_to_update.containsKey("_is_dirty")) {
            value_to_update.put("_is_dirty", "true");
        }
        int count = 0;
        int match = matcher.match(uri);
        switch (match) {
            case COLLECTION:
                SQLiteDatabase db = model.getWritableDatabase();
                count = db.update(model.getTableName(), value_to_update, selection, selectionArgs);
                // Updating relation columns
                if (values[1].size() > 0) {
                    storeUpdateRelationRecords(model, values[1], selection, selectionArgs);
                }

                break;
            case SINGLE_ROW:
                String row_id = uri.getLastPathSegment();
                db = model.getWritableDatabase();
                count = db.update(model.getTableName(), value_to_update, OColumn.ROW_ID + "  = ?", new String[]{row_id});
                // Updating relation columns for record
                if (values[1].size() > 0) {
                    storeUpdateRelationRecords(model, values[1], OColumn.ROW_ID + "  = ?", new String[]{row_id});
                }
                break;
            case UriMatcher.NO_MATCH:
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        notifyDataChange(uri);
        return count;
    }

    private void storeUpdateRelationRecords(OModel model, ContentValues values,
                                            String selection, String[] args) {
        int row_id = model.selectRowId(selection, args);
        for (String key : values.keySet()) {
            try {
                OColumn column = model.getColumn(key);
                RelValues relValues = (RelValues) OObjectUtils.byteToObject(
                        (byte[]) values.get(key));
                model.handleRelationValues(row_id, column, relValues);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private ContentValues[] generateValues(OModel model, ContentValues values) {
        OValues data_value = new OValues();
        OValues rel_value = new OValues();
        for (String key : values.keySet()) {
            OColumn column = model.getColumn(key);
            if (column != null) {
                if (column.getRelationType() == null) {
                    data_value.put(key, values.get(key));
                } else {
                    if (column.getRelationType() == OColumn.RelationType.ManyToOne) {
                        if (!(values.get(key) instanceof byte[]))
                            data_value.put(key, values.get(key));
                        else {
                            // Creating many to one record and assigning id to record
                            OModel m2oModel = model.createInstance(column.getType());
                            try {
                                OValues m2oVal = (OValues) OObjectUtils.byteToObject(
                                        (byte[]) values.get(key));
                                int id = m2oModel.insert(m2oVal);
                                data_value.put(key, id);
                            } catch (IOException | ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        rel_value.put(key, values.get(key));
                    }
                }
            }
        }
        return new ContentValues[]{data_value.toContentValues(), rel_value.toContentValues()};
    }

    private void notifyDataChange(Uri uri) {
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null);
    }
}
