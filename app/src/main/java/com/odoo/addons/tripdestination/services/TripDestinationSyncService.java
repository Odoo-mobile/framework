package com.odoo.addons.tripdestination.services;

import android.content.Context;
import android.os.Bundle;

import com.odoo.addons.tripdestination.providers.CmmsTripDestination;
import com.odoo.core.service.OSyncAdapter;
import com.odoo.core.service.OSyncService;
import com.odoo.core.support.OUser;

/**
 * Created by Sylwek on 01/05/2016.
 */
public class TripDestinationSyncService extends OSyncService {
    public static final String TAG = TripDestinationSyncService.class.getSimpleName();

    @Override
    public OSyncAdapter getSyncAdapter(OSyncService service, Context context) {
        return new OSyncAdapter(context, CmmsTripDestination.class, service, true);
    }

    @Override
    public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {
        adapter.syncDataLimit(80);
    }
}