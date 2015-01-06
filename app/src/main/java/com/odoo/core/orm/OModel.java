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
 * Created on 30/12/14 3:31 PM
 */
package com.odoo.core.orm;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.odoo.base.addons.ir.IrModel;
import com.odoo.core.auth.OdooAccountManager;
import com.odoo.core.orm.annotation.Odoo;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OBoolean;
import com.odoo.core.orm.fields.types.ODateTime;
import com.odoo.core.orm.fields.types.OInteger;
import com.odoo.core.orm.provider.BaseModelProvider;
import com.odoo.core.support.OUser;
import com.odoo.core.utils.OCursorUtils;
import com.odoo.core.utils.ODateUtils;
import com.odoo.core.utils.OPreferenceManager;
import com.odoo.core.utils.StringUtils;
import com.odoo.core.utils.logger.OLog;

import java.io.InvalidObjectException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import odoo.ODomain;
import odoo.OdooVersion;

public class OModel extends OSQLite {

    public static final String TAG = OModel.class.getSimpleName();
    public static final String BASE_AUTHORITY = "com.odoo.core.provider.content";
    public static final String KEY_UPDATE_IDS = "key_update_ids";
    public static final String KEY_INSERT_IDS = "key_insert_ids";
    public static final int INVALID_ROW_ID = -1;
    private Context mContext;
    private OUser mUser;
    private String model_name = null;
    private List<OColumn> mColumns = new ArrayList<>();
    private List<OColumn> mRelationColumns = new ArrayList<>();
    private HashMap<String, Field> mDeclaredFields = new HashMap<>();
    private OdooVersion mOdooVersion = null;

    // Relation record command
    public enum Command {
        Add(0), Update(1), Delete(2), Replace(6);

        int type;

        Command(int type) {
            this.type = type;
        }

        public int getValue() {
            return type;
        }
    }

    // Base Columns
    OColumn id = new OColumn("ID", OInteger.class).setDefaultValue(0);
    @Odoo.api.v8
    @Odoo.api.v9alpha
    public OColumn create_date = new OColumn("Created On", ODateTime.class);

    @Odoo.api.v8
    @Odoo.api.v9alpha
    public OColumn write_date = new OColumn("Last Updated On", ODateTime.class);

    // Local Base columns
    OColumn _id = new OColumn("_ID", OInteger.class).setAutoIncrement().setLocalColumn();
    OColumn _write_date = new OColumn("Local Write Date", ODateTime.class).setLocalColumn();
    OColumn _is_dirty = new OColumn("Dirty record", OBoolean.class).setDefaultValue(false).setLocalColumn();
    OColumn _is_active = new OColumn("Active Record", OBoolean.class).setDefaultValue(true).setLocalColumn();

    public OModel(Context context, String model_name, OUser user) {
        super(context, user);
        mContext = context;
        mUser = (user == null) ? OUser.current(context) : user;
        this.model_name = model_name;
        mOdooVersion = new OdooVersion();
        mOdooVersion.setVersion_number(mUser.getVersion_number());
        mOdooVersion.setServer_serie(mUser.getVersion_serie());
    }

    public OUser getUser() {
        return mUser;
    }

    public OdooVersion getOdooVersion() {
        return mOdooVersion;
    }

    public List<OColumn> getColumns() {
        if (mColumns.size() == 0) {
            prepareFields();
        }
        return mColumns;
    }

    public List<OColumn> getColumns(Boolean local) {
        if (local != null) {
            List<OColumn> cols = new ArrayList<>();
            for (OColumn column : getColumns())
                if (local == column.isLocal())
                    cols.add(column);
            return cols;
        } else {
            return mColumns;
        }
    }

    public List<OColumn> getRelationColumns() {
        if (mColumns.size() <= 0)
            prepareFields();
        return mRelationColumns;
    }

    public OColumn getColumn(String column_name) {
        if (mDeclaredFields.size() <= 0)
            prepareFields();
        Field filed = mDeclaredFields.get(column_name);
        return getColumn(filed);
    }

