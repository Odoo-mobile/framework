package com.odoo.base.ir;

import android.content.Context;

import com.odoo.orm.OColumn;
import com.odoo.orm.OColumn.RelationType;
import com.odoo.orm.OModel;
import com.odoo.orm.types.OEDateTime;
import com.odoo.orm.types.OEVarchar;

public class IrModel extends OModel {

	OColumn name = new OColumn("Model Description", OEVarchar.class, 100);
	OColumn model = new OColumn("Model", OEVarchar.class, 100);
	OColumn state = new OColumn("State", OEVarchar.class, 64);
	OColumn field_id = new OColumn("Fields", IrModelFields.class,
			RelationType.OneToMany, "model_id");

	// Local Column
	OColumn last_synced = new OColumn("Last Synced on ", OEDateTime.class)
			.setLocalColumn();

	public IrModel(Context context) {
		super(context, "ir.model");
	}

}
