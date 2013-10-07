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

import android.content.Context;

import com.openerp.base.res.Res_Company;
import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.Fields;
import com.openerp.orm.Types;

// TODO: Auto-generated Javadoc
/**
 * The Class Ir_AttachmentDBHelper.
 */
public class Ir_AttachmentDBHelper extends BaseDBHelper {

	/**
	 * Instantiates a new ir_ attachment db helper.
	 * 
	 * @param context
	 *            the context
	 */
	public Ir_AttachmentDBHelper(Context context) {
		super(context);
		/* setting model name */
		this.name = "ir.attachment";

		/* providing model columns */
		columns.add(new Fields("name", "Name", Types.text()));
		columns.add(new Fields("datas_fname", "Data File Name", Types.text()));
		columns.add(new Fields("type", "Type", Types.text()));
		columns.add(new Fields("file_size", "File Size", Types.integer()));
		columns.add(new Fields("res_model", "Model", Types.varchar(100)));
		columns.add(new Fields("company_id", "company id", Types
				.many2One(new Res_Company(context))));
		columns.add(new Fields("res_id", "resource id", Types.integer()));
	}
}
