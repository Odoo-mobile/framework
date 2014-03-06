/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http:www.openerp.com>)
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
package com.openerp.util.tags;

import java.io.Serializable;

public class TagsItem implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id = 0;
	private String subject = null;
	private String sub_subject = null;
	private String image = null;

	public TagsItem(int id, String subject, String sub_subject) {
		super();
		this.id = id;
		this.subject = subject;
		this.sub_subject = sub_subject;
	}

	public TagsItem(int id, String subject, String sub_subject, String image) {
		super();
		this.id = id;
		this.subject = subject;
		this.sub_subject = sub_subject;
		this.image = image;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSub_subject() {
		return sub_subject;
	}

	public void setSub_subject(String sub_subject) {
		this.sub_subject = sub_subject;
	}

	@Override
	public String toString() {
		return this.subject + " " + this.sub_subject + " " + this.id;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

}
