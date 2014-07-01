package com.odoo.addons.idea;

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
import com.odoo.addons.idea.Library.Keys;
import com.odoo.addons.idea.model.BookBook;
import com.odoo.orm.OEDataRow;
import com.odoo.support.BaseFragment;
import com.odoo.util.drawer.DrawerItem;

public class LibraryDetail extends BaseFragment {

	View mView = null;
	Keys mKey = null;
	Integer mId = null;
	OForm mForm = null;
	Boolean mEditMode = false;
	OEDataRow mRecord = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		mView = inflater.inflate(R.layout.fragment_library_detail, container,
				false);
		initArgs();
		return mView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		init();
	}

	private void init() {
		mForm = (OForm) mView.findViewById(R.id.odooFormBooks);
		switch (mKey) {
		case Books:
			BookBook books = new BookBook(getActivity());
			mRecord = books.select(mId);
			mForm.initForm(mRecord);
			break;
		case Authors:
			break;
		case Students:
			break;
		case Category:
			break;
		}
	}

	private void initArgs() {
		Bundle args = getArguments();
		mKey = Library.Keys.valueOf(args.getString("key"));
		mId = args.getInt("id");
	}

	@Override
	public Object databaseHelper(Context context) {
		return null;
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		return null;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_library_detail_edit:
			mEditMode = !mEditMode;
			mForm.initForm(mRecord, mEditMode);
			break;
		case R.id.menu_library_detail_save:
			mEditMode = false;
			// mForm.initForm(mRecord, mEditMode);
			if (mForm.getFormValues() != null) {
				getActivity().getSupportFragmentManager().popBackStack();
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
		inflater.inflate(R.menu.menu_fragment_library_detail, menu);
		if (mEditMode) {
			menu.findItem(R.id.menu_library_detail_save).setVisible(false);
		} else {
			menu.findItem(R.id.menu_library_detail_save).setVisible(true);
		}
	}

}
