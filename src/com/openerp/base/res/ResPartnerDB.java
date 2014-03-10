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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.openerp.orm.OEColumn;
import com.openerp.orm.OEDatabase;
import com.openerp.orm.OEFields;

/**
 * The Class Res_PartnerDBHelper.
 */
public class ResPartnerDB extends OEDatabase {

	Context mContext = null;

	/**
	 * Instantiates a new res_ partner db helper.
	 * 
	 * @param context
	 *            the context
	 */
	public ResPartnerDB(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public String getModelName() {
		return "res.partner";
	}

	@Override
	public List<OEColumn> getModelColumns() {
		List<OEColumn> columns = new ArrayList<OEColumn>();
		columns.add(new OEColumn("is_company", "Is Company", OEFields.text()));
		columns.add(new OEColumn("name", "Name", OEFields.text()));
		columns.add(new OEColumn("image_small", "Image", OEFields.blob()));
		columns.add(new OEColumn("street", "Street", OEFields.text()));
		columns.add(new OEColumn("street2", "Street 2", OEFields.text()));
		columns.add(new OEColumn("city", "City", OEFields.text()));
		columns.add(new OEColumn("zip", "Zip", OEFields.text()));
		columns.add(new OEColumn("website", "website", OEFields.text()));
		columns.add(new OEColumn("phone", "Phone", OEFields.text()));
		columns.add(new OEColumn("mobile", "Mobile", OEFields.text()));
		columns.add(new OEColumn("email", "email", OEFields.text()));
		columns.add(new OEColumn("company_id", "company", OEFields
				.manyToOne(new ResCompanyDB(mContext))));
		return columns;
	}

}
