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

package com.odoo.addons.idea;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.odoo.orm.OEColumn;
import com.odoo.orm.OEDatabase;
import com.odoo.orm.OEFields;

public class BooksDB extends OEDatabase {

	Context mContext = null;

	public BooksDB(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public String getModelName() {
		return "book.book";
	}

	@Override
	public List<OEColumn> getModelColumns() {
		List<OEColumn> columns = new ArrayList<OEColumn>();
		columns.add(new OEColumn("name", "Book Name", OEFields.varchar(100)));
		columns.add(new OEColumn("language", "Book Language", OEFields
				.varchar(64)));
		columns.add(new OEColumn("author_id", "Book Author", OEFields
				.manyToOne(new BookAuthor(mContext))));
		columns.add(new OEColumn("student_id", "Book Student", OEFields
				.manyToOne(new BookStudent(mContext))));
		columns.add(new OEColumn("category_ids", "Book Categories", OEFields
				.manyToMany(new BookCategory(mContext))));
		columns.add(new OEColumn("description", "Book Description", OEFields
				.text()));
		return columns;
	}

	static class BookCategory extends OEDatabase {

		public BookCategory(Context context) {
			super(context);
		}

		@Override
		public String getModelName() {
			return "book.category";
		}

		@Override
		public List<OEColumn> getModelColumns() {
			List<OEColumn> columns = new ArrayList<OEColumn>();
			columns.add(new OEColumn("name", "Category Name", OEFields
					.varchar(100)));
			columns.add(new OEColumn("description", "Category Description",
					OEFields.text()));
			return columns;
		}

	}

	static class BookAuthor extends OEDatabase {
		Context mContext = null;

		public BookAuthor(Context context) {
			super(context);
			mContext = context;
		}

		@Override
		public String getModelName() {
			return "book.author";
		}

		@Override
		public List<OEColumn> getModelColumns() {
			List<OEColumn> columns = new ArrayList<OEColumn>();
			columns.add(new OEColumn("name", "Author Name", OEFields
					.varchar(100)));
			columns.add(new OEColumn("country_id", "Author Country", OEFields
					.manyToOne(new ResCountry(mContext))));
			columns.add(new OEColumn("description", "About Author", OEFields
					.text()));
			return columns;
		}
	}

	static class ResCountry extends OEDatabase {

		public ResCountry(Context context) {
			super(context);
		}

		@Override
		public String getModelName() {
			return "res.country";
		}

		@Override
		public List<OEColumn> getModelColumns() {
			List<OEColumn> columns = new ArrayList<OEColumn>();
			columns.add(new OEColumn("name", "Country Name", OEFields
					.varchar(100)));
			return columns;
		}
	}

	static class BookStudent extends OEDatabase {
		Context mContext = null;

		public BookStudent(Context context) {
			super(context);
			mContext = context;
		}

		@Override
		public String getModelName() {
			return "book.student";
		}

		@Override
		public List<OEColumn> getModelColumns() {
			List<OEColumn> columns = new ArrayList<OEColumn>();
			columns.add(new OEColumn("name", "Student Name", OEFields
					.varchar(100)));
			columns.add(new OEColumn("course", "Course Name", OEFields
					.varchar(100)));
			columns.add(new OEColumn("contact", "Student Contact", OEFields
					.varchar(15)));
			columns.add(new OEColumn("book_ids", "Assigned Books", OEFields
					.oneToMany(new BooksDB(mContext), "student_id")));
			return columns;
		}
	}

}
