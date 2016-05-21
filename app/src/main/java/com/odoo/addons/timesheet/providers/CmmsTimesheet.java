package com.odoo.addons.timesheet.providers;

import android.content.Context;
import android.net.Uri;

import com.odoo.core.orm.OM2ORecord;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.ODate;
import com.odoo.core.orm.fields.types.ODateTime;
import com.odoo.core.support.OUser;

/**
 * Created by Sylwek on 27/12/2015.
 */
public class CmmsTimesheet extends OModel {
    public static final String AUTHORITY = "com.odoo.addons.timesheet.timesheet";
    public static final String TAG =  CmmsTimesheet.class.getSimpleName();
    OColumn user_id = new OColumn("User", OM2ORecord.class);
    OColumn date = new OColumn("Date", ODate.class);
    OColumn start_time = new OColumn("Start Time", ODateTime.class);
    OColumn end_time = new OColumn("End Time", ODateTime.class);
    OColumn start_overtime = new OColumn("Overtime Start", ODateTime.class);
    OColumn end_overtime = new OColumn("Overtime End", ODateTime.class);

    public CmmsTimesheet(Context context, OUser user) {
        super(context, " cmms.timesheet", user);
        setDefaultNameColumn("date");
    }
    @Override
    public Uri uri() {
        return buildURI(AUTHORITY);
    }
}

/**
 * 'user_id': fields.many2one('res.users', 'Manager',required=True),
 'date': fields.date("Date"),
 'start_time': fields.fields.datetime("Start Time"),
 'end_time': fields.fields.datetime("End Time"),
 'start_overtime': fields.fields.datetime("Overtime Start"),
 'end_overtime': fields.fields.datetime("Overtime End"),
 */