package com.odoo.addons.intervention.providers;

import android.content.Context;

import com.odoo.addons.Equipment.providers.CmmsEquipment;
import com.odoo.base.addons.res.ResUsers;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.ODate;
import com.odoo.core.orm.fields.types.OSelection;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

/**
 * Created by Sylwek on 05/12/2015.
 */
public class CmmsIntervention extends OModel {
    public static final String AUTHORITY = "com.corepd.addons.intervention.Intervention";
    public static final String TAG = CmmsIntervention.class.getSimpleName();
    OColumn name = new OColumn("name", OVarchar.class);
    OColumn equipment_id = new OColumn("equipment_id", CmmsEquipment.class, OColumn.RelationType.ManyToOne)
            .setRelatedColumn("equipment_id");
    OColumn user_id = new OColumn("user_id", ResUsers.class, OColumn.RelationType.ManyToOne).setRequired();
    OColumn date = new OColumn("date", ODate.class);
    OColumn observation = new OColumn("observation", OVarchar.class);
    OColumn motif = new OColumn("motif", OVarchar.class);
    OColumn date_inter = new OColumn("date_inter", ODate.class);
    OColumn date_end = new OColumn("date_end", ODate.class);
  //  OColumn issue = new OColumn("issue", CmmsFailure.class, OColumn.RelationType.ManyToOne).setRequired();
     // OColumn equipment = new OColumn("equipment", CmmsEquipment.class, OColumn.RelationType.ManyToOne);
    OColumn state = new OColumn("state", OSelection.class)
            .addSelection("draft","Draft")
            .addSelection("done", "Fixed")
            .addSelection("cancel", "Canceled");
    OColumn priority = new OColumn("priority", OSelection.class)
            .addSelection("normal", "Normal")
            .addSelection("low", "Low")
            .addSelection("urgent","Urgent")
            .addSelection("other","Other");

    public CmmsIntervention(Context context, OUser user) {
        super(context, "cmms.intervention", user);
        setDefaultNameColumn("name");
    }


}

//'name': fields.char('Intervention reference', size=64),
//        'equipment_id': fields.many2one('cmms.equipment', 'Serial Number', required=True),
//        'date': fields.datetime('Date'),
//        'user_id': fields.many2one('res.users', 'Added by:', readonly=True),
//
//        'observation': fields.text('Observation'),
//        'motif': fields.text('Motif'),
//        'date_inter': fields.datetime('Intervention date'),
//        'date_end': fields.datetime('Intervention end date'),
//        'type': fields.selection(AVAILABLE_INTERVENTION_TYPE,'Intervention type', size=32),
//        'issue' : fields.many2one('cmms.failure','Failure',required=True),//
//        'priority': fields.selection([('normal','Normal'),('low','Low'),('urgent','Urgent'),('other','Other')],'priority', size=32),
//        'state' : fields.selection([('draft',u'Draft'),('done',u'Fixed'),('cancel',u'Canceled')],u'Status',required=True),