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
package com.openerp.addons.message;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import openerp.OEArguments;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.openerp.R;
import com.openerp.base.ir.Ir_AttachmentDBHelper;
import com.openerp.base.res.ResPartnerDB;
import com.openerp.orm.OEDataRow;
import com.openerp.orm.OEHelper;
import com.openerp.orm.OEM2MIds;
import com.openerp.orm.OEM2MIds.Operation;
import com.openerp.orm.OEValues;
import com.openerp.support.OEUser;
import com.openerp.support.listview.OEListAdapter;
import com.openerp.support.listview.OEListAdapter.RowFilterTextListener;
import com.openerp.util.Base64Helper;
import com.openerp.util.HTMLHelper;
import com.openerp.util.OEDate;
import com.openerp.util.controls.OETextView;
import com.openerp.util.tags.MultiTagsTextView.TokenListener;
import com.openerp.util.tags.TagsView;
import com.openerp.util.tags.TagsView.CustomTagViewListener;

public class MessageComposeActivity extends Activity implements TokenListener {

	public static final String TAG = "com.openerp.addons.message.MessageComposeActivity";
	public static final Integer PICKFILE_RESULT_CODE = 1;
	Context mContext = null;

	Boolean isReply = false;
	Boolean isQuickCompose = false;
	Integer mParentMessageId = 0;
	OEDataRow mMessageRow = null;

	HashMap<String, OEDataRow> mSelectedPartners = new HashMap<String, OEDataRow>();

	/**
	 * DBs
	 */
	ResPartnerDB mPartnerDB = null;
	MessageDB mMessageDB = null;
	PartnerLoader mPartnerLoader = null;

	enum AttachmentType {
		IMAGE, FILE
	}

	/**
	 * Attachment URIs
	 */
	List<Uri> mAttachmentURIs = new ArrayList<Uri>();
	List<Object> mAttachments = new ArrayList<Object>();
	GridView mAttachmentGridView = null;
	OEListAdapter mAttachmentAdapter = null;

