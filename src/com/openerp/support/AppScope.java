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

import android.support.v4.app.Fragment;

import com.openerp.MainActivity;

// TODO: Auto-generated Javadoc
/**
 * The Class AppScope.
 */
public class AppScope {

	/** The user. */
	private OEUser user = new OEUser();

	/** The context. */
	private MainActivity context = null;

	public AppScope(Fragment fragment) {
		super();
		this.context = (MainActivity) fragment.getActivity();
		this.user = context.getUserContext();
	}

	/**
	 * Instantiates a new app scope.
	 * 
	 * @param user
	 *            the user
	 * @param context
	 *            the context
	 */
	public AppScope(MainActivity context) {
		super();
		this.user = context.getUserContext();
		this.context = context;
	}

	/**
	 * User.
	 * 
	 * @return the user object
	 */
	public OEUser User() {
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
