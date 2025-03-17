package com.odoo.base.addons.res;

import android.content.Context;

import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

public class ResCountryState extends OModel {

    OColumn name = new OColumn("Name", OVarchar.class);
    OColumn code = new OColumn("Code", OVarchar.class);
    OColumn country_id = new OColumn("Country", ResCountry.class, OColumn.RelationType.ManyToOne);

    public ResCountryState(Context context, OUser user) {
        super(context, "res.country.state", user);
    }
}
