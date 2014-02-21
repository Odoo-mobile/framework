package com.openerp.orm;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.openerp.base.ir.Ir_AttachmentDBHelper;
import com.openerp.base.ir.Ir_model;
import com.openerp.base.mail.MailFollowers;
import com.openerp.base.res.Res_Company;
import com.openerp.base.res.Res_PartnerDBHelper;
import com.openerp.config.ModulesConfig;
import com.openerp.support.Module;
import com.openerp.support.fragment.FragmentHelper;

public class OESQLiteHelper extends SQLiteOpenHelper {

	public static String DATABASE_NAME = "OpenERPSQLite";
	public static int DATABASE_VERSION = 1;
	Context mContext = null;
	ModulesConfig mModuleConfig = null;

	public OESQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
		mModuleConfig = new ModulesConfig();
	}

	public List<OEDBHelper> baseModels() {
		List<OEDBHelper> baseModels = new ArrayList<OEDBHelper>();
		baseModels.add(new Res_PartnerDBHelper(mContext));
		baseModels.add(new Res_Company(mContext));
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

}
