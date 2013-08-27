package com.openerp.base.ir;

import android.content.Context;

import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.Fields;
import com.openerp.orm.Types;

public class Ir_AttachmentDBHelper extends BaseDBHelper {

    public Ir_AttachmentDBHelper(Context context) {
	super(context);
	/* setting model name */
	this.name = "ir.attachment";

	/* providing model columns */
	columns.add(new Fields("name", "Name", Types.text()));
	columns.add(new Fields("datas_fname", "Data File Name", Types.text()));
	columns.add(new Fields("type", "Type", Types.text()));
	columns.add(new Fields("file_size", "File Size", Types.integer()));
	columns.add(new Fields("db_datas", "Base64 Data", Types.blob()));

    }

}
