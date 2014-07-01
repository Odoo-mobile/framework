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

package com.odoo.support;

import android.app.AlertDialog;
import android.content.Context;

import com.odoo.R;

public class OExceptionDialog extends AlertDialog.Builder {

	public OExceptionDialog(Context context) {
		super(context);
	}

	public OExceptionDialog(Context context, boolean isCancelable,
			String message) {
		super(context);
		this.setTitle("Odoo Exception");
		this.setCancelable(isCancelable);
		this.setMessage(message);
		setPositiveButton(context.getResources().getString(R.string.label_ok),
				null);
	}

}
