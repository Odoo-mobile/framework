package com.odoo.addons.Equipment.providers;

import com.odoo.core.orm.provider.BaseModelProvider;

/**
 * Created by Sylwek on 02/12/2015.
 */
public class EquipmentSyncProvider extends BaseModelProvider {
    public static final String TAG = EquipmentSyncProvider.class.getSimpleName();

    @Override
    public String authority() {
        return CmmsEquipment.AUTHORITY;
    }

}
