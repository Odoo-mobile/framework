package com.openerp.support;

import android.app.ProgressDialog;
import android.content.Context;

public class OEDialog extends ProgressDialog {

	public OEDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public OEDialog(Context context, boolean isCancelable, String message){
		super(context);
		this.setTitle("Please wait...");
		this.setCancelable(isCancelable);
		this.setMessage(message);
	}
	
}
