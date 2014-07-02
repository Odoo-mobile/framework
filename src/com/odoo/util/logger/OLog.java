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
package com.odoo.util.logger;

import android.text.TextUtils;

public class OLog {
	/** The logger. */
	private final java.util.logging.Logger logger = java.util.logging.Logger
			.getLogger(this.getClass().toString());

	public static void log(String... messages) {
		String message = TextUtils.join(", ", messages);
		OLog log = new OLog();
		log._log(message);
	}

	public void _log(String message) {
		logger.severe(message);
	}
}
