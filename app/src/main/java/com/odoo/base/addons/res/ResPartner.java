/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 *
 * Created on 30/12/14 4:00 PM
 */
package com.odoo.base.addons.res;

import android.content.Context;

import com.odoo.addons.partners.models.ResCountryState;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.annotation.Odoo;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OBlob;
import com.odoo.core.orm.fields.types.OBoolean;
import com.odoo.core.orm.fields.types.ODateTime;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

public class ResPartner extends OModel {

    OColumn name = new OColumn("Name", OVarchar.class).setSize(100);
    OColumn is_company = new OColumn("Is Company", OBoolean.class).setDefaultValue(false);
    OColumn image_small = new OColumn("Avatar", OBlob.class).setDefaultValue(false);
    OColumn street = new OColumn("Street", OVarchar.class).setSize(100);
    OColumn street2 = new OColumn("Street2", OVarchar.class).setSize(100);
    OColumn city = new OColumn("City", OVarchar.class);
    OColumn zip = new OColumn("Zip", OVarchar.class);
    OColumn website = new OColumn("Website", OVarchar.class).setSize(100);
    OColumn phone = new OColumn("Phone", OVarchar.class).setSize(15);
    OColumn mobile = new OColumn("Mobile", OVarchar.class).setSize(15);
    OColumn email = new OColumn("Email", OVarchar.class);
    OColumn company_id = new OColumn("Company", ResCompany.class, OColumn.RelationType.ManyToOne);

    // Extra Columns (Demo purpose only)
    OColumn date = new OColumn("Date", ODateTime.class);
    OColumn child_ids = new OColumn("Contacts", ResPartner.class, OColumn.RelationType.OneToMany)
            .setRelatedColumn("parent_id");
    OColumn customer = new OColumn("Customer", OBoolean.class).setDefaultValue(false);
    OColumn supplier = new OColumn("Supplier", OBoolean.class).setDefaultValue(false);

    // Annotation demo columns
    @Odoo.hasDomainFilter
    OColumn state_id = new OColumn("State", ResCountryState.class, OColumn.RelationType.ManyToOne)
            .addDomain("country_id", "=", null);
    @Odoo.onChange(method = "onChangeCompanyId")
    OColumn parent_id = new OColumn("Related Company", ResPartner.class, OColumn.RelationType.ManyToOne)
            .addDomain("is_company", "=", true);

    public ResPartner(Context context, OUser user) {
        super(context, "res.partner", user);
    }


    public ODataRow onChangeCompanyId(ODataRow row) {
        ODataRow res = new ODataRow();
        res.put("city", row.getString("city"));
        res.put("website", row.getString("website"));
        res.put("country_id", row.getM2ORecord("country_id").getId());
        return res;
    }
}
