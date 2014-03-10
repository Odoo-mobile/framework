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
			OEVersion version = oe.getOEVersion();
			if ((version.getVersion_number() == 7
					&& version.getVersion_type().equals("saas") && version
					.getVersion_type_number() == 3)
					|| (version.getVersion_number() >= 8)) {
				name = "calendar.event";
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
