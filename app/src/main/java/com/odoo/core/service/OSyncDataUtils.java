/**
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
 * Created on 2/1/15 4:15 PM
 */
package com.odoo.core.service;

import android.content.Context;
import android.content.SyncResult;
import android.util.Log;

import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.support.OUser;
import com.odoo.core.support.OdooFields;
import com.odoo.core.utils.JSONUtils;
import com.odoo.core.utils.ODateUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import odoo.ODomain;
import odoo.Odoo;

public class OSyncDataUtils {
    public static final String TAG = OSyncDataUtils.class.getSimpleName();
    private Context mContext;
    private OModel mModel;
    private OUser mUser;
    private JSONObject response;
    private HashSet<Integer> recordsId = new HashSet<>();
    private HashMap<String, SyncRelationRecords> relationRecordsHashMap = new HashMap<>();
    private Odoo mOdoo;
    private SyncResult mResult;
    private HashMap<String, List<Integer>> updateToServerRecords = new HashMap<>();
    private Boolean mCreateRelationRecords = true;

    public OSyncDataUtils(Context context, Odoo odoo, OModel model, OUser user, JSONObject response,
                          SyncResult result, Boolean createRelRecord) {
        mContext = context;
        mOdoo = odoo;
        mModel = model;
        mUser = user;
        this.response = response;
        mResult = result;
        mCreateRelationRecords = createRelRecord;
        checkLocalUpdatedRecords();
        handleResult();
    }


