package com.odoo.base.addons.res;

import android.content.Context;

import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

public class ResPartnerCategory extends OModel {

    OColumn name = new OColumn("Name", OVarchar.class);

    public ResPartnerCategory(Context context, OUser user) {
        super(context, "res.partner.category", user);
    }
}