    private OColumn getColumn(Field field) {
        OColumn column = null;
        if (field != null) {
            try {
                field.setAccessible(true);
                column = (OColumn) field.get(this);
                column.setName(field.getName());
                Boolean validField = compatibleField(field);
                if (validField) {
                    /**
                     * TODO:
                     * 1. Check for functional annotation
                     * 2. Check for function store annotation
                     * 3. Check for onChange event method
                     * 4. Check for domain filter column
                     */
                    return column;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        }
        return null;
    }

    private boolean compatibleField(Field field) {
        if (mOdooVersion != null) {
            Annotation[] annotations = field.getDeclaredAnnotations();
            if (annotations.length > 0) {
                int version = 0;
                for (Annotation annotation : annotations) {
                    // Check for odoo api annotation
                    Class<? extends Annotation> type = annotation.annotationType();
                    if (type.getDeclaringClass().isAssignableFrom(Odoo.api.class)) {
                        switch (mOdooVersion.getVersion_number()) {
                            case 9:
                                if (type.isAssignableFrom(Odoo.api.v9alpha.class)) {
                                    version++;
                                }
                                break;
                            case 8:
                                if (type.isAssignableFrom(Odoo.api.v8.class)) {
                                    version++;
                                }
                                break;
                            case 7:
                                if (type.isAssignableFrom(Odoo.api.v7.class)) {
                                    version++;
                                }
                                break;
                        }
                    }
                    // Check for functional annotation
                    if (type.isAssignableFrom(Odoo.Functional.class)
                            || type.isAssignableFrom(Odoo.onChange.class)
                            || type.isAssignableFrom(Odoo.hasDomainFilter.class)) {
                        version++;
                    }
                }
                return (version > 0) ? true : false;
            }
            return true;
        }
        return false;
    }

    private void prepareFields() {
        mColumns.clear();
        mRelationColumns.clear();
        List<Field> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(getClass().getSuperclass().getDeclaredFields()));
        fields.addAll(Arrays.asList(getClass().getDeclaredFields()));
        mDeclaredFields.clear();
        for (Field field : fields) {
            if (field.getType().isAssignableFrom(OColumn.class)) {
                mDeclaredFields.put(field.getName(), field);
                try {
                    OColumn column = getColumn(field);
                    if (column != null) {
                        if (column.getRelationType() != null) {
                            mRelationColumns.add(column);
                        }
                        //TODO: check for functional columns.
                        mColumns.add(column);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getModelName() {
        return model_name;
    }

    public List<OColumn> getManyToManyColumns(OModel relation_model) {
        List<OColumn> cols = new ArrayList<OColumn>();
        _write_date.setName("_write_date");
        cols.add(_write_date);
        _is_dirty.setName("_is_dirty");
        cols.add(_is_dirty);
        _is_active.setName("_is_active");
        cols.add(_is_active);

        OColumn base_id = new OColumn("Base Id", OInteger.class);
        base_id.setName(getTableName() + "_id");
        cols.add(base_id);
        OColumn relation_id = new OColumn("Relation Id", OInteger.class);
        relation_id.setName(relation_model.getTableName() + "_id");
        cols.add(relation_id);
        return cols;
    }

    public OModel createInstance(Class<?> type) {
        try {
            Constructor<?> constructor = type.getConstructor(Context.class, OUser.class);
            OModel model = (OModel) constructor.newInstance(mContext, mUser);
            return model;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getTableName() {
        return getModelName().replaceAll("\\.", "_");
    }

    public String toString() {
        return getModelName();
    }

    public static OModel get(Context context, String model_name, String username) {
        OModel model = null;
        try {
            OPreferenceManager pfManager = new OPreferenceManager(context);
            Class<?> model_class = Class.forName(pfManager.getString(
                    model_name, null));
            if (model_class != null)
                model = new OModel(context, model_name, OdooAccountManager.getDetails(context, username))
                        .createInstance(model_class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return model;
    }

    public Uri uri() {
        String path = getModelName().toLowerCase(Locale.getDefault());
        return BaseModelProvider.buildURI(BASE_AUTHORITY, path, mUser.getAndroidName());
    }

    public ODomain defaultDomain() {
        return new ODomain();
    }

    private String[] updateProjection(String[] projection) {
        HashSet<String> names = new HashSet<>();
        if (projection == null) {
            projection = projection();
        }
        names.addAll(Arrays.asList(projection));
        names.addAll(Arrays.asList(new String[]{OColumn.ROW_ID, "id"}));
        return names.toArray(new String[names.size()]);
    }

    public String[] projection() {
        List<String> names = new ArrayList<>();
        for (OColumn column : getColumns()) {
            if (column.getRelationType() == null || column.canFunctionalStore()) {
                names.add(column.getName());
            } else if (column.getRelationType() == OColumn.RelationType.ManyToOne) {
                names.add(column.getName());
            }
        }
        return names.toArray(new String[names.size()]);
    }

    // Sync default methods
    public boolean checkForCreateDate() {
        return true;
    }

    public boolean checkForWriteDate() {
        return true;
    }

    // Database Operations
    public ODataRow browse(int row_id) {
        ODataRow row = null;
        Cursor cr = mContext.getContentResolver().query(uri().withAppendedPath(uri(), row_id + ""),
                null, null, null, null);
        if (cr != null && cr.getCount() > 0) {
            cr.moveToFirst();
            row = OCursorUtils.toDatarow(cr);
            cr.close();
        }
        return row;
    }

    public ODataRow browse(String[] projection, String selection, String[] args) {
        List<ODataRow> rows = select(updateProjection(projection), selection, args);
        if (rows.size() > 0) {
            return rows.get(0);
        }
        return null;
    }

    public List<Integer> getServerIds() {
        List<Integer> ids = new ArrayList<>();
        for (ODataRow row : select(new String[]{"id"})) {
            if (row.getInt("id") != 0) {
                ids.add(row.getInt("id"));
            }
        }
        return ids;
    }

    public boolean isEmptyTable() {
        return (count(null, null) <= 0);
    }

    public String getLastSyncDateTime() {
        IrModel model = new IrModel(mContext, mUser);
        List<ODataRow> records = model.select(null, "model = ?", new String[]{getModelName()});
        if (records.size() > 0) {
            return records.get(0).getString("last_synced");
        }
        return null;
    }

    public void setLastSyncDateTimeToNow() {
        IrModel model = new IrModel(mContext, mUser);
        OValues values = new OValues();
        values.put("model", getModelName());
        values.put("last_synced", ODateUtils.getUTCDate());
        model.insertOrUpdate("model = ?", new String[]{getModelName()}, values);
    }

    public List<ODataRow> select() {
        return select(null, null, null, null);
    }

    public List<ODataRow> select(String[] projection) {
        return select(projection, null, null, null);
    }

    public List<ODataRow> select(String[] projection, String where, String[] args) {
        return select(projection, where, args, null);
    }

    public List<ODataRow> select(String[] projection, String where, String[] args, String sortOrder) {
        Cursor cr = mContext.getContentResolver().query(uri(),
                updateProjection(projection), where, args, sortOrder);
        List<ODataRow> rows = new ArrayList<>();
        if (cr != null && cr.moveToFirst()) {
            do {
                ODataRow row = OCursorUtils.toDatarow(cr);
                for (OColumn column : getRelationColumns(projection)) {
                    if (!row.getString(column.getName()).equals("false")
                            || column.getRelationType() == OColumn.RelationType.OneToMany
                            || column.getRelationType() == OColumn.RelationType.ManyToMany) {
                        switch (column.getRelationType()) {
                            case ManyToMany:
                                OM2MRecord m2mRecords = new OM2MRecord(this, column, row.getInt(OColumn.ROW_ID));
                                row.put(column.getName(), m2mRecords);
                                OLog.log("Setting many to many column " + column.getName());
                                break;
                            case ManyToOne:
                                OM2ORecord m2ORecord = new OM2ORecord(this, column, row.getInt(column.getName()));
                                row.put(column.getName(), m2ORecord);
                                break;
                            case OneToMany:
                                OO2MRecord o2MRecord = new OO2MRecord(this, column, row.getInt(OColumn.ROW_ID));
                                row.put(column.getName(), o2MRecord);
                                break;
                        }
                    }
                }
                rows.add(row);
            } while (cr.moveToNext());
        }

        cr.close();
        return rows;
    }

    private List<OColumn> getRelationColumns(String[] projection) {
        List<OColumn> cols = new ArrayList<>();
        if (projection != null) {
            for (String key : projection) {
                OColumn column = getColumn(key);
                if (column.getRelationType() != null) {
                    cols.add(column);
                }
            }
        } else {
            cols.addAll(getRelationColumns());
        }
        return cols;
    }

    public int insertOrUpdate(String selection, String[] args, OValues values) {
        int count = update(selection, args, values);
        if (count <= 0) {
            return insert(values);
        } else {
            return selectRowId(selection, args);
        }
    }

    public int selectRowId(String selection, String[] args) {
        int row_id = INVALID_ROW_ID;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cr = db.query(getTableName(), new String[]{OColumn.ROW_ID}, selection, args, null, null, null);
        try {
            if (cr.moveToFirst()) {
                row_id = cr.getInt(0);
            }
        } finally {
            cr.close();
            db.close();
        }
        return row_id;
    }

    public int selectRowId(int server_id) {
        List<ODataRow> rows = select(new String[]{OColumn.ROW_ID}, "id = ?", new String[]{server_id + ""});
        if (rows.size() > 0) {
            return rows.get(0).getInt(OColumn.ROW_ID);
        }
        return INVALID_ROW_ID;
    }

    public int insert(OValues values) {
        Uri uri = mContext.getContentResolver().insert(uri(), values.toContentValues());
        if (uri != null) {
            return Integer.parseInt(uri.getLastPathSegment());
        }
        return INVALID_ROW_ID;
    }

    public List<Integer> insert(List<OValues> valuesCollection) {
        ArrayList<ContentProviderOperation> batches = new ArrayList<>();
        for (OValues values : valuesCollection) {
            ContentProviderOperation.Builder batch = ContentProviderOperation.newInsert(uri());
            batch.withValues(values.toContentValues());
            batch.withYieldAllowed(true);
            batches.add(batch.build());
        }
        if (batches.size() > 0) {
            try {
                ContentProviderResult[] results = mContext.getContentResolver()
                        .applyBatch(BASE_AUTHORITY, batches);
                List<Integer> ids = new ArrayList<>();
                for (ContentProviderResult result : results) {
                    ids.add((Integer.parseInt(result.uri.getLastPathSegment())));
                }
                return ids;
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (OperationApplicationException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    public boolean hasServerRecord(int server_id) {
        int count = count("id = ? ", new String[]{server_id + ""});
        return (count > 0);
    }

    public boolean hasRecord(int row_id) {
        int count = count(OColumn.ROW_ID + " = ? ", new String[]{row_id + ""});
        return (count > 0);
    }


    public HashMap<String, List<Integer>> insertOrUpdate(List<OValues> valuesCollection) {
        List<Integer> localIds = getServerIds();
        ArrayList<ContentProviderOperation> batches = new ArrayList<>();
        List<Integer> updateIds = new ArrayList<>();
        for (OValues values : valuesCollection) {
            ContentProviderOperation.Builder batch = null;
            boolean exists = (values.contains("id") && localIds.contains(values.getInt("id")));
            if (!exists) {
                batch = ContentProviderOperation.newInsert(uri());
            } else {
                int row_id = selectRowId(values.getInt("id"));
                updateIds.add(row_id);
                batch = ContentProviderOperation.newUpdate(uri().buildUpon().appendPath(row_id + "").build());
            }
            batch.withValues(values.toContentValues());
            batch.withYieldAllowed(false);
            batches.add(batch.build());
        }
        if (batches.size() > 0) {
            try {
                ContentProviderResult[] results = mContext.getContentResolver()
                        .applyBatch(BASE_AUTHORITY, batches);
                List<Integer> ids = new ArrayList<>();
                for (ContentProviderResult result : results) {
                    if (result.uri != null) {
                        int new_id = (Integer.parseInt(result.uri.getLastPathSegment()));
                        ids.add(new_id);
                    }
                }
                HashMap<String, List<Integer>> map = new HashMap<>();
                map.put(KEY_INSERT_IDS, ids);
                map.put(KEY_UPDATE_IDS, updateIds);
                return map;
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (OperationApplicationException e) {
                e.printStackTrace();
            }
        }
        return new HashMap<>();
    }

    public int delete(String selection, String[] args) {
        List<ODataRow> records = select(new String[]{"_is_active"}, selection, args);
        int count = 0;
        for (ODataRow row : records) {
            if (row.getBoolean("_is_active")) {
                OValues values = new OValues();
                values.put("_is_active", false);
                update(row.getInt(OColumn.ROW_ID), values);
            } else {
                mContext.getContentResolver().delete(uri(), OColumn.ROW_ID + "= ?",
                        new String[]{row.getString(OColumn.ROW_ID)});
            }
            count++;
        }
        return count;
    }

    public boolean delete(int row_id) {
        int count = mContext.getContentResolver().delete(uri().withAppendedPath(uri(), row_id + ""), null, null);
        return (count > 0) ? true : false;
    }

    public int update(String selection, String[] args, OValues values) {
        return mContext.getContentResolver().update(uri(), values.toContentValues(), selection, args);
    }

    public boolean update(int row_id, OValues values) {
        int count = mContext.getContentResolver().update(uri().withAppendedPath(uri(), row_id + ""),
                values.toContentValues(), null, null);
        return (count > 0) ? true : false;
    }


    public List<ODataRow> query(String query) {
        return query(query, null);
    }

    public List<ODataRow> query(String query, String[] args) {
        List<ODataRow> rows = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cr = db.rawQuery(query, args);
        try {
            if (cr.moveToFirst()) {
                do {
                    rows.add(OCursorUtils.toDatarow(cr));
                } while (cr.moveToNext());
            }
        } finally {
            cr.close();
            db.close();
        }
        return rows;

    }

    public int count(String selection, String[] args) {
        int count = 0;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cr = db.query(getTableName(), new String[]{"count(*)"}, selection, args, null, null, null);
        try {
            cr.moveToFirst();
            count = cr.getInt(0);
        } finally {
            cr.close();
            db.close();
        }
        return count;
    }


    public void storeManyToManyRecord(String column_name, int row_id, List<Integer> relationIds,
                                      Command command)
            throws InvalidObjectException {
        OColumn column = getColumn(column_name);
        if (column != null) {
            OModel rel_model = createInstance(column.getType());
            String table = getTableName() + "_" + rel_model.getTableName() + "_rel";
            String base_column = getTableName() + "_id";
            String rel_column = rel_model.getTableName() + "_id";

            SQLiteDatabase db = getWritableDatabase();
            try {
                switch (command) {
                    case Add:
                        if (relationIds.size() > 0) {
                            for (int id : relationIds) {
                                ContentValues values = new ContentValues();
                                values.put(base_column, row_id);
                                values.put(rel_column, id);
                                values.put("_write_date", ODateUtils.getDate());
                                db.insert(table, null, values);
                            }
                        }
                        break;
                    case Update:
                        break;
                    case Delete:
                        // Deleting records to relation model
                        if (relationIds.size() > 0) {
                            for (int id : relationIds) {
                                db.delete(table, base_column + " = ? AND  " + rel_column
                                        + " = ?", new String[]{row_id + "", id + ""});
                            }
                        }
                        break;
                    case Replace:
                        // Removing old entries
                        String where = base_column + " = ? ";
                        String[] args = new String[]{row_id + ""};
                        db.delete(table, base_column + " = ?", new String[]{row_id + ""});
                        // Creating new entries
                        storeManyToManyRecord(column_name, row_id, relationIds, Command.Add);
                        break;
                }
            } finally {
                db.close();
            }
        } else {
            throw new InvalidObjectException("Column [" + column_name + "] not found in " + getModelName() + " model.");

        }
    }

    public List<ODataRow> selectManyToManyRecords(String[] projection, String column_name, int row_id) {
        OColumn column = getColumn(column_name);
        OModel rel_model = createInstance(column.getType());
        String table = getTableName() + "_" + rel_model.getTableName() + "_rel";
        String base_column = getTableName() + "_id";
        String rel_column = rel_model.getTableName() + "_id";

        // Getting relation table ids
        List<String> ids = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cr = null;
        try {
            cr = db.query(table, new String[]{rel_column}, base_column + "=?",
                    new String[]{row_id + ""}, null, null, null);
            if (cr.moveToFirst()) {
                do {
                    ids.add(cr.getInt(0) + "");
                } while (cr.moveToNext());
            }
        } finally {
            if (cr != null) {
                cr.close();
            }
            db.close();
        }
        return rel_model.select(projection, OColumn.ROW_ID + " IN (" + StringUtils.repeat(" ?, ", ids.size() - 1) + " ?)",
                ids.toArray(new String[ids.size()]));
    }
}