    private void checkLocalUpdatedRecords() {
        try {
            // FIXME: Check for _is_active

            // Array of records which are new or need to update in local
            JSONArray finalRecords = new JSONArray();

            // Getting list of ids which are present in local database
            List<Integer> serverIds = new ArrayList<>();
            HashMap<String, JSONObject> serverIdRecords = new HashMap<>();
            JSONArray records = response.getJSONArray("records");
            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.getJSONObject(i);
                if (mModel.hasServerRecord(record.getInt("id"))) {
                    int server_id = record.getInt("id");
                    serverIds.add(server_id);
                    serverIdRecords.put("key_" + server_id, record);
                } else {
                    finalRecords.put(record);
                }
            }

            // Comparing dirty (updated) record
            List<Integer> updateToServerIds = new ArrayList<>();
            if (serverIds.size() > 0) {
                HashMap<String, String> write_dates = getWriteDate(mModel, serverIds);
                for (Integer server_id : serverIds) {
                    String key = "key_" + server_id;
                    String write_date = write_dates.get(key);
                    ODataRow record = mModel.browse(new String[]{"_write_date"}, "id = ?",
                            new String[]{server_id + ""});
                    Date write_date_obj = ODateUtils.createDateObject(write_date,
                            ODateUtils.DEFAULT_FORMAT, false);
                    Date _write_date_obj = ODateUtils.createDateObject(record.getString("_write_date"),
                            ODateUtils.DEFAULT_FORMAT, false);

                    if (_write_date_obj.compareTo(write_date_obj) > 0) {
                        // Local record is latest
                        updateToServerIds.add(server_id);
                    } else {
                        finalRecords.put(serverIdRecords.get(key));
                    }
                }
            }
            if (updateToServerIds.size() > 0) {
                updateToServerRecords.put(mModel.getModelName(), updateToServerIds);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, String> getWriteDate(OModel model, List<Integer> ids) {
        HashMap<String, String> map = new HashMap<>();
        try {
            JSONArray result = null;
            if (model.getColumn("write_date") != null) {
                OdooFields fields = new OdooFields(new String[]{"write_date"});
                ODomain domain = new ODomain();
                domain.add("id", "in", ids);
                JSONObject data = mOdoo.search_read(model.getModelName(), fields.get(), domain.get());
                result = data.getJSONArray("records");
            } else {
                JSONObject data = mOdoo.perm_read(model.getModelName(), ids);
                result = data.getJSONArray("result");
            }

            if (result.length() > 0) {
                for (int i = 0; i < result.length(); i++) {
                    JSONObject obj = result.getJSONObject(i);
                    map.put("key_" + obj.getInt("id"), obj.getString("write_date"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    private void handleResult() {
        try {
            JSONArray records = response.getJSONArray("records");
            int length = records.length();
            List<OValues> valuesCollection = new ArrayList<>();
            List<OColumn> columns = mModel.getColumns(false);
            for (int i = 0; i < length; i++) {
                JSONObject record = records.getJSONObject(i);
                OValues values = new OValues();
                recordsId.add(record.getInt("id"));
                for (OColumn column : columns) {
                    String name = column.getName();
                    if (column.getRelationType() == null) {
                        // Normal Columns
                        values.put(name, record.get(name));
                    } else {
                        // Relation Columns
                        if (!(record.get(name) instanceof Boolean)) {
                            switch (column.getRelationType()) {
                                case ManyToOne:
                                    JSONArray m2oData = record.getJSONArray(name);
                                    OModel m2o_model = mModel.createInstance(column.getType());
                                    OValues m2oValue = new OValues();
                                    m2oValue.put("id", m2oData.get(0));
                                    m2oValue.put("name", m2oData.get(1));
                                    int m2oRowId = m2o_model.insertOrUpdate("id = ?", new String[]{m2oData.getInt(0) + ""},
                                            m2oValue);
                                    if (m2oRowId == OModel.INVALID_ROW_ID) {
                                        m2oRowId = mModel.selectRowId(m2oData.getInt(0));
                                    }
                                    values.put(name, m2oRowId);
                                    if (mCreateRelationRecords) {
                                        // Add id to sync if model contains more than (id,name) columns
                                        if (m2o_model.getColumns(false).size() > 2
                                                || (m2o_model.getColumns(false).size() > 4
                                                && mModel.getOdooVersion().getVersion_number() > 7)) {
                                            List<Integer> m2oIds = new ArrayList<>();
                                            m2oIds.add(m2oData.getInt(0));
                                            addUpdateRelationRecord(mModel, m2o_model, name, null,
                                                    column.getRelationType(), m2oIds);
                                        }
                                    }
                                    break;
                                case ManyToMany:
                                    OModel m2mModel = mModel.createInstance(column.getType());
                                    List<Integer> m2mIds = JSONUtils.<Integer>toList(record.getJSONArray(name));
                                    if (mCreateRelationRecords) {
                                        addUpdateRelationRecord(mModel, m2mModel, name, null,
                                                column.getRelationType(),
                                                (column.getRecordSyncLimit() > 0) ?
                                                        m2mIds.subList(0, column.getRecordSyncLimit()) : m2mIds);
                                    }
                                    List<Integer> m2mRowIds = new ArrayList<>();
                                    for (Integer id : m2mIds) {
                                        OValues m2mValues = new OValues();
                                        m2mValues.put("id", id);
                                        int r_id = m2mModel.insertOrUpdate("id = ?", new String[]{id + ""}, m2mValues);
                                        m2mRowIds.add(r_id);
                                    }
                                    if (m2mRowIds.size() > 0) {
                                        // Putting many to many related ids
                                        // (generated _id for each of server ids)
                                        values.put(name, m2mRowIds);
                                    }
                                    break;
                                case OneToMany:
                                    if (mCreateRelationRecords) {
                                        OModel o2mModel = mModel.createInstance(column.getType());
                                        List<Integer> o2mIds = JSONUtils.<Integer>toList(record.getJSONArray(name));
                                        addUpdateRelationRecord(mModel, o2mModel, name, column.getRelatedColumn(),
                                                column.getRelationType(),
                                                (column.getRecordSyncLimit() > 0) ?
                                                        o2mIds.subList(0, column.getRecordSyncLimit()) : o2mIds);
                                    }
                                    break;
                            }
                        }
                    }
                }
                // Some default values
                values.put("_write_date", ODateUtils.getUTCDate());
                values.put("_is_active", true);
                valuesCollection.add(values);
            }
            HashMap<String, List<Integer>> map = mModel.insertOrUpdate(valuesCollection);
            if (map.size() > 0) {
                mResult.stats.numInserts = map.get(OModel.KEY_INSERT_IDS).size();
                mResult.stats.numUpdates = map.get(OModel.KEY_UPDATE_IDS).size();
                Log.i(TAG, mResult.stats.numInserts + " Record created");
                Log.i(TAG, mResult.stats.numUpdates + " Record updated");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean updateRecordsOnServer() {
        try {
            //TODO: Update latest dirty record to server. (local_write_date > server_write_date)
            // Use key (modal name) from updateToServerRecords
            // use updateToServerRecords ids

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void addUpdateRelationRecord(OModel baseModel, OModel model, String column, String relatedColumn,
                                         OColumn.RelationType type, List<Integer> ids) {
        String key = model.getTableName() + "_" + column;
        if (relationRecordsHashMap.containsKey(key)) {
            SyncRelationRecords data = relationRecordsHashMap.get(key);
            data.updateIds(ids);
            relationRecordsHashMap.put(key, data);
        } else {
            relationRecordsHashMap.put(key,
                    new SyncRelationRecords(baseModel, model, column, relatedColumn, type, ids));
        }
    }

    public HashMap<String, SyncRelationRecords> getRelationRecordsHashMap() {
        if (mCreateRelationRecords)
            return relationRecordsHashMap;
        return new HashMap<>();
    }

    public static class SyncRelationRecords {
        private OModel baseModel;
        private OModel relationModel;
        private String relationColumn;
        private String relatedColumn;
        private OColumn.RelationType relationType;
        private List<Integer> serverIds = new ArrayList<>();

        public SyncRelationRecords(OModel baseModel, OModel relationModel, String relationColumn, String relatedColumn,
                                   OColumn.RelationType relationType, List<Integer> serverIds) {
            this.baseModel = baseModel;
            this.relationModel = relationModel;
            this.relationColumn = relationColumn;
            this.relatedColumn = relatedColumn;
            this.relationType = relationType;
            this.serverIds.addAll(serverIds);
        }

        public OModel getBaseModel() {
            return baseModel;
        }

        public void setBaseModel(OModel baseModel) {
            this.baseModel = baseModel;
        }

        public OModel getRelationModel() {
            return relationModel;
        }

        public void setRelationModel(OModel relationModel) {
            this.relationModel = relationModel;
        }

        public String getRelationColumn() {
            return relationColumn;
        }

        public void setRelationColumn(String relationColumn) {
            this.relationColumn = relationColumn;
        }


        public String getRelatedColumn() {
            return relatedColumn;
        }

        public void setRelatedColumn(String relatedColumn) {
            this.relatedColumn = relatedColumn;
        }

        public OColumn.RelationType getRelationType() {
            return relationType;
        }

        public void setRelationType(OColumn.RelationType relationType) {
            this.relationType = relationType;
        }

        public List<Integer> getServerIds() {
            return serverIds;
        }

        public void setServerIds(List<Integer> serverIds) {
            this.serverIds.clear();
            this.serverIds.addAll(serverIds);
        }

        public void updateIds(List<Integer> ids) {
            this.serverIds.addAll(ids);
        }


        public List<Integer> getUniqueIds() {
            List<Integer> ids = new ArrayList<>();
            HashSet<Integer> uIds = new HashSet<>(serverIds);
            ids.addAll(uIds);
            return ids;
        }
    }

}
