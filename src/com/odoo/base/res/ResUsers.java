package com.odoo.base.res;

import android.content.Context;

import com.odoo.base.res.providers.users.UsersProvider;
import com.odoo.orm.OColumn;
import com.odoo.orm.OModel;
import com.odoo.orm.types.OVarchar;
import com.odoo.support.provider.OContentProvider;

public class ResUsers extends OModel {

	OColumn name = new OColumn("Name", OVarchar.class, 64);
	OColumn login = new OColumn("User Login name", OVarchar.class, 64);

	public ResUsers(Context context) {
		super(context, "res.users");
	}

	@Override
	public Boolean canUpdateToServer() {
		return false;
	}

	@Override
	public OContentProvider getContentProvider() {
		return new UsersProvider();
	}
}
