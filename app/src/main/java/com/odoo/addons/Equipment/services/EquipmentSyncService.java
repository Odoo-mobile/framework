package com.odoo.addons.Equipment.services;

import android.content.Context;
import android.os.Bundle;

import com.odoo.addons.Equipment.providers.CmmsEquipment;
import com.odoo.core.service.OSyncAdapter;
import com.odoo.core.service.OSyncService;
import com.odoo.core.support.OUser;

/**
 * Created by burol on 02/12/2015.
 */
public class EquipmentSyncService extends OSyncService {
    public static final String TAG = EquipmentSyncService.class.getSimpleName();

    @Override
    public OSyncAdapter getSyncAdapter(OSyncService service, Context context) {
        return new OSyncAdapter(context, CmmsEquipment.class, service, true);
    }

    @Override
    public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {
        adapter.syncDataLimit(80);
    }
}
