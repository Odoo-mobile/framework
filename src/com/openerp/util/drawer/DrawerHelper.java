package com.openerp.util.drawer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.openerp.config.ModulesConfig;
import com.openerp.support.Module;
import com.openerp.support.fragment.FragmentHelper;

public class DrawerHelper {
	public static List<DrawerItem> drawerItems(Context context) {
		List<DrawerItem> items = new ArrayList<DrawerItem>();
		for (Module module : new ModulesConfig().modules()) {
			FragmentHelper model = (FragmentHelper) module.getModuleInstance();
			List<DrawerItem> drawerItems = model.drawerMenus(context);
			if (drawerItems != null)
				items.addAll(drawerItems);
		}
		return items;
	}
}
