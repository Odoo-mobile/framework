package com.openerp.addons.messages;

import android.content.Context;

import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.OEColumn;
import com.openerp.orm.OETypes;

public class UserGroupsDb extends BaseDBHelper {

	public UserGroupsDb(Context context) {
		super(context);
		this.name = "mail.group";

		columns.add(new OEColumn("name", "Name", OETypes.varchar(64)));
		columns.add(new OEColumn("description", "Description", OETypes.text()));
		columns.add(new OEColumn("image_medium", "medium Image", OETypes.blob()));
	}

}
