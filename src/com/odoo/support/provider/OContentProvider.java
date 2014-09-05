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

import org.json.JSONArray;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.odoo.orm.OColumn;
import com.odoo.orm.OColumn.RelationType;
import com.odoo.orm.OModel;
import com.odoo.orm.OModel.Command;
import com.odoo.orm.SelectionBuilder;
import com.odoo.util.JSONUtils;

/**
 * The Class OContentProvider.
 */
public abstract class OContentProvider extends ContentProvider implements
		OContentProviderHelper {

	private final int COLLECTION = 1;

	private final int SINGLE_ROW = 2;

	private UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

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
		reInitModel();
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

	private void handleManyToMany(HashMap<String, List<Integer>> record_ids,
			int _id) {
		if (record_ids.size() > 0) {
			for (String key : record_ids.keySet()) {
				List<Integer> ids = record_ids.get(key);
				OColumn column = model.getColumn(key);
				OModel rel_model = model.createInstance(column.getType());
				model.manageManyToManyRecords(model.getWritableDatabase(),
						rel_model, ids, _id, Command.Replace);
			}
		}
	}

	private HashMap<String, List<Integer>> getManyToManyRecords(
			ContentValues values) {
		HashMap<String, List<Integer>> ids = new HashMap<String, List<Integer>>();
		for (OColumn col : model.getRelationColumns()) {
			if (col.getRelationType() == RelationType.ManyToMany) {
				if (values.containsKey(col.getName())) {
					List<Integer> record_ids = new ArrayList<Integer>();
					try {
						record_ids.addAll(JSONUtils
								.<Integer> toList(new JSONArray(values.get(
										col.getName()).toString())));
					} catch (Exception e) {
						e.printStackTrace();
					}
					ids.put(col.getName(), record_ids);
					values.remove(col.getName());
				}
			}
		}
		return ids;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		reInitModel();
		HashMap<String, List<Integer>> manyToManyIds = getManyToManyRecords(initialValues);
		final SQLiteDatabase db = model.getWritableDatabase();
		assert db != null;
		final int match = matcher.match(uri);
		Uri result;
		switch (match) {
		case COLLECTION:
			long id = db.insertOrThrow(model.getTableName(), null,
					initialValues);
			handleManyToMany(manyToManyIds, (int) id);
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
		return result;
	}

	@Override
	public boolean onCreate() {
		model = model(getContext());
		matcher.addURI(authority(), path(), COLLECTION);
		matcher.addURI(authority(), path() + "/#", SINGLE_ROW);
		return ((model == null) ? false : true);
	}

	private void reInitModel() {
		if (model.getDatabaseName().length() == 0) {
			onCreate();
		}
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
		reInitModel();
		SelectionBuilder builder = new SelectionBuilder();
		final SQLiteDatabase db = model.getWritableDatabase();
		final int match = matcher.match(uri);
		int count;
		switch (match) {
		case COLLECTION:
			Cursor cr = query(uri, new String[] { OColumn.ROW_ID }, where,
					whereArgs, null);
			while (cr.moveToNext()) {
				int id = cr.getInt(cr.getColumnIndex(OColumn.ROW_ID));
				handleManyToMany(getManyToManyRecords(values), id);
			}
			cr.close();

			count = builder.table(model.getTableName()).where(where, whereArgs)
					.update(db, values);
			break;
		case SINGLE_ROW:
			String id = uri.getLastPathSegment();
			handleManyToMany(getManyToManyRecords(values), Integer.parseInt(id));
			count = builder.table(model.getTableName()).where(where, whereArgs)
					.where(OColumn.ROW_ID + "=?", id).where(where, whereArgs)
					.update(db, values);
			break;
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		Context ctx = getContext();
		assert ctx != null;
		ctx.getContentResolver().notifyChange(uri, null, false);
		return count;
	}

	private String[] validateProjections(String[] projection) {
		List<String> columns = new ArrayList<String>();
		columns.addAll(Arrays.asList(projection));
		columns.addAll(Arrays.asList(new String[] { OColumn.ROW_ID, "is_dirty",
				"is_active", "odoo_name" }));
		if (model.getColumn("id") != null)
			columns.add("id");
		return columns.toArray(new String[columns.size()]);
	}

	private Cursor createQuery(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sort) {
		reInitModel();
		SQLiteQueryBuilder query = new SQLiteQueryBuilder();
		boolean withAlias = (projection.length < model.projection().length);
		StringBuffer joins = new StringBuffer();
		String base_table = model.getTableName();
		String base_alias = base_table + "_base";
		HashMap<String, String> projectionMap = new HashMap<String, String>();
		List<String> mJoinTables = new ArrayList<String>();
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
					if (!mJoinTables.contains(alias)) {
						mJoinTables.add(alias);
						joins.append(" JOIN ");
						joins.append(table);
						joins.append(" ON ");
						joins.append(base_alias + "." + column.getName());
						joins.append(" = ");
						joins.append(alias + "." + OColumn.ROW_ID);
						joins.append(" ");
					}
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
		String where = null;
		if (selection != null && selectionArgs != null) {
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
			where = whr.toString();
		}
		Cursor c = null;
		int uriMatch = matcher.match(uri);
		switch (uriMatch) {
		case SINGLE_ROW:
			// Return a single entry, by ID.
			String id = uri.getLastPathSegment();
			query.appendWhere(base_alias + "." + OColumn.ROW_ID + " = " + id);
		case COLLECTION:
			c = query.query(model.getReadableDatabase(), null, where,
					selectionArgs, null, null, sort);
			return c;
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

}
