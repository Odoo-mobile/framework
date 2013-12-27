package com.openerp.base.ir;

import android.content.Context;

import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.OEColumn;
import com.openerp.orm.OETypes;

public class Ir_model extends BaseDBHelper {

	public Ir_model(Context context) {
		super(context);
		name = "ir.model";

		columns.add(new OEColumn("model", "model name", OETypes.varchar(50)));
		columns.add(new OEColumn("is_installed", "is installed", OETypes.varchar(6)));

	}
}
