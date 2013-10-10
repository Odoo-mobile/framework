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

package com.openerp.providers.groups;

import com.openerp.support.provider.OEContentProvider;

// TODO: Auto-generated Javadoc
/**
 * The Class NoteProvider.
 */
public class UserGroupsProvider extends OEContentProvider {

	/** The contenturi. */
	public static String CONTENTURI = "com.openerp.providers.groups.UserGroupsProvider";

	/** The authority. */
	public static String AUTHORITY = "com.openerp.providers.groups";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openerp.support.provider.OEContentProviderHelper#authority()
	 */
	@Override
	public String authority() {
		return AUTHORITY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openerp.support.provider.OEContentProviderHelper#contentUri()
	 */
	@Override
	public String contentUri() {
		return CONTENTURI;
	}

}
