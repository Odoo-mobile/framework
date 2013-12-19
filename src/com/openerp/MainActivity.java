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
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.openerp.addons.note.Note;
import com.openerp.auth.OpenERPAccountManager;
import com.openerp.base.about.AboutFragment;
import com.openerp.base.account.AccountFragment;
import com.openerp.base.account.AccountsDetail;
import com.openerp.base.account.UserProfile;
import com.openerp.base.res.Res_PartnerDBHelper;
import com.openerp.support.BaseFragment;
import com.openerp.support.Boot;
import com.openerp.support.FragmentHandler;
import com.openerp.support.OEUser;
import com.openerp.util.Base64Helper;
import com.openerp.util.OnBackButtonPressedListener;
import com.openerp.util.drawer.DrawerAdatper;
import com.openerp.util.drawer.DrawerItem;

/**
 * The Class MainActivity.
 */
public class MainActivity extends FragmentActivity implements
		DrawerItem.DrawerItemClickListener {

	public static final int RESULT_SETTINGS = 1;
	public static boolean set_setting_menu = false;
	public static Context context = null;

	DrawerLayout mDrawerLayout = null;
	ActionBarDrawerToggle mDrawerToggle = null;
	List<DrawerItem> mDrawerListItems = new ArrayList<DrawerItem>();
	DrawerAdatper mDrawerAdatper = null;
	String mAppTitle = "";
	String mDrawerTitle = "";
	String mDrawerSubtitle = "";
	int mDrawerItemSelectedPosition = -1;
	ListView mDrawerListView = null;

	public FragmentHandler fragmentHandler;

	public enum SETTING_KEYS {
		GLOBAL_SETTING, PROFILE, LOGOUT, ACCOUNTS, ADD_ACCOUNT, ABOUT_US
	}

	private CharSequence mTitle;
	private PullToRefreshAttacher mPullToRefreshAttacher;
	private OnBackButtonPressedListener backPressed = null;
	private Boot boot = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
		if (findViewById(R.id.fragment_container) != null) {
			initDrawerControls();
			fragmentHandler = new FragmentHandler(this);
			boot = new Boot(this);
			if (savedInstanceState != null) {
				mDrawerItemSelectedPosition = savedInstanceState
						.getInt("current_drawer_item");
				mPullToRefreshAttacher = new PullToRefreshAttacher(this);
				initDrawer(boot.getDrawerItems());
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
				lockDrawer(true);
				// Starting New account setup wizard.
				Fragment fragment = new AccountFragment();
				fragmentHandler.setBackStack(true, null);
				fragmentHandler.startNewFragmnet(fragment);

				return;
			} else {
				// Application contain user account, so going for next stuff.
				// Checking that rather user have requested to create new
				// account from application account setting.
				lockDrawer(false);
				Intent intent = getIntent();
				boolean reqForNewAccount = intent.getBooleanExtra(
						"create_new_account", false);

				// checking for logged in user and not request for new account
				// setup.
				if (OpenERPAccountManager.isAnyUser(this) && !reqForNewAccount) {
					// The attacher should always be created in the Activity's
					// onCreate
					initDrawer(boot.getDrawerItems());
					mPullToRefreshAttacher = new PullToRefreshAttacher(this);
					if (savedInstanceState != null) {
						return;
					}
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

	public void refreshDrawer(String tag_key, Context context) {
		int start_index = -1;
		List<DrawerItem> updated_menus = new ArrayList<DrawerItem>();
		for (int i = 0; i < mDrawerListItems.size(); i++) {
			DrawerItem item = mDrawerListItems.get(i);
			if (item.getKey().equals(tag_key) && !item.isGroupTitle()) {
				if (start_index < 0) {
					start_index = i - 1;
					BaseFragment instance = (BaseFragment) item
							.getFragmentInstace();
					updated_menus.addAll(instance.drawerMenus(context));
					break;
				}
			}
		}
		for (DrawerItem item : updated_menus) {
			mDrawerAdatper.updateDrawerItem(start_index, item);
			start_index++;
		}
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
										OEUser.current(context)
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

	public OEUser getUserContext() {
		return OEUser.current(context);
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
		if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
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
			Fragment about = new AboutFragment();
			fragmentHandler.setBackStack(true, null);
			fragmentHandler.replaceFragmnet(about);
			return true;
		case ADD_ACCOUNT:
			set_setting_menu = true;
			getActionBar().setDisplayHomeAsUpEnabled(false);
			getActionBar().setHomeButtonEnabled(false);
			Fragment fragment = new AccountFragment();
			fragmentHandler.setBackStack(true, null);
			fragmentHandler.replaceFragmnet(fragment);
			return true;
		case ACCOUNTS:
			set_setting_menu = true;
			Fragment acFragment = new AccountsDetail();
			fragmentHandler.setBackStack(true, null);
			fragmentHandler.replaceFragmnet(acFragment);
			return true;
		case PROFILE:
			set_setting_menu = true;
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
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		if (mDrawerToggle != null) {
			mDrawerToggle.syncState();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		if (mDrawerToggle != null) {
			mDrawerToggle.onConfigurationChanged(newConfig);
		}
	}

	// PullToRefresh
	public PullToRefreshAttacher getPullToRefreshAttacher() {
		return mPullToRefreshAttacher;
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Note.isStateExist = null;
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position,
			long id) {
		DrawerItem item = mDrawerListItems.get(position);
		if (!item.isGroupTitle()) {
			if (!item.getKey().equals("com.openerp.settings")) {
				mDrawerItemSelectedPosition = position;
			}
			mAppTitle = item.getTitle();
			loadFragment(item);
			mDrawerLayout.closeDrawers();
		}
		mDrawerListView.setItemChecked(mDrawerItemSelectedPosition, true);

	}

	private void initDrawerControls() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerListView = (ListView) findViewById(R.id.left_drawer);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open, R.string.app_name) {

			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				getActionBar().setIcon(R.drawable.ic_launcher);
				setTitle(mAppTitle, null);
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				setTitle(mDrawerTitle, mDrawerSubtitle);
				setUserPicIcon(context);
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	private void initDrawer(List<DrawerItem> drawerItems) {
		if (OEUser.current(context) != null) {
			Res_PartnerDBHelper partner = new Res_PartnerDBHelper(context);
			Object obj = partner.search(partner, new String[] { "name" },
					new String[] { "id = ?" },
					new String[] { OEUser.current(context).getPartner_id() })
					.get("records");
			String user_name = "";
			if (obj instanceof Boolean) {
				user_name = OEUser.current(context).getUsername();
			} else {
				user_name = ((List<HashMap<String, Object>>) obj).get(0)
						.get("name").toString();
			}
			mDrawerTitle = user_name;
			mDrawerSubtitle = OEUser.current(context).getHost();
			getActionBar().setHomeButtonEnabled(true);
			getActionBar().setDisplayHomeAsUpEnabled(true);
			setDrawerItems(drawerItems);
			if (mDrawerItemSelectedPosition > 0) {
				mAppTitle = mDrawerListItems.get(mDrawerItemSelectedPosition)
						.getTitle();
				setTitle(mAppTitle);
			}
		}
	}

	private void setDrawerItems(List<DrawerItem> drawerItems) {
		mDrawerListItems.addAll(drawerItems);
		mDrawerListItems.addAll(setSettingMenu());
		mDrawerAdatper = new DrawerAdatper(this, R.layout.drawer_item_layout,
				R.layout.drawer_item_group_layout, mDrawerListItems);
		mDrawerListView.setAdapter(mDrawerAdatper);
		mDrawerAdatper.notifyDataSetChanged();
		if (mDrawerItemSelectedPosition >= 0) {
			mDrawerListView.setItemChecked(mDrawerItemSelectedPosition, true);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		mDrawerListView.setOnItemClickListener(this);
		int position = -1;
		if (mDrawerListItems.size() > 0) {
			if (!mDrawerListItems.get(0).isGroupTitle()) {
				mDrawerListView.setItemChecked(0, true);
				position = 0;
			} else {
				mDrawerListView.setItemChecked(1, true);
				position = 1;
			}
		}
		if (mDrawerItemSelectedPosition >= 0) {
			position = mDrawerItemSelectedPosition;
		}

		if (getIntent().getAction() != null
				&& !getIntent().getAction().toString()
						.equalsIgnoreCase("android.intent.action.MAIN")) {
			if (getIntent().getAction().toString().equalsIgnoreCase("MESSAGE")) {
				int size = mDrawerListItems.size();
				for (int i = 0; i < size; i++) {
					if (mDrawerAdatper.getItem(i).getTitle()
							.equalsIgnoreCase("Messages")) {
						loadFragment(mDrawerAdatper.getItem(i + 1));
					}
				}
			}
			if (getIntent().getAction().toString().equalsIgnoreCase("NOTES")) {
				int size = mDrawerListItems.size();
				for (int i = 0; i < size; i++) {
					if (mDrawerAdatper.getItem(i).getTitle()
							.equalsIgnoreCase("Notes")) {
						loadFragment(mDrawerAdatper.getItem(i + 1));
						break;
					}
				}
			}
		} else {
			if (position > 0) {
				if (position != mDrawerItemSelectedPosition) {
					loadFragment(mDrawerListItems.get(position));
				}
			}
		}
	}

	private void loadFragment(DrawerItem item) {
		Object instance = item.getFragmentInstace();
		if (instance instanceof Intent) {
			startActivity((Intent) instance);
		} else {
			Fragment fragment = (Fragment) instance;
			if (fragment.getArguments() != null
					&& fragment.getArguments().containsKey("settings")) {
				onSettingItemSelected(SETTING_KEYS.valueOf(fragment
						.getArguments().get("settings").toString()));
			} else {
				if (item.getTagColor() != null
						&& !fragment.getArguments().containsKey("tag_color")) {
					Bundle tagcolor = fragment.getArguments();
					tagcolor.putInt("tag_color",
							Color.parseColor(item.getTagColor()));
					fragment.setArguments(tagcolor);
				}
			}
			if (fragment != null
					&& !fragment.getArguments().containsKey("settings")) {
				fragmentHandler.setBackStack(false, null);
				fragmentHandler.replaceFragmnet(fragment);
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void setUserPicIcon(Context context) {
		if (!OEUser.current(context).getAvatar().equals("false")) {
			Drawable profPic = new BitmapDrawable(Base64Helper.getBitmapImage(
					this, OEUser.current(context).getAvatar()));
			getActionBar().setIcon(profPic);
		}
	}

	private List<DrawerItem> setSettingMenu() {
		List<DrawerItem> sys = new ArrayList<DrawerItem>();
		String key = "com.openerp.settings";
		sys.add(new DrawerItem(key, "Settings", true));
		sys.add(new DrawerItem(key, "Profile", 0, R.drawable.ic_action_user,
				getFragBundle(new Fragment(), "settings", SETTING_KEYS.PROFILE)));

		sys.add(new DrawerItem(key, "Settings", 0,
				R.drawable.ic_action_settings, getFragBundle(new Fragment(),
						"settings", SETTING_KEYS.GLOBAL_SETTING)));

		sys.add(new DrawerItem(key, "Accounts", 0,
				R.drawable.ic_action_accounts, getFragBundle(new Fragment(),
						"settings", SETTING_KEYS.ACCOUNTS)));

		sys.add(new DrawerItem(key, "Add Account", 0,
				R.drawable.ic_action_add_account, getFragBundle(new Fragment(),
						"settings", SETTING_KEYS.ADD_ACCOUNT)));
		sys.add(new DrawerItem(
				key,
				"About Us",
				0,
				R.drawable.ic_action_about,
				getFragBundle(new Fragment(), "settings", SETTING_KEYS.ABOUT_US)));
		sys.add(new DrawerItem(key, "Logout", 0, R.drawable.ic_action_logout,
				getFragBundle(new Fragment(), "settings", SETTING_KEYS.LOGOUT)));

		return sys;
	}

	private Fragment getFragBundle(Fragment fragment, String key,
			SETTING_KEYS val) {
		Bundle bundle = new Bundle();
		bundle.putString(key, val.toString());
		fragment.setArguments(bundle);
		return fragment;
	}

	private void lockDrawer(boolean flag) {
		if (!flag) {
			mDrawerLayout.setDrawerLockMode(DrawerLayout.STATE_IDLE);
		} else {
			mDrawerLayout
					.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("current_drawer_item", mDrawerItemSelectedPosition);
		super.onSaveInstanceState(outState);
	}

}
