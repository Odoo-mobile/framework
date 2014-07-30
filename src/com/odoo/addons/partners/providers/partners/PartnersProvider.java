package com.odoo.addons.partners.providers.partners;

import com.odoo.support.provider.OContentProvider;

public class PartnersProvider extends OContentProvider{

	public static String CONTENTURI = "com.odoo.addons.partners.providers.partners.PartnersProvider";
	public static String AUTHORITY = "com.odoo.addons.partners.providers.partners";
	
	@Override
	public String authority() {
		return AUTHORITY;
	}

	@Override
	public String contentUri() {
		// TODO Auto-generated method stub
		return CONTENTURI;
	}

}
