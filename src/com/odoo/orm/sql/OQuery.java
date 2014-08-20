package com.odoo.orm.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;

import com.odoo.orm.OColumn;
import com.odoo.orm.OColumn.RelationType;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OModel;

/**
 * The Class OQuery. SQL Statement generator
 */
public class OQuery {

	/** The Constant TAG. */
	public static final String TAG = OQuery.class.getSimpleName();

	/** The model. */
	private OModel model = null;

	/** The type. */
	private QueryType type = null;

	/** The context. */
	private Context mContext = null;

	/** The sql dump. */
	private StringBuffer sqlDump = null;

	/** The wheres. */
	private List<OWhere> wheres = new ArrayList<OWhere>();

	/** The columns. */
	private List<String> columns = new ArrayList<String>();

	/** The relation columns. */
	private HashMap<String, RelationColumnAlias> relationColumns = new HashMap<String, RelationColumnAlias>();

	/** The m offset. */
	private Integer mLimit = 0, mOffset = 0;

	/** The order. */
	private String orderBy = null, order = "";

	/**
	 * The Enum QueryType.
	 */
	public enum QueryType {

		/** The Insert. */
		Insert,
		/** The Select. */
		Select,
		/** The Update. */
		Update,
		/** The Delete. */
		Delete
	}

	/**
	 * Instantiates a new o query.
	 * 
	 * @param context
	 *            the context
	 * @param model
	 *            the model
	 * @param type
	 *            the type
	 */
	public OQuery(Context context, OModel model, QueryType type) {
		mContext = context;
		this.model = model;
		this.type = type;
	}

	/**
	 * Columns.
	 * 
	 * @param columns
	 *            the columns
	 * @return the o query
	 */
	public OQuery columns(String... columns) {
		this.columns.clear();
		if (model.getColumn(OColumn.ROW_ID) != null)
			this.columns.add(OColumn.ROW_ID);
		if (model.getColumn("id") != null)
			this.columns.add("id");
		for (String col : columns) {
			if (col.contains(".")) {
				createRelColumn(col);
			}
			this.columns.add(col);
		}
		return this;
	}

	/**
	 * Creates the rel column.
	 * 
	 * @param column
	 *            the column
	 * @param fromColumns
	 *            the from columns
	 */
	private void createRelColumn(String column) {
		String[] parts = column.split("\\.");
		OColumn col = model.getColumn(parts[0]);
		OModel rel_model = model.createInstance(col.getType());
		String table_alias = createAlias(rel_model);
		String table_name = rel_model.getTableName();
		if (col.getRelationType() == RelationType.ManyToMany) {
			table_name = model.getTableName() + "_" + rel_model.getTableName()
					+ "_rel";
			table_alias = table_name + "_alias";
			OModel rel_base_model = model.createInstance(col.getType());
			String rel_base_alias = createAlias(rel_base_model);
			relationColumns.put(column + ".base", new RelationColumnAlias(
					rel_base_model.getTableName(), rel_base_alias,
					rel_base_model.getTableName() + "_id", parts[1],
					rel_base_model, null));
		} else {
			if (rel_model.getTableName().equals(model.getTableName()))
				table_alias += "_" + parts[1];
		}
		relationColumns.put(column,
				new RelationColumnAlias(table_name, table_alias, parts[1],
						parts[0], rel_model, col.getRelationType()));
	}

	/**
	 * Adds the where.
	 * 
	 * @param column
	 *            the column
	 * @param operator
	 *            the operator
	 * @param value
	 *            the value
	 * @return the o query
	 */
	public OQuery addWhere(String column, String operator, Object value) {
		addWhere(column, operator, value, null);
		return this;
	}

	/**
	 * Adds the where.
	 * 
	 * @param column
	 *            the column
	 * @param operator
	 *            the operator
	 * @param value
	 *            the value
	 * @param conditional_operator
	 *            the conditional_operator
	 * @return the o query
	 */
	public OQuery addWhere(String column, String operator, Object value,
			String conditional_operator) {
		if (column.contains(".")) {
			createRelColumn(column);
		}
		wheres.add(new OWhere(column, operator, value, conditional_operator));
		return this;
	}

