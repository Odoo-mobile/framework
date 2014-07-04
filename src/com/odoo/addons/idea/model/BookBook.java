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

package com.odoo.addons.idea.model;

import android.content.Context;

import com.odoo.orm.OColumn;
import com.odoo.orm.OColumn.RelationType;
import com.odoo.orm.OModel;
import com.odoo.orm.types.OText;
import com.odoo.orm.types.OVarchar;

public class BookBook extends OModel {

	OColumn name = new OColumn("Name", OVarchar.class, 100).setRequired(true);

	OColumn language = new OColumn("Language", OVarchar.class, 64)
			.setRequired(true);
	OColumn author_id = new OColumn("Author", BookAuthor.class,
			RelationType.ManyToOne);
	OColumn student_id = new OColumn("Student", BookStudent.class,
			RelationType.ManyToOne).setRequired(true);
	OColumn category_ids = new OColumn("Categories", BookCategory.class,
			RelationType.ManyToMany);
	OColumn description = new OColumn("Description", OText.class);

	public BookBook(Context context) {
		super(context, "book.book");
	}

	public static class BookCategory extends OModel {

		OColumn name = new OColumn("Name", OVarchar.class, 100);
		OColumn description = new OColumn("Description", OText.class);

		public BookCategory(Context context) {
			super(context, "book.category");
		}

	}

	public static class BookAuthor extends OModel {

		OColumn name = new OColumn("Name", OVarchar.class, 100);
		OColumn country_id = new OColumn("Country", ResCountry.class,
				RelationType.ManyToOne);
		OColumn description = new OColumn("Author Description", OText.class);

		public BookAuthor(Context context) {
			super(context, "book.author");
		}
	}

	public static class ResCountry extends OModel {
		OColumn name = new OColumn("Country Name", OVarchar.class, 100);

		public ResCountry(Context context) {
			super(context, "res.country");
		}
	}

	public static class BookStudent extends OModel {

		OColumn name = new OColumn("Name", OVarchar.class, 100);
		OColumn course = new OColumn("Course Name", OVarchar.class, 100);
		OColumn contact = new OColumn("Contact", OVarchar.class, 15);
		OColumn book_ids = new OColumn("Assigned Books", BookBook.class,
				RelationType.OneToMany).setRelatedColumn("student_id");

		public BookStudent(Context context) {
			super(context, "book.student");
		}
	}

}
