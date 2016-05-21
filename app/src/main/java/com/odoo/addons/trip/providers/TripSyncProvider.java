package com.odoo.addons.trip.providers;

import com.odoo.core.orm.provider.BaseModelProvider;

/**
 * Created by Sylwek on 01/05/2016.
 */
public class TripSyncProvider  extends BaseModelProvider {
    public static final String TAG = TripSyncProvider.class.getSimpleName();

    @Override
    public String authority() {
        return CmmsTrips.AUTHORITY;
    }
}
