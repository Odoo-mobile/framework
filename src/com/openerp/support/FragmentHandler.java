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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.openerp.MainActivity;
import com.openerp.R;

/**
 * The Class FragmentHandler.
 */
public class FragmentHandler {

	/** The instance. */
	MainActivity instance = null;

	/** The is back stack. */
	boolean isBackStack = false;

	/** The bundle. */
	Bundle bundle = null;

	/** The back stack tag. */
	String backStackTag = "";

	/**
	 * Instantiates a new fragment handler.
	 * 
	 * @param object
	 *            the object
	 */
	public FragmentHandler(MainActivity object) {
		this.instance = object;
	}

	/**
	 * Sets the back stack.
	 * 
	 * @param isBackStack
	 *            the is back stack
	 * @param tag
	 *            the tag
	 */
	public void setBackStack(boolean isBackStack, String tag) {
		this.isBackStack = isBackStack;
		this.backStackTag = tag;
	}

	/**
	 * Start new fragmnet.
	 * 
	 * @param fragment
	 *            the fragment
	 */
	public void startNewFragmnet(Fragment fragment) {
		if (this.bundle != null) {
			fragment.setArguments(this.bundle);
		}
		instance.getSupportFragmentManager().beginTransaction()
				.add(R.id.fragment_container, fragment).commit();
	}

	/**
	 * Replace fragmnet.
	 * 
	 * @param fragment
	 *            the fragment
	 */
	public void replaceFragmnet(Fragment fragment) {
		if (this.bundle != null) {
			fragment.setArguments(this.bundle);
		}
		FragmentTransaction ft = instance.getSupportFragmentManager()
				.beginTransaction();
		ft.replace(R.id.fragment_container, fragment);
		if (this.isBackStack) {
			ft.addToBackStack(this.backStackTag);
		}
		ft.commit();
	}

	/**
	 * Sets the fragment arguments.
	 * 
	 * @param bundle
	 *            the new fragment arguments
	 */
	public void setFragmentArguments(Bundle bundle) {
		this.bundle = bundle;
	}
}
