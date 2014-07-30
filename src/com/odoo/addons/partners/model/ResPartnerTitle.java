package com.odoo.addons.partners.model;

import android.content.Context;

import com.odoo.orm.OColumn;
import com.odoo.orm.OModel;
import com.odoo.orm.types.OVarchar;

public class ResPartnerTitle extends OModel {

	OColumn name= new OColumn("Title", OVarchar.class, 64).setRequired(true);
	OColumn shortcut= new OColumn("Abbreviation", OVarchar.class, 64);
	OColumn domain= new OColumn("Abbreviation", OVarchar.class, 64).setRequired(true).setDefault("contact");
	
	public ResPartnerTitle(Context context) {
		super(context, "res.partner.title");
	}

	
}
