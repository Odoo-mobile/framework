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
package com.openerp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import android.accounts.Account;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.openerp.auth.OpenERPAccountManager;
import com.openerp.base.account.AccountFragment;
import com.openerp.config.ModulesConfig;
import com.openerp.orm.OEHelper;
import com.openerp.support.Boot;
import com.openerp.support.FragmentHandler;
import com.openerp.support.Module;
import com.openerp.support.UserObject;
import com.openerp.support.menu.OEMenuItems;

// TODO: Auto-generated Javadoc
/**
 * The Class MainActivity.
 */
public class MainActivity extends FragmentActivity {

	public static final int RESULT_SETTINGS = 1;
	public static OEHelper openerp = null;
	public static UserObject userContext = new UserObject();
	MenuDrawerHelper drawer = null;
	private CharSequence mTitle;
	ArrayList<Module> moduleLists = null;
	public FragmentHandler fragmentHandler;
	private PullToRefreshAttacher mPullToRefreshAttacher;
	public static Context context = null;
	private OEMenuItems[] systemMenus = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
		if (findViewById(R.id.fragment_container) != null) {
			if (savedInstanceState != null) {
				return;
			}
			fragmentHandler = new FragmentHandler(MainActivity.this);
			Boot boot = new Boot(this);
			moduleLists = boot.getModules();
			refreshMenu(this);
			/**
			 * Getting Application users list. If it's null that means
			 * application does not contain any account and it will request user
			 * to create new one.
			 */
			if (OpenERPAccountManager.fetchAllAccounts(this) == null) {
				getActionBar().setDisplayHomeAsUpEnabled(false);
				getActionBar().setHomeButtonEnabled(false);
				// Starting New account setup wizard.
				Fragment fragment = new AccountFragment();
				fragmentHandler.startNewFragmnet(fragment);
				return;
			} else {
				// Application contain user account, so going for next stuff.
				// Checking that rather user have requested to create new
				// account from application account setting.
				Intent intent = getIntent();
				boolean reqForNewAccount = intent.getBooleanExtra(
						"create_new_account", false);

				// checking for logged in user and not request for new account
				// setup.
				if (OpenERPAccountManager.isAnyUser(this) && !reqForNewAccount) {
					// Starting user addon.
					startup();
					if (savedInstanceState != null) {
						return;
					}
					this.loadDefaultModule();

				} else {

					// Load new account setup wizard if user have requested to
					// create new account.
					if (reqForNewAccount) {
						getActionBar().setDisplayHomeAsUpEnabled(false);
						getActionBar().setHomeButtonEnabled(false);
						Fragment fragment = new AccountFragment();
						fragmentHandler.startNewFragmnet(fragment);
						return;
					} else {

						// If user had not request for new account than showing
						// list of account to user for login.
						Dialog dialog = onCreateDialogSingleChoice();
						dialog.setCancelable(false);
						dialog.show();
					}
				}

			}

		}

	}

	public void refreshMenu(Context context) {
		// TODO Auto-generated method stub
		drawer = new MenuDrawerHelper((MainActivity) context);
	}

	private void startup() {
		MainActivity.userContext = OpenERPAccountManager.currentUser(this);
		try {
			MainActivity.openerp = new OEHelper(this, MainActivity.userContext);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		// The attacher should always be created in the Activity's
		// onCreate
		mPullToRefreshAttacher = new PullToRefreshAttacher(this);
	}

	String[] accountNames = null;

	public Dialog onCreateDialogSingleChoice() {

		// Initialize the Alert Dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Source of the data in the DIalog

		List<UserObject> accounts = OpenERPAccountManager
				.fetchAllAccounts(this);
		accountNames = new String[accounts.size()];
		int i = 0;
		for (UserObject user : accounts) {
			accountNames[i] = user.getAndroidName();
			i++;
		}

		// Set the dialog title
		builder.setTitle("Select Account")
				// Specify the list array, the items to be selected by default
				// (null for none),
				// and the listener through which to receive callbacks when
				// items are selected
				.setSingleChoiceItems(accountNames, 1,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								OpenERPAccountManager.loginUser(context,
										accountNames[which]);
							}
						})

				// Set the action buttons
				.setPositiveButton("Login",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// User clicked OK, so save the result somewhere
								// or return them to the component that opened
								// the dialog
								// TODO Auto-generated method stub

								startup();
								// startActivity(new Intent(MainActivity.this,
								// MainActivity.class));
								// finish();
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								finish();
							}
						});

		return builder.create();
	}

	private Dialog logoutConfirmDialog() {

		// Initialize the Alert Dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
								OpenERPAccountManager.logoutUser(context,
										MainActivity.userContext
												.getAndroidName());
								finish();
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

	public UserObject getUserContext() {
		return MainActivity.userContext;
	}

	private void loadDefaultModule() {
		for (Module module : drawer.modules) {
			if (module.isLoadDefault()) {
				fragmentHandler.startNewFragmnet((Fragment) module
						.getModuleInstance());
				drawer.mDrawerList.setItemChecked(1, true);
				selectItem(1);
				// TextView menuTitle = (TextView) drawer.mDrawerList
				// .getChildAt(1).findViewById(R.id.txvMenuTitle);
				// setTitle(menuTitle.getText());
				break;
			}
		}

	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	public void setTitle(CharSequence title, CharSequence subtitle) {
		mTitle = title;
		this.setTitle(mTitle);
		getActionBar().setSubtitle(subtitle);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.

		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.menu_global_settings:
			Intent i = new Intent(this, AppSettingsActivity.class);
			startActivityForResult(i, RESULT_SETTINGS);
			return true;
		case R.id.menu_logout:
			Dialog logoutConfirm = this.logoutConfirmDialog();
			logoutConfirm.show();
			return true;
		case R.id.menu_new_account:
			getActionBar().setDisplayHomeAsUpEnabled(false);
			getActionBar().setHomeButtonEnabled(false);
			drawer.mDrawerLayout.closeDrawer(drawer.mDrawerList);
			Fragment fragment = new AccountFragment();
			fragmentHandler.replaceFragmnet(fragment);
			return true;
		default:
			if (drawer.mDrawerToggle.onOptionsItemSelected(item)) {
				return true;
			}
			return super.onOptionsItemSelected(item);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case RESULT_SETTINGS:
			Log.e("Compelted", "Settind box finish");
			break;

		}

	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		if (drawer == null) {
			drawer = new MenuDrawerHelper((MainActivity) context);
		}
		boolean drawerOpen = drawer.mDrawerLayout
				.isDrawerOpen(drawer.mDrawerList);
		if (drawerOpen) {
			menu.clear();
			getMenuInflater().inflate(R.menu.main_menu_drawer_open, menu);
			return true;
		} else {
			return super.onPrepareOptionsMenu(menu);
		}
		// menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);

	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		drawer.mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		drawer.mDrawerToggle.onConfigurationChanged(newConfig);
	}

	public void selectItem(int position) {

		Fragment fragment = null;
		if (this.systemMenus != null) {
			getActionBar().setDisplayShowTitleEnabled(true);
			getActionBar()
					.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			setTitle(this.systemMenus[position].getTitle());
			fragment = (Fragment) this.systemMenus[position]
					.getFragmentInstance();
		}
		if (fragment != null) {
			fragmentHandler.replaceFragmnet(fragment);
		}
		// update selected item and title, then close the drawer
		drawer.mDrawerList.setItemChecked(position, true);
		drawer.mDrawerLayout.closeDrawer(drawer.mDrawerList);
	}

	// PullToRefresh
	public PullToRefreshAttacher getPullToRefreshAttacher() {
		return mPullToRefreshAttacher;
	}

	public void setSystemMenus(OEMenuItems[] menus) {
		this.systemMenus = menus;
	}

	/**
	 * Sets the auto sync.
	 * 
	 * @param authority
	 *            the authority
	 * @param isON
	 *            the is on
	 */
	public void setAutoSync(String authority, boolean isON) {
		Account account = OpenERPAccountManager.getAccount(this,
				MainActivity.userContext.getAndroidName());
		ContentResolver.setSyncAutomatically(account, authority, isON);
	}

	/**
	 * Request sync.
	 * 
	 * @param authority
	 *            the authority
	 */
	public void requestSync(String authority) {
		Account account = OpenERPAccountManager.getAccount(this,
				MainActivity.userContext.getAndroidName());
		Bundle settingsBundle = new Bundle();
		settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
		ContentResolver.requestSync(account, authority, settingsBundle);
	}

	/**
	 * Sets the sync periodic.
	 * 
	 * @param authority
	 *            the authority
	 * @param interval_in_minute
	 *            the interval_in_minute
	 * @param seconds_per_minute
	 *            the seconds_per_minute
	 * @param milliseconds_per_second
	 *            the milliseconds_per_second
	 */
	public void setSyncPeriodic(String authority, long interval_in_minute,
			long seconds_per_minute, long milliseconds_per_second) {
		Account account = OpenERPAccountManager.getAccount(this,
				MainActivity.userContext.getAndroidName());
		Bundle extras = new Bundle();
		this.setAutoSync(authority, true);
		ContentResolver.setIsSyncable(account, authority, 1);
		final long sync_interval = interval_in_minute * seconds_per_minute
				* milliseconds_per_second;
		ContentResolver.addPeriodicSync(account, authority, extras,
				sync_interval);

	}
}
