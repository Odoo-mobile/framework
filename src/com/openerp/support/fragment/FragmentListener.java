package com.openerp.support.fragment;

import android.support.v4.app.Fragment;

public interface FragmentListener {
	public void startMainFragment(Fragment fragment, boolean addToBackState);

	public void startDetailFragment(Fragment fragment);

	public void restart();
}
