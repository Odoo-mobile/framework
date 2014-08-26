package com.odoo.base.ir.providers.attachments;

import android.content.Context;
import android.net.Uri;

import com.odoo.base.ir.IrAttachment;
import com.odoo.base.ir.providers.model.ModelProvider;
import com.odoo.orm.OModel;
import com.odoo.support.provider.OContentProvider;

public class AttachmentProvider extends OContentProvider {
	public static final String AUTHORITY = "com.odoo.base.ir.providers.attachments";
	public static final String PATH = "ir_attachment";
	public static final Uri CONTENT_URI = OContentProvider.buildURI(AUTHORITY,
			PATH);

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

	@Override
	public OModel model(Context context) {
		return new IrAttachment(context);
	}

}
