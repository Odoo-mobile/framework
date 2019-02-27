package com.odoo.base.addons.product;

import android.content.Context;

import com.odoo.base.addons.res.ResCountry;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

public class ProductTemplate extends OModel {

    OColumn name = new OColumn("Name", OVarchar.class);
    OColumn description = new OColumn("Code", OVarchar.class);

    public ProductTemplate(Context context, OUser user) {
        super(context, "product.template", user);
    }
}
