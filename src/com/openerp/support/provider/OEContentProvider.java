/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http://www.openerp.com>)
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * 
 */

package com.openerp.support.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.openerp.orm.OESQLiteHelper;

/**
 * The Class OEContentProvider.
 */
public abstract class OEContentProvider extends ContentProvider implements
		OEContentProviderHelper {

	/** The Constant CONSTANTS. */
	private static final int CONSTANTS = 1;

	/** The authority. */
	public static String AUTHORITY = "";

	/** The Constant CONSTANT_ID. */
	private static final int CONSTANT_ID = 2;

	/** The Constant MATCHER. */
	private static final UriMatcher MATCHER;

	/** The Constant TABLE. */
	private static final String TABLE = "constants";

	/** The contenturi. */
	public static String CONTENTURI = "";

	/** The db. */
	OESQLiteHelper db = null;

	/**
	 * The Class Constants.
	 */
	public static final class Constants implements BaseColumns {

		/** The Constant CONTENT_URI. */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ CONTENTURI + "/constants");

		/** The Constant DEFAULT_SORT_ORDER. */
		public static final String DEFAULT_SORT_ORDER = "title";

		/** The Constant TITLE. */
		public static final String TITLE = "title";

		/** The Constant VALUE. */
		public static final String VALUE = "value";
	}

	static {
		MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		MATCHER.addURI(CONTENTURI, "constants", CONSTANTS);
		MATCHER.addURI(CONTENTURI, "constants/#", CONSTANT_ID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#delete(android.net.Uri,
	 * java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		// TODO Auto-generated method stub
		int count = db.getWritableDatabase().delete(TABLE, where, whereArgs);

		getContext().getContentResolver().notifyChange(uri, null);

		return (count);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		if (isCollectionUri(uri)) {

			return (CONTENTURI + "/constant");
		}

		return (CONTENTURI + "/constant");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#insert(android.net.Uri,
	 * android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
		db = new OESQLiteHelper(getContext());
		AUTHORITY = authority();
		CONTENTURI = contentUri();
		return ((db == null) ? false : true);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#query(android.net.Uri,
	 * java.lang.String[], java.lang.String, java.lang.String[],
	 * java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sort) {
		// TODO Auto-generated method stub
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		qb.setTables(TABLE);

		String orderBy;

		if (TextUtils.isEmpty(sort)) {
			orderBy = Constants.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sort;
		}

		Cursor c = qb.query(db.getReadableDatabase(), projection, selection,
				selectionArgs, null, null, orderBy);

		c.setNotificationUri(getContext().getContentResolver(), uri);

		return (c);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#update(android.net.Uri,
	 * android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		// TODO Auto-generated method stub
		int count = db.getWritableDatabase().update(TABLE, values, where,
				whereArgs);

		getContext().getContentResolver().notifyChange(uri, null);

		return (count);
	}

	/**
	 * Checks if is collection uri.
	 * 
	 * @param url
	 *            the url
	 * @return true, if is collection uri
	 */
	private boolean isCollectionUri(Uri url) {
		return (MATCHER.match(url) == CONSTANTS);
	}

}

interface OEContentProviderHelper {
	public String authority();

	public String contentUri();
}
