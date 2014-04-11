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
package com.openerp.base.login;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.openerp.MainActivity;
import com.openerp.R;
import com.openerp.config.SyncWizardValues;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.SyncValue;
import com.openerp.util.drawer.DrawerItem;

public class SyncWizard extends BaseFragment {

	View rootView = null;
	MainActivity context = null;
	CheckBox checkbox[] = null;
	RadioGroup[] rdoGroups = null;
	HashMap<String, String> authorities = new HashMap<String, String>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		context = (MainActivity) getActivity();
		scope = new AppScope(this);
		rootView = inflater.inflate(R.layout.fragment_sync_wizard, container,
				false);
		getActivity().setTitle(R.string.title_configuration);
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
		getActivity().getActionBar().setHomeButtonEnabled(false);

		generateLayout();

		return rootView;
	}

	private void generateLayout() {

		LinearLayout layout = (LinearLayout) rootView
				.findViewById(R.id.layoutLoginConfig);
		SyncWizardValues syncValues = new SyncWizardValues();
		List<SyncValue> syncValuesList = syncValues.syncValues();
		if (syncValuesList.size() == 0) {
			getActivity().finish();
			getActivity().startActivity(getActivity().getIntent());
		}
		checkbox = new CheckBox[syncValuesList.size()];
		rdoGroups = new RadioGroup[syncValuesList.size()];
		TextView[] txvTitles = new TextView[syncValuesList.size()];
		int i = 0;
		int id = 1;
		Typeface tf_light = Typeface.create("sans-serif-light", 0);
		Typeface tf_bold = Typeface.create("sans-serif-condensed", 0);
		for (SyncValue value : syncValuesList) {
			if (!value.getIsGroup()) {
				if (value.getType() == SyncValue.Type.CHECKBOX) {
					checkbox[i] = new CheckBox(scope.context());
					checkbox[i].setId(id);
					checkbox[i].setText(value.getTitle());
					checkbox[i].setTypeface(tf_light);
					layout.addView(checkbox[i]);
				} else {
					rdoGroups[i] = new RadioGroup(scope.context());
					rdoGroups[i].setId(i + 50);
					RadioButton[] rdoButtons = new RadioButton[value
							.getRadioGroups().size()];
					int mId = 1;
					int j = 0;
					for (SyncValue rdoVal : value.getRadioGroups()) {
						rdoButtons[j] = new RadioButton(scope.context());
						rdoButtons[j].setId(mId);
						rdoButtons[j].setText(rdoVal.getTitle());
						rdoButtons[j].setTypeface(tf_light);
						rdoGroups[i].addView(rdoButtons[j]);
						mId++;
						j++;
					}
					layout.addView(rdoGroups[i]);
				}
				authorities.put(id + "", value.getAuthority());
				i++;
				id++;
			} else {
				txvTitles[i] = new TextView(scope.context());
				txvTitles[i].setId(id);
				txvTitles[i].setText(value.getTitle());
				txvTitles[i].setAllCaps(true);
				txvTitles[i].setPadding(0, 5, 0, 3);
				txvTitles[i].setTypeface(tf_bold);
				layout.addView(txvTitles[i]);
				View lineView = new View(scope.context());
				lineView.setBackgroundColor(Color.parseColor("#BEBEBE"));
				lineView.setLayoutParams(new LayoutParams(
						LayoutParams.MATCH_PARENT, 1));
				layout.addView(lineView);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateOptionsMenu(android.view.Menu,
	 * android.view.MenuInflater)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_fragment_sync_wizard, menu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onOptionsItemSelected(android.view.MenuItem
	 * )
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection

		switch (item.getItemId()) {
		case R.id.menu_start_application:
			for (CheckBox chkBox : checkbox) {
				if (chkBox != null) {
					String authority = authorities.get(chkBox.getId() + "")
							.toString();
					scope.main().setAutoSync(authority, chkBox.isChecked());
					scope.main().cancelSync(authority);
				}
			}
			for (RadioGroup rdoGrp : rdoGroups) {
				if (rdoGrp != null) {
					for (int i = 0; i < rdoGrp.getChildCount(); i++) {
						RadioButton rdoBtn = (RadioButton) rdoGrp.getChildAt(i);
						SharedPreferences settings = PreferenceManager
								.getDefaultSharedPreferences(scope.context());
						Editor editor = settings.edit();
						//TODO: store preference setting for your options.
						editor.commit();
						String authority = authorities.get(rdoBtn.getId() + "");
						scope.main().setAutoSync(authority, rdoBtn.isChecked());
						scope.main().cancelSync(authority);
					}
				}
			}
			getActivity().finish();
			getActivity().startActivity(getActivity().getIntent());
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public Object databaseHelper(Context context) {
		return null;
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		return null;
	}

}
