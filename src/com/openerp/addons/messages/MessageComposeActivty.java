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
package com.openerp.addons.messages;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.accounts.Account;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.openerp.MainActivity;
import com.openerp.R;
import com.openerp.auth.OpenERPAccountManager;
import com.openerp.base.ir.Ir_AttachmentDBHelper;
import com.openerp.base.res.Res_PartnerDBHelper;
import com.openerp.orm.OEHelper;
import com.openerp.providers.message.MessageProvider;
import com.openerp.support.JSONDataHelper;
import com.openerp.support.listview.ControlClickEventListener;
import com.openerp.support.listview.OEListViewAdapter;
import com.openerp.support.listview.OEListViewRows;
import com.openerp.util.Base64Helper;

public class MessageComposeActivty extends Activity {
	private static final int PICKFILE_RESULT_CODE = 1;
	private static final int ADD_RECIPIENT = 2;
	List<Uri> file_uris = new ArrayList<Uri>();
	ListView lstAttachments = null;
	List<OEListViewRows> attachments = new ArrayList<OEListViewRows>();
	OEListViewAdapter lstAttachmentAdapter = null;
	List<OEListViewRows> partners_list = new ArrayList<OEListViewRows>();
	HashMap<String, Object> selectedPartners = new HashMap<String, Object>();

	enum ATTACHMENT_TYPE {
		IMAGE, TEXT_FILE
	}

