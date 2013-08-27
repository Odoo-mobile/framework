package com.openerp.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.openerp.MainActivity;

public class OpenERPAuthenticator extends AbstractAccountAuthenticator {
    private Context mConetext;

    public OpenERPAuthenticator(Context context) {
	super(context);
	mConetext = context;
	// TODO Auto-generated constructor stub
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response,
	    String accountType, String authTokenType,
	    String[] requiredFeatures, Bundle options)
	    throws NetworkErrorException {
	// TODO Auto-generated method stub

	final Bundle result;
	final Intent intent;

	intent = new Intent(this.mConetext, MainActivity.class);
	result = new Bundle();
	intent.putExtra("create_new_account", true);
	result.putParcelable(AccountManager.KEY_INTENT, intent);

	return result;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response,
	    Account account, Bundle options) throws NetworkErrorException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response,
	    String accountType) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response,
	    Account account, String authTokenType, Bundle options)
	    throws NetworkErrorException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response,
	    Account account, String[] features) throws NetworkErrorException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Bundle getAccountRemovalAllowed(
	    AccountAuthenticatorResponse response, Account account)
	    throws NetworkErrorException {
	Bundle result = super.getAccountRemovalAllowed(response, account);
	if (result != null
		&& result.containsKey(AccountManager.KEY_BOOLEAN_RESULT)
		&& !result.containsKey(AccountManager.KEY_INTENT)) {
	    final boolean removalAllowed = result
		    .getBoolean(AccountManager.KEY_BOOLEAN_RESULT);

	    if (removalAllowed) {
		// Do removal stuff here
		Log.e("TODO : Do Database Removal Stuff Here For Account => ",
			account.name);
	    }
	}

	return result;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response,
	    Account account, String authTokenType, Bundle options)
	    throws NetworkErrorException {
	// TODO Auto-generated method stub
	return null;
    }

}
