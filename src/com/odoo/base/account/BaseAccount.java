package com.odoo.base.account;

import android.content.Context;

import com.odoo.orm.OColumn;
import com.odoo.orm.OModel;
import com.odoo.orm.types.OBlob;
import com.odoo.orm.types.OBoolean;
import com.odoo.orm.types.OVarchar;

public class BaseAccount extends OModel {

	OColumn name = new OColumn("Name", OVarchar.class).setLocalColumn();
	OColumn image = new OColumn("Image", OBlob.class).setLocalColumn();
	OColumn host = new OColumn("Name", OVarchar.class).setLocalColumn();
	OColumn is_active = new OColumn("Name", OBoolean.class).setLocalColumn();

	public BaseAccount(Context context) {
		super(context, "base.account");
	}

}
