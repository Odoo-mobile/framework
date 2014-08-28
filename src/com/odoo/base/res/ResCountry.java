package com.odoo.base.res;

import android.content.Context;

import com.odoo.base.res.providers.country.CountryProvider;
import com.odoo.orm.OColumn;
import com.odoo.orm.OModel;
import com.odoo.orm.types.OVarchar;
import com.odoo.support.provider.OContentProvider;

public class ResCountry extends OModel {

	OColumn name = new OColumn("Name", OVarchar.class);

	public ResCountry(Context context) {
		super(context, "res.country");
	}

	@Override
	public OContentProvider getContentProvider() {
		return new CountryProvider();
	}
}
