package com.openerp.base.mail;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.openerp.base.res.Res_PartnerDBHelper;
import com.openerp.orm.OEColumn;
import com.openerp.orm.OEDatabase;
import com.openerp.orm.OEFields;

public class MailFollowers extends OEDatabase {
	Context mContext = null;

	public MailFollowers(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public String getModelName() {
		return "mail.followers";
	}

	@Override
	public List<OEColumn> getModelColumns() {
		List<OEColumn> cols = new ArrayList<OEColumn>();
		cols.add(new OEColumn("res_model", "Model", OEFields.text()));
		cols.add(new OEColumn("res_id", "Note ID", OEFields.integer()));
		cols.add(new OEColumn("partner_id", "Partner ID", OEFields
				.manyToOne(new Res_PartnerDBHelper(mContext))));
		return cols;
	}
}
