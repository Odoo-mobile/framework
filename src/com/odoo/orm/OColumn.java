package com.odoo.orm;

public class OColumn {

	public enum RelationType {
		OneToMany, ManyToMany, ManyToOne
	}

	private String name = "";
	private String label = "";
	private Integer size = 0;
	private Class<?> type = null;
	private String related_column = null;
	private RelationType relation_type = null;
	private Object default_value = null;
	private Boolean auto_increment = false;
	private String parse_pattern = null;
	private Boolean required = false;
	private Boolean local_column = false;

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

	public OColumn(String label) {
		super();
		this.label = label;
	}

	public OColumn(String label, Class<?> class_instance) {
		super();
		this.label = label;
		this.type = class_instance;
	}

	public OColumn(String label, Class<?> class_instance,
			RelationType relation_type) {
		super();
		this.label = label;
		this.type = class_instance;
		this.relation_type = relation_type;
	}

	public OColumn(String label, Class<?> class_instance,
			RelationType relation_type, String related_column) {
		super();
		this.label = label;
		this.type = class_instance;
		this.relation_type = relation_type;
		this.related_column = related_column;
	}

	public OColumn(String label, Class<?> class_instance, Integer size) {
		super();
		this.label = label;
		this.size = size;
		this.type = class_instance;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public String getRelatedColumn() {
		return related_column;
	}

	public OColumn setRelatedColumn(String related_column) {
		this.related_column = related_column;
		return this;
	}

	public RelationType getRelationType() {
		return relation_type;
	}

	public void setRelationType(RelationType relation_type) {
		this.relation_type = relation_type;
	}

	public OColumn setDefault(Object obj) {
		this.default_value = obj;
		return this;
	}

	public OColumn setAutoIncrement(Boolean auto_incremnet) {
		this.auto_increment = auto_incremnet;
		return this;
	}

	public OColumn setParsePatter(String pattern) {
		this.parse_pattern = pattern;
		return this;
	}

	public OColumn setRequired(Boolean required) {
		this.required = required;
		return this;
	}

	public OColumn setLocalColumn() {
		this.local_column = true;
		return this;
	}

	public Boolean isAutoIncrement() {
		return auto_increment;
	}

	public String getParsePattern() {
		return parse_pattern;
	}

	public Object getDefaultValue() {
		return default_value;
	}

	public Boolean isRequired() {
		return this.required;
	}

	public Boolean isLocal() {
		return local_column;
	}

}
