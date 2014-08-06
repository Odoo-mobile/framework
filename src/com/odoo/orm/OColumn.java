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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The Class OColumn.
 */
public class OColumn {

	/** The Constant ROW_ID. */
	public static final String ROW_ID = "local_id";

	/** The column domains. */
	private LinkedHashMap<String, ColumnDomain> columnDomains = new LinkedHashMap<String, OColumn.ColumnDomain>();

	/** The condition_operator_index. */
	private Integer condition_operator_index = 0;

	/**
	 * The Enum RelationType.
	 */
	public enum RelationType {

		/** The One to many. */
		OneToMany,
		/** The Many to many. */
		ManyToMany,
		/** The Many to one. */
		ManyToOne
	}

	/** The name. */
	private String name = "";

	/** The label. */
	private String label = "";

	/** The size. */
	private Integer size = 0;

	/** The type. */
	private Class<?> type = null;

	/** The related_column. */
	private String related_column = null;

	/** The relation_type. */
	private RelationType relation_type = null;

	/** The default_value. */
	private Object default_value = null;

	/** The auto_increment. */
	private Boolean auto_increment = false;

	/** The parse_pattern. */
	private String parse_pattern = null;

	/** The required. */
	private Boolean required = false;

	/** The local_column. */
	private Boolean local_column = false;

	/** The is_functional_column. */
	private Boolean is_functional_column = false;

	/** The functional_method. */
	private Method functional_method = null;

	/** The use_annotation. */
	private Boolean use_annotation = true;

	/** The functional_store. */
	private Boolean functional_store = false;

