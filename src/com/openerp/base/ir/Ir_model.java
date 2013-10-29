package com.openerp.base.ir;

import android.content.Context;

import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.Fields;
import com.openerp.orm.Types;

public class Ir_model extends BaseDBHelper {

	public Ir_model(Context context) {
		super(context);
		name = "ir.model";

		columns.add(new Fields("model", "model name", Types.varchar(50)));
		columns.add(new Fields("is_installed", "is installed", Types.varchar(6)));

	}
}
