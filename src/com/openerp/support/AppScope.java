package com.openerp.support;

import com.openerp.MainActivity;

public class AppScope {
	private UserObject user = new UserObject();
	private MainActivity context = null;

	public AppScope(UserObject user, MainActivity context) {
		super();
		this.user = user;
		this.context = context;
	}

	public UserObject User() {
		return user;
	}

	public MainActivity context() {
		return context;
	}

}
