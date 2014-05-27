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
package com.odoo.orm;

import com.odoo.orm.types.OEBlob;
import com.odoo.orm.types.OEBoolean;
import com.odoo.orm.types.OEDateTime;
import com.odoo.orm.types.OEInteger;
import com.odoo.orm.types.OEManyToMany;
import com.odoo.orm.types.OEManyToOne;
import com.odoo.orm.types.OEOneToMany;
import com.odoo.orm.types.OEReal;
import com.odoo.orm.types.OEText;
import com.odoo.orm.types.OETimestamp;
import com.odoo.orm.types.OEVarchar;

public class OEFields {

	public static OEVarchar varchar(int size) {
		return new OEVarchar(size);
	}

	public static OEInteger integer() {
		return new OEInteger(0);
	}

	public static OEInteger integer(int size) {
		return new OEInteger(size);
	}

	public static OEReal real() {
		return new OEReal(0);
	}

	public static OEReal real(int size) {
		return new OEReal(size);
	}

	public static OEBoolean booleantype() {
		return new OEBoolean();
	}

	public static OETimestamp timestamp(String dateformat) {
		return new OETimestamp(dateformat);
	}

	public static OEDateTime datetime(String dateformat) {
		return new OEDateTime(dateformat);
	}

	public static OEText text() {
		return new OEText();
	}

	public static OEBlob blob() {
		return new OEBlob();
	}

	public static OEManyToMany manyToMany(Object db) {
		return new OEManyToMany((OEDBHelper) db);
	}

	public static OEManyToOne manyToOne(Object db) {
		return new OEManyToOne((OEDBHelper) db);
	}

	public static OEOneToMany oneToMany(Object db) {
		return new OEOneToMany((OEDBHelper) db);
	}

}
