package com.odoo.addons.productionline.providers;

import com.odoo.core.orm.provider.BaseModelProvider;

/**
 * Created by Sylwek on 26/06/2016.
 */

public class ProductionLineSyncProvider extends BaseModelProvider {
    public static final String TAG = ProductionLineSyncProvider.class.getSimpleName();

    @Override
    public String authority() {
        return CmmsProductionLine.AUTHORITY;
    }
}