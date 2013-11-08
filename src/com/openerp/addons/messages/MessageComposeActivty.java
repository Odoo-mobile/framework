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
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.accounts.Account;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.text.InputType;
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
import com.openerp.orm.Fields;
import com.openerp.orm.OEHelper;
import com.openerp.providers.message.MessageProvider;
import com.openerp.support.AppScope;
import com.openerp.support.JSONDataHelper;
import com.openerp.support.listview.ControlClickEventListener;
import com.openerp.support.listview.OEListViewAdapter;
import com.openerp.support.listview.OEListViewRows;
import com.openerp.util.Base64Helper;
import com.openerp.util.HTMLHelper;
import com.openerp.util.OEDate;
import com.openerp.util.tags.TagsItems;
import com.openerp.util.tags.TagsView;

public class MessageComposeActivty extends Activity {
	private static final int PICKFILE_RESULT_CODE = 1;
	private static final int ADD_RECIPIENT = 2;
	List<Uri> file_uris = new ArrayList<Uri>();
	ListView lstAttachments = null;
	List<OEListViewRows> attachments = new ArrayList<OEListViewRows>();
	OEListViewAdapter lstAttachmentAdapter = null;
	List<OEListViewRows> partners_list = new ArrayList<OEListViewRows>();
	HashMap<String, Object> selectedPartners = new HashMap<String, Object>();
	boolean is_note_body = false;
	boolean is_reply = false;
	int message_id = 0;
	AppScope scope = null;
	TagsView receipients_view = null;
	List<TagsItems> parters = new ArrayList<TagsItems>();
	/** The parent_row. */
	HashMap<String, Object> parent_row = null;
	ReceipientsTagsCustomAdapter partner_adapter = null;

	enum ATTACHMENT_TYPE {
		IMAGE, TEXT_FILE
	}

