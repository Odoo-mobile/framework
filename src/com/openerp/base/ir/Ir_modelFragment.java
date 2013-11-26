package com.openerp.base.ir;

import android.content.Context;

import com.openerp.support.BaseFragment;
import com.openerp.support.menu.OEMenu;

public class Ir_modelFragment extends BaseFragment {

	@Override
	public Object databaseHelper(Context context) {
		return new Ir_model(context);
	}

	@Override
	public OEMenu menuHelper(Context context) {
		// TODO Auto-generated method stub
		return null;
	}

}
