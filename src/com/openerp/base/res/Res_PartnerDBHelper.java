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

package com.openerp.base.res;

import android.content.Context;

import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.Fields;
import com.openerp.orm.Types;

// TODO: Auto-generated Javadoc
/**
 * The Class Res_PartnerDBHelper.
 */
public class Res_PartnerDBHelper extends BaseDBHelper {

	/**
	 * Instantiates a new res_ partner db helper.
	 * 
	 * @param context
	 *            the context
	 */
	public Res_PartnerDBHelper(Context context) {
		super(context);
		/* setting model name */
		this.name = "res.partner";

		/* providing model columns */
		columns.add(new Fields("is_company", "Is Company", Types.text()));
		columns.add(new Fields("name", "Name", Types.text()));
		columns.add(new Fields("image_small", "Image", Types.blob()));
		columns.add(new Fields("street", "Street", Types.text()));
		columns.add(new Fields("street2", "Street 2", Types.text()));
		columns.add(new Fields("city", "City", Types.text()));
		columns.add(new Fields("zip", "Zip", Types.text()));
		columns.add(new Fields("website", "website", Types.text()));
		columns.add(new Fields("phone", "Phone", Types.text()));
		columns.add(new Fields("mobile", "Mobile", Types.text()));
		columns.add(new Fields("email", "email", Types.text()));
		columns.add(new Fields("company_id", "company", Types
				.many2One(new Res_Company(context))));

	}

}
