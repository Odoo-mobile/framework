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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.odoo.base.addons.BaseModels;
import com.odoo.config.Addons;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.support.OUser;
import com.odoo.core.support.addons.OAddon;
import com.odoo.core.support.addons.fragment.IBaseFragment;
import com.odoo.core.utils.OPreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class OSQLite extends SQLiteOpenHelper {
    public static final String TAG = OSQLite.class.getSimpleName();
    public static final String KEY_MODEL_CLASS_REGISTER = "key_model_class_register";
    public static final int DATABASE_VERSION = 1;
    private Context mContext;
    private OUser mUser = null;
    private Addons mAddons;
    private OPreferenceManager mPref;

    public OSQLite(Context context, OUser user) {
        super(context, (user != null) ? user.getDBName() : OUser.current(context).getDBName(), null, DATABASE_VERSION);
        mContext = context;
        mAddons = new Addons();
        mUser = (user != null) ? user : OUser.current(context);
        mPref = new OPreferenceManager(mContext);
        synchronized (this) {
            if (!mPref.getBoolean(KEY_MODEL_CLASS_REGISTER, false)) {
                mPref.setBoolean(KEY_MODEL_CLASS_REGISTER, true);
                // Registering model class paths
                registerModelsClassPath();
            }
        }
    }

    private List<OModel> getModels() {
        List<OModel> models = new ArrayList<>();
        models.addAll(BaseModels.baseModels(mContext));
        for (OAddon addon : mAddons.getAddons()) {
            IBaseFragment fragment = (IBaseFragment) addon.get();
            try {
                Class<Object> model = fragment.database();
                if (model != null) {
                    OModel dbModel = (OModel) model.getConstructor(Context.class, OUser.class)
                            .newInstance(mContext, mUser);
                    models.add(dbModel);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return models;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "creating database.");
        OSQLHelper sqlHelper = new OSQLHelper(mContext);
        // Creating tables
        for (OModel model : getModels()) {
            sqlHelper.createStatements(model);
        }
        for (String key : sqlHelper.getStatements().keySet()) {
            String query = sqlHelper.getStatements().get(key);
            db.execSQL(query);
            Log.i(TAG, "Table Created : " + key);
        }
        registerModels(sqlHelper.getModels());

    }

    private void registerModels(List<String> models) {
        OPreferenceManager mPref = new OPreferenceManager(mContext);
        mPref.putStringSet("models", models);
    }

    private void registerModelsClassPath() {
        OSQLHelper sqlHelper = new OSQLHelper(mContext);
        for (OModel model : sqlHelper.getAllModels(getModels())) {
            String key = model.getModelName();
            String path = model.getClass().getName();
            // Setting class path
            mPref.putString(key, path);
            List<String> server_cols = getColumns(path, true);
            mPref.putStringSet(key + ".server", server_cols);
            List<String> local_cols = getColumns(path, false);
            mPref.putStringSet(key + ".local", local_cols);
        }
    }

    private List<String> getColumns(String model_class, boolean server_columns) {
        List<String> cols = new ArrayList<String>();
        try {
            OModel m = new OModel(mContext, null, null);
            Class<?> cls = Class.forName(model_class);
            OModel model = m.createInstance(cls);
            for (OColumn col : model.getColumns(!server_columns)) {
                cols.add(col.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cols;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "upgrading database.");
        OSQLHelper sqlHelper = new OSQLHelper(mContext);
        for (OModel model : getModels()) {
            sqlHelper.createDropStatements(model);
        }
        for (String key : sqlHelper.getStatements().keySet()) {
            String query = sqlHelper.getStatements().get(key);
            db.execSQL(query);
            Log.i(TAG, "Table dropped " + key);
        }
        onCreate(db);
    }

    public void dropDatabase() {
        if (mContext.deleteDatabase(getDatabaseName())) {
            Log.i(TAG, getDatabaseName() + " database dropped.");
        }
    }
}
