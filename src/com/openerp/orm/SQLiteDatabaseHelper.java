/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http://www.openerp.com>)
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * 
 */

package com.openerp.orm;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.openerp.config.ModulesConfig;
import com.openerp.support.Module;

// TODO: Auto-generated Javadoc
/**
 * The Class SQLiteDatabaseHelper.
 */
public class SQLiteDatabaseHelper extends SQLiteOpenHelper {

	/** The database version. */
	public static int DATABASE_VERSION = 1;

	/** The database name. */
	public static String DATABASE_NAME = "OpenERPSQLite";

	/** The modules. */
	ArrayList<Module> modules = null;

	/** The system tables. */
	ArrayList<HashMap<String, String>> systemTables;

	/** The context. */
	Context context = null;

	/** The statements. */
	ArrayList<SQLStatement> statements = null;

	/**
	 * Instantiates a new sQ lite database helper.
	 * 
	 * @param context
	 *            the context
	 */
	public SQLiteDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
		this.context = context;
		modules = new ModulesConfig().modules();
		/*
		 * this.statements = new ArrayList<SQLStatement>(); Boot boot = new
		 * Boot(context, modules); this.statements = boot.getAllStatements();
		 */
	}

	/**
	 * Sets the system tables.
	 * 
	 * @param systemTables
	 *            the system tables
	 */
	public void setSystemTables(ArrayList<HashMap<String, String>> systemTables) {
		this.systemTables = systemTables;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
	 * .SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
	 * .SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	/**
	 * Creates the table.
	 * 
	 * @param statement
	 *            the statement
	 */
	public void createTable(SQLStatement statement) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL(statement.getStatement());
		db.close();
	}

}
