/*
 * 
 */
package com.openerp.addons.messages;

import android.content.Context;

import com.openerp.base.ir.Ir_AttachmentDBHelper;
import com.openerp.base.res.Res_PartnerDBHelper;
import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.OEColumn;
import com.openerp.orm.OETypes;

public class MessageDBHelper extends BaseDBHelper {

	Context mContext = null;

	public MessageDBHelper(Context context) {
		super(context);
		mContext = context;
		/* setting model name */
		name = "mail.message";

		/* providing model columns */
		columns.add(new OEColumn("partner_ids", "Partners", OETypes
				.many2Many(new Res_PartnerDBHelper(context))));
		columns.add(new OEColumn("subject", "Subject", OETypes.text()));
		columns.add(new OEColumn("type", "Type", OETypes.varchar(30)));
		columns.add(new OEColumn("body", "Body", OETypes.text()));
		columns.add(new OEColumn("email_from", "Email From", OETypes.text()));
		columns.add(new OEColumn("parent_id", "Parent", OETypes.integer()));
		columns.add(new OEColumn("record_name", "Record Title", OETypes.text()));
		columns.add(new OEColumn("to_read", "To Read", OETypes.varchar(5)));
		columns.add(new OEColumn("author_id", "Author", OETypes
				.many2One(new Res_PartnerDBHelper(context))));
		columns.add(new OEColumn("model", "Model", OETypes.varchar(50)));
		columns.add(new OEColumn("res_id", "Resouce Reference", OETypes.text()));
		columns.add(new OEColumn("date", "Date", OETypes.varchar(20)));
		columns.add(new OEColumn("has_voted", "Has Voted", OETypes.varchar(5)));
		columns.add(new OEColumn("vote_nb", "vote numbers", OETypes.integer()));
		columns.add(new OEColumn("starred", "Starred", OETypes.varchar(5)));
		columns.add(new OEColumn("attachment_ids", "Attachments", OETypes
				.many2Many(new Ir_AttachmentDBHelper(context))));

	}
}
