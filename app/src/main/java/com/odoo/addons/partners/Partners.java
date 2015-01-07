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
 * Created on 30/12/14 3:28 PM
 */
package com.odoo.addons.partners;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.odoo.R;
import com.odoo.base.addons.res.ResPartner;
import com.odoo.core.account.OdooLogin;
import com.odoo.core.support.addons.fragment.BaseFragment;
import com.odoo.core.support.drawer.ODrawerItem;

import java.util.ArrayList;
import java.util.List;

public class Partners extends BaseFragment {

    public static final String KEY = Partners.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.partners, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public Class<ResPartner> database() {
        return ResPartner.class;
    }

    @Override
    public List<ODrawerItem> drawerMenus(Context context) {
        List<ODrawerItem> items = new ArrayList<ODrawerItem>();
        items.add(new ODrawerItem(KEY)
                .setTitle("Default Menu")
                .setInstance(new Partners())
                .setExtra(getData("default")));
        items.add(new ODrawerItem(KEY).setTitle("Partners").setGroupTitle());
        items.add(new ODrawerItem(KEY)
                .setTitle("With Icon")
                .setIcon(R.drawable.ic_action_add)
                .setInstance(new Partners())
                .setExtra(getData("with_icon")));
        items.add(new ODrawerItem(KEY)
                .setTitle("With Counter")
                .setIcon(R.drawable.ic_action_add)
                .setCounter(10)
                .setInstance(new Partners())
                .setExtra(getData("with_counter")));
        items.add(new ODrawerItem(KEY)
                .setTitle("Intent Call")
                .setIcon(R.drawable.ic_action_action_done_all)
                .setInstance(OdooLogin.class)
                .setExtra(getData("intent_call")));
        return items;
    }

    public Bundle getData(String val) {
        Bundle data = new Bundle();
        data.putString("my_key", val);
        return data;
    }
}
