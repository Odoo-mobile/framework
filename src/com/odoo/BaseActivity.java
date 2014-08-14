package com.odoo;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.odoo.BaseActivity.SettingKeys;
import com.odoo.auth.OdooAccountManager;
import com.odoo.support.OUser;
import com.odoo.support.fragment.BaseFragment;
import com.odoo.util.Base64Helper;
import com.odoo.util.OAppRater;
import com.odoo.util.OControls;
import com.odoo.util.drawer.DrawerHelper;
import com.odoo.util.drawer.DrawerItem;
import com.odoo.util.drawer.DrawerListener;

public abstract class BaseActivity extends FragmentActivity implements
		FragmentLoader, DrawerListener {
	public static final String TAG = BaseActivity.class.getSimpleName();
	private static final int NAVDRAWER_LAUNCH_DELAY = 250;
	private static final int ACCOUNT_BOX_EXPAND_ANIM_DURATION = 200;
	private DrawerLayout mDrawerLayout = null;
	private ActionBarDrawerToggle mDrawerToggle = null;
	private List<DrawerItem> mNavDrawerItems = new ArrayList<DrawerItem>();
	private int mDrawerItemSelectedPosition = -1;
	private View[] mNavDrawerItemViews;
	private String mTitle = "";
	private ImageView mExpandAccountBoxIndicator;
	private Context mContext = null;
	private Handler mHandler;
	private String mAppTitle = "";
	private Boolean mAccountBoxExpanded = false;
	private Boolean mRequestForNewAccount = false;
	private LinearLayout mAccountListContainer;
	private ViewGroup mDrawerItemsListContainer;

	protected enum SettingKeys {
		GLOBAL_SETTING, PROFILE, ACCOUNTS, ABOUT_US
	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		mContext = this;
		mHandler = new Handler();
		OAppRater.app_launched(this);
		mRequestForNewAccount = getIntent().getBooleanExtra(
				"create_new_account", false);
	}

	protected Boolean isNewAccountRequest() {
		return mRequestForNewAccount;
	}

	protected int getNavDrawerLaunchDelay() {
		return NAVDRAWER_LAUNCH_DELAY;
	}

	protected int getDrawerItemPosition() {
		return mDrawerItemSelectedPosition;
	}

	protected void setDrawerItemPosition(int position) {
		mDrawerItemSelectedPosition = position;
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = (String) title;
		getActionBar().setTitle(mTitle);
	}

	public void setTitle(CharSequence title, CharSequence subtitle) {
		mTitle = (String) title;
		this.setTitle(mTitle);
		getActionBar().setSubtitle(subtitle);
	}

	protected void initDrawerControls() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_navigation_drawer, R.string.drawer_open,
				R.string.app_name) {

			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				getActionBar().setIcon(R.drawable.ic_odoo_o);
				setTitle(mAppTitle, null);
				invalidateOptionsMenu();
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				invalidateOptionsMenu();
				setTitle(R.string.app_name);
			}

			@Override
			public void onDrawerStateChanged(int newState) {
				super.onDrawerStateChanged(newState);
				invalidateOptionsMenu();
			}

			@Override
			public void onDrawerSlide(View drawerView, float slideOffset) {
				super.onDrawerSlide(drawerView, slideOffset);
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
		mDrawerToggle.syncState();
	}

	protected void populateNavDrawer() {
		Log.d(TAG, "initDrawer()");
		if (mDrawerLayout == null)
			initDrawerControls();
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		mNavDrawerItems.clear();
		mNavDrawerItems.addAll(DrawerHelper.drawerItems(mContext));
		mNavDrawerItems.addAll(setSettingMenu());
		createNavDrawerItems();
	}

	private List<DrawerItem> setSettingMenu() {
		List<DrawerItem> sys = new ArrayList<DrawerItem>();
		String key = "com.odoo.settings";
		Resources r = getResources();
		sys.add(new DrawerItem(key, r.getString(R.string.title_settings), true));
		sys.add(new DrawerItem(key, r.getString(R.string.title_profile), 0,
				R.drawable.ic_action_user, getFragBundle(new Fragment(),
						"settings", SettingKeys.PROFILE)));

		sys.add(new DrawerItem(key, r.getString(R.string.title_settings), 0,
				R.drawable.ic_action_settings, getFragBundle(new Fragment(),
						"settings", SettingKeys.GLOBAL_SETTING)));
		return sys;
	}

	private Fragment getFragBundle(Fragment fragment, String key,
			SettingKeys val) {
		Bundle bundle = new Bundle();
		bundle.putString(key, val.toString());
		fragment.setArguments(bundle);
		return fragment;
	}

	public void lockDrawer(boolean flag) {
		if (!flag) {
			mDrawerLayout.setDrawerLockMode(DrawerLayout.STATE_IDLE);
		} else {
			mDrawerLayout
					.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		}
	}

	protected DrawerItem getDrawerItem(int position) {
		return mNavDrawerItems.get(position);
	}

	protected int getCurrentPosition() {
		if (mNavDrawerItems.size() > 0)
			return (mDrawerItemSelectedPosition < 0) ? 0
					: mDrawerItemSelectedPosition;
		return -1;
	}

	abstract protected void intentRequests();

	private void createNavDrawerItems() {
		mDrawerItemsListContainer = (ViewGroup) findViewById(R.id.navdrawer_items_list);
		if (mDrawerItemsListContainer == null) {
			return;
		}

		mNavDrawerItemViews = new View[mNavDrawerItems.size()];
		mDrawerItemsListContainer.removeAllViews();
		int i = 0;
		for (DrawerItem item : mNavDrawerItems) {
			mNavDrawerItemViews[i] = makeNavDrawerItem(item,
					mDrawerItemsListContainer);
			mDrawerItemsListContainer.addView(mNavDrawerItemViews[i]);
			++i;
		}
		if (mDrawerItemSelectedPosition == -1) {
			onNavDrawerItemClicked(mNavDrawerItems.get(0));
		}
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	private View makeNavDrawerItem(final DrawerItem item, ViewGroup container) {
		boolean selected = mDrawerItemSelectedPosition == item.getId();

		int layoutToInflate = 0;
		if (item.isGroupTitle()) {
			layoutToInflate = R.layout.base_navdrawer_separator;
		} else {
			layoutToInflate = R.layout.base_navdrawer_item;
		}
		View view = getLayoutInflater().inflate(layoutToInflate, container,
				false);
		if (item.isGroupTitle()) {
			return view;
		}
		view.setTag(item.getKey());

		ImageView iconView = (ImageView) view.findViewById(R.id.icon);
		TextView titleView = (TextView) view.findViewById(R.id.title);
		TextView counterView = (TextView) view.findViewById(R.id.counter);
		int iconId = (item.getIcon() > 0) ? item.getIcon()
				: R.drawable.ic_action_arrow_next;
		String title = item.getTitle();
		Integer counter = item.getCounter();
		// set icon and text
		iconView.setVisibility(iconId > 0 ? View.VISIBLE : View.GONE);
		if (iconId > 0) {
			iconView.setImageResource(iconId);
		}
		titleView.setText(title);
		if (counter > 0) {
			String ctr = (counter > 99) ? "99+" : counter + "";
			counterView.setText(ctr);
		} else {
			counterView.setVisibility(View.GONE);
		}
		formatNavDrawerItem(view, item, selected);

		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onNavDrawerItemClicked(item);
			}
		});
		return view;
	}

	@Override
	public void refreshDrawer(String tag_key) {
		Log.d(TAG, "refreshDrawer()");
		int startIndex = -1;
		List<DrawerItem> newItems = new ArrayList<DrawerItem>();
		for (View v : mNavDrawerItemViews) {
			if (v.getTag() != null && v.getTag().toString().equals(tag_key)) {
				int index = mDrawerItemsListContainer.indexOfChild(v);
				if (startIndex == -1)
					startIndex = index;
				DrawerItem item = mNavDrawerItems.get(index);
				BaseFragment instance = (BaseFragment) item
						.getFragmentInstace();
				newItems = instance.drawerMenus(mContext);
				break;
			}
		}
		int itemIndex = 0;
		for (int i = startIndex; i < newItems.size(); i++) {
			ViewGroup v = (ViewGroup) mDrawerItemsListContainer.getChildAt(i);
			DrawerItem item = newItems.get(itemIndex);
			String counter = (item.getCounter() > 99) ? "99+" : (item
					.getCounter() == 0) ? "" : item.getCounter() + "";
			OControls.setText(v, R.id.counter, counter);
			itemIndex++;
		}

	}

	protected void onNavDrawerItemClicked(final DrawerItem item) {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				goToNavDrawerItem(item);
			}
		}, getNavDrawerLaunchDelay());

		if (!isSettingDrawerItem(item))
			setSelectedNavDrawerItem(item);
		mDrawerLayout.closeDrawer(Gravity.START);
	}

	private boolean isSettingDrawerItem(DrawerItem item) {
		Fragment fragment = (Fragment) item.getFragmentInstace();
		if (fragment.getArguments() != null
				&& fragment.getArguments().containsKey("settings"))
			return true;
		return false;
	}

	private void goToNavDrawerItem(DrawerItem item) {
		if (!item.isGroupTitle()) {
			if (!item.getKey().equals("com.odoo.settings")) {
				mDrawerItemSelectedPosition = mNavDrawerItems.indexOf(item);
			}
			mAppTitle = item.getTitle();
			setTitle(mAppTitle);
			loadFragment(item);
			mDrawerLayout.closeDrawers();
		}
	}

	private void setSelectedNavDrawerItem(DrawerItem item) {
		if (mNavDrawerItemViews != null) {
			for (int i = 0; i < mNavDrawerItemViews.length; i++) {
				if (i < mNavDrawerItems.size()) {
					DrawerItem thisItem = mNavDrawerItems.get(i);
					formatNavDrawerItem(mNavDrawerItemViews[i], thisItem,
							item.getId() == thisItem.getId());
				}
			}
		}
	}

	private void formatNavDrawerItem(View view, DrawerItem item,
			boolean selected) {
		if (item.isGroupTitle()) {
			// not applicable
			return;
		}

		ImageView iconView = (ImageView) view.findViewById(R.id.icon);
		TextView titleView = (TextView) view.findViewById(R.id.title);
		TextView counterView = (TextView) view.findViewById(R.id.counter);
		// configure its appearance according to whether or not it's selected
		titleView.setTextColor(selected ? getResources().getColor(
				R.color.navdrawer_text_color_selected) : getResources()
				.getColor(R.color.navdrawer_text_color));
		counterView.setTextColor(selected ? getResources().getColor(
				R.color.navdrawer_text_color_selected) : getResources()
				.getColor(R.color.navdrawer_text_color));
		iconView.setColorFilter(selected ? getResources().getColor(
				R.color.navdrawer_icon_tint_selected) : getResources()
				.getColor(R.color.navdrawer_icon_tint));
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (OUser.current(mContext) != null && !isNewAccountRequest()) {
			populateNavDrawer();
			setupAccountBox();
			// Sync the toggle state after onRestoreInstanceState has occurred.
			if (mDrawerToggle != null) {
				mDrawerToggle.syncState();
			}
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

	protected void setupAccountBox() {
		mAccountListContainer = (LinearLayout) findViewById(R.id.account_list);
		if (mAccountListContainer == null) {
			// This activity does not have an account box
			return;
		}
		final View chosenAccountView = findViewById(R.id.chosen_account_view);
		OUser chosenAccount = OUser.current(this);
		if (chosenAccount == null) {
			// No account logged in; hide account box
			chosenAccountView.setVisibility(View.GONE);
			mAccountListContainer.setVisibility(View.GONE);
			return;
		} else {
			chosenAccountView.setVisibility(View.VISIBLE);
			mAccountListContainer.setVisibility(View.INVISIBLE);
		}
		ImageView profileImageView = (ImageView) chosenAccountView
				.findViewById(R.id.profile_image);
		TextView nameTextView = (TextView) chosenAccountView
				.findViewById(R.id.profile_name_text);
		TextView user_url = (TextView) chosenAccountView
				.findViewById(R.id.profile_email_text);
		mExpandAccountBoxIndicator = (ImageView) findViewById(R.id.expand_account_box_indicator);
		String name = chosenAccount.getName();
		if (name == null) {
			nameTextView.setVisibility(View.GONE);
		} else {
			nameTextView.setText(name);
		}
		String url = (chosenAccount.isOAauthLogin()) ? chosenAccount
				.getInstanceUrl() : chosenAccount.getHost();
		user_url.setText(url);

		if (!chosenAccount.getAvatar().equals("false")) {
			Bitmap profPic = Base64Helper.getBitmapImage(this,
					OUser.current(mContext).getAvatar());
			if (profPic != null) {
				profileImageView.setImageBitmap(profPic);
			}

		}

		List<OUser> accounts = OdooAccountManager.fetchAllAccounts(mContext);
		if (accounts != null && accounts.size() > 0) {
			chosenAccountView.setEnabled(true);

			mExpandAccountBoxIndicator.setVisibility(View.VISIBLE);
			chosenAccountView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					mAccountBoxExpanded = !mAccountBoxExpanded;
					setupAccountBoxToggle();
				}
			});
			setupAccountBoxToggle();
			populateAccountList(accounts);
		}
	}

	private void populateAccountList(List<OUser> accounts) {
		mAccountListContainer.removeAllViews();

		LayoutInflater layoutInflater = LayoutInflater.from(this);
		OUser me = OUser.current(mContext);
		for (final OUser account : accounts) {

			View itemView = layoutInflater.inflate(
					R.layout.base_navdrawer_account_list_item,
					mAccountListContainer, false);
			((TextView) itemView.findViewById(R.id.profile_email_text))
					.setText(account.getName());
			if (!account.getAvatar().equals("false"))
				((ImageView) itemView.findViewById(R.id.profile_image))
						.setImageBitmap(Base64Helper.getBitmapImage(mContext,
								account.getAvatar()));
			if (!account.getAndroidName().equals(me.getAndroidName())) {
				itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						OdooAccountManager.loginUser(mContext,
								account.getAndroidName());
						mAccountBoxExpanded = false;
						setupAccountBoxToggle();
						mDrawerLayout.closeDrawer(Gravity.START);
						setupAccountBox();
						Intent intent = getIntent();
						finish();
						startActivity(intent);
					}
				});
			}
			mAccountListContainer.addView(itemView);
		}

		populateAccountOptionsList(layoutInflater);
	}

	private void populateAccountOptionsList(LayoutInflater layoutInflater) {
		View itemView = layoutInflater.inflate(
				R.layout.base_navdrawer_account_options, mAccountListContainer,
				false);
		itemView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onSettingItemSelected(SettingKeys.ACCOUNTS);
				mAccountBoxExpanded = false;
				setupAccountBoxToggle();
				mDrawerLayout.closeDrawer(Gravity.START);
			}
		});
		mAccountListContainer.addView(itemView);
	}

	private void setupAccountBoxToggle() {
		mExpandAccountBoxIndicator
				.setImageResource(mAccountBoxExpanded ? R.drawable.ic_drawer_accounts_collapse
						: R.drawable.ic_drawer_accounts_expand);
		int hideTranslateY = -mAccountListContainer.getHeight() / 4; // last 25%
																		// of
																		// animation
		if (mAccountBoxExpanded && mAccountListContainer.getTranslationY() == 0) {
			// initial setup
			mAccountListContainer.setAlpha(0);
			mAccountListContainer.setTranslationY(hideTranslateY);
		}

		AnimatorSet set = new AnimatorSet();
		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mDrawerItemsListContainer
						.setVisibility(mAccountBoxExpanded ? View.INVISIBLE
								: View.VISIBLE);
				mAccountListContainer
						.setVisibility(mAccountBoxExpanded ? View.VISIBLE
								: View.INVISIBLE);
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				onAnimationEnd(animation);
			}
		});

		if (mAccountBoxExpanded) {
			mAccountListContainer.setVisibility(View.VISIBLE);
			AnimatorSet subSet = new AnimatorSet();
			subSet.playTogether(
					ObjectAnimator
							.ofFloat(mAccountListContainer, View.ALPHA, 1)
							.setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),
					ObjectAnimator.ofFloat(mAccountListContainer,
							View.TRANSLATION_Y, 0).setDuration(
							ACCOUNT_BOX_EXPAND_ANIM_DURATION));
			set.playSequentially(
					ObjectAnimator.ofFloat(mDrawerItemsListContainer,
							View.ALPHA, 0).setDuration(
							ACCOUNT_BOX_EXPAND_ANIM_DURATION), subSet);
			set.start();
		} else {
			mDrawerItemsListContainer.setVisibility(View.VISIBLE);
			AnimatorSet subSet = new AnimatorSet();
			subSet.playTogether(
					ObjectAnimator
							.ofFloat(mAccountListContainer, View.ALPHA, 0)
							.setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),
					ObjectAnimator.ofFloat(mAccountListContainer,
							View.TRANSLATION_Y, hideTranslateY).setDuration(
							ACCOUNT_BOX_EXPAND_ANIM_DURATION));
			set.playSequentially(
					subSet,
					ObjectAnimator.ofFloat(mDrawerItemsListContainer,
							View.ALPHA, 1).setDuration(
							ACCOUNT_BOX_EXPAND_ANIM_DURATION));
			set.start();
		}

		set.start();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

interface FragmentLoader {
	public void loadFragment(DrawerItem item);

	public boolean onSettingItemSelected(SettingKeys key);
}
