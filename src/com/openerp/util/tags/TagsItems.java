package com.openerp.util.tags;

import java.io.Serializable;

public class TagsItems implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id = 0;
	private String subject = null;
	private String sub_subject = null;
	private String image = null;

	public TagsItems(int id, String subject, String sub_subject) {
		super();
		this.id = id;
		this.subject = subject;
		this.sub_subject = sub_subject;
	}

	public TagsItems(int id, String subject, String sub_subject, String image) {
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
		return this.subject;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}
}
