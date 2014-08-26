package com.odoo.base.ir.providers.model;

import android.content.Context;
import android.net.Uri;

import com.odoo.base.ir.IrModel;
import com.odoo.orm.OModel;
import com.odoo.support.provider.OContentProvider;

public class ModelProvider extends OContentProvider {
	public static final String AUTHORITY = "com.odoo.base.ir.providers.model";
	public static final String PATH = "ir_model";
	public static final Uri CONTENT_URI = OContentProvider.buildURI(AUTHORITY,
			PATH);

	@Override
	public OModel model(Context context) {
		return new IrModel(context);
	}

	@Override
	public String authority() {
		return ModelProvider.AUTHORITY;
	}

	@Override
	public String path() {
		return ModelProvider.PATH;
	}

	@Override
	public Uri uri() {
		return ModelProvider.CONTENT_URI;
	}

}
