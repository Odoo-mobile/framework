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

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OBoolean;
import com.odoo.core.orm.fields.types.ODateTime;
import com.odoo.core.orm.fields.types.OInteger;
import com.odoo.core.provider.BaseModelProvider;
import com.odoo.core.support.OUser;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class OModel extends OSQLite {

    public static final String TAG = OModel.class.getSimpleName();

    private Context mContext;
    private OUser mUser;
    private String model_name = null;
    private List<OColumn> mColumns = new ArrayList<>();
    private List<OColumn> mRelationColumns = new ArrayList<>();
    private HashMap<String, Field> mDeclaredFields = new HashMap<>();
    // Base Columns
    OColumn id = new OColumn("ID", OInteger.class).setDefaultValue(0);

    // Local Base columns
    OColumn _id = new OColumn("_ID", OInteger.class).setAutoIncrement().setLocalColumn();
    OColumn _write_date = new OColumn("Local Write Date", ODateTime.class).setLocalColumn();
    OColumn _is_dirty = new OColumn("Dirty record", OBoolean.class).setDefaultValue(false).setLocalColumn();
    OColumn _is_active = new OColumn("Active Record", OBoolean.class).setDefaultValue(true).setLocalColumn();

    public OModel(Context context, String model_name, OUser user) {
        super(context, user);
        mContext = context;
        mUser = user;
        this.model_name = model_name;
    }

    public List<OColumn> getColumns() {
        if (mColumns.size() == 0) {
            prepareFields();
        }
        return mColumns;
    }

    public List<OColumn> getColumns(Boolean local) {
        if (mColumns.size() <= 0) {
            prepareFields();
        }
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
        return mRelationColumns;
    }

    private OColumn getColumn(Field field) {
        OColumn column = null;
        try {
            field.setAccessible(true);
            column = (OColumn) field.get(this);
            column.setName(field.getName());
            //TODO: check for api version
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        return column;
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

    public Uri uri() {
        String authority = "com.odoo.core.provider.content";
        String path = getModelName().toLowerCase(Locale.getDefault());
        return BaseModelProvider.buildURI(authority, path);
    }

}
