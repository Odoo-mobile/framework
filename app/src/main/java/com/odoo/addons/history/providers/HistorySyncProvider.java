package com.odoo.addons.history.providers;

import com.odoo.base.addons.res.CmmsHistory;
import com.odoo.core.orm.provider.BaseModelProvider;

/**
 * Created by Burol on 05/12/2015.
 */
public class HistorySyncProvider extends BaseModelProvider {
    public static final String TAG = HistorySyncProvider.class.getSimpleName();

    @Override
    public String authority() {
        return CmmsHistory.AUTHORITY;
    }

}
