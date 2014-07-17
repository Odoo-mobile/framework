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
import java.util.List;

import odoo.ODomain;

import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.odoo.orm.annotations.Odoo;
import com.odoo.orm.annotations.Odoo.Functional;
import com.odoo.orm.types.OBoolean;
import com.odoo.orm.types.ODateTime;
import com.odoo.orm.types.OInteger;
import com.odoo.orm.types.OText;
import com.odoo.orm.types.OVarchar;
import com.odoo.support.OUser;
import com.odoo.util.ODate;
import com.odoo.util.PreferenceManager;
import com.odoo.util.StringUtils;

/**
 * The Class OModel.
 */
public class OModel extends OSQLiteHelper implements OModelHelper {

	/** The Constant TAG. */
	public static final String TAG = OModel.class.getSimpleName();

	/** The m context. */
	private Context mContext = null;

	/** The _name. */
	private String _name = null;

	/** The m columns. */
	private List<OColumn> mColumns = new ArrayList<OColumn>();

	/** The m functional columns. */
	private List<OColumn> mFunctionalColumns = new ArrayList<OColumn>();

	/** The m user. */
	private OUser mUser = null;

	/** The m sync helper. */
	private OSyncHelper mSyncHelper = null;

	/** The m check in active record. */
	private Boolean mCheckInActiveRecord = false;

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
	OColumn id = new OColumn("ID", OInteger.class).setDefault(false);

	// Local Base Columns
	/** The local_id. */
	OColumn local_id = new OColumn("Local ID", OInteger.class)
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

