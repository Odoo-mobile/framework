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
package com.openerp.base.account;

import java.util.List;

import openerp.OEVersionException;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.openerp.R;
import com.openerp.base.login.Login;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.OEDialog;
import com.openerp.support.OpenERPServerConnection;
import com.openerp.util.drawer.DrawerItem;

/**
 * The Class AccountFragment.
 */
public class AccountFragment extends BaseFragment {

	/** The item arr. */
	String[] itemArr = null;

	/** The context. */
	Context context = null;

	/** The m action mode. */
	ActionMode mActionMode;

	/** The open erp server url. */
	String openERPServerURL = "";

	/** The edt server url. */
	EditText edtServerUrl = null;

	/** The server connect a sync. */
	ConnectToServer serverConnectASync = null;

	/** The root view. */
	View rootView = null;

	/** The checkbox secure connection. */
	CheckBox chkSecureConnection = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		this.context = getActivity();
		// Inflate the layout for this fragment
		rootView = inflater
				.inflate(R.layout.fragment_account, container, false);

		scope = new AppScope(this);

		rootView.findViewById(R.id.edtServerURL).requestFocus();
		getActivity().setTitle("Setup Account");
		chkSecureConnection = (CheckBox) rootView
				.findViewById(R.id.chkIsSecureConnection);
		final EditText edtUrl = (EditText) rootView
				.findViewById(R.id.edtServerURL);
		chkSecureConnection.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				String serverUrl = edtUrl.getText().toString().toLowerCase();
				if (chkSecureConnection.isChecked()) {
					serverUrl = serverUrl.replace("http://", "");
					serverUrl = "https://" + serverUrl;
				} else {
					serverUrl = serverUrl.replace("https://", "");
				}
				edtUrl.setText(serverUrl);
				edtUrl.setSelection(edtUrl.length());
			}
		});
		return rootView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openerp.support.FragmentHelper#databaseHelper(android.content.Context
	 * )
	 */
	@Override
	public Object databaseHelper(Context context) {
		return new AccountDBHelper(context);
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
		inflater.inflate(R.menu.menu_fragment_account, menu);
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
		case R.id.menu_account_next:
			StringBuffer serverURL = new StringBuffer();
			edtServerUrl = (EditText) rootView.findViewById(R.id.edtServerURL);
			edtServerUrl.setError(null);
			if (TextUtils.isEmpty(edtServerUrl.getText())) {
				edtServerUrl.setError("Provide Server URL");
			} else {

				if (!edtServerUrl.getText().toString().contains("http://")
						&& !edtServerUrl.getText().toString()
								.contains("https://")) {
					String http_https = "http://";
					if (chkSecureConnection.isChecked()) {
						http_https = "https://";
					}
					serverURL.append(http_https);
				}

				serverURL.append(edtServerUrl.getText());
				this.openERPServerURL = serverURL.toString();
				serverConnectASync = new ConnectToServer();
				serverConnectASync.execute((Void) null);

			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * The Class ConnectToServer.
	 */
	public class ConnectToServer extends AsyncTask<Void, Void, Boolean> {

		/** The pdialog. */
		OEDialog pdialog = null;

		/** The error msg. */
		String errorMsg = "";

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			pdialog = new OEDialog(scope.context(), false, "Connecting...");
			pdialog.show();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Boolean doInBackground(Void... params) {

			try {
				// Simulate network access.
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				return false;
			}

			OpenERPServerConnection oeConnect = new OpenERPServerConnection();

			boolean flag = false;
			try {
				flag = oeConnect
						.testConnection(getActivity(), openERPServerURL);
				if (!flag) {
					errorMsg = "Unable to reach OpenERP 7.0 Server.";
				}
			} catch (OEVersionException e) {
				flag = false;
				errorMsg = e.getMessage();
			}

			return flag;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(final Boolean success) {
			pdialog.hide();
			if (success) {
				// Start New Fragment for Login
				Login loginFragment = new Login();
				Bundle bundle = new Bundle();
				bundle.putString("openERPServerURL", openERPServerURL);
				scope.context().fragmentHandler.setFragmentArguments(bundle);
				scope.context().fragmentHandler.setBackStack(true, null);
				scope.context().fragmentHandler.replaceFragmnet(loginFragment);

				serverConnectASync.cancel(true);
				serverConnectASync = null;

			} else {
				Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_LONG)
						.show();
				serverConnectASync.cancel(true);
				serverConnectASync = null;
			}
		}

	}

	@Override
	public void onStop() {
		super.onStop();
		scope.context().getActionBar().setDisplayHomeAsUpEnabled(true);
		scope.context().getActionBar().setHomeButtonEnabled(true);

	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		return null;
	}

}
