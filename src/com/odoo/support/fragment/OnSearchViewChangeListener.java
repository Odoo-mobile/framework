package com.odoo.support.fragment;

public interface OnSearchViewChangeListener {
	public boolean onSearchViewTextChange(String newFilter);

	public void onSearchViewClose();
}