	/**
	 * Instantiates a new o model.
	 * 
	 * @param context
	 *            the context
	 * @param model_name
	 *            the model_name
	 */
	public OModel(Context context, String model_name) {
		super(context);
		mContext = context;
		_name = model_name;
		mUser = OUser.current(mContext);
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

	/**
	 * Prepare columns.
	 */
	public void prepareColumns() {
		List<Field> fields = new ArrayList<Field>();
		fields.addAll(Arrays.asList(getClass().getSuperclass()
				.getDeclaredFields()));
		fields.addAll(Arrays.asList(getClass().getDeclaredFields()));
		for (Field field : fields) {
			field.setAccessible(true);
			try {
				if (field.getType().isAssignableFrom(OColumn.class)) {
					OColumn column = (OColumn) field.get(this);
					column.setName(field.getName());
					Method method = checkForFunctionalColumn(field);
					if (method != null) {
						column.setFunctionalMethod(method);
						mFunctionalColumns.add(column);
					} else
						mColumns.add(column);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
			Field field = getClass().getDeclaredField(name);
			if (field == null)
				field = getClass().getSuperclass().getDeclaredField(name);
			if (field != null) {
				field.setAccessible(true);
				Method method = checkForFunctionalColumn(field);
				column = (OColumn) field.get(this);
				column.setName(name);
				if (method != null)
					column.setFunctionalMethod(method);
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
				return getClass().getMethod(method_name, ODataRow.class);
			} catch (NoSuchMethodException e) {
				Log.e(TAG, "No Such Method: " + e.getMessage());
			}
		}
		return null;
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
	public Object getFunctionalMethodValue(OColumn column, ODataRow record) {
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
		return select(id, false);
	}

	/**
	 * Select.
	 * 
	 * @param id
	 *            the id
	 * @param local_record
	 *            the local_record
	 * @return the o data row
	 */
	public ODataRow select(Integer id, boolean local_record) {
		String selection = (local_record) ? "local_id = ?" : "id = ?";
		List<ODataRow> records = select(selection, new Object[] { id }, null,
				null, null);
		if (records.size() > 0)
			return records.get(0);
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
		Cursor cr = db.query(getTableName(), new String[] { "*" },
				getWhereClause(where), getWhereArgs(where, whereArgs), groupBy,
				having, orderBy);
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
											.getColumnIndex("id"))));
							break;
						case OneToMany:
							row.put(col.getName(),
									new OO2MRecord(this, col, cr.getInt(cr
											.getColumnIndex("id"))));
							break;
						case ManyToOne:
							row.put(col.getName(),
									new OM2ORecord(this, col, cr.getString(cr
											.getColumnIndex(col.getName()))));
							break;
						}
					}
				}
				if (row.getInt("id") == 0
						|| row.getString("id").equals("false")) {
					row.put("id", 0);
					row.put("local_record", true);
				} else {
					row.put("local_record", false);
				}
				records.add(row);
			} while (cr.moveToNext());
		}
		cr.close();
		db.close();
		return records;
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
		String where = base_col + " = ?";
		Object[] whereArgs = new Object[] { base_id };
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
				"id IN (" + StringUtils.repeat(" ?, ", ids.size() - 1) + "?)",
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
	private Object createRecordRow(OColumn column, Cursor cr) {
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
		Log.v(TAG, "creating OR Replacing " + values_list.size() + " records");
		List<Integer> ids = new ArrayList<Integer>();
		for (OValues values : values_list) {
			if (!hasRecord(values.getInt("id")))
				ids.add(create(values));
			else {
				ids.add(values.getInt("id"));
				update(values, values.getInt("id"));
			}
		}
		return ids;
	}

	/**
	 * Checks for record.
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
		Integer newId = (values.contains("id")) ? values.getInt("id") : 0;
		if (!values.contains("odoo_name")) {
			values.put("odoo_name", mUser.getAndroidName());
		}
		SQLiteDatabase db = getWritableDatabase();
		db.insert(getTableName(), null, createValues(db, values));
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
	public int update(OValues values, int id) {
		return update(values, id, false);
	}

	/**
	 * Update.
	 * 
	 * @param values
	 *            the values
	 * @param id
	 *            the id
	 * @param local_record
	 *            the local_record
	 * @return the int
	 */
	public int update(OValues values, Integer id, Boolean local_record) {
		return update(values, (local_record) ? "local_id = ? " : "id = ?",
				new Object[] { id });
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
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = createValues(db, updateValues);
		values.put("is_dirty", "true");
		affectedRows = db.update(getTableName(), values, getWhereClause(where),
				getWhereArgs(where, whereArgs));
		db.close();
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
		return delete("id  = ? ", new Object[] { id });
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

	// createValues : used by create and update methods
	/**
	 * Creates the values.
	 * 
	 * @param db
	 *            the db
	 * @param values
	 *            the values
	 * @return the content values
	 */
	private ContentValues createValues(SQLiteDatabase db, OValues values) {
		ContentValues vals = new ContentValues();
		for (OColumn column : getColumns()) {
			if (values.contains(column.getName())) {
				if (column.getRelationType() == null) {
					if (values.get(column.getName()) != null)
						vals.put(column.getName(), values.get(column.getName())
								.toString());
				} else {
					switch (column.getRelationType()) {
					case ManyToOne:
						vals.put(column.getName(), values.get(column.getName())
								.toString());
						break;
					case ManyToMany:
						OModel rel_model = createInstance(column.getType());
						List<Integer> rel_ids = (List<Integer>) values
								.get(column.getName());
						manageManyToManyRecords(db, rel_model, rel_ids,
								values.getInt("id"), Command.Replace);
						// (6,false,[new ids]) - will replace with given
						break;
					case OneToMany:
						// (0, false {fields}) - will going to create/add new
						// line
						// (1, id, {fields}) - will going to update given id.
						// (2, id, false) - will going to delete record.
						break;
					}
				}
			}
		}
		vals.put("local_write_date", ODate.getDate());
		return vals;
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
			break;
		case Update:
			break;
		case Delete:
			break;
		case Replace:
			// Removing old entries
			String where = base_column + " = ? ";
			String[] args = new String[] { base_id + "" };
			db.delete(table, getWhereClause(where), getWhereArgs(where, args));
			// Creating new entries
			for (int id : ids) {
				ContentValues values = new ContentValues();
				values.put(base_column, base_id);
				values.put(rel_column, id);
				values.put("odoo_name", mUser.getAndroidName());
				values.put("local_write_date", ODate.getDate());
				db.insert(table, null, values);
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
		return mSyncHelper;
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
