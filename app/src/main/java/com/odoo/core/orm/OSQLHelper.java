/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p/>
 * Created on 31/12/14 3:11 PM
 */
package com.odoo.core.orm;

import android.content.Context;
import android.util.Log;

import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OInteger;
import com.odoo.core.orm.fields.types.OTypeHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OSQLHelper {
    public static final String TAG = OSQLHelper.class.getSimpleName();
    private Context mContext = null;
    private List<String> mModels = new ArrayList<>();
    private HashMap<String, String> mSQLStatements = new HashMap<>();

    public OSQLHelper(Context context) {
        mContext = context;
    }

    public List<String> getModels() {
        return mModels;
    }

    public void createStatements(OModel model) {
        StringBuffer sql = null;
        if (!mModels.contains(model.getModelName())) {
            mModels.add(model.getModelName());
            sql = new StringBuffer();
            sql.append("CREATE TABLE IF NOT EXISTS ");
            sql.append(model.getTableName());
            sql.append(" (");
            List<OColumn> columns = model.getColumns();
            sql.append(generateColumnStatement(model, columns));
            sql.deleteCharAt(sql.lastIndexOf(","));
            sql.append(")");
            mSQLStatements.put(model.getTableName(), sql.toString());
        }
    }

    private String generateColumnStatement(OModel model, List<OColumn> columns) {
        StringBuffer column_statement = new StringBuffer();
        List<String> finishedColumns = new ArrayList<>();
        for (OColumn column : columns) {
            if (!finishedColumns.contains(column.getName())) {
                finishedColumns.add(column.getName());
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
            List<OColumn> m2mCols = model.getManyToManyColumns(column, relation_model);
            String table_name = column.getRelTableName() != null ? column.getRelTableName() :
                    model.getTableName() + "_" + relation_model.getTableName() + "_rel";
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
                mSQLStatements.put(table_name, sql.toString());
                Log.v(TAG, "Table Created : " + table_name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getType(OColumn column) {
        try {
            if (column.getRelationType() == null) {
                if (column.getType().getSuperclass().isAssignableFrom(OTypeHelper.class)) {
                    OTypeHelper type = (OTypeHelper) column.getType().newInstance();
                    type.setSize(column.getSize());
                    return type.getType();
                }
            } else if (column.getRelationType() == OColumn.RelationType.ManyToOne) {
                return new OInteger().getType();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public HashMap<String, String> getStatements() {
        return mSQLStatements;
    }

    public void createDropStatements(OModel model) {
        StringBuffer sql = null;
        try {
            if (!mModels.contains(model.getTableName())) {
                mModels.add(model.getTableName());
                sql = new StringBuffer();
                sql.append("DROP TABLE IF EXISTS ");
                sql.append(model.getTableName());
                mSQLStatements.put(model.getTableName(), sql.toString());
                Log.v(TAG, "Table Dropped : " + model.getTableName());
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
                                mSQLStatements.put(table_name, sql.toString());
                                Log.v(TAG, "Table Dropped : " + table_name);
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

    public List<OModel> getAllModels(List<OModel> models) {
        mModels.clear();
        List<OModel> all_models = new ArrayList<>();
        for (OModel model : models) {
            if (!mModels.contains(model.getModelName())) {
                mModels.add(model.getModelName());
                all_models.add(model);
                // Checks for relation models
                List<OModel> relModels = getRelationModels(model, model.getRelationColumns());
                all_models.addAll(relModels);
            }
        }
        mModels.clear();
        return all_models;
    }

    private List<OModel> getRelationModels(OModel model, List<OColumn> cols) {
        List<OModel> models = new ArrayList<>();
        for (OColumn col : cols) {
            OModel rel_model = model.createInstance(col.getType());
            if (rel_model != null && !mModels.contains(rel_model.getModelName())) {
                mModels.add(rel_model.getModelName());
                models.add(rel_model);
                models.addAll(getRelationModels(rel_model, rel_model.getRelationColumns()));
            }
        }
        return models;
    }
}