package com.openerp.auth;



import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class OpenERPAuthenticateService extends Service {
	private static final String TAG = "AccountAuthenticatorService";
	private static OpenERPAuthenticator oeAccountAuthenticator = null;
	public OpenERPAuthenticateService(){
		
		super();
	
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		IBinder ret= null;
		if(intent.getAction().equals(android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT)){
			ret = new OpenERPAuthenticator(this).getIBinder();
		}
		return ret;
	}
	private OpenERPAuthenticator getAuthenticator(){
		if(oeAccountAuthenticator==null){
			oeAccountAuthenticator = new OpenERPAuthenticator(this);
		}
		return oeAccountAuthenticator;
		
	}

}