	/**
	 * Creates the statement.
	 */
	private void createStatement() {
		sqlDump = new StringBuffer();
		sqlDump.append(queryType());
		sqlDump.append(" ");

		if (type == QueryType.Select) {
			sqlDump.append(queryColumns());
			sqlDump.append(" ");
		}
		sqlDump.append(tableNames());
		sqlDump.append(getWhereClauses());

		if (orderBy != null) {
			sqlDump.append(" ");
			sqlDump.append("ORDER BY ");
			if (isJoin())
				sqlDump.append(createAlias(model) + "." + orderBy);
			else
				sqlDump.append(orderBy);
			sqlDump.append(" ");
			sqlDump.append(order);
		}

		if (mLimit > 0) {
			sqlDump.append(" ");
			sqlDump.append("LIMIT ");
			sqlDump.append(mOffset);
			sqlDump.append(", ");
			sqlDump.append(mLimit);
		}
	}

	/**
	 * Query type.
	 * 
	 * @return the string
	 */
	private String queryType() {
		switch (type) {
		case Insert:
			return "INSERT INTO";
		case Select:
			return "SELECT";
		case Update:
			return "UPDATE";
		case Delete:
			return "DELETE FROM";
		}
		return null;
	}

	/**
	 * Query columns.
	 * 
	 * @return the string
	 */
	private String queryColumns() {
		StringBuffer cols = new StringBuffer();
		String base_alias = createAlias(model);
		if (columns.size() == 0) {
			if (isJoin()) {
				for (OColumn c : model.getColumns()) {
					cols.append(base_alias);
					cols.append(".");
					cols.append(c.getName());
					cols.append(", ");
				}
				cols.deleteCharAt(cols.lastIndexOf(", "));
			} else {
				cols.append("*");
			}
		} else {
			for (String column : columns) {
				if (isJoin()) {
					if (column.contains(".")) {
						RelationColumnAlias col = relationColumns.get(column);
						String rel_alias = null;
						String column_name = col.getColumnName();
						if (col.getRelationType() == RelationType.ManyToMany) {
							col = relationColumns.get(column + ".base");
							column_name = col.getRelationColumnName();
						}
						rel_alias = col.getTable_alias();
						if (base_alias.equals(col.getTable_alias())) {
							rel_alias += "_" + col.getColumnName();
						}
						cols.append(rel_alias);
						cols.append(".");
						cols.append(column_name);
						cols.append(" AS ");
						cols.append(column.replaceAll("\\.", "_"));
					} else {
						if (column.equals("*")) {
							for (OColumn c : model.getColumns()) {
								if (c.getRelationType() == null) {
									cols.append(base_alias);
									cols.append(".");
									cols.append(c.getName());
									cols.append(", ");
								}
							}
							cols.deleteCharAt(cols.lastIndexOf(", "));
						} else {
							cols.append(base_alias);
							cols.append(".");
							cols.append(column);
							cols.append(" AS ");
							cols.append(column);
						}
					}
					cols.append(", ");
				} else {
					cols.append(column);
					cols.append(", ");
				}
			}
		}
		if (cols.lastIndexOf(", ") > 0)
			cols.deleteCharAt(cols.lastIndexOf(", "));
		return cols.toString();
	}

	/**
	 * Checks if query required join with multiple tables.
	 * 
	 * @return true, if there is any relation column
	 */
	private boolean isJoin() {
		return (relationColumns.size() > 0);
	}

	/**
	 * Table names.
	 * 
	 * @return the string
	 */
	private String tableNames() {
		List<String> mAsLists = new ArrayList<String>();
		switch (type) {
		case Insert:
		case Select:
			if (!isJoin())
				return "FROM " + model.getTableName();
			else {
				String alias = createAlias(model);
				StringBuffer tables = new StringBuffer();
				tables.append("FROM ");
				tables.append(model.getTableName());
				tables.append(" AS ");
				tables.append(alias);
				tables.append(", ");
				for (String col_name : relationColumns.keySet()) {
					RelationColumnAlias col = relationColumns.get(col_name);
					if (!mAsLists.contains(col.getTable_alias())) {
						tables.append(col.getTable());
						tables.append(" AS ");
						tables.append(col.getTable_alias());
						tables.append(", ");
						mAsLists.add(col.getTable_alias());
					}
				}
				tables.deleteCharAt(tables.lastIndexOf(", "));
				return tables.toString();
			}
		case Update:
		case Delete:
		}
		return null;
	}

