package com.odoo.addons.intervention.services;

import android.content.Context;
import android.os.Bundle;

import com.odoo.addons.intervention.providers.CmmsIntervention;
import com.odoo.core.service.OSyncAdapter;
import com.odoo.core.service.OSyncService;
import com.odoo.core.support.OUser;

/**
 * Created by Burol on 05/12/2015.
 */
public class InterventionSyncService extends OSyncService {
    public static final String TAG = InterventionSyncService.class.getSimpleName();

    @Override
    public OSyncAdapter getSyncAdapter(OSyncService service, Context context) {
        return new OSyncAdapter(context, CmmsIntervention.class, service, true);
    }

    @Override
    public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {
        adapter.syncDataLimit(80);
    }
}
