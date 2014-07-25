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

/**
 * The Class OColumn.
 */
public class OColumn {

	public static final String ROW_ID = "local_id";

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

	private Boolean use_annotation = true;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
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
	public OColumn setParsePatter(String pattern) {
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

}
