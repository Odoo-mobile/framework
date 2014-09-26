package com.odoo.base.login_signup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLPeerUnverifiedException;

import odoo.OVersionException;
import odoo.Odoo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.odoo.App;
import com.odoo.R;
import com.odoo.auth.OdooAccountManager;
import com.odoo.orm.OdooHelper;
import com.odoo.support.AppScope;
import com.odoo.support.OUser;
import com.odoo.support.OdooServerConnection;
import com.odoo.support.fragment.BaseFragment;
import com.odoo.support.fragment.FragmentListener;
import com.odoo.util.OControls;
import com.odoo.util.drawer.DrawerItem;

public class LoginSignup extends BaseFragment implements OnClickListener,
		OnFocusChangeListener {

	View mView = null;
	Boolean mSelfHosted = false;
	EditText edtHostedURL, edtUsername, edtPassword;
	OdooURLTest mOdooURLTest = null;
	Spinner dbListSpinner = null;
	List<String> mDBList = new ArrayList<String>();
	Boolean mSSLForceConnect = false;
	String mServerURL = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		showActionBar(false);
		scope = new AppScope(this);
		scope.main().lockDrawer(true);
		mView = inflater.inflate(R.layout.base_login_signup_layout, container,
				false);
		mView.findViewById(R.id.forgot_password).setOnClickListener(this);
		mView.findViewById(R.id.create_account).setOnClickListener(this);
		init();
		return mView;
	}

	private void init() {
		initControls();
	}

	private void initControls() {
		mView.findViewById(R.id.txvAddSelfHosted).setOnClickListener(this);
		mView.findViewById(R.id.btnLogin).setOnClickListener(this);
		edtUsername = (EditText) mView.findViewById(R.id.edtUserName);
		edtUsername.setOnFocusChangeListener(this);
		edtPassword = (EditText) mView.findViewById(R.id.edtPassword);

	}

	@Override
	public Object databaseHelper(Context context) {
		return null;
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		return null;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.txvAddSelfHosted:
			if (mSelfHosted) {
				mSelfHosted = false;
				OControls.setGone(mView, R.id.layoutSelfHosted);
				OControls.setText(mView, R.id.txvAddSelfHosted,
						"Add self-hosted URL");
				OControls.setGone(mView, R.id.layoutBorderDB);
				OControls.setGone(mView, R.id.layoutDatabase);
			} else {
				mSelfHosted = true;
				OControls.setVisible(mView, R.id.layoutSelfHosted, true,
						getActivity());
				OControls.setText(mView, R.id.txvAddSelfHosted,
						"Login with odoo.com");
				edtHostedURL = (EditText) mView
						.findViewById(R.id.edtSelfHostedURL);
				edtHostedURL.setOnFocusChangeListener(this);
				edtHostedURL.requestFocus();
			}
			break;
		case R.id.btnLogin:
			login();
			break;
		case R.id.forgot_password:
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("https://www.odoo.com/web/reset_password"));
			startActivity(intent);
			break;
		case R.id.create_account:
			intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("https://accounts.odoo.com/web/signup"));
			startActivity(intent);
			break;
		}

	}

	private void login() {
		Log.v("LoginSignup", "login()");
		if (mSelfHosted) {
			edtHostedURL.setError(null);
			if (TextUtils.isEmpty(edtHostedURL.getText())) {
				edtHostedURL.setError("Provide your server URL");
				edtHostedURL.requestFocus();
				return;
			} else {
				mServerURL = createServerURL(edtHostedURL.getText().toString());
			}
		} else {
			mServerURL = "https://accounts.odoo.com";
		}
		edtUsername.setError(null);
		edtPassword.setError(null);
		if (TextUtils.isEmpty(edtUsername.getText())) {
			edtUsername.setError("Provide username or email");
			edtUsername.requestFocus();
			return;
		}
		if (TextUtils.isEmpty(edtPassword.getText())) {
			edtPassword.setError("Provide password");
			edtPassword.requestFocus();
			return;
		}
		if (mDBList.size() > 0) {
			int db_index = (dbListSpinner == null) ? 0 : dbListSpinner
					.getSelectedItemPosition() - 1;
			if (dbListSpinner != null && db_index + 1 <= 0) {
				Toast.makeText(
						getActivity(),
						getResources()
								.getString(R.string.toast_select_database),
						Toast.LENGTH_LONG).show();
				return;
			}
			String database_name = mDBList.get(db_index);
			Bundle bundle = new Bundle();
			bundle.putString("server_url", mServerURL);
			bundle.putString("username", edtUsername.getText().toString());
			bundle.putString("password", edtPassword.getText().toString());
			bundle.putString("database", database_name);
			bundle.putBoolean("force_connect", mSSLForceConnect);
			LoginProcess loginProcess = new LoginProcess(bundle);
			loginProcess.execute();
		} else {
			if (mOdooURLTest != null)
				mOdooURLTest.cancel(true);
			if (edtHostedURL == null || !mSelfHosted) {
				mOdooURLTest = new OdooURLTest(mServerURL, false, true);
				mOdooURLTest.execute();
			} else {
				if (!TextUtils.isEmpty(edtHostedURL.getText())) {
					mServerURL = createServerURL(edtHostedURL.getText()
							.toString());
					mOdooURLTest = new OdooURLTest(mServerURL, false, true);
					mOdooURLTest.execute();
				}
			}
		}
	}

	class LoginProcess extends AsyncTask<Void, Void, Boolean> {

		Bundle userData = null;
		OUser mUser = null;
		Odoo mOdooInstance = null;
		App app = null;

		public LoginProcess(Bundle bundle) {
			userData = bundle;
			app = (App) scope.context().getApplicationContext();
			app.setOdooInstance(null);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			OControls.setGone(mView, R.id.controls);
			OControls.setVisible(mView, R.id.loginProgress);
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			OdooHelper odoo = new OdooHelper(getActivity(),
					userData.getBoolean("force_connect"));
			mUser = odoo.login(userData.getString("username"),
					userData.getString("password"),
					userData.getString("database"),
					userData.getString("server_url"));
			if (mUser != null) {
				mOdooInstance = app.getOdoo();
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			super.onPostExecute(success);
			if (success) {
				if (OdooAccountManager.fetchAllAccounts(getActivity()) != null) {
					if (OdooAccountManager.getAccount(getActivity(),
							mUser.getAndroidName()) == null) {
						if (OdooAccountManager.isAnyUser(getActivity())) {
							OdooAccountManager.logoutUser(
									getActivity(),
									OdooAccountManager.currentUser(
											getActivity()).getAndroidName());
						}
					} else {
						OControls.setGone(mView, R.id.loginProgress);
						OControls.setVisible(mView, R.id.controls);
						edtUsername.setError(getResources().getString(
								R.string.toast_user_already_exists));
						return;
					}
				}
				app.setOdooInstance(mOdooInstance);
				AccountCreate account_create = new AccountCreate();
				Bundle args = mUser.getAsBundle();
				args.putBoolean("self_hosted", mSelfHosted);
				account_create.setArguments(args);

				FragmentListener frag = (FragmentListener) getActivity();
				frag.startMainFragment(account_create, false);
			} else {
				OControls.setGone(mView, R.id.loginProgress);
				OControls.setVisible(mView, R.id.controls);
				edtPassword.setError(getResources().getString(
						R.string.toast_invalid_username_password));
			}
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (v.getId() == R.id.edtSelfHostedURL && !hasFocus) {
			if (!TextUtils.isEmpty(edtHostedURL.getText())) {
				if (mOdooURLTest != null)
					mOdooURLTest.cancel(true);
				mServerURL = createServerURL(edtHostedURL.getText().toString());
				mOdooURLTest = new OdooURLTest(mServerURL, false, false);
				mOdooURLTest.execute();
			}
		}
		if (v.getId() == R.id.edtUserName && !hasFocus) {
			if (edtHostedURL == null) {
				if (mOdooURLTest != null)
					mOdooURLTest.cancel(true);
				mOdooURLTest = new OdooURLTest(mServerURL, false, false);
				mOdooURLTest.execute();
			}
		}
	}

	class OdooURLTest extends AsyncTask<Void, Void, Boolean> {

		Boolean mForceConnect = false;
		Boolean mAutoLogin = false;
		OdooServerConnection odooConnect = null;
		String errorMsg = "";
		boolean mSSLError = false;
		String odooServerURL = "";

		public OdooURLTest(String serverURL, boolean forceConnect,
				boolean autoLogin) {
			mForceConnect = forceConnect;
			odooServerURL = serverURL;
			mAutoLogin = autoLogin;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			OControls.setVisible(mView, R.id.serverURLCheckProgress);
			OControls.setGone(mView, R.id.imgValidURL);
			OControls.setGone(mView, R.id.layoutBorderDB);
			OControls.setGone(mView, R.id.layoutDatabase);
			if (mAutoLogin) {
				OControls.setGone(mView, R.id.controls);
				OControls.setVisible(mView, R.id.loginProgress);
			}
			odooConnect = new OdooServerConnection(mForceConnect);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			boolean flag = false;
			try {
				flag = odooConnect.testConnection(getActivity(), odooServerURL);
				if (!flag) {
					errorMsg = getResources().getString(
							R.string.toast_unable_to_reach_odoo_server);
				}
			} catch (SSLPeerUnverifiedException ssl) {
				flag = false;
				mSSLError = true;
				errorMsg = ssl.getMessage();
			} catch (OVersionException e) {
				flag = false;
				errorMsg = e.getMessage();
			}

			return flag;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			super.onPostExecute(success);
			OControls.setGone(mView, R.id.serverURLCheckProgress);
			if (success) {
				OControls.setVisible(mView, R.id.imgValidURL);
				mOdooURLTest.cancel(true);
				mOdooURLTest = null;
				String[] databases = odooConnect.getDatabases();
				if (databases.length > 0) {
					initDatabaseSpinner(databases);
					mSSLForceConnect = mForceConnect;
					if (mAutoLogin) {
						OControls.setGone(mView, R.id.loginProgress);
						OControls.setVisible(mView, R.id.controls);
						login();
					}
				} else {
					Toast.makeText(
							getActivity(),
							getResources().getString(
									R.string.toast_no_database_found),
							Toast.LENGTH_LONG).show();
				}
			} else {
				mOdooURLTest.cancel(true);
				mOdooURLTest = null;
				if (mSSLError) {
					showForceConnectDialog(errorMsg);
				} else {
					if (edtHostedURL != null) {
						edtHostedURL.setError(errorMsg);
						edtHostedURL.requestFocus();
					}
				}
			}

		}

	}

	private void initDatabaseSpinner(String[] dbs) {
		if (dbs.length > 1) {
			OControls.setVisible(mView, R.id.layoutBorderDB, true,
					getActivity());
			OControls.setVisible(mView, R.id.layoutDatabase, true,
					getActivity());
			dbListSpinner = (Spinner) mView
					.findViewById(R.id.spinnerDatabaseList);
		}
		List<String> dbLists = new ArrayList<String>();
		dbLists.addAll(Arrays.asList(dbs));
		mDBList.clear();
		mDBList.addAll(dbLists);
		if (dbs.length > 1)
			loadDatabaseList(dbLists);
	}

	private void loadDatabaseList(List<String> dbList) {
		try {
			dbList.add(0,
					getActivity().getString(R.string.login_select_database));
			ArrayAdapter<String> dbAdapter = new ArrayAdapter<String>(
					getActivity(), R.layout.spinner_custom_layout, dbList);
			dbAdapter.setDropDownViewResource(R.layout.spinner_custom_layout);
			dbListSpinner.setAdapter(dbAdapter);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private String createServerURL(String server_url) {
		StringBuffer serverURL = new StringBuffer();

		if (!server_url.contains("http://") && !server_url.contains("https://")) {
			String http_https = "http://";
			serverURL.append(http_https);
		}

		serverURL.append(server_url);
		return serverURL.toString();
	}

	private void showForceConnectDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setIcon(R.drawable.ic_action_alerts_and_states_warning);
		builder.setTitle(R.string.title_ssl_warning);
		builder.setMessage(R.string.untrusted_ssl_warning);
		builder.setPositiveButton(R.string.label_process_anyway,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (mOdooURLTest != null) {
							mOdooURLTest.cancel(true);
							mOdooURLTest = null;
						}
						mOdooURLTest = new OdooURLTest(mServerURL, true, false);
						mOdooURLTest.execute((Void) null);
					}
				});
		builder.setNegativeButton(R.string.label_cancel, null);
		builder.show();
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().getActionBar().hide();
	}
}
