package com.odoo;


import android.os.Bundle;

import com.odoo.core.orm.fields.OColumn;

public class ActivityCommons {
    public static boolean hasRecordInExtra(Bundle bundle) {
        return bundle != null && bundle.containsKey(OColumn.ROW_ID);
    }
}
