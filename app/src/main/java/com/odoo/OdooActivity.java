/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p/>
 * Created on 18/12/14 5:25 PM
 */
package com.odoo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.odoo.core.account.AppIntro;
import com.odoo.core.account.ManageAccounts;
import com.odoo.core.account.OdooLogin;
import com.odoo.core.account.OdooUserAskPassword;
import com.odoo.core.account.OdooUserObjectUpdater;
import com.odoo.core.auth.OdooAccountManager;
import com.odoo.core.auth.OdooAuthenticator;
import com.odoo.core.support.OUser;
import com.odoo.core.support.OdooCompatActivity;
import com.odoo.core.support.addons.fragment.IBaseFragment;
import com.odoo.core.support.drawer.ODrawerItem;
import com.odoo.core.support.sync.SyncUtils;
import com.odoo.core.utils.BitmapUtils;
import com.odoo.core.utils.OAlert;
import com.odoo.core.utils.OAppBarUtils;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.OFragmentUtils;
import com.odoo.core.utils.OPreferenceManager;
import com.odoo.core.utils.OResource;
import com.odoo.core.utils.drawer.DrawerUtils;
import com.odoo.core.utils.sys.IOnActivityResultListener;
import com.odoo.core.utils.sys.IOnBackPressListener;

import java.util.List;

public class OdooActivity extends OdooCompatActivity {

    public static final String TAG = OdooActivity.class.getSimpleName();
    public static final Integer DRAWER_ITEM_LAUNCH_DELAY = 300;
    public static final Integer DRAWER_ACCOUNT_BOX_ANIMATION_DURATION = 250;
    public static final String KEY_ACCOUNT_REQUEST = "key_account_request";
    public static final String KEY_NEW_USER_NAME = "key_new_account_username";
    public static final String KEY_CURRENT_DRAWER_ITEM = "key_drawer_item_index";
    public static final String KEY_APP_TITLE = "key_app_title";
    public static final String KEY_HAS_ACTIONBAR_SPINNER = "key_has_actionbar_spinner";
    public static final Integer REQUEST_ACCOUNT_CREATE = 1101;
    public static final Integer REQUEST_ACCOUNTS_MANAGE = 1102;
    public static final String KEY_FRESH_LOGIN = "key_fresh_login";

