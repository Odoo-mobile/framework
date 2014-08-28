package com.odoo.base.res.providers.partners;

import android.content.Context;
import android.net.Uri;

import com.odoo.base.res.ResPartner;
import com.odoo.orm.OModel;
import com.odoo.support.provider.OContentProvider;

public class PartnersProvider extends OContentProvider {

	public static final String AUTHORITY = "com.odoo.base.res.providers.partners";
	public static final String PATH = "res_partner";
	public static final Uri CONTENT_URI = OContentProvider.buildURI(AUTHORITY,
			PATH);

	@Override
	public OModel model(Context context) {
		return new ResPartner(context);
	}

	@Override
	public String authority() {
		return AUTHORITY;
	}

	@Override
	public String path() {
		return PATH;
	}

	@Override
	public Uri uri() {
		return PartnersProvider.CONTENT_URI;
	}
}
