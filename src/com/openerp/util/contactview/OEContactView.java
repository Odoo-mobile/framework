package com.openerp.util.contactview;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.QuickContactBadge;
import android.widget.Toast;

import com.openerp.base.res.Res_PartnerSyncHelper;

public class OEContactView extends QuickContactBadge {

	int partner_id = 0;
	Uri contact_uri = null;
	Res_PartnerSyncHelper partner_helper = null;
	Context mContext = null;

	public OEContactView(Context context) {
		super(context);
		mContext = context;
		partner_helper = new Res_PartnerSyncHelper(context);
	}

	public OEContactView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		partner_helper = new Res_PartnerSyncHelper(context);
	}

	public OEContactView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		partner_helper = new Res_PartnerSyncHelper(context);
	}

	@Override
	public void onClick(View v) {
		if (hasUri(partner_id)) {
			super.onClick(v);
		} else {
			createContact(partner_id);
		}
	}

	public boolean hasUri(int id) {
		if (contact_uri != null) {
			return true;
		}
		return false;
	}

	public void assignPartnerId(int id) {
		partner_id = id;
		contact_uri = partner_helper.getPartnerUri(id);
		if (contact_uri != null) {
			assignContactUri(contact_uri);
		}
	}

	private void createContact(int partner_id) {
		contactAddConfirmation(partner_id).show();
	}

	private Dialog contactAddConfirmation(final int partner_id) {

		// Initialize the Alert Dialog

		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		// Source of the data in the DIalog

		// Set the dialog title
		builder.setTitle("Add Contact")
				.setMessage("Add this partner to your contact.")

				// Set the action buttons
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								if (partner_helper.createNewContact(partner_id)) {
									Toast.makeText(mContext, "Contact Saved.",
											Toast.LENGTH_LONG).show();
									assignPartnerId(partner_id);
								}
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								return;
							}
						});

		return builder.create();
	}
}