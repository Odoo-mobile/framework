package com.odoo.core.rpc.helper.utils.gson;

import com.google.gson.internal.LinkedTreeMap;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public abstract class OdooRecord<K, V> extends AbstractMap<K, V> {

    public List<OdooRecord> records = new ArrayList<>();

    public String getString(String key) {
        if (containsKey(key))
            return get(key).toString();
        return "false";
    }

    public Double getDouble(String key) {
        return (Double) get(key);
    }

    public Integer getInt(String key) {
        return getDouble(key).intValue();
    }

    public Boolean getBoolean(String key) {
        return (Boolean) get(key);
    }

    public OdooRecord getM20(String key) {
        if (!getString(key).equals("false")) {
            OdooRecord rec = new LinkedTreeMap();
            List<Object> value = getArray(key);
            rec.put("id", value.get(0));
            rec.put("name", value.get(1));
            return rec;
        }
        return null;
    }

    public List<Integer> getM2M(String key) {
        return getO2M(key);
    }

    public List<Integer> getO2M(String key) {
        if (!getString(key).equals("false")) {
            return getArray(key);
        }
        return new ArrayList<>();
    }

    public <T> List<T> getArray(String key) {
        return (List<T>) get(key);
    }
}