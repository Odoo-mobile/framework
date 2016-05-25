package com.odoo.addons.trip.providers;

import android.content.Context;
import android.net.Uri;

import com.odoo.base.addons.res.ResUsers;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.ODateTime;
import com.odoo.core.orm.fields.types.OInteger;
import com.odoo.core.orm.fields.types.OSelection;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

/**
 * Created by Sylwek on 01/05/2016.
 */
public class CmmsTrips extends OModel {
    public static final String AUTHORITY = "com.corepd.addons.trip.Trip";
    public static final String TAG = CmmsTrips.class.getSimpleName();
    OColumn name = new OColumn("name", OVarchar.class);
    OColumn id = new OColumn("id", OInteger.class);
    OColumn state = new OColumn("Status", OSelection.class).addSelection("1","Not Started").addSelection("2", "In Progress").addSelection("3","Complete").addSelection("4","Incomplete");
    OColumn description = new OColumn("Description", OVarchar.class);
    OColumn startdate = new OColumn("Start Date", ODateTime.class);
    OColumn enddate = new OColumn("End Date", ODateTime.class);
    OColumn user_id = new OColumn("Manager", ResUsers.class, OColumn.RelationType.ManyToOne);
    OColumn user1 = new OColumn("User 1", ResUsers.class, OColumn.RelationType.ManyToOne);
    OColumn user2 = new OColumn("User 2", ResUsers.class, OColumn.RelationType.ManyToOne);
    OColumn user3 = new OColumn("User 3", ResUsers.class, OColumn.RelationType.ManyToOne);
    OColumn user4 = new OColumn("User 4", ResUsers.class, OColumn.RelationType.ManyToOne);
    //OColumn trip_destination = new OColumn("Trip Dest..", CmmsTripDestination.class, OColumn.RelationType.OneToMany).setRelatedColumn("_id");








    public CmmsTrips(Context context, OUser user) {
        super(context, "cmms.trips", user);
        setDefaultNameColumn("name");
    }

    @Override
    public boolean allowUpdateRecordOnServer() {
        return true;
    }
    @Override
    public Uri uri() {
        return buildURI(AUTHORITY);
    }

}
