package com.openerp.orm;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.openerp.base.ir.Ir_AttachmentDBHelper;
import com.openerp.base.ir.Ir_model;
import com.openerp.base.mail.MailFollowers;
import com.openerp.base.res.ResPartnerDB;
import com.openerp.base.res.ResCompanyDB;
import com.openerp.config.ModulesConfig;
import com.openerp.support.Module;
import com.openerp.support.fragment.FragmentHelper;

public class OESQLiteHelper extends SQLiteOpenHelper {

	public static final String TAG = OESQLiteHelper.class.getSimpleName();

	public static final String DATABASE_NAME = "OpenERPSQLite.db";
	public static final int DATABASE_VERSION = 1;
	Context mContext = null;
	ModulesConfig mModuleConfig = null;
	List<String> mDBTables = new ArrayList<String>();

	public OESQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
		mModuleConfig = new ModulesConfig();
	}

	public List<OEDBHelper> baseModels() {
		List<OEDBHelper> baseModels = new ArrayList<OEDBHelper>();
		baseModels.add(new ResPartnerDB(mContext));
		baseModels.add(new ResCompanyDB(mContext));
		baseModels.add(new Ir_model(mContext));
		baseModels.add(new Ir_AttachmentDBHelper(mContext));
		baseModels.add(new MailFollowers(mContext));
		return baseModels;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		SQLHelper sqlHelper = new SQLHelper();
		for (OEDBHelper db_helper : baseModels()) {
			List<String> sqlQueries = sqlHelper.createTable(db_helper);
			for (String query : sqlQueries) {
				db.execSQL(query);
			}
		}
		for (Module module : mModuleConfig.modules()) {
			FragmentHelper model = (FragmentHelper) module.getModuleInstance();
			OEDBHelper model_db = (OEDBHelper) model.databaseHelper(mContext);
			List<String> sqlQueries = sqlHelper.createTable(model_db);
			for (String query : sqlQueries) {
				db.execSQL(query);
			}
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		SQLHelper sqlHelper = new SQLHelper();
		for (Module module : mModuleConfig.modules()) {
			FragmentHelper model = (FragmentHelper) module.getModuleInstance();
			OEDBHelper model_db = (OEDBHelper) model.databaseHelper(mContext);
			List<String> sqlQueries = sqlHelper.dropTable(model_db);
			for (String query : sqlQueries) {
				db.execSQL(query);
			}
		}
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
			total = db.delete(table, "oea_name = ?",
					new String[] { account_name });
			Log.v(TAG, total + " cleaned from " + table);
		}
		db.close();
		Log.i(TAG, account_name + " records cleaned");
		return true;
	}
}
