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

package com.openerp.support;

import com.openerp.MainActivity;

// TODO: Auto-generated Javadoc
/**
 * The Class AppScope.
 */
public class AppScope {

	/** The user. */
	private UserObject user = new UserObject();

	/** The context. */
	private MainActivity context = null;

	/**
	 * Instantiates a new app scope.
	 * 
	 * @param user
	 *            the user
	 * @param context
	 *            the context
	 */
	public AppScope(UserObject user, MainActivity context) {
		super();
		this.user = user;
		this.context = context;
	}

	/**
	 * User.
	 * 
	 * @return the user object
	 */
	public UserObject User() {
		return user;
	}

	/**
	 * Context.
	 * 
	 * @return the main activity
	 */
	public MainActivity context() {
		return context;
	}

}
