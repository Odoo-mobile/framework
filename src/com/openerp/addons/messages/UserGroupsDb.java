package com.openerp.addons.messages;

import android.content.Context;

import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.Fields;
import com.openerp.orm.Types;

public class UserGroupsDb extends BaseDBHelper {

	public UserGroupsDb(Context context) {
		super(context);
		this.name = "mail.group";

		columns.add(new Fields("name", "Name", Types.varchar(64)));
		columns.add(new Fields("description", "Description", Types.text()));
		columns.add(new Fields("image_medium", "medium Image", Types.blob()));
	}

}
