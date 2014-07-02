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
import com.odoo.orm.OEValues;
import com.odoo.support.BaseFragment;
import com.odoo.util.drawer.DrawerItem;

public class LibraryDetail extends BaseFragment {

	View mView = null;
	Keys mKey = null;
	Integer mId = null;
	Boolean mLocalRecord = false;
	OForm mForm = null;
	Boolean mEditMode = false;
	OEDataRow mRecord = null;
	Menu mMenu = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		initArgs();
		setHasOptionsMenu(true);
		mView = inflater.inflate(R.layout.fragment_library_detail, container,
				false);
		return mView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		init();
	}

	private void init() {
		updateMenu(mEditMode);
		mForm = (OForm) mView.findViewById(R.id.odooFormBooks);
		switch (mKey) {
		case Books:
			BookBook books = new BookBook(getActivity());
			if (mId != null) {
				mRecord = books.select(mId, mLocalRecord);
				mForm.initForm(mRecord);
			} else {
				mForm.setModel(books);
				mForm.setEditable(mEditMode);
			}
			break;
		case Authors:
			break;
		case Students:
			break;
		case Category:
			break;
		}

	}

	private void updateMenu(boolean edit_mode) {
		mMenu.findItem(R.id.menu_library_detail_save).setVisible(edit_mode);
		mMenu.findItem(R.id.menu_library_detail_edit).setVisible(!edit_mode);
	}

	private void initArgs() {
		Bundle args = getArguments();
		mKey = Library.Keys.valueOf(args.getString("key"));
		if (args.containsKey("id")) {
			mLocalRecord = args.getBoolean("local_record");
			if (mLocalRecord) {
				mId = args.getInt("local_id");
			} else
				mId = args.getInt("id");
		} else
			mEditMode = true;
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
			updateMenu(mEditMode);
			mForm.setEditable(mEditMode);
			break;
		case R.id.menu_library_detail_save:
			mEditMode = false;
			OEValues values = mForm.getFormValues();
			if (values != null) {
				updateMenu(mEditMode);
				if (mId != null)
					new BookBook(getActivity()).update(values, mId,
							mLocalRecord);
				else
					new BookBook(getActivity()).create(values);
				getActivity().getSupportFragmentManager().popBackStack();
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.menu_fragment_library_detail, menu);
		mMenu = menu;
		updateMenu(mEditMode);
	}

}