	/** The functional_store_depends. */
	private String[] functional_store_depends = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("{");
		str.append("name: ");
		str.append(name);
		str.append(",label: ");
		str.append(label);
		str.append(",type: ");
		str.append(type.getName());
		str.append("}");
		return str.toString();
	}

	/**
	 * Instantiates a new o column.
	 * 
	 * @param label
	 *            the label
	 */
	public OColumn(String label) {
		super();
		this.label = label;
	}

	/**
	 * Instantiates a new o column.
	 * 
	 * @param label
	 *            the label
	 * @param class_instance
	 *            the class_instance
	 */
	public OColumn(String label, Class<?> class_instance) {
		super();
		this.label = label;
		this.type = class_instance;
	}

	/**
	 * Instantiates a new o column.
	 * 
	 * @param label
	 *            the label
	 * @param class_instance
	 *            the class_instance
	 * @param relation_type
	 *            the relation_type
	 */
	public OColumn(String label, Class<?> class_instance,
			RelationType relation_type) {
		super();
		this.label = label;
		this.type = class_instance;
		this.relation_type = relation_type;
	}

	/**
	 * Instantiates a new o column.
	 * 
	 * @param label
	 *            the label
	 * @param class_instance
	 *            the class_instance
	 * @param relation_type
	 *            the relation_type
	 * @param related_column
	 *            the related_column
	 */
	public OColumn(String label, Class<?> class_instance,
			RelationType relation_type, String related_column) {
		super();
		this.label = label;
		this.type = class_instance;
		this.relation_type = relation_type;
		this.related_column = related_column;
	}

	/**
	 * Instantiates a new o column.
	 * 
	 * @param label
	 *            the label
	 * @param class_instance
	 *            the class_instance
	 * @param size
	 *            the size
	 */
	public OColumn(String label, Class<?> class_instance, Integer size) {
		super();
		this.label = label;
		this.size = size;
		this.type = class_instance;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the label.
	 * 
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label.
	 * 
	 * @param label
	 *            the new label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Gets the size.
	 * 
	 * @return the size
	 */
	public Integer getSize() {
		return size;
	}

	/**
	 * Sets the size.
	 * 
	 * @param size
	 *            the new size
	 */
	public void setSize(Integer size) {
		this.size = size;
	}

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public Class<?> getType() {
		return type;
	}

	/**
	 * Sets the type.
	 * 
	 * @param type
	 *            the new type
	 */
	public void setType(Class<?> type) {
		this.type = type;
	}

	/**
	 * Gets the related column.
	 * 
	 * @return the related column
	 */
	public String getRelatedColumn() {
		return related_column;
	}

	/**
	 * Sets the related column.
	 * 
	 * @param related_column
	 *            the related_column
	 * @return the o column
	 */
	public OColumn setRelatedColumn(String related_column) {
		this.related_column = related_column;
		return this;
	}

	/**
	 * Gets the relation type.
	 * 
	 * @return the relation type
	 */
	public RelationType getRelationType() {
		return relation_type;
	}

	/**
	 * Sets the relation type.
	 * 
	 * @param relation_type
	 *            the new relation type
	 */
	public void setRelationType(RelationType relation_type) {
		this.relation_type = relation_type;
	}

	/**
	 * Sets the default.
	 * 
	 * @param obj
	 *            the obj
	 * @return the o column
	 */
	public OColumn setDefault(Object obj) {
		this.default_value = obj;
		return this;
	}

	/**
	 * Sets the auto increment.
	 * 
	 * @param auto_incremnet
	 *            the auto_incremnet
	 * @return the o column
	 */
	public OColumn setAutoIncrement(Boolean auto_incremnet) {
		this.auto_increment = auto_incremnet;
		return this;
	}

	/**
	 * Sets the parse patter.
	 * 
	 * @param pattern
	 *            the pattern
	 * @return the o column
	 */
	public OColumn setParsePattern(String pattern) {
		this.parse_pattern = pattern;
		return this;
	}

	/**
	 * Sets the required.
	 * 
	 * @param required
	 *            the required
	 * @return the o column
	 */
	public OColumn setRequired(Boolean required) {
		this.required = required;
		return this;
	}

	/**
	 * Sets the local column.
	 * 
	 * @return the o column
	 */
	public OColumn setLocalColumn() {
		this.local_column = true;
		return this;
	}

	/**
	 * Checks if is auto increment.
	 * 
	 * @return the boolean
	 */
	public Boolean isAutoIncrement() {
		return auto_increment;
	}

	/**
	 * Gets the parses the pattern.
	 * 
	 * @return the parses the pattern
	 */
	public String getParsePattern() {
		return parse_pattern;
	}

	/**
	 * Gets the default value.
	 * 
	 * @return the default value
	 */
	public Object getDefaultValue() {
		return default_value;
	}

	/**
	 * Checks if is required.
	 * 
	 * @return the boolean
	 */
	public Boolean isRequired() {
		return this.required;
	}

	/**
	 * Checks if is local.
	 * 
	 * @return the boolean
	 */
	public Boolean isLocal() {
		return local_column;
	}

	/**
	 * Checks if is functional column.
	 * 
	 * @return the boolean
	 */
	public Boolean isFunctionalColumn() {
		return is_functional_column;
	}

	/**
	 * Gets the method.
	 * 
	 * @return the method
	 */
	public Method getMethod() {
		return functional_method;
	}

	/**
	 * Sets the functional method.
	 * 
	 * @param method
	 *            the new functional method
	 */
	public void setFunctionalMethod(Method method) {
		functional_method = method;
		is_functional_column = true;
		setLocalColumn();
	}

	/**
	 * Sets the local column.
	 * 
	 * @param local
	 *            the new local column
	 */
	public void setLocalColumn(Boolean local) {
		local_column = local;
	}

	/**
	 * Sets the accessible.
	 * 
	 * @param accessible
	 *            the accessible
	 * @return the o column
	 */
	public OColumn setAccessible(Boolean accessible) {
		use_annotation = accessible;
		return this;
	}

	/**
	 * Checks if is accessible.
	 * 
	 * @return the boolean
	 */
	public Boolean isAccessible() {
		return use_annotation;
	}

	/**
	 * Adds the domain.
	 * 
	 * @param column_name
	 *            the column_name
	 * @param operator
	 *            the operator
	 * @param value
	 *            the value
	 * @return the o column
	 */
	public OColumn addDomain(String column_name, String operator, Object value) {
		columnDomains.put(column_name, new ColumnDomain(column_name, operator,
				value));
		return this;
	}

	/**
	 * Adds the domain.
	 * 
	 * @param condition_operator
	 *            the condition_operator
	 * @return the o column
	 */
	public OColumn addDomain(String condition_operator) {
		columnDomains.put("condition_operator_" + (condition_operator_index++)
				+ condition_operator, new ColumnDomain(condition_operator));
		return this;
	}

	/**
	 * Gets the domains.
	 * 
	 * @return the domains
	 */
	public LinkedHashMap<String, ColumnDomain> getDomains() {
		return columnDomains;
	}

	/**
	 * Clone domain.
	 * 
	 * @param domains
	 *            the domains
	 * @return the o column
	 */
	public OColumn cloneDomain(LinkedHashMap<String, ColumnDomain> domains) {
		columnDomains.putAll(domains);
		return this;
	}

	/**
	 * Sets the functional store.
	 * 
	 * @param store
	 *            the new functional store
	 */
	public void setFunctionalStore(Boolean store) {
		functional_store = store;
	}

	/**
	 * Gets the functional store.
	 * 
	 * @return the functional store
	 */
	public Boolean canFunctionalStore() {
		return functional_store;
	}

	/**
	 * Sets the functional store depends.
	 * 
	 * @param depends
	 *            the depends
	 * @return the o column
	 */
	public OColumn setFunctionalStoreDepends(String[] depends) {
		functional_store_depends = depends;
		return this;
	}

	/**
	 * Gets the functional store depends.
	 * 
	 * @return the functional store depends
	 */
	public List<String> getFunctionalStoreDepends() {
		if (functional_store_depends != null)
			return Arrays.asList(functional_store_depends);
		return new ArrayList<String>();
	}

	/**
	 * The Class ColumnDomain.
	 */
	public class ColumnDomain {

		/** The column. */
		String column = null;

		/** The operator. */
		String operator = null;

		/** The value. */
		Object value = null;

		/** The conditional_operator. */
		String conditional_operator = null;

		/**
		 * Instantiates a new column domain.
		 * 
		 * @param conditional_operator
		 *            the conditional_operator
		 */
		public ColumnDomain(String conditional_operator) {
			this.conditional_operator = conditional_operator;
		}

		/**
		 * Instantiates a new column domain.
		 * 
		 * @param column
		 *            the column
		 * @param operator
		 *            the operator
		 * @param value
		 *            the value
		 */
		public ColumnDomain(String column, String operator, Object value) {
			this.column = column;
			this.operator = operator;
			this.value = value;
		}

		/**
		 * Gets the column.
		 * 
		 * @return the column
		 */
		public String getColumn() {
			return column;
		}

		/**
		 * Sets the column.
		 * 
		 * @param column
		 *            the new column
		 */
		public void setColumn(String column) {
			this.column = column;
		}

		/**
		 * Gets the operator.
		 * 
		 * @return the operator
		 */
		public String getOperator() {
			return operator;
		}

		/**
		 * Sets the operator.
		 * 
		 * @param operator
		 *            the new operator
		 */
		public void setOperator(String operator) {
			this.operator = operator;
		}

		/**
		 * Gets the value.
		 * 
		 * @return the value
		 */
		public Object getValue() {
			return value;
		}

		/**
		 * Sets the value.
		 * 
		 * @param value
		 *            the new value
		 */
		public void setValue(Object value) {
			this.value = value;
		}

		/**
		 * Gets the conditional operator.
		 * 
		 * @return the conditional operator
		 */
		public String getConditionalOperator() {
			return conditional_operator;
		}

		/**
		 * Sets the conditional operator.
		 * 
		 * @param conditional_operator
		 *            the new conditional operator
		 */
		public void setConditionalOperator(String conditional_operator) {
			this.conditional_operator = conditional_operator;
		}

		@Override
		public String toString() {
			StringBuffer domain = new StringBuffer();
			domain.append("[");
			if (this.conditional_operator == null) {
				domain.append(this.column);
				domain.append(", ");
				domain.append(this.operator);
				domain.append(", ");
				domain.append(this.value);
			} else {
				domain.append(this.conditional_operator);
			}
			domain.append("]");
			return domain.toString();
		}
	}

}
