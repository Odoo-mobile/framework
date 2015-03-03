package com.odoo.core.account;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.odoo.OdooActivity;
import com.odoo.base.addons.res.ResCompany;
import com.odoo.core.auth.OdooAccountManager;
import com.odoo.core.auth.OdooAuthenticator;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.support.OUser;
import com.odoo.core.support.OdooInstancesSelectorDialog;
import com.odoo.core.support.OdooLoginHelper;
import com.odoo.core.support.OdooServerTester;
import com.odoo.core.support.OdooUserLoginSelectorDialog;
import com.odoo.core.utils.IntentUtils;
import com.odoo.core.utils.OAlertDialog;
import com.odoo.core.utils.OResource;
import com.odoo.R;
import com.odoo.datas.OConstants;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLPeerUnverifiedException;

import odoo.OdooAccountExpireException;
import odoo.OdooInstance;

public class OdooLogin extends ActionBarActivity implements View.OnClickListener, View.OnFocusChangeListener, OdooInstancesSelectorDialog.OnInstanceSelectListener, OdooUserLoginSelectorDialog.IUserLoginSelectListener {

    private EditText edtUsername, edtPassword, edtSelfHosted;
    private Boolean mCreateAccountRequest = false;
    private Boolean mSelfHostedURL = false;
    private Boolean mForceConnect = false;
    private Boolean mConnectedToServer = false;
    private Boolean mAutoLogin = false;
    private Boolean mRequestedForAccount = false;
    private OdooURLTester odooURLTester = null;
    private LoginProcess loginProcess = null;
    private AccountCreater accountCreator = null;
    private OdooServerTester mServerTester = null;
    private InstanceGetter instanceGetter = null;
    private OdooLoginHelper loginHelper = null;
    private Spinner databaseSpinner = null;
    private List<String> databases = new ArrayList<String>();
    private TextView mLoginProcessStatus = null;
    private TextView mTermsCondition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_login);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(OdooAuthenticator.KEY_NEW_ACCOUNT_REQUEST))
                mCreateAccountRequest = true;
            if (extras.containsKey(OdooActivity.KEY_ACCOUNT_REQUEST)) {
                mRequestedForAccount = true;
                setResult(RESULT_CANCELED);
            }
        }
        if (!mCreateAccountRequest) {
            if (OdooAccountManager.anyActiveUser(this)) {
                startOdooActivity();
            } else if (OdooAccountManager.hasAnyAccount(this)) {
                OdooUserLoginSelectorDialog dialog = new OdooUserLoginSelectorDialog(this);
                dialog.setUserLoginSelectListener(this);
                dialog.show();
                return;
            }
        }
        init();
    }

    private void init() {
        loginHelper = new OdooLoginHelper(this);
        mServerTester = new OdooServerTester(this);
        mLoginProcessStatus = (TextView) findViewById(R.id.login_process_status);
        mTermsCondition = (TextView) findViewById(R.id.termsCondition);
        mTermsCondition.setMovementMethod(LinkMovementMethod.getInstance());
        findViewById(R.id.btnLogin).setOnClickListener(this);
        findViewById(R.id.forgot_password).setOnClickListener(this);
        findViewById(R.id.create_account).setOnClickListener(this);
        findViewById(R.id.txvAddSelfHosted).setOnClickListener(this);
        edtSelfHosted = (EditText) findViewById(R.id.edtSelfHostedURL);
    }

    private void startOdooActivity() {
        startActivity(new Intent(this, OdooActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_base_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txvAddSelfHosted:
                toggleSelfHostedURL();
                break;
            case R.id.btnLogin:
                loginUser();
                break;
            case R.id.forgot_password:
                IntentUtils.openURLInBrowser(this, OConstants.URL_ODOO_RESET_PASSWORD);
                break;
            case R.id.create_account:
                IntentUtils.openURLInBrowser(this, OConstants.URL_ODOO_SIGN_UP);
                break;
        }
    }

    private void toggleSelfHostedURL() {
        TextView txvAddSelfHosted = (TextView) findViewById(R.id.txvAddSelfHosted);
        if (!mSelfHostedURL) {
            mSelfHostedURL = true;
            findViewById(R.id.layoutSelfHosted).setVisibility(View.VISIBLE);
            edtSelfHosted.setOnFocusChangeListener(this);
            edtSelfHosted.requestFocus();
            txvAddSelfHosted.setText(R.string.label_login_with_odoo);
        } else {
            findViewById(R.id.layoutBorderDB).setVisibility(View.GONE);
            findViewById(R.id.layoutDatabase).setVisibility(View.GONE);
            findViewById(R.id.layoutSelfHosted).setVisibility(View.GONE);
            mSelfHostedURL = false;
            txvAddSelfHosted.setText(R.string.label_add_self_hosted_url);
            edtSelfHosted.setText("");
        }
    }

    @Override
    public void onFocusChange(final View v, final boolean hasFocus) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (odooURLTester != null) {
                    odooURLTester.cancel(true);
                }
                if (mSelfHostedURL && v.getId() == R.id.edtSelfHostedURL && !hasFocus) {
                    if (!TextUtils.isEmpty(edtSelfHosted.getText())
                            && validateURL(edtSelfHosted.getText().toString())) {
                        String test_url = createServerURL(edtSelfHosted.getText().toString());
                        odooURLTester = new OdooURLTester();
                        odooURLTester.execute(test_url);
                    }
                }
            }
        }, 500);
    }

    private boolean validateURL(String url) {
        return (url.contains("."));
    }

    private String createServerURL(String server_url) {
        StringBuilder serverURL = new StringBuilder();
        if (!server_url.contains("http://") && !server_url.contains("https://")) {
            serverURL.append("http://");
        }
        serverURL.append(server_url);
        return serverURL.toString();
    }

    private void showForceConnectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_ssl_warning);
        builder.setMessage(R.string.untrusted_ssl_warning);
        builder.setPositiveButton(R.string.label_process_anyway,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (odooURLTester != null) {
                            odooURLTester.cancel(true);
                            odooURLTester = null;
                        }
                        mForceConnect = true;
                        odooURLTester = new OdooURLTester();
                        odooURLTester.execute(createServerURL(edtSelfHosted.getText().toString()));
                    }
                });
        builder.setNegativeButton(R.string.label_cancel, null);
        builder.show();
    }

    // User Login
    private void loginUser() {
        String serverURL = createServerURL((mSelfHostedURL) ? edtSelfHosted.getText().toString() : OConstants.URL_ODOO_ACCOUNTS);
        String databaseName = null;
        edtUsername = (EditText) findViewById(R.id.edtUserName);
        edtPassword = (EditText) findViewById(R.id.edtPassword);

        if (mSelfHostedURL) {
            edtSelfHosted.setError(null);
            if (TextUtils.isEmpty(edtSelfHosted.getText())) {
                edtSelfHosted.setError(OResource.string(this, R.string.error_provide_server_url));
                edtSelfHosted.requestFocus();
                return;
            }

            if (databaseSpinner != null && databases.size() > 1 && databaseSpinner.getSelectedItemPosition() == 0) {
                Toast.makeText(this, OResource.string(this, R.string.label_select_database), Toast.LENGTH_LONG).show();
                return;
            }

        }
        edtUsername.setError(null);
        edtPassword.setError(null);
        if (TextUtils.isEmpty(edtUsername.getText())) {
            edtUsername.setError(OResource.string(this, R.string.error_provide_username));
            edtUsername.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(edtPassword.getText())) {
            edtPassword.setError(OResource.string(this, R.string.error_provide_password));
            edtPassword.requestFocus();
            return;
        }

        if (mConnectedToServer) {
            databaseName = databases.get(0);
            if (databaseSpinner != null) {
                databaseName = databases.get(databaseSpinner.getSelectedItemPosition());
            }
            mAutoLogin = false;
            if (loginProcess != null) {
                loginProcess.cancel(true);
            }
            loginProcess = new LoginProcess();
            loginProcess.execute(databaseName, serverURL);

        } else {
            if (odooURLTester != null)
                odooURLTester.cancel(true);
            mAutoLogin = true;
            odooURLTester = new OdooURLTester();
            odooURLTester.execute(serverURL);
        }
    }

    private void showDatabases() {
        if (databases.size() > 1) {
            findViewById(R.id.layoutBorderDB).setVisibility(View.VISIBLE);
            findViewById(R.id.layoutDatabase).setVisibility(View.VISIBLE);
            databaseSpinner = (Spinner) findViewById(R.id.spinnerDatabaseList);
            databases.add(0, OResource.string(this, R.string.label_select_database));
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, databases);
            databaseSpinner.setAdapter(adapter);
        } else {
            databaseSpinner = null;
            findViewById(R.id.layoutBorderDB).setVisibility(View.GONE);
            findViewById(R.id.layoutDatabase).setVisibility(View.GONE);
        }
    }

    @Override
    public void instanceSelected(OdooInstance instance, OUser user) {
        OUser userData = user;
        if (!instance.getInstanceUrl().equals(OConstants.URL_ODOO)) {
            if (loginProcess != null)
                loginProcess.cancel(true);
            loginProcess = new LoginProcess(instance, user);
            loginProcess.execute();
            return;
        }
        accountCreator = new AccountCreater();
        accountCreator.execute(userData);
    }

    @Override
    public void canceledInstanceSelect() {
        findViewById(R.id.controls).setVisibility(View.VISIBLE);
        findViewById(R.id.login_progress).setVisibility(View.GONE);
    }

    @Override
    public void onUserSelected(OUser user) {
        OdooAccountManager.login(this, user.getAndroidName());
        startOdooActivity();
    }

    @Override
    public void onNewAccountRequest() {
        init();
    }

    @Override
    public void onCancelSelect() {
        finish();
    }

    private class LoginProcess extends AsyncTask<String, Void, OUser> {

        private OdooInstance mInstance;
        private OUser mUser;
        private String mExpireMessage = null;

        public LoginProcess() {

        }

        public LoginProcess(OdooInstance instance, OUser user) {
            mInstance = instance;
            mUser = user;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.controls).setVisibility(View.GONE);
            findViewById(R.id.login_progress).setVisibility(View.VISIBLE);
            if (mInstance != null && mUser != null)
                mLoginProcessStatus.setText(OResource.string(OdooLogin.this, R.string.status_logging_in_with_instance));
            else
                mLoginProcessStatus.setText(OResource.string(OdooLogin.this, R.string.status_logging_in));
        }

        @Override
        protected OUser doInBackground(String... params) {
            try {
                if (mInstance == null && mUser == null) {
                    String username = edtUsername.getText().toString();
                    String password = edtPassword.getText().toString();
                    return loginHelper.login(username, password, params[0], params[1], mForceConnect);
                } else {
                    mSelfHostedURL = true;
                    return loginHelper.instanceLogin(mInstance, mUser);
                }
            } catch (OdooAccountExpireException expired) {
                mExpireMessage = expired.getMessage();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(OUser user) {
            super.onPostExecute(user);
            edtUsername.setError(null);
            if (user == null) {
                findViewById(R.id.controls).setVisibility(View.VISIBLE);
                findViewById(R.id.login_progress).setVisibility(View.GONE);
                if (mExpireMessage != null) {
                    mSelfHostedURL = false;
                    OAlertDialog dialog = new OAlertDialog(OdooLogin.this);
                    dialog.setTitle(OResource.string(OdooLogin.this, R.string.title_instance_expired));
                    dialog.setMessage(mExpireMessage);
                    dialog.show();
                } else {
                    edtUsername.setError(OResource.string(OdooLogin.this, R.string.error_invalid_username_or_password));
                }
            } else {
                mLoginProcessStatus.setText(OResource.string(OdooLogin.this, R.string.status_login_success));
                if (!mSelfHostedURL) {
                    instanceGetter = new InstanceGetter();
                    instanceGetter.execute(user);
                } else {
                    accountCreator = new AccountCreater();
                    accountCreator.execute(user);
                }
            }
        }
    }

    private class InstanceGetter extends AsyncTask<OUser, Void, List<OdooInstance>> {

        private OUser mUser;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoginProcessStatus.setText(OResource.string(OdooLogin.this, R.string.status_getting_instances));
        }

        @Override
        protected List<OdooInstance> doInBackground(OUser... params) {
            mUser = params[0];
            return loginHelper.getOdooInstances(mUser);
        }

        @Override
        protected void onPostExecute(List<OdooInstance> odooInstances) {
            super.onPostExecute(odooInstances);
            if (odooInstances.size() > 1) {
                OdooInstancesSelectorDialog instancesSelectorDialog = new OdooInstancesSelectorDialog(OdooLogin.this, mUser);
                instancesSelectorDialog.setInstances(odooInstances);
                instancesSelectorDialog.setOnInstanceSelectListener(OdooLogin.this);
                instancesSelectorDialog.showDialog();
            } else {
                // Login to default odoo instance (www.odoo.com)
                accountCreator = new AccountCreater();
                accountCreator.execute(mUser);
            }
        }
    }

    private class AccountCreater extends AsyncTask<OUser, Void, Boolean> {

        private OUser mUser;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoginProcessStatus.setText(OResource.string(OdooLogin.this, R.string.status_creating_account));
        }

        @Override
        protected Boolean doInBackground(OUser... params) {
            mUser = params[0];
            if (OdooAccountManager.createAccount(OdooLogin.this, params[0])) {
                mUser = OdooAccountManager.getDetails(OdooLogin.this, mUser.getAndroidName());
                try {
                    // Syncing company details
                    ODataRow company_details = new ODataRow();
                    company_details.put("id", mUser.getCompany_id());
                    ResCompany company = new ResCompany(OdooLogin.this, mUser);
                    company.quickCreateRecord(company_details);

                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            mLoginProcessStatus.setText(OResource.string(OdooLogin.this, R.string.status_redirecting));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!mRequestedForAccount)
                        startOdooActivity();
                    else {
                        Intent intent = new Intent();
                        intent.putExtra(OdooActivity.KEY_NEW_USER_NAME, mUser.getAndroidName());
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            }, 1500);
        }
    }

    private class OdooURLTester extends AsyncTask<String, Void, Boolean> {

        private Boolean mRequiredForceConnect = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.v("OdooURLTester", "Connecting to Server");
            edtSelfHosted.setError(null);
            if (mAutoLogin) {
                findViewById(R.id.controls).setVisibility(View.GONE);
                findViewById(R.id.login_progress).setVisibility(View.VISIBLE);
                mLoginProcessStatus.setText(OResource.string(OdooLogin.this, R.string.status_connecting_to_server));
            }
            findViewById(R.id.imgValidURL).setVisibility(View.GONE);
            findViewById(R.id.serverURLCheckProgress).setVisibility(View.VISIBLE);
            findViewById(R.id.layoutBorderDB).setVisibility(View.GONE);
            findViewById(R.id.layoutDatabase).setVisibility(View.GONE);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                return mServerTester.testConnection(params[0], mForceConnect);
            } catch (SSLPeerUnverifiedException peer) {
                mRequiredForceConnect = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            findViewById(R.id.serverURLCheckProgress).setVisibility(View.GONE);
            edtSelfHosted.setError(null);
            if (success) {
                // Connected to server
                Log.v("OdooURLTester", "Connected to server.");
                mLoginProcessStatus.setText(OResource.string(OdooLogin.this, R.string.status_connected_to_server));
                databases.clear();
                databases.addAll(mServerTester.getDatabases());
                showDatabases();
                mConnectedToServer = true;
                findViewById(R.id.imgValidURL).setVisibility(View.VISIBLE);
                if (mAutoLogin) {
                    loginUser();
                }
            } else if (mRequiredForceConnect) {
                showForceConnectDialog();
            } else {
                edtSelfHosted.setError(OResource.string(OdooLogin.this, R.string.error_invalid_odoo_url));
                edtSelfHosted.requestFocus();
            }
        }
    }
}