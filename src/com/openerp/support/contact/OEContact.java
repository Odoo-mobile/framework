/**
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http://www.openerp.com>)
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * 
 */
package com.openerp.support.contact;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

import com.openerp.auth.OpenERPAccountManager;
import com.openerp.orm.OEDataRow;
import com.openerp.orm.OEValues;
import com.openerp.support.OEUser;
import com.openerp.util.Base64Helper;

public class OEContact {

	public static final String TAG = "com.openerp.support.contact.OEContact";

	ContentResolver mContentResolver = null;
	Uri rawContactUri = null;

	String[] mProjections = new String[] { BaseColumns._ID,
			ContactsContract.RawContacts.SYNC1 };

	Uri mRawContacts = null;
	Uri mContactContract = null;

	String mRawContactId = null;

	Account mAccount = null;
	OEUser mUser = null;
	Context mContext = null;

	public OEContact(Context context, OEUser user) {
		mContext = context;
		mUser = user;
		mAccount = OpenERPAccountManager.getAccount(context,
				user.getAndroidName());
		mContentResolver = mContext.getContentResolver();

		mRawContacts = RawContacts.CONTENT_URI;
		mContactContract = ContactsContract.Data.CONTENT_URI;
		mRawContactId = ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID;

		rawContactUri = RawContacts.CONTENT_URI.buildUpon()
				.appendQueryParameter(RawContacts.ACCOUNT_NAME, mAccount.name)
				.appendQueryParameter(RawContacts.ACCOUNT_TYPE, mAccount.type)
				.build();

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
	}

	public List<OEValues> localContacts() {
		List<OEValues> contacts = new ArrayList<OEValues>();
		Cursor cr = mContentResolver.query(rawContactUri, mProjections, null,
				null, null);
		if (cr.moveToFirst()) {
			do {
				OEValues values = new OEValues();
				for (String key : mProjections)
					values.put(key, cr.getString(cr.getColumnIndex(key)));
				contacts.add(values);
			} while (cr.moveToNext());
		}
		cr.close();
		return contacts;
	}

	public boolean contactExists(int partner_id) {
		Log.d(TAG, "OEContact->contactExists()");
		boolean flag = false;
		if (contactUri(partner_id) != null)
			flag = true;
		return flag;
	}

	public boolean createContacts(List<OEDataRow> partners) {
		Log.d(TAG, "OEContact->createContacts()");
		boolean flag = false;
		for (OEDataRow partner : partners)
			flag = createContact(partner);
		return flag;
	}

	public boolean removeContact(int partner_id) {
		int count = mContentResolver.delete(rawContactUri, mProjections[1]
				+ " = ?", new String[] { partner_id + "" });
		if (count > 0) {
			return true;
		}

		return false;
	}

