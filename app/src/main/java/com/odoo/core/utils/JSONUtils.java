package com.odoo.core.utils;

import android.text.TextUtils;

import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class JSONUtils {
    public static <T> List<T> toList(JSONArray array) {
        List<T> list = new ArrayList<T>();
        try {
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    list.add((T) array.get(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static <T> JSONArray toArray(List<T> list) {
        JSONArray array = new JSONArray();
        try {
            for (T obj : list)
                array.put(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return array;
    }

    public static <T> List<T> toList(String list_data) {
        List<T> list = new ArrayList<>();
        try {
            list.addAll(JSONUtils.<T>toList(new JSONArray(list_data)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static ODataRow toDataRow(JSONObject json) {
        ODataRow row = new ODataRow();
        try {
            @SuppressWarnings("unchecked")
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                row.put(key, json.get(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return row;
    }

    public static JSONObject toJSONObject(ODataRow row) {
        JSONObject json = new JSONObject();
        try {
            for (String key : row.keys()) {
                json.put(key, row.get(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    public static JSONObject createJSONValues(OModel model, ODataRow row) {
        JSONObject values = null;
        try {
            values = new JSONObject();
            for (OColumn col : model.getColumns(false)) {
                if (col.getName().equals("id") && row.getInt("id") == 0) {
                    /* FIXME: 7.0 not supporting
                    Response from server : column "id" specified more than once
                     */
                    continue;
                }
                if (col.getRelationType() == null) {
                    if (!col.getName().equals("create_date") || !col.getName().equals("write_date")) {
                        Object val = row.get(col.getName());
                        if (val == null || val.toString().equals("false")
                                || TextUtils.isEmpty(val.toString())) {
                            val = false;
                        }
                        values.put(col.getName(), val);
                    }
                } else {
                    // Relation columns
                    switch (col.getRelationType()) {
                        case ManyToOne:
                            if (!row.getString(col.getName()).equals("false")) {
                                ODataRow m2o = row.getM2ORecord(col.getName()).browse();
                                if (m2o != null)
                                    values.put(col.getName(), m2o.getInt("id"));
                            }
                            break;
                        case OneToMany:
                            JSONArray o2mRecords = new JSONArray();
                            List<ODataRow> o2mRecordList = row.getO2MRecord(
                                    col.getName()).browseEach();
                            if (o2mRecordList.size() > 0) {
                                JSONArray rec_ids = new JSONArray();
                                for (ODataRow o2mR : o2mRecordList) {
                                    if (o2mR.getInt("id") != 0)
                                        rec_ids.put(o2mR.getInt("id"));
                                }
                                o2mRecords.put(6);
                                o2mRecords.put(false);
                                o2mRecords.put(rec_ids);
                                values.put(col.getName(),
                                        new JSONArray().put(o2mRecords));
                            }
                            break;
                        case ManyToMany:
                            JSONArray m2mRecords = new JSONArray();
                            List<ODataRow> m2mRecordList = row.getM2MRecord(
                                    col.getName()).browseEach();
                            if (!m2mRecordList.isEmpty()) {
                                JSONArray rec_ids = new JSONArray();
                                for (ODataRow o2mR : m2mRecordList) {
                                    if (o2mR.getInt("id") != 0)
                                        rec_ids.put(o2mR.getInt("id"));
                                }
                                m2mRecords.put(6);
                                m2mRecords.put(false);
                                m2mRecords.put(rec_ids);
                                values.put(col.getName(),
                                        new JSONArray().put(m2mRecords));
                            }
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

}
