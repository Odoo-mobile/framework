package com.openerp.base.login;

import java.util.HashMap;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.openerp.MainActivity;
import com.openerp.R;
import com.openerp.providers.groups.UserGroupsProvider;
import com.openerp.providers.meeting.MeetingProvider;
import com.openerp.providers.message.MessageProvider;
import com.openerp.providers.note.NoteProvider;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.util.drawer.DrawerItem;

public class SyncWizard extends BaseFragment {

	View rootView = null;
	MainActivity context = null;
	CheckBox checkbox[] = null;
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
		SyncAdapterType[] types = ContentResolver.getSyncAdapterTypes();

		LinearLayout layout = (LinearLayout) rootView
				.findViewById(R.id.layoutLoginConfig);
		checkbox = new CheckBox[types.length];
		int i = 0;
		int id = 1;
		// for (SyncAdapterType type : types) {
		// if (type.authority.contains("com.openerp")) {
		// String[] parts = TextUtils.split(type.authority, "\\.");
		// String name = parts[parts.length - 1];
		// checkbox[i] = new CheckBox(scope.context());
		// checkbox[i].setId(id);
		// checkbox[i].setText(name);
		// layout.addView(checkbox[i]);
		// authorities.put(id + "", type.authority);
		// i++;
		// id++;
		// }
		// }
		// messages
		checkbox[i] = new CheckBox(scope.context());
		checkbox[i].setId(id);
		checkbox[i].setText("Messages");
		layout.addView(checkbox[i]);
		authorities.put(id + "", MessageProvider.AUTHORITY);
		i++;
		id++;

		// groups
		checkbox[i] = new CheckBox(scope.context());
		checkbox[i].setId(id);
		checkbox[i].setText("Groups");
		layout.addView(checkbox[i]);
		authorities.put(id + "", UserGroupsProvider.AUTHORITY);
		i++;
		id++;

		// contacts
		checkbox[i] = new CheckBox(scope.context());
		checkbox[i].setId(id);
		checkbox[i].setText("Contacts");
		layout.addView(checkbox[i]);
		authorities.put(id + "", ContactsContract.AUTHORITY);
		i++;
		id++;

		// Notes
		checkbox[i] = new CheckBox(scope.context());
		checkbox[i].setId(id);
		checkbox[i].setText("Notes");
		layout.addView(checkbox[i]);
		authorities.put(id + "", NoteProvider.AUTHORITY);
		i++;
		id++;

		// meeting
		checkbox[i] = new CheckBox(scope.context());
		checkbox[i].setText("Meetings");
		checkbox[i].setId(id);
		layout.addView(checkbox[i]);
		authorities.put(id + "", MeetingProvider.AUTHORITY);
		i++;
		id++;

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
