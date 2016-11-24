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
 * Created on 30/12/14 3:31 PM
 */
package com.odoo.core.orm;

import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.odoo.App;
import com.odoo.BuildConfig;
import com.odoo.base.addons.ir.IrModel;
import com.odoo.core.auth.OdooAccountManager;
import com.odoo.core.orm.annotation.Odoo;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OBoolean;
import com.odoo.core.orm.fields.types.ODateTime;
import com.odoo.core.orm.fields.types.OInteger;
import com.odoo.core.orm.fields.types.OSelection;
import com.odoo.core.orm.provider.BaseModelProvider;
import com.odoo.core.rpc.helper.ODomain;
import com.odoo.core.rpc.helper.OdooVersion;
import com.odoo.core.rpc.listeners.IModuleInstallListener;
import com.odoo.core.service.ISyncServiceListener;
import com.odoo.core.service.OSyncAdapter;
import com.odoo.core.support.OUser;
import com.odoo.core.support.sync.SyncUtils;
import com.odoo.core.utils.OCursorUtils;
import com.odoo.core.utils.ODateUtils;
import com.odoo.core.utils.OListUtils;
import com.odoo.core.utils.OStorageUtils;
import com.odoo.core.utils.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;


public class OModel implements ISyncServiceListener {

    public static final String TAG = OModel.class.getSimpleName();
    private String BASE_AUTHORITY = BuildConfig.APPLICATION_ID + ".core.provider.content";
    public static final int INVALID_ROW_ID = -1;
    private OSQLite sqLite = null;
    private Context mContext;
    private OUser mUser;
    private String model_name = null;
    private List<OColumn> mColumns = new ArrayList<>();
    private List<OColumn> mRelationColumns = new ArrayList<>();
    private List<OColumn> mFunctionalColumns = new ArrayList<>();
    private HashMap<String, Field> mDeclaredFields = new HashMap<>();
    private OdooVersion mOdooVersion = null;
    private String default_name_column = "name";
    private boolean hasMailChatter = false;

    // Base Columns
    OColumn id = new OColumn("ID", OInteger.class).setDefaultValue(0);
    @Odoo.api.v8
    @Odoo.api.v9
    @Odoo.api.v10
    @Odoo.api.v11alpha
    public OColumn create_date = new OColumn("Created On", ODateTime.class);

    @Odoo.api.v8
    @Odoo.api.v9
    @Odoo.api.v10
    @Odoo.api.v11alpha
    public OColumn write_date = new OColumn("Last Updated On", ODateTime.class);

    // Local Base columns
    OColumn _id = new OColumn("_ID", OInteger.class).setAutoIncrement().setLocalColumn();
    OColumn _write_date = new OColumn("Local Write Date", ODateTime.class).setLocalColumn();
    OColumn _is_dirty = new OColumn("Dirty record", OBoolean.class).setDefaultValue(false).setLocalColumn();
    OColumn _is_active = new OColumn("Active Record", OBoolean.class).setDefaultValue(true).setLocalColumn();

    public OModel(Context context, String model_name, OUser user) {
        mContext = context;
        mUser = (user == null) ? OUser.current(context) : user;
        this.model_name = model_name;
        if (mUser != null) {
            mOdooVersion = mUser.getOdooVersion();

            sqLite = App.getSQLite(mUser.getAndroidName());
            if (sqLite == null) {
                sqLite = new OSQLite(mContext, mUser);
                App.setSQLite(mUser.getAndroidName(), sqLite);
            }
        }
    }

    public SQLiteDatabase getReadableDatabase() {
        return sqLite.getReadableDatabase();
    }

    public SQLiteDatabase getWritableDatabase() {
        return sqLite.getWritableDatabase();
    }

    public String getDatabaseName() {
        return sqLite.getDatabaseName();
    }

