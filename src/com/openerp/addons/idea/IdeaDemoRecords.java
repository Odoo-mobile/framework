/**
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
package com.openerp.addons.idea;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.openerp.orm.OEDataRow;
import com.openerp.orm.OEM2MIds;
import com.openerp.orm.OEM2MIds.Operation;
import com.openerp.orm.OEValues;
import com.openerp.util.logger.OELog;

public class IdeaDemoRecords {
	IdeaDBHelper ideaDb = null;
	Context mContext = null;

	public IdeaDemoRecords(Context context) {
		mContext = context;
		ideaDb = new IdeaDBHelper(mContext);
	}

	public void createDemoRecords() {
		createIdeaUserTypes();
		createIdeaUsers();
		createIdeaCategory();
		createIdea();
		createIdeaFiles();
		int count = updateRecords();
		OELog.log("Row updated : " + count);
	}

	private void createIdeaUserTypes() {
		IdeaDBHelper.IdeaUserType userType = ideaDb.new IdeaUserType(mContext);
		userType.truncateTable();
		for (int i = 1; i <= 3; i++) {
			OEValues values = new OEValues();
			values.put("id", i);
			values.put("type", "Type " + i);
			long newId = userType.create(values);
			Log.d("IdeaDemoRecords", newId
					+ " Record created for idea.user.type");
		}
	}

	private void createIdeaUsers() {
		IdeaDBHelper.IdeaUsers ideaUsers = ideaDb.new IdeaUsers(mContext);
		ideaUsers.truncateTable();
		for (int i = 1; i <= 5; i++) {
			OEValues values = new OEValues();
			values.put("id", i);
			values.put("name", "User " + i);
			values.put("city", "City " + i);
			values.put("user_type", i); // many to one field
			long newId = ideaUsers.create(values);
			Log.d("IdeaDemoRecords", newId + " Record created for idea.users");
		}
	}

	private void createIdeaCategory() {
		IdeaDBHelper.IdeaCategory ideaCategory = ideaDb.new IdeaCategory(
				mContext);
		ideaCategory.truncateTable();
		for (int i = 1; i <= 3; i++) {
			OEValues values = new OEValues();
			values.put("id", i);
			values.put("name", "Category " + i);
			long newId = ideaCategory.create(values);
			Log.d("IdeaDemoRecords", newId
					+ " Record created for idea.category");
		}
	}

	private void createIdea() {
		ideaDb.truncateTable();
		for (int i = 1; i <= 3; i++) {
			OEValues values = new OEValues();
			values.put("id", i);
			values.put("name", "Idea " + i);
			values.put("description", "Description " + i);
			values.put("category_id", i);
			Integer[] ids = new Integer[] { 1, 2 };
			List<Integer> user_ids = Arrays.asList(ids);
			values.put("user_ids", user_ids);
			long newId = ideaDb.create(values);
			Log.d("IdeaDemoRecords", newId + " Record created for idea.idea");
		}
	}

	private void createIdeaFiles() {
		IdeaDBHelper.IdeaFiles ideaFiles = ideaDb.new IdeaFiles(mContext);
		for (int i = 1; i <= 2; i++) {
			OEValues vals = new OEValues();
			vals.put("id", i);
			vals.put("idea_idea_id", i + 1);
			vals.put("name", "idea file " + i);
			long newId = ideaFiles.create(vals);
			Log.d("IdeaDemoRecords", newId + " Record created for idea.files");
		}
	}

	public void selectAll() {
		for (OEDataRow row : ideaDb.select()) {
			OELog.log("RECORD :::::::::::::::::::::::: " + row.getString("id"));
			OELog.log("name : " + row.getString("name"));
			OELog.log("category : "
					+ row.getM2ORecord("category_id").browse()
							.getString("name"));
			OELog.log("user_ids : "
					+ row.getM2MRecord("user_ids").browseEach().get(0)
							.getM2ORecord("user_type").browse()
							.getString("type"));
			for (OEDataRow file : row.getO2MRecord("idea_files").browseEach()) {
				OELog.log("idea_files:" + file.getString("name"));
				OELog.log("idea_idea_id:" + file.getM2ORecord("idea_idea_id").browse().getString("id"));
			}
		}
	}

	public int updateRecords() {
		int count = 0;
		OEValues values = new OEValues();
		values.put("description", "Updated Description");
		values.put("category_id", 3);
		Integer[] ids = new Integer[] { 3, 4 };
		List<Integer> user_ids = Arrays.asList(ids);
		values.put("user_ids", new OEM2MIds(Operation.APPEND, user_ids));
		count = ideaDb.update(values, "id = ?", new String[] { "2" });
		return count;
	}
}