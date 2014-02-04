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

package com.openerp.addons.messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.openerp.MainActivity;
import com.openerp.R;
import com.openerp.auth.OpenERPAccountManager;
import com.openerp.base.ir.Ir_AttachmentDBHelper;
import com.openerp.base.res.Res_PartnerDBHelper;
import com.openerp.orm.OEDataRow;
import com.openerp.orm.OEHelper;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.JSONDataHelper;
import com.openerp.support.OEArgsHelper;
import com.openerp.support.OEUser;
import com.openerp.support.OpenERPServerConnection;
import com.openerp.support.listview.OEListAdapter;
import com.openerp.support.listview.OEListViewRow;
import com.openerp.util.Base64Helper;
import com.openerp.util.OEBinaryDownloadHelper;
import com.openerp.util.OEDate;
import com.openerp.util.OEFileSizeHelper;
import com.openerp.util.contactview.OEContactView;
import com.openerp.util.controls.OETextView;
import com.openerp.util.drawer.DrawerItem;

/**
 * The Class MessageDetail.
 */
public class MessageDetail extends BaseFragment implements OnClickListener {

	/** The root view. */
	View rootView = null;
	private static final int MESSAGE_REPLY = 3;

	OEListAdapter mListAdapter = null;
	List<Object> mMessageObjects = new ArrayList<Object>();
	ListView mMessageListView = null;
	MessagesLoader mMessageLoader = null;

	ReadUnreadOperation mReadUnreadOperation = null;
	StarredOperation mStarredOperation = null;
	/** The message_id. */
	int message_id = 0;

	/** The parent_row. */
	OEDataRow parent_row = null;
	public static String oea_name = null;

	int[] starred_drawables = new int[] { R.drawable.ic_action_starred,
			R.drawable.ic_action_unstarred };

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		setHasOptionsMenu(true);
		scope = new AppScope(this);
		db = (MessageDBHelper) getModel();

