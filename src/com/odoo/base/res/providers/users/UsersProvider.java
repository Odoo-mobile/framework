package com.odoo.base.res.providers.users;

import android.content.Context;
import android.net.Uri;

import com.odoo.base.res.ResUsers;
import com.odoo.orm.OModel;
import com.odoo.support.provider.OContentProvider;

public class UsersProvider extends OContentProvider {
	public static final String AUTHORITY = "com.odoo.base.res.providers.users";
	public static final String PATH = "res_users";
	public static final Uri CONTENT_URI = OContentProvider.buildURI(AUTHORITY,
			PATH);

	@Override
	public OModel model(Context context) {
		return new ResUsers(context);
	}

	@Override
	public String authority() {
		return UsersProvider.AUTHORITY;
	}

	@Override
	public String path() {
		return UsersProvider.PATH;
	}

	@Override
	public Uri uri() {
		return UsersProvider.CONTENT_URI;
	}
}
