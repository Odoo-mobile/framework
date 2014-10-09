package com.odoo.addons.partners.providers.country_state;

import android.content.Context;
import android.net.Uri;

import com.odoo.addons.partners.model.ResCountryState;
import com.odoo.orm.OModel;
import com.odoo.support.provider.OContentProvider;

public class CountryStateProvider extends OContentProvider {
	public static final String AUTHORITY = "com.odoo.addons.partners.providers.country_state";
	public static final String PATH = "res_country_state";
	public static final Uri CONTENT_URI = OContentProvider.buildURI(AUTHORITY,
			PATH);

	@Override
	public OModel model(Context context) {
		return new ResCountryState(context);
	}

	@Override
	public String authority() {
		return CountryStateProvider.AUTHORITY;
	}

	@Override
	public String path() {
		return CountryStateProvider.PATH;
	}

	@Override
	public Uri uri() {
		return CountryStateProvider.CONTENT_URI;
	}

}
