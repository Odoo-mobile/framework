/**
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
package com.openerp.orm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.openerp.base.ir.Ir_model;
import com.openerp.orm.OEM2MIds.Operation;
import com.openerp.orm.types.OEDateTime;
import com.openerp.orm.types.OEManyToMany;
import com.openerp.orm.types.OEManyToOne;
import com.openerp.orm.types.OEOneToMany;
import com.openerp.orm.types.OETimestamp;
import com.openerp.orm.types.OETypeHelper;
import com.openerp.receivers.DataSetChangeReceiver;
import com.openerp.support.OEUser;
import com.openerp.util.OEDate;

public abstract class OEDatabase extends OESQLiteHelper implements OEDBHelper {
	public static final String TAG = "com.openerp.orm.OEDatabase";
	Context mContext = null;
	OEDBHelper mDBHelper = null;
	OEUser mUser = null;
	List<OEDataRow> mRemovedRecords = new ArrayList<OEDataRow>();
	OEHelper mOEHelper = null;

	public OEDatabase(Context context) {
		super(context);
		mContext = context;
		mUser = OEUser.current(mContext);
		mDBHelper = this;
	}

	public String modelName() {
		return mDBHelper.getModelName();
	}

	public String tableName() {
		return mDBHelper.getModelName().replaceAll("\\.", "_");
	}

	public void setAccountUser(OEUser user) {
		mUser = user;
	}

	public int count() {
		return count(null, null);
	}

	public int count(String where, String[] whereArgs) {
		int count = 0;
		if (where == null) {
			where = " oea_name = ?";
			whereArgs = new String[] { mUser.getAndroidName() };
		} else {
			where += " and oea_name = ?";
			List<String> tmpWhereArgs = new ArrayList<String>();
			tmpWhereArgs.addAll(Arrays.asList(whereArgs));
			tmpWhereArgs.add(mUser.getAndroidName());
			whereArgs = tmpWhereArgs.toArray(new String[tmpWhereArgs.size()]);
		}
		SQLiteDatabase db = getReadableDatabase();
		Cursor cr = db.query(tableName(), new String[] { "count(*) as total" },
				where, whereArgs, null, null, null);
		if (cr.moveToFirst()) {
			count = cr.getInt(0);
		}
		cr.close();
		db.close();
		return count;
	}

	public int update(OEValues values, int id) {
		return update(values, "id = ?", new String[] { id + "" });
	}

	public void updateManyToManyRecords(String column, Operation operation,
			int id, int rel_id) {
		List<Integer> ids = new ArrayList<Integer>();
		ids.add(rel_id);
		updateManyToManyRecords(column, operation, id, ids);
	}

	public void updateManyToManyRecords(String column, Operation operation,
			int id, List<Integer> ids) {
		OEDBHelper m2mObj = findFieldModel(column);
		manageMany2ManyRecords(m2mObj, operation, (long) id, ids);
	}

	public int update(OEValues values, String where, String[] whereArgs) {
		if (where == null) {
			where = " oea_name = ?";
			whereArgs = new String[] { mUser.getAndroidName() };
		} else {
			where += " and oea_name = ?";
			List<String> tmpWhereArgs = new ArrayList<String>();
			tmpWhereArgs.addAll(Arrays.asList(whereArgs));
			tmpWhereArgs.add(mUser.getAndroidName());
			whereArgs = tmpWhereArgs.toArray(new String[tmpWhereArgs.size()]);
		}
		if (!values.contains("oea_name")) {
			values.put("oea_name", mUser.getAndroidName());
		}
		SQLiteDatabase db = getWritableDatabase();
		HashMap<String, Object> res = getContentValues(values);
		ContentValues cValues = (ContentValues) res.get("cValues");
		int count = db.update(tableName(), cValues, where, whereArgs);
		db.close();
		if (res.containsKey("m2mObjects")) {
			@SuppressWarnings("unchecked")
			List<HashMap<String, Object>> objectList = (List<HashMap<String, Object>>) res
					.get("m2mObjects");
			for (HashMap<String, Object> obj : objectList) {
				OEDBHelper m2mDb = (OEDBHelper) obj.get("m2mObject");
				for (OEDataRow row : select(where, whereArgs, null, null, null)) {
					manageMany2ManyRecords(m2mDb, Operation.REPLACE,
							row.getInt("id"), obj.get("m2mRecordsObj"));
				}
			}
		}
		return count;

	}

	public List<Long> createORReplace(List<OEValues> listValues) {
		return createORReplace(listValues, false);
	}

	public List<Long> createORReplace(List<OEValues> listValues,
			boolean canDeleteLocalIfNotExists) {
		List<Long> ids = new ArrayList<Long>();
		for (OEValues values : listValues) {
			long id = values.getInt("id");
			if (id == -1)
				continue;
			int count = count("id = ?", new String[] { values.getString("id") });
			if (count == 0) {
				ids.add(id);
				create(values);
			} else {
				ids.add(id);
				update(values, values.getInt("id"));
			}
		}
		if (canDeleteLocalIfNotExists) {
			mRemovedRecords = new ArrayList<OEDataRow>();
			for (OEDataRow row : select()) {
				if (!ids.contains(Long.parseLong(row.getString("id")))) {
					delete(row.getInt("id"));
					mRemovedRecords.add(row);
				}
			}
		}
		return ids;
	}

	public List<OEDataRow> getRemovedRecords() {
		return mRemovedRecords;
	}

	public long create(OEValues values) {
		long newId = 0;
		if (!values.contains("oea_name")) {
			values.put("oea_name", mUser.getAndroidName());
		}
		SQLiteDatabase db = getWritableDatabase();
		HashMap<String, Object> res = getContentValues(values);
		ContentValues cValues = (ContentValues) res.get("cValues");
		db.insert(tableName(), null, cValues);
		newId = cValues.getAsInteger("id");
		broadcastInfo(newId);
		db.close();
		if (res.containsKey("m2mObjects")) {
			@SuppressWarnings("unchecked")
			List<HashMap<String, Object>> objectList = (List<HashMap<String, Object>>) res
					.get("m2mObjects");
			for (HashMap<String, Object> obj : objectList) {
				OEDBHelper m2mDb = (OEDBHelper) obj.get("m2mObject");
				manageMany2ManyRecords(m2mDb, Operation.ADD, newId,
						obj.get("m2mRecordsObj"));
			}
		}
		return newId;
	}

	private HashMap<String, Object> getContentValues(OEValues values) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		ContentValues cValues = new ContentValues();
		List<HashMap<String, Object>> m2mObjectList = new ArrayList<HashMap<String, Object>>();
		List<OEColumn> cols = mDBHelper.getModelColumns();
		cols.addAll(getDefaultCols());
		for (OEColumn col : cols) {
			String key = col.getName();
			if (values.contains(key)) {
				if (values.get(key) instanceof OEM2MIds
						|| values.get(key) instanceof List) {
					HashMap<String, Object> m2mObjects = new HashMap<String, Object>();
					OEDBHelper m2mDb = findFieldModel(key);
					m2mObjects.put("m2mObject", m2mDb);
					m2mObjects.put("m2mRecordsObj", values.get(key));
					m2mObjectList.add(m2mObjects);
					continue;
				}
				cValues.put(key, values.get(key).toString());
			}
			/**
			 * Adding default timestamp in UTC TimeZone in YYYY-MM-DD HH:MM:SS
			 * format.
			 */
			if (col.getType() instanceof OETimestamp) {
				cValues.put(key, OEDate.getDate());
			}
		}
		result.put("m2mObjects", m2mObjectList);
		result.put("cValues", cValues);
		return result;
	}

	@SuppressWarnings("unchecked")
	private void manageMany2ManyRecords(OEDBHelper relDb, Operation operation,
			long id, Object idsObj) {
		String first_table = tableName();
		String second_table = relDb.getModelName().replaceAll("\\.", "_");
		String rel_table = first_table + "_" + second_table + "_rel";
		List<Integer> ids = new ArrayList<Integer>();
		if (idsObj instanceof OEM2MIds) {
			OEM2MIds idsObject = (OEM2MIds) idsObj;
			operation = idsObject.getOperation();
			ids = idsObject.getIds();
		}
		if (idsObj instanceof List) {
			ids = (List<Integer>) idsObj;
		}
		SQLiteDatabase db = null;
		String col_first = first_table + "_id";
		String col_second = second_table + "_id";
		if (operation == Operation.REPLACE) {
			db = getWritableDatabase();
			db.delete(rel_table, col_first + " = ? AND oea_name = ?",
					new String[] { id + "", mUser.getAndroidName() });
			db.close();
		}
		for (Integer rId : ids) {
			ContentValues values = new ContentValues();
			values.put(col_first, id);
			values.put(col_second, rId);
			values.put("oea_name", mUser.getAndroidName());
			switch (operation) {
			case ADD:
			case APPEND:
			case REPLACE:
				Log.d(TAG,
						"manageMany2ManyRecords() ADD, APPEND, REPLACE called");
				if (!hasRecord(rel_table, col_first + " = ? AND " + col_second
						+ " = ? AND oea_name = ?", new String[] { id + "",
						rId + "", mUser.getAndroidName() })) {
					db = getWritableDatabase();
					db.insert(rel_table, null, values);
					db.close();
				}
				break;
			case REMOVE:
				Log.d(TAG, "createMany2ManyRecords() REMOVE called");
				db = getWritableDatabase();
				db.delete(rel_table, col_first + " = ? AND " + col_second
						+ " = ? AND oea_name = ?", new String[] { id + "",
						rId + "", mUser.getAndroidName() });
				db.close();
				break;
			}
		}
	}

	private boolean hasRecord(String table, String where, String[] whereArgs) {
		boolean flag = false;
		if (where == null) {
			where = " oea_name = ?";
			whereArgs = new String[] { mUser.getAndroidName() };
		} else {
			where += " and oea_name = ?";
			List<String> tmpWhereArgs = new ArrayList<String>();
			tmpWhereArgs.addAll(Arrays.asList(whereArgs));
			tmpWhereArgs.add(mUser.getAndroidName());
			whereArgs = tmpWhereArgs.toArray(new String[tmpWhereArgs.size()]);
		}
		SQLiteDatabase db = getReadableDatabase();
		Cursor cr = db.query(table, new String[] { "count(*) as total" },
				where, whereArgs, null, null, null);
		cr.moveToFirst();
		int count = cr.getInt(0);
		cr.close();
		db.close();
		if (count > 0) {
			flag = true;
		}
		return flag;
	}

	private OEDBHelper findFieldModel(String field) {
		for (OEColumn col : mDBHelper.getModelColumns()) {
			if (field.equals(col.getName())) {
				OEManyToMany m2m = (OEManyToMany) col.getType();
				return m2m.getDBHelper();
			}
		}
		return null;
	}

	public int delete() {
		return delete(null, null);
	}

	public int delete(String table) {
		return delete(table, null, null);
	}

	public int delete(int id) {
		return delete("id = ?", new String[] { id + "" });
	}

	public int delete(String where, String[] whereArgs) {
		return delete(tableName(), where, whereArgs);
	}

	private int delete(String table, String where, String[] whereArgs) {
		if (where == null) {
			where = "oea_name = ?";
			whereArgs = new String[] { mUser.getAndroidName() };
		} else {
			where += " AND oea_name = ?";
			List<String> tmpWhereArgs = new ArrayList<String>();
			tmpWhereArgs.addAll(Arrays.asList(whereArgs));
			tmpWhereArgs.add(mUser.getAndroidName());
			whereArgs = tmpWhereArgs.toArray(new String[tmpWhereArgs.size()]);
		}

		int count = 0;
		if (deleteMany2ManyRecord(select(where, whereArgs))) {
			SQLiteDatabase db = getWritableDatabase();
			count = db.delete(table, where, whereArgs);
			db.close();
		}
		return count;
	}

	private boolean deleteMany2ManyRecord(List<OEDataRow> records) {
		for (OEDataRow rec : records) {
			int id = rec.getInt("id");
			for (OEColumn col : getModelColumns()) {
				if (col.getType() instanceof OEManyToMany) {
					OEDatabase m2mDB = (OEDatabase) ((OEManyToMany) col
							.getType()).getDBHelper();
					List<Integer> idsObj = new ArrayList<Integer>();
					for (OEDataRow m2mRec : rec.getM2MRecord(col.getName())
							.browseEach())
						idsObj.add(m2mRec.getInt("id"));
					manageMany2ManyRecords(m2mDB, Operation.REMOVE, id, idsObj);
				}
			}
		}
		return true;
	}

	public List<OEDataRow> select() {
		return select(null, null, null, null, null);
	}

	public OEDataRow select(int id) {
		List<OEDataRow> rows = select("id = ?", new String[] { id + "" }, null,
				null, null);
		if (rows.size() > 0) {
			return rows.get(0);
		}
		return null;
	}

	public List<OEDataRow> select(String where, String[] whereArgs) {
		return select(where, whereArgs, null, null, null);
	}

	public List<Integer> ids() {
		List<Integer> ids = new ArrayList<Integer>();
		for (OEDataRow row : select()) {
			ids.add(row.getInt("id"));
		}
		return ids;
	}

	public List<OEDataRow> select(String where, String[] whereArgs,
			String groupBy, String having, String orderBy) {
		if (where == null) {
			where = "oea_name = ?";
			whereArgs = new String[] { mUser.getAndroidName() };
		} else {
			where += " AND oea_name = ?";
			List<String> tmpWhereArgs = new ArrayList<String>();
			tmpWhereArgs.addAll(Arrays.asList(whereArgs));
			tmpWhereArgs.add(mUser.getAndroidName());
			whereArgs = tmpWhereArgs.toArray(new String[tmpWhereArgs.size()]);
		}
		List<OEDataRow> rows = new ArrayList<OEDataRow>();
		SQLiteDatabase db = getReadableDatabase();
		String[] cols = getColumns();
		Cursor cr = db.query(tableName(), cols, where, whereArgs, groupBy,
				having, orderBy);
		List<OEColumn> mCols = mDBHelper.getModelColumns();
		mCols.addAll(getDefaultCols());
		if (cr.moveToFirst()) {
			do {
				OEDataRow row = new OEDataRow();
				for (OEColumn col : mCols) {
					row.put(col.getName(), createRowData(col, cr));
				}
				rows.add(row);
			} while (cr.moveToNext());
		}
		cr.close();
		db.close();
		return rows;
	}

	public List<OEDataRow> selectM2M(OEDBHelper rel_db, String where,
			String[] whereArgs) {
		if (where == null) {
			where = "oea_name = ?";
			whereArgs = new String[] { mUser.getAndroidName() };
		} else {
			where += " AND oea_name = ?";
			List<String> tmpWhereArgs = new ArrayList<String>();
			tmpWhereArgs.addAll(Arrays.asList(whereArgs));
			tmpWhereArgs.add(mUser.getAndroidName());
			whereArgs = tmpWhereArgs.toArray(new String[tmpWhereArgs.size()]);
		}
		List<OEDataRow> rows = new ArrayList<OEDataRow>();
		HashMap<String, Object> mRelObj = relTableColumns(rel_db);
		@SuppressWarnings("unchecked")
		List<OEColumn> mCols = (List<OEColumn>) mRelObj.get("columns");
		List<String> cols = new ArrayList<String>();
		for (OEColumn col : mCols) {
			cols.add(col.getName());
		}
		SQLiteDatabase db = getReadableDatabase();
		Cursor cr = db.query(mRelObj.get("rel_table").toString(),
				cols.toArray(new String[cols.size()]), where, whereArgs, null,
				null, null);
		OEDatabase rel_db_obj = (OEDatabase) rel_db;
		String rel_col_name = rel_db_obj.tableName() + "_id";
		if (cr.moveToFirst()) {
			do {
				int id = cr.getInt(cr.getColumnIndex(rel_col_name));
				rows.add(rel_db_obj.select(id));
			} while (cr.moveToNext());
		}
		cr.close();
		db.close();
		return rows;
	}

	private Object createRowData(OEColumn col, Cursor cr) {
		if (col.getType() instanceof OETypeHelper) {
			String value = cr.getString(cr.getColumnIndex(col.getName()));
			if (col.getType() instanceof OETimestamp
					|| col.getType() instanceof OEDateTime) {
				value = OEDate.getDate(mContext, value, TimeZone.getDefault()
						.getID(), OEDate.DEFAULT_FORMAT);
			}
			return value;
		}
		if (col.getType() instanceof OEManyToOne) {
			return new OEM2ORecord(col, cr.getString(cr.getColumnIndex(col
					.getName())));
		}
		if (col.getType() instanceof OEManyToMany) {
			return new OEM2MRecord(this, col,
					cr.getInt(cr.getColumnIndex("id")));
		}

		if (col.getType() instanceof OEOneToMany) {
			return new OEO2MRecord(this, col,
					cr.getInt(cr.getColumnIndex("id")));
		}
		return null;
	}

	private String[] getColumns() {
		List<String> cols = new ArrayList<String>();
		cols.add("id");
		for (OEColumn col : mDBHelper.getModelColumns()) {
			if (col.getType() instanceof OETypeHelper
					|| col.getType() instanceof OEManyToOne) {
				cols.add(col.getName());
			}
		}
		cols.add("oea_name");
		return cols.toArray(new String[cols.size()]);
	}

	public OEHelper getOEInstance() {
		Log.d(TAG, "OEDatabase->getOEInstance()");
		if (mOEHelper == null) {
			try {
				mOEHelper = new OEHelper(mContext, this);
			} catch (Exception e) {
				Log.d(TAG, "OEDatabase->getOEInstance()");
				Log.e(TAG, e.getMessage()
						+ ". No connection with OpenERP server");
			}
		}
		return mOEHelper;
	}

	public boolean isInstalledOnServer() {
		OEHelper oe = getOEInstance();
		boolean installed = false;
		if (oe != null) {
			installed = oe.isModelInstalled(getModelName());
		} else {
			Ir_model ir = new Ir_model(mContext);
			List<OEDataRow> rows = ir.select("model = ?",
					new String[] { getModelName() });
			if (rows.size() > 0) {
				installed = rows.get(0).getBoolean("is_installed");
			}
		}
		return installed;
	}

	public boolean truncateTable(String table) {
		if (delete(table) > 0) {
			return true;
		}
		return false;
	}

	public boolean truncateTable() {
		if (delete() > 0) {
			return true;
		}
		return false;
	}

	public boolean isEmptyTable() {
		boolean flag = true;
		if (count() > 0) {
			flag = false;
		}
		return flag;
	}

	public HashMap<String, Object> relTableColumns(OEDBHelper relDB) {
		List<OEColumn> mCols = new ArrayList<OEColumn>();
		HashMap<String, Object> res = new HashMap<String, Object>();
		String main_table = tableName();
		String ref_table = relDB.getModelName().replaceAll("\\.", "_");
		String rel_table = main_table + "_" + ref_table + "_rel";
		res.put("rel_table", rel_table);
		mCols.add(new OEColumn(main_table + "_id", "Main ID", OEFields
				.integer()));
		mCols.add(new OEColumn(ref_table + "_id", "Ref ID", OEFields.integer()));
		mCols.add(new OEColumn("oea_name", "Android name", OEFields.text()));
		res.put("columns", mCols);
		return res;
	}

	public List<OEColumn> getDefaultCols() {
		List<OEColumn> cols = new ArrayList<OEColumn>();
		cols.add(new OEColumn("id", "id", OEFields.integer()));
		cols.add(new OEColumn("oea_name", "android name", OEFields.varchar(50),
				false));
		return cols;
	}

	public List<OEColumn> getDatabaseColumns() {
		return mDBHelper.getModelColumns();
	}

	public List<OEColumn> getDatabaseServerColumns() {
		List<OEColumn> cols = new ArrayList<OEColumn>();
		for (OEColumn col : mDBHelper.getModelColumns()) {
			if (col.canSync()) {
				cols.add(col);
			}
		}
		return cols;
	}

	public int lastId() {
		int last_id = 0;
		SQLiteDatabase db = getReadableDatabase();
		Cursor cr = db.query(tableName(), new String[] { "MAX(id) as id" },
				"oea_name = ?", new String[] { mUser.getAndroidName() }, null,
				null, null);
		if (cr.moveToFirst())
			last_id = cr.getInt(0);
		cr.close();
		db.close();
		return last_id;
	}

	private void broadcastInfo(long newId) {
		Intent intent = new Intent();
		intent.setAction(DataSetChangeReceiver.DATA_CHANGED);
		intent.putExtra("id", String.valueOf(newId));
		intent.putExtra("model", modelName());
		mContext.sendBroadcast(intent);
	}
}
