package com.odoo.core.utils;

import android.text.TextUtils;

import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.rpc.helper.ORecordValues;
import com.odoo.core.rpc.helper.utils.gson.OdooRecord;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class OdooRecordUtils {
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
            list.addAll(OdooRecordUtils.<T>toList(new JSONArray(list_data)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static ODataRow toDataRow(OdooRecord record) {
        ODataRow row = new ODataRow();
        Set<String> keys = record.keySet();
        for (String key : keys) {
            row.put(key, record.get(key));
        }
        return row;
    }

    public static ORecordValues toRecordValues(ODataRow row) {
        ORecordValues json = new ORecordValues();
        try {
            for (String key : row.keys()) {
                json.put(key, row.get(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    public static ORecordValues createRecordValues(OModel model, ODataRow row) {
        ORecordValues values = new ORecordValues();
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
                    values.put(col.getSyncColumn(), val);
                }
            } else {
                // Relation columns
                switch (col.getRelationType()) {
                    case ManyToOne:
                        if (!row.getString(col.getName()).equals("false")) {
                            ODataRow m2o = row.getM2ORecord(col.getName()).browse();
                            if (m2o != null) {
                                values.put(col.getSyncColumn(), m2o.getInt("id"));
                            }
                        } else {
                            values.put(col.getSyncColumn(), false);
                        }
                        break;
                    case OneToMany:
                        List<Object> o2mRecords = new ArrayList<>();
                        List<ODataRow> o2mRecordList = row.getO2MRecord(
                                col.getName()).browseEach();
                        List<Integer> rec_ids = new ArrayList<>();
                        if (o2mRecordList.size() > 0) {
                            for (ODataRow o2mR : o2mRecordList) {
                                if (o2mR.getInt("id") != 0)
                                    rec_ids.add(o2mR.getInt("id"));
                            }
                        }
                        o2mRecords.add(6);
                        o2mRecords.add(false);
                        o2mRecords.add(rec_ids);
                        List<Object> oneToManyValue = new ArrayList<>();
                        oneToManyValue.add(o2mRecords);
                        values.put(col.getSyncColumn(), oneToManyValue);

                        break;
                    case ManyToMany:
                        List<Object> m2mRecords = new ArrayList<>();
                        List<ODataRow> m2mRecordList = row.getM2MRecord(
                                col.getName()).browseEach();
                        rec_ids = new ArrayList<>();
                        if (!m2mRecordList.isEmpty()) {
                            for (ODataRow o2mR : m2mRecordList) {
                                if (o2mR.getInt("id") != 0)
                                    rec_ids.add(o2mR.getInt("id"));
                            }
                        }
                        m2mRecords.add(6);
                        m2mRecords.add(false);
                        m2mRecords.add(rec_ids);
                        List<Object> manyToManyRecord = new ArrayList<>();
                        manyToManyRecord.add(m2mRecords);
                        values.put(col.getSyncColumn(), manyToManyRecord);
                        break;
                }
            }

        }
        return values;
    }
}
