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
package com.odoo.orm;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import odoo.ODomain;
import odoo.OdooVersion;

import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.odoo.App;
import com.odoo.orm.OColumn.RelationType;
import com.odoo.orm.ORelIds.RelData;
import com.odoo.orm.annotations.Odoo;
import com.odoo.orm.annotations.Odoo.Functional;
import com.odoo.orm.sql.OQuery;
import com.odoo.orm.sql.OQuery.QueryType;
import com.odoo.orm.types.OBoolean;
import com.odoo.orm.types.ODateTime;
import com.odoo.orm.types.OInteger;
import com.odoo.orm.types.OText;
import com.odoo.orm.types.OVarchar;
import com.odoo.receivers.DataSetChangeReceiver;
import com.odoo.support.OUser;
import com.odoo.support.provider.OContentProvider;
import com.odoo.support.provider.OContentProviderHelper;
import com.odoo.util.ODate;
import com.odoo.util.PreferenceManager;
import com.odoo.util.StringUtils;
import com.odoo.util.logger.OLog;

/**
 * The Class OModel.
 */
public class OModel extends OSQLiteHelper implements OModelHelper {

	/** The Constant TAG. */
	public static final String TAG = OModel.class.getSimpleName();

	/** The context. */
	private Context mContext = null;

	/** The _name. */
	private String _name = null;

	/** The columns. */
	private List<OColumn> mColumns = new ArrayList<OColumn>();

	/** The functional columns. */
	private List<OColumn> mFunctionalColumns = new ArrayList<OColumn>();
	private List<OColumn> mRelationColumns = new ArrayList<OColumn>();

	/** The user. */
	private OUser mUser = null;

	/** The sync helper. */
	private OSyncHelper mSyncHelper = null;

	/** The syncing data. */
	private Boolean mSyncingData = false;

	/** The check in active record. */
	private Boolean mCheckInActiveRecord = false;

	/** The app. */
	private App mApp = null;

	/** The odoo version. */
	private OdooVersion mOdooVersion = null;

	/** The offset. */
	private Integer mOffset = 0;

	/** The limit. */
	private Integer mLimit = -1;

	/** The with functional columns. */
	private Boolean mWithFunctionalColumns = false;

	/**
	 * The Enum Command.
	 */
	public enum Command {

		/** The Add. */
		Add,
		/** The Update. */
		Update,
		/** The Delete. */
		Delete,
		/** The Replace. */
		Replace
	}

	// Server Base Columns
	/** The id. */
	OColumn id = new OColumn("ID", OInteger.class).setDefault(0);

	/** The create_date. */
	@Odoo.api.v8
	@Odoo.api.v9alpha
	public OColumn create_date = new OColumn("Created On", ODateTime.class)
			.setParsePattern(ODate.DEFAULT_FORMAT);

	/** The write_date. */
	@Odoo.api.v8
	@Odoo.api.v9alpha
	public OColumn write_date = new OColumn("Last Updated On", ODateTime.class)
			.setParsePattern(ODate.DEFAULT_FORMAT);

	// Local Base Columns
	/** The _id. */
	OColumn _id = new OColumn("Local ID", OInteger.class)
			.setAutoIncrement(true).setLocalColumn();

	/** The odoo_name. */
	OColumn odoo_name = new OColumn("Odoo Account Name", OVarchar.class, 100)
			.setRequired(true).setLocalColumn();

	/** The local_write_date. */
	OColumn local_write_date = new OColumn("Local Write Date", ODateTime.class)
			.setLocalColumn();

	/** The is_dirty. */
	OColumn is_dirty = new OColumn("Dirty Row", OText.class).setDefault(false)
			.setLocalColumn();

	/** The is_active. */
	OColumn is_active = new OColumn("Row Active", OBoolean.class).setDefault(
			true).setLocalColumn();

	/** The declared fields. */
	private HashMap<String, Field> mDeclaredFields = new HashMap<String, Field>();

	public static String getSyncUserDB(Context context) {
		OUser user = OModel.getSyncUser(context);
		if (user != null) {
			return user.getDBName();
		}
		return "";
	}

	public static OUser getSyncUser(Context context) {
		App app = (App) context.getApplicationContext();
		OUser user = app.getSyncUser();
		if (user == null) {
			user = OUser.current(context);
		}
		return user;
	}

	/**
	 * Instantiates a new o model.
	 * 
	 * @param context
	 *            the context
	 * @param model_name
	 *            the model_name
	 */
	public OModel(Context context, String model_name) {
		super(context, OModel.getSyncUserDB(context));
		mContext = context;
		_name = model_name;
		mUser = OUser.current(mContext);
		mApp = (App) context.getApplicationContext();
		if (mUser != null) {
			mOdooVersion = new OdooVersion();
			mOdooVersion.setVersion_number(mUser.getVersion_number());
			mOdooVersion.setServer_serie(mUser.getVersion_serie());
		} else {
			mApp.createInstance();
			mOdooVersion = mApp.getOdooVersion();
		}
		createFieldList();
	}

