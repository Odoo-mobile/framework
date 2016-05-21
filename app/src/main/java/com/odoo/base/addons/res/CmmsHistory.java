package com.odoo.base.addons.res;

import android.content.Context;
import android.net.Uri;

import com.odoo.addons.Equipment.providers.CmmsEquipment;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.ODate;
import com.odoo.core.support.OUser;

/**
 * Created by Sylwek on 05/12/2015.
 */
public class CmmsHistory extends OModel {
    public static final String AUTHORITY = "com.odoo.addons.history.History";
    public static final String TAG =  CmmsHistory.class.getSimpleName();
    OColumn equipment = new OColumn("equipment_id", CmmsEquipment.class, OColumn.RelationType.ManyToOne).setRequired();
    OColumn customer = new OColumn("customer", ResPartner.class, OColumn.RelationType.ManyToOne).setRequired();
    OColumn start_date = new OColumn("start_date", ODate.class);
    OColumn end_date = new OColumn("end_date", ODate.class);

    public  CmmsHistory(Context context, OUser user) {
        super(context, " cmms.history", user);
        setDefaultNameColumn("customer");
    }
    @Override
    public Uri uri() {
        return buildURI(AUTHORITY);
    }
}

//'equipment_id': fields.many2one('cmms.equipment','Unit of work ref', required=True),
//        'customer': fields.many2one('res.partner', 'Customer',required=True),
//        'start_date': fields.date("Start Date"),
//        'end_date': fields.date("End Date"),