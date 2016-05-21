package com.odoo.addons.preventive.services;

import android.content.Context;
import android.os.Bundle;

import com.odoo.addons.preventive.providers.CmmsPreventive;
import com.odoo.core.service.OSyncAdapter;
import com.odoo.core.service.OSyncService;
import com.odoo.core.support.OUser;

/**
 * Created by Burol on 05/12/2015.
 */
public class PreventiveSyncService extends OSyncService {
    public static final String TAG = PreventiveSyncService.class.getSimpleName();

    @Override
    public OSyncAdapter getSyncAdapter(OSyncService service, Context context) {
        return new OSyncAdapter(context, CmmsPreventive.class, service, true);
    }

    @Override
    public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {
        adapter.syncDataLimit(80);
    }
}
