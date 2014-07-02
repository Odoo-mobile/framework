/*
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * 
 */

package com.odoo.addons.idea;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.odoo.R;
import com.odoo.addons.idea.model.BookBook;
import com.odoo.addons.idea.model.BookBook.BookAuthor;
import com.odoo.addons.idea.model.BookBook.BookCategory;
import com.odoo.addons.idea.model.BookBook.BookStudent;
import com.odoo.addons.idea.providers.library.LibraryProvider;
import com.odoo.orm.OEDataRow;
import com.odoo.receivers.SyncFinishReceiver;
import com.odoo.support.AppScope;
import com.odoo.support.BaseFragment;
import com.odoo.support.listview.OEListAdapter;
import com.odoo.util.OEControls;
import com.odoo.util.drawer.DrawerItem;
import com.openerp.OETouchListener;
import com.openerp.OETouchListener.OnPullListener;

/**
 * The Class Idea.
 */
public class Library extends BaseFragment implements OnPullListener,
		OnItemClickListener {

	public static final String TAG = Library.class.getSimpleName();

	enum Keys {
		Books, Authors, Students, Category
	}

	View mView = null;
	ListView mListControl = null;
	List<Object> mListRecords = new ArrayList<Object>();
	OEListAdapter mListAdapter = null;
	OETouchListener mTouchListener = null;
	DataLoader mDataLoader = null;
	Keys mCurrentKey = Keys.Books;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		scope = new AppScope(this);
		mView = inflater.inflate(R.layout.fragment_library, container, false);
		init();
		return mView;
	}

	private void init() {
		checkArguments();
		mListControl = (ListView) mView.findViewById(R.id.listRecords);
		mListAdapter = new OEListAdapter(getActivity(),
				R.layout.fragment_library_custom_view, mListRecords) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View mView = convertView;
				if (mView == null) {
					mView = getActivity().getLayoutInflater().inflate(
							getResource(), parent, false);
				}
				OEDataRow row = (OEDataRow) mListRecords.get(position);
				createView(mView, row);
				return mView;
			}
		};
		mTouchListener = scope.main().getTouchAttacher();
		mTouchListener.setPullableView(mListControl, this);
		mListControl.setAdapter(mListAdapter);
		mListControl.setOnItemClickListener(this);
		mDataLoader = new DataLoader();
		mDataLoader.execute();
	}

	class DataLoader extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			scope.main().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (db().isEmptyTable()) {
						scope.main().requestSync(LibraryProvider.AUTHORITY);
					}
					mListRecords.clear();
					switch (mCurrentKey) {
					case Books:
						mListRecords.addAll(db().select());
						break;
					case Authors:
						BookAuthor author = new BookAuthor(getActivity());
						mListRecords.addAll(author.select());
						break;
					case Category:
						BookCategory category = new BookCategory(getActivity());
						mListRecords.addAll(category.select());
						break;
					case Students:
						BookStudent student = new BookStudent(getActivity());
						mListRecords.addAll(student.select());
						break;
					}
				}
			});
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mListAdapter.notifiyDataChange(mListRecords);
			OEControls.setGone(mView, R.id.loadingProgress);
		}

	}

	private void createView(View mView, OEDataRow row) {
		OEControls.setText(mView, R.id.txvName, row.getString("name"));
		List<String> values = new ArrayList<String>();
		switch (mCurrentKey) {
		case Books:
			for (OEDataRow category : row.getM2MRecord("category_ids")
					.browseEach()) {
				values.add("- " + category.getString("name"));
			}
			break;
		case Authors:
			OEDataRow country = row.getM2ORecord("country_id").browse();
			if (country != null) {
				values.add(country.getString("name"));
			}

			break;
		case Students:
			for (OEDataRow book : row.getO2MRecord("book_ids").browseEach()) {
				values.add("- " + book.getString("name"));
			}
			break;
		case Category:
		}
		OEControls.setText(mView, R.id.txvValues, TextUtils.join("\n", values));
	}

	private void checkArguments() {
		Bundle arg = getArguments();
		mCurrentKey = Keys.valueOf(arg.getString("library"));
	}

	@Override
	public Object databaseHelper(Context context) {
		return new BookBook(context);
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		List<DrawerItem> menu = new ArrayList<DrawerItem>();
		menu.add(new DrawerItem(TAG, "Library", true));
		menu.add(new DrawerItem(TAG, "Books", count(context, Keys.Books), 0,
				object(Keys.Books)));
		menu.add(new DrawerItem(TAG, "Authors", count(context, Keys.Authors),
				0, object(Keys.Authors)));
		menu.add(new DrawerItem(TAG, "Students", count(context, Keys.Students),
				0, object(Keys.Students)));
		menu.add(new DrawerItem(TAG, "Book Category", count(context,
				Keys.Category), 0, object(Keys.Category)));
		return menu;
	}

	private int count(Context context, Keys key) {
		int count = 0;
		switch (key) {
		case Authors:
			count = new BookAuthor(context).count();
			break;
		case Books:
			count = new BookBook(context).count();
			break;
		case Category:
			count = new BookCategory(context).count();
			break;
		case Students:
			count = new BookStudent(context).count();
			break;
		default:
			break;
		}
		return count;
	}

	private Fragment object(Keys value) {
		Library library = new Library();
		Bundle args = new Bundle();
		args.putString("library", value.toString());
		library.setArguments(args);
		return library;
	}

	@Override
	public void onPullStarted(View arg0) {
		scope.main().requestSync(LibraryProvider.AUTHORITY);
	}

	@Override
	public void onResume() {
		super.onResume();
		scope.main().registerReceiver(mSyncFinishReceiver,
				new IntentFilter(SyncFinishReceiver.SYNC_FINISH));
	}

	@Override
	public void onPause() {
		super.onPause();
		scope.main().unregisterReceiver(mSyncFinishReceiver);
	}

	SyncFinishReceiver mSyncFinishReceiver = new SyncFinishReceiver() {
		@Override
		public void onReceive(Context context, android.content.Intent intent) {
			scope.main().refreshDrawer(TAG);
			mTouchListener.setPullComplete();
			if (mDataLoader != null) {
				mDataLoader.cancel(true);
			}
			mDataLoader = new DataLoader();
			mDataLoader.execute();
		}
	};

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		OEDataRow row = (OEDataRow) mListRecords.get(position);
		LibraryDetail library = new LibraryDetail();
		Bundle bundle = new Bundle();
		bundle.putString("key", mCurrentKey.toString());
		bundle.putAll(row.getPrimaryBundleData());
		library.setArguments(bundle);
		startFragment(library, true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.menu_fragment_library, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_library_detail_create) {
			LibraryDetail library = new LibraryDetail();
			Bundle bundle = new Bundle();
			bundle.putString("key", mCurrentKey.toString());
			library.setArguments(bundle);
			startFragment(library, true);
		}
		return super.onOptionsItemSelected(item);
	}
}