	EnumMap<ATTACHMENT_TYPE, String> attachments_type = new EnumMap<MessageComposeActivty.ATTACHMENT_TYPE, String>(
			ATTACHMENT_TYPE.class);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message_compose);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setTitle("Compose");
		getActionBar().setDisplayHomeAsUpEnabled(true);

		lstAttachments = (ListView) findViewById(R.id.lstAttachments);
		String[] from = new String[] { "name" };
		int[] to = new int[] { R.id.txvFileName };
		lstAttachmentAdapter = new OEListViewAdapter(MainActivity.context,
				R.layout.message_attachment_listview_item, attachments, from,
				to, null);
		lstAttachments.setAdapter(lstAttachmentAdapter);
		lstAttachmentAdapter.setItemClickListener(R.id.imgBtnRemoveAttachment,
				new ControlClickEventListener() {

					@Override
					public OEListViewRows controlClicked(int position,
							OEListViewRows row, View view) {
						attachments.remove(position);
						lstAttachmentAdapter.refresh(attachments);
						return null;
					}
				});

		Res_PartnerDBHelper partners = new Res_PartnerDBHelper(this);
		HashMap<String, Object> data = partners.search(partners);
		if ((Integer) data.get("total") > 0) {
			for (HashMap<String, Object> row : (List<HashMap<String, Object>>) data
					.get("records")) {
				OEListViewRows newRow = new OEListViewRows(Integer.parseInt(row
						.get("id").toString()), row);
				partners_list.add(newRow);
			}
		}
		OEHelper oe = partners.getOEInstance();
		JSONObject domain = new JSONObject();
		try {
			domain.put("domain", new JSONArray("[[\"id\", \"not in\", "
					+ JSONDataHelper
							.intArrayToJSONArray(oe.getAllIds(partners))
							.toString() + "]]"));
		} catch (Exception e) {
		}
		findViewById(R.id.imgBtnAddRecipients).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(MessageComposeActivty.this,
								MessageRecipientActivity.class);
						intent.putExtra("selected_ids", selectedPartners);
						startActivityForResult(intent, ADD_RECIPIENT);
					}
				});

		handleIntentFilter(getIntent());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_message_compose_activty, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case android.R.id.home:
			// app icon in action bar clicked; go home
			finish();
			return true;
		case R.id.menu_message_compose_add_attachment_images:
			requestForAttachmentIntent(ATTACHMENT_TYPE.IMAGE);
			return true;
		case R.id.menu_message_compose_add_attachment_files:
			requestForAttachmentIntent(ATTACHMENT_TYPE.TEXT_FILE);
			return true;
		case R.id.menu_message_compose_send:

			EditText edtSubject = (EditText) findViewById(R.id.edtMessageSubject);
			EditText edtBody = (EditText) findViewById(R.id.edtMessageBody);
			edtSubject.setError(null);
			edtBody.setError(null);
			if (selectedPartners.size() == 0) {
				Toast.makeText(this, "Select atleast one receiptent",
						Toast.LENGTH_LONG).show();
			} else if (TextUtils.isEmpty(edtSubject.getText())) {
				edtSubject.setError("Provide Message Subject !");
			} else if (TextUtils.isEmpty(edtBody.getText())) {
				edtBody.setError("Provide Message Body !");
			} else {

				Toast.makeText(this, "Sending message...", Toast.LENGTH_LONG)
						.show();
				String subject = edtSubject.getText().toString();
				String body = edtBody.getText().toString();

				Ir_AttachmentDBHelper attachment = new Ir_AttachmentDBHelper(
						MainActivity.context);
				JSONArray newAttachmentIds = new JSONArray();
				for (Uri file : file_uris) {
					File fileData = new File(getRealPathFromURI(file));
					ContentValues values = new ContentValues();
					values.put("datas_fname", fileData.getName());
					values.put("res_model", "mail.compose.message");
					values.put("company_id", 1);
					values.put("type", "binary");
					values.put("res_id", 0);
					values.put("file_size", fileData.length());
					values.put("db_datas", Base64Helper.fileUriToBase64(file,
							getContentResolver()));
					values.put("name", fileData.getName());
					int newId = attachment.create(attachment, values);
					newAttachmentIds.put(newId);
				}

				// TASK: sending mail
				HashMap<String, Object> values = new HashMap<String, Object>();
				values.put("subject", subject);
				values.put("body", body);
				values.put("partner_ids", getPartnersId());
				values.put("attachment_ids", newAttachmentIds);

				SendMailMessage sendMessage = new SendMailMessage(values);
				sendMessage.execute((Void) null);

			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private JSONArray getPartnersId() {
		JSONArray list = new JSONArray();
		for (String key : selectedPartners.keySet()) {
			ContentValues val = (ContentValues) selectedPartners.get(key);
			list.put(val.getAsInteger("id"));
		}
		return list;
	}

	/**
	 * Handle message intent filter for attachments
	 * 
	 * @param intent
	 */
	private void handleIntentFilter(Intent intent) {
		attachments_type.put(ATTACHMENT_TYPE.IMAGE, "image/*");
		attachments_type.put(ATTACHMENT_TYPE.TEXT_FILE, "application/*");

		String action = intent.getAction();
		String type = intent.getType();

		// Single attachment
		if (Intent.ACTION_SEND.equals(action) && type != null) {
			Uri fileUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
			file_uris.add(fileUri);
			handleReceivedFile();
		}

		// Multiple Attachments
		if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
			ArrayList<Uri> fileUris = intent
					.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
			file_uris.addAll(fileUris);
			handleReceivedFile();

		}

	}

	/**
	 * getting real path from attachment URI.
	 * 
	 * @param contentUri
	 * @return
	 */
	private String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		CursorLoader loader = new CursorLoader(this, contentUri, proj, null,
				null, null);
		Cursor cursor = loader.loadInBackground();
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case PICKFILE_RESULT_CODE:
			if (resultCode == RESULT_OK) {
				String FilePath = data.getDataString();
				Uri fileUri = Uri.parse(FilePath);
				file_uris.add(fileUri);
				handleReceivedFile();
			}
			break;
		case ADD_RECIPIENT:
			if (resultCode == RESULT_OK) {
				selectedPartners = (HashMap<String, Object>) data.getExtras()
						.get("result");
				StringBuffer users_list = new StringBuffer();
				users_list.append(TextUtils.join(", ",
						selectedPartners.keySet()));
				EditText edtTo = (EditText) findViewById(R.id.edtMessageTo);
				edtTo.setText(users_list.toString());
			}
			break;
		}

	}

	/**
	 * requesting for file browse for attachment in message
	 * 
	 * @param type
	 */
	private void requestForAttachmentIntent(ATTACHMENT_TYPE type) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType(attachments_type.get(type));
		startActivityForResult(intent, PICKFILE_RESULT_CODE);
	}

	private void handleReceivedFile() {
		attachments.clear();
		for (Uri uri : file_uris) {
			int row_id = Integer.parseInt(uri.getLastPathSegment().toString());
			File file = new File(getRealPathFromURI(uri));
			HashMap<String, Object> data = new HashMap<String, Object>();
			data.put("name", file.getName());
			OEListViewRows row = new OEListViewRows(row_id, data);
			attachments.add(row);
			lstAttachmentAdapter.refresh(attachments);
		}
	}

	class SendMailMessage extends AsyncTask<Void, Void, Boolean> {
		HashMap<String, Object> values = new HashMap<String, Object>();

		public SendMailMessage(HashMap<String, Object> values) {
			this.values = values;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub

			// Message Details
			String subject = values.get("subject").toString();
			String body = values.get("body").toString();
			JSONArray partner_ids = (JSONArray) values.get("partner_ids");
			JSONArray attachment_ids = (JSONArray) values.get("attachment_ids");

			// mail_message object
			MessageDBHelper message = new MessageDBHelper(MainActivity.context);

			// OpenERP Helper instance
			OEHelper oe = message.getOEInstance();

			// Res partner object
			Res_PartnerDBHelper partners = new Res_PartnerDBHelper(
					MainActivity.context);

			// Getting current user detail (name and email)
			String partner_id = OpenERPAccountManager.currentUser(
					getApplicationContext()).getPartner_id();
			HashMap<String, Object> user_details = partners.search(partners,
					new String[] { "id = ?" }, new String[] { partner_id });
			String userFullname = "";
			String userEmail = "";
			if ((Integer) user_details.get("total") > 0) {
				userFullname = ((List<HashMap<String, Object>>) user_details
						.get("records")).get(0).get("name").toString();
				userEmail = ((List<HashMap<String, Object>>) user_details
						.get("records")).get(0).get("email").toString();
			}

			// Preparing arguments for send message
			try {
				String model = "mail.compose.message";
				JSONObject arguments = new JSONObject();
				arguments.put("composition_mode", "comment");
				arguments.put("model", false);
				arguments.put("parent_id", false);
				arguments.put("email_from", userFullname + "<" + userEmail
						+ ">");
				arguments.put("subject", subject);
				arguments.put("post", true);
				arguments.put("notify", false);
				arguments.put("same_thread", true);

				JSONArray partnerIds = new JSONArray();
				partnerIds.put(6);
				partnerIds.put(false);
				partnerIds.put(partner_ids);

				arguments.put("partner_ids",
						new JSONArray("[" + partnerIds.toString() + "]"));
				arguments.put("body", body);

				JSONArray attachmentsObj = new JSONArray();

				if (attachment_ids.length() < 0) {
					attachmentsObj.put(6);
					attachmentsObj.put(false);
					attachmentsObj.put(new JSONArray());
				}

				for (int k = 0; k < attachment_ids.length(); k++) {

					JSONArray attachmentIds = new JSONArray();
					attachmentIds.put(4);
					attachmentIds.put(attachment_ids.get(k));
					attachmentIds.put(false);
					attachmentsObj.put(attachmentIds);
				}

				arguments.put("attachment_ids",
						new JSONArray(attachmentsObj.toString()));

				arguments.put("template_id", false);

				JSONArray args = new JSONArray();
				args.put(arguments);

				JSONObject kwargs = new JSONObject();
				kwargs.put("context", oe.updateContext(new JSONObject()));
				oe.updateKWargs(kwargs);

				// Creating compose message
				JSONObject messageRes = oe.call_kw(model, "create", args);
				// Preparing ids for send mail
				String cmsgId = messageRes.getString("result");
				args = null;
				args = new JSONArray();
				args.put(new JSONArray("[" + Integer.parseInt(cmsgId) + "]"));
				args.put(oe.updateContext(new JSONObject()));
				oe.updateKWargs(null);

				// sending mail
				JSONObject send_mail = oe.call_kw(model, "send_mail", args);

				// Requesting for sync
				Account account = OpenERPAccountManager.getAccount(
						getApplicationContext(),
						MainActivity.userContext.getAndroidName());
				Bundle settingsBundle = new Bundle();
				settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL,
						true);
				settingsBundle.putBoolean(
						ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
				ContentResolver.requestSync(account, MessageProvider.AUTHORITY,
						settingsBundle);

			} catch (Exception e) {
			}
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			if (success) {
				Toast.makeText(getApplicationContext(),
						"Message sent succussfull.", Toast.LENGTH_LONG).show();
				selectedPartners = new HashMap<String, Object>();
				finish();
			} else {
				Toast.makeText(getApplicationContext(),
						"Unable to send message.", Toast.LENGTH_LONG).show();
			}
		}

	}
}
