package com.odoo.support.provider;

import android.content.Context;
import android.net.Uri;

import com.odoo.orm.OModel;

public interface OContentProviderHelper {
	public OModel model(Context context);

	public String authority();

	public String path();

	public Uri uri();
}