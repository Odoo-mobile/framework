package com.odoo.addons.tripdestination.providers;

import android.content.Context;
import android.net.Uri;

import com.odoo.addons.Equipment.providers.CmmsEquipment;
import com.odoo.addons.intervention.providers.CmmsIntervention;
import com.odoo.addons.preventive.providers.CmmsPreventive;
import com.odoo.addons.trip.providers.CmmsTrips;
import com.odoo.base.addons.res.ResCompany;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OBoolean;
import com.odoo.core.orm.fields.types.ODateTime;
import com.odoo.core.orm.fields.types.OFloat;
import com.odoo.core.orm.fields.types.OInteger;
import com.odoo.core.orm.fields.types.OSelection;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

/**
 * Created by Sylwek on 01/05/2016.
 */
public class CmmsTripDestination extends OModel {
    public static final String AUTHORITY = "com.corepd.addons.tripdestination.TripDestination";
    public static final String TAG = CmmsTripDestination.class.getSimpleName();
    OColumn startdate = new OColumn("Start Date", ODateTime.class);
    OColumn state = new OColumn("Status", OSelection.class).addSelection("1","Not Started").addSelection("2", "Driving").addSelection("3","Working").addSelection("4","Complete").addSelection("5","Incomplete").setLabel("Current Status");
//    OColumn trip = new OColumn("Trip", CmmsTrips.class, OColumn.RelationType.ManyToOne);
//    OColumn order1 = new OColumn("Order", OInteger.class);
//
//    OColumn description = new OColumn("Description", OVarchar.class);
//
//    OColumn enddate = new OColumn("End Date", ODateTime.class);
//    OColumn driving_time = new OColumn("Est. Driving Time", OFloat.class);
//    OColumn distance = new OColumn("Distance", OFloat.class);
//    OColumn equipment_id = new OColumn("Equipment",CmmsEquipment.class, OColumn.RelationType.ManyToOne);
//    //v2
//    OColumn scheduled_time = new OColumn("Scheduled Time", ODateTime.class);
//    OColumn action = new OColumn("Action",OBoolean.class);
//    OColumn installation = new OColumn("Install",OBoolean.class);
//    OColumn training = new OColumn("Training",OBoolean.class);
//    OColumn loler = new OColumn("LOLER",OBoolean.class);
//    OColumn customer = new OColumn("Customer",ResCompany.class, OColumn.RelationType.ManyToOne);
//    OColumn intervention = new OColumn("Intervention",CmmsIntervention.class, OColumn.RelationType.ManyToOne);
//    OColumn preventive = new OColumn("Preventive",CmmsPreventive.class, OColumn.RelationType.ManyToOne);


    //('1', 'Not Started'), ('2', 'Driving'),('3', 'Working'),('4', 'Complete'),('5', 'Incomplete')], "Status"



//
//
//            'intermotif':fields.related('intervention', 'motif', type="char"),
//            'interobservation':fields.related('intervention', 'observation', type="char"),

    public CmmsTripDestination(Context context, OUser user) {
        super(context, "cmms.trip_destination", user);
        setDefaultNameColumn("equipment_id");
    }
    @Override
    public Uri uri() {
        return buildURI(AUTHORITY);
    }

}

