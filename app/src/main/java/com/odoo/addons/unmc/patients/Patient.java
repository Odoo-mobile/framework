package com.odoo.addons.unmc.patients;


import android.content.Context;

import com.odoo.BuildConfig;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.ODateTime;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

public class Patient extends OModel {
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID +
            ".core.provider.content.sync.unmc_patient";
    public static final String MALE_VAL = "male";
    public static final String FEMALE_VAL = "female";
    private static final String MODEL_NAME = "unmc.patient";
    OColumn surname = new OColumn("Surname", OVarchar.class);
    OColumn first_name = new OColumn("First Name", OVarchar.class);
    OColumn gender = new OColumn("Gender", OVarchar.class);
    OColumn date_of_birth = new OColumn("Date of birth", ODateTime.class);
    OColumn parent_name_or_guardian = new OColumn("Parent Name or Guardian", OVarchar.class);
    OColumn phone_number = new OColumn("Phone number", OVarchar.class);
    OColumn address = new OColumn("Address", OVarchar.class);
    OColumn barcode_id = new OColumn("Barcode Id", OVarchar.class);

    public Patient(Context context, OUser user) {
        super(context, "unmc.patient", user);
    }

}
