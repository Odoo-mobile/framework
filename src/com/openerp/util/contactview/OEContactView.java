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

import com.openerp.base.res.ResPartnerDB;
import com.openerp.orm.OEDataRow;
import com.openerp.support.OEUser;
import com.openerp.support.contact.OEContact;

public class OEContactView extends QuickContactBadge {

	public static final String TAG = "com.openerp.util.contactview.OEContactView";

	int mPartner_id = 0;
	Uri mContactUri = null;
	OEContact mContact = null;
	Context mContext = null;
	OEUser mUser = null;

	public OEContactView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public OEContactView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public OEContactView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();
	}

	private void init() {
		mUser = OEUser.current(mContext);
		mContact = new OEContact(mContext, mUser);
	}

	@Override
	public void onClick(View v) {
		if (hasUri(mPartner_id)) {
			super.onClick(v);
		} else {
			createContact(mPartner_id);
		}
	}

	public boolean hasUri(int id) {
		if (mContactUri != null) {
			return true;
		}
		return false;
	}

	public void assignPartnerId(int id) {
		mPartner_id = id;
		mContactUri = mContact.contactUri(id);
		if (mContactUri != null) {
			assignContactUri(mContactUri);
		}
	}

	private void createContact(int partner_id) {
		contactAddConfirmation(partner_id).show();
	}

	private Dialog contactAddConfirmation(final int partner_id) {

		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

		builder.setTitle("Add Contact")
				.setMessage("Add this partner to your contact.")

				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								OEDataRow partner = new ResPartnerDB(mContext)
										.select(partner_id);
								if (mContact.createContact(partner)) {
									Toast.makeText(mContext, "Contact Saved.",
											Toast.LENGTH_LONG).show();
									assignPartnerId(partner_id);
								}
							}
						}).setNegativeButton("Cancel", null);

		return builder.create();
	}
}