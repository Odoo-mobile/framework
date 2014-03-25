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

package com.openerp.addons.idea;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.openerp.orm.OEColumn;
import com.openerp.orm.OEDBHelper;
import com.openerp.orm.OEDatabase;
import com.openerp.orm.OEFields;

public class IdeaDBHelper extends OEDatabase {
	Context mContext = null;

	public IdeaDBHelper(Context context) {
		super(context);
		mContext = context;
	}

	class IdeaCategory extends OEDatabase implements OEDBHelper {
		Context mContext = null;

		public IdeaCategory(Context context) {
			super(context);
			mContext = context;

		}

		@Override
		public String getModelName() {
			return "idea.category";
		}

		@Override
		public List<OEColumn> getModelColumns() {
			List<OEColumn> cols = new ArrayList<OEColumn>();
			cols.add(new OEColumn("name", "Name", OEFields.varchar(50)));
			return cols;
		}

	}

	class IdeaUsers extends OEDatabase implements OEDBHelper {
		Context mContext = null;

		public IdeaUsers(Context context) {
			super(context);
			mContext = context;
		}

		@Override
		public String getModelName() {
			return "idea.users";
		}

		@Override
		public List<OEColumn> getModelColumns() {
			List<OEColumn> cols = new ArrayList<OEColumn>();
			cols.add(new OEColumn("name", "Name", OEFields.varchar(50)));
			cols.add(new OEColumn("city", "city", OEFields.varchar(50)));
			cols.add(new OEColumn("user_type", "Type", OEFields
					.manyToOne(new IdeaUserType(mContext))));
			return cols;
		}

	}

	class IdeaUserType extends OEDatabase implements OEDBHelper {
		Context mContext = null;

		public IdeaUserType(Context context) {
			super(context);
			mContext = context;
		}

		@Override
		public String getModelName() {
			return "idea.user.type";
		}

		@Override
		public List<OEColumn> getModelColumns() {
			List<OEColumn> cols = new ArrayList<OEColumn>();
			cols.add(new OEColumn("type", "Name", OEFields.varchar(50)));
			return cols;
		}

	}

	@Override
	public String getModelName() {
		return "idea.idea";
	}

	@Override
	public List<OEColumn> getModelColumns() {
		List<OEColumn> columns = new ArrayList<OEColumn>();

		columns.add(new OEColumn("name", "Name", OEFields.varchar(64)));
		columns.add(new OEColumn("description", "Description", OEFields.text()));
		columns.add(new OEColumn("category_id", "Idea Category", OEFields
				.manyToOne(new IdeaCategory(mContext))));
		columns.add(new OEColumn("user_ids", "Idea Users", OEFields
				.manyToMany(new IdeaUsers(mContext))));
		columns.add(new OEColumn("idea_files", "idea_files", OEFields
				.oneToMany(new IdeaFiles(mContext))));
		return columns;
	}

	class IdeaFiles extends OEDatabase {
		Context mContext = null;

		public IdeaFiles(Context context) {
			super(context);
			mContext = context;
		}

		@Override
		public String getModelName() {
			return "idea.files";
		}

		@Override
		public List<OEColumn> getModelColumns() {
			List<OEColumn> cols = new ArrayList<OEColumn>();
			cols.add(new OEColumn("name", "name", OEFields.varchar(55)));
			cols.add(new OEColumn("idea_idea_id", "idea id", OEFields
					.manyToOne(new IdeaDBHelper(mContext))));
			return cols;
		}

	}
}
