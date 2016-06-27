package com.odoo.addons.preventive.providers;

import android.content.Context;
import android.net.Uri;

import com.odoo.addons.Equipment.providers.CmmsEquipment;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OBoolean;
import com.odoo.core.orm.fields.types.ODate;
import com.odoo.core.orm.fields.types.OInteger;
import com.odoo.core.orm.fields.types.OSelection;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

/**
 * Created by Sylwek on 05/12/2015.
 */
public class CmmsPreventive extends OModel {
    public static final String AUTHORITY = "com.corepd.addons.preventive.Preventive";
    public static final String TAG =  CmmsPreventive.class.getSimpleName();
    OColumn name = new OColumn("name", OVarchar.class);
//    OColumn type = new OColumn("type", OVarchar.class);
    OColumn equipment = new OColumn("equipment_id", CmmsEquipment.class, OColumn.RelationType.ManyToOne).setRequired();
    OColumn meters = new OColumn("meters", OSelection.class)
            .addSelection("days", "Days");
    OColumn recurrent = new OColumn("recurrent", OBoolean.class);
    OColumn days_interval = new OColumn("days_interval", OInteger.class).setDefaultValue(180);
    OColumn days_last_done = new OColumn("days_last_done", ODate.class).setRequired();
    OColumn days_warning = new OColumn("days_warn_period", OInteger.class).setDefaultValue(30);
    OColumn user = new OColumn("user_id", OVarchar.class);
    public CmmsPreventive(Context context, OUser user) {
        super(context, " cmms.pm", user);
        setDefaultNameColumn("name");
    }
    @Override
    public Uri uri() {
        return buildURI(AUTHORITY);
    }

    public int days_left(ODataRow oDataRow)
    {
        //TODO - calculate days from last till today
      //  days_last_done.
        return 0;
    }
    public String status()
    {
        //  days_last_done.
        return "";
    }
}
//        'name':fields.char('Ref PM',size=20, required=True),
//        'equipment_id': fields.many2one('cmms.equipment', 'Unit of work', required=True),
//        'meter':fields.selection([ ('days', 'Days')], 'Unit of measure'),
//        'recurrent':fields.boolean('Recurrent ?', help="Mark this option if PM is periodic"),
//        'days_interval':fields.integer('Interval'),
//        'days_last_done':fields.date('Begun the',required=True),
//        'days_next_due':fields.function(_days_next_due, method=True, type="date", string='Next date'),
//        'days_warn_period':fields.integer('Warning date'),
//        'user_id': fields.many2one('res.users', 'Chef'),
//        'days_left':fields.function(_days_due, method=True, type="integer", string='Staying days'),
//        'state':fields.function(_get_state, method=True, type="char", string='Status'),
//        'equipment_id_name':fields.related('equipment_id', 'name', string="Location", type="char"),