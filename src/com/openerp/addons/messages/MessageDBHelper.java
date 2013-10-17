/*
 * 
 */
package com.openerp.addons.messages;

import android.content.Context;

import com.openerp.base.ir.Ir_AttachmentDBHelper;
import com.openerp.base.res.Res_PartnerDBHelper;
import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.Fields;
import com.openerp.orm.Types;

public class MessageDBHelper extends BaseDBHelper {

	public MessageDBHelper(Context context) {
		super(context);
		/* setting model name */
		name = "mail.message";

		/* providing model columns */
		columns.add(new Fields("partner_ids", "Partners", Types
				.many2Many(new Res_PartnerDBHelper(context))));
		columns.add(new Fields("subject", "Subject", Types.text()));
		columns.add(new Fields("type", "Type", Types.varchar(30)));
		columns.add(new Fields("body", "Body", Types.text()));
		columns.add(new Fields("email_from", "Email From", Types.text()));
		columns.add(new Fields("parent_id", "Parent", Types.integer()));
		columns.add(new Fields("record_name", "Record Title", Types.text()));
		columns.add(new Fields("to_read", "To Read", Types.varchar(5)));
		columns.add(new Fields("author_id", "Author", Types
				.many2One(new Res_PartnerDBHelper(context))));
		columns.add(new Fields("model", "Model", Types.varchar(50)));
		columns.add(new Fields("res_id", "Resouce Reference", Types.text()));
		columns.add(new Fields("date", "Date", Types.varchar(20)));
		columns.add(new Fields("has_voted", "Has Voted", Types.varchar(5)));
		columns.add(new Fields("vote_nb", "vote numbers", Types.integer()));
		columns.add(new Fields("starred", "Starred", Types.varchar(5)));
		columns.add(new Fields("attachment_ids", "Attachments", Types
				.many2Many(new Ir_AttachmentDBHelper(context))));

	}
}
