package com.odoo.orm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.odoo.orm.OColumn.RelationType;
import com.odoo.orm.types.OBlob;
import com.odoo.orm.types.OBoolean;
import com.odoo.orm.types.ODateTime;
import com.odoo.orm.types.OHtml;
import com.odoo.orm.types.OInteger;
import com.odoo.orm.types.OReal;
import com.odoo.orm.types.OText;
import com.odoo.orm.types.OTimestamp;
import com.odoo.orm.types.OVarchar;

public class OSQLHelper {

	public static final String TAG = OSQLHelper.class.getSimpleName();
	Context mContext = null;
	List<String> mModels = new ArrayList<String>();
	List<String> mSQLStatements = new ArrayList<String>();
	HashMap<String, String> mModelClassPaths = new HashMap<String, String>();

	public OSQLHelper(Context context) {
		mContext = context;
	}

	public List<String> getModelName() {
		return mModels;
	}

	public void createStatements(OModel model) {
		StringBuffer sql = null;
		if (!mModels.contains(model.getModelName())) {
			mModels.add(model.getModelName());
			mModelClassPaths.put(model.getModelName(), model.getClass()
					.getName());
			sql = new StringBuffer();
			sql.append("CREATE TABLE IF NOT EXISTS ");
			sql.append(model.getTableName());
			sql.append(" (");

			List<OColumn> columns = model.getColumns();
			sql.append(generateColumnStatement(model, columns));
			sql.deleteCharAt(sql.lastIndexOf(","));
			sql.append(")");
			Log.v(TAG, "Table Created : " + model.getTableName());
			mSQLStatements.add(sql.toString());
		}
	}

	private String generateColumnStatement(OModel model, List<OColumn> columns) {
		StringBuffer column_statement = new StringBuffer();
		for (OColumn column : columns) {
			String type = getType(column);
			if (type != null) {
				column_statement.append(column.getName());
				column_statement.append(" " + type + " ");
				if (column.isAutoIncrement()) {
					column_statement.append(" PRIMARY KEY ");
					column_statement.append(" AUTOINCREMENT ");
				}
				Object default_value = column.getDefaultValue();
				if (default_value != null) {
					column_statement.append(" DEFAULT ");
					if (default_value instanceof String) {
						column_statement.append("'" + default_value + "'");
					} else {
						column_statement.append(default_value);
					}
				}
				column_statement.append(", ");
			}
			if (column.getRelationType() != null) {
				createRelationTable(model, column);
			}
		}
		return column_statement.toString();
	}

	private void createRelationTable(OModel base_model, OColumn column) {
		try {
			OModel rel_model = base_model.createInstance(column.getType());
			switch (column.getRelationType()) {
			case ManyToOne:
			case OneToMany:
				createStatements(rel_model);
				break;
			case ManyToMany:
				manyToManyTable(column, base_model);
				// Creating master table for related column
				createStatements(base_model.createInstance(column.getType()));
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void manyToManyTable(OColumn column, OModel model) {
		StringBuffer sql = null;
		try {
			OModel relation_model = model.createInstance(column.getType());
			List<OColumn> m2mCols = model.getManyToManyColumns(relation_model);
			String table_name = model.getTableName() + "_"
					+ relation_model.getTableName() + "_rel";
			if (!mModels.contains(table_name)) {
				sql = new StringBuffer();
				mModels.add(table_name);
				String col_statement = generateColumnStatement(model, m2mCols);
				sql.append("CREATE TABLE IF NOT EXISTS ");
				sql.append(table_name);
				sql.append(" (");
				sql.append(col_statement);
				sql.deleteCharAt(sql.lastIndexOf(","));
				sql.append(")");
				mSQLStatements.add(sql.toString());
				Log.v(TAG, "Table Created : " + table_name);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// FIXME : need to reduce code
	private String getType(OColumn column) {
		try {
			Class<?> type_class = column.getType();
			// Varchar
			if (type_class.isAssignableFrom(OVarchar.class)) {
				OVarchar varchar = new OVarchar(column.getSize());
				return varchar.getType();
			}

			// Integer
			if (type_class.isAssignableFrom(OInteger.class)) {
				OInteger integer = new OInteger(column.getSize());
				return integer.getType();
			}

			// boolean
			if (type_class.isAssignableFrom(OBoolean.class)) {
				OBoolean tBoolean = new OBoolean();
				return tBoolean.getType();
			}

			// Blob
			if (type_class.isAssignableFrom(OBlob.class)) {
				OBlob blob = new OBlob();
				return blob.getType();
			}
			// DateTime
			if (type_class.isAssignableFrom(ODateTime.class)) {
				ODateTime datetime = new ODateTime(column.getParsePattern());
				return datetime.getType();
			}
			// Real
			if (type_class.isAssignableFrom(OReal.class)) {
				OReal real = new OReal(column.getSize());
				return real.getType();
			}
			// Text
			if (type_class.isAssignableFrom(OText.class)) {
				return new OText().getType();
			}

			// Text
			if (type_class.isAssignableFrom(OHtml.class)) {
				return new OHtml().getType();
			}
			// TimeStamp
			if (type_class.isAssignableFrom(OTimestamp.class)) {
				OTimestamp timestamp = new OTimestamp(column.getParsePattern());
				return timestamp.getType();
			}
			// ManyToOne
			if (column.getRelationType() != null
					&& column.getRelationType() == RelationType.ManyToOne) {
				OInteger integer = new OInteger();
				return integer.getType();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void createDropStatements(OModel model) {
		StringBuffer sql = null;
		try {
			if (!mModels.contains(model.getTableName())) {
				mModels.add(model.getTableName());
				sql = new StringBuffer();
				sql.append("DROP TABLE IF EXISTS ");
				sql.append(model.getTableName());
				mSQLStatements.add(sql.toString());
				Log.v(TAG, "Table Droped : " + model.getTableName());
				for (OColumn col : model.getColumns()) {
					if (col.getRelationType() != null) {
						switch (col.getRelationType()) {
						case ManyToMany:
							OModel rel = model.createInstance(col.getType());
							String table_name = model.getTableName() + "_"
									+ rel.getTableName() + "_rel";
							sql = new StringBuffer();
							sql.append("DROP TABLE IF EXISTS ");
							sql.append(table_name);
							mModels.add(table_name);
							mSQLStatements.add(sql.toString());
							Log.v(TAG, "Table Droped : " + table_name);
							break;
						case ManyToOne:
						case OneToMany:
							createDropStatements(model.createInstance(col
									.getType()));
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<String> getStatements() {
		return mSQLStatements;
	}

	public HashMap<String, String> getModelClassPath() {
		return mModelClassPaths;
	}

}
