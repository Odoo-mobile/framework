package com.odoo.addons.unmc.patients;

import com.odoo.core.orm.provider.BaseModelProvider;

public class PatientsSyncProvider extends BaseModelProvider {
    public static final String TAG = PatientsSyncProvider.class.getSimpleName();

    @Override
    public String authority() {
        return Patient.AUTHORITY;
    }
}
