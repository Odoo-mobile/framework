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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.accounts.Account;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncAdapterType;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.openerp.addons.messages.MessageComposeActivty;
import com.openerp.addons.note.ComposeNoteActivity;
import com.openerp.auth.OpenERPAccountManager;
import com.openerp.base.about.AboutFragment;
import com.openerp.base.account.AccountFragment;
import com.openerp.base.account.AccountsDetail;
import com.openerp.base.account.UserProfile;
import com.openerp.base.res.Res_PartnerDBHelper;
import com.openerp.config.ModulesConfig;
import com.openerp.support.Boot;
import com.openerp.support.FragmentHandler;
import com.openerp.support.Module;
import com.openerp.support.OEUser;
import com.openerp.support.menu.OEMenuItems;
import com.openerp.util.Base64Helper;
import com.openerp.util.OnBackButtonPressedListener;

/**
 * The Class MainActivity.
 */
public class MainActivity extends FragmentActivity {

	public static final int RESULT_SETTINGS = 1;
	public static boolean set_setting_menu = false;
	public static Context context = null;
	public FragmentHandler fragmentHandler;

	public enum SETTING_KEYS {
		GLOBAL_SETTING, PROFILE, LOGOUT, ACCOUNTS, ADD_ACCOUNT, ABOUT_US
	}

