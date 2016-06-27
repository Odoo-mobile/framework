package com.odoo.addons.productionline.providers;

import android.content.Context;

import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

/**
 * Created by Odoo-CorePD on 24/06/2016.
 */
public class CmmsProductionLine extends OModel {
    public static final String TAG = CmmsProductionLine.class.getSimpleName();
    public static final String AUTHORITY = "com.corepd.addons.productionline.ProductionLine";
    OColumn location = new OColumn("Location", OVarchar.class);
    OColumn name = new OColumn("Production Line", OVarchar.class);
    OColumn code = new OColumn("Line reference", OVarchar.class);

    public CmmsProductionLine(Context context, OUser user) {
        super(context, "cmms.line", user);
    }


    @Override
    public boolean allowCreateRecordOnServer() {
        return false;
    }

    @Override
    public boolean allowUpdateRecordOnServer() {
        return false;
    }

    @Override
    public boolean allowDeleteRecordInLocal() {
        return false;
    }
}