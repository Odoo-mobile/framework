package com.odoo.core.orm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RelValues implements Serializable {

    private HashMap<RelCommands, List<Object>> columnValues = new HashMap<>();

    public RelValues append(List<Object> values) {
        return append(values.toArray(new Object[values.size()]));
    }

    public RelValues append(Object... values) {
        return manageRecord(RelCommands.Append, values);
    }

    public RelValues replace(List<Object> values) {
        return replace(values.toArray(new Object[values.size()]));
    }

    public RelValues replace(Object... values) {
        return manageRecord(RelCommands.Replace, values);
    }

    public RelValues delete(List<Integer> values) {
        return delete(values.toArray(new Integer[values.size()]));
    }

    public RelValues delete(Integer... values) {
        List<Object> ids = new ArrayList<>();
        ids.addAll(Arrays.asList(values));
        return manageRecord(RelCommands.Delete, ids.toArray(new Object[ids.size()]));
    }

    public RelValues unlink(List<Integer> values) {
        return unlink(values.toArray(new Integer[values.size()]));
    }

    public RelValues unlink(Integer... values) {
        List<Object> ids = new ArrayList<>();
        ids.addAll(Arrays.asList(values));
        return manageRecord(RelCommands.Unlink, ids.toArray(new Object[ids.size()]));
    }

    private RelValues manageRecord(RelCommands command, Object... values) {
        if (columnValues.containsKey(command)) {
            List<Object> items = new ArrayList<>();
            items.addAll(columnValues.get(command));
            items.addAll(Arrays.asList(values));
            columnValues.put(command, items);
        } else {
            columnValues.put(command, Arrays.asList(values));
        }
        return this;
    }

    public HashMap<RelCommands, List<Object>> getColumnValues() {
        return columnValues;
    }

    @Override
    public String toString() {
        return columnValues + "";
    }
}
