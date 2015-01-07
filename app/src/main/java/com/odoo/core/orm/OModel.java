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
import com.odoo.core.utils.OListUtils;
import com.odoo.core.utils.OPreferenceManager;
import com.odoo.core.utils.StringUtils;
import com.odoo.core.utils.logger.OLog;

import java.io.InvalidObjectException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
    private List<OColumn> mFunctionalColumns = new ArrayList<>();
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
                    // Functional Method
                    Method method = checkForFunctionalColumn(field);
                    if (method != null) {
                        column.setIsFunctionalColumn(true);
                        column.setFunctionalMethod(method);
                        column.setFunctionalStore(checkForFunctionalStore(field));
                        column.setFunctionalStoreDepends(getFunctionalDepends(field));
                        if (!column.canFunctionalStore()) {
                            column.setLocalColumn();
                        }
                    }

                    // Onchange method for column
                    Method onChangeMethod = checkForOnChangeMethod(field);
                    if (onChangeMethod != null) {
                        column.setOnChangeMethod(onChangeMethod);
                        column.setOnChangeBGProcess(checkForOnChangeBGProcess(field));
                    }

                    // domain filter on column
                    column.setHasDomainFilterColumn(isDomainFilterColumn(field));
                    return column;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        }
        return null;
    }

    private boolean isDomainFilterColumn(Field field) {
        Annotation annotation = field.getAnnotation(Odoo.hasDomainFilter.class);
        if (annotation != null) {
            Odoo.hasDomainFilter domainFilter = (Odoo.hasDomainFilter) annotation;
            return domainFilter.checkDomainRuntime();
        }
        return false;
    }

    private Boolean checkForOnChangeBGProcess(Field field) {
        Annotation annotation = field.getAnnotation(Odoo.onChange.class);
        if (annotation != null) {
            Odoo.onChange onChange = (Odoo.onChange) annotation;
            return onChange.bg_process();
        }
        return false;
    }

    private Method checkForOnChangeMethod(Field field) {
        Annotation annotation = field.getAnnotation(Odoo.onChange.class);
        if (annotation != null) {
            Odoo.onChange onChange = (Odoo.onChange) annotation;
            String method_name = onChange.method();
            try {
                return getClass().getMethod(method_name, ODataRow.class);
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "No Such Method: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Check for functional store.
     *
     * @param field the field
     * @return the boolean
     */
    public Boolean checkForFunctionalStore(Field field) {
        Annotation annotation = field.getAnnotation(Odoo.Functional.class);
        if (annotation != null) {
            Odoo.Functional functional = (Odoo.Functional) annotation;
            return functional.store();
        }
        return false;
    }


    /**
     * Gets the functional depends.
     *
     * @param field the field
     * @return the functional depends
     */
    public String[] getFunctionalDepends(Field field) {
        Annotation annotation = field.getAnnotation(Odoo.Functional.class);
        if (annotation != null) {
            Odoo.Functional functional = (Odoo.Functional) annotation;
            return functional.depends();
        }
        return null;
    }

    private Method checkForFunctionalColumn(Field field) {
        Annotation annotation = field.getAnnotation(Odoo.Functional.class);
        if (annotation != null) {
            Odoo.Functional functional = (Odoo.Functional) annotation;
            String method_name = functional.method();
            try {
                if (functional.store())
                    return getClass().getMethod(method_name, OValues.class);
                else
                    return getClass().getMethod(method_name, ODataRow.class);
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "No Such Method: " + e.getMessage());
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
                        if (column.isFunctionalColumn()) {
                            if (column.canFunctionalStore()) {
                                mColumns.add(column);
                            }
                            mFunctionalColumns.add(column);
                        } else {
                            mColumns.add(column);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<OColumn> getFunctionalColumns() {
        if (mColumns.size() <= 0)
            prepareFields();
        return mFunctionalColumns;
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
        String[] allProjection = projection;
        if (allProjection == null) {
            allProjection = projection();
        } else {
            for (String col : projection) {
                OColumn column = getColumn(col);
                if (column.isFunctionalColumn() && column.canFunctionalStore()) {
                    names.add(column.getName());
                }
            }
        }
        names.addAll(Arrays.asList(allProjection));
        names.addAll(Arrays.asList(new String[]{OColumn.ROW_ID, "id", "_write_date", "_is_dirty", "_is_active"}));
        return names.toArray(new String[names.size()]);
    }

    public String[] projection(Boolean onlyServerColumns) {
        List<String> names = new ArrayList<>();
        for (OColumn column : getColumns(false)) {
            if (column.getRelationType() == null || column.canFunctionalStore()) {
                names.add(column.getName());
            } else if (column.getRelationType() == OColumn.RelationType.ManyToOne) {
                names.add(column.getName());
            }
        }
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

    public boolean allowUpdateRecordOnServer() {
        return true;
    }

    public boolean allowCreateRecordOnServer() {
        return true;
    }

    public boolean allowDeleteRecordOnServer() {
        return true;
    }

    public boolean allowDeleteRecordInLocal() {
        return true;
    }

    // Database Operations
    public ODataRow browse(int row_id) {
        return browse(null, row_id);
    }

    public ODataRow browse(String[] projection, int row_id) {
        List<ODataRow> rows = select(projection, OColumn.ROW_ID + " = ?", new String[]{row_id + ""});
        if (rows.size() > 0) {
            return rows.get(0);
        }
        return null;
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
                for (OColumn column : getFunctionalColumns(projection)) {
                    List<String> depends = column.getFunctionalStoreDepends();
                    OLog.log(column.getName() + ":" + depends);
                    if (depends != null && depends.size() > 0) {
                        ODataRow values = new ODataRow();
                        for (String depend : depends) {
                            if (row.contains(depend)) {
                                values.put(depend, row.get(depend));
                            }
                        }
                        if (values.size() == depends.size()) {
                            Object value = getFunctionalMethodValue(column, values);
                            row.put(column.getName(), value);
                        }
                    }
                }
                rows.add(row);
            } while (cr.moveToNext());
        }

        cr.close();
        return rows;
    }

    public Object getFunctionalMethodValue(OColumn column, Object record) {
        if (column.isFunctionalColumn()) {
            Method method = column.getFunctionalMethod();
            OModel model = this;
            try {
                return method.invoke(model, new Object[]{record});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private List<OColumn> getFunctionalColumns(String[] projection) {
        List<OColumn> cols = new ArrayList<>();
        if (projection != null) {
            for (String key : projection) {
                OColumn column = getColumn(key);
                if (column.isFunctionalColumn() && !column.canFunctionalStore()) {
                    cols.add(column);
                }
            }
        } else {
            for (OColumn column : getFunctionalColumns()) {
                if (!column.canFunctionalStore())
                    cols.add(column);
            }
        }
        return cols;
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

    public boolean isServerRecordDirty(int server_id) {
        int count = count("id = ? and _is_dirty = ?", new String[]{server_id + "", "true"});
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

    public int deleteRecords(List<Integer> serverIds, boolean permanently) {
        String selection = "id IN (" + StringUtils.repeat("?, ", serverIds.size() - 1) + " ?)";
        String[] args = OListUtils.toStringList(serverIds).toArray(new String[serverIds.size()]);
        if (permanently) {
            return delete(selection, args, true);
        } else {
            OValues values = new OValues();
            values.put("_is_active", "false");
            return update(selection, args, values);
        }
    }

    public int delete(String selection, String[] args) {
        return delete(selection, args, false);
    }

    public int delete(String selection, String[] args, boolean permanently) {
        int count = 0;
        if (permanently) {
            count = mContext.getContentResolver().delete(uri(), selection, args);
        } else {
            List<ODataRow> records = select(new String[]{"_is_active"}, selection, args);
            for (ODataRow row : records) {
                if (row.getBoolean("_is_active")) {
                    OValues values = new OValues();
                    values.put("_is_active", "false");
                    update(row.getInt(OColumn.ROW_ID), values);
                }
                count++;
            }
        }
        return count;
    }

    public boolean delete(int row_id) {
        return delete(row_id, false);
    }

    public boolean delete(int row_id, boolean permanently) {
        int count = 0;
        if (permanently)
            count = mContext.getContentResolver().delete(uri().withAppendedPath(uri(), row_id + ""), null, null);
        else {
            OValues values = new OValues();
            values.put("_is_active", "false");
            update(row_id, values);
            count++;
        }
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
        }
        db.close();
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
