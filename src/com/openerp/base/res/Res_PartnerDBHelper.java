package com.openerp.base.res;

import android.content.Context;
import android.util.Log;

import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.Fields;
import com.openerp.orm.Types;

public class Res_PartnerDBHelper extends BaseDBHelper {

	public Res_PartnerDBHelper(Context context) {
		super(context);
		/* setting model name */
		this.name = "res.partner";

		/* providing model columns */
		columns.add(new Fields("is_company", "Is Company", Types.text()));
		columns.add(new Fields("name", "Name", Types.text()));
		columns.add(new Fields("image", "Image", Types.blob()));
		columns.add(new Fields("street", "Street", Types.text()));
		columns.add(new Fields("street2", "Street 2", Types.text()));
		columns.add(new Fields("city", "City", Types.text()));
		columns.add(new Fields("zip", "Zip", Types.text()));
		columns.add(new Fields("website", "website", Types.text()));
		columns.add(new Fields("phone", "Phone", Types.text()));
		columns.add(new Fields("mobile", "Mobile", Types.text()));
		columns.add(new Fields("email", "email", Types.text()));

	}

}
