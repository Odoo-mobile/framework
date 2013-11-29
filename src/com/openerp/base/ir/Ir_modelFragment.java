package com.openerp.base.ir;

import java.util.List;

import android.content.Context;

import com.openerp.support.BaseFragment;
import com.openerp.util.drawer.DrawerItem;

public class Ir_modelFragment extends BaseFragment {

	@Override
	public Object databaseHelper(Context context) {
		return new Ir_model(context);
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		return null;
	}

}
