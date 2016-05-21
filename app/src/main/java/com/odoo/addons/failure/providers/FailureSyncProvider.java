package com.odoo.addons.failure.providers;


import com.odoo.base.addons.res.CmmsFailure;
import com.odoo.core.orm.provider.BaseModelProvider;

/**
 * Created by Burol on 05/12/2015.
 */
public class FailureSyncProvider extends BaseModelProvider {
    public static final String TAG = FailureSyncProvider.class.getSimpleName();

    @Override
    public String authority() {
        return CmmsFailure.AUTHORITY;
    }

}
