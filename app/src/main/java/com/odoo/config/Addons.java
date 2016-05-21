/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 *
 * Created on 30/12/14 3:11 PM
 */
package com.odoo.config;

import com.odoo.addons.Equipment.Equipment;
import com.odoo.addons.customers.Customers;
import com.odoo.addons.intervention.Intervention;
import com.odoo.addons.timesheet.Timesheet;
import com.odoo.addons.trip.Trip;
import com.odoo.addons.tripdestination.TripDestination;
import com.odoo.core.support.addons.AddonsHelper;
import com.odoo.core.support.addons.OAddon;

public class Addons extends AddonsHelper {

    /**
     * Declare your required module here
     * NOTE: For maintain sequence use object name in asc order.
     * Ex.:
     * OAddon partners = new OAddon(Partners.class).setDefault();
     */
    OAddon customers = new OAddon(Customers.class);
    OAddon equipment = new OAddon(Equipment.class);
    OAddon intervention = new OAddon(Intervention.class);
    OAddon trips = new OAddon(Trip.class);
    OAddon tripdestination = new OAddon(TripDestination.class).setDefault();
  //  OAddon a_timesheet = new OAddon(Timesheet.class);
}