	public OModel newInstance(OModel model) {
		try {
			return model.getClass().newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public OContentProvider getContentProvider() {
		return null;
	}

	/**
	 * Generate content Uri.
	 * 
	 * @return the uri
	 */
	public Uri uri() {
		return OContentProvider.buildURI(authority(), path());
	}

	public String path() {
		OContentProviderHelper provider = (OContentProviderHelper) getContentProvider();
		if (provider != null)
			return provider.path();
		else
			OLog.log("Override getContentProvider() method in "
					+ getClass().getSimpleName());
		return null;
	}

	/**
	 * Generate content Authority.
	 * 
	 * @return the string
	 */
	public String authority() {
		OContentProviderHelper provider = (OContentProviderHelper) getContentProvider();
		if (provider != null)
			return provider.authority();
		else
			OLog.log("Override getContentProvider() method in "
					+ getClass().getSimpleName());
		return null;
	}

	/**
	 * Sets the user.
	 * 
	 * @param user
	 *            the new user
	 */
	public void setUser(OUser user) {
		mUser = user;
	}

	public OUser getUser() {
		return mUser;
	}

	/**
	 * Gets the model name.
	 * 
	 * @return the model name
	 */
	public String getModelName() {
		return _name;
	}

	/**
	 * Gets the table name.
	 * 
	 * @return the table name
	 */
	public String getTableName() {
		return _name.replaceAll("\\.", "_");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.odoo.orm.OModelHelper#getColumns()
	 */
	public List<OColumn> getColumns() {
		return getColumns(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.odoo.orm.OModelHelper#getColumns(java.lang.Boolean)
	 */
	@Override
	public List<OColumn> getColumns(Boolean local) {
		if (mColumns.size() <= 0) {
			prepareColumns();
		}
		if (local != null) {
			List<OColumn> cols = new ArrayList<OColumn>();
			for (OColumn column : getColumns())
				if (local == column.isLocal())
					cols.add(column);
			return cols;
		} else {
			return mColumns;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.odoo.orm.OModelHelper#defaultDomain()
	 */
	@Override
	public ODomain defaultDomain() {
		return new ODomain();
	}

	/**
	 * Gets the many to many columns.
	 * 
	 * @param relation_model
	 *            the relation_model
	 * @return the many to many columns
	 */
	public List<OColumn> getManyToManyColumns(OModel relation_model) {
		List<OColumn> cols = new ArrayList<OColumn>();
		odoo_name.setName("odoo_name");
		cols.add(odoo_name);
		local_write_date.setName("local_write_date");
		cols.add(local_write_date);
		is_dirty.setName("is_dirty");
		cols.add(is_dirty);
		is_active.setName("is_active");
		cols.add(is_active);

		OColumn base_id = new OColumn("Base Id", OInteger.class);
		base_id.setName(getTableName() + "_id");
		cols.add(base_id);
		OColumn relation_id = new OColumn("Relation Id", OInteger.class);
		relation_id.setName(relation_model.getTableName() + "_id");
		cols.add(relation_id);
		return cols;
	}

	private void createFieldList() {
		try {
			List<Field> fields = new ArrayList<Field>();
			fields.addAll(Arrays.asList(getClass().getSuperclass()
					.getDeclaredFields()));
			fields.addAll(Arrays.asList(getClass().getDeclaredFields()));
			mDeclaredFields.clear();
			for (Field field : fields) {
				if (field.getType().isAssignableFrom(OColumn.class)) {
					mDeclaredFields.put(field.getName(), field);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Prepare columns.
	 */
	public void prepareColumns() {
		if (mDeclaredFields.size() == 0)
			createFieldList();
		for (String key : mDeclaredFields.keySet()) {
			OColumn column = getColumn(key);
			if (column != null) {
				if (column.getRelationType() != null) {
					mRelationColumns.add(column);
				}
				if (column.isFunctionalColumn()) {
					if (column.canFunctionalStore()) {
						mColumns.add(column);
					}
					mFunctionalColumns.add(column);
				} else {
					mColumns.add(column);
				}
			}
		}
	}

	public List<OColumn> getRelationColumns() {
		return mRelationColumns;
	}

	public List<OColumn> getFunctionalColumns() {
		return mFunctionalColumns;
	}

	/**
	 * Gets the column.
	 * 
	 * @param name
	 *            the name
	 * @return the column
	 */
	public OColumn getColumn(String name) {
		OColumn column = null;
		try {
			Field field = mDeclaredFields.get(name);
			if (field != null) {
				field.setAccessible(true);
				column = (OColumn) field.get(this);
				column.setName(field.getName());
				Boolean validField = (column.isAccessible()) ? validateFieldVersion(field)
						: true;
				if (validField) {
					Method method = checkForFunctionalColumn(field);
					column.setName(name);
					if (method != null) {
						column.setFunctionalMethod(method);
						column.setFunctionalStore(checkForFunctionalStore(field));
						column.checkRowId(checkForFunctionalRowIdCheck(field));
						if (column.canFunctionalStore()) {
							column.setFunctionalStoreDepends(getFunctionalDepends(field));
						} else {
							column.setLocalColumn();
						}
					}
				} else
					return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return column;
	}

	public OColumn getPermReadColumn(String name) {
		OColumn column = null;
		try {
			Field field = getClass().getSuperclass().getDeclaredField(name);
			if (field != null) {
				field.setAccessible(true);
				if (validateFieldVersion(field)) {
					Method method = checkForFunctionalColumn(field);
					column = (OColumn) field.get(this);
					column.setName(name);
					if (method != null) {
						column.setFunctionalMethod(method);
						column.setFunctionalStore(checkForFunctionalStore(field));
						if (column.canFunctionalStore()) {
							column.setFunctionalStoreDepends(getFunctionalDepends(field));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return column;
	}

	/**
	 * Check for functional column.
	 * 
	 * @param field
	 *            the field
	 * @return the method
	 */
	private Method checkForFunctionalColumn(Field field) {
		Annotation annotation = field.getAnnotation(Odoo.Functional.class);
		if (annotation != null) {
			Odoo.Functional functional = (Functional) annotation;
			String method_name = functional.method();
			try {
				if (functional.store())
					return getClass().getMethod(method_name, OValues.class);
				else
					return getClass().getMethod(method_name, ODataRow.class);
			} catch (NoSuchMethodException e) {
				Log.e(TAG, "No Such Method: " + e.getMessage());
			}
		}
		return null;
	}

	/**
	 * Check for functional store.
	 * 
	 * @param field
	 *            the field
	 * @return the boolean
	 */
	public Boolean checkForFunctionalStore(Field field) {
		Annotation annotation = field.getAnnotation(Odoo.Functional.class);
		if (annotation != null) {
			Odoo.Functional functional = (Functional) annotation;
			return functional.store();
		}
		return false;
	}

	/**
	 * Check for functional row id check.
	 * 
	 * @param field
	 *            the field
	 * @return the boolean
	 */
	private Boolean checkForFunctionalRowIdCheck(Field field) {
		Annotation annotation = field.getAnnotation(Odoo.Functional.class);
		if (annotation != null) {
			Odoo.Functional functional = (Functional) annotation;
			return functional.checkRowId();
		}
		return true;
	}

	/**
	 * Gets the functional depends.
	 * 
	 * @param field
	 *            the field
	 * @return the functional depends
	 */
	public String[] getFunctionalDepends(Field field) {
		Annotation annotation = field.getAnnotation(Odoo.Functional.class);
		if (annotation != null) {
			Odoo.Functional functional = (Functional) annotation;
			if (functional.store()) {
				return functional.depends();
			}
		}
		return null;
	}

	public OdooVersion getOdooVersion() {
		return mOdooVersion;
	}

	private Boolean validateFieldVersion(Field field) {
		if (mOdooVersion != null) {
			Annotation[] annotations = field.getAnnotations();
			if (annotations.length > 0) {
				int versions = 0;
				for (Annotation annotation : annotations) {
					if (annotation.annotationType().getDeclaringClass()
							.isAssignableFrom(Odoo.api.class)) {
						switch (mOdooVersion.getVersion_number()) {
						case 9: // Checks for v9
							if (annotation.annotationType().isAssignableFrom(
									Odoo.api.v9alpha.class)) {
								versions++;
							}
							break;
						case 8: // Checks for v8
							if (annotation.annotationType().isAssignableFrom(
									Odoo.api.v8.class)) {
								versions++;
							}
							break;
						case 7: // Checks for v7
							if (annotation.annotationType().isAssignableFrom(
									Odoo.api.v7.class)) {
								versions++;
							}
							break;
						}
					}
					if (annotation.annotationType().isAssignableFrom(
							Odoo.Functional.class)) {
						versions++;
					}
				}
				return (versions > 0) ? true : false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Gets the functional method value.
	 * 
	 * @param column
	 *            the column
	 * @param record
	 *            the record
	 * @return the functional method value
	 */
	public Object getFunctionalMethodValue(OColumn column, Object record) {
		if (column.isFunctionalColumn()) {
			Method method = column.getMethod();
			OModel model = this;
			try {
				return method.invoke(model, new Object[] { record });
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Creates the instance.
	 * 
	 * @param model_class
	 *            the model_class
	 * @return the o model
	 */
	public OModel createInstance(Class<?> model_class) {
		try {
			Constructor<?> constr = model_class.getConstructor(Context.class);
			OModel model = (OModel) constr
					.newInstance(new Object[] { mContext });
			return model;
		} catch (Exception e) {
			Log.d(TAG, model_class.getSimpleName());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Generate columns string array used as Projection in ContentProvider.
	 * 
	 * @return the string[]
	 */
	public String[] projection() {
		List<String> projection = new ArrayList<String>();
		for (OColumn col : getColumns()) {
			if (col.getRelationType() == null
					|| (col.getRelationType() != null && col.getRelationType() == RelationType.ManyToOne)) {
				projection.add(col.getName());
			}
		}
		return projection.toArray(new String[projection.size()]);
	}

	/**
	 * Select.
	 * 
	 * @return the list
	 */
	public List<ODataRow> select() {
		return select(null, null, null, null, null);
	}

	/**
	 * Select.
	 * 
	 * @param id
	 *            the id
	 * @return the o data row
	 */
	public ODataRow select(Integer id) {
		String selection = OColumn.ROW_ID + " = ?";
		List<ODataRow> records = select(selection, new Object[] { id }, null,
				null, null);
		if (records.size() > 0)
			return records.get(0);
		return null;
	}

	public Integer selectRowId(Integer server_id) {
		List<ODataRow> records = select("id = ? ", new Object[] { server_id });
		if (records.size() > 0) {
			return records.get(0).getInt(OColumn.ROW_ID);
		}
		return null;
	}

	public Integer selectServerId(Integer row_id) {
		List<ODataRow> records = browse().columns("id", OColumn.ROW_ID, "name")
				.addWhere(OColumn.ROW_ID, "=", row_id).fetch();
		if (records.size() > 0) {
			return records.get(0).getInt("id");
		}
		return null;
	}

	/**
	 * Select.
	 * 
	 * @param where
	 *            the where
	 * @param args
	 *            the args
	 * @return the list
	 */
	public List<ODataRow> select(String where, Object[] args) {
		return select(where, args, null, null, null);
	}

	/**
	 * Select.
	 * 
	 * @param where
	 *            the where
	 * @param whereArgs
	 *            the where args
	 * @param groupBy
	 *            the group by
	 * @param having
	 *            the having
	 * @param orderBy
	 *            the order by
	 * @return the list
	 */
	public List<ODataRow> select(String where, Object[] whereArgs,
			String groupBy, String having, String orderBy) {
		List<ODataRow> records = new ArrayList<ODataRow>();
		SQLiteDatabase db = getReadableDatabase();
		String limit = null;
		if (mLimit > 0) {
			limit = mOffset + ", " + mLimit;
		}
		Cursor cr = db.query(getTableName(), new String[] { "*" },
				getWhereClause(where), getWhereArgs(where, whereArgs), groupBy,
				having, orderBy, limit);
		if (cr.moveToFirst()) {
			do {
				ODataRow row = new ODataRow();
				for (OColumn col : getColumns()) {
					if (col.getRelationType() == null) {
						row.put(col.getName(), createRecordRow(col, cr));
					} else {
						switch (col.getRelationType()) {
						case ManyToMany:
							row.put(col.getName(),
									new OM2MRecord(this, col, cr.getInt(cr
											.getColumnIndex(OColumn.ROW_ID))));
							break;
						case OneToMany:
							row.put(col.getName(),
									new OO2MRecord(this, col, cr.getInt(cr
											.getColumnIndex(OColumn.ROW_ID))));
							break;
						case ManyToOne:
							row.put(col.getName(),
									new OM2ORecord(this, col, cr.getInt(cr
											.getColumnIndex(col.getName()))));
							break;
						}
					}
				}
				/*
				 * Adding functional column values to record values if not
				 * syncing
				 */
				if (!mSyncingData) {
					for (OColumn col : mFunctionalColumns) {
						if (!col.canFunctionalStore()) {
							row.put(col.getName(),
									getFunctionalMethodValue(col, row));
						}
					}
				}
				if (row.getInt("id") == 0
						|| row.getString("id").equals("false"))
					row.put("id", 0);
				records.add(row);
			} while (cr.moveToNext());
		}
		cr.close();
		db.close();
		return records;
	}

	public List<ODataRow> query(String sql, String[] args) {
		List<ODataRow> records = new ArrayList<ODataRow>();
		SQLiteDatabase db = getReadableDatabase();
		Cursor cr = db.rawQuery(sql, args);
		if (cr.moveToFirst()) {
			do {
				ODataRow row = createRowFromCursor(cr);
				if (mWithFunctionalColumns) {
					for (OColumn col : mFunctionalColumns) {
						if (!col.canFunctionalStore()) {
							row.put(col.getName(),
									getFunctionalMethodValue(col, row));
						}
					}
				}
				records.add(row);
			} while (cr.moveToNext());
		}
		cr.close();
		db.close();
		return records;
	}

	public OModel withFunctionalColumns() {
		return withFunctionalColumns(true);
	}

	public OModel withFunctionalColumns(Boolean withFunctionalColumns) {
		mWithFunctionalColumns = withFunctionalColumns;
		if (withFunctionalColumns) {
			prepareColumns();
		}
		return this;
	}

	public ODataRow createRowFromCursor(Cursor cr) {
		ODataRow row = new ODataRow();
		for (String col_name : cr.getColumnNames()) {
			OColumn col = new OColumn(col_name).setDefault(false);
			col.setName(col_name);
			row.put(col_name, createRecordRow(col, cr));
		}
		if (row.getInt("id") == 0 || row.getString("id").equals("false"))
			row.put("id", 0);
		return row;
	}

	/**
	 * Gets the name.
	 * 
	 * @param model
	 *            the model
	 * @param row_id
	 *            the row_id
	 * @return the name
	 */
	public String getName(int row_id) {
		String name = "false";
		if (getColumn("name") != null) {
			SQLiteDatabase db = getReadableDatabase();
			Cursor cr = db.query(getTableName(), new String[] { "name" },
					OColumn.ROW_ID + " = ?", new String[] { row_id + "" },
					null, null, null);
			if (cr.moveToFirst()) {
				name = cr.getString(cr.getColumnIndex("name"));
			}
			cr.close();
			db.close();
		}
		return name;
	}

	/**
	 * Select one to many rel ids.
	 * 
	 * @param base
	 *            the base
	 * @param rel
	 *            the rel
	 * @param base_id
	 *            the base_id
	 * @param ref_column
	 *            the ref_column
	 * @return the list
	 */
	public List<Integer> selecto2MRelIds(OModel base, OModel rel, int base_id,
			String ref_column) {
		List<Integer> ids = new ArrayList<Integer>();
		SQLiteDatabase db = getReadableDatabase();
		Cursor cr = db.query(rel.getTableName(), new String[] { "id" },
				ref_column + " = ?", new String[] { base_id + "" }, null, null,
				null);
		if (cr.moveToFirst()) {
			do {
				ids.add(cr.getInt(cr.getColumnIndex("id")));
			} while (cr.moveToNext());
		}
		cr.close();
		db.close();
		return ids;
	}

	/**
	 * Select many to many rel ids.
	 * 
	 * @param base
	 *            the base
	 * @param rel
	 *            the rel
	 * @param base_id
	 *            the base_id
	 * @return the list
	 */
	public List<Integer> selectM2MRelIds(OModel base, OModel rel, int base_id) {
		List<Integer> ids = new ArrayList<Integer>();
		String table = base.getTableName() + "_" + rel.getTableName() + "_rel";
		String base_col = base.getTableName() + "_id";
		String rel_col = rel.getTableName() + "_id";
		SQLiteDatabase db = getReadableDatabase();
		String where = base_col + " = ? and odoo_name = ?";
		Object[] whereArgs = new Object[] { base_id, mUser.getAndroidName() };
		Cursor cr = db.query(table, new String[] { "*" },
				getWhereClause(where), getWhereArgs(where, whereArgs), null,
				null, null);
		if (cr.moveToFirst()) {
			do {
				int rel_id = cr.getInt(cr.getColumnIndex(rel_col));
				if (rel.count(OColumn.ROW_ID + " = ?", new Object[] { rel_id }) > 0)
					ids.add(rel_id);
			} while (cr.moveToNext());
		}
		cr.close();
		db.close();
		return ids;
	}

	/**
	 * Select m2 m records.
	 * 
	 * @param base
	 *            the base
	 * @param rel
	 *            the rel
	 * @param base_id
	 *            the base_id
	 * @return the list
	 */
	public List<ODataRow> selectM2MRecords(OModel base, OModel rel, int base_id) {
		List<ODataRow> records = new ArrayList<ODataRow>();
		String table = base.getTableName() + "_" + rel.getTableName() + "_rel";
		String base_col = base.getTableName() + "_id";
		String rel_col = rel.getTableName() + "_id";
		SQLiteDatabase db = getReadableDatabase();
		String where = base_col + " = ? and odoo_name = ?";
		Object[] whereArgs = new Object[] { base_id, mUser.getAndroidName() };
		Cursor cr = db.query(table, new String[] { "*" },
				getWhereClause(where), getWhereArgs(where, whereArgs), null,
				null, null);
		List<Integer> ids = new ArrayList<Integer>();
		if (cr.moveToFirst()) {
			do {
				int rel_id = cr.getInt(cr.getColumnIndex(rel_col));
				ids.add(rel_id);
			} while (cr.moveToNext());
		}
		cr.close();
		db.close();
		records.addAll(rel.select(
				OColumn.ROW_ID + " IN ("
						+ StringUtils.repeat(" ?, ", ids.size() - 1) + "?)",
				new Object[] { ids }));
		return records;
	}

	/**
	 * Creates the record row.
	 * 
	 * @param column
	 *            the column
	 * @param cr
	 *            the cr
	 * @return the object
	 */
	public Object createRecordRow(OColumn column, Cursor cr) {
		Object value = false;
		if (column.getDefaultValue() != null) {
			value = column.getDefaultValue();
		}
		int index = cr.getColumnIndex(column.getName());
		switch (cr.getType(index)) {
		case Cursor.FIELD_TYPE_NULL:
			value = false;
			break;
		case Cursor.FIELD_TYPE_STRING:
			value = cr.getString(index);
			break;
		case Cursor.FIELD_TYPE_INTEGER:
			value = cr.getInt(index);
			break;
		case Cursor.FIELD_TYPE_FLOAT:
			value = cr.getFloat(index);
			break;
		case Cursor.FIELD_TYPE_BLOB:
			value = cr.getBlob(index);
			break;
		}
		return value;
	}

	/**
	 * Truncate.
	 * 
	 * @return true, if successful
	 */
	public boolean truncate() {
		delete(null, null);
		return true;
	}

	/**
	 * Count.
	 * 
	 * @return the int
	 */
	public int count() {
		return count(null, null);
	}

	/**
	 * Count.
	 * 
	 * @param where
	 *            the where
	 * @param whereArgs
	 *            the where args
	 * @return the int
	 */
	public int count(String where, Object[] whereArgs) {

		int count = 0;
		SQLiteDatabase db = getReadableDatabase();
		String whr = getWhereClause(where);
		String[] args = getWhereArgs(whr, whereArgs);
		Cursor cr = db.query(getTableName(),
				new String[] { "count(*) as total" }, whr, args, null, null,
				null);
		cr.moveToFirst();
		count = cr.getInt(0);
		cr.close();
		db.close();
		return count;
	}

	/**
	 * Creates the or replace.
	 * 
	 * @param values
	 *            the values
	 * @return the integer
	 */
	public Integer createORReplace(OValues values) {
		List<OValues> vals = new ArrayList<OValues>();
		vals.add(values);
		return createORReplace(vals).get(0);
	}

	/**
	 * Creates the or replace.
	 * 
	 * @param values_list
	 *            the values_list
	 * @return the list
	 */
	public List<Integer> createORReplace(List<OValues> values_list) {
		List<Integer> ids = new ArrayList<Integer>();
		for (OValues values : values_list) {
			if (!hasRecord(values.getInt("id")))
				ids.add(create(values));
			else {
				ids.add(selectRowId(values.getInt("id")));
				values.put(OColumn.ROW_ID, selectRowId(values.getInt("id")));
				update(values, "id = ?", new Object[] { values.getInt("id") });
			}
		}
		return ids;
	}

	/**
	 * Checks for record with server 'id' column.
	 * 
	 * @param id
	 *            the id
	 * @return true, if successful
	 */
	public boolean hasRecord(int id) {
		if (count("id = ? ", new Object[] { id }) > 0)
			return true;
		return false;
	}

	/**
	 * Creates the.
	 * 
	 * @param values
	 *            the values
	 * @return the int
	 */
	public int create(OValues values) {
		if (!values.contains("odoo_name")) {
			values.put("odoo_name", mUser.getAndroidName());
		}
		ContentValues vals = createValues(values);
		SQLiteDatabase db = getWritableDatabase();
		db.insert(getTableName(), null, vals);
		db.close();
		int newId = getCreateId();
		values.put(OColumn.ROW_ID, newId);
		updateRelationColumns(values);
		sendDatasetChangeBroadcast(newId);
		notifyDataChange(newId);
		return newId;
	}

	/**
	 * Update relation columns values for ManyToMany and OneToMany
	 * 
	 * @param vals
	 * @param newId
	 * @return
	 */
	private boolean updateRelationColumns(OValues values) {
		SQLiteDatabase db = getWritableDatabase();
		for (OColumn column : getColumns()) {
			if (column.getRelationType() != null
					&& values.contains(column.getName())) {
				switch (column.getRelationType()) {
				case ManyToMany:
					OModel rel_model = createInstance(column.getType());
					Command command = Command.Replace;
					List<Integer> rel_ids = new ArrayList<Integer>();
					if (values.get(column.getName()) instanceof ORelIds) {
						ORelIds rel_objs = (ORelIds) values.get(column
								.getName());
						Integer base_id = rel_objs.getBaseId();
						if (base_id == null) {
							base_id = values.getInt(OColumn.ROW_ID);
						}
						for (String key : rel_objs.keys()) {
							RelData data = rel_objs.get(key);
							command = data.getCommand();
							rel_ids = new ArrayList<Integer>();
							rel_ids.addAll(data.getIds());
							if (command == Command.Add) {
								manageManyToManyRecords(db, rel_model, rel_ids,
										base_id, Command.Replace);
							}
						}
					} else {
						rel_ids = (List<Integer>) values.get(column.getName());
						if (rel_ids == null) {
							rel_ids = new ArrayList<Integer>();
						}
						manageManyToManyRecords(db, rel_model, rel_ids,
								values.getInt(OColumn.ROW_ID), command);
					}
					break;
				case OneToMany:
					rel_model = createInstance(column.getType());
					command = Command.Replace;
					Integer rec_id = 0;
					if (values.get(column.getName()) instanceof ORelIds) {
						ORelIds rel_objs = (ORelIds) values.get(column
								.getName());
						rec_id = rel_objs.getBaseId();
						if (rec_id == null) {
							rec_id = values.getInt(OColumn.ROW_ID);
						}
						for (String key : rel_objs.keys()) {
							RelData data = rel_objs.get(key);
							command = data.getCommand();
							rel_ids = new ArrayList<Integer>();
							rel_ids.addAll(data.getIds());
							OValues rel_vals = new OValues();
							switch (command) {
							case Add:
								rel_vals.put(column.getRelatedColumn(), rec_id);
								break;
							case Delete:
								rel_vals.put(column.getRelatedColumn(), false);
								break;
							default:
								break;
							}
							String whr = OColumn.ROW_ID
									+ " IN ("
									+ StringUtils.repeat(" ?, ",
											rel_ids.size() - 1) + "?)";
							Object[] args = new Object[] { rel_ids };
							if (rel_ids.size() > 0)
								rel_model.update(rel_vals, whr, args);
						}
					}
					break;
				default:
					break;
				}
			}
		}
		db.close();
		return true;
	}

	private void notifyDataChange(Integer id) {
		// FIXME: remove method after content provider ready
		/*
		 * Uri uri = uri(); if (id != null) uri.buildUpon().appendPath(id + "");
		 * mContext.getContentResolver().notifyChange(uri, null, false);
		 */
	}

	private void sendDatasetChangeBroadcast(Integer newId) {
		Intent intent = new Intent();
		intent.setAction(DataSetChangeReceiver.DATA_CHANGED);
		intent.putExtra("model", getModelName());
		intent.putExtra("id", newId);
		mContext.sendBroadcast(intent);
	}

	/**
	 * Gets the creates the id.
	 * 
	 * @return the creates the id
	 */
	private int getCreateId() {
		int newId = 0;
		SQLiteDatabase db = getReadableDatabase();
		Cursor cr = db.query("sqlite_sequence", new String[] { "name", "seq" },
				"name = ?", new String[] { getTableName() }, null, null, null);
		if (cr.moveToFirst()) {
			newId = cr.getInt(cr.getColumnIndex("seq"));
		}
		cr.close();
		db.close();
		return newId;
	}

	/**
	 * Update.
	 * 
	 * @param values
	 *            the values
	 * @param id
	 *            the id
	 * @return the int
	 */
	public int update(OValues values, Integer id) {
		return update(values, OColumn.ROW_ID + " = ? ", new Object[] { id });
	}

	/**
	 * Update.
	 * 
	 * @param updateValues
	 *            the update values
	 * @param where
	 *            the where
	 * @param whereArgs
	 *            the where args
	 * @return the int
	 */
	public int update(OValues updateValues, String where, Object[] whereArgs) {
		int affectedRows = 0;
		ContentValues values = createValues(updateValues);
		SQLiteDatabase db = getWritableDatabase();
		if (!updateValues.contains("is_dirty"))
			values.put("is_dirty", "true");
		affectedRows = db.update(getTableName(), values, getWhereClause(where),
				getWhereArgs(where, whereArgs));
		db.close();
		updateRelationColumns(updateValues);
		notifyDataChange(null);
		return affectedRows;
	}

	/**
	 * Delete.
	 * 
	 * @param id
	 *            the id
	 * @return true, if successful
	 */
	public boolean delete(int id) {
		return delete(OColumn.ROW_ID + " = ? ", new Object[] { id });
	}

	/**
	 * Delete.
	 * 
	 * @param where
	 *            the where
	 * @param whereArgs
	 *            the where args
	 * @return true, if successful
	 */
	public boolean delete(String where, Object[] whereArgs) {
		return delete(where, whereArgs, false);
	}

	/**
	 * Delete.
	 * 
	 * @param where
	 *            the where
	 * @param whereArgs
	 *            the where args
	 * @param removeFromLocal
	 *            the remove from local
	 * @return true, if successful
	 */
	public boolean delete(String where, Object[] whereArgs,
			boolean removeFromLocal) {
		Boolean deleted = false;
		if (removeFromLocal) {
			SQLiteDatabase db = getWritableDatabase();
			if (db.delete(getTableName(), getWhereClause(where),
					getWhereArgs(where, whereArgs)) > 0) {
				deleted = true;
			}
			db.close();
		} else {
			// Setting is_active to false.
			OValues values = new OValues();
			values.put("is_dirty", "true");
			values.put("is_active", "false");
			if (update(values, where, whereArgs) > 0)
				deleted = true;
		}
		return deleted;
	}

	/**
	 * Creates the values.
	 * 
	 * @param db
	 *            the db
	 * @param values
	 *            the values
	 * @return the content values
	 */
	private ContentValues createValues(OValues values) {
		ContentValues vals = new ContentValues();
		for (OColumn column : getColumns()) {
			// Checking for functional store column
			if (column.isFunctionalColumn() && column.canFunctionalStore()) {
				if ((column.checkRowId() && values.contains(OColumn.ROW_ID) || !column
						.checkRowId())) {
					int contains = 0;
					// getting depends column from values
					for (String col : column.getFunctionalStoreDepends())
						if (values.contains(col))
							contains++;
					if (contains == column.getFunctionalStoreDepends().size()) {
						// Getting functional value before create or update
						vals.put(column.getName(),
								getFunctionalMethodValue(column, values)
										.toString());
					}
				}
			}
			if (values.contains(column.getName())) {
				if (column.getRelationType() == null) {
					if (values.get(column.getName()) != null) {
						vals.put(column.getName(), values.get(column.getName())
								.toString());
					}
				} else {
					switch (column.getRelationType()) {
					case ManyToOne:
						vals.put(column.getName(), values.get(column.getName())
								.toString());
						break;
					default:
						break;
					}
				}
			}
		}
		vals.put("local_write_date", ODate.getDate());
		return vals;
	}

	/**
	 * Adds the many to many record.
	 * 
	 * @param column
	 *            the column
	 * @param base_id
	 *            the base_id
	 * @param relation_record_id
	 *            the relation_record_id
	 */
	public void addManyToManyRecord(String column, Integer base_id,
			Integer relation_record_id) {
		List<Integer> ids = new ArrayList<Integer>();
		ids.add(relation_record_id);
		addManyToManyRecords(column, base_id, ids);
	}

	/**
	 * Adds the many to many records.
	 * 
	 * @param column
	 *            the column
	 * @param base_id
	 *            the base_id
	 * @param relation_record_ids
	 *            the relation_record_ids
	 */
	public void addManyToManyRecords(String column, Integer base_id,
			List<Integer> relation_record_ids) {
		handleManyToManyData(column, base_id, relation_record_ids, Command.Add);
	}

	/**
	 * Delete many to many record.
	 * 
	 * @param column
	 *            the column
	 * @param base_id
	 *            the base_id
	 * @param relation_record_id
	 *            the relation_record_id
	 */
	public void deleteManyToManyRecord(String column, Integer base_id,
			Integer relation_record_id) {
		List<Integer> ids = new ArrayList<Integer>();
		ids.add(relation_record_id);
		deleteManyToManyRecords(column, base_id, ids);
	}

	/**
	 * Delete many to many records.
	 * 
	 * @param column
	 *            the column
	 * @param base_id
	 *            the base_id
	 * @param relation_record_ids
	 *            the relation_record_ids
	 */
	public void deleteManyToManyRecords(String column, Integer base_id,
			List<Integer> relation_record_ids) {
		handleManyToManyData(column, base_id, relation_record_ids,
				Command.Delete);
	}

	/**
	 * Replace many to many record.
	 * 
	 * @param column
	 *            the column
	 * @param base_id
	 *            the base_id
	 * @param relation_record_id
	 *            the relation_record_id
	 */
	public void replaceManyToManyRecord(String column, Integer base_id,
			Integer relation_record_id) {
		List<Integer> ids = new ArrayList<Integer>();
		ids.add(relation_record_id);
		replaceManyToManyRecords(column, base_id, ids);

	}

	/**
	 * Replace many to many records.
	 * 
	 * @param column
	 *            the column
	 * @param base_id
	 *            the base_id
	 * @param relation_record_ids
	 *            the relation_record_ids
	 */
	public void replaceManyToManyRecords(String column, Integer base_id,
			List<Integer> relation_record_ids) {
		handleManyToManyData(column, base_id, relation_record_ids,
				Command.Replace);
	}

	/**
	 * Handle many to many data.
	 * 
	 * @param column
	 *            the column
	 * @param base_id
	 *            the base_id
	 * @param relation_record_ids
	 *            the relation_record_ids
	 * @param command
	 *            the command
	 */
	private void handleManyToManyData(String column, Integer base_id,
			List<Integer> relation_record_ids, Command command) {
		OColumn col = getColumn(column);
		if (col.getRelationType() == RelationType.ManyToMany) {
			SQLiteDatabase db = getWritableDatabase();
			OModel rel_model = createInstance(col.getType());
			manageManyToManyRecords(db, rel_model, relation_record_ids,
					base_id, command);
			db.close();
		}
	}

	/**
	 * Manage many to many records.
	 * 
	 * @param db
	 *            the db
	 * @param rel_model
	 *            the rel_model
	 * @param ids
	 *            the ids
	 * @param base_id
	 *            the base_id
	 * @param command
	 *            the command
	 */
	public void manageManyToManyRecords(SQLiteDatabase db, OModel rel_model,
			List<Integer> ids, Integer base_id, Command command) {
		String table = getTableName() + "_" + rel_model.getTableName() + "_rel";
		String base_column = getTableName() + "_id";
		String rel_column = rel_model.getTableName() + "_id";

		switch (command) {
		case Add:
			// Adding records to relation model
			if (ids.size() > 0) {
				for (int id : ids) {
					ContentValues values = new ContentValues();
					values.put(base_column, base_id);
					values.put(rel_column, id);
					values.put("odoo_name", mUser.getAndroidName());
					values.put("local_write_date", ODate.getDate());
					db.insert(table, null, values);
				}
			}
			break;
		case Update:
			break;
		case Delete:
			// Deleting records to relation model
			if (ids.size() > 0) {
				for (int id : ids) {
					db.delete(table, base_column + " = ? AND  " + rel_column
							+ " = ?", new String[] { base_id + "", id + "" });
				}
			}
			break;
		case Replace:
			// Removing old entries
			if (ids.size() > 0) {
				String where = base_column + " = ? ";
				String[] args = new String[] { base_id + "" };
				db.delete(table, getWhereClause(where),
						getWhereArgs(where, args));
				// Creating new entries
				manageManyToManyRecords(db, rel_model, ids, base_id,
						Command.Add);
			}
			break;
		}
	}

	/**
	 * Gets the where args.
	 * 
	 * @param where
	 *            the where
	 * @param whereArgs
	 *            the where args
	 * @return the where args
	 */
	private String[] getWhereArgs(String where, Object[] whereArgs) {
		List<String> args = new ArrayList<String>();
		if (whereArgs != null) {
			for (Object obj : whereArgs) {
				if (obj instanceof int[]) {
					// List<String> ids = new ArrayList<String>();
					for (int id : (int[]) obj) {
						// ids.add(id + "");
						args.add(id + "");
					}
					// args.add(TextUtils.join(",", ids));
				} else if (obj instanceof ArrayList<?>) {
					for (Object id : (ArrayList<?>) obj) {
						// ids.add(id + "");
						args.add(id + "");
					}
					// args.add(TextUtils.join("','", ids));
				} else {
					args.add(obj.toString());
				}
			}
		}
		args.add((mUser != null) ? mUser.getAndroidName() : "");
		if (!mCheckInActiveRecord)
			args.add("true");
		return args.toArray(new String[args.size()]);
	}

	/**
	 * Gets the where clause.
	 * 
	 * @param where
	 *            the where
	 * @return the where clause
	 */
	private String getWhereClause(String where) {
		String newWhereClause = (where != null) ? where + " AND " : "";
		if (!mCheckInActiveRecord)
			newWhereClause += "odoo_name = ? AND is_active = ? ";
		else
			newWhereClause += "odoo_name = ? ";
		return newWhereClause;
	}

	/**
	 * Gets the sync helper.
	 * 
	 * @return the sync helper
	 */
	public OSyncHelper getSyncHelper() {
		if (mSyncHelper == null)
			mSyncHelper = new OSyncHelper(mContext, mUser, this);
		mSyncingData = true;
		return mSyncHelper;
	}

	/**
	 * Sets the syncing data flag.
	 * 
	 * @param syncingData
	 *            the syncing data
	 * @return the o model
	 */
	public OModel setSyncingDataFlag(Boolean syncingData) {
		mSyncingData = syncingData;
		return this;
	}

	/**
	 * Gets the model values.
	 * 
	 * @param model
	 *            the model
	 * @return the model values
	 */
	public List<OValues> getModelValues(String model) {
		List<String> models = new ArrayList<String>();
		models.add(model);
		return getModelValues(models);

	}

	/**
	 * Gets the model values.
	 * 
	 * @param models
	 *            the models
	 * @return the model values
	 */
	public List<OValues> getModelValues(List<String> models) {
		if (mSyncHelper == null)
			mSyncHelper = new OSyncHelper(mContext, mUser, this);
		return mSyncHelper.modelInfo(models);
	}

	/**
	 * Gets the.
	 * 
	 * @param context
	 *            the context
	 * @param model_name
	 *            the model_name
	 * @return the o model
	 */
	public static OModel get(Context context, String model_name) {
		OModel model = null;
		try {
			PreferenceManager pfManager = new PreferenceManager(context);
			Class<?> model_class = Class.forName(pfManager.getString(
					model_name, null));
			if (model_class != null)
				model = new OModel(context, model_name)
						.createInstance(model_class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return model;
	}

	/**
	 * Checks if is empty table.
	 * 
	 * @return true, if is empty table
	 */
	public boolean isEmptyTable() {
		if (count() <= 0)
			return true;
		return false;
	}

	/**
	 * Ids.
	 * 
	 * @return the list
	 */
	public List<Integer> ids() {
		List<Integer> ids = new ArrayList<Integer>();
		for (ODataRow row : select()) {
			ids.add(row.getInt("id"));
		}
		return ids;
	}

	/**
	 * Before create row.
	 * 
	 * @param column
	 *            the column
	 * @param original_record
	 *            the original_record
	 * @return the JSON object
	 */
	public JSONObject beforeCreateRow(OColumn column, JSONObject original_record) {
		return original_record;
	}

	/**
	 * Check in active record.
	 * 
	 * @param checkInactiveRecord
	 *            the check inactive record
	 */
	public void checkInActiveRecord(Boolean checkInactiveRecord) {
		mCheckInActiveRecord = checkInactiveRecord;
	}

	/**
	 * Check for local update.
	 * 
	 * @return the boolean
	 */
	public Boolean checkForLocalUpdate() {
		return true;
	}

	/**
	 * Can update to server.
	 * 
	 * @return the boolean
	 */
	public Boolean canUpdateToServer() {
		return true;
	}

	/**
	 * Can delete from server.
	 * 
	 * @return the boolean
	 */
	public Boolean canDeleteFromServer() {
		return true;
	}

	/**
	 * Can delete from local.
	 * 
	 * @return the boolean
	 */
	public Boolean canDeleteFromLocal() {
		return true;
	}

	/**
	 * Can create on server.
	 * 
	 * @return the boolean
	 */
	public Boolean canCreateOnServer() {
		return true;
	}

	/**
	 * Check for write date.
	 * 
	 * @return the boolean
	 */
	public Boolean checkForWriteDate() {
		return true;
	}

	/**
	 * Check for create date.
	 * 
	 * @return the boolean
	 */
	public Boolean checkForCreateDate() {
		return true;
	}

	public Boolean checkForLocalLatestUpdate() {
		return true;
	}

	public OUser user() {
		return mUser;
	}

	public OModel setLimit(Integer limit) {
		mLimit = limit;
		return this;
	}

	public OModel setOffset(Integer offset_index) {
		mOffset = offset_index;
		return this;
	}

	public Integer getNextOffset() {
		return mOffset + mLimit;
	}

	public void setCreateWriteLocal(Boolean make_local) {
		if (make_local) {
			write_date.setLocalColumn();
			create_date.setLocalColumn();
		}
	}

	/**
	 * browse, New API
	 * 
	 * @return OQuery object
	 */
	public OQuery browse() {
		return new OQuery(mContext, this, QueryType.Select);
	}

	/**
	 * The Class AutoUpdateOnServer.
	 */
	class AutoUpdateOnServer extends AsyncTask<Void, Void, Void> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Void doInBackground(Void... params) {
			getSyncHelper().syncWithServer();
			return null;
		}
	}

	public OContentResolver resolver() {
		return new OContentResolver(this, mContext);
	}

}

/**
 * The Interface OModelHelper.
 */
interface OModelHelper {

	/**
	 * Gets the columns.
	 * 
	 * @return the columns
	 */
	public List<OColumn> getColumns();

	/**
	 * Gets the columns.
	 * 
	 * @param local
	 *            the local
	 * @return the columns
	 */
	public List<OColumn> getColumns(Boolean local);

	/**
	 * Default domain.
	 * 
	 * @return the o domain
	 */
	public ODomain defaultDomain();
}
