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
 * Created on 31/12/14 11:20 AM
 */
package com.odoo.core.orm.fields;

import com.odoo.core.orm.OModel;
import com.odoo.core.orm.annotation.Odoo;
import com.odoo.core.orm.fields.utils.DomainFilterParser;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class OColumn {
    public static final String TAG = OColumn.class.getSimpleName();
    public static final String ROW_ID = "_id";
    private LinkedHashMap<String, String> mSelectionMap = new LinkedHashMap<>();
    private Odoo.Domain domainFilter;

    public static enum RelationType {
        OneToMany,
        ManyToOne,
        ManyToMany
    }

    private String name, label, related_column;
    private Integer size;
    private Class<?> type;
    private RelationType relationType;
    private Object defaultValue;
    private Boolean autoIncrement = false, required = false;
    private Boolean isLocalColumn = false;
    private LinkedHashMap<String, ColumnDomain> columnDomains = new LinkedHashMap<>();
    private Integer condition_operator_index = 0;
    private Integer recordSyncLimit = 0;

    //Annotation properties
    private Method mOnChangeMethod = null;
    private Boolean mOnChangeBGProcess = false;
    private Boolean mHasDomainFilterColumn = false;
    private Boolean is_functional_column = false;
    private Method functional_method = null;
    private Boolean use_annotation = true;
    private Boolean functional_store = false;
    private String[] functional_store_depends = null;
    private String syncColumnName = null;

    // Custom table name and custom column names for many to many type column
    private String rel_table_name = null;
    private String rel_base_column = null;
    private String rel_relation_column = null;

    public OColumn(String label, Class<?> type) {
        this.label = label;
        this.type = type;
    }

    public OColumn(String label, Class<?> type, RelationType relationType) {
        this(label, type);
        this.relationType = relationType;
    }

    public OColumn setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getRecordSyncLimit() {
        return recordSyncLimit;
    }

    public OColumn setRecordSyncLimit(Integer recordSyncLimit) {
        this.recordSyncLimit = recordSyncLimit;
        return this;
    }

    public String getName() {
        return name;
    }

    public OColumn setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public OColumn setRelatedColumn(String column) {
        related_column = column;
        return this;
    }

    public OColumn setSize(Integer size) {
        this.size = size;
        return this;
    }

    public Integer getSize() {
        return size;
    }

    public OColumn setDefaultValue(Object defValue) {
        defaultValue = defValue;
        return this;
    }

    public OColumn setRequired() {
        required = true;
        return this;
    }

    public boolean isRequired() {
        return required;
    }

    public OColumn setAutoIncrement() {
        autoIncrement = true;
        return this;
    }

    public OColumn setLocalColumn() {
        isLocalColumn = true;
        return this;
    }

    public OColumn setType(Class<?> type) {
        this.type = type;
        return this;
    }

    public Class<?> getType() {
        return type;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public Boolean isAutoIncrement() {
        return autoIncrement;
    }

    public Boolean isLocal() {
        return isLocalColumn;
    }

    public String getRelatedColumn() {
        return related_column;
    }

    public OColumn addDomain(String column_name, String operator, Object value) {
        columnDomains.put(column_name, new ColumnDomain(column_name, operator,
                value));
        return this;
    }

    public OColumn addDomain(String condition_operator) {
        columnDomains.put("condition_operator_" + (condition_operator_index++)
                + condition_operator, new ColumnDomain(condition_operator));
        return this;
    }

    public LinkedHashMap<String, ColumnDomain> getDomains() {
        return columnDomains;
    }

    public boolean hasDomainFilterColumn() {
        return mHasDomainFilterColumn;
    }

    public OColumn setHasDomainFilterColumn(Boolean domainFilterColumn) {
        mHasDomainFilterColumn = domainFilterColumn;
        return this;
    }

    public void setDomainFilter(Odoo.Domain domainFilter) {
        this.domainFilter = domainFilter;
    }

    public boolean hasOnChange() {
        return (mOnChangeMethod != null);
    }

    public Method getOnChangeMethod() {
        return mOnChangeMethod;
    }

    public void setOnChangeMethod(Method method) {
        mOnChangeMethod = method;
    }

    public Boolean isOnChangeBGProcess() {
        return mOnChangeBGProcess;
    }

    public void setOnChangeBGProcess(Boolean process) {
        mOnChangeBGProcess = process;
    }

    public void cleanDomains() {
        columnDomains.clear();
    }

    public DomainFilterParser getDomainFilterParser(OModel model) {
        if (domainFilter != null) {
            return new DomainFilterParser(model, this, domainFilter.value());
        }
        return null;
    }

    /**
     * Clone domain.
     *
     * @param domains the domains
     * @return the o column
     */
    public OColumn cloneDomain(LinkedHashMap<String, ColumnDomain> domains) {
        columnDomains.putAll(domains);
        return this;
    }

    /**
     * Sets the functional store.
     *
     * @param store the new functional store
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
     * @param depends the depends
     * @return the o column
     */
    public OColumn setFunctionalStoreDepends(String[] depends) {
        functional_store_depends = depends;
        return this;
    }

    public Boolean isFunctionalColumn() {
        return is_functional_column;
    }

    public OColumn setIsFunctionalColumn(Boolean is_functional_column) {
        this.is_functional_column = is_functional_column;
        return this;
    }

    public Method getFunctionalMethod() {
        return functional_method;
    }

    public OColumn setFunctionalMethod(Method functional_method) {
        this.functional_method = functional_method;
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

    public HashMap<String, String> getSelectionMap() {
        return mSelectionMap;
    }

    public OColumn addSelection(String key, String value) {
        mSelectionMap.put(key, value);
        return this;
    }

    public String getSyncColumnName() {
        return syncColumnName;
    }

    public void setSyncColumnName(String syncColumnName) {
        this.syncColumnName = syncColumnName;
    }

    public String getSyncColumn() {
        if (getSyncColumnName() != null) {
            return getSyncColumnName();
        }
        return getName();
    }

    public String getRelTableName() {
        return rel_table_name;
    }

    public OColumn setRelTableName(String tableName) {
        this.rel_table_name = tableName;
        return this;
    }

    public String getRelBaseColumn() {
        return rel_base_column;
    }

    public OColumn setRelBaseColumn(String rel_base_column) {
        this.rel_base_column = rel_base_column;
        return this;
    }

    public String getRelRelationColumn() {
        return rel_relation_column;
    }

    public OColumn setRelRelationColumn(String rel_relation_column) {
        this.rel_relation_column = rel_relation_column;
        return this;
    }

    @Override
    public String toString() {
        return "OColumn{" +
                "name='" + name + '\'' +
                ", label='" + label + '\'' +
                ", related_column='" + related_column + '\'' +
                ", size=" + size +
                ", type=" + type +
                ", relationType=" + relationType +
                ", defaultValue=" + defaultValue +
                ", autoIncrement=" + autoIncrement +
                ", required=" + required +
                ", isLocalColumn=" + isLocalColumn +
                ", columnDomains=" + columnDomains +
                ", condition_operator_index=" + condition_operator_index +
                ", recordSyncLimit=" + recordSyncLimit +
                ", mOnChangeMethod=" + mOnChangeMethod +
                ", mOnChangeBGProcess=" + mOnChangeBGProcess +
                ", mHasDomainFilterColumn=" + mHasDomainFilterColumn +
                ", is_functional_column=" + is_functional_column +
                ", functional_method=" + functional_method +
                ", use_annotation=" + use_annotation +
                ", functional_store=" + functional_store +
                ", functional_store_depends=" + Arrays.toString(functional_store_depends) +
                '}';
    }

    public static class ColumnDomain {

        private String column = null;
        private String operator = null;
        private Object value = null;
        private String conditional_operator = null;

        public ColumnDomain(String conditional_operator) {
            this.conditional_operator = conditional_operator;
        }

        public ColumnDomain(String column, String operator, Object value) {
            this.column = column;
            this.operator = operator;
            this.value = value;
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

        public String getConditionalOperator() {
            return conditional_operator;
        }

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
