package com.odoo;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
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
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
	private Boolean hideActionbar = true;

	// variables that control the Action Bar auto hide behavior (aka
	// "quick recall")
	private static final int HEADER_HIDE_ANIM_DURATION = 300;
	private boolean mActionBarAutoHideEnabled = false;
	private int mActionBarAutoHideSensivity = 0;
	private int mActionBarAutoHideMinY = 0;
	private int mActionBarAutoHideSignal = 0;
	private boolean mActionBarShown = true;
	// When set, these components will be shown/hidden in sync with the action
	// bar
	// to implement the "quick recall" effect (the Action Bar and the header
	// views disappear
	// when you scroll down a list, and reappear quickly when you scroll up).
	private ArrayList<View> mHideableHeaderViews = new ArrayList<View>();

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

	protected void autoShowOrHideActionBar(boolean show) {
		if (show == mActionBarShown) {
			return;
		}
		mActionBarShown = show;
		// getLPreviewUtils().showHideActionBarIfPartOfDecor(show);
		onActionBarAutoShowOrHide(show);
	}

	protected void registerHideableHeaderView(View hideableHeaderView) {
		if (!mHideableHeaderViews.contains(hideableHeaderView)) {
			mHideableHeaderViews.add(hideableHeaderView);
		}
	}

	protected void deregisterHideableHeaderView(View hideableHeaderView) {
		if (mHideableHeaderViews.contains(hideableHeaderView)) {
			mHideableHeaderViews.remove(hideableHeaderView);
		}
	}

	public void hideActionBar(boolean hide) {
		hideActionbar = hide;
	}

	@SuppressLint({ "InlinedApi", "NewApi" })
	protected void onActionBarAutoShowOrHide(boolean shown) {
		for (View view : mHideableHeaderViews) {
			if (shown) {
				view.animate().translationY(0).alpha(1)
						.setDuration(HEADER_HIDE_ANIM_DURATION)
						.setInterpolator(new DecelerateInterpolator());
			} else {
				view.animate().translationY(-view.getBottom()).alpha(0)
						.setDuration(HEADER_HIDE_ANIM_DURATION)
						.setInterpolator(new DecelerateInterpolator());
			}
		}
		if (hideActionbar) {
			if (shown) {
				getActionBar().show();
			} else {
				getActionBar().hide();
			}
		}
	}

	/**
	 * Initializes the Action Bar auto-hide (aka Quick Recall) effect.
	 */
	private void initActionBarAutoHide() {
		mActionBarAutoHideEnabled = true;
		mActionBarAutoHideMinY = getResources().getDimensionPixelSize(
				R.dimen.action_bar_auto_hide_min_y);
		mActionBarAutoHideSensivity = getResources().getDimensionPixelSize(
				R.dimen.action_bar_auto_hide_sensivity);
	}

	/**
	 * Indicates that the main content has scrolled (for the purposes of
	 * showing/hiding the action bar for the "action bar auto hide" effect).
	 * currentY and deltaY may be exact (if the underlying view supports it) or
	 * may be approximate indications: deltaY may be INT_MAX to mean
	 * "scrolled forward indeterminately" and INT_MIN to mean
	 * "scrolled backward indeterminately". currentY may be 0 to mean "somewhere
	 * close to the start of the list" and INT_MAX to mean "we don't know, but
	 * not at the start of the list"
	 */
	private void onMainContentScrolled(int currentY, int deltaY) {
		if (deltaY > mActionBarAutoHideSensivity) {
			deltaY = mActionBarAutoHideSensivity;
		} else if (deltaY < -mActionBarAutoHideSensivity) {
			deltaY = -mActionBarAutoHideSensivity;
		}

		if (Math.signum(deltaY) * Math.signum(mActionBarAutoHideSignal) < 0) {
			// deltaY is a motion opposite to the accumulated signal, so reset
			// signal
			mActionBarAutoHideSignal = deltaY;
		} else {
			// add to accumulated signal
			mActionBarAutoHideSignal += deltaY;
		}

		boolean shouldShow = currentY < mActionBarAutoHideMinY
				|| (mActionBarAutoHideSignal <= -mActionBarAutoHideSensivity);
		autoShowOrHideActionBar(shouldShow);
	}

	protected void enableActionBarAutoHide(final ListView listView) {
		initActionBarAutoHide();
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			final static int ITEMS_THRESHOLD = 3;
			int lastFvi = 0;

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				onMainContentScrolled(firstVisibleItem <= ITEMS_THRESHOLD ? 0
						: Integer.MAX_VALUE,
						lastFvi - firstVisibleItem > 0 ? Integer.MIN_VALUE
								: lastFvi == firstVisibleItem ? 0
										: Integer.MAX_VALUE);
				lastFvi = firstVisibleItem;
			}
		});
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

	protected void populateNavDrawer(Bundle savedBundle) {
		Log.d(TAG, "initDrawer()");
		if (mDrawerLayout == null)
			initDrawerControls();
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		mNavDrawerItems.clear();
		mNavDrawerItems.addAll(DrawerHelper.drawerItems(mContext));
		mNavDrawerItems.addAll(setSettingMenu());
		createNavDrawerItems(savedBundle);
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

	abstract protected boolean intentRequests();

	private void createNavDrawerItems(Bundle savedBundle) {
		mDrawerItemsListContainer = (ViewGroup) findViewById(R.id.navdrawer_items_list);
		if (mDrawerItemsListContainer == null) {
			return;
		}

		mNavDrawerItemViews = new View[mNavDrawerItems.size()];
		mDrawerItemsListContainer.removeAllViews();
		int i = 0;
		for (DrawerItem item : mNavDrawerItems) {
			mNavDrawerItemViews[i] = makeNavDrawerItem(item,
					mDrawerItemsListContainer, savedBundle);
			mDrawerItemsListContainer.addView(mNavDrawerItemViews[i]);
			++i;
		}
		if (mDrawerItemSelectedPosition == -1) {
			selectDrawerItem(savedBundle);
		}
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	private void selectDrawerItem(Bundle savedBundle) {
		if (savedBundle != null) {
			return;
		}
		for (DrawerItem item : mNavDrawerItems) {
			if (!item.isGroupTitle()) {
				onNavDrawerItemClicked(item, savedBundle);
				break;
			}
		}
	}

	private View makeNavDrawerItem(final DrawerItem item, ViewGroup container,
			final Bundle savedBundle) {
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
			OControls
					.setText(view, R.id.navdrawer_group_title, item.getTitle());
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
		if (iconId > 0) {
			iconView.setImageResource(iconId);
		}
		titleView.setText(title);
		if (counter > 0) {
			String ctr = /* (counter > 99) ? "99+" : */counter + "";
			counterView.setText(ctr);
		} else {
			counterView.setText("");
		}
		formatNavDrawerItem(view, item, selected);

		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onNavDrawerItemClicked(item, savedBundle);
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
			if (!item.isGroupTitle()) {
				String counter = (item.getCounter() > 99) ? "99+" : (item
						.getCounter() == 0) ? "" : item.getCounter() + "";
				OControls.setText(v, R.id.counter, counter);
				itemIndex++;
			}
		}

	}

	protected void onNavDrawerItemClicked(final DrawerItem item,
			Bundle savedBundle) {
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
		if (item.getFragmentInstace() instanceof Fragment) {
			Fragment fragment = (Fragment) item.getFragmentInstace();
			if (fragment.getArguments() != null
					&& fragment.getArguments().containsKey("settings"))
				return true;
		}
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

	public void setDrawerToggleSyncState() {
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
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("current_drawer_item", mDrawerItemSelectedPosition);
		super.onSaveInstanceState(outState);
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
