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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
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
		projection = validateProjections(projection);
		Cursor c = createQuery(uri, projection, selection, selectionArgs, sort);
		Context ctx = getContext();
		assert ctx != null;
		c.setNotificationUri(ctx.getContentResolver(), uri);
		return c;
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

	private String[] validateProjections(String[] projection) {
		List<String> columns = new ArrayList<String>();
		columns.addAll(Arrays.asList(projection));
		columns.add(OColumn.ROW_ID);
		if (model.getColumn("id") != null)
			columns.add("id");
		return columns.toArray(new String[columns.size()]);
	}

	private Cursor createQuery(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sort) {
		SQLiteQueryBuilder query = new SQLiteQueryBuilder();
		int total_column = model.getColumns().size();
		boolean withAlias = (projection.length < total_column - 1);
		StringBuffer joins = new StringBuffer();
		String base_table = model.getTableName();
		String base_alias = base_table + "_base";
		HashMap<String, String> projectionMap = new HashMap<String, String>();
		for (String col_name : projection) {
			String col = col_name;
			if (col_name.contains(".")) {
				col = col_name.split("\\.")[0];
			}
			OColumn column = model.getColumn(col);
			String display_col = col;
			if (withAlias) {
				display_col = base_alias + "." + col + " AS " + col;
				if (column.getRelationType() != null) {
					OModel rel_model = model.createInstance(column.getType());
					String table = rel_model.getTableName();
					String alias = table;
					alias = table + "_self";
					table += " AS " + alias;
					joins.append(" JOIN ");
					joins.append(table);
					joins.append(" ON ");
					joins.append(base_alias + "." + column.getName());
					joins.append(" = ");
					joins.append(alias + "." + OColumn.ROW_ID);
					joins.append(" ");
					String rel_col = col;
					String rel_col_name = "";
					if (col_name.contains(".")) {
						rel_col += "_" + col_name.split("\\.")[1];
						rel_col_name = col_name.split("\\.")[1];
					}
					projectionMap.put(rel_col, alias + "." + rel_col_name
							+ " AS " + rel_col);
				}
			}
			projectionMap.put(col, display_col);
		}
		StringBuffer tables = new StringBuffer();
		tables.append(base_table + ((withAlias) ? " AS " + base_alias : " "));
		tables.append(joins.toString());
		query.setTables(tables.toString());
		query.setProjectionMap(projectionMap);
		StringBuffer whr = new StringBuffer();
		if (withAlias) {
			// Check for and
			Pattern pattern = Pattern.compile("and|AND");
			String[] data = pattern.split(selection);
			for (String token : data) {
				whr.append(base_alias + "." + token.trim());
				whr.append(" AND ");
			}
			whr.delete(whr.length() - 5, whr.length());
			// Check for or
			pattern = Pattern.compile("or|OR");
			data = pattern.split(whr.toString());
			whr = new StringBuffer();
			for (String token : data) {
				whr.append((!token.contains(base_alias)) ? base_alias + "."
						+ token.trim() : token.trim());
				whr.append(" OR ");
			}
			whr.delete(whr.length() - 4, whr.length());
		} else {
			whr.append(selection);
		}
		Cursor c = null;
		int uriMatch = matcher.match(uri);
		switch (uriMatch) {
		case SINGLE_ROW:
			// Return a single entry, by ID.
			String id = uri.getLastPathSegment();
			query.appendWhere(base_alias + "." + OColumn.ROW_ID + " = " + id);
		case COLLECTION:
			c = query.query(model.getReadableDatabase(), null, whr.toString(),
					selectionArgs, null, null, sort);
			return c;
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}
}
