package com.openerp.addons.message;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.openerp.orm.OEColumn;
import com.openerp.orm.OEDatabase;
import com.openerp.orm.OEFields;

public class MailGroupDB extends OEDatabase {

	public MailGroupDB(Context context) {
		super(context);
	}

	@Override
	public String getModelName() {
		return "mail.group";
	}

	@Override
	public List<OEColumn> getModelColumns() {
		List<OEColumn> columns = new ArrayList<OEColumn>();
		columns.add(new OEColumn("name", "Name", OEFields.varchar(64)));
		columns.add(new OEColumn("description", "Description", OEFields.text()));
		columns.add(new OEColumn("image_medium", "medium Image", OEFields
				.blob()));
		return columns;
	}

}
