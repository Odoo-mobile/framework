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
package com.odoo.base.account;

import java.util.ArrayList;
import java.util.List;

import odoo.controls.OList;
import odoo.controls.OList.BeforeListRowCreateListener;
import odoo.controls.OList.OnListRowViewClickListener;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.odoo.R;
import com.odoo.auth.OdooAccountManager;
import com.odoo.base.login_signup.LoginSignup;
import com.odoo.orm.ODataRow;
import com.odoo.support.AppScope;
import com.odoo.support.OUser;
import com.odoo.support.fragment.BaseFragment;
import com.odoo.util.OControls;
import com.odoo.util.drawer.DrawerItem;

public class AccountsDetail extends BaseFragment implements
		BeforeListRowCreateListener, OnListRowViewClickListener {
	private View rootView = null;
	private OList gridAccounts = null;
	private List<ODataRow> mAccounts = new ArrayList<ODataRow>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		rootView = inflater.inflate(R.layout.base_account_list, container,
				false);
		scope = new AppScope(this);
		scope.main().setTitle(R.string.title_accounts);
		setupGrid();
		return rootView;
	}

	private void setupGrid() {
		gridAccounts = (OList) rootView.findViewById(R.id.gridAccounts);
		mAccounts.clear();
		mAccounts.addAll(getAccounts());
		gridAccounts.setBeforeListRowCreateListener(this);
		gridAccounts.setOnListRowViewClickListener(R.id.btnLogin, this);
		gridAccounts.setOnListRowViewClickListener(R.id.btnLogout, this);
		gridAccounts.setOnListRowViewClickListener(R.id.btnDelete, this);
		gridAccounts.initListControl(mAccounts);

	}

	private List<ODataRow> getAccounts() {
		List<ODataRow> list = new ArrayList<ODataRow>();
		for (OUser account : OdooAccountManager.fetchAllAccounts(scope
				.context())) {
			ODataRow row_data = new ODataRow();
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
			LoginSignup loginSignUp = new LoginSignup();
			startFragment(loginSignUp, true);
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
								OdooAccountManager.logoutUser(scope.context(),
										scope.User().getAndroidName());
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
								OdooAccountManager.removeAccount(
										scope.context(), accountName);
								new Handler().postDelayed(new Runnable() {

									@Override
									public void run() {
										scope.main().finish();
										scope.main().startActivity(
												scope.main().getIntent());
									}
								}, 1000);
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

	@Override
	public void beforeListRowCreate(int position, ODataRow row, View view) {
		OControls.toggleViewVisibility(view, R.id.btnLogin,
				!row.getBoolean("is_active"));
		OControls.toggleViewVisibility(view, R.id.btnLogout,
				row.getBoolean("is_active"));
	}

	@Override
	public void onRowViewClick(ViewGroup view_group, View view, int position,
			ODataRow row) {
		switch (view.getId()) {
		case R.id.btnDelete:
			String accountName = row.getString("name").toString();
			Dialog deleteAccount = deleteAccount(accountName);
			deleteAccount.show();
			break;
		case R.id.btnLogin:
			OdooAccountManager
					.loginUser(scope.context(), row.getString("name"));
			scope.main().finish();
			scope.main().startActivity(scope.main().getIntent());
			break;
		case R.id.btnLogout:
			Dialog logoutConfirm = logoutConfirmDialog();
			logoutConfirm.show();
			break;
		default:
			break;
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().setTitle("Accounts");
		getActivity().getActionBar().show();
		scope.main().lockDrawer(false);
	}
}