	/**
	 * Controls & Adapters
	 */
	TagsView mPartnerTagsView = null;
	OEListAdapter mPartnerTagsAdapter = null;
	List<Object> mTagsPartners = new ArrayList<Object>();
	EditText edtSubject = null, edtBody = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message_compose);
		mContext = this;
		initDBs();
		initActionbar();
		handleIntent();
		initControls();
		checkForContact();
	}

	private void initControls() {
		if (!isReply) {
			mPartnerLoader = new PartnerLoader();
			mPartnerLoader.execute();
		}

		mPartnerTagsView = (TagsView) findViewById(R.id.receipients_view);
		mPartnerTagsView.setCustomTagView(new CustomTagViewListener() {

			@Override
			public View getViewForTags(LayoutInflater layoutInflater,
					Object object, ViewGroup tagsViewGroup) {
				OEDataRow row = (OEDataRow) object;
				View mView = layoutInflater.inflate(
						R.layout.fragment_message_receipient_tag_layout, null);
				OETextView txvSubject = (OETextView) mView
						.findViewById(R.id.txvTagSubject);
				txvSubject.setText(row.getString("name"));
				ImageView imgPic = (ImageView) mView
						.findViewById(R.id.imgTagImage);
				if (!row.getString("image_small").equals("false")) {
					imgPic.setImageBitmap(Base64Helper.getBitmapImage(
							getApplicationContext(),
							row.getString("image_small")));
				}
				return mView;
			}
		});
		mPartnerTagsAdapter = new OEListAdapter(this,
				R.layout.tags_view_partner_item_layout, mTagsPartners) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View mView = convertView;
				if (mView == null) {
					mView = getLayoutInflater().inflate(getResource(), parent,
							false);
				}
				OEDataRow row = (OEDataRow) mTagsPartners.get(position);
				TextView txvSubject = (TextView) mView
						.findViewById(R.id.txvSubject);
				TextView txvSubSubject = (TextView) mView
						.findViewById(R.id.txvSubSubject);
				ImageView imgPic = (ImageView) mView
						.findViewById(R.id.imgReceipientPic);
				txvSubject.setText(row.getString("name"));
				if (!row.getString("email").equals("false")) {
					txvSubSubject.setText(row.getString("email"));
				} else {
					txvSubSubject.setText("No email");
				}
				if (!row.getString("image_small").equals("false")) {
					imgPic.setImageBitmap(Base64Helper.getBitmapImage(mContext,
							row.getString("image_small")));
				}
				return mView;
			}
		};
		mPartnerTagsAdapter
				.setRowFilterTextListener(new RowFilterTextListener() {

					@Override
					public String filterCompareWith(Object object) {
						OEDataRow row = (OEDataRow) object;
						return row.getString("name") + " "
								+ row.getString("email");
					}
				});
		mPartnerTagsView.setAdapter(mPartnerTagsAdapter);
		mPartnerTagsView.setPrefix("To: ");
		mPartnerTagsView.allowDuplicates(false);
		mPartnerTagsView.setTokenListener(this);

		// Attachment View
		mAttachmentGridView = (GridView) findViewById(R.id.lstAttachments);
		mAttachmentAdapter = new OEListAdapter(this,
				R.layout.activity_message_compose_attachment_file_view_item,
				mAttachments) {
			@Override
			public View getView(final int position, View convertView,
					ViewGroup parent) {
				OEDataRow row = (OEDataRow) mAttachments.get(position);
				View mView = convertView;
				if (mView == null)
					mView = getLayoutInflater().inflate(getResource(), parent,
							false);
				TextView txvFileName = (TextView) mView
						.findViewById(R.id.txvFileName);
				txvFileName.setText(row.getString("name"));

				ImageView imgAttachmentImg = (ImageView) mView
						.findViewById(R.id.imgAttachmentFile);
				if (row.getString("type").equals("file")) {
					imgAttachmentImg
							.setImageResource(R.drawable.file_attachment);
				} else {
					imgAttachmentImg.setImageURI((Uri) row.get("uri"));
				}
				mView.findViewById(R.id.imgBtnRemoveAttachment)
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								mAttachmentURIs.remove(position);
								mAttachments.remove(position);
								mAttachmentAdapter
										.notifiyDataChange(mAttachments);
							}
						});
				return mView;
			}
		};
		mAttachmentGridView.setAdapter(mAttachmentAdapter);

		// Edittext
		edtSubject = (EditText) findViewById(R.id.edtMessageSubject);
		edtBody = (EditText) findViewById(R.id.edtMessageBody);
	}

	private void initDBs() {
		mPartnerDB = new ResPartnerDB(mContext);
		mMessageDB = new MessageDB(mContext);
	}

	private void checkForContact() {
		Intent intent = getIntent();
		handleIntentFilter(intent);
		if (intent.getData() != null) {
			Cursor cursor = getContentResolver().query(intent.getData(), null,
					null, null, null);
			if (cursor.moveToFirst()) {
				int partner_id = cursor.getInt(cursor.getColumnIndex("data2"));
				OEDataRow row = mPartnerDB.select(partner_id);
				mSelectedPartners.put("key_" + row.getString("id"), row);
				mPartnerTagsView.addObject(row);
				isQuickCompose = true;
			}
		}

		if (isReply) {
			mMessageRow = mMessageDB.select(mParentMessageId);
			List<OEDataRow> partners = mMessageRow.getM2MRecord("partner_ids")
					.browseEach();
			if (partners != null) {
				for (OEDataRow partner : partners) {
					mSelectedPartners.put("key_" + partner.getString("id"),
							partner);
					mPartnerTagsView.addObject(partner);
				}
			}
			edtSubject.setText("Re: " + mMessageRow.getString("subject"));
			edtBody.requestFocus();
		}
	}

	private void handleIntent() {
		Intent intent = getIntent();
		String title = "Compose";
		if (intent.hasExtra("send_reply")) {
			isReply = true;
			mParentMessageId = intent.getExtras().getInt("message_id");
			title = "Reply";
		}
		setTitle(title);
	}

	private void initActionbar() {
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_message_compose_activty, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.menu_message_compose_add_attachment_images:
			requestForAttachmentIntent(AttachmentType.IMAGE);
			return true;
		case R.id.menu_message_compose_add_attachment_files:
			requestForAttachmentIntent(AttachmentType.FILE);
			return true;
		case R.id.menu_message_compose_send:
			edtSubject.setError(null);
			edtBody.setError(null);
			if (mSelectedPartners.size() == 0)
				Toast.makeText(mContext, "Select atleast one receiptent",
						Toast.LENGTH_LONG).show();
			else if (TextUtils.isEmpty(edtSubject.getText())) {
				edtSubject.setError("Provide Message Subject !");
			} else if (TextUtils.isEmpty(edtBody.getText())) {
				edtBody.setError("Provide Message Body !");
			} else {
				Toast.makeText(this, "Sending message...", Toast.LENGTH_LONG)
						.show();
				SendMessage sendMessage = new SendMessage();
				sendMessage.execute();
				if (isQuickCompose)
					finish();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	class SendMessage extends AsyncTask<Void, Void, Void> {
		OEHelper mOE = null;
		boolean isConnection = true;
		String mToast = "";
		int newMessageId = 0;

		public SendMessage() {
			mOE = mMessageDB.getOEInstance();
			if (mOE == null)
				isConnection = false;
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (isConnection) {
				Ir_AttachmentDBHelper attachment = new Ir_AttachmentDBHelper(
						mContext);
				JSONArray attachmentIds = new JSONArray();
				OEHelper oe = attachment.getOEInstance();
				List<Integer> attachment_ids_list = new ArrayList<Integer>();
				for (Uri uri : mAttachmentURIs) {
					File fileData = new File(uri.getPath());
					String filename = getFilenameFromUri(uri);
					OEValues values = new OEValues();
					values.put("datas_fname", filename);
					values.put("res_model", "mail.compose.message");
					values.put("company_id", OEUser.current(mContext)
							.getCompany_id());
					values.put("type", "binary");
					values.put("res_id", 0);
					values.put("file_size", fileData.length());
					values.put("db_datas", Base64Helper.fileUriToBase64(uri,
							getContentResolver()));
					values.put("name", filename);
					if (oe != null) {
						long newId = oe.create(values);
						attachmentIds.put(newId);
						attachment_ids_list.add((int) newId);
					}
				}
				try {
					OEDataRow user = new ResPartnerDB(mContext).select(OEUser
							.current(mContext).getPartner_id());
					OEArguments args = new OEArguments();

					// Partners
					JSONArray partners = new JSONArray();
					List<Integer> partner_ids_list = new ArrayList<Integer>();
					for (String key : mSelectedPartners.keySet()) {
						partners.put(mSelectedPartners.get(key).getInt("id"));
						partner_ids_list.add(mSelectedPartners.get(key).getInt(
								"id"));
					}
					JSONArray partner_ids = new JSONArray();
					if (partners.length() > 0) {
						partner_ids.put(6);
						partner_ids.put(false);
						partner_ids.put(partners);
					}

					// attachment ids
					JSONArray attachment_ids = new JSONArray();
					if (attachmentIds.length() > 0) {
						attachment_ids.put(6);
						attachment_ids.put(false);
						attachment_ids.put(attachmentIds);
					}

					if (!isReply) {
						mToast = "Message sent.";
						JSONObject arguments = new JSONObject();
						arguments.put("composition_mode", "comment");
						arguments.put("model", false);
						arguments.put("parent_id", false);
						String email_from = user.getString("name") + " <"
								+ user.getString("email") + ">";
						arguments.put("email_from", email_from);
						arguments.put("subject", edtSubject.getText()
								.toString());
						arguments.put("body", edtBody.getText().toString());
						arguments.put("post", true);
						arguments.put("notify", false);
						arguments.put("same_thread", true);
						arguments.put("use_active_domain", false);
						arguments.put("reply_to", false);
						arguments.put("res_id", 0);

						if (partner_ids.length() > 0)
							arguments.put("partner_ids", new JSONArray("["
									+ partner_ids.toString() + "]"));
						else
							arguments.put("partner_ids", new JSONArray());

						if (attachment_ids.length() > 0)
							arguments.put("attachment_ids", new JSONArray("["
									+ attachment_ids.toString() + "]"));
						else
							arguments.put("attachment_ids", new JSONArray());
						arguments.put("template_id", false);

						JSONObject kwargs = new JSONObject();
						kwargs.put("context",
								mOE.openERP().updateContext(new JSONObject()));

						args.add(arguments);
						String model = "mail.compose.message";

						// Creating compose message
						int id = (Integer) mOE.call_kw(model, "create", args,
								null, kwargs);

						// Resetting kwargs
						args = new OEArguments();
						args.add(new JSONArray().put(id));
						args.add(mOE.openERP().updateContext(new JSONObject()));

						// Sending mail
						mOE.call_kw(model, "send_mail", args, null, null);
						mOE.syncWithServer();
					} else {
						mToast = "Message reply sent.";
						String model = "mail.thread";
						String method = "message_post";
						args = new OEArguments();
						args.add(false);

						JSONObject context = new JSONObject();
						int res_id = mMessageRow.getInt("res_id");
						String res_model = mMessageRow.getString("model");
						context.put("default_model",
								(res_model.equals("false") ? false : res_model));
						context.put("default_res_id", (res_id == 0) ? false
								: res_id);
						context.put("default_parent_id", mParentMessageId);
						context.put("mail_post_autofollow", true);
						context.put("mail_post_autofollow_partner_ids",
								new JSONArray());

						JSONObject kwargs = new JSONObject();
						kwargs.put("context", context);
						kwargs.put("subject", edtSubject.getText().toString());
						kwargs.put("body", edtBody.getText().toString());
						kwargs.put("parent_id", mParentMessageId);
						kwargs.put("attachment_ids", attachmentIds);
						if (partner_ids.length() > 0)
							kwargs.put("partner_ids", new JSONArray("["
									+ partner_ids.toString() + "]"));
						else
							kwargs.put("partner_ids", new JSONArray());
						newMessageId = (Integer) mOE.call_kw(model, method,
								args, null, kwargs);

						// Creating local entry
						OEValues values = new OEValues();

						OEM2MIds partnerIds = new OEM2MIds(Operation.ADD,
								partner_ids_list);
						values.put("id", newMessageId);
						values.put("partner_ids", partnerIds);
						values.put("subject", edtSubject.getText().toString());
						values.put("type", "comment");
						values.put("body", edtBody.getText().toString());
						values.put("email_from", false);
						values.put("parent_id", mParentMessageId);
						values.put("record_name", false);
						values.put("to_read", false);
						values.put("author_id", user.getInt("id"));
						values.put("model", res_model);
						values.put("res_id", res_id);
						values.put("date", OEDate.getDate());
						values.put("has_voted", false);
						values.put("vote_nb", 0);
						values.put("starred", false);
						OEM2MIds attachment_Ids = new OEM2MIds(Operation.ADD,
								attachment_ids_list);
						values.put("attachment_ids", attachment_Ids);
						newMessageId = (int) mMessageDB.create(values);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (!isConnection) {
				Toast.makeText(mContext, "No Connection", Toast.LENGTH_LONG)
						.show();
			} else {
				Toast.makeText(mContext, mToast, Toast.LENGTH_LONG).show();
				Intent intent = new Intent();
				intent.putExtra("new_message_id", newMessageId);
				setResult(RESULT_OK, intent);
				finish();
			}
		}

	}

	private void requestForAttachmentIntent(AttachmentType type) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		switch (type) {
		case FILE:
			intent.setType("application/file");
			break;
		case IMAGE:
			intent.setType("image/*");
			break;
		}
		try {
			startActivityForResult(intent, PICKFILE_RESULT_CODE);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, "No Activity found to handle intent.",
					Toast.LENGTH_LONG).show();
		}
	}

	class PartnerLoader extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			mTagsPartners.clear();
			// Loading records from server
			OEHelper oe = mPartnerDB.getOEInstance();
			if (oe != null) {
				mTagsPartners.addAll(oe.search_read());
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mPartnerTagsAdapter.notifiyDataChange(mTagsPartners);
		}

	}

	@Override
	public void onTokenAdded(Object token, View view) {
		OEDataRow row = (OEDataRow) token;
		mSelectedPartners.put("key_" + row.getString("id"), row);
	}

	@Override
	public void onTokenSelected(Object token, View view) {

	}

	@Override
	public void onTokenRemoved(Object token) {
		OEDataRow row = (OEDataRow) token;
		if (!isReply)
			mSelectedPartners.remove("key_" + row.getString("id"));
		else
			mPartnerTagsView.addObject(token);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICKFILE_RESULT_CODE && resultCode == RESULT_OK) {
			String FilePath = data.getDataString();
			Uri fileUri = Uri.parse(FilePath);
			mAttachmentURIs.add(fileUri);
			handleIntentFilter(data);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Handle message intent filter for attachments
	 * 
	 * @param intent
	 */
	private void handleIntentFilter(Intent intent) {
		String action = intent.getAction();
		String type = intent.getType();

		// Single attachment
		if (Intent.ACTION_SEND.equals(action) && type != null) {
			Uri fileUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
			mAttachmentURIs.add(fileUri);
		}

		// Multiple Attachments
		if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
			ArrayList<Uri> fileUris = intent
					.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
			mAttachmentURIs.addAll(fileUris);

		}
		// note.note send as mail
		if (intent.hasExtra("note_body")) {
			EditText edtBody = (EditText) findViewById(R.id.edtMessageBody);
			String body = intent.getExtras().getString("note_body");
			edtBody.setText(HTMLHelper.stringToHtml(body));
		}

		if (intent.hasExtra("android.intent.extra.TEXT")) {
			edtBody.setText(intent.getExtras().getString(
					"android.intent.extra.TEXT"));
		} else {
			handleReceivedFile();
		}

	}

	private void handleReceivedFile() {
		mAttachments.clear();
		for (Uri uri : mAttachmentURIs) {
			ContentResolver cR = getContentResolver();
			String type = cR.getType(uri);
			OEDataRow data = new OEDataRow();
			data.put("name", getFilenameFromUri(uri));
			data.put("uri", uri);
			data.put("type", "file");
			if (type.contains("image/")) {
				data.put("type", "image");
			}
			mAttachments.add(data);
			mAttachmentAdapter.notifiyDataChange(mAttachments);
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

	@Override
	protected void onPause() {
		super.onPause();
		if (mPartnerLoader != null)
			mPartnerLoader.cancel(true);
	}
}
