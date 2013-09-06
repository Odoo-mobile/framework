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
