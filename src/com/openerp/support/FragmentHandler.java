package com.openerp.support;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.openerp.MainActivity;
import com.openerp.R;

public class FragmentHandler {
    MainActivity instance = null;
    boolean isBackStack = false;
    Bundle bundle = null;
    String backStackTag = "";

    public FragmentHandler(MainActivity object) {
	this.instance = object;
    }

    public void setBackStack(boolean isBackStack, String tag) {
	this.isBackStack = isBackStack;
	this.backStackTag = tag;
    }

    public void startNewFragmnet(Fragment fragment) {
	if (this.bundle != null) {
	    fragment.setArguments(this.bundle);
	}
	instance.getSupportFragmentManager().beginTransaction()
		.add(R.id.fragment_container, fragment).commit();
    }

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

    public void setFragmentArguments(Bundle bundle) {
	this.bundle = bundle;
    }
}
