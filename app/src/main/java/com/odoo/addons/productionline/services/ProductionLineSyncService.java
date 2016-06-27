package com.odoo.addons.productionline.services;

import android.content.Context;
import android.os.Bundle;

import com.odoo.addons.productionline.providers.CmmsProductionLine;
import com.odoo.core.service.OSyncAdapter;
import com.odoo.core.service.OSyncService;
import com.odoo.core.support.OUser;

/**
 * Created by Sylwek on 26/06/2016.
 */

public class ProductionLineSyncService extends OSyncService {
    public static final String TAG = ProductionLineSyncService.class.getSimpleName();

    @Override
    public OSyncAdapter getSyncAdapter(OSyncService service, Context context) {
        return new OSyncAdapter(context, CmmsProductionLine.class, service, true, true);
    }

    @Override
    public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {
        adapter.syncDataLimit(80);
    }

}
