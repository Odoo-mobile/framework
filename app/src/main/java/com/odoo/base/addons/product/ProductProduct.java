/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p/>
 * Created on 30/12/14 4:00 PM
 */
package com.odoo.base.addons.product;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.odoo.BuildConfig;
import com.odoo.OdooActivity;
import com.odoo.base.addons.product.UoM;
import com.odoo.base.addons.res.ResCompany;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.annotation.Odoo;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OBlob;
import com.odoo.core.orm.fields.types.OBoolean;
import com.odoo.core.orm.fields.types.OFloat;
import com.odoo.core.orm.fields.types.OText;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;
import static com.odoo.core.orm.fields.OColumn.*;

import java.util.ArrayList;
import java.util.List;

public class ProductProduct extends OModel {

    public static final String TAG = ProductProduct.class.getSimpleName();
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID +
            ".core.provider.content.sync.product_product";

    OColumn name = new OColumn("Name", OVarchar.class).setSize(100).setRequired();
    OColumn active = new OColumn("Active", OBoolean.class).setDefaultValue(false);
    OColumn image = new OColumn("Image", OBlob.class).setDefaultValue(false);
    OColumn image_small = new OColumn("Avatar", OBlob.class).setDefaultValue(false);
    OColumn lst_price = new OColumn("Sale Price", OFloat.class);
    OColumn default_code = new OColumn("Default Code", OVarchar.class);
    OColumn code = new OColumn("Code", OVarchar.class);
    OColumn product_tmpl_id = new OColumn(null, ProductTemplate.class, RelationType.ManyToOne);

//
//    @Odoo.Domain("[['uom_id', '=', @uom_id]]")
//    OColumn uom_id = new OColumn("UOM", UoM.class, OColumn.RelationType.ManyToOne);

    public ProductProduct(Context context, OUser user) {
        super(context, "product.product", user);
        setHasMailChatter(true);
    }

    @Override
    public Uri uri() {

        return buildURI(AUTHORITY);
    }
//
//    public String storeCompanyName(OValues value) {
//        try {
//            if (!value.getString("parent_id").equals("false")) {
//                List<Object> parent_id = (ArrayList<Object>) value.get("parent_id");
//                return parent_id.get(1) + "";
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "";
//    }
//
//    public static String getContact(Context context, int row_id) {
//        ODataRow row = new ProductProduct(context, null).browse(row_id);
//        String contact;
//        if (row.getString("mobile").equals("false")) {
//            contact = row.getString("phone");
//        } else {
//            contact = row.getString("mobile");
//        }
//        return contact;
//    }
//
//    public String getAddress(ODataRow row) {
//        String add = "";
//        if (!row.getString("street").equals("false"))
//            add += row.getString("street") + ", ";
//        if (!row.getString("street2").equals("false"))
//            add += "\n" + row.getString("street2") + ", ";
//        if (!row.getString("city").equals("false"))
//            add += row.getString("city");
//        if (!row.getString("zip").equals("false"))
//            add += " - " + row.getString("zip") + " ";
//        return add;
//    }


    @Override
    public void onModelUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onSyncStarted(){
        Log.e(TAG, "ProductProduct->onSyncStarted");
    }

    @Override
    public void onSyncFinished(){
        Log.e(TAG, "ProductProduct->onSyncFinished");
    }
}
