package com.odoo.addons.partners;

import java.util.List;

import odoo.controls.OForm;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.odoo.R;
import com.odoo.base.res.ResPartner;
import com.odoo.orm.OColumn;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OValues;
import com.odoo.support.fragment.BaseFragment;
import com.odoo.util.drawer.DrawerItem;

public class PartnersDetail extends BaseFragment {

	View mView = null;
	OForm mForm = null;
	private Integer mId = null;
	ODataRow mRecord = new ODataRow();
	Boolean mEditMode = false;
	Menu mMenu = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		initargs();
		setHasOptionsMenu(true);
		mView = inflater.inflate(R.layout.partners_detail, container, false);
		return mView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		init();
	}

	private void init() {
		mForm = (OForm) mView.findViewById(R.id.partnerDetail);
		ResPartner resPartner = new ResPartner(getActivity());
		if (mId != null) {
			mRecord = resPartner.select(mId);
			mForm.initForm(mRecord);

		} else {
			mForm.setModel(resPartner);
		}
		mForm.setEditable(mEditMode);
	}

	private void initargs() {
		Bundle arg = getArguments();
		if (arg != null && arg.containsKey(OColumn.ROW_ID)) {
			mId = arg.getInt(OColumn.ROW_ID);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_partners_detail, menu);
		mMenu = menu;
		updateMenu(mEditMode);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_partner_detail_edit:
			mEditMode = !mEditMode;
			updateMenu(mEditMode);
			mForm.setEditable(mEditMode);
			break;
		case R.id.menu_partner_detail_save:
			mEditMode = false;
			OValues values = mForm.getFormValues();
			if (values != null) {
				updateMenu(mEditMode);
				if (mId != null) {
					db().update(values, mId);
				} else {
					db().create(values);
				}
				getActivity().getSupportFragmentManager().popBackStack();
			}
			break;
		case R.id.menu_partner_detail_delete:
			db().delete(mId);
			getActivity().getSupportFragmentManager().popBackStack();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateMenu(Boolean editMode) {
		mMenu.findItem(R.id.menu_partner_detail_edit).setVisible(!editMode);
		mMenu.findItem(R.id.menu_partner_detail_save).setVisible(editMode);
	}

	@Override
	public Object databaseHelper(Context context) {
		return new ResPartner(context);
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		return null;
	}

}
