package com.odoo.base.ir;

import android.content.Context;

import com.odoo.base.ir.providers.model.ModelProvider;
import com.odoo.orm.OColumn;
import com.odoo.orm.OModel;
import com.odoo.orm.types.ODateTime;
import com.odoo.orm.types.OVarchar;
import com.odoo.support.provider.OContentProvider;

public class IrModel extends OModel {

	OColumn name = new OColumn("Model Description", OVarchar.class, 100);
	OColumn model = new OColumn("Model", OVarchar.class, 100);
	OColumn state = new OColumn("State", OVarchar.class, 64);
	// FIXME
	// OColumn field_id = new OColumn("Fields", IrModelFields.class,
	// RelationType.OneToMany, "model_id");

	// Local Column
	OColumn last_synced = new OColumn("Last Synced on ", ODateTime.class)
			.setLocalColumn();

	public IrModel(Context context) {
		super(context, "ir.model");
	}

	@Override
	public Boolean checkForCreateDate() {
		return false;
	}

	@Override
	public Boolean checkForWriteDate() {
		return false;
	}

	@Override
	public OContentProvider getContentProvider() {
		return new ModelProvider();
	}
}
