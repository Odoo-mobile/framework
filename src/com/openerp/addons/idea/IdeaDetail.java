package com.openerp.addons.idea;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.openerp.R;
import com.openerp.support.BaseFragment;
import com.openerp.util.drawer.DrawerItem;

public class IdeaDetail extends BaseFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mView = inflater.inflate(R.layout.fragment_idea_detail, container,
				false);
		return mView;
	}

	@Override
	public Object databaseHelper(Context context) {
		return new IdeaDBHelper(context);
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		return null;
	}

}
