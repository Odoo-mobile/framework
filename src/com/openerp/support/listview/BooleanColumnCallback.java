/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http://www.openerp.com>)
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * 
 */
package com.openerp.support.listview;

import android.view.View;

// TODO: Auto-generated Javadoc
/**
 * The Interface BooleanColumnCallback.
 */
public interface BooleanColumnCallback {

	/**
	 * Update flag values.
	 * 
	 * @param row
	 *            the row
	 * @param view
	 *            the view
	 * @return the oE list view rows
	 */
	public OEListViewRow updateFlagValues(OEListViewRow row, View view);

}
