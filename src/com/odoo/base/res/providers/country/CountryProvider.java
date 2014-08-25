package com.odoo.base.res.providers.country;

import android.content.Context;
import android.net.Uri;

import com.odoo.base.res.ResCountry;
import com.odoo.orm.OModel;
import com.odoo.support.provider.OContentProvider;

public class CountryProvider extends OContentProvider {

	public static final String AUTHORITY = "com.odoo.base.res.providers.country";
	public static final String PATH = "res_country";
	public static final Uri CONTENT_URI = OContentProvider.buildURI(AUTHORITY,
			PATH);

	@Override
	public OModel model(Context context) {
		return new ResCountry(context);
	}

	@Override
	public String authority() {
		return CountryProvider.AUTHORITY;
	}

	@Override
	public String path() {
		return CountryProvider.PATH;
	}

	@Override
	public Uri uri() {
		return CountryProvider.CONTENT_URI;
	}

}