		rootView = inflater.inflate(R.layout.fragment_message_detail_view,
				container, false);
		oea_name = OpenERPAccountManager.currentUser(MainActivity.context)
				.getAndroidName();
		setupListView();
		return rootView;
	}

	private void setupListView() {
		mMessageListView = (ListView) rootView
				.findViewById(R.id.lstMessageDetail);
		mListAdapter = new OEListAdapter(getActivity(),
				R.layout.fragment_message_detail_listview_items,
				mMessageObjects) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View mView = convertView;
				if (mView == null) {
					mView = getActivity().getLayoutInflater().inflate(
							getResource(), parent, false);
				}
				handleView(position, mView, mMessageObjects.get(position));

				return mView;
			}
		};
		mMessageListView.setAdapter(mListAdapter);
	}

	private View handleView(final int position, final View mView,
			final Object obj) {
		OEListViewRow row = (OEListViewRow) obj;
		TextView txvAuthor, txvEmail, txvTime, txvTo;
		final TextView txvVoteNumber;
		txvAuthor = (TextView) mView.findViewById(R.id.txvMessageAuthor);
		txvEmail = (TextView) mView.findViewById(R.id.txvAuthorEmail);
		txvTime = (TextView) mView.findViewById(R.id.txvTime);
		txvTo = (TextView) mView.findViewById(R.id.txvTo);
		txvVoteNumber = (TextView) mView.findViewById(R.id.txvmessageVotenb);

		String author = row.getRow_data().getString("email_from");
		String email = author;
		if (author.equals("false")) {
			author = row.getRow_data().getString("name");
			email = row.getRow_data().getString("email");
		}
		txvAuthor.setText(author);
		txvEmail.setText(email);

		txvTime.setText(OEDate.getDate(row.getRow_data().getString("date"),
				TimeZone.getDefault().getID(), "MMM dd, yyyy,  hh:mm a"));

		txvTo.setText(row.getRow_data().getString("partners"));

		/* Handling vote control */
		txvVoteNumber.setText(row.getRow_data().getString("vote_nb"));
		int vote_nb = row.getRow_data().getInt("vote_nb");
		if (vote_nb == 0) {
			txvVoteNumber.setText("");
		}
		boolean hasVoted = row.getRow_data().getBoolean("has_voted");
		if (!hasVoted) {
			txvVoteNumber.setCompoundDrawablesWithIntrinsicBounds(
					getResources().getDrawable(
							R.drawable.ic_thumbs_up_unselected_dark_tablet),
					null, null, null);
		} else {
			txvVoteNumber.setCompoundDrawablesWithIntrinsicBounds(
					getResources().getDrawable(
							R.drawable.ic_thumbs_up_selected_dark_tablet),
					null, null, null);
		}
		txvVoteNumber.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				handleVoteToggle(position, txvVoteNumber, obj);
			}
		});

		WebView webView = (WebView) mView.findViewById(R.id.webViewMessageBody);
		webView.loadData(row.getRow_data().getString("body"), "text/html",
				"UTF-8");

		// Handling attachment for each message
		handleAttachments(obj, mView);

		ImageView imgUserPicture, imgBtnStar;
		imgUserPicture = (ImageView) mView.findViewById(R.id.imgUserPicture);
		imgBtnStar = (ImageView) mView.findViewById(R.id.imgBtnStar);

		// Handling starred event
		final boolean starred = row.getRow_data().getBoolean("starred");
		imgBtnStar.setImageResource((starred) ? starred_drawables[0]
				: starred_drawables[1]);
		imgBtnStar.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Handling Starred click event
				mStarredOperation = new StarredOperation(position,
						(starred) ? false : true);
				mStarredOperation.execute();
			}
		});

		imgUserPicture.setImageBitmap(Base64Helper.getBitmapImage(
				getActivity(), row.getRow_data().getString("image")));

		// Handling reply button click event
		mView.findViewById(R.id.imgBtnReply).setOnClickListener(this);

		// handling contact view
		OEContactView oe_contactView = (OEContactView) mView
				.findViewById(R.id.imgUserPicture);
		int partner_id = Integer.parseInt(row.getRow_data().get("partner_id")
				.toString());
		oe_contactView.assignPartnerId(partner_id);
		return mView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openerp.support.FragmentHelper#databaseHelper(android.content.Context
	 * )
	 */
	@Override
	public Object databaseHelper(Context context) {
		return new MessageDBHelper(context);
	}

	@Override
	public void onStart() {
		super.onStart();
		Bundle bundle = getArguments();
		if (bundle != null) {
			if (bundle.containsKey("message_id")) {
				message_id = bundle.getInt("message_id");
				mMessageLoader = new MessagesLoader(message_id);
				mMessageLoader.execute();
			}
		}
	}

	/**
	 * Gets the partners of message.
	 * 
	 * @param message_id
	 *            the message_id
	 * @return the partners of message
	 */
	public String[] getPartnersOfMessage(String message_id) {
		Res_PartnerDBHelper partners = new Res_PartnerDBHelper(
				MainActivity.context);
		oea_name = OpenERPAccountManager.currentUser(MainActivity.context)
				.getAndroidName();
		List<OEDataRow> records = partners
				.executeSQL(
						"SELECT id,name,oea_name FROM res_partner where id in (select res_partner_id from mail_message_res_partner_rel where mail_message_id = ? and oea_name = ?) and oea_name = ?",
						new String[] { message_id, oea_name, oea_name });
		List<String> names = new ArrayList<String>();
		if (records.size() > 0) {
			for (OEDataRow row : records) {
				if (row.getString("name").equals(
						OEUser.current(getActivity()).getPartner_id())) {
					names.add("me");
				} else {
					names.add(row.get("name").toString());
				}
			}
		} else {
			names.add("nobody");
		}
		return names.toArray(new String[names.size()]);
	}

	/**
	 * Gets the attachments of message.
	 * 
	 * @param message_id
	 *            the message_id
	 * @return the partners of message
	 */
	public List<OEListViewRow> getAttachmentsOfMessage(String message_id) {
		List<OEListViewRow> lists = new ArrayList<OEListViewRow>();
		Ir_AttachmentDBHelper attachments = new Ir_AttachmentDBHelper(
				MainActivity.context);
		oea_name = OpenERPAccountManager.currentUser(MainActivity.context)
				.getAndroidName();
		List<OEDataRow> records = attachments
				.executeSQL(
						"SELECT * FROM ir_attachment where id in (select ir_attachment_id from mail_message_ir_attachment_rel where mail_message_id = ? and oea_name = ?) and oea_name = ?",
						new String[] { message_id, oea_name, oea_name });
		if (records.size() > 0) {
			for (OEDataRow row : records) {
				int attachment_id = row.getInt("id");
				OEListViewRow list_row = new OEListViewRow(attachment_id, row);
				lists.add(list_row);
			}
		}
		return lists;
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		return null;
	}

	/* Method for Make Message as TODO */
	public boolean markAsTodo(OEArgsHelper messageIds, boolean markFlag) {
		boolean flag = false;
		OEHelper openerp = getOEInstance();

		OEArgsHelper args = new OEArgsHelper();

		// Param 1 : message_ids list
		args.addArg(messageIds.getArgs());

		// Param 2 : starred - boolean value
		args.addArg(markFlag);

		// Param 3 : create_missing - If table does not contain any value for
		// this row than create new one
		args.addArg(true);

		// Creating Local Database Requirement Values
		ContentValues values = new ContentValues();
		String value = (markFlag) ? "true" : "false";
		values.put("starred", value);

		flag = openerp.callServerMethod(getModel(), "set_message_starred",
				args.getArgs(), values,
				JSONDataHelper.jsonArrayTointArray(messageIds.getArgs()));
		return flag;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateOptionsMenu(android.view.Menu,
	 * android.view.MenuInflater)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_fragment_message_detail, menu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onOptionsItemSelected(android.view.MenuItem
	 * )
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection
		switch (item.getItemId()) {
		case R.id.menu_message_detail_read:
			mReadUnreadOperation = new ReadUnreadOperation(false, message_id);
			mReadUnreadOperation.execute();
			return true;
		case R.id.menu_message_detail_unread:
			mReadUnreadOperation = new ReadUnreadOperation(true, message_id);
			mReadUnreadOperation.execute();
			return true;
		case R.id.menu_message_compose:
			scope.context().startActivity(
					new Intent(scope.context(), MessageComposeActivty.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case MESSAGE_REPLY:
			if (resultCode == Activity.RESULT_OK) {
				Bundle bundle = new Bundle();
				bundle.putInt("message_id", message_id);
				mMessageLoader = new MessagesLoader(message_id);
				mMessageLoader.execute();
			}
			break;
		}
	}

	public class MessagesLoader extends AsyncTask<Void, Void, Void> {
		int mMessageId = 0;
		String mTitle = "";

		public MessagesLoader(int message_id) {
			mMessageId = message_id;
		}

		@Override
		protected void onPreExecute() {
			scope.main().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					rootView.findViewById(R.id.loadingProgress).setVisibility(
							View.VISIBLE);
				}
			});
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			mMessageObjects.clear();
			String query = "select t1.id as message_id , t1.*, t2.id as partner_id, t2.name, t2.image_small as image, t2.email from mail_message t1, res_partner t2 where (t1.id = ? or t1.parent_id = ?) and (t2.id = t1.author_id or t1.author_id = 'false') group by t1.id order by t1.date desc";
			List<OEDataRow> records = db.executeSQL(query, new String[] {
					String.valueOf(message_id), String.valueOf(message_id) });
			if (records.size() > 0) {
				for (OEDataRow row_detail : records) {
					int msg_id = row_detail.getInt("message_id");
					String key = row_detail.getString("parent_id");
					OEListViewRow rowObj = null;
					String[] ids = getPartnersOfMessage(row_detail
							.getString("message_id"));
					String partners = "nobody";
					if (ids != null) {
						partners = TextUtils.join(", ", ids);
					}
					row_detail.put("partners", partners);
					if (key.equals("false")) {
						// Parent Message
						if (row_detail.getString("author_id").equals("false")) {
							row_detail.put("image", "false");
						}
						rowObj = new OEListViewRow(msg_id, row_detail);
						parent_row = row_detail;
						if (!row_detail.getString("model").equals("false")) {
							mMessageObjects.add(rowObj);
						} else {
							mMessageObjects.add(0, rowObj);
						}
						String sub = rowObj.getRow_data().getString("subject");
						if (sub.equals("false")) {
							sub = rowObj.getRow_data().getString("type");
						}
						mTitle = sub;
						if (row_detail.getString("model").equals("mail.group")) {
							if (UserGroups.menu_color.containsKey("group_"
									+ row_detail.getString("res_id"))) {
								View tagColor = rootView
										.findViewById(R.id.groupColorLine);
								tagColor.setBackgroundColor(UserGroups.menu_color
										.get("group_"
												+ row_detail
														.getString("res_id")));
							}
						}
					} else {
						rowObj = new OEListViewRow(msg_id, row_detail);
						mMessageObjects.add(rowObj);
					}

				}
			}
			return null;

		}

		@Override
		protected void onPostExecute(Void result) {
			scope.main().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					try {
						rootView.findViewById(R.id.loadingProgress)
								.setVisibility(View.GONE);
						mListAdapter.notifiyDataChange(mMessageObjects);
						OETextView txvTitle = (OETextView) rootView
								.findViewById(R.id.txvMessageTitle);
						txvTitle.setText(mTitle);
					} catch (Exception e) {
						e.printStackTrace();
					}
					/*
					 * if (success) { MarkingAsRead read = new
					 * MarkingAsRead(message_id); read.execute((Void) null); }
					 */
				}
			});
		}

	}

	private void handleAttachments(Object obj, View mView) {
		OEListViewRow item = (OEListViewRow) obj;
		/* handling attachments */
		List<OEListViewRow> attachments = getAttachmentsOfMessage(item
				.getRow_id() + "");
		int index = 0;
		if (attachments.size() > 0) {
			LayoutInflater vi = (LayoutInflater) scope.context()
					.getApplicationContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View insertPoint = mView.findViewById(R.id.gridAttachments);
			((ViewGroup) insertPoint).removeAllViews();
			for (OEListViewRow row : attachments) {
				View v = vi.inflate(
						R.layout.fragment_message_detail_attachment_grid_item,
						null, true);
				OETextView txvAttachmentName = (OETextView) v
						.findViewById(R.id.txvFileName);

				txvAttachmentName.setText(row.getRow_data().get("name")
						.toString());
				OETextView txvAttachmentSize = (OETextView) v
						.findViewById(R.id.txvFileSize);
				long fileSize = Long.parseLong(row.getRow_data()
						.get("file_size").toString());
				String file_size = OEFileSizeHelper.readableFileSize(fileSize);
				txvAttachmentSize.setText((file_size.equals("0")) ? " "
						: file_size);

				OETextView txvAttachmentId = (OETextView) v
						.findViewById(R.id.txvAttachmentId);
				txvAttachmentId.setText(String.valueOf(row.getRow_id()));
				((ViewGroup) insertPoint).addView(v, index,
						new ViewGroup.LayoutParams(
								ViewGroup.LayoutParams.MATCH_PARENT,
								ViewGroup.LayoutParams.MATCH_PARENT));
				v.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						int attachment_id = Integer.parseInt(((OETextView) v
								.findViewById(R.id.txvAttachmentId)).getText()
								.toString());
						OEBinaryDownloadHelper binaryDownload = new OEBinaryDownloadHelper();
						binaryDownload.downloadBinary(attachment_id, db);
					}
				});
				index++;

			}

		} else {
			mView.findViewById(R.id.layoutMessageAttachments).setVisibility(
					View.GONE);
		}
	}

	private void handleVoteToggle(int position, TextView view, Object obj) {
		OEListViewRow row = (OEListViewRow) obj;
		int vote_nb = row.getRow_data().getInt("vote_nb");
		boolean hasVoted = row.getRow_data().getBoolean("has_voted");
		MessageVoteToggle voteToggle = new MessageVoteToggle(row.getRow_id(),
				vote_nb, hasVoted);
		String newVote = "";
		boolean btnvoted = false;
		if (hasVoted) {
			newVote = (vote_nb - 1) + "";
			row.getRow_data().put("has_voted", "false");
		} else {
			btnvoted = true;
			newVote = (vote_nb + 1) + "";
			row.getRow_data().put("has_voted", "true");
		}
		row.getRow_data().put("vote_nb", newVote);
		mListAdapter.replaceObjectAtPosition(position, row);
		voteToggle.execute((Void) null);
		view.setText(newVote);
		if (!btnvoted) {
			view.setCompoundDrawablesWithIntrinsicBounds(
					getResources().getDrawable(
							R.drawable.ic_thumbs_up_unselected_dark_tablet),
					null, null, null);
		} else {
			view.setCompoundDrawablesWithIntrinsicBounds(getResources()
					.getDrawable(R.drawable.ic_thumbs_up_selected_dark_tablet),
					null, null, null);
		}
		if (newVote.equals("0")) {
			view.setText("");
		}
	}

	private class MessageVoteToggle extends AsyncTask<Void, Void, Boolean> {
		int message_id = 0;
		int vote_nb = 0;
		boolean has_voted = false;

		public MessageVoteToggle(int message_id, int current_vote_nb,
				boolean has_voted) {
			this.message_id = message_id;
			this.vote_nb = current_vote_nb;
			this.has_voted = has_voted;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			db = new MessageDBHelper(scope.context());
			OEHelper oe = db.getOEInstance();
			try {
				JSONArray args = new JSONArray();
				args.put(message_id);
				oe.call_kw(db.getModelName(), "vote_toggle", new JSONArray("["
						+ args.toString() + "]"));
				ContentValues values = new ContentValues();
				String vote = "false";
				if (!this.has_voted) {
					vote = "true";
					vote_nb = vote_nb + 1;
				} else {
					vote_nb = vote_nb - 1;
				}

				values.put("has_voted", vote);
				values.put("vote_nb", vote_nb);
				return db.write(db, values, message_id, true);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

	}

	// Message reply
	@Override
	public void onClick(View v) {
		Intent composeIntent = new Intent(scope.context(),
				MessageComposeActivty.class);
		composeIntent.putExtra("message_id", message_id);
		composeIntent.putExtra("send_reply", true);
		startActivityForResult(composeIntent, MESSAGE_REPLY);
	}

	/**
	 * Marking each row starred/unstarred in background
	 */
	public class StarredOperation extends AsyncTask<Void, Void, Boolean> {

		boolean mStarred = false;
		ProgressDialog mProgressDialog = null;
		boolean isConnection = true;
		int mPosition = 0;

		public StarredOperation(int position, boolean starred) {
			mPosition = position;
			mStarred = starred;
			try {
				if (!OpenERPServerConnection.isNetworkAvailable(getActivity())) {
					isConnection = false;
				}
			} catch (Exception e) {
				isConnection = false;
			}
			mProgressDialog = new ProgressDialog(getActivity());
			mProgressDialog.setMessage("Working...");
			if (isConnection) {
				mProgressDialog.show();
			}
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			if (!isConnection) {
				return false;
			}
			OEArgsHelper messageIds = new OEArgsHelper();
			OEListViewRow row = (OEListViewRow) mMessageObjects.get(mPosition);
			messageIds.addArg(row.getRow_id());
			OEHelper openerp = getOEInstance();

			OEArgsHelper args = new OEArgsHelper();

			// Param 1 : message_ids list
			args.addArg(messageIds.getArgs());

			// Param 2 : starred - boolean value
			args.addArg(mStarred);

			// Param 3 : create_missing - If table does not contain any value
			// for
			// this row than create new one
			args.addArg(true);

			// Creating Local Database Requirement Values
			ContentValues values = new ContentValues();
			String value = (mStarred) ? "true" : "false";
			values.put("starred", value);

			boolean response = openerp.callServerMethod(getModel(),
					"set_message_starred", args.getArgs(), values,
					JSONDataHelper.jsonArrayTointArray(messageIds.getArgs()));
			return response;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {

				OEListViewRow row = (OEListViewRow) mMessageObjects
						.get(mPosition);
				row.getRow_data().put("starred", mStarred);

				mListAdapter.notifiyDataChange(mMessageObjects);
			} else {
				Toast.makeText(getActivity(), "No connection",
						Toast.LENGTH_LONG).show();
			}
			mProgressDialog.dismiss();
		}

	}

	/**
	 * Making message read or unread or Archive
	 */
	public class ReadUnreadOperation extends AsyncTask<Void, Void, Boolean> {

		ProgressDialog mProgressDialog = null;
		boolean mToRead = false;
		boolean isConnection = true;
		int mParentId = 0;

		public ReadUnreadOperation(boolean toRead, int parent_id) {
			mToRead = toRead;
			mParentId = parent_id;
			try {
				if (!OpenERPServerConnection.isNetworkAvailable(getActivity())) {
					isConnection = false;
				}
			} catch (Exception e) {
				isConnection = false;
			}
			mProgressDialog = new ProgressDialog(getActivity());
			mProgressDialog.setMessage("Working...");
			if (isConnection) {
				mProgressDialog.show();
			}
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			if (!isConnection) {
				return false;
			}
			boolean flag = false;
			OEArgsHelper args = new OEArgsHelper();
			args = new OEArgsHelper();
			String default_model = "false";
			int res_id = 0;

			List<HashMap<String, Object>> ids = db.executeSQL(
					db.getModelName(),
					new String[] { "id" },
					new String[] { "id = ?", "OR", "parent_id = ?" },
					new String[] { String.valueOf(mParentId),
							String.valueOf(mParentId) });
			for (HashMap<String, Object> id : ids) {
				if (mParentId != Integer.parseInt(id.get("id").toString())) {
					args.addArg(Integer.parseInt(id.get("id").toString()));
				}
			}
			args.addArg(mParentId);
			if (toggleReadUnread(args, default_model, res_id, mParentId,
					mToRead)) {
				flag = true;
			}

			return flag;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (!result) {
				Toast.makeText(getActivity(), "No connection",
						Toast.LENGTH_LONG).show();
			}
			mProgressDialog.dismiss();
		}

	}

	/* Method for Make Message as Read,Unread and Archive */
	private boolean toggleReadUnread(OEArgsHelper idsArg, String default_model,
			int res_id, int parent_id, boolean to_read) {
		boolean flag = false;
		OEHelper openerp = getOEInstance();
		JSONObject newContext = new JSONObject();
		try {
			if (default_model.equals("false")) {
				newContext.put("default_model", false);
			} else {
				newContext.put("default_model", default_model);
			}
			newContext.put("default_res_id", res_id);
			newContext.put("default_parent_id", parent_id);
			OEArgsHelper args = new OEArgsHelper();

			// Param 1 : message_ids list
			args.addArg(idsArg.getArgs());

			// Param 2 : to_read - boolean value
			args.addArg((to_read) ? false : true);

			// Param 3 : create_missing - If table does not contain any value
			// for
			// this row than create new one
			args.addArg(true);

			// Param 4 : context
			args.addArg(newContext);

			// Creating Local Database Requirement Values
			ContentValues values = new ContentValues();
			String value = (to_read) ? "true" : "false";
			values.put("starred", "false");
			values.put("to_read", value);
			flag = openerp.callServerMethod(getModel(), "set_message_read",
					args.getArgs(), values,
					JSONDataHelper.jsonArrayTointArray(idsArg.getArgs()));
			for (int uId : JSONDataHelper.jsonArrayTointArray(idsArg.getArgs())) {
				db.write(db, values, uId, true);
			}
		} catch (Exception e) {
		}
		return flag;
	}
}
