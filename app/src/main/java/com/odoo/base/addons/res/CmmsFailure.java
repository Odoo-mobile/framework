package com.odoo.base.addons.res;

import android.content.Context;
import android.net.Uri;

import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

/**
 * Created by Sylwek on 05/12/2015.
 */
public class CmmsFailure extends OModel {
    public static final String AUTHORITY = "com.odoo.addons.failure.Failure";
    public static final String TAG =  CmmsFailure.class.getSimpleName();
    OColumn name = new OColumn("name", OVarchar.class);
    OColumn code = new OColumn("code", OVarchar.class);
    OColumn description = new OColumn("description", OVarchar.class);

    public CmmsFailure(Context context, OUser user) {
        super(context, " cmms.failure", user);
        setDefaultNameColumn("name");
    }
    @Override
    public Uri uri() {
        return buildURI(AUTHORITY);
    }
}

//'name': fields.char('Type of failure', size=32, required=True),
//        'code': fields.char('Code', size=32),
//        'description': fields.text('Failure description'),