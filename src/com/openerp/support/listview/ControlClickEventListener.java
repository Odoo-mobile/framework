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
 * The listener interface for receiving controlClickEvent events. The class that
 * is interested in processing a controlClickEvent event implements this
 * interface, and the object created with that class is registered with a
 * component using the component's
 * <code>addControlClickEventListener<code> method. When
 * the controlClickEvent event occurs, that object's appropriate
 * method is invoked.
 * 
 * @see ControlClickEventEvent
 */
public interface ControlClickEventListener {

	/**
	 * Control clicked.
	 * 
	 * @param row
	 *            the row
	 * @param view
	 *            the view
	 * @return the oE list view rows
	 */
	public OEListViewRow controlClicked(int position, OEListViewRow row, View view);

}
