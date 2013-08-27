package com.openerp.orm;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.openerp.config.ModulesConfig;
import com.openerp.support.Boot;
import com.openerp.support.Module;

public class SQLiteDatabaseHelper extends SQLiteOpenHelper {
	public static int DATABASE_VERSION = 1;
	public static String DATABASE_NAME = "OpenERPSQLite";
	ArrayList<Module> modules = null;
	ArrayList<HashMap<String, String>> systemTables;
	Context context = null;
	ArrayList<SQLStatement> statements = null;

	public SQLiteDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
		this.context = context;
		modules = new ModulesConfig().applicationModules();
		/*
		 * this.statements = new ArrayList<SQLStatement>(); Boot boot = new
		 * Boot(context, modules); this.statements = boot.getAllStatements();
		 */
	}

	public void setSystemTables(ArrayList<HashMap<String, String>> systemTables) {
		this.systemTables = systemTables;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	public void createTable(SQLStatement statement) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL(statement.getStatement());
		db.close();
	}

}