	private MenuDrawerHelper drawer = null;
	private CharSequence mTitle;
	private List<Module> moduleLists = null;
	private PullToRefreshAttacher mPullToRefreshAttacher;
	private OEMenuItems[] systemMenus = null;
	private ListView drawablelist = null;
	private OnBackButtonPressedListener backPressed = null;
	private OEUser oeUser = null;
	private Boot boot = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
		drawablelist = (ListView) findViewById(R.id.left_drawer);
		if (findViewById(R.id.fragment_container) != null) {
			fragmentHandler = new FragmentHandler(this);
			boot = new Boot(this);
			moduleLists = boot.getModules();
			refreshMenu(this, boot.getAppMenu());
			if (savedInstanceState != null) {
				mPullToRefreshAttacher = new PullToRefreshAttacher(this);
				return;
			}
			/**
			 * Getting Application users list. If it's null that means
			 * application does not contain any account and it will request user
			 * to create new one.
			 */
			if (OpenERPAccountManager.hasAccounts(this) == false) {
				getActionBar().setDisplayHomeAsUpEnabled(false);
				getActionBar().setHomeButtonEnabled(false);
				drawer.lockDrawer(true);
				// Starting New account setup wizard.
				Fragment fragment = new AccountFragment();
				fragmentHandler.setBackStack(true, null);
				fragmentHandler.startNewFragmnet(fragment);

				return;
			} else {
				// Application contain user account, so going for next stuff.
				// Checking that rather user have requested to create new
				// account from application account setting.
				drawer.lockDrawer(false);
				Intent intent = getIntent();
				boolean reqForNewAccount = intent.getBooleanExtra(
						"create_new_account", false);

				// checking for logged in user and not request for new account
				// setup.
				if (OpenERPAccountManager.isAnyUser(this) && !reqForNewAccount) {
					oeUser = OEUser.current(context);
					// The attacher should always be created in the Activity's
					// onCreate
					mPullToRefreshAttacher = new PullToRefreshAttacher(this);
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
		RefreshMenu menurefresh = new RefreshMenu();
		menurefresh.execute((Void) null);
	}

	class RefreshMenu extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					drawer = new MenuDrawerHelper((MainActivity) context);
				}
			});
			return null;
		}

	}

	public void refreshMenu(Context context, List<OEMenuItems> menu_list) {
		drawer = new MenuDrawerHelper((MainActivity) context, menu_list);
	}

	String[] accountNames = null;
	String selectedAccountName = "";

	public Dialog onCreateDialogSingleChoice() {

		// Initialize the Alert Dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Source of the data in the DIalog

		List<OEUser> accounts = OpenERPAccountManager.fetchAllAccounts(this);
		accountNames = new String[accounts.size()];
		int i = 0;
		for (OEUser user : accounts) {
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
								selectedAccountName = accountNames[which];
							}
						})
				.setNeutralButton("New", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						getActionBar().setDisplayHomeAsUpEnabled(false);
						getActionBar().setHomeButtonEnabled(false);
						Fragment fragment = new AccountFragment();
						fragmentHandler.startNewFragmnet(fragment);
					}
				})
				// Set the action buttons
				.setPositiveButton("Login",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								OpenERPAccountManager.loginUser(context,
										selectedAccountName);
								finish();
								startActivity(getIntent());
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
								OpenERPAccountManager.logoutUser(context,
										oeUser.getAndroidName());
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

	public OEUser getUserContext() {
		return oeUser;
	}

	/* Find menu's position in left drawable listview */
	public int findPosition(String moduleName) {
		for (int i = 0; i < drawablelist.getCount(); i++) {
			OEMenuItems menu = (OEMenuItems) drawablelist.getItemAtPosition(i);
			if ((menu.getTitle()).equalsIgnoreCase(moduleName)) {
				return i + 1;
			}
		}
		return 0;
	}

	private void loadDefaultModule() {
		Module module = new ModulesConfig().getDefaultModule();
		if (getIntent().getAction() != null
				&& !getIntent().getAction().toString()
						.equalsIgnoreCase("android.intent.action.MAIN")) {
			if (getIntent().getAction().toString()
					.equalsIgnoreCase("composeMessage")) {
				startActivity(new Intent(context, MessageComposeActivty.class));
			}
			if (getIntent().getAction().toString()
					.equalsIgnoreCase("composeNote")) {
				startActivity(new Intent(context, ComposeNoteActivity.class));
			}
			selectItem(findPosition(getIntent().getAction().toString()));
		} else {
			// application open by App ICON
			fragmentHandler.startNewFragmnet((Fragment) module
					.getModuleInstance());
			drawer.mDrawerList.setItemChecked(1, true);
			selectItem(1);
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
		if (drawer.mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public boolean onSettingItemSelected(SETTING_KEYS key) {
		switch (key) {
		case GLOBAL_SETTING:
			set_setting_menu = false;
			Intent i = new Intent(this, AppSettingsActivity.class);
			startActivityForResult(i, RESULT_SETTINGS);
			return true;
		case LOGOUT:
			Dialog logoutConfirm = this.logoutConfirmDialog();
			logoutConfirm.show();
			return true;
		case ABOUT_US:
			set_setting_menu = true;
			getActionBar().setDisplayHomeAsUpEnabled(false);
			getActionBar().setHomeButtonEnabled(false);
			drawer.mDrawerLayout.closeDrawer(drawer.mDrawerList);
			Fragment about = new AboutFragment();
			fragmentHandler.setBackStack(true, null);
			fragmentHandler.replaceFragmnet(about);
			return true;
		case ADD_ACCOUNT:
			set_setting_menu = true;
			getActionBar().setDisplayHomeAsUpEnabled(false);
			getActionBar().setHomeButtonEnabled(false);
			drawer.mDrawerLayout.closeDrawer(drawer.mDrawerList);
			Fragment fragment = new AccountFragment();
			fragmentHandler.setBackStack(true, null);
			fragmentHandler.replaceFragmnet(fragment);
			return true;
		case ACCOUNTS:
			set_setting_menu = true;
			drawer.mDrawerLayout.closeDrawer(drawer.mDrawerList);
			Fragment acFragment = new AccountsDetail();
			fragmentHandler.setBackStack(true, null);
			fragmentHandler.replaceFragmnet(acFragment);
			return true;
		case PROFILE:
			set_setting_menu = true;
			drawer.mDrawerLayout.closeDrawer(drawer.mDrawerList);
			Fragment profileFragment = new UserProfile();
			fragmentHandler.setBackStack(true, null);
			fragmentHandler.replaceFragmnet(profileFragment);
			return true;
		default:
			return true;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case RESULT_SETTINGS:
			handleSyncProvider();
			break;
		}

	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		if (drawer == null) {
			drawer = new MenuDrawerHelper((MainActivity) context,
					boot.getAppMenu());
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

	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		if (drawer != null) {
			drawer.mDrawerToggle.syncState();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		drawer.mDrawerToggle.onConfigurationChanged(newConfig);
	}

	public void selectItem(int position) {
		set_setting_menu = false;
		Fragment fragment = null;
		if (this.systemMenus != null && this.systemMenus.length > 0) {
			getActionBar().setDisplayShowTitleEnabled(true);
			getActionBar()
					.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

			setTitle(this.systemMenus[position].getTitle());

			Object instance = this.systemMenus[position].getFragmentInstance();
			if (instance instanceof Intent) {
				startActivity((Intent) instance);
				return;
			} else {
				fragment = (Fragment) instance;
				if (fragment.getArguments() != null
						&& fragment.getArguments().containsKey("settings")) {
					onSettingItemSelected(SETTING_KEYS.valueOf(fragment
							.getArguments().get("settings").toString()));

				} else {
					if (this.systemMenus[position].hasMenuTagColor()
							&& !fragment.getArguments()
									.containsKey("tag_color")) {
						Bundle tagcolor = fragment.getArguments();
						tagcolor.putInt("tag_color",
								this.systemMenus[position].getMenuTagColor());
						fragment.setArguments(tagcolor);
					}
				}
			}
		} else {
			Toast.makeText(this, "No Module installed on server !",
					Toast.LENGTH_LONG).show();
			finish();
		}
		if (fragment != null
				&& !fragment.getArguments().containsKey("settings")) {
			fragmentHandler.setBackStack(false, null);
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
		try {
			Account account = OpenERPAccountManager.getAccount(this, OEUser
					.current(context).getAndroidName());
			if (!ContentResolver.isSyncActive(account, authority)) {
				ContentResolver.setSyncAutomatically(account, authority, isON);
			}
		} catch (NullPointerException eNull) {

		}
	}

	/**
	 * Request sync.
	 * 
	 * @param authority
	 *            the authority
	 * @param bundle
	 *            the extra data
	 */
	public void requestSync(String authority, Bundle bundle) {
		Account account = OpenERPAccountManager.getAccount(this, OEUser
				.current(context).getAndroidName());
		Bundle settingsBundle = new Bundle();
		settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
		if (bundle != null) {
			settingsBundle.putAll(bundle);
		}
		ContentResolver.requestSync(account, authority, settingsBundle);
	}

	/**
	 * Request sync.
	 * 
	 * @param authority
	 *            the authority
	 */
	public void requestSync(String authority) {
		requestSync(authority, null);
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
		Account account = OpenERPAccountManager.getAccount(this, OEUser
				.current(context).getAndroidName());
		Bundle extras = new Bundle();
		this.setAutoSync(authority, true);
		ContentResolver.setIsSyncable(account, authority, 1);
		final long sync_interval = interval_in_minute * seconds_per_minute
				* milliseconds_per_second;
		ContentResolver.addPeriodicSync(account, authority, extras,
				sync_interval);

	}

	/**
	 * Cancel sync.
	 * 
	 * @param authority
	 *            the authority
	 */
	public void cancelSync(String authority) {
		Account account = OpenERPAccountManager.getAccount(this, OEUser
				.current(context).getAndroidName());
		ContentResolver.cancelSync(account, authority);
	}

	/**
	 * Handle sync provider. Depend on user settings
	 */
	public void handleSyncProvider() {

		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean sync_enable = sharedPrefs.getBoolean("perform_sync", false);
		int sync_interval = Integer.parseInt(sharedPrefs.getString(
				"sync_interval", "1440"));

		List<String> default_authorities = new ArrayList<String>();
		default_authorities.add("com.android.calendar");
		default_authorities.add("com.android.contacts");
		SyncAdapterType[] list = ContentResolver.getSyncAdapterTypes();
		for (SyncAdapterType lst : list) {
			if (lst.authority.contains("com.openerp.providers")) {
				default_authorities.add(lst.authority);
			}
		}
		for (String authority : default_authorities) {
			if (sync_enable) {
				setSyncPeriodic(authority, sync_interval, 1, 1);
			} else {
				cancelSync(authority);
				setAutoSync(authority, false);
			}
		}
		Toast.makeText(this, "Setting saved.", Toast.LENGTH_LONG).show();
	}

	public void drawerCloseListener(String title) {
		Log.d("MenuDrawer", "Closed");
		getActionBar().setIcon(R.drawable.ic_launcher);
		if (!set_setting_menu) {
			setTitle(title, null);
		} else {
			getActionBar().setSubtitle(null);
		}
	}

	public void drawerOpenListener() {
		if (oeUser != null) {
			if (!oeUser.getAvatar().equals("false")) {
				Drawable profPic = new BitmapDrawable(
						Base64Helper.getBitmapImage(this, oeUser.getAvatar()));
				getActionBar().setIcon(profPic);
			}
			Res_PartnerDBHelper partner = new Res_PartnerDBHelper(context);
			Object obj = partner.search(partner, new String[] { "name" },
					new String[] { "id = ?" },
					new String[] { oeUser.getPartner_id() }).get("records");
			String user_name = "";
			if (obj instanceof Boolean) {
				user_name = oeUser.getUsername();
			} else {
				user_name = ((List<HashMap<String, Object>>) obj).get(0)
						.get("name").toString();
			}

			setTitle(user_name, oeUser.getHost());
		}
	}

	@Override
	public void onBackPressed() {
		if (backPressed != null) {
			if (backPressed.onBackPressed()) {
				super.onBackPressed();
			}
		} else {
			super.onBackPressed();
		}
	}

	public void setOnBackPressed(OnBackButtonPressedListener callback) {
		backPressed = callback;
	}

	private Dialog appCloseConfirmDialog() {

		// Initialize the Alert Dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Source of the data in the DIalog

		// Set the dialog title
		builder.setTitle("Confirm")
				.setMessage("Are you sure want to exit?")

				// Set the action buttons
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// User clicked OK, so save the result somewhere
								// or return them to the component that opened
								// the dialog
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
}
