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
package com.openerp.base.mail;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.openerp.base.res.ResPartnerDB;
import com.openerp.orm.OEColumn;
import com.openerp.orm.OEDatabase;
import com.openerp.orm.OEFields;

public class MailFollowers extends OEDatabase {
	Context mContext = null;

	public MailFollowers(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public String getModelName() {
		return "mail.followers";
	}

	@Override
	public List<OEColumn> getModelColumns() {
		List<OEColumn> cols = new ArrayList<OEColumn>();
		cols.add(new OEColumn("res_model", "Model", OEFields.text()));
		cols.add(new OEColumn("res_id", "Note ID", OEFields.integer()));
		cols.add(new OEColumn("partner_id", "Partner ID", OEFields
				.manyToOne(new ResPartnerDB(mContext))));
		return cols;
	}
}
