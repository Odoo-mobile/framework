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
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

import com.openerp.R;
import com.openerp.auth.OpenERPAccountManager;
import com.openerp.services.ContactSyncService;
import com.openerp.util.Base64Helper;

public class Res_PartnerSyncHelper {

	Context context = null;
	private static ContentResolver mContentResolver = null;
	private static String PhotoTimestampColumn = ContactsContract.RawContacts.SYNC2;
	private static String UsernameColumn = ContactsContract.RawContacts.SYNC1;

	public Res_PartnerSyncHelper(Context context) {
		context = this.context;
	}

	private static void addContact(Context context, Account account,
			String partner_id, String name, String username, String mail,
			String number, String mobile, String website, String street,
			String street2, String city, String zip, String company,
			String image) {
		ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
		int rawContactInsertIndex = operationList.size();

		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newInsert(RawContacts.CONTENT_URI);
		builder.withValue(RawContacts.ACCOUNT_NAME, account.name);
		builder.withValue(RawContacts.ACCOUNT_TYPE, account.type);
		builder.withValue(RawContacts.SYNC1, username);
		operationList.add(builder.build());

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

		builder = ContentProviderOperation
				.newInsert(ContactsContract.Data.CONTENT_URI);
		builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
		builder.withValue(ContactsContract.Data.MIMETYPE,
				"vnd.android.cursor.item/vnd.com.openerp.auth.profile");
		builder.withValue(ContactsContract.Data.DATA1, username);
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
		// INSERT imAGE
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

	private static void updateContactPhoto(
			ArrayList<ContentProviderOperation> operationList,
			long rawContactId, String photo_string, Context context) {
		if (!photo_string.equals("false")) {
			Bitmap avatar = Base64Helper.getBitmapImage(context, photo_string);
			ByteArrayOutputStream convertStream = new ByteArrayOutputStream(
					avatar.getWidth() * avatar.getHeight() * 4);
			avatar.compress(Bitmap.CompressFormat.JPEG, 95, convertStream);
			try {
				convertStream.flush();
				convertStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			// On pre-Honeycomb systems, it's important to call recycle on
			// bitmaps
			avatar.recycle();
			byte[] photo = convertStream.toByteArray();
			ContentProviderOperation.Builder builder = ContentProviderOperation
					.newDelete(ContactsContract.Data.CONTENT_URI);
			builder.withSelection(ContactsContract.Data.RAW_CONTACT_ID + " = '"
					+ rawContactId + "' AND " + ContactsContract.Data.MIMETYPE
					+ " = '"
					+ ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
					+ "'", null);
			operationList.add(builder.build());

			try {
				if (photo != null) {
					builder = ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI);
					builder.withValue(
							ContactsContract.CommonDataKinds.Photo.RAW_CONTACT_ID,
							rawContactId);
					builder.withValue(
							ContactsContract.Data.MIMETYPE,
							ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
					builder.withValue(
							ContactsContract.CommonDataKinds.Photo.PHOTO, photo);
					operationList.add(builder.build());

					builder = ContentProviderOperation
							.newUpdate(ContactsContract.RawContacts.CONTENT_URI);
					builder.withSelection(
							ContactsContract.RawContacts.CONTACT_ID + " = '"
									+ rawContactId + "'", null);
					builder.withValue(PhotoTimestampColumn,
							String.valueOf(System.currentTimeMillis()));
					operationList.add(builder.build());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static class SyncEntry {
		public Long raw_id = 0L;
		public Long photo_timestamp = null;
	}

	public void SyncContect(Context context, Account account) {
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
		Cursor c1 = mContentResolver.query(rawContactUri, new String[] {
				BaseColumns._ID, UsernameColumn, PhotoTimestampColumn }, null,
				null, null);
		while (c1.moveToNext()) {
			SyncEntry entry = new SyncEntry();
			entry.raw_id = c1.getLong(c1.getColumnIndex(BaseColumns._ID));
			entry.photo_timestamp = c1.getLong(c1
					.getColumnIndex(PhotoTimestampColumn));
			localContacts.put(c1.getString(1), entry);
		}

		ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
		try {
			Res_PartnerDBHelper dbHelper = new Res_PartnerDBHelper(context);
			HashMap<String, Object> res = dbHelper.search(dbHelper);
			// checking if records exist?
			int total = Integer.parseInt(res.get("total").toString());
			// System.out.println("TOTAL PARTNERS ::" + total);

			if (total > 0) {
				@SuppressWarnings("unchecked")
				List<HashMap<String, Object>> rows = (List<HashMap<String, Object>>) res
						.get("records");

				for (HashMap<String, Object> row_data : rows) {
					if (localContacts.get(row_data.get("id").toString()) == null) {

						if (!(row_data.get("company_id").toString())
								.equalsIgnoreCase("false")) {
							JSONArray db_company_id = new JSONArray(row_data
									.get("company_id").toString());
							String com_id = db_company_id.getJSONArray(0)
									.getString(0).toString();

							if (com_id.equalsIgnoreCase(String
									.valueOf(company_id))) {

								String partnerID = row_data.get("id")
										.toString();
								// Remove everything except characters and
								// digits
								String name = (row_data.get("name").toString())
										.replaceAll("[^\\w\\s]", "");
								String userName = row_data.get("id").toString();
								String mail = row_data.get("email").toString();
								String number = row_data.get("phone")
										.toString();
								String mobile = row_data.get("mobile")
										.toString();
								String website = row_data.get("website")
										.toString();
								String street = row_data.get("street")
										.toString();
								String street2 = row_data.get("street2")
										.toString();
								String city = row_data.get("city").toString();
								String zip = row_data.get("zip").toString();
								String company = "OpenERP";
								String image = row_data.get("image").toString();

								addContact(context, account, partnerID, name,
										userName, mail, number, mobile,
										website, street, street2, city, zip,
										company, image);
							}
						}
					}
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}
