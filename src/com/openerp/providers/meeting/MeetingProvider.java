package com.openerp.providers.meeting;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.openerp.orm.SQLiteDatabaseHelper;

public class MeetingProvider extends ContentProvider {

    private static final int CONSTANTS = 1;
    public static String AUTHORITY = "com.openerp.providers.meeting";
    private static final int CONSTANT_ID = 2;
    private static final UriMatcher MATCHER;
    private static final String TABLE = "constants";
    SQLiteDatabaseHelper db = null;

    public static final class Constants implements BaseColumns {

	public static final Uri CONTENT_URI = Uri
		.parse("content://com.openerp.providers.meeting.MeetingProvider/constants");
	public static final String DEFAULT_SORT_ORDER = "title";
	public static final String TITLE = "title";
	public static final String VALUE = "value";
    }

    static {
	MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	MATCHER.addURI("com.openerp.providers.meeting.MeetingProvider",
		"constants", CONSTANTS);
	MATCHER.addURI("com.openerp.providers.meeting.MeetingProvider",
		"constants/#", CONSTANT_ID);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
	// TODO Auto-generated method stub
	int count = db.getWritableDatabase().delete(TABLE, where, whereArgs);
	getContext().getContentResolver().notifyChange(uri, null);
	return (count);
    }

    @Override
    public String getType(Uri uri) {
	// TODO Auto-generated method stub
	if (isCollectionUri(uri)) {
	    return ("com.openerp.providers.meeting.MeetingProvider/constant");
	}
	return ("com.openerp.providers.meeting.MeetingProvider/constant");
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
	// TODO Auto-generated method stub
	long rowID = db.getWritableDatabase().insert(TABLE, Constants.TITLE,
		initialValues);

	if (rowID > 0) {
	    Uri uriObj = ContentUris.withAppendedId(
		    MeetingProvider.Constants.CONTENT_URI, rowID);
	    getContext().getContentResolver().notifyChange(uri, null);

	    return (uriObj);
	}

	throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate() {
	// TODO Auto-generated method stub
	db = new SQLiteDatabaseHelper(getContext());
	return ((db == null) ? false : true);
    }

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

    @Override
    public int update(Uri uri, ContentValues values, String where,
	    String[] whereArgs) {
	// TODO Auto-generated method stub
	int count = db.getWritableDatabase().update(TABLE, values, where,
		whereArgs);

	getContext().getContentResolver().notifyChange(uri, null);

	return (count);
    }

    private boolean isCollectionUri(Uri url) {
	return (MATCHER.match(url) == CONSTANTS);
    }

}