	public boolean createContact(OEDataRow partner) {
		Log.d(TAG, "OEContact->createContact()");
		boolean flag = false;

		boolean isContact = contactExists(partner.getInt("id"));
		if (isContact)
			removeContact(partner.getInt("id"));
		ContentProviderOperation.Builder builder = null;
		ArrayList<ContentProviderOperation> oList = new ArrayList<ContentProviderOperation>();
		int rawContactInsertIndex = oList.size();

		// Contact Raw details
		builder = ContentProviderOperation.newInsert(mRawContacts);
		builder.withValue(RawContacts.ACCOUNT_NAME, mAccount.name);
		builder.withValue(RawContacts.ACCOUNT_TYPE, mAccount.type);
		builder.withValue(RawContacts.SYNC1, partner.getInt("id"));
		oList.add(builder.build());

		// Display Name
		builder = ContentProviderOperation.newInsert(mContactContract);
		builder.withValueBackReference(mRawContactId, 0);
		builder.withValue(
				ContactsContract.Data.MIMETYPE,
				ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
		builder.withValue(
				ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
				partner.getString("name"));
		oList.add(builder.build());

		// Connection to send message from contact
		builder = ContentProviderOperation.newInsert(mContactContract);
		builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
		builder.withValue(ContactsContract.Data.MIMETYPE,
				"vnd.android.cursor.item/vnd.com.openerp.auth.profile");
		builder.withValue(ContactsContract.Data.DATA1,
				partner.getString("name"));
		builder.withValue(ContactsContract.Data.DATA2, partner.getInt("id"));
		builder.withValue(ContactsContract.Data.DATA3, "Send Message");
		oList.add(builder.build());

		// Email
		if (!partner.getString("email").equals("false")) {
			builder = ContentProviderOperation.newInsert(mContactContract);
			builder.withValueBackReference(mRawContactId, 0);
			builder.withValue(ContactsContract.Data.MIMETYPE,
					ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
			builder.withValue(ContactsContract.CommonDataKinds.Email.DATA,
					partner.getString("email"));
			oList.add(builder.build());
		}

		// Phone number
		if (!partner.getString("phone").equals("false")) {
			builder = ContentProviderOperation
					.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValueBackReference(
					ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID,
					0);
			builder.withValue(ContactsContract.Data.MIMETYPE,
					ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
			builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
					partner.getString("phone"));
			oList.add(builder.build());
		}

		// Mobile number
		if (!partner.getString("mobile").equals("false")) {
			builder = ContentProviderOperation.newInsert(mContactContract);
			builder.withValueBackReference(mRawContactId, 0);
			builder.withValue(ContactsContract.Data.MIMETYPE,
					ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
			builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
					partner.getString("mobile"));
			builder.withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
					ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
			oList.add(builder.build());
		}

		// Website
		if (!partner.getString("website").equals("false")) {
			builder = ContentProviderOperation
					.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValueBackReference(
					ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID,
					0);
			builder.withValue(ContactsContract.Data.MIMETYPE,
					ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
			builder.withValue(ContactsContract.CommonDataKinds.Website.TYPE,
					partner.getString("website"));
			oList.add(builder.build());
		}

		// Address street 1
		if (!partner.getString("street").equals("false")) {
			builder = ContentProviderOperation.newInsert(mContactContract);
			builder.withValueBackReference(mRawContactId, 0);
			builder.withValue(
					ContactsContract.Data.MIMETYPE,
					ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
			builder.withValue(
					ContactsContract.CommonDataKinds.StructuredPostal.STREET,
					partner.getString("street"));
			oList.add(builder.build());
		}

		// Address street 2
		if (!partner.getString("street2").equals("false")) {
			builder = ContentProviderOperation.newInsert(mContactContract);
			builder.withValueBackReference(mRawContactId, 0);
			builder.withValue(
					ContactsContract.Data.MIMETYPE,
					ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
			builder.withValue(
					ContactsContract.CommonDataKinds.StructuredPostal.STREET,
					partner.getString("street2"));
			oList.add(builder.build());
		}

		// Address City
		if (!partner.getString("city").equals("false")) {
			builder = ContentProviderOperation.newInsert(mContactContract);
			builder.withValueBackReference(mRawContactId, 0);
			builder.withValue(
					ContactsContract.Data.MIMETYPE,
					ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
			builder.withValue(
					ContactsContract.CommonDataKinds.StructuredPostal.CITY,
					partner.getString("city"));
			oList.add(builder.build());
		}

		// Zip code
		if (!partner.getString("zip").equals("false")) {
			builder = ContentProviderOperation.newInsert(mContactContract);
			builder.withValueBackReference(mRawContactId, 0);
			builder.withValue(
					ContactsContract.Data.MIMETYPE,
					ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
			builder.withValue(
					ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
					partner.getString("zip"));
			oList.add(builder.build());
		}

		// Partner Image
		if (!partner.getString("image_small").equals("false")) {

			Bitmap bitmapOrg = Base64Helper.getBitmapImage(mContext,
					partner.getString("image_small"));
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmapOrg.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			oList.add(ContentProviderOperation
					.newInsert(mContactContract)
					.withValueBackReference(
							ContactsContract.Data.RAW_CONTACT_ID,
							rawContactInsertIndex)
					.withValue(
							ContactsContract.Data.MIMETYPE,
							ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
					.withValue(ContactsContract.CommonDataKinds.Photo.PHOTO,
							stream.toByteArray()).build());
		}

		// Organization
		String company = "false";
		company = partner.getM2ORecord("company_id").browse().getString("name");
		if (!company.equals("false")) {
			oList.add(ContentProviderOperation
					.newInsert(mContactContract)
					.withValueBackReference(
							ContactsContract.Data.RAW_CONTACT_ID, 0)
					.withValue(
							ContactsContract.Data.MIMETYPE,
							ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
					.withValue(
							ContactsContract.CommonDataKinds.Organization.COMPANY,
							company)
					.withValue(
							ContactsContract.CommonDataKinds.Organization.TYPE,
							ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
					.build());
		}

		try {
			mContentResolver.applyBatch(ContactsContract.AUTHORITY, oList);
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}

	public Uri contactUri(int partner_id) {
		Uri uri = null;
		Cursor cr = mContentResolver.query(rawContactUri, mProjections,
				mProjections[1] + " = ?", new String[] { partner_id + "" },
				null);

		if (cr.moveToFirst()) {
			String raw_id = cr.getString(0);
			uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI,
					raw_id);
		}
		cr.close();
		return uri;
	}
}
