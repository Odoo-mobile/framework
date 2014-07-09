package com.odoo.orm;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import odoo.ODomain;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.odoo.orm.types.OBoolean;
import com.odoo.orm.types.ODateTime;
import com.odoo.orm.types.OInteger;
import com.odoo.orm.types.OText;
import com.odoo.orm.types.OVarchar;
import com.odoo.support.OUser;
import com.odoo.util.ODate;
import com.odoo.util.PreferenceManager;
import com.odoo.util.StringUtils;

public class OModel extends OSQLiteHelper implements OModelHelper {

	public static final String TAG = OModel.class.getSimpleName();

	Context mContext = null;
	String _name = null;
	List<OColumn> mColumns = new ArrayList<OColumn>();
	OUser mUser = null;
	OSyncHelper mSyncHelper = null;

	public enum Command {
		Add, Update, Delete, Replace
	}

	// Server Base Columns
	OColumn id = new OColumn("ID", OInteger.class).setDefault(false);

	// Local Base Columns
	OColumn local_id = new OColumn("Local ID", OInteger.class)
			.setAutoIncrement(true).setLocalColumn();
	OColumn odoo_name = new OColumn("Odoo Account Name", OVarchar.class, 100)
			.setRequired(true).setLocalColumn();
	OColumn local_write_date = new OColumn("Local Write Date", ODateTime.class)
			.setLocalColumn();
	OColumn is_dirty = new OColumn("Dirty Row", OText.class).setDefault(false)
			.setLocalColumn();
	OColumn is_active = new OColumn("Row Active", OBoolean.class).setDefault(
			true).setLocalColumn();

	public OModel(Context context, String model_name) {
		super(context);
		mContext = context;
		_name = model_name;
		mUser = OUser.current(mContext);
	}

	public void setUser(OUser user) {
		mUser = user;
	}

	public String getModelName() {
		return _name;
	}

	public String getTableName() {
		return _name.replaceAll("\\.", "_");
	}

	public List<OColumn> getColumns() {
		return getColumns(null);
	}

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

	@Override
	public ODomain defaultDomain() {
		return new ODomain();
	}

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
					mColumns.add(column);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public OColumn getColumn(String name) {
		OColumn column = null;
		try {
			Field field = getClass().getDeclaredField(name);
			if (field == null)
				field = getClass().getSuperclass().getDeclaredField(name);
			if (field != null) {
				field.setAccessible(true);
				column = (OColumn) field.get(this);
				column.setName(name);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return column;
	}

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

	public List<ODataRow> select() {
		return select(null, null, null, null, null);
	}

	public ODataRow select(Integer id) {
		return select(id, false);
	}

	public ODataRow select(Integer id, boolean local_record) {
		String selection = (local_record) ? "local_id = ?" : "id = ?";
		List<ODataRow> records = select(selection, new Object[] { id }, null,
				null, null);
		if (records.size() > 0)
			return records.get(0);
		return null;
	}

	public List<ODataRow> select(String where, Object[] args) {
		return select(where, args, null, null, null);
	}

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

	public boolean truncate() {

		return true;
	}

	public int count() {
		return count(null, null);
	}

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

	public Integer createORReplace(OValues values) {
		List<OValues> vals = new ArrayList<OValues>();
		vals.add(values);
		return createORReplace(vals).get(0);
	}

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

	public boolean hasRecord(int id) {
		if (count("id = ? ", new Object[] { id }) > 0)
			return true;
		return false;
	}

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

	public int update(OValues values, int id) {
		return update(values, id, false);
	}

	public int update(OValues values, Integer id, Boolean local_record) {
		return update(values, (local_record) ? "local_id = ? " : "id = ?",
				new Object[] { id });
	}

	public int update(OValues updateValues, String where, Object[] whereArgs) {
		int affectedRows = 0;
		SQLiteDatabase db = getWritableDatabase();
		affectedRows = db.update(getTableName(),
				createValues(db, updateValues), getWhereClause(where),
				getWhereArgs(where, whereArgs));
		db.close();
		return affectedRows;
	}

	// createValues : used by create and update methods
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
					List<String> ids = new ArrayList<String>();
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
		args.add("true");
		return args.toArray(new String[args.size()]);
	}

	private String getWhereClause(String where) {
		String newWhereClause = (where != null) ? where + " AND " : "";
		newWhereClause += "odoo_name = ? AND is_active = ? ";
		return newWhereClause;
	}

	public OSyncHelper getSyncHelper() {
		if (mSyncHelper == null)
			mSyncHelper = new OSyncHelper(mContext, mUser, this);
		return mSyncHelper;
	}

	public List<OValues> getModelValues(String model) {
		List<String> models = new ArrayList<String>();
		models.add(model);
		return getModelValues(models);

	}

	public List<OValues> getModelValues(List<String> models) {
		if (mSyncHelper == null)
			mSyncHelper = new OSyncHelper(mContext, mUser, this);
		return mSyncHelper.modelInfo(models);
	}

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

	public boolean isEmptyTable() {
		if (count() <= 0)
			return true;
		return false;
	}
}

interface OModelHelper {
	public List<OColumn> getColumns();

	public List<OColumn> getColumns(Boolean local);

	public ODomain defaultDomain();
}