	EnumMap<ATTACHMENT_TYPE, String> attachments_type = new EnumMap<MessageComposeActivty.ATTACHMENT_TYPE, String>(
			ATTACHMENT_TYPE.class);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message_compose);
		scope = new AppScope(MainActivity.userContext,
				(MainActivity) MainActivity.context);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		receipients_view = (TagsView) findViewById(R.id.receipients_view);
		partner_adapter = new ReceipientsTagsCustomAdapter(this,
				R.layout.message_receipient_item_layout, parters);
		receipients_view.setAdapter(partner_adapter);
		/* tags component */
		parters.addAll(getAllPartners());
		receipients_view.setPrefix("To: ");
		receipients_view.allowDuplicates(false);

		Intent replyIntent = getIntent();
		if (replyIntent.hasExtra("send_reply")) {
			findViewById(R.id.imgBtnAddRecipients).setEnabled(false);
			is_reply = true;
			message_id = replyIntent.getExtras().getInt("message_id");
			MessageDBHelper msgDb = new MessageDBHelper(this);
			parent_row = ((List<HashMap<String, Object>>) msgDb.search(msgDb,

			new String[] { "id = ?" }, new String[] { message_id + "" }).get(
					"records")).get(0);
			getActionBar().setTitle("Reply");
			EditText edtSubject = (EditText) findViewById(R.id.edtMessageSubject);
			edtSubject.setText("Re: " + parent_row.get("subject").toString());
			JSONArray partner_ids = new JSONArray();
			try {

				JSONArray partners = getPartnersOfMessage(message_id + "");
				for (int i = 0; i < partners.length(); i++) {
					JSONArray partner = partners.getJSONArray(i);
					partner_ids.put(partner.get(0));

					String display_name = partner.getString(1);

					ContentValues value = new ContentValues();
					value.put("pos", -1);
					value.put("id", partner.getInt(0));
					value.put("email", "");

					selectedPartners.put(display_name, value);

					StringBuffer users_list = new StringBuffer();

					users_list.append(TextUtils.join(", ",
							selectedPartners.keySet()));

					EditText edtTo = (EditText) findViewById(R.id.edtMessageTo);
					edtTo.setText(users_list.toString());

					findViewById(R.id.edtMessageBody).requestFocus();
				}
				parent_row.put("partners", partner_ids);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			getActionBar().setTitle("Compose");
			EditText edtTo = (EditText) findViewById(R.id.edtMessageTo);
			edtTo.setInputType(InputType.TYPE_NULL);
			edtTo.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					startActivityAddRecipients();
				}
			});
		}

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
						file_uris.remove(position);
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
						startActivityAddRecipients();
					}
				});

		handleIntentFilter(getIntent());
	}

	private List<TagsItems> getAllPartners() {
		Res_PartnerDBHelper partners = new Res_PartnerDBHelper(this);
		HashMap<String, Object> records = partners.search(partners,
				new String[] { "oea_name = ?" },
				new String[] { OpenERPAccountManager.currentUser(this)
						.getAndroidName() });
		if (Integer.parseInt(records.get("total").toString()) > 0) {
			ArrayList<TagsItems> rows = new ArrayList<TagsItems>();
			for (HashMap<String, Object> row : (List<HashMap<String, Object>>) records
					.get("records")) {
				rows.add(new TagsItems(Integer.parseInt(row.get("id")
						.toString()), row.get("name").toString(), row.get(
						"email").toString(), row.get("image_small").toString()));
			}
			getPartnersFromServer();
			return rows;
		} else {
			return new ArrayList<TagsItems>();
		}
	}

	public Boolean getPartnersFromServer() {
		boolean flag = true;
		Res_PartnerDBHelper res_partners = new Res_PartnerDBHelper(this);
		OEHelper oe = res_partners.getOEInstance();
		try {
			ArrayList<Fields> cols = res_partners.getServerColumns();
			JSONObject fields = new JSONObject();
			for (Fields field : cols) {
				fields.accumulate("fields", field.getName());
			}
			JSONObject domain = new JSONObject();
			JSONArray ids = JSONDataHelper.intArrayToJSONArray(oe
					.getAllIds(res_partners));

			domain.accumulate("domain", new JSONArray("[[\"id\", \"not in\", "
					+ ids.toString() + "]]"));

			// oe.debugMode(true);
			JSONObject result = oe.search_read("res.partner", fields, domain,
					0, 0, null, null);

			for (int i = 0; i < result.getInt("length"); i++) {
				JSONObject row = result.getJSONArray("records")
						.getJSONObject(i);

				int id = row.getInt("id");
				final TagsItems item = new TagsItems(id, row.getString("name")
						.toString(), row.getString("email").toString(),
						row.getString("image_small"));
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						parters.add(item);
					}
				});

			}
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}
		partner_adapter.notifyDataSetChanged();
		return flag;

	}

	public void startActivityAddRecipients() {
		Intent intent = new Intent(MessageComposeActivty.this,
				MessageRecipientActivity.class);
		intent.putExtra("selected_ids", selectedPartners);
		startActivityForResult(intent, ADD_RECIPIENT);
	}

	public JSONArray getPartnersOfMessage(String message_id) {
		Res_PartnerDBHelper partners = new Res_PartnerDBHelper(
				MainActivity.context);
		String oea_name = OpenERPAccountManager.currentUser(
				MainActivity.context).getAndroidName();
		List<HashMap<String, Object>> records = partners
				.executeSQL(
						"SELECT id,name,oea_name FROM res_partner where id in (select res_partner_id from mail_message_res_partner_rel where mail_message_id = ? and oea_name = ?) and oea_name = ?",
						new String[] { message_id, oea_name, oea_name });
		JSONArray names = new JSONArray();
		if (records.size() > 0) {
			for (HashMap<String, Object> row : records) {
				JSONArray rec = new JSONArray();
				rec.put(row.get("id"));
				rec.put(row.get("name"));
				names.put(rec);
			}
		}
		return names;
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
					File fileData = new File(file.getPath());
					ContentValues values = new ContentValues();
					values.put("datas_fname", getFilenameFromUri(file));
					values.put("res_model", "mail.compose.message");
					values.put("company_id", scope.User().getCompany_id());
					values.put("type", "binary");
					values.put("res_id", 0);
					values.put("file_size", fileData.length());
					values.put("db_datas", Base64Helper.fileUriToBase64(file,
							getContentResolver()));
					values.put("name", getFilenameFromUri(file));
					int newId = attachment.create(attachment, values);
					newAttachmentIds.put(newId);
				}

				// TASK: sending mail
				HashMap<String, Object> values = new HashMap<String, Object>();
				values.put("subject", subject);
				if (is_note_body) {
					values.put("body", Html.toHtml(edtBody.getText()));
				} else {
					values.put("body", body);
				}
				values.put("partner_ids", getPartnersId());
				values.put("attachment_ids", newAttachmentIds);

				if (is_reply) {
					SendMailMessageReply sendMessageRply = new SendMailMessageReply(
							values);
					sendMessageRply.execute((Void) null);
				} else {
					SendMailMessage sendMessage = new SendMailMessage(values);
					sendMessage.execute((Void) null);
				}

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

		// note.note send as mail
		if (intent.hasExtra("note_body")) {
			EditText edtBody = (EditText) findViewById(R.id.edtMessageBody);
			String body = intent.getExtras().getString("note_body");
			edtBody.setText(HTMLHelper.stringToHtml(body));
			is_note_body = true;
		}

	}

	/**
	 * getting real path from attachment URI.
	 * 
	 * @param contentUri
	 * @return
	 */
	private String getFilenameFromUri(Uri contentUri) {
		String filename = "unknown";
		if (contentUri.getScheme().toString().compareTo("content") == 0) {
			Cursor cursor = getContentResolver().query(contentUri, null, null,
					null, null);
			if (cursor.moveToFirst()) {
				int column_index = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				filename = cursor.getString(column_index);
				File fl = new File(filename);
				filename = fl.getName();
			}
		} else if (contentUri.getScheme().compareTo("file") == 0) {
			filename = contentUri.getLastPathSegment().toString();
		} else {
			filename = filename + "_" + contentUri.getLastPathSegment();
		}
		return filename;
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
		int row_id = 1;// Integer.parseInt(uri.getLastPathSegment().toString());
		for (Uri uri : file_uris) {
			// File file = new File(uri.getPath());
			HashMap<String, Object> data = new HashMap<String, Object>();
			data.put("name", getFilenameFromUri(uri));
			OEListViewRows row = new OEListViewRows(row_id, data);
			attachments.add(row);
			lstAttachmentAdapter.refresh(attachments);
			row_id++;
		}
	}

	class SendMailMessage extends AsyncTask<Void, Void, Boolean> {
		HashMap<String, Object> values = new HashMap<String, Object>();

		public SendMailMessage(HashMap<String, Object> values) {
			this.values = values;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
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

	class SendMailMessageReply extends AsyncTask<Void, Void, Boolean> {
		HashMap<String, Object> values = new HashMap<String, Object>();

		public SendMailMessageReply(HashMap<String, Object> values) {
			this.values = values;
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			// Message Details
			String subject = values.get("subject").toString();
			String body = values.get("body").toString();
			try {
				JSONArray partner_ids = new JSONArray(values.get("partner_ids")
						.toString());
				JSONArray attachment_ids = new JSONArray(values.get(
						"attachment_ids").toString());
				MessageDBHelper message = new MessageDBHelper(
						MainActivity.context);
				OEHelper oe = message.getOEInstance();
				ContentValues values = new ContentValues();

				JSONArray arguments = new JSONArray("[false]");
				String crate_date = OEDate.getDate();
				JSONObject kwargs = new JSONObject();
				kwargs.put("body", body);
				kwargs.put("subject", subject);
				kwargs.put("date", crate_date);
				kwargs.put("parent_id", message_id);
				kwargs.put("attachment_ids", attachment_ids);
				kwargs.put("partner_ids", partner_ids);

				JSONObject oecontext = new JSONObject();
				String model = parent_row.get("model").toString();

				oecontext
						.put("default_model",
								(!model.equals("mail.thread") ? (model
										.equals("false") ? false : model)
										: false));
				oecontext
						.put("default_res_id",
								(parent_row.get("res_id").toString()
										.equals("0") ? false : parent_row
										.get("res_id")));
				oecontext.put("default_parent_id", message_id);
				oecontext.put("mail_post_autofollow", true);
				oecontext.put("mail_post_autofollow_partner_ids",
						new JSONArray());

				kwargs.put("context", oecontext);
				kwargs.put("type", "comment");
				kwargs.put("content_subtype", "plaintext");
				kwargs.put("subtype", "mail.mt_comment");

				values.put("type", "comment");
				values.put("body", body);
				values.put("parent_id", message_id);
				values.put("attachment_ids", attachment_ids.toString());
				values.put("email_from", "false");
				values.put("record_name", "false");
				values.put("to_read", "false");
				values.put("author_id", scope.User().getPartner_id());
				values.put("model", oecontext.getString("default_model"));
				values.put("res_id", oecontext.getString("default_res_id"));
				values.put("date", crate_date);
				values.put("starred", "false");
				values.put("partner_ids", partner_ids.toString());
				oe.updateKWargs(kwargs);
				JSONObject result = oe.call_kw("mail.thread", "message_post",
						arguments);
				values.put("id", result.getString("result"));
				values.put("has_voted", "false");
				values.put("vote_nb", 0);
				int newid = message.create(message, values);

				String query = "select t1.id as message_id , t1.*, t2.name, t2.image_small, t2.email from mail_message t1, res_partner t2 where (t1.id = ? or t1.parent_id = ?) and (t2.id = t1.author_id or t1.author_id = 'false') group by t1.id order by t1.id desc";
				List<HashMap<String, Object>> records = message.executeSQL(
						query,
						new String[] { String.valueOf(newid),
								String.valueOf(newid) });

				HashMap<String, Object> row = new HashMap<String, Object>();
				row.put("total", records.size());
				row.put("records", records);
				if ((Integer) row.get("total") > 0) {

					List<HashMap<String, Object>> rows_detail = (List<HashMap<String, Object>>) row
							.get("records");
					for (HashMap<String, Object> row_detail : rows_detail) {

						int msg_id = Integer.parseInt(row_detail.get(
								"message_id").toString());
						String key = row_detail.get("parent_id").toString();
						OEListViewRows rowObj = null;
						String[] ids = new MessageDetail()
								.getPartnersOfMessage(row_detail.get(
										"message_id").toString());
						String partners = "nobody";
						if (ids != null) {
							partners = TextUtils.join(", ", ids);
						}
						row_detail.put("partners", partners);

						rowObj = new OEListViewRows(msg_id, row_detail);

					}
				}
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			if (success) {
				Toast.makeText(getApplicationContext(),
						"Message sent succussfull.", Toast.LENGTH_LONG).show();
				selectedPartners = new HashMap<String, Object>();
				setResult(RESULT_OK);
				finish();
			} else {
				Toast.makeText(getApplicationContext(),
						"Unable to send message.", Toast.LENGTH_LONG).show();
			}
		}

	}
}
