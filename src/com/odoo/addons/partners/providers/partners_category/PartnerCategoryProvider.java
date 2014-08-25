package com.odoo.addons.partners.providers.partners_category;

import android.content.Context;
import android.net.Uri;

import com.odoo.addons.partners.model.ResPartnerCategory;
import com.odoo.orm.OModel;
import com.odoo.support.provider.OContentProvider;

public class PartnerCategoryProvider extends OContentProvider {
	public static final String AUTHORITY = "com.odoo.addons.partners.providers.partners_category";
	public static final String PATH = "res_partner_category";
	public static final Uri CONTENT_URI = OContentProvider.buildURI(AUTHORITY,
			PATH);

	@Override
	public OModel model(Context context) {
		return new ResPartnerCategory(context);
	}

	@Override
	public String authority() {
		return PartnerCategoryProvider.AUTHORITY;
	}

	@Override
	public String path() {
		return PartnerCategoryProvider.PATH;
	}

	@Override
	public Uri uri() {
		return PartnerCategoryProvider.CONTENT_URI;
	}

}