    private DrawerLayout mDrawerLayout = null;
    private ActionBarDrawerToggle mDrawerToggle = null;
    private IOnBackPressListener backPressListener = null;
    private IOnActivityResultListener mIOnActivityResultListener = null;
    //Drawer Containers
    private LinearLayout mDrawerAccountContainer = null;
    private LinearLayout mDrawerItemContainer = null;
    private Boolean mAccountBoxExpanded = false;
    private Bundle mSavedInstanceState = null;
    private Integer mDrawerSelectedIndex = -1;
    private Boolean mHasActionBarSpinner = false;
    private App app;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "OdooActivity->onCreate");
        mSavedInstanceState = savedInstanceState;
        app = (App) getApplicationContext();
        startApp(savedInstanceState);
    }

    private void startApp(Bundle savedInstanceState) {
        OPreferenceManager preferenceManager = new OPreferenceManager(this);
        if (!preferenceManager.getBoolean(KEY_FRESH_LOGIN, false)) {
            preferenceManager.setBoolean(KEY_FRESH_LOGIN, true);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(OdooActivity.this, AppIntro.class));
                }
            }, 1000);
        }
        setContentView(R.layout.odoo_activity);
        OAppBarUtils.setAppBar(this, true);
        setupDrawer();
        // Validating user object
        validateUserObject();
    }

    private void validateUserObject() {
        if (OdooAccountManager.anyActiveUser(this)) {
            OUser user = OUser.current(this);
            if (!OdooAccountManager.isValidUserObj(this, user)
                    && app.inNetwork()) {
                OdooUserObjectUpdater.showUpdater(this, new OdooUserObjectUpdater.OnUpdateFinish() {
                    @Override
                    public void userObjectUpdateFinished() {
                        startActivity(new Intent(OdooActivity.this, OdooLogin.class));
                        finish();
                    }

                    @Override
                    public void userObjectUpdateFail() {
                        Toast.makeText(OdooActivity.this, OResource.string(OdooActivity.this,
                                R.string.toast_something_gone_wrong), Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            }
        }
    }

    // Creating drawer
    private void setupDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_name, R.string.app_name) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                setTitle(getResources().getString(R.string.app_name));
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
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerToggle.syncState();

        setupAccountBox();
        setupDrawerBox();
    }

    private void setupDrawerBox() {
        mDrawerItemContainer = (LinearLayout) findViewById(R.id.drawerItemList);
        mDrawerItemContainer.removeAllViews();
        List<ODrawerItem> items = DrawerUtils.getDrawerItems(this);
        for (ODrawerItem item : items) {
            View view = LayoutInflater.from(this).
                    inflate((item.isGroupTitle()) ? R.layout.base_drawer_group_layout :
                            R.layout.base_drawer_menu_item, mDrawerItemContainer, false);
            view.setTag(item);
            if (!item.isGroupTitle()) {
                view.setOnClickListener(drawerItemClick);
            }
            mDrawerItemContainer.addView(DrawerUtils.fillDrawerItemValue(view, item));
        }
    }

    private View.OnClickListener drawerItemClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int index = mDrawerItemContainer.indexOfChild(v);
            if (mDrawerSelectedIndex != index) {
                ODrawerItem item = (ODrawerItem) v.getTag();
                if (item.getInstance() instanceof Fragment) {
                    focusOnDrawerItem(index);
                    setTitle(item.getTitle());
                }
                loadDrawerItemInstance(item.getInstance(), item.getExtra());
            } else {
                closeDrawer();
            }
        }
    };

    public void closeDrawer() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
        }, DRAWER_ITEM_LAUNCH_DELAY);

    }

    /**
     * Loads fragment or start intent
     *
     * @param instance, instance of fragment or intent
     */
    private void loadDrawerItemInstance(Object instance, Bundle extra) {
        if (instance != null) {
            if (instance instanceof Intent) {
                Log.i(TAG, "Loading intent: " + instance.getClass().getCanonicalName());
                startActivity((Intent) instance);
            }
            if (instance instanceof Class<?>) {
                Class<?> cls = (Class<?>) instance;
                Intent intent = null;
                if (cls.getSuperclass().isAssignableFrom(Activity.class)) {
                    intent = new Intent(this, cls);
                }
                if (cls.getSuperclass().isAssignableFrom(ActionBarActivity.class)) {
                    intent = new Intent(this, cls);
                }
                if (intent != null) {
                    if (extra != null)
                        intent.putExtras(extra);
                    loadDrawerItemInstance(intent, null);
                    return;
                }
            }
            if (instance instanceof Fragment) {
                Log.i(TAG, "Loading fragment: " + instance.getClass().getCanonicalName());
                OFragmentUtils.get(this, mSavedInstanceState).startFragment((Fragment) instance, false, extra);
            }
        }
        closeDrawer();
    }

    public void loadFragment(Fragment fragment, Boolean addToBackState, Bundle extra) {
        OFragmentUtils.get(this, null).startFragment(fragment, addToBackState, extra);
    }

    private void setupAccountBox() {
        mDrawerAccountContainer = (LinearLayout) findViewById(R.id.accountList);
        View chosenAccountView = findViewById(R.id.drawerAccountView);
        OUser currentUser = OUser.current(this);
        if (currentUser == null) {
            chosenAccountView.setVisibility(View.GONE);
            mDrawerAccountContainer.setVisibility(View.GONE);
            return;
        } else {
            chosenAccountView.setVisibility(View.VISIBLE);
            mDrawerAccountContainer.setVisibility(View.INVISIBLE);
        }

        ImageView avatar = (ImageView) chosenAccountView.findViewById(R.id.profile_image);
        TextView name = (TextView) chosenAccountView.findViewById(R.id.profile_name_text);
        TextView url = (TextView) chosenAccountView.findViewById(R.id.profile_url_text);

        name.setText(currentUser.getName());
        url.setText(currentUser.getHost());

        if (!currentUser.getAvatar().equals("false")) {
            Bitmap bitmap = BitmapUtils.getBitmapImage(this, currentUser.getAvatar());
            if (bitmap != null)
                avatar.setImageBitmap(bitmap);
        }

        // Setting Accounts
        List<OUser> accounts = OdooAccountManager.getAllAccounts(this);
        if (accounts.size() > 0) {
            chosenAccountView.setEnabled(true);
            ImageView boxIndicator = (ImageView) findViewById(R.id.expand_account_box_indicator);
            boxIndicator.setVisibility(View.VISIBLE);
            chosenAccountView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAccountBoxExpanded = !mAccountBoxExpanded;
                    accountBoxToggle();
                }
            });
            populateAccountList(currentUser, accounts);
        }
    }

    private void accountBoxToggle() {
        ImageView boxIndicator = (ImageView) findViewById(R.id.expand_account_box_indicator);
        boxIndicator.setImageResource(mAccountBoxExpanded ? R.drawable.ic_drawer_accounts_collapse
                : R.drawable.ic_drawer_accounts_expand);
        int hideTranslateY = -mDrawerAccountContainer.getHeight() / 4;
        if (mAccountBoxExpanded && mDrawerAccountContainer.getTranslationY() == 0) {
            // initial setup
            mDrawerAccountContainer.setAlpha(0);
            mDrawerAccountContainer.setTranslationY(hideTranslateY);
        }

        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mDrawerItemContainer
                        .setVisibility(mAccountBoxExpanded ? View.INVISIBLE
                                : View.VISIBLE);
                mDrawerAccountContainer
                        .setVisibility(mAccountBoxExpanded ? View.VISIBLE
                                : View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimationEnd(animation);
            }
        });

        if (mAccountBoxExpanded) {
            mDrawerAccountContainer.setVisibility(View.VISIBLE);
            AnimatorSet subSet = new AnimatorSet();
            subSet.playTogether(
                    ObjectAnimator
                            .ofFloat(mDrawerAccountContainer, View.ALPHA, 1)
                            .setDuration(DRAWER_ACCOUNT_BOX_ANIMATION_DURATION),
                    ObjectAnimator.ofFloat(mDrawerAccountContainer,
                            View.TRANSLATION_Y, 0).setDuration(
                            DRAWER_ACCOUNT_BOX_ANIMATION_DURATION));
            set.playSequentially(
                    ObjectAnimator.ofFloat(mDrawerItemContainer,
                            View.ALPHA, 0).setDuration(
                            DRAWER_ACCOUNT_BOX_ANIMATION_DURATION), subSet);
            set.start();
        } else {
            mDrawerItemContainer.setVisibility(View.VISIBLE);
            AnimatorSet subSet = new AnimatorSet();
            subSet.playTogether(
                    ObjectAnimator
                            .ofFloat(mDrawerAccountContainer, View.ALPHA, 0)
                            .setDuration(DRAWER_ACCOUNT_BOX_ANIMATION_DURATION),
                    ObjectAnimator.ofFloat(mDrawerAccountContainer,
                            View.TRANSLATION_Y, hideTranslateY).setDuration(
                            DRAWER_ACCOUNT_BOX_ANIMATION_DURATION));
            set.playSequentially(
                    subSet,
                    ObjectAnimator.ofFloat(mDrawerItemContainer,
                            View.ALPHA, 1).setDuration(
                            DRAWER_ACCOUNT_BOX_ANIMATION_DURATION));
            set.start();
        }

        set.start();

    }

    private void populateAccountList(OUser me, List<OUser> accounts) {
        mDrawerAccountContainer.removeAllViews();
        for (final OUser user : accounts) {
            if (!user.getAndroidName().equals(me.getAndroidName())) {
                View view = LayoutInflater.from(this).inflate(R.layout.base_drawer_account_item, mDrawerAccountContainer, false);
                ImageView avatar = (ImageView) view.findViewById(R.id.profile_image);
                if (!user.getAvatar().equals("false")) {
                    Bitmap img = BitmapUtils.getBitmapImage(this, user.getAvatar());
                    if (img != null)
                        avatar.setImageBitmap(img);
                }
                OControls.setText(view, R.id.profile_name_text, user.getName());
                OControls.setText(view, R.id.profile_url_text, user.getHost());
                // Setting login event for other account
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        OdooUserAskPassword.get(OdooActivity.this, user)
                                .setOnUserPasswordValidateListener(
                                        new OdooUserAskPassword.OnUserPasswordValidateListener() {
                                            @Override
                                            public void onSuccess() {
                                                // Logging in to other account
                                                OdooAccountManager.login(OdooActivity.this,
                                                        user.getAndroidName());
                                                mAccountBoxExpanded = false;
                                                accountBoxToggle();
                                                mDrawerLayout.closeDrawer(GravityCompat.START);
                                                // Restarting activity
                                                restartActivity();
                                            }

                                            @Override
                                            public void onCancel() {
                                            }

                                            @Override
                                            public void onFail() {
                                                OAlert.showError(OdooActivity.this,
                                                        OResource.string(OdooActivity.this,
                                                                R.string.error_invalid_password));
                                            }
                                        }).show();

                    }
                });
                mDrawerAccountContainer.addView(view);
            }
        }
        accountListDefaultItems();
    }

    private void restartActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(OdooActivity.this, OdooActivity.class);
                finish();
                startActivity(intent);
            }
        }, DRAWER_ITEM_LAUNCH_DELAY);
    }

    private void accountListDefaultItems() {
        // Adding add account
        View view = generateView(OResource.string(this, R.string.label_drawer_account_add_account),
                R.drawable.ic_action_add);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginActivity = new Intent(OdooActivity.this, OdooLogin.class);
                loginActivity.putExtra(OdooAuthenticator.KEY_NEW_ACCOUNT_REQUEST, true);
                loginActivity.putExtra(KEY_ACCOUNT_REQUEST, true);
                startActivityForResult(loginActivity, REQUEST_ACCOUNT_CREATE);
            }
        });
        mDrawerAccountContainer.addView(view);

        // Adding add account
        view = generateView(OResource.string(this, R.string.label_drawer_account_manage_accounts),
                R.drawable.ic_action_settings);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(OdooActivity.this, ManageAccounts.class),
                        REQUEST_ACCOUNTS_MANAGE);
            }
        });
        mDrawerAccountContainer.addView(view);
    }

    private View generateView(String title, int res_id) {
        View view = LayoutInflater.from(this).inflate(R.layout.base_drawer_account_item,
                mDrawerAccountContainer, false);
        OControls.setGone(view, R.id.profile_url_text);
        ImageView icon = (ImageView) view.findViewById(R.id.profile_image);
        icon.setImageResource(res_id);
        icon.setColorFilter(OResource.color(this, R.color.body_text_2));
        TextView name = (TextView) view.findViewById(R.id.profile_name_text);
        name.setTypeface(name.getTypeface(), Typeface.BOLD);
        name.setText(title);
        return view;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (backPressListener != null) {
            if (backPressListener.onBackPressed()) {
                super.onBackPressed();
            }
        } else
            super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mIOnActivityResultListener != null) {
            mIOnActivityResultListener.onOdooActivityResult(requestCode, resultCode, data);
        }
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ACCOUNT_CREATE) {
                if (mDrawerLayout != null) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                    accountBoxToggle();
                }
                OdooAccountManager.login(this, data.getStringExtra(KEY_NEW_USER_NAME));
                restartActivity();
            }
            if (requestCode == REQUEST_ACCOUNTS_MANAGE) {
                startActivity(new Intent(this, OdooLogin.class));
                finish();
            }
        }
    }

    /**
     * Set system back button press listener
     *
     * @param listener
     */
    public void setOnBackPressListener(IOnBackPressListener listener) {
        backPressListener = listener;
    }

    public void setOnActivityResultListener(IOnActivityResultListener listener) {
        mIOnActivityResultListener = listener;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mSavedInstanceState = savedInstanceState;
        if (savedInstanceState == null) {
            // Loading Default Fragment (if any)
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    IBaseFragment fragment = DrawerUtils.getDefaultDrawerFragment();
                    if (fragment != null) {
                        ODrawerItem item = DrawerUtils.getStartableObject(OdooActivity.this, fragment);
                        setTitle(item.getTitle());
                        loadDrawerItemInstance(item.getInstance(), item.getExtra());
                        int selected_item = DrawerUtils.findItemIndex(item, mDrawerItemContainer);
                        if (selected_item > -1) {
                            focusOnDrawerItem(selected_item);
                        }
                    }
                }
            }, DRAWER_ITEM_LAUNCH_DELAY);
        } else {
            mHasActionBarSpinner = savedInstanceState.getBoolean(KEY_HAS_ACTIONBAR_SPINNER);
            mDrawerSelectedIndex = savedInstanceState.getInt(KEY_CURRENT_DRAWER_ITEM);
            setTitle(savedInstanceState.getString(KEY_APP_TITLE));
            focusOnDrawerItem(mDrawerSelectedIndex);
        }
    }


    private void focusOnDrawerItem(int index) {
        mDrawerSelectedIndex = index;
        for (int i = 0; i < mDrawerItemContainer.getChildCount(); i++) {
            DrawerUtils.focusOnView(this, mDrawerItemContainer.getChildAt(i), i == index);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_CURRENT_DRAWER_ITEM, mDrawerSelectedIndex);
        outState.putString(KEY_APP_TITLE, getTitle().toString());
        outState.putBoolean(KEY_HAS_ACTIONBAR_SPINNER, mHasActionBarSpinner);
        super.onSaveInstanceState(outState);
    }


    public SyncUtils sync() {
        return SyncUtils.get(this);
    }

    /**
     * Actionbar Spinner handler
     */

    public void setHasActionBarSpinner(Boolean hasActionBarSpinner) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            Spinner spinner = (Spinner) findViewById(R.id.spinner_nav);
            if (hasActionBarSpinner) {
                if (spinner != null)
                    spinner.setVisibility(View.VISIBLE);
                actionBar.setDisplayShowTitleEnabled(false);
            } else {
                if (spinner != null)
                    spinner.setVisibility(View.GONE);
                actionBar.setDisplayShowTitleEnabled(true);
            }
            mHasActionBarSpinner = hasActionBarSpinner;
        }
    }

    public Spinner getActionBarSpinner() {
        Spinner spinner = null;
        if (mHasActionBarSpinner) {
            spinner = (Spinner) findViewById(R.id.spinner_nav);
            spinner.setAdapter(null);
        }
        return spinner;
    }

    public void refreshDrawer() {
        setupDrawerBox();
    }

}
