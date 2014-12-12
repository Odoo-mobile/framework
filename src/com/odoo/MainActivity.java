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
package com.odoo;

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
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.odoo.auth.OdooAccountManager;
import com.odoo.base.account.AccountsDetail;
import com.odoo.base.account.UserProfile;
import com.odoo.base.ir.IrModel;
import com.odoo.base.login_signup.AccountCreate;
import com.odoo.base.login_signup.LoginSignup;
import com.odoo.support.OUser;
import com.odoo.support.fragment.AsyncTaskListener;
import com.odoo.support.fragment.FragmentListener;
import com.odoo.util.PreferenceManager;
import com.odoo.util.drawer.DrawerItem;

/**
 * The Class MainActivity.
 */
public class MainActivity extends BaseActivity implements FragmentListener {

	private static final String TAG = "com.odoo.MainActivity";
	private static final int RESULT_SETTINGS = 1;
	private Context mContext = null;
	private boolean mNewFragment = false;
	private FragmentManager mFragment = null;
	private OTouchListener mTouchAttacher;
	private boolean mTwoPane;
	private OUser mAccount = null;
	Bundle mSavedInstanceState = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSavedInstanceState = savedInstanceState;
		setContentView(R.layout.activity_main);
		if (findViewById(R.id.fragment_detail_container) != null) {
			findViewById(R.id.fragment_detail_container).setVisibility(
					View.GONE);
			mTwoPane = true;
		}
		if (isTwoPane()) {
			setSubToolBar((Toolbar) findViewById(R.id.toolbar));
			setToolBar((Toolbar) findViewById(R.id.tablet_toolbar));
			setTitle("");
		} else {
			setToolBar((Toolbar) findViewById(R.id.toolbar));
		}
		if (getToolBar() == null)
			setActionBarIcon(R.drawable.ic_odoo_o);
		mContext = this;
		mFragment = getSupportFragmentManager();
		if (OUser.current(mContext) != null && savedInstanceState == null) {
			IrModel models = new IrModel(mContext);
			if (models.count() <= 0) {
				// Database cleaned so re-creating database
				updateAccount();
			} else {
				onTaskDone(savedInstanceState);
			}
		} else {
			onTaskDone(savedInstanceState);
		}
	}

	private void updateAccount() {
		OUser userdata = OUser.current(mContext);
		if (userdata != null) {
			AccountCreate account = new AccountCreate();
			Bundle args = new Bundle();
			args.putBoolean("no_config_wizard", true);
			args.putAll(userdata.getAsBundle());
			account.setArguments(args);
			startMainFragment(account, false);
		} else {
			List<OUser> accounts = OdooAccountManager.fetchAllAccounts(this);
			if (accounts.size() <= 0) {
				getActionbar().setDisplayHomeAsUpEnabled(false);
				getActionbar().setDisplayShowTitleEnabled(false);
				getActionbar().setHomeButtonEnabled(false);
				initDrawerControls();
				lockDrawer(true);
				LoginSignup loginSignUp = new LoginSignup();
				startMainFragment(loginSignUp, false);
			}
		}
	}

	public void onTaskDone(Bundle savedInstanceState) {
		initTouchListener();
		initDrawerControls();
		if (savedInstanceState != null) {
			return;
		}
		init();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (OUser.current(mContext) != null && !isNewAccountRequest()
				&& !intentRequests()) {
			populateNavDrawer(savedInstanceState);
			setupAccountBox();
			if (savedInstanceState != null) {
				setDrawerItemPosition(savedInstanceState
						.getInt("current_drawer_item"));
			} else {
				if (OdooAccountManager.isAnyUser(mContext)) {
					setDrawerItemPosition((getDrawerItemPosition() < 0) ? 0
							: getDrawerItemPosition());
					onNavDrawerItemClicked(
							getDrawerItem(getDrawerItemPosition()),
							savedInstanceState);
				}
			}
		}
	}

	private void init() {
		Log.d(TAG, "MainActivity->init()");
		/**
		 * checks for available account related to Odoo
		 */
		if (!OdooAccountManager.hasAccounts(this) || isNewAccountRequest()) {
			getActionbar().setDisplayHomeAsUpEnabled(false);
			getActionbar().setDisplayShowTitleEnabled(false);
			getActionbar().setHomeButtonEnabled(false);
			lockDrawer(true);
			LoginSignup loginSignUp = new LoginSignup();
			startMainFragment(loginSignUp, false);
		} else {
			lockDrawer(false);
			/**
			 * User found but not logged in. Requesting for login with available
			 * accounts.
			 */
			if (!OdooAccountManager.isAnyUser(mContext)) {
				accountSelectionDialog(
						OdooAccountManager.fetchAllAccounts(mContext)).show();
			}
		}
	}

	private void initTouchListener() {
		mTouchAttacher = new OTouchListener(this);
	}

	private String[] accountList(List<OUser> accounts) {
		String[] account_list = new String[accounts.size()];
		int i = 0;
		for (OUser user : accounts) {
			account_list[i] = user.getAndroidName();
			i++;
		}
		return account_list;
	}

	private Dialog accountSelectionDialog(final List<OUser> accounts) {

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
						getActionbar().setDisplayHomeAsUpEnabled(false);
						getActionbar().setDisplayShowTitleEnabled(false);
						getActionbar().setHomeButtonEnabled(false);
						LoginSignup loginSignUp = new LoginSignup();
						startMainFragment(loginSignUp, false);
					}
				})
				// Set the action buttons
				.setPositiveButton(R.string.label_login,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								if (mAccount != null) {
									OdooAccountManager.loginUser(mContext,
											mAccount.getAndroidName());
									onPostCreate(null);
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

	public boolean onSettingItemSelected(SettingKeys key) {
		switch (key) {
		case GLOBAL_SETTING:
			Intent i = new Intent(this, SettingActivity.class);
			startActivityForResult(i, RESULT_SETTINGS);
			return true;
		case ACCOUNTS:
			AccountsDetail acFragment = new AccountsDetail();
			startMainFragment(acFragment, true);
			return true;
		case PROFILE:
			// UserProfile profileFragment = new UserProfile();
			// startMainFragment(profileFragment, true);
			Intent intent = new Intent(this, UserProfile.class);
			startActivity(intent);
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

		Account mAccount = OdooAccountManager.getAccount(mContext, OUser
				.current(mContext).getAndroidName());

		for (SyncAdapterType lst : list) {
			if (lst.authority.contains("com.odoo")
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

	public OTouchListener getTouchAttacher() {
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
			Account account = OdooAccountManager.getAccount(this, OUser
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
		Account account = OdooAccountManager.getAccount(
				getApplicationContext(), OUser.current(getApplicationContext())
						.getAndroidName());
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
		Account account = OdooAccountManager.getAccount(this,
				OUser.current(mContext).getAndroidName());
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
		Account account = OdooAccountManager.getAccount(this,
				OUser.current(mContext).getAndroidName());
		ContentResolver.cancelSync(account, authority);
	}

	@Override
	public void loadFragment(DrawerItem item) {
		Object frag = item.getFragmentInstace();
		if (frag instanceof Fragment) {
			Fragment fragment = (Fragment) frag;
			if (item.getTagColor() != null
					&& !fragment.getArguments().containsKey("tag_color")) {
				Bundle tagcolor = fragment.getArguments();
				tagcolor.putInt("tag_color",
						Color.parseColor(item.getTagColor()));
				fragment.setArguments(tagcolor);
			}
			frag = fragment;
		}
		loadFragment(frag);
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

	public DrawerItem getNavItem() {
		if (getCurrentPosition() != -1)
			return getDrawerItem(getCurrentPosition());
		return null;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("current_drawer_item", getDrawerItemPosition());
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
		if (mFragment.findFragmentByTag("main_fragment") != null
				&& mSavedInstanceState != null) {
			mFragment.popBackStack("main_fragment",
					FragmentManager.POP_BACK_STACK_INCLUSIVE);
			mFragment.popBackStack(null,
					FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}
		FragmentTransaction tran = mFragment.beginTransaction().replace(
				container_id, fragment, "main_fragment");
		tran.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
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
		tran.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
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

	@Override
	protected boolean intentRequests() {
		/**
		 * TODO: handle intent request from outside
		 */
		if (getIntent().getAction() != null
				&& !getIntent().getAction().toString()
						.equalsIgnoreCase("android.intent.action.MAIN")) {
			lockDrawer(false);
			/**
			 * TODO: handle widget fragment requests.
			 */

		}
		return false;
	}

	/**
	 * AsyncTask quick task helper
	 */

	public class BackgroundTask extends AsyncTask<Void, Void, Object> {
		AsyncTaskListener mListener = null;

		public BackgroundTask(AsyncTaskListener listener) {
			mListener = listener;
		}

		@Override
		protected Object doInBackground(Void... params) {
			if (mListener != null) {
				return mListener.onPerformTask();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if (mListener != null) {
				mListener.onFinish(result);
				mListener = null;
			}
		}
	}

	public BackgroundTask newBackgroundTask(AsyncTaskListener taskListener) {
		return new BackgroundTask(taskListener);
	}

}
