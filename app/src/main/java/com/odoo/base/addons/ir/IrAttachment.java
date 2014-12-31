/**
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
 * Created on 31/12/14 12:41 PM
 */
package com.odoo.base.addons.ir;

import android.content.Context;

import com.odoo.base.addons.res.ResCompany;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OInteger;
import com.odoo.core.orm.fields.types.OText;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

public class IrAttachment extends OModel {
    public static final String TAG = IrAttachment.class.getSimpleName();

    OColumn name = new OColumn("Name", OVarchar.class);
    OColumn datas_fname = new OColumn("Data file name", OText.class);
    OColumn file_size = new OColumn("File Size", OInteger.class);
    OColumn res_model = new OColumn("Model", OVarchar.class).setSize(100);
    OColumn file_type = new OColumn("Content Type", OVarchar.class).setSize(100);
    OColumn company_id = new OColumn("Company", ResCompany.class,
            OColumn.RelationType.ManyToOne);
    OColumn res_id = new OColumn("Resource id", OInteger.class).setDefaultValue(0);
    OColumn scheme = new OColumn("File Scheme", OVarchar.class).setSize(100)
            .setLocalColumn();
    // Local Column
    OColumn file_uri = new OColumn("File URI", OVarchar.class).setSize(150)
            .setLocalColumn().setDefaultValue(false);
    OColumn type = new OColumn("Type", OText.class).setLocalColumn();

    public IrAttachment(Context context, OUser user) {
        super(context, "ir.attachment", user);
    }
}
