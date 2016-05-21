package com.odoo.addons.timesheet.providers;

import com.odoo.core.orm.provider.BaseModelProvider;

/**
 * Created by BSylwek on 05/12/2015.
 */
public class TimesheetSyncProvider extends BaseModelProvider {
    public static final String TAG = TimesheetSyncProvider.class.getSimpleName();

    @Override
    public String authority() {
        return CmmsTimesheet.AUTHORITY;
    }

}