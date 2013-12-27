package com.openerp.addons.messages;

import android.content.Context;

import com.openerp.base.res.Res_PartnerDBHelper;
import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.OEColumn;
import com.openerp.orm.OETypes;

public class MailFollowerDb extends BaseDBHelper {

	public MailFollowerDb(Context context) {
		super(context);
		this.name = "mail.followers";

		columns.add(new OEColumn("res_model", "Model", OETypes.varchar(128)));
		columns.add(new OEColumn("res_id", "Res ID", OETypes.integer()));
		columns.add(new OEColumn("partner_id", "partner id", OETypes
				.many2One(new Res_PartnerDBHelper(context))));
	}

}
