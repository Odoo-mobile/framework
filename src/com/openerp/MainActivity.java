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
import java.util.List;

import android.accounts.Account;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.openerp.auth.OpenERPAccountManager;
import com.openerp.base.about.AboutFragment;
import com.openerp.base.account.AccountFragment;
import com.openerp.base.account.AccountsDetail;
import com.openerp.base.account.UserProfile;
import com.openerp.base.res.ResPartnerDB;
import com.openerp.orm.OEDataRow;
import com.openerp.support.BaseFragment;
import com.openerp.support.OEUser;
import com.openerp.support.fragment.FragmentListener;
import com.openerp.util.Base64Helper;
import com.openerp.util.OEAppRater;
import com.openerp.util.OnBackButtonPressedListener;
import com.openerp.util.PreferenceManager;
import com.openerp.util.drawer.DrawerAdatper;
import com.openerp.util.drawer.DrawerHelper;
import com.openerp.util.drawer.DrawerItem;
import com.openerp.util.drawer.DrawerListener;

/**
 * The Class MainActivity.
 */
public class MainActivity extends FragmentActivity implements
		DrawerItem.DrawerItemClickListener, FragmentListener, DrawerListener {

	public static final String TAG = "com.openerp.MainActivity";
	public static final int RESULT_SETTINGS = 1;
	public static boolean set_setting_menu = false;
	public Context mContext = null;

	DrawerLayout mDrawerLayout = null;
	ActionBarDrawerToggle mDrawerToggle = null;
	List<DrawerItem> mDrawerListItems = new ArrayList<DrawerItem>();
	DrawerAdatper mDrawerAdatper = null;
	String mAppTitle = "";
	String mDrawerTitle = "";
	String mDrawerSubtitle = "";
	int mDrawerItemSelectedPosition = -1;
	ListView mDrawerListView = null;
	boolean mNewFragment = false;

	FragmentManager mFragment = null;

	public enum SettingKeys {
		GLOBAL_SETTING, PROFILE, ACCOUNTS, ABOUT_US
	}

	private CharSequence mTitle;
	private OETouchListener mTouchAttacher;
	private OnBackButtonPressedListener backPressed = null;
	private boolean mTwoPane;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getActionBar().setIcon(R.drawable.ic_launcher);
		if (savedInstanceState != null) {
			mDrawerItemSelectedPosition = savedInstanceState
					.getInt("current_drawer_item");
		}
		mContext = this;
		mFragment = getSupportFragmentManager();
		if (findViewById(R.id.fragment_detail_container) != null) {
			findViewById(R.id.fragment_detail_container).setVisibility(
					View.GONE);
			mTwoPane = true;
		}
		init();
	}

	private void checkForRateApplication() {
		OEAppRater.app_launched(this);
	}

	private void init() {
		Log.d(TAG, "MainActivity->init()");
		initDrawerControls();
		boolean reqForNewAccount = getIntent().getBooleanExtra(
				"create_new_account", false);
		/**
		 * checks for available account related to OpenERP
		 */
		if (!OpenERPAccountManager.hasAccounts(this) || reqForNewAccount) {
			getActionBar().setDisplayHomeAsUpEnabled(false);
			getActionBar().setHomeButtonEnabled(false);
			lockDrawer(true);
			AccountFragment account = new AccountFragment();
			startMainFragment(account, false);
		} else {
			lockDrawer(false);
			/**
			 * User found but not logged in. Requesting for login with available
			 * accounts.
			 */
			if (!OpenERPAccountManager.isAnyUser(mContext)) {
				accountSelectionDialog(
						OpenERPAccountManager.fetchAllAccounts(mContext))
						.show();
			} else {
				OETouchListener.DEFAULT_HEADER_LAYOUT = R.layout.default_header;
				OETouchListener.DEFAULT_ANIM_HEADER_IN = R.anim.fade_in;
				OETouchListener.DEFAULT_ANIM_HEADER_OUT = R.anim.fade_out;
				OETouchListener.ptr_progress = R.id.ptr_progress;
				OETouchListener.ptr_text = R.id.ptr_text;
				OETouchListener.refresh_pull_label = R.string.pull_to_refresh_pull_label;
				OETouchListener.refreshing_label = R.string.pull_to_refresh_refreshing_label;
				OETouchListener.release_label = R.string.pull_to_refresh_release_label;
				OETouchListener.contentView = R.id.ptr_content;
				OETouchListener.opaqueBackground = R.id.ptr_text_opaque_bg;

				mTouchAttacher = new OETouchListener(this);
				initDrawer();
			}
		}
		checkForRateApplication();
	}

	private void initDrawerControls() {
		Log.d(TAG, "MainActivity->initDrawerControls()");
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerListView = (ListView) findViewById(R.id.left_drawer);
		if (OEUser.current(mContext) != null)
			setDrawerHeader();
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_navigation_drawer, R.string.drawer_open,
				R.string.app_name) {

			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				getActionBar().setIcon(R.drawable.ic_launcher);
				setTitle(mAppTitle, null);
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				setTitle(R.string.app_name);
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	private void setDrawerHeader() {
		mDrawerTitle = OEUser.current(mContext).getUsername();
		mDrawerSubtitle = OEUser.current(mContext).getHost();

		ResPartnerDB partner = new ResPartnerDB(mContext);
		OEDataRow partnerInfo = partner.select(OEUser.current(mContext)
				.getPartner_id());
		if (partnerInfo != null) {
			mDrawerTitle = partnerInfo.getString("name");
		}

		View v = getLayoutInflater().inflate(R.layout.drawer_header,
				mDrawerListView, false);
		TextView mUserName, mUserURL;
		ImageView imgUserPic = (ImageView) v
				.findViewById(R.id.imgUserProfilePic);
		mUserName = (TextView) v.findViewById(R.id.txvUserProfileName);
		mUserURL = (TextView) v.findViewById(R.id.txvUserServerURL);
		mUserName.setText(mDrawerTitle);
		mUserURL.setText(mDrawerSubtitle);
		if (OEUser.current(mContext) != null
				&& !OEUser.current(mContext).getAvatar().equals("false")) {
			Bitmap profPic = Base64Helper.getBitmapImage(this,
					OEUser.current(mContext).getAvatar());
			if (profPic != null)
				imgUserPic.setImageBitmap(profPic);
		}
		v.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onSettingItemSelected(SettingKeys.PROFILE);
				mDrawerLayout.closeDrawers();
			}
		});
		mDrawerListView.addHeaderView(v, null, false);
	}

	private void setDrawerItems() {
		Log.d(TAG, "MainActivity->setDrawerItems()");
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		mDrawerListItems.addAll(DrawerHelper.drawerItems(mContext));
		mDrawerListItems.addAll(setSettingMenu());
		mDrawerAdatper = new DrawerAdatper(this, R.layout.drawer_item_layout,
				R.layout.drawer_item_group_layout, mDrawerListItems);
		mDrawerListView.setAdapter(mDrawerAdatper);
		mDrawerAdatper.notifyDataSetChanged();
		if (mDrawerItemSelectedPosition >= 0) {
			mDrawerListView.setItemChecked(mDrawerItemSelectedPosition, true);
		}
	}

	private void initDrawer() {
		setDrawerItems();
		Log.d(TAG, "MainActivity->initDrawer()");
		mDrawerListView.setOnItemClickListener(this);
		int position = 1;
		if (mDrawerListItems.size() > 0) {
			if (!mDrawerListItems.get(1).isGroupTitle()) {
				mDrawerListView.setItemChecked(1 + 1, true);
				position = 1;
			} else {
				mDrawerListView.setItemChecked(2 + 1, true);
				position = 2;
			}
		}
		if (mDrawerItemSelectedPosition >= 0) {
			position = mDrawerItemSelectedPosition;
		}
		mAppTitle = mDrawerListItems.get(position).getTitle();
		setTitle(mAppTitle);

		/**
		 * TODO: handle intent request from outside
		 */
		if (getIntent().getAction() != null
				&& !getIntent().getAction().toString()
						.equalsIgnoreCase("android.intent.action.MAIN")) {

			/**
			 * TODO: handle widget fragment requests.
			 */

		} else {
			if (position > 0) {
				if (position != mDrawerItemSelectedPosition) {
					loadFragment(mDrawerListItems.get(position));
				}
			}
		}
	}

	private String[] accountList(List<OEUser> accounts) {
		String[] account_list = new String[accounts.size()];
		int i = 0;
		for (OEUser user : accounts) {
			account_list[i] = user.getAndroidName();
			i++;
		}
		return account_list;
	}

	OEUser mAccount = null;

	public Dialog accountSelectionDialog(final List<OEUser> accounts) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(R.string.title_select_account)
				.setSingleChoiceItems(accountList(accounts), 1,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mAccount = accounts.get(which);
							}
						})
				.setNeutralButton(R.string.label_new, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						getActionBar().setDisplayHomeAsUpEnabled(false);
						getActionBar().setHomeButtonEnabled(false);
						AccountFragment fragment = new AccountFragment();
						startMainFragment(fragment, false);
					}
				})
				// Set the action buttons
				.setPositiveButton(R.string.label_login,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								if (mAccount != null) {
									OpenERPAccountManager.loginUser(mContext,
											mAccount.getAndroidName());
								} else {
									Toast.makeText(mContext,
											"Please select account",
											Toast.LENGTH_LONG).show();
								}
								init();
							}
						})
				.setNegativeButton(R.string.label_cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								finish();
							}
						});

		return builder.create();
	}

	@Override
	public void refreshDrawer(String tag_key) {
		Log.d(TAG, "MainActivity->DrawerListener->refreshDrawer()");
		int start_index = -1;
		List<DrawerItem> updated_menus = new ArrayList<DrawerItem>();
		for (int i = 0; i < mDrawerListItems.size(); i++) {
			DrawerItem item = mDrawerListItems.get(i);
			if (item.getKey().equals(tag_key) && !item.isGroupTitle()) {
				if (start_index < 0) {
					start_index = i - 1;
					BaseFragment instance = (BaseFragment) item
							.getFragmentInstace();
					updated_menus.addAll(instance.drawerMenus(mContext));
					break;
				}
			}
		}
		for (DrawerItem item : updated_menus) {
			mDrawerAdatper.updateDrawerItem(start_index, item);
			start_index++;
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
		if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public boolean onSettingItemSelected(SettingKeys key) {
		switch (key) {
		case GLOBAL_SETTING:
			set_setting_menu = false;
			Intent i = new Intent(this, AppSettingsActivity.class);
			startActivityForResult(i, RESULT_SETTINGS);
			return true;
		case ABOUT_US:
			set_setting_menu = true;
			getActionBar().setDisplayHomeAsUpEnabled(false);
			getActionBar().setHomeButtonEnabled(false);
			AboutFragment about = new AboutFragment();
			startMainFragment(about, true);
			return true;
		case ACCOUNTS:
			set_setting_menu = true;
			AccountsDetail acFragment = new AccountsDetail();
			startMainFragment(acFragment, true);
			return true;
		case PROFILE:
			set_setting_menu = true;
			UserProfile profileFragment = new UserProfile();
			startMainFragment(profileFragment, true);
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
			updateSyncSettings();
			break;
		}

	}

	private void updateSyncSettings() {
		Log.d(TAG, "MainActivity->updateSyncSettings()");

		PreferenceManager mPref = new PreferenceManager(mContext);
		int sync_interval = mPref.getInt("sync_interval", 1440);

		List<String> default_authorities = new ArrayList<String>();
		default_authorities.add("com.android.calendar");
		default_authorities.add("com.android.contacts");

		SyncAdapterType[] list = ContentResolver.getSyncAdapterTypes();

		Account mAccount = OpenERPAccountManager.getAccount(mContext, OEUser
				.current(mContext).getAndroidName());

		for (SyncAdapterType lst : list) {
			if (lst.authority.contains("com.openerp")
					&& lst.authority.contains("providers")) {
				default_authorities.add(lst.authority);
			}
		}
		for (String authority : default_authorities) {
			boolean isSyncActive = ContentResolver.getSyncAutomatically(
					mAccount, authority);
			if (isSyncActive) {
				setSyncPeriodic(authority, sync_interval, 60, 1);
			}
		}
		Toast.makeText(this, R.string.toast_setting_saved, Toast.LENGTH_LONG)
				.show();
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
	public OETouchListener getTouchAttacher() {
		return mTouchAttacher;
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
					.current(mContext).getAndroidName());
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
		Account account = OpenERPAccountManager.getAccount(
				getApplicationContext(), OEUser
						.current(getApplicationContext()).getAndroidName());
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
				.current(mContext).getAndroidName());
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
				.current(mContext).getAndroidName());
		ContentResolver.cancelSync(account, authority);
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
	public void onItemClick(AdapterView<?> adapter, View view, int position,
			long id) {
		int item_position = position - 1;
		DrawerItem item = mDrawerListItems.get(item_position);
		if (!item.isGroupTitle()) {
			if (!item.getKey().equals("com.openerp.settings")) {
				mDrawerItemSelectedPosition = item_position + 1;
			}
			mAppTitle = item.getTitle();
			loadFragment(item);
			mDrawerLayout.closeDrawers();
		}
		mDrawerListView.setItemChecked(mDrawerItemSelectedPosition, true);

	}

	private void loadFragment(DrawerItem item) {

		Fragment fragment = (Fragment) item.getFragmentInstace();
		;
		if (item.getTagColor() != null
				&& !fragment.getArguments().containsKey("tag_color")) {
			Bundle tagcolor = fragment.getArguments();
			tagcolor.putInt("tag_color", Color.parseColor(item.getTagColor()));
			fragment.setArguments(tagcolor);
		}
		loadFragment(fragment);
	}

	private void loadFragment(Object instance) {
		if (instance instanceof Intent) {
			startActivity((Intent) instance);
		} else {
			Fragment fragment = (Fragment) instance;
			if (fragment.getArguments() != null
					&& fragment.getArguments().containsKey("settings")) {
				onSettingItemSelected(SettingKeys.valueOf(fragment
						.getArguments().get("settings").toString()));
			}
			if (fragment != null
					&& !fragment.getArguments().containsKey("settings")) {
				startMainFragment(fragment, false);
			}
		}
	}

	private List<DrawerItem> setSettingMenu() {
		List<DrawerItem> sys = new ArrayList<DrawerItem>();
		String key = "com.openerp.settings";
		Resources r = getResources();
		sys.add(new DrawerItem(key, r.getString(R.string.title_settings), true));
		sys.add(new DrawerItem(key, r.getString(R.string.title_profile), 0,
				R.drawable.ic_action_user, getFragBundle(new Fragment(),
						"settings", SettingKeys.PROFILE)));

		sys.add(new DrawerItem(key, r
				.getString(R.string.title_general_settings), 0,
				R.drawable.ic_action_settings, getFragBundle(new Fragment(),
						"settings", SettingKeys.GLOBAL_SETTING)));

		sys.add(new DrawerItem(key, r.getString(R.string.title_accounts), 0,
				R.drawable.ic_action_accounts, getFragBundle(new Fragment(),
						"settings", SettingKeys.ACCOUNTS)));
		sys.add(new DrawerItem(key, r.getString(R.string.title_about_us), 0,
				R.drawable.ic_action_about, getFragBundle(new Fragment(),
						"settings", SettingKeys.ABOUT_US)));
		return sys;
	}

	private Fragment getFragBundle(Fragment fragment, String key,
			SettingKeys val) {
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

	@Override
	public void startMainFragment(Fragment fragment, boolean addToBackState) {
		Log.d(TAG, "MainActivity->FragmentListener->startMainFragment()");
		int container_id = R.id.fragment_container;

		if (isTwoPane()) {
			findViewById(R.id.fragment_detail_container).setVisibility(
					View.GONE);
			Fragment detail = mFragment.findFragmentByTag("detail_fragment");
			if (detail != null && !mNewFragment && !detail.isInLayout()) {
				startDetailFragment(recreateFragment(detail));
			}

		}
		FragmentTransaction tran = mFragment.beginTransaction().replace(
				container_id, fragment, "main_fragment");
		if (addToBackState) {
			tran.addToBackStack(null);
		}
		tran.commitAllowingStateLoss();
	}

	@Override
	public void startDetailFragment(Fragment fragment) {
		Log.d(TAG, "MainActivity->FragmentListener->startDetailFragment()");
		int container_id = (isTwoPane()) ? R.id.fragment_detail_container
				: R.id.fragment_container;
		FragmentTransaction tran = mFragment.beginTransaction().replace(
				container_id, fragment, "detail_fragment");
		if (!isTwoPane()) {
			tran.addToBackStack(null);
			tran.commit();
		} else {
			findViewById(R.id.fragment_detail_container).setVisibility(
					View.VISIBLE);
			tran.commitAllowingStateLoss();
		}
	}

	private Fragment recreateFragment(Fragment fragment) {
		Log.d(TAG, "recreateFragment()");
		Fragment newInstance = null;
		try {
			Fragment.SavedState savedState = mFragment
					.saveFragmentInstanceState(fragment);

			newInstance = fragment.getClass().newInstance();
			newInstance.setInitialSavedState(savedState);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newInstance;
	}

	@Override
	public void restart() {
		Log.d(TAG, "MainActivity->FragmentListener->restart()");
		getIntent().putExtra("create_new_account", false);
		init();
	}

	@Override
	public boolean isTwoPane() {
		return mTwoPane;
	}
}
