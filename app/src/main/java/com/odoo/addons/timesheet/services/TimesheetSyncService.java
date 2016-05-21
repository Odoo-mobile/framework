package com.odoo.addons.timesheet.services;

import android.content.Context;
import android.os.Bundle;

import com.odoo.addons.timesheet.providers.CmmsTimesheet;
import com.odoo.base.addons.res.CmmsHistory;
import com.odoo.core.service.OSyncAdapter;
import com.odoo.core.service.OSyncService;
import com.odoo.core.support.OUser;

/**
 * Created by Sylwek on 05/12/2015.
 */
public class TimesheetSyncService extends OSyncService {
    public static final String TAG = TimesheetSyncService.class.getSimpleName();

    @Override
    public OSyncAdapter getSyncAdapter(OSyncService service, Context context) {
        return new OSyncAdapter(context, CmmsTimesheet.class, service, true);
    }

    @Override
    public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {
        adapter.syncDataLimit(80);
    }
}
