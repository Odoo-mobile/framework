/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http://www.openerp.com>)
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * 
 */

package com.openerp;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.openerp.auth.OpenERPAccountManager;
import com.openerp.config.ModulesConfig;
import com.openerp.support.Module;
import com.openerp.support.menu.OEMenu;
import com.openerp.support.menu.OEMenuAdapter;
import com.openerp.support.menu.OEMenuItems;

// TODO: Auto-generated Javadoc
/**
 * The Class MenuDrawerHelper.
 */
public class MenuDrawerHelper {

	/** The m drawer layout. */
	DrawerLayout mDrawerLayout;

	/** The m drawer list. */
	public ListView mDrawerList;

	/** The m drawer toggle. */
	public ActionBarDrawerToggle mDrawerToggle;

	/** The instance. */
	private MainActivity instance = null;

	/** The menu items. */
	private List<OEMenuItems> mMenuItems = new ArrayList<OEMenuItems>();

	/** The modules. */
	ArrayList<Module> modules = null;

	/** The current menu. */
	private int currentMenu = 1;

	/** The menus. */
	public OEMenuItems[] menus = null;

	/**
	 * Instantiates a new menu drawer helper.
	 * 
	 * @param object
	 *            the object
	 */
	public MenuDrawerHelper(MainActivity object) {
		super();
		this.instance = object;
		modules = new ModulesConfig().modules();
		mMenuItems = null;
		this.init();
	}

	/**
	 * Instantiates a new menu drawer helper.
	 * 
	 * @param object
	 *            the object
	 */
	public MenuDrawerHelper(MainActivity object, List<OEMenuItems> app_menu) {
		super();
		this.instance = object;
		modules = new ModulesConfig().modules();
		mMenuItems = app_menu;
		this.init();
	}

	/**
	 * Gets the menu items list.
	 * 
	 * @return the menu items list
	 */
	private List<OEMenuItems> getMenuItemsList() {
		List<OEMenuItems> allMenus = new ArrayList<OEMenuItems>();
		for (Module module : modules) {
			try {
				Class newClass = Class.forName(module.getModuleInstance()
						.getClass().getName());

				Object receiver = newClass.newInstance();
				Class params[] = new Class[1];
				params[0] = Context.class;

				Method method = newClass
						.getDeclaredMethod("menuHelper", params);

				Object obj = method.invoke(receiver, this.instance);

				if (obj != null) {
					OEMenu menu = (OEMenu) obj;
					allMenus.add(new OEMenuItems(menu.getMenuTitle()
							.toUpperCase(), null, 0, true));
					for (OEMenuItems menuItem : menu.getMenuItems()) {
						allMenus.add(menuItem);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return allMenus;
	}

	/**
	 * Gets the menu list.
	 * 
	 * @return the menu list
	 */
	private OEMenuItems[] getMenuList() {
		if (mMenuItems == null) {
			mMenuItems = getMenuItemsList();
		}
		List<OEMenuItems> mList = mMenuItems;
		mList.addAll(getSettingMenu());
		menus = new OEMenuItems[mList.size()];
		int i = 0;
		for (OEMenuItems menu : mList) {
			menus[i] = menu;
			i++;
		}
		this.instance.setSystemMenus(menus);
		return menus;
	}

	private List<OEMenuItems> getSettingMenu() {
		List<OEMenuItems> allMenus = new ArrayList<OEMenuItems>();
		allMenus.add(new OEMenuItems("SETTINGS", null, 0, true));
		allMenus.add(new OEMenuItems(R.drawable.ic_action_user, "Profile",
				getFragBundle(new Fragment(), "settings",
						MainActivity.SETTING_KEYS.PROFILE), 0, false));
		allMenus.add(new OEMenuItems(R.drawable.ic_action_settings, "Settings",
				getFragBundle(new Fragment(), "settings",
						MainActivity.SETTING_KEYS.GLOBAL_SETTING), 0, false));
		allMenus.add(new OEMenuItems(R.drawable.ic_action_accounts, "Accounts",
				getFragBundle(new Fragment(), "settings",
						MainActivity.SETTING_KEYS.ACCOUNTS), 0, false));
		allMenus.add(new OEMenuItems(R.drawable.ic_action_add_account,
				"Add account", getFragBundle(new Fragment(), "settings",
						MainActivity.SETTING_KEYS.ADD_ACCOUNT), 0, false));
		allMenus.add(new OEMenuItems(R.drawable.ic_action_about, "About Us",
				getFragBundle(new Fragment(), "settings",
						MainActivity.SETTING_KEYS.ABOUT_US), 0, false));
		allMenus.add(new OEMenuItems(R.drawable.ic_action_logout, "Logout",
				getFragBundle(new Fragment(), "settings",
						MainActivity.SETTING_KEYS.LOGOUT), 0, false));
		return allMenus;
	}

	private Fragment getFragBundle(Fragment fragment, String key,
			MainActivity.SETTING_KEYS val) {
		Bundle bundle = new Bundle();
		bundle.putString(key, val.toString());
		fragment.setArguments(bundle);
		return fragment;
	}

	/**
	 * Inits the.
	 * 
	 * @return true, if successful
	 */
	private boolean init() {
		boolean flag = false;

		mDrawerLayout = (DrawerLayout) this.instance
				.findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) this.instance.findViewById(R.id.left_drawer);

		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

		if (OpenERPAccountManager.isAnyUser(instance)) {
			mDrawerList.setAdapter(new OEMenuAdapter(this.instance,
					R.layout.drawer_menu_item, this.getMenuList()));
		}
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		// enable ActionBar app icon to behave as action to toggle nav drawer
		this.instance.getActionBar().setDisplayHomeAsUpEnabled(true);
		this.instance.getActionBar().setHomeButtonEnabled(true);
		mDrawerToggle = new ActionBarDrawerToggle(this.instance, /*
																 * host Activity
																 */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				// getActionBar().setTitle(mTitle);
				instance.invalidateOptionsMenu(); // creates call to
				instance.drawerCloseListener(menus[currentMenu].getTitle());
				// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				// getActionBar().setTitle(mTitle);
				instance.invalidateOptionsMenu(); // creates call to
				instance.drawerOpenListener();
				// onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		return flag;
	}

	/* The click listner for ListView in the navigation drawer */
	/**
	 * The listener interface for receiving drawerItemClick events. The class
	 * that is interested in processing a drawerItemClick event implements this
	 * interface, and the object created with that class is registered with a
	 * component using the component's
	 * <code>addDrawerItemClickListener<code> method. When
	 * the drawerItemClick event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see DrawerItemClickEvent
	 */
	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.widget.AdapterView.OnItemClickListener#onItemClick(android
		 * .widget.AdapterView, android.view.View, int, long)
		 */
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			OEMenuItems menuItem = menus[position];
			if (!menuItem.isGroup()) {
				currentMenu = position;
				instance.setTitle(menuItem.getTitle());
				instance.selectItem(position);
			}
		}
	}

	public void lockDrawer(boolean bool) {
		if (!bool) {
			mDrawerLayout.setDrawerLockMode(DrawerLayout.STATE_IDLE);
		} else {
			mDrawerLayout
					.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		}
	}

}
