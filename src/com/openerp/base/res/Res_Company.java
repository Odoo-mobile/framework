package com.openerp.base.res;

import android.content.Context;

import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.Fields;
import com.openerp.orm.Types;

/**
 * The Class Res_Company.
 */
public class Res_Company extends BaseDBHelper {

	/**
	 * Instantiates a new res_company.
	 * 
	 * @param context
	 *            the context
	 */
	public Res_Company(Context context) {
		super(context);
		name = "res.company";
		columns.add(new Fields("name", "Name", Types.varchar(100)));
	}

}