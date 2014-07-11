/*
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
 */
package odoo.controls;

import com.odoo.orm.OColumn;
import com.odoo.orm.ODataRow;

/**
 * The listener interface for receiving OViewPagerObject events. The class that
 * is interested in processing a OViewPagerObject event implements this
 * interface, and the object created with that class is registered with a
 * component using the component's
 * <code>addOViewPagerObjectListener<code> method. When
 * the OViewPagerObject event occurs, that object's appropriate
 * method is invoked.
 * 
 * @see OViewPagerObjectEvent
 */
public interface OViewPagerObjectListener {

	/**
	 * Gets the object.
	 * 
	 * @param position
	 *            the position
	 * @return the object
	 */
	public ODataRow getObject(int position);

	/**
	 * Object count.
	 * 
	 * @return the int
	 */
	public int objectCount();

	/**
	 * Gets the column.
	 * 
	 * @return the column
	 */
	public OColumn getColumn();
}
