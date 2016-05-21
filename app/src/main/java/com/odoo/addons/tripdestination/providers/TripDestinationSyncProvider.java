package com.odoo.addons.tripdestination.providers;

import com.odoo.core.orm.provider.BaseModelProvider;

/**
 * Created by Sylwek
 */
public class TripDestinationSyncProvider  extends BaseModelProvider {
    public static final String TAG = TripDestinationSyncProvider.class.getSimpleName();

    @Override
    public String authority() {
        return CmmsTripDestination.AUTHORITY;
    }
}
