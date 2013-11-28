package com.openerp.base.account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;

import com.openerp.R;
import com.openerp.auth.OpenERPAccountManager;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.OEUser;
import com.openerp.support.listview.OEListViewAdapter;
import com.openerp.support.listview.OEListViewOnCreateListener;
import com.openerp.support.listview.OEListViewRows;
import com.openerp.util.drawer.DrawerItem;

public class AccountsDetail extends BaseFragment {
	View rootView = null;
	GridView gridAccounts = null;
	OEListViewAdapter adapter = null;
	List<OEListViewRows> accounts = new ArrayList<OEListViewRows>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		rootView = inflater.inflate(R.layout.fragment_all_accounts_detail,
				container, false);
		scope = new AppScope(this);
		scope.context().setTitle("Accounts");
		setupGrid();
		return rootView;
	}

	private void setupGrid() {
		gridAccounts = (GridView) rootView.findViewById(R.id.gridAccounts);
		String[] from = new String[] { "image", "name", "host" };
		int[] to = new int[] { R.id.imgAccountPic, R.id.txvAccountName,
				R.id.txvAccountHost };
		adapter = new OEListViewAdapter(scope.context(),
				R.layout.fragment_account_detail_item, getAccounts(), from, to,
				null);
		adapter.addViewListener(new OEListViewOnCreateListener() {

			@Override
			public View listViewOnCreateListener(int position, View row_view,
					final OEListViewRows row_data) {
				View newView = row_view;
				Button btnLogin = (Button) newView.findViewById(R.id.btnLogin);
				Button btnLogout = (Button) newView
						.findViewById(R.id.btnLogout);
				Button btnDelete = (Button) newView
						.findViewById(R.id.btnDelete);
				btnDelete.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						String accountName = row_data.getRow_data().get("name")
								.toString();
						Dialog deleteAccount = deleteAccount(accountName);
						deleteAccount.show();

					}
				});
				if ((Boolean) row_data.getRow_data().get("is_active")) {
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
									row_data.getRow_data().get("name")
											.toString());
							scope.context().finish();
							scope.context().startActivity(
									scope.context().getIntent());
						}
					});
				}
				return newView;
			}
		});
		adapter.addImageColumn("image");
		gridAccounts.setAdapter(adapter);

	}

	private List<OEListViewRows> getAccounts() {
		List<OEListViewRows> list = new ArrayList<OEListViewRows>();
		for (OEUser account : OpenERPAccountManager.fetchAllAccounts(scope
				.context())) {
			HashMap<String, Object> row_data = new HashMap<String, Object>();

			row_data.put("name", account.getAndroidName());
			row_data.put("image", account.getAvatar());
			row_data.put("host", account.getHost());
			row_data.put("is_active", account.isIsactive());
			OEListViewRows row = new OEListViewRows(Integer.parseInt(account
					.getUser_id()), row_data);
			list.add(row);
		}
		return list;
	}

	@Override
	public Object databaseHelper(Context context) {
		return null;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_fragment_all_accounts, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add_new_account:
			Fragment fragment = new AccountFragment();
			scope.context().fragmentHandler.setBackStack(true, null);
			scope.context().fragmentHandler.replaceFragmnet(fragment);
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
		builder.setTitle("Confirm")
				.setMessage("Are you sure want to logout?")

				// Set the action buttons
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// User clicked OK, so save the result somewhere
								// or return them to the component that opened
								// the dialog
								// TODO Auto-generated method stub
								OpenERPAccountManager.logoutUser(scope
										.context(), scope.User()
										.getAndroidName());
								scope.context().finish();
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								return;
							}
						});

		return builder.create();
	}

	private Dialog deleteAccount(final String accountName) {

		// Initialize the Alert Dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(scope.context());
		// Source of the data in the DIalog

		// Set the dialog title
		builder.setTitle("Confirm")
				.setMessage("Are you sure want to delete account?")

				// Set the action buttons
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// User clicked OK, so save the result somewhere
								// or return them to the component that opened
								// the dialog
								OpenERPAccountManager.removeAccount(
										scope.context(), accountName);
								scope.context().finish();
								scope.context().startActivity(
										scope.context().getIntent());
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								return;
							}
						});

		return builder.create();
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		return null;
	}
}
