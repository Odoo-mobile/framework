package com.openerp.base.login;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
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
		getActivity().setTitle("Configuration");
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
		getActivity().getActionBar().setHomeButtonEnabled(false);

		generateLayout();

		return rootView;
	}

	@Override
	public Object databaseHelper(Context context) {
		return null;
	}

	private void generateLayout() {

		LinearLayout layout = (LinearLayout) rootView
				.findViewById(R.id.layoutLoginConfig);
		SyncWizardValues syncValues = new SyncWizardValues();
		List<SyncValue> syncValuesList = syncValues.syncValues();
		checkbox = new CheckBox[syncValuesList.size()];
		rdoGroups = new RadioGroup[syncValuesList.size()];
		TextView[] txvTitles = new TextView[syncValuesList.size()];
		int i = 0;
		int id = 1;
		for (SyncValue value : syncValuesList) {
			if (!value.getIsGroup()) {
				if (value.getType() == SyncValue.Type.CHECKBOX) {
					checkbox[i] = new CheckBox(scope.context());
					checkbox[i].setId(id);
					checkbox[i].setText(value.getTitle());
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
				txvTitles[i].setTypeface(null, Typeface.BOLD);
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
					scope.context().setAutoSync(
							authorities.get(chkBox.getId() + "").toString(),
							chkBox.isChecked());
				}
			}
			for (RadioGroup rdoGrp : rdoGroups) {
				if (rdoGrp != null) {
					for (int i = 0; i < rdoGrp.getChildCount(); i++) {
						RadioButton rdoBtn = (RadioButton) rdoGrp.getChildAt(i);
						SharedPreferences settings = PreferenceManager
								.getDefaultSharedPreferences(scope.context());
						Editor editor = settings.edit();
						if (rdoBtn.getText().equals("Local Contacts")
								&& rdoBtn.isChecked()) {
							editor.putBoolean("local_contact_sync", true);
							editor.putBoolean("server_contact_sync", false);
						}
						if (rdoBtn.getText().equals("All Contacts")
								&& rdoBtn.isChecked()) {
							editor.putBoolean("server_contact_sync", true);
							editor.putBoolean("local_contact_sync", false);
						}
						editor.commit();
						scope.context().setAutoSync(
								authorities.get(rdoBtn.getId() + ""),
								rdoBtn.isChecked());
					}
				}
			}
			Intent intent = getActivity().getIntent();
			intent.putExtra("create_new_account", false);
			getActivity().finish();
			getActivity().startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		return null;
	}

}
