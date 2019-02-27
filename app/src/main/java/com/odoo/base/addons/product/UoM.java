package com.odoo.base.addons.product;

import android.content.Context;

import com.odoo.base.addons.res.ResCountry;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OBoolean;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

public class UoM extends OModel {

    OColumn name = new OColumn("Name", OVarchar.class);
    OColumn category_id = new OColumn("UoM Category", OVarchar.class, OColumn.RelationType.ManyToOne);
    OColumn active = new OColumn("Active", OBoolean.class);

    public UoM(Context context, OUser user) {
        super(context, "res.country.state", user);
    }
}
