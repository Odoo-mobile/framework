package com.odoo.addons.idea.providers.library;

import com.odoo.support.provider.OEContentProvider;

public class LibraryProvider extends OEContentProvider {

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
