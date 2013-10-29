package com.openerp.base.ir;

import android.content.Context;
import android.os.Bundle;

import com.openerp.support.BaseFragment;
import com.openerp.support.menu.OEMenu;

public class Ir_modelFragment extends BaseFragment {

	@Override
	public Object databaseHelper(Context context) {
		return new Ir_model(context);
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
