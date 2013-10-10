package com.openerp.addons.messages;

import android.content.Context;

import com.openerp.base.res.Res_PartnerDBHelper;
import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.Fields;
import com.openerp.orm.Types;

public class MailFollowerDb extends BaseDBHelper {

	public MailFollowerDb(Context context) {
		super(context);
		this.name = "mail.followers";

		columns.add(new Fields("res_model", "Model", Types.varchar(128)));
		columns.add(new Fields("res_id", "Res ID", Types.integer()));
		columns.add(new Fields("partner_id", "partner id", Types
				.many2One(new Res_PartnerDBHelper(context))));
	}

}
