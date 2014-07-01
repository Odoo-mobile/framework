package com.odoo.orm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.odoo.base.ir.IrAttachment;
import com.odoo.base.ir.IrModel;
import com.odoo.base.mail.MailFollowers;
import com.odoo.base.res.ResPartner;
import com.odoo.config.OModules;
import com.odoo.support.OModule;
import com.odoo.util.PreferenceManager;

public class OSQLiteHelper extends SQLiteOpenHelper {
	public static final String TAG = OSQLiteHelper.class.getSimpleName();

	public static final String DATABASE_NAME = "OdooSQLite.db";
	public static final int DATABASE_VERSION = 1;
	Context mContext = null;
	OModules mModules = null;
	List<String> mDBTables = new ArrayList<String>();

	public OSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
		mModules = new OModules();
	}

	public List<OModel> baseModels() {
		List<OModel> models = new ArrayList<OModel>();
		models.add(new IrModel(mContext));
		models.add(new ResPartner(mContext));
		models.add(new IrAttachment(mContext));
		models.add(new MailFollowers(mContext));
		return models;
	}

	public List<OModel> moduleModels() {
		List<OModel> models = new ArrayList<OModel>();
		for (OModule module : mModules.getModules()) {
			models.add(module.getModel(mContext));
		}
		return models;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		OSQLHelper sql = new OSQLHelper(mContext);
		for (OModel model : baseModels()) {
			sql.createStatements(model);
		}
		for (OModule module : mModules.getModules()) {
			sql.createStatements(module.getModel(mContext));
		}
		for (String query : sql.getStatements()) {
			db.execSQL(query);
		}
		registerModels(sql.getModelName());
		registerModelsClassPath(sql.getModelClassPath());
	}

	private void registerModels(List<String> models) {
		PreferenceManager pfManager = new PreferenceManager(mContext);
		pfManager.putStringSet("models", models);
	}

	private void registerModelsClassPath(HashMap<String, String> modelClassPath) {
		PreferenceManager pfManager = new PreferenceManager(mContext);
		for (String key : modelClassPath.keySet()) {
			// Setting class path
			pfManager.putString(key, modelClassPath.get(key));

			List<String> server_cols = getColumns(modelClassPath.get(key)
					.toString(), true);
			pfManager.putStringSet(key + ".server", server_cols);
			List<String> local_cols = getColumns(modelClassPath.get(key)
					.toString(), false);
			pfManager.putStringSet(key + ".local", local_cols);

		}
	}

	private List<String> getColumns(String model_class, boolean server_columns) {
		List<String> cols = new ArrayList<String>();
		try {
			OModel m = new OModel(mContext, null);
			Class<?> cls = Class.forName(model_class);
			OModelHelper model = m.createInstance(cls);
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
		for (OModel model : baseModels()) {
			OSQLHelper sql = new OSQLHelper(mContext);
			sql.createDropStatements(model);
			for (String s : sql.getStatements()) {
				db.execSQL(s);
			}
		}
		// Recreating tables
		onCreate(db);
	}

	private void setDBTables() {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cr = db.query("sqlite_master", new String[] { "name" },
				"type = ?", new String[] { "table" }, null, null, null);
		if (cr.moveToFirst()) {
			do {
				String table = cr.getString(0);
				if (!table.equals("android_metadata")
						&& !table.equals("sqlite_sequence")) {
					mDBTables.add(table);
				}
			} while (cr.moveToNext());
		}
		cr.close();
		db.close();
	}

	public boolean hasTable(String table_or_model) {
		if (mDBTables.size() == 0)
			setDBTables();
		String table = table_or_model;
		if (table_or_model.contains(".")) {
			table = table_or_model.replaceAll("\\.", "_");
		}
		if (mDBTables.contains(table)) {
			return true;
		}
		return false;
	}

	public boolean cleanUserRecords(String account_name) {
		Log.d(TAG, "cleanUserRecords()");
		if (mDBTables.size() == 0)
			setDBTables();
		SQLiteDatabase db = getWritableDatabase();
		for (String table : mDBTables) {
			int total = 0;
			total = db.delete(table, "odoo_name = ?",
					new String[] { account_name });
			Log.v(TAG, total + " cleaned from " + table);
		}
		db.close();
		Log.i(TAG, account_name + " records cleaned");
		return true;
	}
}
