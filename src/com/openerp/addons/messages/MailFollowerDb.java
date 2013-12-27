/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http:www.openerp.com>)
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
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * 
 */
package com.openerp.addons.messages;

import android.content.Context;

import com.openerp.base.res.Res_PartnerDBHelper;
import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.OEColumn;
import com.openerp.orm.OETypes;

public class MailFollowerDb extends BaseDBHelper {

	public MailFollowerDb(Context context) {
		super(context);
		this.name = "mail.followers";

		columns.add(new OEColumn("res_model", "Model", OETypes.varchar(128)));
		columns.add(new OEColumn("res_id", "Res ID", OETypes.integer()));
		columns.add(new OEColumn("partner_id", "partner id", OETypes
				.many2One(new Res_PartnerDBHelper(context))));
	}

}
