package com.odoo.addons.idea.providers.library;

import com.odoo.support.provider.OContentProvider;

public class LibraryProvider extends OContentProvider {

	public static String CONTENTURI = "com.odoo.addons.idea.providers.library.LibraryProvider";
	public static String AUTHORITY = "com.odoo.addons.idea.providers.library";

	@Override
	public String authority() {
		return AUTHORITY;
	}

	@Override
	public String contentUri() {
		return CONTENTURI;
	}

}
