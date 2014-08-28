package com.odoo.addons.partners.model;

import android.content.Context;

import com.odoo.addons.partners.providers.partners_category.PartnerCategoryProvider;
import com.odoo.orm.OColumn;
import com.odoo.orm.OModel;
import com.odoo.orm.types.OVarchar;
import com.odoo.support.provider.OContentProvider;

public class ResPartnerCategory extends OModel {

	OColumn name = new OColumn("Category Name", OVarchar.class, 64);

	public ResPartnerCategory(Context context) {
		super(context, "res.partner.category");
	}

	@Override
	public OContentProvider getContentProvider() {
		return new PartnerCategoryProvider();
	}
}
