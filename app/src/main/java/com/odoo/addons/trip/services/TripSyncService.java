package com.odoo.addons.trip.services;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import com.odoo.addons.trip.providers.CmmsTrips;
import com.odoo.core.service.OSyncAdapter;
import com.odoo.core.service.OSyncService;
import com.odoo.core.support.OUser;

/**
 * Created by Sylwek on 01/05/2016.
 */
public class TripSyncService extends OSyncService {
    public static final String TAG = TripSyncService.class.getSimpleName();

    @Override
    public OSyncAdapter getSyncAdapter(OSyncService service, Context context) {
        return new OSyncAdapter(context, CmmsTrips.class, service, true,true);
    }

    @Override
    public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {
        adapter.syncDataLimit(80);
    }

}

