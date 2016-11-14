/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p>
 * Created on 30/12/14 4:27 PM
 */
package com.odoo.core.utils.drawer;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.odoo.R;
import com.odoo.SettingsActivity;
import com.odoo.config.Addons;
import com.odoo.core.account.Profile;
import com.odoo.core.support.addons.OAddon;
import com.odoo.core.support.addons.fragment.IBaseFragment;
import com.odoo.core.support.drawer.ODrawerItem;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.OPreferenceManager;
import com.odoo.core.utils.OResource;

import java.util.ArrayList;
import java.util.List;


public class DrawerUtils {

    public static List<ODrawerItem> getDrawerItems(Context context) {
        List<ODrawerItem> items = new ArrayList<>();
        for (OAddon addon : new Addons().getAddons()) {
            IBaseFragment frag = (IBaseFragment) addon.get();
            if (frag != null) {
                List<ODrawerItem> menus = frag.drawerMenus(context);
                if (menus != null) {
                    items.addAll(menus);
                }
            }
        }
        items.addAll(DrawerUtils.baseSettingsItems(context));
        return items;
    }

    public static List<ODrawerItem> baseSettingsItems(Context context) {
        String key = "base.settings";
        OPreferenceManager pref = new OPreferenceManager(context);
        List<ODrawerItem> settings = new ArrayList<>();
        settings.add(new ODrawerItem(key).setTitle(OResource.string(context, R.string.label_settings))
                .setGroupTitle());
        settings.add(new ODrawerItem(key).setTitle(OResource.string(context, R.string.title_profile))
                .setInstance(Profile.class).setIcon(R.drawable.ic_action_user));
        settings.add(new ODrawerItem(key).setTitle(OResource.string(context, R.string.label_settings))
                .setIcon(R.drawable.ic_action_settings)
                .setInstance(SettingsActivity.class));
        return settings;
    }

    public static View fillDrawerItemValue(View view, ODrawerItem item) {
        if (item.isGroupTitle()) {
            OControls.setText(view, R.id.group_title, item.getTitle());
        } else {
            if (item.getIcon() > 0)
                OControls.setImage(view, R.id.icon, item.getIcon());
            else
                view.findViewById(R.id.icon).setVisibility(View.GONE);
            OControls.setText(view, R.id.title, item.getTitle());
            if (item.getCounter() > 0) {
                OControls.setText(view, R.id.counter, item.getCounter() + "");
            }
        }
        return view;
    }

    public static IBaseFragment getDefaultDrawerFragment() {
        OAddon addon = new Addons().getDefaultAddon();
        if (addon != null) {
            return (IBaseFragment) addon.get();
        }
        return null;
    }

    public static ODrawerItem getStartableObject(Context context, IBaseFragment fragment) {
        List<ODrawerItem> items = fragment.drawerMenus(context);
        if (items != null) {
            for (ODrawerItem item : items) {
                if (!item.isGroupTitle() && item.getInstance() != null) {
                    return item;
                }
            }
        }
        return null;
    }

    public static void focusOnView(Context context, View view, boolean focused) {
        ODrawerItem item = (ODrawerItem) view.getTag();
        if (!item.isGroupTitle()) {
            ImageView icon = (ImageView) view.findViewById(R.id.icon);
            TextView title = (TextView) view.findViewById(R.id.title);
            TextView counter = (TextView) view.findViewById(R.id.counter);
            if (focused) {
                icon.setColorFilter(context.getResources().getColor(R.color.drawer_icon_tint_selected));
                title.setTextColor(context.getResources().getColor(R.color.drawer_text_color_selected));
                title.setTypeface(title.getTypeface(), Typeface.BOLD);
                counter.setTextColor(context.getResources().getColor(R.color.drawer_text_color_selected));
                counter.setTypeface(title.getTypeface(), Typeface.BOLD);
            } else {
                icon.setColorFilter(context.getResources().getColor(R.color.drawer_icon_tint));
                title.setTextColor(context.getResources().getColor(R.color.drawer_text_color));
                title.setTypeface(null, Typeface.NORMAL);
                counter.setTextColor(context.getResources().getColor(R.color.drawer_text_color));
                counter.setTypeface(null, Typeface.NORMAL);
            }
        }
    }

    public static Integer findItemIndex(ODrawerItem item, LinearLayout itemContainer) {
        for (int i = 0; i < itemContainer.getChildCount(); i++) {
            ODrawerItem dItem = (ODrawerItem) itemContainer.getChildAt(i).getTag();
            if (dItem != null && dItem.getKey().equals(item.getKey())) {
                return i;
            }
        }
        return -1;

    }
}