    public void onModelUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Override in model
    }

    public Context getContext() {
        return mContext;
    }

    public void close() {
        // Any operation when closing database
    }

    public void setDefaultNameColumn(String nameColumn) {
        default_name_column = nameColumn;
    }

    public String getDefaultNameColumn() {
        return default_name_column;
    }

    public OModel setModelName(String model_name) {
        this.model_name = model_name;
        return this;
    }

    public boolean hasMailChatter() {
        return hasMailChatter;
    }

    public void setHasMailChatter(boolean hasMailChatter) {
        this.hasMailChatter = hasMailChatter;
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
        OColumn column;
        if (field != null) {
            try {
                field.setAccessible(true);
                column = (OColumn) field.get(this);
                if (column.getName() == null)
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

                    // Adding sync column name (if local column name is different)
                    String syncColumnName = checkForSyncColumnName(field);
                    column.setSyncColumnName(syncColumnName);

                    // domain filter on column
                    column.setHasDomainFilterColumn(field.getAnnotation(Odoo.Domain.class) != null);
                    if (column.hasDomainFilterColumn()) {
                        column.setDomainFilter(field.getAnnotation(Odoo.Domain.class));
                    }
                    return column;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        }
        return null;
    }

    private Boolean checkForOnChangeBGProcess(Field field) {
        Annotation annotation = field.getAnnotation(Odoo.onChange.class);
        if (annotation != null) {
            Odoo.onChange onChange = (Odoo.onChange) annotation;
            return onChange.bg_process();
        }
        return false;
    }

    private String checkForSyncColumnName(Field field) {
        Annotation annotation = field.getAnnotation(Odoo.SyncColumnName.class);
        if (annotation != null) {
            Odoo.SyncColumnName syncColumnName = (Odoo.SyncColumnName) annotation;
            if (syncColumnName.value().length() != 0) {
                return syncColumnName.value();
            }
        }
        return null;
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
                        switch (mOdooVersion.getVersionNumber()) {
                            case 11:
                                if (type.isAssignableFrom(Odoo.api.v11alpha.class)) {
                                    version++;
                                }
                                break;
                            case 10:
                                if (type.isAssignableFrom(Odoo.api.v10.class)) {
                                    version++;
                                }
                                break;
                            case 9:
                                if (type.isAssignableFrom(Odoo.api.v9.class)) {
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
                            || type.isAssignableFrom(Odoo.Domain.class)
                            || type.isAssignableFrom(Odoo.SyncColumnName.class)) {
                        version++;
                    }
                }
                return (version > 0);
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
                String name = field.getName();
                try {
                    OColumn column = getColumn(field);
                    if (column != null) {
                        name = column.getName();
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
                mDeclaredFields.put(name, field);
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

    public List<OColumn> getManyToManyColumns(OColumn column, OModel relation_model) {
        List<OColumn> cols = new ArrayList<>();
        _write_date.setName("_write_date");
        cols.add(_write_date);
        _is_dirty.setName("_is_dirty");
        cols.add(_is_dirty);
        _is_active.setName("_is_active");
        cols.add(_is_active);

        OColumn base_id = new OColumn("Base Id", OInteger.class);
        base_id.setName(column.getRelBaseColumn() != null ? column.getRelBaseColumn()
                : getTableName() + "_id");
        cols.add(base_id);
        OColumn relation_id = new OColumn("Relation Id", OInteger.class);
        relation_id.setName(column.getRelRelationColumn() != null ?
                column.getRelRelationColumn() : relation_model.getTableName() + "_id");
        cols.add(relation_id);
        return cols;
    }

    public OModel createInstance(Class<?> type) {
        try {
            Constructor<?> constructor = type.getConstructor(Context.class, OUser.class);
            return (OModel) constructor.newInstance(mContext, mUser);
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
        OUser user = OdooAccountManager.getDetails(context, username);
        return App.getModel(context, model_name, user);
    }

    public String authority() {
        return BASE_AUTHORITY;
    }

    public Uri buildURI(String authority) {
        BASE_AUTHORITY = authority;
        String path = getModelName().toLowerCase(Locale.getDefault());
        return BaseModelProvider.buildURI(BASE_AUTHORITY, path, mUser.getAndroidName());
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
        names.addAll(Arrays.asList(OColumn.ROW_ID, "id", "_write_date", "_is_dirty", "_is_active"));
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

    public String getLabel(String column, String key) {
        OColumn col = getColumn(column);
        if (col.getType().isAssignableFrom(OSelection.class)) {
            return col.getSelectionMap().get(key);
        }
        return "false";
    }

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
            String date = records.get(0).getString("last_synced");
            Date write_date = ODateUtils.createDateObject(date, ODateUtils.DEFAULT_FORMAT, true);
            Calendar cal = Calendar.getInstance();
            cal.setTime(write_date);
            /*
                Fixed for Postgres SQL
                It stores milliseconds so comparing date wrong. 
             */
            cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) + 2);
            write_date = cal.getTime();
            return ODateUtils.getDate(write_date, ODateUtils.DEFAULT_FORMAT);
        }
        return null;
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
        try {
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
        } finally {
            if (cr != null) cr.close();
        }
        return rows;
    }

    public Object getFunctionalMethodValue(OColumn column, Object record) {
        if (column.isFunctionalColumn()) {
            Method method = column.getFunctionalMethod();
            OModel model = this;
            try {
                return method.invoke(model, record);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public Object getOnChangeMethodValue(OColumn column, Object record) {
        Method method = column.getOnChangeMethod();
        OModel model = this;
        try {
            return method.invoke(model, record);
        } catch (Exception e) {
            e.printStackTrace();
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

    public int insertOrUpdate(int serverId, OValues values) {
        if (hasServerRecord(serverId)) {
            int row_id = selectRowId(serverId);
            update(row_id, values);
            return row_id;
        } else {
            return insert(values);
        }
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
        }
        return row_id;
    }

    public int selectServerId(int row_id) {
        return browse(row_id).getInt("id");
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
            count = mContext.getContentResolver().delete(Uri.withAppendedPath(uri(), row_id + ""), null, null);
        else {
            OValues values = new OValues();
            values.put("_is_active", "false");
            update(row_id, values);
            count++;
        }
        return count > 0;
    }

    public int update(String selection, String[] args, OValues values) {
        return mContext.getContentResolver().update(uri(), values.toContentValues(), selection, args);
    }

    public boolean update(int row_id, OValues values) {
        int count = mContext.getContentResolver().update(Uri.withAppendedPath(uri(), row_id + ""),
                values.toContentValues(), null, null);
        return count > 0;
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
        }
        return count;
    }


    /**
     * Handle record values for insert, update, delete operation with relation columns
     * Each record have different behaviour, appending, deleting, unlink reference and
     * replacing with new list
     *
     * @param record_id Main record id on which relation values affected
     * @param column    column object of the record (for relation column only)
     * @param values    values list with commands (@see RelCommands)
     */
    public void handleRelationValues(int record_id, OColumn column, RelValues values) {
        OModel relModel = createInstance(column.getType());
        HashMap<RelCommands, List<Object>> columnValues = values.getColumnValues();
        for (RelCommands commands : columnValues.keySet()) {
            switch (column.getRelationType()) {
                case OneToMany:
                    handleOneToManyRecords(column, commands, relModel, record_id, columnValues);
                    break;
                case ManyToMany:
                    handleManyToManyRecords(column, commands, relModel, record_id, columnValues);
                    break;
            }
        }
    }

    private void handleOneToManyRecords(OColumn column, RelCommands commands, OModel relModel,
                                        int record_id, HashMap<RelCommands, List<Object>> values) {
        if (commands == RelCommands.Replace) {
            // Force to unlink record even no any other record values available.
            OValues old_values = new OValues();
            old_values.put(column.getRelatedColumn(), 0);
            int count = relModel.update(column.getRelatedColumn() + " = ?", new String[]{record_id + ""},
                    old_values);
            Log.i(TAG, String.format("#%d references removed " + relModel.getModelName(), count));
        }
        for (Object rowObj : values.get(commands)) {
            switch (commands) {
                case Append:
                    OValues value;
                    if (rowObj instanceof OValues) {
                        value = (OValues) rowObj;
                        value.put(column.getRelatedColumn(), record_id);
                        relModel.insert(value);
                    } else {
                        int rec_id = (int) rowObj;
                        value = new OValues();
                        value.put(column.getRelatedColumn(), record_id);
                        relModel.update(rec_id, value);
                    }
                    break;
                case Replace:
                    if (rowObj instanceof OValues) {
                        value = (OValues) rowObj;
                        value.put(column.getRelatedColumn(), record_id);
                        relModel.insert(value);
                    } else {
                        int rec_id = (int) rowObj;
                        value = new OValues();
                        value.put(column.getRelatedColumn(), record_id);
                        relModel.update(rec_id, value);
                    }
                    break;
                case Delete:
                    relModel.delete((int) rowObj);
                    break;
                case Unlink:
                    // Removing all older references
                    OValues old_update = new OValues();
                    old_update.put(column.getRelatedColumn(), 0);
                    relModel.update((int) rowObj, old_update);
                    break;
            }
        }
    }

    private void handleManyToManyRecords(OColumn column, RelCommands command, OModel relModel,
                                         int record_id, HashMap<RelCommands, List<Object>> values) {

        String table = column.getRelTableName() != null ? column.getRelTableName() :
                getTableName() + "_" + relModel.getTableName() + "_rel";
        String base_column = column.getRelBaseColumn() != null ? column.getRelBaseColumn() :
                getTableName() + "_id";
        String rel_column = column.getRelRelationColumn() != null ? column.getRelRelationColumn() :
                relModel.getTableName() + "_id";
        SQLiteDatabase db = getWritableDatabase();
        switch (command) {
            case Append:
                // Inserting each relation id with base record id to relation table
                List<Object> append_items = values.get(command);
                StringBuilder sql = new StringBuilder("INSERT INTO ").append(table)
                        .append(" (")
                        .append(base_column).append(", ")
                        .append(rel_column)
                        .append(", _write_date )").append(" VALUES ");
                for (Object obj : append_items) {
                    int id;
                    if (obj instanceof OValues) {
                        Log.d(TAG, "creating quick record for many to many ");
                        id = relModel.insert((OValues) obj);
                    } else id = (int) obj;
                    sql.append(" (").append(record_id).append(",")
                            .append(id).append(", ")
                            .append("'").append(ODateUtils.getUTCDate()).append("'), ");
                }
                String statement = sql.substring(0, sql.length() - 2);
                db.execSQL(statement);
                break;
            case Replace:
                List<Object> ids = values.get(command);
                // Unlink records
                values.put(RelCommands.Unlink, ids);
                handleManyToManyRecords(column, RelCommands.Unlink, relModel, record_id, values);

                // Appending record in relation with base record
                values.put(RelCommands.Append, ids);
                handleManyToManyRecords(column, RelCommands.Append, relModel, record_id, values);
                break;
            case Delete:
                // Unlink relation with base record and removing relation records
                values.put(RelCommands.Unlink, values.get(command));
                handleManyToManyRecords(column, RelCommands.Unlink, relModel, record_id, values);

                // Deleting master record from relation model with given ids
                String deleteSql = "DELETE FROM " + relModel.getTableName() + " WHERE " + OColumn.ROW_ID + " IN (" +
                        TextUtils.join(",", values.get(command)) + ")";
                db.execSQL(deleteSql);
                break;
            case Unlink:
                // Unlink relation with base record
                String unlinkSQL = "DELETE FROM " + table + " WHERE " + base_column + " = " + record_id + " AND " + rel_column + " IN (" +
                        TextUtils.join(",", values.get(command)) + ")";
                db.execSQL(unlinkSQL);
                break;
        }
        values.remove(command);
        db.close();
    }

    public List<ODataRow> selectManyToManyRecords(String[] projection, String column_name, int row_id) {
        OColumn column = getColumn(column_name);
        OModel rel_model = createInstance(column.getType());
        String table = column.getRelTableName() != null ? column.getRelTableName() :
                getTableName() + "_" + rel_model.getTableName() + "_rel";
        String base_column = column.getRelBaseColumn() != null ? column.getRelBaseColumn() :
                getTableName() + "_id";
        String rel_column = column.getRelRelationColumn() != null ? column.getRelRelationColumn() :
                rel_model.getTableName() + "_id";

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
        }
        List<ODataRow> data = rel_model.select(projection, OColumn.ROW_ID + " IN (" + StringUtils.repeat(" ?, ", ids.size() - 1) + " ?)",
                ids.toArray(new String[ids.size()]));
        rel_model.close();
        return data;
    }

    public ServerDataHelper getServerDataHelper() {
        return new ServerDataHelper(mContext, this, getUser());
    }

    public String getName(int row_id) {
        ODataRow row = browse(row_id);
        if (row != null) {
            return row.getString("name");
        }
        return "false";
    }

    public void quickSyncRecords(ODomain domain) {
        OSyncAdapter syncAdapter = new OSyncAdapter(mContext, getClass(), null, true);
        syncAdapter.setModel(this);
        syncAdapter.setDomain(domain);
        syncAdapter.checkForWriteCreateDate(false);
        syncAdapter.onPerformSync(getUser().getAccount(), null, authority(), null, new SyncResult());
    }

    public ODataRow quickCreateRecord(ODataRow record) {
        OSyncAdapter syncAdapter = new OSyncAdapter(mContext, getClass(), null, true);
        syncAdapter.setModel(this);
        ODomain domain = new ODomain();
        domain.add("id", "=", record.getFloat("id").intValue());
        syncAdapter.setDomain(domain);
        syncAdapter.checkForWriteCreateDate(false);
        syncAdapter.onPerformSync(getUser().getAccount(), null, authority(), null, new SyncResult());
        return browse(null, "id = ?", new String[]{record.getString("id")});
    }

    public ODataRow countGroupBy(String column, String group_by, String having, String[] args) {
        String sql = "select count(*) as total, " + column;
        sql += " from " + getTableName() + " group by " + group_by + " having " + having;
        List<ODataRow> data = query(sql, args);
        if (data.size() > 0) {
            return data.get(0);
        } else {
            ODataRow row = new ODataRow();
            row.put("total", 0);
            return row;
        }
    }

    public void isInstalledOnServer(final String module_name, IModuleInstallListener callback) {
        App app = (App) mContext.getApplicationContext();
        app.getOdoo(getUser()).installedOnServer(module_name, new IModuleInstallListener() {
            @Override
            public void installedOnServer(boolean isInstalled) {
                IrModel model = new IrModel(mContext, getUser());
                OValues values = new OValues();
                values.put("id", 0);
                values.put("name", module_name);
                values.put("state", "installed");
                model.insertOrUpdate("name = ?", new String[]{module_name}, values);
            }
        });
    }

    public String getDatabaseLocalPath() {
        return sqLite.databaseLocalPath();
    }

    public void exportDB() {
        FileChannel source;
        FileChannel destination;
        String currentDBPath = getDatabaseLocalPath();
        String backupDBPath = OStorageUtils.getDirectoryPath("file")
                + "/" + getDatabaseName();
        File currentDB = new File(currentDBPath);
        File backupDB = new File(backupDBPath);
        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            String subject = "Database Export: " + getDatabaseName();
            Uri uri = Uri.fromFile(backupDB);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.setType("message/rfc822");
            mContext.startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSyncStarted() {
        // Will be over ride by extending model
    }

    @Override
    public void onSyncFinished() {
        // Will be over ride by extending model
    }

    @Override
    public void onSyncFailed() {
        // Will be over ride by extending model
    }

    @Override
    public void onSyncTimedOut() {
        // Will be over ride by extending model
    }

    public SyncUtils sync() {
        return SyncUtils.get(mContext);
    }
}