	/**
	 * Creates the alias.
	 * 
	 * @param model
	 *            the model
	 * @return the string
	 */
	private String createAlias(OModel model) {
		return model.getTableName() + "_alias";
	}

	/**
	 * Gets the where clauses.
	 * 
	 * @return the where clauses
	 */
	private String getWhereClauses() {
		StringBuffer clause = new StringBuffer();
		Boolean join = isJoin();
		String base_alias = createAlias(model);
		if (wheres.size() > 0 || join) {
			clause.append(" WHERE ");
			// Creating referenced table clauses
			for (String col : relationColumns.keySet()) {
				RelationColumnAlias alias = relationColumns.get(col);
				if (alias.getRelationType() != null) {
					String alias_name = base_alias;
					if (alias.getRelationType() == RelationType.ManyToMany) {
						alias = relationColumns.get(col + ".base");

						alias_name = model.getTableName() + "_"
								+ alias.getRelationModel().getTableName()
								+ "_rel_alias";
						// Base model key check with many2many
						clause.append(base_alias);
						clause.append(".");
						clause.append(OColumn.ROW_ID);
						clause.append(" = ");
						clause.append(alias_name);
						clause.append(".");
						clause.append(model.getTableName() + "_id");
						clause.append(" AND ");

						// related model key check wity many2many
						clause.append(alias_name);
						clause.append(".");
						clause.append(alias.getColumnName());
						clause.append(" = ");
						clause.append(alias.getTable_alias());
						clause.append(".");
						clause.append(OColumn.ROW_ID);

					} else {
						clause.append(alias_name);
						clause.append(".");
						clause.append(alias.getRelationColumnName());
						clause.append(" = ");
						clause.append(alias.getTable_alias());
						clause.append(".");
						clause.append(OColumn.ROW_ID);
					}
					clause.append(" AND ");

				}
			}

			for (OWhere where : wheres) {
				if (join) {
					String table_alias = base_alias;
					String column = null;
					if (where.getColumn().contains(".")) {
						RelationColumnAlias alias = relationColumns.get(where
								.getColumn());
						table_alias = alias.getTable_alias();
						column = alias.getColumnName();
					} else {
						column = where.getColumn();
					}
					clause.append(table_alias);
					clause.append(".");
					clause.append(column);
				} else
					clause.append(where.getColumn());
				clause.append(" ");
				clause.append(where.getOperator());
				clause.append(" ");
				if (where.getValue() instanceof String
						|| where.getValue() instanceof Boolean)
					clause.append("'" + where.getValue() + "'");
				else
					clause.append(where.getValue());
				clause.append(" AND ");
			}

			clause.delete(clause.length() - 4, clause.length());
		}
		return clause.toString();
	}

	/**
	 * Gets the query.
	 * 
	 * @return the query
	 */
	public String getQuery() {
		createStatement();
		return sqlDump.toString();
	}

	/**
	 * Fetch.
	 * 
	 * @return the list
	 */
	public List<ODataRow> fetch() {
		return model.query(getQuery(), null);
	}

	/**
	 * Sets the offset.
	 * 
	 * @param offset
	 *            the offset
	 * @return the o query
	 */
	public OQuery setOffset(Integer offset) {
		mOffset = offset;
		return this;
	}

	/**
	 * Sets the limit.
	 * 
	 * @param limit
	 *            the limit
	 * @return the o query
	 */
	public OQuery setLimit(Integer limit) {
		mLimit = limit;
		return this;
	}

	/**
	 * Sets the order.
	 * 
	 * @param column
	 *            the column
	 * @return the o query
	 */
	public OQuery setOrder(String column) {
		orderBy = column;
		return this;
	}

	/**
	 * Sets the order.
	 * 
	 * @param column
	 *            the column
	 * @param order
	 *            the order
	 * @return the o query
	 */
	public OQuery setOrder(String column, String order) {
		orderBy = column;
		this.order = order;
		return this;
	}

	/**
	 * Gets the next offset.
	 * 
	 * @return the next offset
	 */
	public Integer getNextOffset() {
		return mOffset + mLimit;
	}

	/**
	 * With functional columns.
	 * 
	 * @param withFunctionalColumns
	 *            the with functional columns
	 * @return the o query
	 */
	public OQuery withFunctionalColumns(Boolean withFunctionalColumns) {
		model.withFunctionalColumns(withFunctionalColumns);
		return this;
	}
}
