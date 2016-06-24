package com.odoo.addons.productionline.providers;

import android.content.Context;
import android.net.Uri;

import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OBlob;
import com.odoo.core.orm.fields.types.OBoolean;
import com.odoo.core.orm.fields.types.ODate;
import com.odoo.core.orm.fields.types.OInteger;
import com.odoo.core.orm.fields.types.OSelection;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

/**
 * Created by Odoo-CorePD on 24/06/2016.
 */
public class CmmsProductionLine extends OModel {
    public static final String TAG = CmmsProductionLine.class.getSimpleName();
//FIXME - ADD production line auth
    OColumn location = new OColumn("Location", OVarchar.class);
    OColumn name = new OColumn("Production Line", OVarchar.class);

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