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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.odoo.App;
import com.odoo.core.support.OUser;
import com.odoo.datas.OConstants;

import java.util.HashMap;

public class OSQLite extends SQLiteOpenHelper {
    public static final String TAG = OSQLite.class.getSimpleName();
    private Context mContext;
    private OUser mUser = null;
    private App odooApp;

    public OSQLite(Context context, OUser user) {
        super(context, (user != null) ? user.getDBName() : OUser.current(context).getDBName(), null
                , OConstants.DATABASE_VERSION);
        mContext = context;
        odooApp = (App) context.getApplicationContext();
        mUser = (user != null) ? user : OUser.current(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "creating database.");
        ModelRegistryUtils registryUtils = odooApp.getModelRegistry();
        HashMap<String, Class<? extends OModel>> models = registryUtils.getModels();
        OSQLHelper sqlHelper = new OSQLHelper(mContext);

        for (String key : models.keySet()) {
            OModel model = App.getModel(mContext, key, mUser);
            sqlHelper.createStatements(model);
        }
        for (String key : sqlHelper.getStatements().keySet()) {
            String query = sqlHelper.getStatements().get(key);
            db.execSQL(query);
            Log.i(TAG, "Table Created : " + key);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "upgrading database.");
        ModelRegistryUtils registryUtils = odooApp.getModelRegistry();
        HashMap<String, Class<? extends OModel>> models = registryUtils.getModels();
        for (String key : models.keySet()) {
            OModel model = App.getModel(mContext, key, mUser);
            if (model != null) model.onModelUpgrade(db, oldVersion, newVersion);
        }
    }

    public void dropDatabase() {
        if (mContext.deleteDatabase(getDatabaseName())) {
            Log.i(TAG, getDatabaseName() + " database dropped.");
        }
    }

    public String databaseLocalPath() {
        App app = (App) mContext.getApplicationContext();
        return Environment.getDataDirectory().getPath() +
                "/data/" + app.getPackageName() + "/databases/" + getDatabaseName();
    }

    public String getUserAndroidName() {
        return (this.mUser != null) ? this.mUser.getAndroidName() : "";
    }
}
