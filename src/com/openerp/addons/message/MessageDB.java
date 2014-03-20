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
package com.openerp.addons.message;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.content.Context;

import com.openerp.base.ir.Ir_AttachmentDBHelper;
import com.openerp.base.res.ResPartnerDB;
import com.openerp.orm.OEColumn;
import com.openerp.orm.OEDatabase;
import com.openerp.orm.OEFields;
import com.openerp.orm.OEFieldsHelper.ValueWatcher;
import com.openerp.orm.OEValues;

public class MessageDB extends OEDatabase {

	Context mContext = null;

	public MessageDB(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public String getModelName() {
		return "mail.message";
	}

	@Override
	public List<OEColumn> getModelColumns() {
		List<OEColumn> columns = new ArrayList<OEColumn>();
		columns.add(new OEColumn("partner_ids", "Partners", OEFields
				.manyToMany(new ResPartnerDB(mContext))));
		columns.add(new OEColumn("subject", "Subject", OEFields.text()));
		columns.add(new OEColumn("type", "Type", OEFields.varchar(30)));
		columns.add(new OEColumn("body", "Body", OEFields.text()));
		columns.add(new OEColumn("email_from", "Email From", OEFields.text(),
				false));

		columns.add(new OEColumn("parent_id", "Parent", OEFields.integer()));
		columns.add(new OEColumn("record_name", "Record Title", OEFields.text()));
		columns.add(new OEColumn("to_read", "To Read", OEFields.varchar(5)));

		ValueWatcher mValueWatcher = new ValueWatcher() {

			@Override
			public OEValues getValue(OEColumn col, Object value) {
				OEValues values = new OEValues();
				try {
					if (value instanceof JSONArray) {
						JSONArray array = (JSONArray) value;
						if (array.getInt(0) == 0) {
							values.put(col.getName(), false);
							values.put("email_from", array.getString(1));
						} else {
							values.put("email_from", false);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return values;
			}

		};
		columns.add(new OEColumn("author_id", "author", OEFields
				.manyToOne(new ResPartnerDB(mContext)), mValueWatcher));
		columns.add(new OEColumn("model", "Model", OEFields.varchar(50)));
		columns.add(new OEColumn("res_id", "Resouce Reference", OEFields.text()));
		columns.add(new OEColumn("date", "Date", OEFields.varchar(20)));
		columns.add(new OEColumn("has_voted", "Has Voted", OEFields.varchar(5)));
		columns.add(new OEColumn("vote_nb", "vote numbers", OEFields.integer()));
		columns.add(new OEColumn("starred", "Starred", OEFields.varchar(5)));
		columns.add(new OEColumn("attachment_ids", "Attachments", OEFields
				.manyToMany(new Ir_AttachmentDBHelper(mContext))));
		return columns;
	}
}
