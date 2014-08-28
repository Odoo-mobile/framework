/*
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
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

package com.odoo.base.ir;

import odoo.ODomain;
import odoo.Odoo;

import org.json.JSONObject;

import android.content.Context;

import com.odoo.App;
import com.odoo.base.ir.providers.attachments.AttachmentProvider;
import com.odoo.base.res.ResCompany;
import com.odoo.orm.OColumn;
import com.odoo.orm.OColumn.RelationType;
import com.odoo.orm.OModel;
import com.odoo.orm.types.OInteger;
import com.odoo.orm.types.OText;
import com.odoo.orm.types.OVarchar;
import com.odoo.support.provider.OContentProvider;

/**
 * The Class IrAttachment
 */
public class IrAttachment extends OModel {

	OColumn name = new OColumn("Name", OText.class);
	OColumn datas_fname = new OColumn("Data file name", OText.class);
	OColumn type = new OColumn("Type", OText.class);
	OColumn file_size = new OColumn("File Size", OInteger.class);
	OColumn res_model = new OColumn("Model", OVarchar.class, 100);
	OColumn file_type = new OColumn("Content Type", OVarchar.class, 100);
	OColumn company_id = new OColumn("Company", ResCompany.class,
			RelationType.ManyToOne);
	OColumn res_id = new OColumn("Resource id", OInteger.class);

	// Local Column
	OColumn file_uri = new OColumn("File URI", OVarchar.class, 150)
			.setLocalColumn().setDefault(false);

	public IrAttachment(Context context) {
		super(context, "ir.attachment");
	}

	public String getBase64Data(int server_id, App app) {
		String data = "false";
		try {
			Odoo odoo = app.getOdoo();
			ODomain domain = new ODomain();
			domain.add("id", "=", server_id);
			JSONObject fields = new JSONObject();
			fields.accumulate("fields", "datas");
			JSONObject result = odoo.search_read(getModelName(), fields,
					domain.get());
			if (result.getJSONArray("records").length() > 0) {
				JSONObject row = result.getJSONArray("records")
						.getJSONObject(0);
				data = row.getString("datas");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	@Override
	public OContentProvider getContentProvider() {
		return new AttachmentProvider();
	}
}
