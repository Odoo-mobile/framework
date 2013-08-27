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

import android.content.Context;

import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.Fields;
import com.openerp.orm.Types;

// TODO: Auto-generated Javadoc
/**
 * The Class IdeaDBHelper.
 */
public class IdeaDBHelper extends BaseDBHelper {

	/**
	 * Instantiates a new idea db helper.
	 * 
	 * @param context
	 *            the context
	 */
	public IdeaDBHelper(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		name = "idea.idea";

		columns.add(new Fields("name", "Name", Types.varchar(64)));
		columns.add(new Fields("description", "Description", Types.text()));
		columns.add(new Fields("categoriy_ids", "Idea Category", Types
				.many2Many(new IdeaCategory(context))));

	}

	/**
	 * The Class IdeaCategory.
	 */
	class IdeaCategory extends BaseDBHelper {

		/**
		 * Instantiates a new idea category.
		 * 
		 * @param context
		 *            the context
		 */
		public IdeaCategory(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
			name = "idea.category";
			columns.add(new Fields("name", "Name", Types.text()));
		}

	}

}
