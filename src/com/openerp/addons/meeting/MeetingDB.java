/**
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

import java.util.ArrayList;
import java.util.List;

import openerp.OpenERP.OEVersion;
import android.content.Context;

import com.openerp.base.res.ResPartnerDB;
import com.openerp.orm.OEColumn;
import com.openerp.orm.OEDatabase;
import com.openerp.orm.OEFields;
import com.openerp.orm.OEHelper;

public class MeetingDB extends OEDatabase {

	Context mContext = null;

	public MeetingDB(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public String getModelName() {
		String name = "crm.meeting";
		OEHelper oe = getOEInstance();
		if (oe != null) {
			try {
				OEVersion version = oe.openERP().getOEVersion();
				if ((version.getVersion_number() == 7
						&& version.getVersion_type().equals("saas") && version
						.getVersion_type_number() == 3)
						|| (version.getVersion_number() >= 8)) {
					name = "calendar.event";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return name;
	}

	@Override
	public List<OEColumn> getModelColumns() {
		List<OEColumn> columns = new ArrayList<OEColumn>();
		columns.add(new OEColumn("name", "Name", OEFields.varchar(64)));
		columns.add(new OEColumn("date", "Date", OEFields.text()));
		columns.add(new OEColumn("duration", "Duration", OEFields.text()));
		columns.add(new OEColumn("allday", "All day", OEFields.varchar(6)));
		columns.add(new OEColumn("description", "Description", OEFields.text()));
		columns.add(new OEColumn("location", "Location", OEFields.text()));
		columns.add(new OEColumn("date_deadline", "Dead_line", OEFields.text()));
		columns.add(new OEColumn("partner_ids", "Partner_ids", OEFields
				.manyToMany(new ResPartnerDB(mContext))));

		// Event id of OpenERP Mobile Calendar for meeting
		columns.add(new OEColumn("calendar_event_id", "Calendar_event_id",
				OEFields.integer(), false));

		// OpenERP Calendar Id under which meetings synced as events
		columns.add(new OEColumn("calendar_id", "Calendar_id", OEFields
				.integer(), false));
		return columns;
	}
}
