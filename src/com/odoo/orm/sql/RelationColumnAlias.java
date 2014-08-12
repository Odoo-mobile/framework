package com.odoo.orm.sql;

import com.odoo.orm.OModel;
import com.odoo.orm.OColumn.RelationType;

class RelationColumnAlias {
	private String table = null;
	private String table_alias = null;
	private String columnName = null;
	private String relationColumnName = null;
	private OModel relationModel = null;
	private RelationType relationType = null;

	public RelationColumnAlias(String table, String table_alias,
			String columnName, String relationColumnName, OModel relationModel,
			RelationType relationType) {
		this.table = table;
		this.table_alias = table_alias;
		this.columnName = columnName;
		this.relationColumnName = relationColumnName;
		this.relationModel = relationModel;
		this.relationType = relationType;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getTable_alias() {
		return table_alias;
	}

	public void setTable_alias(String table_alias) {
		this.table_alias = table_alias;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getRelationColumnName() {
		return relationColumnName;
	}

	public void setRelationColumnName(String relationColumnName) {
		this.relationColumnName = relationColumnName;
	}

	public OModel getRelationModel() {
		return relationModel;
	}

	public void setRelationModel(OModel relationModel) {
		this.relationModel = relationModel;
	}

	public RelationType getRelationType() {
		return relationType;
	}

	public void setRelationType(RelationType relationType) {
		this.relationType = relationType;
	}

}
