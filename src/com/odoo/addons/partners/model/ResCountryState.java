package com.odoo.addons.partners.model;

import android.content.Context;

import com.odoo.addons.partners.providers.country_state.CountryStateProvider;
import com.odoo.base.res.ResCountry;
import com.odoo.orm.OColumn;
import com.odoo.orm.OColumn.RelationType;
import com.odoo.orm.OModel;
import com.odoo.orm.types.OVarchar;
import com.odoo.support.provider.OContentProvider;

public class ResCountryState extends OModel {

	OColumn name = new OColumn("Name", OVarchar.class, 100);
	OColumn country_id = new OColumn("Country", ResCountry.class,
			RelationType.ManyToOne);

	public ResCountryState(Context context) {
		super(context, "res.country.state");
	}

	@Override
	public OContentProvider getContentProvider() {
		return new CountryStateProvider();
	}

}
