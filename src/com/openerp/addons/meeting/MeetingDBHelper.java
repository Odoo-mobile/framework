/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http://www.openerp.com>)
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * 
 */
package com.openerp.addons.meeting;

import android.content.Context;

import com.openerp.base.res.Res_PartnerDBHelper;
import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.OEColumn;
import com.openerp.orm.OETypes;

public class MeetingDBHelper extends BaseDBHelper {

    public MeetingDBHelper(Context context) {
	super(context);

	/* setting model name */
	name = "crm.meeting";

	/* providing model columns */
	columns.add(new OEColumn("name", "Name", OETypes.varchar(64)));
	columns.add(new OEColumn("date", "Date", OETypes.text()));
	columns.add(new OEColumn("duration", "Duration", OETypes.text()));
	columns.add(new OEColumn("description", "Description", OETypes.text()));
	columns.add(new OEColumn("location", "Location", OETypes.text()));
	columns.add(new OEColumn("date_deadline", "Dead_line", OETypes.text()));
	columns.add(new OEColumn("partner_ids", "Partner_ids", OETypes
		.many2Many(new Res_PartnerDBHelper(context))));

	// Is meeting synced in OpenERP Mobile Calendar as Event ?
	columns.add(new OEColumn("in_cal_sync", "In_cal_sync", OETypes.text(),
		false));

	// Event id of OpenERP Mobile Calendar for meeting
	columns.add(new OEColumn("calendar_event_id", "Calendar_event_id", OETypes
		.text(), false));

	// OpenERP Calendar Id under which meetings synced as events
	columns.add(new OEColumn("calendar_id", "Calendar_id", OETypes.text(),
		false));
    }

}
