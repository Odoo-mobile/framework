package com.odoo.orm.sql;

public class OWhere {
	private String column = null;
	private String operator = null;
	private Object value = null;
	private String conditional_operator = null;

	public OWhere(String column, String operator, Object value) {
		this.column = column;
		this.operator = operator;
		this.value = value;
	}

	public OWhere(String column, String operator, Object value,
			String conditional_operator) {
		this.column = column;
		this.operator = operator;
		this.value = value;
		this.conditional_operator = conditional_operator;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getConditional_operator() {
		return conditional_operator;
	}

	public void setConditional_operator(String conditional_operator) {
		this.conditional_operator = conditional_operator;
	}

	@Override
	public String toString() {
		StringBuffer where = new StringBuffer();
		where.append("(");
		where.append(column);
		where.append(", ");
		where.append(operator);
		where.append(", ");
		where.append(value);
		if (conditional_operator != null) {
			where.append(conditional_operator);
			where.append(", ");
		}
		where.append(")");
		return where.toString();
	}

}