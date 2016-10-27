package com.odoo;

import odoo.Odoo;
import odoo.helper.OUser;

public interface OdooInstanceListener {
    void onOdooInstance(Odoo odoo, OUser user);
}
