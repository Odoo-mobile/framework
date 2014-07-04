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

import android.view.View;

/**
 * The listener interface for receiving OListDragDrop events. The class that is
 * interested in processing a OListDragDrop event implements this interface, and
 * the object created with that class is registered with a component using the
 * component's <code>addOListDragDropListener<code> method. When
 * the OListDragDrop event occurs, that object's appropriate
 * method is invoked.
 * 
 */
public interface OListDragDropListener {

	/**
	 * On item drag start.
	 * 
	 * @param drag_view
	 *            the drag_view
	 * @param position
	 *            the position
	 * @param data
	 *            the data
	 */
	public void onItemDragStart(View drag_view, int position, Object data);

	/**
	 * On item drop.
	 * 
	 * @param drop_view
	 *            the drop_view
	 * @param drag_view_data
	 *            the drag_view_data
	 * @param drop_view_data
	 *            the drop_view_data
	 */
	public void onItemDrop(View drop_view, Object drag_view_data,
			Object drop_view_data);

	/**
	 * On item drag end.
	 * 
	 * @param drop_view
	 *            the drop_view
	 * @param position
	 *            the position
	 * @param data
	 *            the data
	 */
	public void onItemDragEnd(View drop_view, int position, Object data);

}
