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

import com.openerp.base.ir.Ir_AttachmentDBHelper;
import com.openerp.base.res.Res_PartnerDBHelper;
import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.OEColumn;
import com.openerp.orm.OETypes;

public class MessageDBHelper extends BaseDBHelper {

	Context mContext = null;

	public MessageDBHelper(Context context) {
		super(context);
		mContext = context;
		/* setting model name */
		name = "mail.message";

		/* providing model columns */
		columns.add(new OEColumn("partner_ids", "Partners", OETypes
				.many2Many(new Res_PartnerDBHelper(context))));
		columns.add(new OEColumn("subject", "Subject", OETypes.text()));
		columns.add(new OEColumn("type", "Type", OETypes.varchar(30)));
		columns.add(new OEColumn("body", "Body", OETypes.text()));
		columns.add(new OEColumn("email_from", "Email From", OETypes.text()));
		columns.add(new OEColumn("parent_id", "Parent", OETypes.integer()));
		columns.add(new OEColumn("record_name", "Record Title", OETypes.text()));
		columns.add(new OEColumn("to_read", "To Read", OETypes.varchar(5)));
		columns.add(new OEColumn("author_id", "Author", OETypes
				.many2One(new Res_PartnerDBHelper(context))));
		columns.add(new OEColumn("model", "Model", OETypes.varchar(50)));
		columns.add(new OEColumn("res_id", "Resouce Reference", OETypes.text()));
		columns.add(new OEColumn("date", "Date", OETypes.varchar(20)));
		columns.add(new OEColumn("has_voted", "Has Voted", OETypes.varchar(5)));
		columns.add(new OEColumn("vote_nb", "vote numbers", OETypes.integer()));
		columns.add(new OEColumn("starred", "Starred", OETypes.varchar(5)));
		columns.add(new OEColumn("attachment_ids", "Attachments", OETypes
				.many2Many(new Ir_AttachmentDBHelper(context))));

	}
}
