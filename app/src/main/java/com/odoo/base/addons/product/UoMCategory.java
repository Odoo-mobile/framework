package com.odoo.base.addons.product;

import android.content.Context;

import com.odoo.base.addons.res.ResCountry;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

public class UoMCategory extends OModel {

    OColumn name = new OColumn("Name", OVarchar.class);
    OColumn description = new OColumn("Description", OVarchar.class);
    OColumn measure_type = new OColumn("Measure Type", OVarchar.class);

    public UoMCategory(Context context, OUser user) {
        super(context, "uom.category", user);
    }
}
