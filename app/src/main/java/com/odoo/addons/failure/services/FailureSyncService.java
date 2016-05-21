package com.odoo.addons.failure.services;

import android.content.Context;
import android.os.Bundle;

import com.odoo.base.addons.res.CmmsFailure;
import com.odoo.core.service.OSyncAdapter;
import com.odoo.core.service.OSyncService;
import com.odoo.core.support.OUser;

/**
 * Created by Burol on 05/12/2015.
 */
public class FailureSyncService extends OSyncService {
    public static final String TAG = FailureSyncService.class.getSimpleName();

    @Override
    public OSyncAdapter getSyncAdapter(OSyncService service, Context context) {
        return new OSyncAdapter(context, CmmsFailure.class, service, true);
    }

    @Override
    public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {
        adapter.syncDataLimit(80);
    }
}
