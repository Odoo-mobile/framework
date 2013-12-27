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
import com.openerp.orm.OEColumn;
import com.openerp.orm.OETypes;

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
		columns.add(new OEColumn("is_company", "Is Company", OETypes.text()));
		columns.add(new OEColumn("name", "Name", OETypes.text()));
		columns.add(new OEColumn("image_small", "Image", OETypes.blob()));
		columns.add(new OEColumn("street", "Street", OETypes.text()));
		columns.add(new OEColumn("street2", "Street 2", OETypes.text()));
		columns.add(new OEColumn("city", "City", OETypes.text()));
		columns.add(new OEColumn("zip", "Zip", OETypes.text()));
		columns.add(new OEColumn("website", "website", OETypes.text()));
		columns.add(new OEColumn("phone", "Phone", OETypes.text()));
		columns.add(new OEColumn("mobile", "Mobile", OETypes.text()));
		columns.add(new OEColumn("email", "email", OETypes.text()));
		columns.add(new OEColumn("company_id", "company", OETypes
				.many2One(new Res_Company(context))));

	}

}
