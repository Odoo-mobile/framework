/**
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
 * Created on 18/12/14 5:25 PM
 */
package com.odoo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.odoo.account.ManageAccounts;
import com.odoo.account.OdooLogin;
import com.odoo.auth.OdooAccountManager;
import com.odoo.auth.OdooAuthenticator;
import com.odoo.support.OUser;
import com.odoo.utils.BitmapUtils;
import com.odoo.utils.OActionBarUtils;
import com.odoo.utils.OControls;
import com.odoo.utils.OResource;
import com.odoo.utils.sys.IOnActivityResultListener;
import com.odoo.utils.sys.IOnBackPressListener;

import java.util.List;

public class OdooActivity extends ActionBarActivity {

    public static final String TAG = OdooActivity.class.getSimpleName();
    public static final Integer DRAWER_ITEM_LAUNCH_DELAY = 300;
    public static final Integer DRAWER_ACCOUNT_BOX_ANIMATION_DURATION = 250;
    public static final String KEY_ACCOUNT_REQUEST = "key_account_request";
    public static final String KEY_NEW_USER_NAME = "key_new_account_username";
    public static final Integer REQUEST_ACCOUNT_CREATE = 1101;
    public static final Integer REQUEST_ACCOUNTS_MANAGE = 1102;

    private DrawerLayout mDrawerLayout = null;
    private ActionBarDrawerToggle mDrawerToggle = null;
    private IOnBackPressListener backPressListener = null;
    private IOnActivityResultListener mIOnActivityResultListener = null;
    //Drawer Containers
    private LinearLayout mDrawerAccountContainer = null;
    private LinearLayout mDrawerItemContainer = null;
    private Boolean mAccountBoxExpanded = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.odoo_activity);
        OActionBarUtils.setActionBar(this, true);
        setupDrawer();
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
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
        mDrawerToggle.syncState();

        setupAccountBox();
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
        url.setText((currentUser.isOAauthLogin()) ? currentUser.getInstanceUrl() : currentUser.getHost());

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
                mDrawerAccountContainer
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
                    ObjectAnimator.ofFloat(mDrawerAccountContainer,
                            View.ALPHA, 0).setDuration(
                            DRAWER_ACCOUNT_BOX_ANIMATION_DURATION), subSet);
            set.start();
        } else {
            mDrawerAccountContainer.setVisibility(View.VISIBLE);
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
                    ObjectAnimator.ofFloat(mDrawerAccountContainer,
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
                OControls.setText(view, R.id.profile_url_text, (user.isOAauthLogin()) ? user.getInstanceUrl() : user.getHost());
                // Setting login event for other account
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Logging in to other account
                        OdooAccountManager.login(OdooActivity.this, user.getAndroidName());

                        mAccountBoxExpanded = false;
                        accountBoxToggle();
                        mDrawerLayout.closeDrawer(Gravity.START);
                        // Restarting activity
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = getIntent();
                                finish();
                                startActivity(intent);
                            }
                        }, DRAWER_ITEM_LAUNCH_DELAY);
                    }
                });
                mDrawerAccountContainer.addView(view);
            }
        }
        accountListDefaultItems();
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
        OControls.setGone(view,R.id.profile_url_text);
        ImageView icon = (ImageView) view.findViewById(R.id.profile_image);
        icon.setImageResource(res_id);
        icon.setColorFilter(OResource.color(this, R.color.body_text_2));
        TextView name = (TextView) view.findViewById(R.id.profile_name_text);
        name.setTypeface(name.getTypeface(), Typeface.BOLD);
        name.setText(title);
        return view;
    }

    /**
     * Lock/Unlock drawer sliding
     *
     * @param locked, boolean value to set drawer locked/unlock
     */
    public void lockDrawer(boolean locked) {
        mDrawerLayout.setDrawerLockMode((!locked) ? DrawerLayout.STATE_IDLE : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
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
                    mDrawerLayout.closeDrawer(Gravity.START);
                    accountBoxToggle();
                }
                OdooAccountManager.login(this, data.getStringExtra(KEY_NEW_USER_NAME));
                setupAccountBox();
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
}
