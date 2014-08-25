/*
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
 */

package com.odoo.support.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.odoo.orm.OColumn;
import com.odoo.orm.OModel;
import com.odoo.orm.SelectionBuilder;
import com.odoo.support.OUser;
import com.odoo.util.ODate;

/**
 * The Class OContentProvider.
 */
public abstract class OContentProvider extends ContentProvider implements
		OContentProviderHelper {

	private final int COLLECTION = 1;

	private final int SINGLE_ROW = 2;

	private UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
	private OUser user = null;

	/** The database model. */
	private OModel model = null;

	public static Uri buildURI(String authority, String path) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.authority(authority);
		uriBuilder.appendPath(path);
		uriBuilder.scheme("content");
		return uriBuilder.build();
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		final SQLiteDatabase db = model.getWritableDatabase();
		assert db != null;
		final int match = matcher.match(uri);
		int count = 0;
		SelectionBuilder builder = new SelectionBuilder();
		switch (match) {
		case COLLECTION:
			count = builder.table(model.getTableName()).where(where, whereArgs)
					.delete(db);
			break;
		case SINGLE_ROW:
			String id = uri.getLastPathSegment();
			count = builder.table(model.getTableName())
					.where(OColumn.ROW_ID + "=?", id).where(where, whereArgs)
					.delete(db);
			break;
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		// Send broadcast to registered ContentObservers, to refresh UI.
		Context ctx = getContext();
		assert ctx != null;
		ctx.getContentResolver().notifyChange(uri, null, false);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		return uri().toString();
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		initialValues.put("local_write_date", ODate.getDate());
		initialValues.put("odoo_name", user.getAndroidName());
		final SQLiteDatabase db = model.getWritableDatabase();
		assert db != null;
		final int match = matcher.match(uri);
		Uri result;
		switch (match) {
		case COLLECTION:
			long id = db.insertOrThrow(model.getTableName(), null,
					initialValues);
			result = Uri.parse(uri() + "/" + id);
			break;
		case SINGLE_ROW:
			throw new UnsupportedOperationException(
					"Insert not supported on URI: " + uri);
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		// Send broadcast to registered ContentObservers, to refresh UI.
		Context ctx = getContext();
		assert ctx != null;
		ctx.getContentResolver().notifyChange(uri, null, false);
		Log.v("", result.toString());
		return result;
	}

	@Override
	public boolean onCreate() {
		model = model(getContext());
		user = model.getUser();
		matcher.addURI(authority(), path(), COLLECTION);
		matcher.addURI(authority(), path() + "/#", SINGLE_ROW);
		return ((model == null) ? false : true);

	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sort) {
		SQLiteDatabase db = model.getReadableDatabase();
		SelectionBuilder builder = new SelectionBuilder();
		int uriMatch = matcher.match(uri);
		switch (uriMatch) {
		case SINGLE_ROW:
			// Return a single entry, by ID.
			String id = uri.getLastPathSegment();
			builder.where(OColumn.ROW_ID + "=?", id);
		case COLLECTION:
			// Return all known entries.
			builder.table(model.getTableName()).where(selection, selectionArgs);
			Cursor c = builder.query(db, projection, sort);
			Context ctx = getContext();
			assert ctx != null;
			c.setNotificationUri(ctx.getContentResolver(), uri);
			return c;
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		values.put("local_write_date", ODate.getDate());
		values.put("odoo_name", user.getAndroidName());
		SelectionBuilder builder = new SelectionBuilder();
		final SQLiteDatabase db = model.getWritableDatabase();
		final int match = matcher.match(uri);
		int count;
		switch (match) {
		case COLLECTION:
			count = builder.table(model.getTableName()).where(where, whereArgs)
					.update(db, values);
			break;
		case SINGLE_ROW:
			String id = uri.getLastPathSegment();
			count = builder.table(model.getTableName()).where(where, whereArgs)
					.where(OColumn.ROW_ID + "=?", id).where(where, whereArgs)
					.update(db, values);
			break;
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		Log.v("", uri + " UPDATED");
		Context ctx = getContext();
		assert ctx != null;
		ctx.getContentResolver().notifyChange(uri, null, false);
		return count;
	}

}
