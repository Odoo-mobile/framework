package com.odoo.core.orm.fields.utils;

import android.os.Bundle;

import com.odoo.core.orm.fields.OColumn;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class DomainFilterParser {

    private OColumn baseColumn;
    private String domainString;
    private LinkedHashMap<String, FilterDomain> filterColumnValues = new LinkedHashMap<>();

    public DomainFilterParser(OColumn column, String domainString) {
        baseColumn = column;
        this.domainString = domainString;
        parseDomains();
    }

    public void parseDomains() {
        try {
            JSONArray fieldDomain = new JSONArray(domainString);
            for (int i = 0; i < fieldDomain.length(); i++) {
                if (fieldDomain.get(i) instanceof String) {
                    FilterDomain operator = new FilterDomain();
                    operator.operator_value = fieldDomain.get(i).toString();
                    filterColumnValues.put("operator#" + i, operator);
                } else {
                    JSONArray domain = fieldDomain.getJSONArray(i);
                    String compareField = domain.getString(0);
                    String operator = domain.getString(1);
                    String value = domain.getString(2);
                    if (value.trim().startsWith("@")) {
                        String valueField = value.replace("@", "");
                        String key = String.format("%s#%s", compareField, valueField);
                        FilterDomain filterDomain = new FilterDomain();
                        filterDomain.baseColumn = compareField;
                        filterDomain.operator = operator;
                        filterDomain.valueColumn = valueField;
                        filterColumnValues.put(key, filterDomain);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getFilterColumns() {
        return new ArrayList<>(filterColumnValues.keySet());
    }

    private void setValue(String key, Object value) {
        if (filterColumnValues.containsKey(key)) {
            FilterDomain filterDomain = filterColumnValues.get(key);
            filterDomain.value = value;
            filterColumnValues.put(key, filterDomain);
        }
    }

    public LinkedHashMap<String, OColumn.ColumnDomain> getDomain(Bundle formData) {
        if (formData != null) {
            for (String key : formData.keySet()) {
                setValue(key, formData.get(key));
            }
        }
        LinkedHashMap<String, OColumn.ColumnDomain> domains = new LinkedHashMap<>();
        int i = 1;
        if (!baseColumn.getDomains().isEmpty()) {
            domains.put("condition_operator_#" + i, new OColumn.ColumnDomain("and"));
        }
        for (FilterDomain domain : filterColumnValues.values()) {
            if (domain.operator_value != null)
                domains.put("condition_operator_#" + i++,
                        new OColumn.ColumnDomain(domain.operator_value));
            else
                domains.put(domain.baseColumn,
                        new OColumn.ColumnDomain(domain.baseColumn, domain.operator, domain.value));
        }
        return domains;
    }

    public FilterDomain getFilter(String key) {
        return filterColumnValues.get(key);
    }


    public class FilterDomain {
        public String baseColumn;
        public String operator;
        public Object value;
        public String valueColumn;
        public String operator_value;

        @Override
        public String toString() {
            if (operator_value != null)
                return operator_value;
            return String.format("[['%s', '%s', @%s=%s]]",
                    baseColumn, operator, valueColumn, value + "");
        }
    }
}
