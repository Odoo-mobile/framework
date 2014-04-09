/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http:www.openerp.com>)
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
package com.openerp.base.account;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.openerp.R;
import com.openerp.auth.OpenERPAccountManager;
import com.openerp.orm.OEDataRow;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.OEUser;
import com.openerp.support.fragment.FragmentListener;
import com.openerp.support.listview.OEListAdapter;
import com.openerp.util.Base64Helper;
import com.openerp.util.drawer.DrawerItem;

public class AccountsDetail extends BaseFragment {
	View rootView = null;
	GridView gridAccounts = null;
	OEListAdapter mAdapter = null;
	List<Object> mAccounts = new ArrayList<Object>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		rootView = inflater.inflate(R.layout.fragment_all_accounts_detail,
				container, false);
		scope = new AppScope(this);
		scope.main().setTitle(R.string.title_accounts);
		setupGrid();
		return rootView;
	}

	private void setupGrid() {
		gridAccounts = (GridView) rootView.findViewById(R.id.gridAccounts);
		mAccounts = new ArrayList<Object>(getAccounts());
		mAdapter = new OEListAdapter(getActivity(),
				R.layout.fragment_account_detail_item, mAccounts) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View mView = convertView;
				if (mView == null) {
					mView = getActivity().getLayoutInflater().inflate(
							getResource(), parent, false);
				}
				TextView txvName, txvHost;
				txvName = (TextView) mView.findViewById(R.id.txvAccountName);
				txvHost = (TextView) mView.findViewById(R.id.txvAccountHost);
				ImageView imgUserPic = (ImageView) mView
						.findViewById(R.id.imgAccountPic);

				final OEDataRow row_data = (OEDataRow) mAccounts.get(position);
				txvName.setText(row_data.getString("name"));
				txvHost.setText(row_data.getString("host"));
				if (!row_data.getString("image").equals("false"))
					imgUserPic.setImageBitmap(Base64Helper.getBitmapImage(
							getActivity(), row_data.getString("image")));
				Button btnLogin = (Button) mView.findViewById(R.id.btnLogin);
				Button btnLogout = (Button) mView.findViewById(R.id.btnLogout);
				Button btnDelete = (Button) mView.findViewById(R.id.btnDelete);
				btnDelete.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						String accountName = row_data.getString("name")
								.toString();
						Dialog deleteAccount = deleteAccount(accountName);
						deleteAccount.show();

					}
				});
				if (row_data.getBoolean("is_active")) {
					btnLogout.setVisibility(View.VISIBLE);
					btnLogin.setVisibility(View.GONE);
					btnLogout.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							Dialog logoutConfirm = logoutConfirmDialog();
							logoutConfirm.show();
						}
					});
				} else {
					btnLogout.setVisibility(View.GONE);
					btnLogin.setVisibility(View.VISIBLE);
					btnLogin.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							OpenERPAccountManager.loginUser(scope.context(),
									row_data.getString("name"));
							scope.main().finish();
							scope.main()
									.startActivity(scope.main().getIntent());
						}
					});
				}
				return mView;
			}
		};
		gridAccounts.setAdapter(mAdapter);

	}

	private List<Object> getAccounts() {
		List<Object> list = new ArrayList<Object>();
		for (OEUser account : OpenERPAccountManager.fetchAllAccounts(scope
				.context())) {
			OEDataRow row_data = new OEDataRow();

			row_data.put("name", account.getAndroidName());
			row_data.put("image", account.getAvatar());
			row_data.put("host", account.getHost());
			row_data.put("is_active", account.isIsactive());
			list.add(row_data);
		}
		return list;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_fragment_all_accounts, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add_new_account:
			AccountFragment fragment = new AccountFragment();
			FragmentListener mFragment = (FragmentListener) getActivity();
			mFragment.startMainFragment(fragment, true);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private Dialog logoutConfirmDialog() {

		// Initialize the Alert Dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(scope.context());
		// Source of the data in the DIalog

		// Set the dialog title
		builder.setTitle(R.string.title_confirm)
				.setMessage(R.string.toast_are_you_sure_logout)

				// Set the action buttons
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// User clicked OK, so save the result somewhere
								// or return them to the component that opened
								// the dialog
								OpenERPAccountManager.logoutUser(scope
										.context(), scope.User()
										.getAndroidName());
								scope.main().finish();
							}
						}).setNegativeButton(R.string.label_cancel, null);

		return builder.create();
	}

	private Dialog deleteAccount(final String accountName) {

		// Initialize the Alert Dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(scope.context());
		// Source of the data in the DIalog

		// Set the dialog title
		builder.setTitle(R.string.title_confirm)
				.setMessage(R.string.toast_are_you_sure_delete_account)

				// Set the action buttons
				.setPositiveButton(R.string.label_yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// User clicked OK, so save the result somewhere
								// or return them to the component that opened
								// the dialog
								OpenERPAccountManager.removeAccount(
										scope.context(), accountName);
								scope.main().finish();
								scope.main().startActivity(
										scope.main().getIntent());
							}
						}).setNegativeButton(R.string.label_cancel, null);

		return builder.create();
	}

	@Override
	public Object databaseHelper(Context context) {
		return null;
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		return null;
	}
}
