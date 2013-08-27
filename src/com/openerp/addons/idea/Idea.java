package com.openerp.addons.idea;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.openerp.R;
import com.openerp.support.BaseFragment;
import com.openerp.support.menu.OEMenu;

public class Idea extends BaseFragment {

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_idea, container,
				false);
		return rootView;
	}

	@Override
	public Object databaseHelper(Context context) {
		// TODO Auto-generated method stub
		return new IdeaDBHelper(context);
	}

	@Override
	public void handleArguments(Bundle bundle) {
		// TODO Auto-generated method stub

	}

	@Override
	public OEMenu menuHelper(Context context) {
		// TODO Auto-generated method stub
		return null;
	}

}
