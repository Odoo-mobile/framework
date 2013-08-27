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

import android.app.ProgressDialog;
import android.content.Context;

// TODO: Auto-generated Javadoc
/**
 * The Class OEDialog.
 */
public class OEDialog extends ProgressDialog {

	/**
	 * Instantiates a new oE dialog.
	 * 
	 * @param context
	 *            the context
	 */
	public OEDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new oE dialog.
	 * 
	 * @param context
	 *            the context
	 * @param isCancelable
	 *            the is cancelable
	 * @param message
	 *            the message
	 */
	public OEDialog(Context context, boolean isCancelable, String message) {
		super(context);
		this.setTitle("Please wait...");
		this.setCancelable(isCancelable);
		this.setMessage(message);
	}

}
