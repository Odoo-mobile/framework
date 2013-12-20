/*
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
package com.openerp.base.res;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;

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

import com.openerp.auth.OpenERPAccountManager;
import com.openerp.support.OEUser;
import com.openerp.util.Base64Helper;

public class Res_PartnerSyncHelper {

	Context mContext = null;
	private static ContentResolver mContentResolver = null;
	private static String SYNC1_PARTNER_ID = ContactsContract.RawContacts.SYNC1;

	public Res_PartnerSyncHelper(Context context) {
		mContext = context;
	}

	public boolean createNewContact(int partner_id) {
		Account account = OpenERPAccountManager.getAccount(mContext, OEUser
				.current(mContext).getAndroidName());
		try {
			Res_PartnerDBHelper dbHelper = new Res_PartnerDBHelper(mContext);
			HashMap<String, Object> res = dbHelper.search(dbHelper,
					new String[] { "(phone != ? ", "OR", "mobile != ? ", "OR",
							"email != ? ) ", "AND", "id = ? " }, new String[] {
							"false", "false", "false", partner_id + "" });
			// checking if records exist?
			int total = Integer.parseInt(res.get("total").toString());

			if (total > 0) {
				@SuppressWarnings("unchecked")
				List<HashMap<String, Object>> rows = (List<HashMap<String, Object>>) res
						.get("records");

				for (HashMap<String, Object> row_data : rows) {

					if (!(row_data.get("company_id").toString())
							.equalsIgnoreCase("false")) {
						String partnerID = row_data.get("id").toString();
						String name = (row_data.get("name").toString())
								.replaceAll("[^\\w\\s]", "");
						String mail = row_data.get("email").toString();
						String number = row_data.get("phone").toString();
						String mobile = row_data.get("mobile").toString();
						String website = row_data.get("website").toString();
						String street = row_data.get("street").toString();
						String street2 = row_data.get("street2").toString();
						String city = row_data.get("city").toString();
						String zip = row_data.get("zip").toString();
						String company = "OpenERP";
						String image = row_data.get("image_small").toString();

						// Creating new contact
						addContact(mContext, account, partnerID, name, mail,
								number, mobile, website, street, street2, city,
								zip, company, image);
						return true;
					}
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	public Uri getPartnerUri(int partner_id) {
		Account account = OpenERPAccountManager.getAccount(mContext, OEUser
				.current(mContext).getAndroidName());
		Uri rawContactUri = RawContacts.CONTENT_URI.buildUpon()
				.appendQueryParameter(RawContacts.ACCOUNT_NAME, account.name)
				.appendQueryParameter(RawContacts.ACCOUNT_TYPE, account.type)
				.build();

		mContentResolver = mContext.getContentResolver();
		Cursor data = mContentResolver.query(rawContactUri, null,
				SYNC1_PARTNER_ID + " = " + partner_id, null, null);
		String contact_raw_id = null;
		while (data.moveToNext()) {
			contact_raw_id = data.getString(data
					.getColumnIndex(ContactsContract.Contacts._ID));
		}
		data.close();

		Uri contact_uri = null;
		if (contact_raw_id != null) {
			contact_uri = Uri.withAppendedPath(
					ContactsContract.Contacts.CONTENT_URI, contact_raw_id);
		}
		return contact_uri;
	}

	private void addContact(Context context, Account account,
			String partner_id, String name, String mail, String number,
			String mobile, String website, String street, String street2,
			String city, String zip, String company, String image) {

		ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
		int rawContactInsertIndex = operationList.size();

		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newInsert(RawContacts.CONTENT_URI);
		builder.withValue(RawContacts.ACCOUNT_NAME, account.name);
		builder.withValue(RawContacts.ACCOUNT_TYPE, account.type);
		builder.withValue(RawContacts.SYNC1, partner_id);
		operationList.add(builder.build());

		// Display Name
		builder = ContentProviderOperation
				.newInsert(ContactsContract.Data.CONTENT_URI);
		builder.withValueBackReference(
				ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID,
				0);
		builder.withValue(
				ContactsContract.Data.MIMETYPE,
				ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
		builder.withValue(
				ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
				name);
		operationList.add(builder.build());

		// Connection to send message from contact
		builder = ContentProviderOperation
				.newInsert(ContactsContract.Data.CONTENT_URI);
		builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
		builder.withValue(ContactsContract.Data.MIMETYPE,
				"vnd.android.cursor.item/vnd.com.openerp.auth.profile");
		builder.withValue(ContactsContract.Data.DATA1, name);
		builder.withValue(ContactsContract.Data.DATA2, partner_id);
		builder.withValue(ContactsContract.Data.DATA3, "Send Message");
		operationList.add(builder.build());

		// Email
		if (!mail.equals("false")) {
			builder = ContentProviderOperation
					.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValueBackReference(
					ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID,
					0);
			builder.withValue(ContactsContract.Data.MIMETYPE,
					ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
			builder.withValue(ContactsContract.CommonDataKinds.Email.DATA, mail);
			operationList.add(builder.build());
		}

		// Phone number
		if (!number.equals("false")) {
			builder = ContentProviderOperation
					.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValueBackReference(
					ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID,
					0);
			builder.withValue(ContactsContract.Data.MIMETYPE,
					ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
			builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
					number);
			operationList.add(builder.build());
		}

		// Mobile number
		if (!mobile.equals("false")) {
			builder = ContentProviderOperation
					.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValueBackReference(
					ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID,
					0);
			builder.withValue(ContactsContract.Data.MIMETYPE,
					ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
			builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
					mobile);
			builder.withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
					ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
			operationList.add(builder.build());
		}

		// Website
		if (!website.equals("false")) {
			builder = ContentProviderOperation
					.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValueBackReference(
					ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID,
					0);
			builder.withValue(ContactsContract.Data.MIMETYPE,
					ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
			builder.withValue(ContactsContract.CommonDataKinds.Website.TYPE,
					website);
			operationList.add(builder.build());
		}

		// Address street 1
		if (!street.equals("false")) {
			builder = ContentProviderOperation
					.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValueBackReference(
					ContactsContract.CommonDataKinds.StructuredPostal.RAW_CONTACT_ID,
					0);
			builder.withValue(
					ContactsContract.Data.MIMETYPE,
					ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
			builder.withValue(
					ContactsContract.CommonDataKinds.StructuredPostal.STREET,
					street);
			operationList.add(builder.build());
		}

		// Address street 2
		if (!street2.equals("false")) {
			builder = ContentProviderOperation
					.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValueBackReference(
					ContactsContract.CommonDataKinds.StructuredPostal.RAW_CONTACT_ID,
					0);
			builder.withValue(
					ContactsContract.Data.MIMETYPE,
					ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
			builder.withValue(
					ContactsContract.CommonDataKinds.StructuredPostal.STREET,
					street2);
			operationList.add(builder.build());
		}

		// Address City
		if (!city.equals("false")) {
			builder = ContentProviderOperation
					.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValueBackReference(
					ContactsContract.CommonDataKinds.StructuredPostal.RAW_CONTACT_ID,
					0);
			builder.withValue(
					ContactsContract.Data.MIMETYPE,
					ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
			builder.withValue(
					ContactsContract.CommonDataKinds.StructuredPostal.CITY,
					city);
			operationList.add(builder.build());
		}

		// Zip code
		if (!zip.equals("false")) {
			builder = ContentProviderOperation
					.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValueBackReference(
					ContactsContract.CommonDataKinds.StructuredPostal.RAW_CONTACT_ID,
					0);
			builder.withValue(
					ContactsContract.Data.MIMETYPE,
					ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
			builder.withValue(
					ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
					zip);
			operationList.add(builder.build());
		}

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		// Partner Image
		if (!image.equals("false")) {

			Bitmap bitmapOrg = Base64Helper.getBitmapImage(context, image);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmapOrg.compress(Bitmap.CompressFormat.JPEG, 100, stream);

			operationList
					.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID,
									rawContactInsertIndex)
							.withValue(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
							.withValue(
									ContactsContract.CommonDataKinds.Photo.PHOTO,
									stream.toByteArray()).build());
		}

		// Organization
		if (!company.equals("false")) {
			operationList
					.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
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
			mContentResolver.applyBatch(ContactsContract.AUTHORITY,
					operationList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static class SyncEntry {
		public Long partner_id = 0L;
	}

	public void syncContacts(Context context, Account account) {
		HashMap<String, SyncEntry> localContacts = new HashMap<String, SyncEntry>();
		mContentResolver = context.getContentResolver();
		int company_id = Integer.parseInt(OpenERPAccountManager.currentUser(
				context).getCompany_id());

		RawContacts.CONTENT_URI
				.buildUpon()
				.appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER,
						"true").build();

		// Load the local contacts
		Uri rawContactUri = RawContacts.CONTENT_URI.buildUpon()
				.appendQueryParameter(RawContacts.ACCOUNT_NAME, account.name)
				.appendQueryParameter(RawContacts.ACCOUNT_TYPE, account.type)
				.build();
		Cursor cursor = mContentResolver.query(rawContactUri, new String[] {
				BaseColumns._ID, SYNC1_PARTNER_ID }, null, null, null);

		while (cursor.moveToNext()) {
			SyncEntry entry = new SyncEntry();
			entry.partner_id = cursor.getLong(cursor
					.getColumnIndex(BaseColumns._ID));
			localContacts.put(cursor.getString(1), entry);
		}
		cursor.close();

		try {
			Res_PartnerDBHelper dbHelper = new Res_PartnerDBHelper(context);
			HashMap<String, Object> res = dbHelper.search(dbHelper,
					new String[] { "phone != ? ", "OR", "mobile != ? ", "OR",
							"email != ?" }, new String[] { "false", "false",
							"false" });
			// checking if records exist?
			int total = Integer.parseInt(res.get("total").toString());

			if (total > 0) {
				@SuppressWarnings("unchecked")
				List<HashMap<String, Object>> rows = (List<HashMap<String, Object>>) res
						.get("records");

				for (HashMap<String, Object> row_data : rows) {

					if (!(row_data.get("company_id").toString())
							.equalsIgnoreCase("false")) {
						JSONArray db_company_id = new JSONArray(row_data.get(
								"company_id").toString());
						String com_id = db_company_id.getJSONArray(0)
								.getString(0).toString();

						if (com_id.equalsIgnoreCase(String.valueOf(company_id))) {
							String partnerID = row_data.get("id").toString();
							String name = (row_data.get("name").toString())
									.replaceAll("[^\\w\\s]", "");
							String mail = row_data.get("email").toString();
							String number = row_data.get("phone").toString();
							String mobile = row_data.get("mobile").toString();
							String website = row_data.get("website").toString();
							String street = row_data.get("street").toString();
							String street2 = row_data.get("street2").toString();
							String city = row_data.get("city").toString();
							String zip = row_data.get("zip").toString();
							String company = "OpenERP";
							String image = row_data.get("image_small")
									.toString();
							if (localContacts
									.get(row_data.get("id").toString()) == null) {
								addContact(context, account, partnerID, name,
										mail, number, mobile, website, street,
										street2, city, zip, company, image);
							}
						}
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
