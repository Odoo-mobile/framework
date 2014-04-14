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

package com.openerp.base.ir;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.openerp.base.res.ResCompanyDB;
import com.openerp.orm.OEColumn;
import com.openerp.orm.OEDatabase;
import com.openerp.orm.OEFields;

/**
 * The Class Ir_AttachmentDBHelper.
 */
public class Ir_AttachmentDBHelper extends OEDatabase {
	Context mContext = null;

	public Ir_AttachmentDBHelper(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public String getModelName() {
		return "ir.attachment";
	}

	@Override
	public List<OEColumn> getModelColumns() {
		List<OEColumn> columns = new ArrayList<OEColumn>();
		columns.add(new OEColumn("name", "Name", OEFields.text()));
		columns.add(new OEColumn("datas_fname", "Data File Name", OEFields
				.text()));
		columns.add(new OEColumn("type", "Type", OEFields.text()));
		columns.add(new OEColumn("file_size", "File Size", OEFields.integer()));
		columns.add(new OEColumn("res_model", "Model", OEFields.varchar(100)));
		columns.add(new OEColumn("file_type", "content type", OEFields
				.varchar(100)));
		columns.add(new OEColumn("file_uri", "File Uri", OEFields.varchar(100),
				false));
		columns.add(new OEColumn("company_id", "company id", OEFields
				.manyToOne(new ResCompanyDB(mContext))));
		columns.add(new OEColumn("res_id", "resource id", OEFields.integer()));
		return columns;
	}
}
