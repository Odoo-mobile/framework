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
import java.util.List;

import android.util.Log;

import com.openerp.orm.types.OEManyToMany;
import com.openerp.orm.types.OEManyToOne;
import com.openerp.orm.types.OEOneToMany;
import com.openerp.orm.types.OETypeHelper;

public class SQLHelper {

	public static final String TAG = SQLHelper.class.getSimpleName();

	public List<String> createTable(OEDBHelper db) {
		return createTable(db, false);
	}

	public List<String> createTable(OEDBHelper db, boolean isOneToMany) {
		Log.d(TAG, "SQLHelper->createTable()");
		List<String> queries = new ArrayList<String>();
		StringBuffer sql = new StringBuffer();
		sql.append("CREATE TABLE IF NOT EXISTS ");
		sql.append(modelToTable(db.getModelName()));
		sql.append(" (");
		for (OEColumn col : db.getModelColumns()) {
			if (col.getName().equals("id") || col.getName().equals("oea_name")) {
				continue;
			}
			if (col.getType() instanceof OETypeHelper) {
				sql.append(col.getName());
				sql.append(" ");
				sql.append(((OETypeHelper) col.getType()).getType());
				sql.append(", ");
			}
			if (col.getType() instanceof OEManyToOne) {
				if (!isOneToMany) {
					OEManyToOne manyToOne = (OEManyToOne) col.getType();
					List<String> many2one = createTable(manyToOne.getDBHelper());
					for (String query : many2one) {
						queries.add(query);
					}
				}
				sql.append(col.getName());
				sql.append(" ");
				sql.append(((OETypeHelper) OEFields.integer()).getType());
				sql.append(", ");

			}
			if (col.getType() instanceof OEOneToMany) {
				OEOneToMany oneToMany = (OEOneToMany) col.getType();
				List<String> one2many = createTable(oneToMany.getDBHelper(),
						true);
				for (String query : one2many) {
					queries.add(query);
				}
				continue;
			}
			if (col.getType() instanceof OEManyToMany) {
				OEManyToMany manyTomany = (OEManyToMany) col.getType();
				List<String> many2many = createTable(manyTomany.getDBHelper());
				for (String query : many2many) {
					queries.add(query);
					queries.add(createMany2ManyRel(db.getModelName(),
							manyTomany.getDBHelper().getModelName()));
				}
			}
		}
		sql.append(defaultColumns());
		sql.deleteCharAt(sql.lastIndexOf(","));
		sql.append(");");
		queries.add(sql.toString());

		String table = modelToTable(db.getModelName());
		Log.d("SQLHelper", "Table created : " + table);

		return queries;
	}

	public String createMany2ManyRel(String model_first, String model_second) {
		String column_first = modelToTable(model_first);
		String column_second = modelToTable(model_second);
		String rel_table = column_first + "_" + column_second + "_rel";
		StringBuffer sql = new StringBuffer();
		sql.append("CREATE TABLE IF NOT EXISTS ");
		sql.append(rel_table);
		sql.append("(");
		sql.append(column_first + "_id ");
		sql.append(((OETypeHelper) OEFields.integer()).getType());
		sql.append(", ");
		sql.append(column_second + "_id ");
		sql.append(((OETypeHelper) OEFields.integer()).getType());
		sql.append(defaultRelColumns());
		sql.append(");");
		Log.d("SQLHelper", "Table created : " + rel_table);
		return sql.toString();
	}

	public String dropMany2ManyRel(String model_first, String model_second) {
		String column_first = modelToTable(model_first);
		String column_second = modelToTable(model_second);
		String rel_table = column_first + "_" + column_second + "_rel";
		StringBuffer sql = new StringBuffer();
		sql.append("DROP TABLE IF EXISTS ");
		sql.append(rel_table + ";");
		Log.d("SQLHelper", "Table dropped : " + rel_table);
		return sql.toString();
	}

	public List<String> dropTable(OEDBHelper db) {
		List<String> queries = new ArrayList<String>();
		StringBuffer sql = new StringBuffer();
		String table = modelToTable(db.getModelName());
		sql.append("DROP TABLE IF EXISTS " + table + ";");
		Log.d("SQHelper", "Table droped : " + table);
		queries.add(sql.toString());
		for (OEColumn col : db.getModelColumns()) {
			if (col.getType() instanceof OEManyToMany) {
				OEManyToMany m2mDb = (OEManyToMany) col.getType();
				for (String que : dropTable(m2mDb.getDBHelper())) {
					queries.add(que);
				}
				queries.add(dropMany2ManyRel(table, modelToTable(m2mDb
						.getDBHelper().getModelName())));
			}
			if (col.getType() instanceof OEManyToOne) {
				sql = new StringBuffer();
				OEManyToOne m2oDb = (OEManyToOne) col.getType();
				for (String que : dropTable(m2oDb.getDBHelper())) {
					queries.add(que);
				}
			}
		}
		return queries;
	}

	private String defaultColumns() {
		StringBuffer defaultCols = new StringBuffer();
		defaultCols.append("id ");
		defaultCols.append(((OETypeHelper) OEFields.integer()).getType());
		defaultCols.append(", ");
		defaultCols.append("oea_name ");
		defaultCols.append(((OETypeHelper) OEFields.varchar(50)).getType());
		defaultCols.append(", ");
		return defaultCols.toString();
	}

	private String defaultRelColumns() {
		StringBuffer defaultCols = new StringBuffer();
		defaultCols.append(", ");
		defaultCols.append("oea_name ");
		defaultCols.append(((OETypeHelper) OEFields.varchar(50)).getType());
		return defaultCols.toString();
	}

	public String modelToTable(String model) {
		return model.replaceAll("\\.", "_");
	}
}
