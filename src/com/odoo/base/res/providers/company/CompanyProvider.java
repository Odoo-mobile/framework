package com.odoo.base.res.providers.company;

import android.content.Context;
import android.net.Uri;

import com.odoo.base.res.ResCompany;
import com.odoo.orm.OModel;
import com.odoo.support.provider.OContentProvider;

public class CompanyProvider extends OContentProvider {
	public static final String AUTHORITY = "com.odoo.base.res.providers.company";
	public static final String PATH = "res_company";
	public static final Uri CONTENT_URI = OContentProvider.buildURI(AUTHORITY,
			PATH);

	@Override
	public OModel model(Context context) {
		return new ResCompany(context);
	}

	@Override
	public String authority() {
		return CompanyProvider.AUTHORITY;
	}

	@Override
	public String path() {
		return CompanyProvider.PATH;
	}

	@Override
	public Uri uri() {
		return CompanyProvider.CONTENT_URI;
	}

}
