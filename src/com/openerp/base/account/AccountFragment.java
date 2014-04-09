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

import javax.net.ssl.SSLPeerUnverifiedException;

import openerp.OEVersionException;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.openerp.R;
import com.openerp.base.login.Login;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.OEDialog;
import com.openerp.support.OpenERPServerConnection;
import com.openerp.support.fragment.FragmentListener;
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
	@SuppressLint("DefaultLocale")
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
		getActivity().setTitle(R.string.title_setup_account);
		chkSecureConnection = (CheckBox) rootView
				.findViewById(R.id.chkIsSecureConnection);
		edtServerUrl = (EditText) rootView.findViewById(R.id.edtServerURL);
		chkSecureConnection.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				String serverUrl = edtServerUrl.getText().toString()
						.toLowerCase();
				if (chkSecureConnection.isChecked()) {
					serverUrl = serverUrl.replace("http://", "");
					serverUrl = serverUrl.replace("https://", "");
					serverUrl = "https://" + serverUrl;
				} else {
					serverUrl = serverUrl.replace("https://", "");
					serverUrl = serverUrl.replace("http://", "");
				}
				edtServerUrl.setText(serverUrl);
				edtServerUrl.setSelection(edtServerUrl.length());
			}
		});

		edtServerUrl.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
						|| (actionId == EditorInfo.IME_ACTION_DONE)) {
					goNext();
				}
				return false;
			}
		});
		return rootView;
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
			goNext();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void goNext() {
		StringBuffer serverURL = new StringBuffer();
		edtServerUrl.setError(null);
		if (TextUtils.isEmpty(edtServerUrl.getText())) {
			edtServerUrl.setError(getResources().getString(
					R.string.toast_provide_server_url));
		} else {

			if (!edtServerUrl.getText().toString().contains("http://")
					&& !edtServerUrl.getText().toString().contains("https://")) {
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
	}

	/**
	 * The Class ConnectToServer.
	 */
	public class ConnectToServer extends AsyncTask<Void, Void, Boolean> {

		/** The pdialog. */
		OEDialog pdialog = null;

		/** The error msg. */
		String errorMsg = "";

		boolean mSSLError = false;

		public ConnectToServer() {
			pdialog = new OEDialog(scope.context(), false, getResources()
					.getString(R.string.title_connecting));
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
					errorMsg = getResources().getString(
							R.string.toast_unable_to_reach_openerp_server);
				}
			} catch (SSLPeerUnverifiedException ssl) {
				flag = false;
				mSSLError = true;
				errorMsg = ssl.getMessage();
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
				loginFragment.setArguments(bundle);
				FragmentListener fragment = (FragmentListener) getActivity();
				fragment.startMainFragment(loginFragment, true);
				serverConnectASync.cancel(true);
				serverConnectASync = null;

			} else {
				serverConnectASync.cancel(true);
				serverConnectASync = null;
				if (mSSLError) {
					showForceConnectDialog(errorMsg);
				} else {
					Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_LONG)
							.show();
				}
			}
		}

	}

	private void showForceConnectDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setIcon(R.drawable.ic_action_alerts_and_states_warning);
		builder.setTitle(R.string.title_ssl_warning);
		builder.setMessage(R.string.untrusted_ssl_warning);
		builder.setNegativeButton(R.string.label_ok, null);
		builder.show();
	}

	@Override
	public void onStop() {
		super.onStop();
		scope.main().getActionBar().setDisplayHomeAsUpEnabled(true);
		scope.main().getActionBar().setHomeButtonEnabled(true);

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
