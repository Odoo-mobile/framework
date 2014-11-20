package com.odoo.addons.partners;

import java.util.List;

import odoo.ODomain;
import odoo.controls.OField;
import odoo.controls.OForm;
import odoo.controls.OSearchableMany2One.DialogListRowViewListener;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.odoo.R;
import com.odoo.base.res.ResPartner;
import com.odoo.orm.OColumn;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OValues;
import com.odoo.support.fragment.BaseFragment;
import com.odoo.util.OControls;
import com.odoo.util.drawer.DrawerItem;

public class PartnersDetail extends BaseFragment implements
		DialogListRowViewListener {

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

		// ManyToOne searchable control with dialog callback
		OField company_id = (OField) mForm.findViewById(R.id.parent_id);
		company_id.setManyToOneSearchableCallbacks(this);
		OField country_id = (OField) mForm.findViewById(R.id.country_id);
		country_id
				.setManyToOneSearchableCallbacks(new DialogListRowViewListener() {

					@Override
					public ODomain onDialogSearchChange(String filter) {
						ODomain domain = new ODomain();
						domain.add("name", "ilike", filter);
						return domain;
					}

					@Override
					public View onDialogListRowGetView(ODataRow data,
							int position, View view, ViewGroup parent) {
						return null;
					}

					@Override
					public void bindDisplayLayoutLoad(ODataRow data, View layout) {
						TextView txv = (TextView) layout;
						if (data != null) {
							txv.setText(data.getString("name"));
						}
					}
				});
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
	public void onMenuCreated(Menu menu) {
		mMenu = menu;
		updateMenu(mEditMode);
	}

	@Override
	public int getMenuForTablet() {
		return R.menu.menu_partners_detail;
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

	@Override
	public View onDialogListRowGetView(ODataRow data, int position, View view,
			ViewGroup parent) {
		View form = LayoutInflater.from(getActivity()).inflate(
				R.layout.partners_company_item, parent, false);
		OControls.setText(form, R.id.company_name, data.getString("name"));
		return form;
	}

	@Override
	public ODomain onDialogSearchChange(String filter) {
		ODomain domain = new ODomain();
		domain.add("name", "ilike", filter);
		return domain;
	}

	@Override
	public void bindDisplayLayoutLoad(ODataRow data, View layout) {
		TextView txvName = (TextView) layout.findViewById(R.id.company_name);
		if (data == null) {
			data = new ODataRow();
			data.put("name", "Select Company");
		}
		txvName.setText(data.getString("name"));
	}
}
