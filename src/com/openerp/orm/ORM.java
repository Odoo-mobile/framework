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
package com.openerp.orm;


/**
 * The Class ORM.
 */
public class ORM  {
//
//	/** The user_name. */
//	String user_name = "";
//	/** The modules. */
//	ArrayList<Module> modules = null;
//
//	/** The fields. */
//	ArrayList<OEColumn> fields = null;
//
//	/** The table name. */
//	String tableName = null;
//
//	/** The context. */
//	Context context = null;
//
//	/** The statements. */
//	HashMap<String, String> statements = null;
//
//	/** The oe_obj. */
//	private static OEHelper oe_obj = null;
//
//	OEUser mUser = null;
//
//	/**
//	 * Instantiates a new orm.
//	 * 
//	 * @param context
//	 *            the context
//	 */
//	public ORM(Context context) {
//		super(context);
//		this.context = context;
//		modules = new ModulesConfig().modules();
//		this.statements = new HashMap<String, String>();
//		mUser = OpenERPAccountManager.currentUser(context);
//		if (mUser != null) {
//			user_name = mUser.getAndroidName();
//		}
//		if (oe_obj == null) {
//			oe_obj = getOEInstance();
//		}
//	}
//
//	/**
//	 * Gets the oE instance.
//	 * 
//	 * @return the oE instance
//	 */
//	public OEHelper getOEInstance() {
//
//		OEHelper openerp = null;
//		try {
//			openerp = new OEHelper(context, mUser);
//		} catch (Exception e) {
//
//		}
//		return openerp;
//	}
//
//	/**
//	 * Creates the statement.
//	 * 
//	 * @param module_key
//	 *            the module_key
//	 * @return the sQL statement
//	 */
//	public SQLStatement createStatement(String module_key) {
//		Module module = this.getModule(module_key);
//		return this.createStatement(this.getDBHelperFromModule(module));
//	}
//
//	/**
//	 * Handles many2many column.
//	 * 
//	 * If many2many column contain only model name than it will create only m2m
//	 * related table. If many2many column contain BaseDBHelper object than it
//	 * will first create master table than create related table.
//	 * 
//	 * @param db
//	 *            the db
//	 * @param field
//	 *            the field
//	 */
//	public void handleMany2ManyCol(BaseDBHelper db, OEColumn field) {
//		List<OEColumn> cols = new ArrayList<OEColumn>();
//
//		// Handle many2many object
//		if (field.getType() instanceof Many2Many) {
//			Many2Many m2mobj = (Many2Many) field.getType();
//
//			if (m2mobj.isM2MObject()) {
//				BaseDBHelper newDb = null;
//				newDb = (BaseDBHelper) m2mobj.getM2mObject();
//
//				SQLStatement statement = newDb.createStatement(newDb);
//				newDb.createTable(statement);
//
//				OEColumn dField = new OEColumn(field.getName(),
//						field.getTitle(), OETypes.many2Many(newDb
//								.getModelName()));
//
//				newDb.handleMany2ManyCol(db, dField);
//			} else {
//				// handle many2many model
//				String model = m2mobj.getModel_name();
//				String rel_table = modelToTable(db.getModelName()) + "_"
//						+ modelToTable(model) + "_rel";
//				String tab1 = modelToTable(db.getModelName());
//				String tab2 = modelToTable(model);
//				String tab1_col = tab1 + "_id";
//				String tab2_col = tab2 + "_id";
//				String common_col = "oea_name";
//				cols.add(new OEColumn(tab1_col, tab1_col, OETypes.integer()));
//				cols.add(new OEColumn(tab2_col, tab2_col, OETypes.integer()));
//				cols.add(new OEColumn(common_col, "Android Name", OETypes
//						.text()));
//				SQLStatement many2ManyTable = createStatement(rel_table, cols);
//				this.createTable(many2ManyTable);
//			}
//
//		}
//	}
//
//	/**
//	 * Creates the statement.
//	 * 
//	 * @param table
//	 *            the table
//	 * @param fields
//	 *            the fields
//	 * @return the sQL statement
//	 */
//	private SQLStatement createStatement(String table, List<OEColumn> fields) {
//		SQLStatement statement = new SQLStatement();
//		StringBuffer sql = new StringBuffer();
//		sql.append("CREATE TABLE IF NOT EXISTS ");
//		sql.append(table);
//		sql.append(" (");
//		for (OEColumn field : fields) {
//			try {
//				sql.append(field.getName());
//				sql.append(" ");
//				sql.append(field.getType());
//				sql.append(", ");
//			} catch (Exception e) {
//
//			}
//		}
//		sql.deleteCharAt(sql.lastIndexOf(","));
//		sql.append(")");
//
//		statement.setTable_name(table);
//		statement.setType("create");
//		statement.setStatement(sql.toString());
//		return statement;
//	}
//
//	/**
//	 * Creates the many2 one table.
//	 * 
//	 * @param db
//	 *            the db
//	 */
//	private void createMany2OneTable(BaseDBHelper db) {
//		SQLStatement statement = db.createStatement(db);
//		db.createTable(statement);
//
//	}
//
//	/**
//	 * Creates the statement.
//	 * 
//	 * @param moduleDBHelper
//	 *            the module db helper
//	 * @return the sQL statement
//	 */
//	public SQLStatement createStatement(BaseDBHelper moduleDBHelper) {
//		this.tableName = modelToTable(moduleDBHelper.getModelName());
//		this.fields = moduleDBHelper.getColumns();
//		SQLStatement statement = new SQLStatement();
//		StringBuffer create = new StringBuffer();
//
//		create.append("CREATE TABLE IF NOT EXISTS ");
//		create.append(this.tableName);
//		create.append(" (");
//		for (OEColumn field : this.fields) {
//
//			Object type = field.getType();
//			if (field.getType() instanceof Many2Many) {
//				handleMany2ManyCol(moduleDBHelper, field);
//				continue;
//			}
//			if (field.getType() instanceof Many2One) {
//				if (((Many2One) field.getType()).isM2OObject()) {
//					BaseDBHelper m2oDb = ((Many2One) field.getType())
//							.getM2OObject();
//					createMany2OneTable(m2oDb);
//
//				}
//				type = OETypes.integer();
//
//			}
//
//			try {
//				create.append(field.getName());
//				create.append(" ");
//				create.append(type.toString());
//				create.append(", ");
//			} catch (Exception e) {
//
//			}
//		}
//		create.deleteCharAt(create.lastIndexOf(","));
//		create.append(")");
//		this.statements.put("create", create.toString());
//		statement.setTable_name(this.tableName);
//		statement.setType("create");
//		statement.setStatement(create.toString());
//		return statement;
//
//	}
//
//	/**
//	 * Gets the statement.
//	 * 
//	 * @param key
//	 *            the key
//	 * @return the statement
//	 */
//	public String getStatement(String key) {
//		if (this.statements.containsKey(key)) {
//			return this.statements.get(key).toString();
//		}
//		return null;
//	}
//
//	/**
//	 * Gets the dB helper from module.
//	 * 
//	 * @param module
//	 *            the module
//	 * @return the dB helper from module
//	 */
//	private BaseDBHelper getDBHelperFromModule(Module module) {
//		@SuppressWarnings("rawtypes")
//		Class newClass;
//		try {
//			newClass = Class.forName(module.getModuleInstance().getClass()
//					.getName());
//			if (newClass.isInstance(module.getModuleInstance())) {
//				Object receiver = newClass.newInstance();
//				@SuppressWarnings("rawtypes")
//				Class params[] = new Class[1];
//				params[0] = Context.class;
//
//				@SuppressWarnings("unchecked")
//				Method method = newClass.getDeclaredMethod("databaseHelper",
//						params);
//
//				Object obj = method.invoke(receiver, this.context);
//				return (BaseDBHelper) obj;
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return null;
//	}
//
//	/**
//	 * Gets the module.
//	 * 
//	 * @param module_key
//	 *            the module_key
//	 * @return the module
//	 */
//	private Module getModule(String module_key) {
//		for (Module module : this.modules) {
//			if (module.getKeyId().equals(module_key)) {
//				return module;
//			}
//		}
//		return null;
//	}
//
//	/**
//	 * Creates the many2many records for a column.
//	 * 
//	 * @param id
//	 *            the id
//	 * @param values
//	 *            the values
//	 * @param key
//	 *            the key
//	 * @param dbHelper
//	 *            the db helper
//	 * @param m2m
//	 *            the m2m
//	 * @param rootRow
//	 *            the root row
//	 */
//	private void createM2MRecords(String id, JSONArray values, String key,
//			BaseDBHelper dbHelper, Many2Many m2m, ContentValues rootRow) {
//		String table1 = modelToTable(dbHelper.getModelName());
//		String table2 = "";
//		BaseDBHelper tbl2Obj = null;
//		if (m2m.isM2MObject()) {
//			tbl2Obj = (BaseDBHelper) m2m.getM2mObject();
//			table2 = modelToTable(tbl2Obj.getModelName());
//		} else {
//			table2 = modelToTable(m2m.getModel_name());
//		}
//		String rel_table = table1 + "_" + table2 + "_rel";
//		String col1 = table1 + "_id";
//		String col2 = table2 + "_id";
//		String col3 = "oea_name";
//
//		// Temp dummy helper
//		BaseDBHelper newDb = generateM2MHelper(dbHelper, m2m);
//
//		int loop_val = (values.length() > 10) ? 10 : values.length();
//		List<Integer> list = new ArrayList<Integer>();
//		for (int i = 0; i < loop_val; i++) {
//			try {
//				int row_id = 0;
//
//				if (values.get(i) instanceof JSONArray) {
//					row_id = values.getJSONArray(i).getInt(0);
//				}
//				if (values.get(i) instanceof JSONObject) {
//					JSONObject obj = (JSONObject) values.get(i);
//					if (obj.has("id")) {
//						row_id = obj.getInt("id");
//					}
//				}
//				if (values.get(i) instanceof Integer) {
//					row_id = values.getInt(i);
//				}
//				ContentValues m2mvals = new ContentValues();
//				String android_name = mUser.getAndroidName();
//				m2mvals.put(col1, id);
//				m2mvals.put(col2, row_id);
//				m2mvals.put(col3, android_name);
//				int res = search(
//						newDb,
//						new String[] { col1 + " = ?", "AND", col2 + "= ?",
//								"AND", col3 + " = ?" },
//						new String[] { id, row_id + "", android_name }).size();
//				if (res == 0) {
//					SQLiteDatabase db = getWritableDatabase();
//					db.insert(rel_table, null, m2mvals);
//					db.close();
//				}
//				if (tbl2Obj != null && !tbl2Obj.hasRecord(tbl2Obj, row_id)) {
//					list.add(row_id);
//				}
//
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		if (list.size() > 0) {
//			oe_obj.syncReferenceTables(tbl2Obj, list, false);
//		}
//	}
//
//	/**
//	 * Many2one record.
//	 * 
//	 * @param array
//	 *            the array
//	 * @return the string
//	 */
//	public String many2oneRecord(JSONArray array) {
//		String id = "";
//		try {
//			id = array.getString(0).toString();
//		} catch (Exception e) {
//
//		}
//		return id;
//
//	}
//
//	/**
//	 * Creates the.
//	 * 
//	 * @param dbHelper
//	 *            the db helper
//	 * @param data_values
//	 *            the data_values
//	 * @return the int
//	 */
//	public int create(BaseDBHelper dbHelper, ContentValues data_values) {
//		int newId = 0;
//		ContentValues values = new ContentValues();
//		if (data_values.containsKey("id")) {
//			newId = data_values.getAsInteger("id");
//		} else {
//			newId = createRecordOnserver(dbHelper, data_values);
//			data_values.put("id", newId);
//		}
//
//		for (OEColumn field : dbHelper.getColumns()) {
//			values.put(field.getName(),
//					data_values.getAsString(field.getName()));
//		}
//
//		values.put("oea_name", mUser.getAndroidName());
//
//		// Handling Many2Many Records
//		HashMap<String, Object> many2manycols = dbHelper.getMany2ManyColumns();
//		for (String key : many2manycols.keySet()) {
//			try {
//				JSONArray m2mArray = new JSONArray(values.getAsString(key));
//				Many2Many m2m = (Many2Many) many2manycols.get(key);
//				createM2MRecords(values.getAsString("id"), m2mArray, key,
//						dbHelper, m2m, values);
//			} catch (Exception e) {
//			}
//			values.remove(key);
//		}
//
//		// Handling Many2One Record
//		HashMap<String, Object> many2onecols = dbHelper.getMany2OneColumns();
//		for (String key : many2onecols.keySet()) {
//			try {
//				if (!values.getAsString(key).equals("false")) {
//					JSONArray m2oArray = new JSONArray(values.getAsString(key));
//					values.put(key, many2oneRecord(m2oArray));
//					List<Integer> list = new ArrayList<Integer>();
//					int m2o_id = Integer.parseInt(many2oneRecord(m2oArray));
//					list.add(m2o_id);
//					BaseDBHelper m2oDb = ((Many2One) many2onecols.get(key))
//							.getM2OObject();
//					if (!m2oDb.hasRecord(m2oDb, m2o_id)) {
//						oe_obj.syncReferenceTables(m2oDb, list, false);
//					}
//				}
//			} catch (Exception e) {
//			}
//
//		}
//
//		SQLiteDatabase db = getWritableDatabase();
//		db.insert(modelToTable(dbHelper.getModelName()), null, values);
//		db.close();
//		return newId;
//	}
//
//	/**
//	 * Creates the record onserver.
//	 * 
//	 * @param dbHelper
//	 *            the db helper
//	 * @param values
//	 *            the values
//	 * @return the int
//	 */
//	public int createRecordOnserver(BaseDBHelper dbHelper, ContentValues values) {
//		String model = dbHelper.getModelName();
//		int newId = 0;
//		try {
//			JSONObject arguments = new JSONObject();
//			for (String key : values.keySet()) {
//				arguments.put(key, values.get(key));
//			}
//			newId = oe_obj.createNew(model, arguments).getInt("result");
//		} catch (Exception e) {
//		}
//		return newId;
//
//	}
//
//	/**
//	 * Creates the record onserver.
//	 * 
//	 * @param dbHelper
//	 *            the db helper
//	 * @param values
//	 *            the values
//	 * @return the int
//	 */
//	public int createRecordOnserver(BaseDBHelper dbHelper, JSONObject values) {
//		String model = dbHelper.getModelName();
//		int newId = 0;
//		try {
//			JSONObject arguments = values;
//			newId = oe_obj.createNew(model, arguments).getInt("result");
//		} catch (Exception e) {
//		}
//		return newId;
//
//	}
//
//	/**
//	 * Search.
//	 * 
//	 * @param db
//	 *            the db
//	 * @return the list
//	 */
//	public List<OEDataRow> search(BaseDBHelper db) {
//		return search(db, null, null, null, null, null, null, null);
//	}
//
//	/**
//	 * Search.
//	 * 
//	 * @param db
//	 *            the db
//	 * @param where
//	 *            the where
//	 * @param whereArgs
//	 *            the where args
//	 * @return the list
//	 */
//	public List<OEDataRow> search(BaseDBHelper db, String[] where,
//			String[] whereArgs) {
//		return search(db, null, where, whereArgs, null, null, null, null);
//	}
//
//	/**
//	 * Search.
//	 * 
//	 * @param db
//	 *            the db
//	 * @param columns
//	 *            the columns
//	 * @param where
//	 *            the where
//	 * @param whereArgs
//	 *            the where args
//	 * @return the list
//	 */
//	public List<OEDataRow> search(BaseDBHelper db, String[] columns,
//			String[] where, String[] whereArgs) {
//		return search(db, columns, where, whereArgs, null, null, null, null);
//	}
//
//	/**
//	 * Search.
//	 * 
//	 * @param db
//	 *            the db
//	 * @param where
//	 *            the where
//	 * @param whereArgs
//	 *            the where args
//	 * @param group_by
//	 *            the group_by
//	 * @return the list
//	 */
//	public List<OEDataRow> search(BaseDBHelper db, String[] where,
//			String[] whereArgs, String group_by) {
//		return search(db, null, where, whereArgs, group_by, null, null, null);
//	}
//
//	/**
//	 * Search.
//	 * 
//	 * @param db
//	 *            the db
//	 * @param where
//	 *            the where
//	 * @param whereArgs
//	 *            the where args
//	 * @param group_by
//	 *            the group_by
//	 * @param having
//	 *            the having
//	 * @return the list
//	 */
//	public List<OEDataRow> search(BaseDBHelper db, String[] where,
//			String[] whereArgs, String group_by, String having) {
//		return search(db, null, where, whereArgs, group_by, having, null, null);
//	}
//
//	/**
//	 * Search.
//	 * 
//	 * @param db
//	 *            the db
//	 * @param where
//	 *            the where
//	 * @param whereArgs
//	 *            the where args
//	 * @param group_by
//	 *            the group_by
//	 * @param having
//	 *            the having
//	 * @param orderby
//	 *            the orderby
//	 * @param ordertype
//	 *            the ordertype
//	 * @return the list
//	 */
//	public List<OEDataRow> search(BaseDBHelper db, String[] where,
//			String[] whereArgs, String group_by, String having, String orderby,
//			String ordertype) {
//		return search(db, null, where, whereArgs, group_by, having, orderby,
//				ordertype);
//	}
//
//	/**
//	 * Search.
//	 * 
//	 * @param db
//	 *            the db
//	 * @param columns
//	 *            the columns
//	 * @param where
//	 *            the where
//	 * @param whereArgs
//	 *            the where args
//	 * @param group_by
//	 *            the group_by
//	 * @param having
//	 *            the having
//	 * @param orderby
//	 *            the orderby
//	 * @param ordertype
//	 *            the ordertype
//	 * @return the list
//	 */
//	public List<OEDataRow> search(BaseDBHelper db, String[] columns,
//			String[] where, String[] whereArgs, String group_by, String having,
//			String orderby, String ordertype) {
//
//		List<OEDataRow> returnVals = new ArrayList<OEDataRow>();
//
//		String order_by = orderby + " " + ordertype;
//		if (orderby == null) {
//			order_by = null;
//		}
//
//		String[] finalWhere = null;
//		if (where == null) {
//			finalWhere = new String[] { "oea_name = ?" };
//		} else {
//			String[] tmpWhere = { "AND", "oea_name = ?" };
//			List<String> tmp = new ArrayList<String>();
//			tmp.addAll(Arrays.asList(where));
//			tmp.addAll(Arrays.asList(tmpWhere));
//
//			finalWhere = tmp
//					.toArray(new String[where.length + tmpWhere.length]);
//		}
//
//		String[] finalWhereArgs = null;
//		if (whereArgs == null) {
//			finalWhereArgs = new String[] { mUser.getAndroidName() };
//		} else {
//			String[] tmpWhereArg = { mUser.getAndroidName() };
//			List<String> tmp = new ArrayList<String>();
//			tmp.addAll(Arrays.asList(whereArgs));
//			tmp.addAll(Arrays.asList(tmpWhereArg));
//
//			finalWhereArgs = tmp.toArray(new String[whereArgs.length
//					+ tmpWhereArg.length]);
//		}
//		returnVals = executeQuery(db, columns, finalWhere, finalWhereArgs,
//				group_by, having, order_by);
//		return returnVals;
//	}
//
//	/**
//	 * Write.
//	 * 
//	 * @param dbHelper
//	 *            the db helper
//	 * @param values
//	 *            the values
//	 * @param id
//	 *            the id
//	 * @return true, if successful
//	 */
//	public boolean write(BaseDBHelper dbHelper, ContentValues values, int id) {
//		return write(dbHelper, values, id, false);
//	}
//
//	/**
//	 * Update m2 m records.
//	 * 
//	 * @param id
//	 *            the id
//	 * @param values
//	 *            the values
//	 * @param key
//	 *            the key
//	 * @param dbHelper
//	 *            the db helper
//	 * @param m2m
//	 *            the m2m
//	 * @param rootRow
//	 *            the root row
//	 */
//	private void updateM2MRecords(String id, JSONArray values, String key,
//			BaseDBHelper dbHelper, Many2Many m2m, ContentValues rootRow) {
//		String table1 = modelToTable(dbHelper.getModelName());
//		String table2 = "";
//		BaseDBHelper tbl2Obj = null;
//		if (m2m.isM2MObject()) {
//			tbl2Obj = (BaseDBHelper) m2m.getM2mObject();
//			table2 = modelToTable(tbl2Obj.getModelName());
//		} else {
//			table2 = modelToTable(m2m.getModel_name());
//		}
//		String rel_table = table1 + "_" + table2 + "_rel";
//		String col1 = table1 + "_id";
//		String col2 = table2 + "_id";
//		String col3 = "oea_name";
//
//		// Temp dummy helper
//		BaseDBHelper newDb = generateM2MHelper(dbHelper, m2m);
//		for (int i = 0; i < values.length(); i++) {
//			try {
//				int row_id = 0;
//
//				if (values.get(i) instanceof JSONArray) {
//					row_id = values.getJSONArray(i).getInt(0);
//				}
//				if (values.get(i) instanceof JSONObject) {
//					JSONObject obj = (JSONObject) values.get(i);
//					if (obj.has("id")) {
//						row_id = obj.getInt("id");
//					}
//				}
//				if (values.get(i) instanceof Integer) {
//					row_id = values.getInt(i);
//				}
//
//				ContentValues m2mvals = new ContentValues();
//				m2mvals.put(col1, id);
//				m2mvals.put(col2, row_id);
//				m2mvals.put(col3, user_name);
//				int res = (Integer) this.search(
//						newDb,
//						new String[] { col1 + " = ?", "AND", col2 + "= ?",
//								"AND", col3 + " = ?" },
//						new String[] { id, row_id + "", user_name }).size();
//				SQLiteDatabase db = getWritableDatabase();
//				if (res == 0) {
//					db.insert(rel_table, null, m2mvals);
//					if (tbl2Obj != null) {
//						List<Integer> list = new ArrayList<Integer>();
//						list.add(row_id);
//						oe_obj.syncReferenceTables(tbl2Obj, list, false);
//					}
//				} else {
//					db.update(rel_table, m2mvals, col1 + " = " + id + " AND "
//							+ col2 + " = " + row_id + " AND " + col3 + " = '"
//							+ user_name + "' ", null);
//				}
//				db.close();
//
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//
//	}
//
//	/**
//	 * Write.
//	 * 
//	 * @param dbHelper
//	 *            the db helper
//	 * @param values
//	 *            the values
//	 * @param id
//	 *            the id
//	 * @param fromServer
//	 *            the from server
//	 * @return true, if successful
//	 */
//	public boolean write(BaseDBHelper dbHelper, ContentValues values, int id,
//			boolean fromServer) {
//		// for update server side data.
//		JSONObject arguments = new JSONObject();
//		// Handling many2one records
//		HashMap<String, Object> many2onecols = dbHelper.getMany2OneColumns();
//		// Handling Many2Many Records
//		HashMap<String, Object> many2manycols = dbHelper.getMany2ManyColumns();
//		for (String key : many2manycols.keySet()) {
//			try {
//
//				JSONArray m2mArray = new JSONArray(values.getAsString(key));
//				Many2Many m2m = (Many2Many) many2manycols.get(key);
//				updateM2MRecords(id + "", m2mArray, key, dbHelper, m2m, values);
//
//				JSONArray m2m_ids = new JSONArray();
//				m2m_ids.put(6);
//				m2m_ids.put(false);
//				m2m_ids.put(m2mArray);
//				arguments.put(key,
//						new JSONArray("[" + m2m_ids.toString() + "]"));
//			} catch (Exception e) {
//
//			}
//
//			values.remove(key);
//		}
//		// Handling many2one records. [id, "name"] to id
//		for (String key : many2onecols.keySet()) {
//			try {
//				String tempVals = values.getAsString(key);
//				if (!tempVals.equals("false")) {
//					JSONArray m2oArray = new JSONArray(values.getAsString(key));
//					int m2oid = m2oArray.getInt(0);
//					values.put(key, m2oid);
//				} else {
//					values.put(key, "false");
//				}
//			} catch (Exception e) {
//			}
//		}
//		boolean flag = false;
//		SQLiteDatabase db = getWritableDatabase();
//		try {
//			if (OpenERPServerConnection.isNetworkAvailable(context)) {
//				String table = modelToTable(dbHelper.getModelName());
//				try {
//
//					for (String key : values.keySet()) {
//						try {
//							int keyid = Integer.parseInt(values
//									.getAsString(key));
//							arguments.put(key, keyid);
//						} catch (Exception e) {
//							String temp = values.getAsString(key);
//							if (temp.equals("true") || temp.equals("false")) {
//								arguments.put(key,
//										((temp.equals("true")) ? true : false));
//							} else {
//								arguments.put(key, values.get(key).toString());
//							}
//						}
//					}
//					if (fromServer) {
//						@SuppressWarnings("unused")
//						int res = db.update(table, values, "id = " + id, null);
//						flag = true;
//					} else {
//						if (oe_obj.updateValues(dbHelper.getModelName(),
//								arguments, id)) {
//							@SuppressWarnings("unused")
//							int res = db.update(table, values, "id = " + id,
//									null);
//							flag = true;
//						}
//					}
//
//				} catch (Exception e) {
//					e.printStackTrace();
//					flag = false;
//				}
//
//			} else {
//				Toast.makeText(context,
//						"Unable to Connect server ! Please Try again Later. ",
//						Toast.LENGTH_LONG).show();
//				flag = false;
//			}
//		} catch (Exception e) {
//		}
//		db.close();
//		return flag;
//	}
//
//	/**
//	 * Model to table.
//	 * 
//	 * @param model
//	 *            the model
//	 * @return the string
//	 */
//	public String modelToTable(String model) {
//		StringBuffer table = new StringBuffer();
//		table.append(model.replaceAll("\\.", "_"));
//		return table.toString();
//	}
//
//	/**
//	 * Execute query.
//	 * 
//	 * @param dbHelper
//	 *            the db helper
//	 * @param fetch_columns
//	 *            the fetch_columns
//	 * @param where
//	 *            the where
//	 * @param whereVals
//	 *            the where vals
//	 * @param group_by
//	 *            the group_by
//	 * @param having
//	 *            the having
//	 * @param orderby
//	 *            the orderby
//	 * @return the list
//	 */
//	private List<OEDataRow> executeQuery(BaseDBHelper dbHelper,
//			String[] fetch_columns, String[] where, String[] whereVals,
//			String group_by, String having, String orderby) {
//		SQLiteDatabase db = getWritableDatabase();
//		List<String> cols = new ArrayList<String>();
//
//		String columns[] = null;
//		if (fetch_columns != null) {
//			cols = new ArrayList<String>();
//			cols = Arrays.asList(fetch_columns);
//		} else {
//			for (OEColumn col : dbHelper.getColumns()) {
//				if (!(col.getType() instanceof Many2Many)) {
//					cols.add(col.getName());
//				}
//				if (col.getColumnDomain() != null) {
//					// Adding custom domain for model
//					List<String> newWhere = new ArrayList<String>(
//							Arrays.asList(where));
//					List<String> newWhereVals = new ArrayList<String>(
//							Arrays.asList(whereVals));
//					newWhere.add("AND");
//					newWhere.add(col.getName()
//							+ col.getColumnDomain().getOperator() + " ?");
//					newWhereVals.add(col.getColumnDomain().getValue());
//					where = newWhere.toArray(new String[newWhere.size()]);
//					whereVals = newWhereVals.toArray(new String[newWhereVals
//							.size()]);
//				}
//			}
//		}
//		columns = cols.toArray(new String[cols.size()]);
//		Cursor cursor = db.query(modelToTable(dbHelper.getModelName()),
//				columns, whereStatement(where, dbHelper), whereVals, group_by,
//				having, orderby);
//		List<OEDataRow> data = getResult(dbHelper, columns, cursor);
//		db.close();
//		return data;
//	}
//
//	/**
//	 * Execute sql.
//	 * 
//	 * @param sqlQuery
//	 *            the sql query
//	 * @param args
//	 *            the args
//	 * @return the list
//	 */
//	public List<OEDataRow> executeSQL(String sqlQuery, String[] args) {
//		SQLiteDatabase db = getWritableDatabase();
//
//		List<OEDataRow> data = new ArrayList<OEDataRow>();
//
//		Cursor cursor = db.rawQuery(sqlQuery.toString(), args);
//		String[] columns = cursor.getColumnNames();
//		if (cursor.moveToFirst()) {
//			do {
//				OEDataRow row = new OEDataRow();
//				if (cursor.getColumnIndex("oea_name") > 0) {
//					if (!cursor.getString(cursor.getColumnIndex("oea_name"))
//							.equals(user_name)) {
//						continue;
//					}
//				} else {
//					Log.e("ORM::executeSQL() - Column name missing",
//							"You must have to provide oea_name column in your sql syntax.");
//					return null;
//				}
//				for (String key : columns) {
//					row.put(key, cursor.getString(cursor.getColumnIndex(key)));
//				}
//				data.add(row);
//			} while (cursor.moveToNext());
//		}
//		db.close();
//		cursor.close();
//		return data;
//	}
//
//	/**
//	 * Execute sql query as per user requirement.
//	 * 
//	 * @param model
//	 *            the model
//	 * @param columns
//	 *            the columns
//	 * @param where
//	 *            the where
//	 * @param args
//	 *            the args query arguments
//	 * @return the cursor of results
//	 */
//	public List<HashMap<String, Object>> executeSQL(String model,
//			String[] columns, String[] where, String[] args) {
//		SQLiteDatabase db = getWritableDatabase();
//		StringBuffer sqlQuery = new StringBuffer();
//		sqlQuery.append("SELECT ");
//		sqlQuery.append(TextUtils.join(",", columns));
//		sqlQuery.append(" FROM ");
//		sqlQuery.append(modelToTable(model));
//		sqlQuery.append(" WHERE ");
//		sqlQuery.append(TextUtils.join(" ", where));
//
//		List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
//		Cursor cursor = db.rawQuery(sqlQuery.toString(), args);
//		columns = new String[cursor.getColumnCount()];
//		columns = cursor.getColumnNames();
//		if (cursor.moveToFirst()) {
//			do {
//				HashMap<String, Object> row = new HashMap<String, Object>();
//				for (String key : columns) {
//					row.put(key, cursor.getString(cursor.getColumnIndex(key)));
//				}
//				data.add(row);
//			} while (cursor.moveToNext());
//		}
//		db.close();
//		cursor.close();
//		return data;
//
//	}
//
//	/**
//	 * Gets the last id.
//	 * 
//	 * @param model_name
//	 *            the model_name
//	 * @param column
//	 *            the column
//	 * @return the last id
//	 */
//	public int getLastId(String model_name, String column) {
//		List<HashMap<String, Object>> data = executeSQL(model_name,
//				new String[] { "max(" + column + ") as id" },
//				new String[] { "oea_name = ?" },
//				new String[] { mUser.getAndroidName() });
//		Object last_id = data.get(0).get("id");
//		if (last_id == null) {
//			return 0;
//		}
//		return Integer.parseInt(last_id.toString());
//	}
//
//	/**
//	 * Count.
//	 * 
//	 * Returns number of row in local database. Zero (0) if there are no any row
//	 * related to where clause.
//	 * 
//	 * @param dbHelper
//	 *            the database helper
//	 * @param where
//	 *            the where clause conditions
//	 * @param whereArgs
//	 *            the where args
//	 * @return the int (number of row in database)
//	 */
//	public int count(BaseDBHelper dbHelper, String[] where, String[] whereArgs) {
//		SQLiteDatabase db = getWritableDatabase();
//		StringBuffer sql = new StringBuffer();
//		sql.append("SELECT count(*) as total FROM ");
//		sql.append(modelToTable(dbHelper.getModelName()));
//		sql.append(" WHERE ");
//		if (where != null && where.length > 0) {
//
//			for (String whr : where) {
//				if (whr.contains(".")) {
//					String[] datas = whr.split("\\.");
//					String table = datas[0];
//					String rel_id = table + "_id";
//					String fetch_id = modelToTable(dbHelper.getModelName())
//							+ "_id";
//					String rel_table = modelToTable(dbHelper.getModelName())
//							+ "_" + table + "_rel";
//					String subQue = "id in (SELECT " + fetch_id + " FROM "
//							+ rel_table + " WHERE " + rel_id + " = ?) ";
//					sql.append(subQue);
//					sql.append(" ");
//
//				} else {
//					sql.append(whr);
//					sql.append(" ");
//				}
//
//			}
//			sql.append(" and oea_name = '" + user_name + "'");
//		} else {
//			sql.append(" oea_name = '" + user_name + "'");
//		}
//		Cursor cursor = db.rawQuery(sql.toString(), whereArgs);
//		cursor.moveToFirst();
//		int count = cursor.getInt(0);
//		db.close();
//		cursor.close();
//		return count;
//
//	}
//
//	/**
//	 * Where statement.
//	 * 
//	 * @param where
//	 *            the where
//	 * @param db
//	 *            the db
//	 * @return the string
//	 */
//	private String whereStatement(String[] where, BaseDBHelper db) {
//		if (where == null) {
//			return null;
//		}
//		StringBuffer statement = new StringBuffer();
//		for (String whr : where) {
//			String[] colAndMark = whr.split("=");
//			if (colAndMark[0].contains(".")) {
//				String[] datas = colAndMark[0].split("\\.");
//				String table = datas[0];
//				String rel_id = table + "_id";
//				String fetch_id = modelToTable(db.getModelName()) + "_id";
//				String rel_table = modelToTable(db.getModelName()) + "_"
//						+ table + "_rel";
//				String subQue = "id in (SELECT " + fetch_id + " FROM "
//						+ rel_table + " WHERE " + rel_id + " = ?) ";
//				statement.append(subQue);
//
//			} else {
//				statement.append(whr);
//				statement.append(" ");
//			}
//		}
//		return statement.toString();
//	}
//
//	/**
//	 * Gets the result.
//	 * 
//	 * @param dbHelper
//	 *            the db helper
//	 * @param fetch_columns
//	 *            the fetch_columns
//	 * @param result
//	 *            the result
//	 * @return the result
//	 */
//	private List<OEDataRow> getResult(BaseDBHelper dbHelper,
//			String[] fetch_columns, Cursor result) {
//
//		HashMap<String, Object> m2m = dbHelper.getMany2ManyColumns();
//		HashMap<String, Object> m2o = dbHelper.getMany2OneColumns();
//		List<OEDataRow> results = new ArrayList<OEDataRow>();
//		String[] columns = result.getColumnNames();
//		if (result.moveToFirst()) {
//			OEDataRow row;
//			do {
//				row = new OEDataRow();
//				for (String col : columns) {
//					String value = result.getString(result.getColumnIndex(col));
//					row.put(col, value);
//				}
//				List<String> user_columns = null;
//				if (fetch_columns != null) {
//					user_columns = Arrays.asList(fetch_columns);
//				}
//
//				// Getting many2many ids for row
//				if (m2m.size() > 0) {
//					String id = result.getString(result.getColumnIndex("id"));
//					for (String key : m2m.keySet()) {
//						if (user_columns != null && user_columns.contains(key)) {
//							Many2Many m2mObj = (Many2Many) m2m.get(key);
//							BaseDBHelper newdb = generateM2MHelper(dbHelper,
//									m2mObj);
//							String col1 = newdb.getColumns().get(0).getName();
//							String col2 = newdb.getColumns().get(1).getName();
//							String col3 = newdb.getColumns().get(2).getName();
//							List<OEDataRow> rel_row = newdb.search(newdb,
//									new String[] { col1 + " = ?", "AND",
//											col3 + " = ?" }, new String[] { id,
//											user_name });
//							int total = rel_row.size();
//							if (total > 0) {
//								JSONArray ids_list = new JSONArray();
//								for (int i = 0; i < total; i++) {
//									JSONArray ids = new JSONArray();
//									OEDataRow rowdata = rel_row.get(i);
//									BaseDBHelper rel_obj = m2mObj
//											.getM2mObject();
//									List<OEDataRow> rel_data = rel_obj.search(
//											rel_obj,
//											new String[] { "id = ? " },
//											new String[] { rowdata.get(col2)
//													.toString() });
//									ids.put(Integer.parseInt(rowdata.get(col2)
//											.toString()));
//									if (rel_data.size() > 0) {
//										ids.put(rel_data.get(0).get("name")
//												.toString());
//									}
//									ids_list.put(ids);
//								}
//								row.put(key, ids_list);
//							}
//						}
//					}
//				}
//
//				// Getting many2one [id, name]
//				if (m2o.size() > 0) {
//					for (String key : m2o.keySet()) {
//						if (user_columns != null && user_columns.contains(key)) {
//							JSONArray ids_list = new JSONArray();
//							String ref_id = result.getString(result
//									.getColumnIndex(key));
//							if (!ref_id.equals("false")) {
//								Many2One m2oObj = (Many2One) m2o.get(key);
//								JSONArray ids = new JSONArray();
//								List<OEDataRow> rel_data = m2oObj
//										.getM2OObject().search(
//												m2oObj.getM2OObject(),
//												new String[] { "id", "name" },
//												new String[] { "id = ? " },
//												new String[] { ref_id });
//								ids.put(ref_id);
//								if (rel_data.size() > 0) {
//									ids.put(rel_data.get(0).get("name")
//											.toString());
//								}
//								ids_list.put(ids);
//							}
//							if (ids_list.length() != 0) {
//								row.put(key, ids_list);
//							} else {
//								row.put(key, false);
//							}
//						}
//					}
//				}
//				results.add(row);
//
//			} while (result.moveToNext());
//		}
//		result.close();
//		return results;
//	}
//
//	/**
//	 * Generate m2 m helper.
//	 * 
//	 * @param db
//	 *            the db
//	 * @param m2m
//	 *            the m2m
//	 * @return the base db helper
//	 */
//	private BaseDBHelper generateM2MHelper(BaseDBHelper db, Many2Many m2m) {
//		BaseDBHelper newdb = new BaseDBHelper(context);
//		newdb.columns = new ArrayList<OEColumn>();
//		String table1 = modelToTable(db.getModelName());
//		String table2 = "";
//		if (m2m.isM2MObject()) {
//			table2 = modelToTable(((BaseDBHelper) m2m.getM2mObject())
//					.getModelName());
//		} else {
//			table2 = modelToTable(m2m.getModel_name());
//		}
//		String rel_table = table1 + "_" + table2 + "_rel";
//		String col1 = table1 + "_id";
//		String col2 = table2 + "_id";
//		String col3 = "oea_name";
//		newdb.name = rel_table.replaceAll("_", ".");
//		newdb.columns.add(new OEColumn(col1, col1, OETypes.integer()));
//		newdb.columns.add(new OEColumn(col2, col2, OETypes.integer()));
//		newdb.columns.add(new OEColumn(col3, col3, OETypes.text()));
//		return newdb;
//	}
//
//	/**
//	 * Column list to string array.
//	 * 
//	 * @param cols
//	 *            the cols
//	 * @return the string[]
//	 */
//	public String[] columnListToStringArray(List<OEColumn> cols) {
//		String[] columns = new String[cols.size()];
//		int i = 0;
//		for (OEColumn col : cols) {
//			columns[i] = col.getName();
//			i++;
//		}
//		return columns;
//	}
//
//	/**
//	 * Checks for record.
//	 * 
//	 * @param db
//	 *            the db
//	 * @param id
//	 *            the id
//	 * @return true, if successful
//	 */
//	public boolean hasRecord(BaseDBHelper db, int id) {
//		SQLiteDatabase dbHelper = getWritableDatabase();
//		String where = " id = " + id + " AND oea_name = '" + user_name + "'";
//		Cursor cursor = dbHelper.query(modelToTable(db.getModelName()),
//				new String[] { "*" }, where, null, null, null, null);
//		boolean flag = false;
//		if (cursor.moveToFirst()) {
//			flag = true;
//		}
//		cursor.close();
//		dbHelper.close();
//		return flag;
//	}
//
//	/**
//	 * Returns all ids of model database helper from local database.
//	 * 
//	 * @param db
//	 *            : instance of database helper
//	 * @return int[] : list of integer array of local database ids for model
//	 */
//	public int[] localIds(BaseDBHelper db) {
//		String table = modelToTable(db.getModelName());
//		String sql = "SELECT id, oea_name FROM " + table
//				+ " WHERE oea_name = ?";
//		List<OEDataRow> records = executeSQL(sql, new String[] { user_name });
//		int[] ids = new int[records.size()];
//		int i = 0;
//		for (OEDataRow row : records) {
//			ids[i] = row.getInt("id");
//			i++;
//		}
//		return ids;
//
//	}
//
//	/**
//	 * Delete.
//	 * 
//	 * @param db
//	 *            the db
//	 * @param id
//	 *            the id
//	 * @param fromLocal
//	 *            the from local
//	 * @return true, if successful
//	 */
//	public boolean delete(BaseDBHelper db, int id, boolean fromLocal) {
//		return delete(db, "id", id, fromLocal);
//	}
//
//	public boolean delete(BaseDBHelper db, String column, int id,
//			boolean fromLocal) {
//		try {
//			if (!fromLocal) {
//
//				if (oe_obj.unlink(db.getModelName(), id)) {
//					SQLiteDatabase sdb = getWritableDatabase();
//					String where = column + " = " + id;
//					sdb.delete(modelToTable(db.getModelName()), where, null);
//					sdb.close();
//					return true;
//				}
//			} else {
//				SQLiteDatabase sdb = getWritableDatabase();
//				String where = column + " = " + id;
//				sdb.delete(modelToTable(db.getModelName()), where, null);
//				sdb.close();
//				return true;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return false;
//	}
//
//	/**
//	 * Checks if is empty table.
//	 * 
//	 * @param db
//	 *            the db
//	 * @return true, if is empty table
//	 */
//	public boolean isEmptyTable(BaseDBHelper db) {
//		boolean flag = true;
//		if (count(db, null, null) > 0) {
//			flag = false;
//		}
//		return flag;
//	}
//
//	/**
//	 * Delete.
//	 * 
//	 * @param db
//	 *            the db
//	 * @param id
//	 *            the id
//	 * @return true, if successful
//	 */
//	public boolean delete(BaseDBHelper db, int id) {
//		return delete(db, id, false);
//
//	}
//
//	/**
//	 * Database tables.
//	 * 
//	 * @return the string[]
//	 */
//	public String[] databaseTables() {
//		String[] tables_list = null;
//		List<String> tables = new ArrayList<String>();
//		SQLiteDatabase db = getWritableDatabase();
//		Cursor cursor = db.rawQuery(
//				"SELECT * FROM sqlite_master WHERE type='table';", null);
//		cursor.moveToFirst();
//		while (!cursor.isAfterLast()) {
//			String tableName = cursor.getString(1);
//			if (!tableName.equals("android_metadata")
//					&& !tableName.equals("sqlite_sequence"))
//				tables.add(tableName);
//			cursor.moveToNext();
//		}
//		cursor.close();
//		tables_list = tables.toArray(new String[tables.size()]);
//		return tables_list;
//	}
//
//	/**
//	 * Clean user records.
//	 * 
//	 * @param user_name
//	 *            the user_name
//	 * @return true, if successful
//	 */
//	public boolean cleanUserRecords(String user_name) {
//		SQLiteDatabase db = getWritableDatabase();
//		for (String table : databaseTables()) {
//			String sql = "DELETE FROM " + table + " where oea_name = '"
//					+ user_name + "'";
//			db.execSQL(sql);
//		}
//		db.close();
//		return true;
//
//	}
}
