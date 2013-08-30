package com.openerp.addons.meeting;

import android.content.Context;

import com.openerp.base.res.Res_PartnerDBHelper;
import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.Fields;
import com.openerp.orm.Types;

public class MeetingDBHelper extends BaseDBHelper {

    public MeetingDBHelper(Context context) {
	super(context);

	/* setting model name */
	name = "crm.meeting";

	/* providing model columns */
	columns.add(new Fields("name", "Name", Types.varchar(64)));
	columns.add(new Fields("date", "Date", Types.text()));
	columns.add(new Fields("duration", "Duration", Types.text()));
	columns.add(new Fields("description", "Description", Types.text()));
	columns.add(new Fields("location", "Location", Types.text()));
	columns.add(new Fields("date_deadline", "Dead_line", Types.text()));
	columns.add(new Fields("partner_ids", "Partner_ids", Types
		.many2Many(new Res_PartnerDBHelper(context))));

	// Is meeting synced in OpenERP Mobile Calendar as Event ?
	columns.add(new Fields("in_cal_sync", "In_cal_sync", Types.text(),
		false));

	// Event id of OpenERP Mobile Calendar for meeting
	columns.add(new Fields("calendar_event_id", "Calendar_event_id", Types
		.text(), false));

	// OpenERP Calendar Id under which meetings synced as events
	columns.add(new Fields("calendar_id", "Calendar_id", Types.text(),
		false));
    }

}
