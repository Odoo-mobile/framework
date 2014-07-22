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
import android.widget.Toast;

import com.odoo.R;
import com.odoo.addons.idea.Library.Keys;
import com.odoo.addons.idea.model.BookBook;
import com.odoo.addons.idea.model.BookBook.BookAuthor;
import com.odoo.addons.idea.model.BookBook.BookCategory;
import com.odoo.addons.idea.model.BookBook.BookStudent;
import com.odoo.orm.OColumn;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OModel;
import com.odoo.orm.OValues;
import com.odoo.support.BaseFragment;
import com.odoo.util.OControls;
import com.odoo.util.drawer.DrawerItem;

public class LibraryDetail extends BaseFragment {

	private View mView = null;
	private Keys mKey = null;
	private Integer mId = null;
	private OForm mForm = null;
	private Boolean mEditMode = false;
	private ODataRow mRecord = null;
	private Menu mMenu = null;
	private OModel mModel = null;

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
		switch (mKey) {
		case Books:
			OControls.setVisible(mView, R.id.odooFormBooks);
			mForm = (OForm) mView.findViewById(R.id.odooFormBooks);
			mModel = new BookBook(getActivity());
			if (mId != null) {
				mRecord = mModel.select(mId);
				mForm.initForm(mRecord);
			} else {
				mForm.setModel(mModel);
				mForm.setEditable(mEditMode);
			}
			break;
		case Authors:
			OControls.setVisible(mView, R.id.odooFormAuthors);
			mForm = (OForm) mView.findViewById(R.id.odooFormAuthors);
			mModel = new BookAuthor(getActivity());
			if (mId != null) {
				mRecord = mModel.select(mId);
				mForm.initForm(mRecord);
			} else {
				mForm.setModel(mModel);
				mForm.setEditable(mEditMode);
			}
			break;
		case Students:
			OControls.setVisible(mView, R.id.odooFormStudents);
			mForm = (OForm) mView.findViewById(R.id.odooFormStudents);
			BookStudent student = new BookStudent(getActivity());
			if (mId != null) {
				mRecord = student.select(mId);
				mForm.initForm(mRecord);
			} else {
				mForm.setModel(student);
				mForm.setEditable(mEditMode);
			}
			break;
		case Category:
			OControls.setVisible(mView, R.id.odooFormCategories);
			mForm = (OForm) mView.findViewById(R.id.odooFormCategories);
			BookCategory category = new BookCategory(getActivity());
			if (mId != null) {
				mRecord = category.select(mId);
				mForm.initForm(mRecord);
			} else {
				mForm.setModel(category);
				mForm.setEditable(mEditMode);
			}
			break;
		}

	}

	private void updateMenu(boolean edit_mode) {
		if (mMenu != null) {
			mMenu.findItem(R.id.menu_library_detail_save).setVisible(edit_mode);
			mMenu.findItem(R.id.menu_library_detail_edit)
					.setVisible(!edit_mode);
		}
	}

	private void initArgs() {
		Bundle args = getArguments();
		mKey = Library.Keys.valueOf(args.getString("key"));
		if (args.containsKey(OColumn.ROW_ID)) {
			mId = args.getInt(OColumn.ROW_ID);
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
		case R.id.menu_library_detail_delete:
			if (mModel.delete(mRecord.getInt("id"))) {
				Toast.makeText(getActivity(), "Record deleted",
						Toast.LENGTH_LONG).show();
				getActivity().getSupportFragmentManager().popBackStack();
			}
			break;
		case R.id.menu_library_detail_save:
			mEditMode = false;
			OValues values = mForm.getFormValues();
			if (values != null) {
				updateMenu(mEditMode);
				if (mId != null) {
					switch (mKey) {
					case Books:
						new BookBook(getActivity()).update(values, mId);
						break;
					case Authors:
						new BookAuthor(getActivity()).update(values, mId);
						break;
					case Students:
						new BookStudent(getActivity()).update(values, mId);
						break;
					case Category:
						new BookCategory(getActivity()).update(values, mId);
						break;
					}
				} else {
					switch (mKey) {
					case Books:
						new BookBook(getActivity()).create(values);
						break;
					case Authors:
						new BookAuthor(getActivity()).create(values);
						break;
					case Students:
						new BookStudent(getActivity()).create(values);
						break;
					case Category:
						new BookCategory(getActivity()).create(values);
						break;
					}
				}
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
