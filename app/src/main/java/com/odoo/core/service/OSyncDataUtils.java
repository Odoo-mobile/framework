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
import com.odoo.core.rpc.Odoo;
import com.odoo.core.rpc.helper.ODomain;
import com.odoo.core.rpc.helper.OdooFields;
import com.odoo.core.rpc.helper.utils.gson.OdooRecord;
import com.odoo.core.rpc.helper.utils.gson.OdooResult;
import com.odoo.core.support.OUser;
import com.odoo.core.utils.ODateUtils;
import com.odoo.core.utils.OListUtils;
import com.odoo.core.utils.OdooRecordUtils;
import com.odoo.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class OSyncDataUtils {
    public static final String TAG = OSyncDataUtils.class.getSimpleName();
    private Context mContext;
    private OModel mModel;
    private OUser mUser;
    private OdooResult response;
    private HashSet<String> recordsId = new HashSet<>();
    private List<String> recentSyncIds = new ArrayList<>();
    private HashMap<String, SyncRelationRecords> relationRecordsHashMap = new HashMap<>();
    private Odoo mOdoo;
    private SyncResult mResult;
    private HashMap<String, List<Integer>> updateToServerRecords = new HashMap<>();
    private Boolean mCreateRelationRecords = true;

    public OSyncDataUtils(Context context, Odoo odoo) {
        mContext = context;
        mOdoo = odoo;
    }

    public void handleResult(OModel model, OUser user, SyncResult result, OdooResult response, Boolean createRelRecord) {
        mModel = model;
        mUser = user;
        mResult = result;
        this.response = response;
        mCreateRelationRecords = createRelRecord;
        List<OdooRecord> updateInLocal = checkLocalUpdatedRecords();
        handleResult(updateInLocal);
    }


    private List<OdooRecord> checkLocalUpdatedRecords() {
        // Array of records which are new or need to update in local
        List<OdooRecord> finalRecords = new ArrayList<>();
        try {
            // Getting list of ids which are present in local database
            List<Integer> serverIds = new ArrayList<>();
            HashMap<String, OdooRecord> serverIdRecords = new HashMap<>();
            List<OdooRecord> records = response.getRecords();
            for (OdooRecord record : records) {
                if (mModel.hasServerRecord(record.getInt("id"))
                        && mModel.isServerRecordDirty(record.getInt("id"))) {
                    int server_id = record.getInt("id");
                    serverIds.add(server_id);
                    serverIdRecords.put("key_" + server_id, record);
                } else {
                    finalRecords.add(record);
                }
            }

            // getting local dirty records if server records length = 0
            for (ODataRow row : mModel.select(new String[]{}, "_is_dirty = ? and _is_active = ? and id != ?",
                    new String[]{"true", "true", "0"})) {
                serverIds.add(row.getInt("id"));
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
                    if (record != null) {
                        Date write_date_obj = ODateUtils.createDateObject(write_date,
                                ODateUtils.DEFAULT_FORMAT, false);
                        Date _write_date_obj = ODateUtils.createDateObject(record.getString("_write_date"),
                                ODateUtils.DEFAULT_FORMAT, false);
                        if (_write_date_obj.compareTo(write_date_obj) > 0) {
                            // Local record is latest
                            updateToServerIds.add(server_id);
                        } else {
                            if (serverIdRecords.containsKey(key)) {
                                finalRecords.add(serverIdRecords.get(key));
                            }
                        }
                    }
                }
            }
            if (updateToServerIds.size() > 0) {
                updateToServerRecords.put(mModel.getModelName(), updateToServerIds);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return finalRecords;
    }

    private HashMap<String, String> getWriteDate(OModel model, List<Integer> ids) {
        HashMap<String, String> map = new HashMap<>();
        try {
            List<OdooRecord> result;
            if (model.getColumn("write_date") != null) {
                OdooFields fields = new OdooFields("write_date");
                ODomain domain = new ODomain();
                domain.add("id", "in", ids);
                OdooResult response =
                        mOdoo.searchRead(model.getModelName(), fields, domain, 0, 0, null);
                result = response.getRecords();
            } else {
                Log.i(TAG, "Perm Read hidden fields for write_date and create_date : (Only in Odoo 7.0) for " + ids);
                OdooResult response = mOdoo.permRead(model.getModelName(), ids);
                result = response.getArray("result");
            }

            if (!result.isEmpty()) {
                for (OdooRecord record : result) {
                    map.put("key_" + record.getInt("id"), record.getString("write_date"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    private void handleResult(List<OdooRecord> records) {
        try {
            recordsId.clear();
            int counter = records.size();
            List<OColumn> columns = mModel.getColumns(false);
            columns.addAll(mModel.getFunctionalColumns());
            for (OdooRecord record : records) {
                if (!recentSyncIds.contains(mModel.getModelName() + ":" + record.getInt("id"))) {
                    OValues values = new OValues();
                    recordsId.add(mModel.getModelName() + "_" + record.getInt("id"));
                    for (OColumn column : columns) {
                        String name = column.getSyncColumn();
                        String lName = column.getName();
                        if (column.getRelationType() == null) {
                            // checks for functional store fields
                            if (column.isFunctionalColumn() && column.canFunctionalStore()) {
                                List<String> depends = column.getFunctionalStoreDepends();
                                OValues dependValues = new OValues();
                                if (!column.isLocal())
                                    dependValues.put(column.getName(), record.get(column.getName()));
                                for (String depend : depends) {
                                    if (record.containsKey(depend)) {
                                        dependValues.put(depend, record.get(depend));
                                    }
                                }
                                Object value = mModel.getFunctionalMethodValue(column, dependValues);
                                values.put(lName, value);
                            } else {
                                // Normal Columns
                                values.put(lName, record.get(name));
                            }
                        } else {
                            // Relation Columns
                            if (!record.getString(name).equals("false")) {
                                switch (column.getRelationType()) {
                                    case ManyToOne:
                                        OdooRecord m2oData = record.getM20(name);
                                        OModel m2o_model = mModel.createInstance(column.getType());
                                        String recKey = m2o_model.getModelName() + "_" + m2oData.getInt("id");
                                        int m2oRowId;
                                        if (!recordsId.contains(recKey)) {
                                            OValues m2oValue = new OValues();
                                            m2oValue.put("id", m2oData.getInt("id"));
                                            m2oValue.put(m2o_model.getDefaultNameColumn(), m2oData.getString("name"));
                                            m2oValue.put("_is_dirty", "false");
                                            m2oRowId = m2o_model.insertOrUpdate(m2oData.getInt("id"),
                                                    m2oValue);
                                        } else {
                                            m2oRowId = m2o_model.selectRowId(m2oData.getInt("id"));
                                        }

                                        values.put(lName, m2oRowId);
                                        if (mCreateRelationRecords) {
                                            // Add id to sync if model contains more than (id,name) columns
                                            if (m2o_model.getColumns(false).size() > 2
                                                    || (m2o_model.getColumns(false).size() > 4
                                                    && mModel.getOdooVersion().getVersionNumber() > 7)) {
                                                List<Integer> m2oIds = new ArrayList<>();
                                                m2oIds.add(m2oData.getInt("id"));
                                                addUpdateRelationRecord(mModel, m2o_model.getTableName(),
                                                        column.getType(), name, null,
                                                        column.getRelationType(), m2oIds);
                                            }
                                        }
                                        m2o_model.close();
                                        break;
                                    case ManyToMany:
                                        OModel m2mModel = mModel.createInstance(column.getType());
                                        List<Integer> m2mIds = OListUtils.doubleToIntList(record.getM2M(name));
                                        if (mCreateRelationRecords) {
                                            addUpdateRelationRecord(mModel, m2mModel.getTableName(), column.getType(),
                                                    name, null, column.getRelationType(),
                                                    (column.getRecordSyncLimit() > 0) ?
                                                            m2mIds.subList(0, column.getRecordSyncLimit()) : m2mIds);
                                        }
                                        List<Integer> m2mRowIds = new ArrayList<>();
                                        for (Integer id : m2mIds) {
                                            recKey = m2mModel.getModelName() + "_" + id;
                                            int r_id;
                                            if (!recordsId.contains(recKey)) {
                                                OValues m2mValues = new OValues();
                                                m2mValues.put("id", id);
                                                m2mValues.put("_is_dirty", "false");
                                                r_id = m2mModel.insertOrUpdate(id, m2mValues);
                                            } else {
                                                r_id = m2mModel.selectRowId(id);
                                            }
                                            m2mRowIds.add(r_id);
                                        }
                                        if (m2mRowIds.size() > 0) {
                                            // Putting many to many related ids
                                            // (generated _id for each of server ids)
                                            values.put(lName, m2mRowIds);
                                        }
                                        m2mModel.close();
                                        break;
                                    case OneToMany:
                                        if (mCreateRelationRecords) {
                                            OModel o2mModel = mModel.createInstance(column.getType());
                                            List<Integer> o2mIds = OListUtils.doubleToIntList(record.getO2M(name));
                                            addUpdateRelationRecord(mModel, o2mModel.getTableName(),
                                                    column.getType(), name, column.getRelatedColumn(),
                                                    column.getRelationType(),
                                                    (column.getRecordSyncLimit() > 0) ?
                                                            o2mIds.subList(0, column.getRecordSyncLimit()) : o2mIds);
                                            o2mModel.close();
                                        }
                                        break;
                                }
                            }
                        }
                    }
                    // Some default values
                    values.put("id", record.getInt("id"));
                    values.put("_write_date", ODateUtils.getUTCDate());
                    values.put("_is_active", "true");
                    values.put("_is_dirty", "false");
                    mModel.insertOrUpdate(record.getInt("id"), values);

                    // Fixed issue of multiple time sync same record. Performance improved
                    // Adding to recent sync list for avoiding duplicate process for record
                    recentSyncIds.add(mModel.getModelName() + ":" + record.getInt("id"));
                    mResult.stats.numEntries++;
                    counter++;
                }
            }
            Log.i(TAG, counter + " records affected");
        } catch (Exception e) {
            mResult.stats.numParseExceptions++;
            e.printStackTrace();
        }
    }


    public boolean updateRecordsOnServer(OSyncAdapter adapter) {
        try {
            // Use key (modal name) from updateToServerRecords
            // use updateToServerRecords ids
            int counter = 0;
            for (String key : updateToServerRecords.keySet()) {
                OModel model = OModel.get(mContext, key, mUser.getAndroidName());
                List<String> ids = OListUtils.toStringList(updateToServerRecords.get(key));
                counter += ids.size();
                for (ODataRow record : model.select(null,
                        "id IN ( " + StringUtils.repeat("?, ", ids.size() - 1) + " ?)",
                        ids.toArray(new String[ids.size()]))) {

                    if (adapter.validateRelationRecords(model, record)) {
                        mOdoo.updateRecord(model.getModelName(), OdooRecordUtils
                                        .createRecordValues(model, record),
                                record.getInt("id"));
                        OValues value = new OValues();
                        value.put("_is_dirty", "false");
                        value.put("_write_date", ODateUtils.getUTCDate());
                        model.update(record.getInt(OColumn.ROW_ID), value);
                        model.close();
                    }
                }
            }
            Log.i(TAG, counter + " records updated on server");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void addUpdateRelationRecord(OModel baseModel, String relTable, Class<?> model,
                                         String column, String relatedColumn,
                                         OColumn.RelationType type, List<Integer> ids) {
        String key = relTable + "_" + column;
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

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (mModel != null)
            mModel.close();
    }

    public static class SyncRelationRecords {
        private OModel baseModel;
        private Class<?> relationModel;
        private String relationColumn;
        private String relatedColumn;
        private OColumn.RelationType relationType;
        private List<Integer> serverIds = new ArrayList<>();

        public SyncRelationRecords(OModel baseModel, Class<?> relationModel, String relationColumn, String relatedColumn,
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

        public Class<?> getRelationModel() {
            return relationModel;
        }

        public void setRelationModel(Class<?> relationModel) {
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
