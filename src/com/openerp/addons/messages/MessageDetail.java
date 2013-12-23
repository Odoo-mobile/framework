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

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.openerp.MainActivity;
import com.openerp.R;
import com.openerp.auth.OpenERPAccountManager;
import com.openerp.base.ir.Ir_AttachmentDBHelper;
import com.openerp.base.res.Res_PartnerDBHelper;
import com.openerp.orm.OEHelper;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.JSONDataHelper;
import com.openerp.support.OEArgsHelper;
import com.openerp.support.listview.BooleanColumnCallback;
import com.openerp.support.listview.ControlClickEventListener;
import com.openerp.support.listview.OEListViewAdapter;
import com.openerp.support.listview.OEListViewOnCreateListener;
import com.openerp.support.listview.OEListViewRows;
import com.openerp.util.OEBinaryDownloadHelper;
import com.openerp.util.OEFileSizeHelper;
import com.openerp.util.contactview.OEContactView;
import com.openerp.util.drawer.DrawerItem;

/**
 * The Class MessageDetail.
 */
public class MessageDetail extends BaseFragment {

	/** The root view. */
	View rootView = null;
	private static final int MESSAGE_REPLY = 3;

	/** The list adapter. */
	OEListViewAdapter listAdapter = null;

	/** The messages_sorted. */
	List<OEListViewRows> messages_sorted = null;

	/** The message_id. */
	int message_id = 0;

	/** The parent_row. */
	HashMap<String, Object> parent_row = null;
	public static String oea_name = null;

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
		return rootView;
	}

	/**
	 * Sets the up list view.
	 * 
	 * @param list
	 *            the new up list view
	 */
	private boolean setupListView(final List<OEListViewRows> list) {
		// Handling List View controls and keys
		String[] from = new String[] { "image", "email_from|name",
				"email_from|email", "body", "date", "partners", "starred",
				"vote_nb" };
		int[] to = new int[] { R.id.imgUserPicture, R.id.txvMessageAuthor,
				R.id.txvAuthorEmail, R.id.webViewMessageBody, R.id.txvTime,
				R.id.txvTo, R.id.imgBtnStar, R.id.txvmessageVotenb };

		// Creating instance for listAdapter
		listAdapter = new OEListViewAdapter(scope.context(),
				R.layout.message_detail_listview_items, list, from, to, db);
		listAdapter.toHTML("body", true);
		listAdapter.addImageColumn("image");
		// listAdapter.layoutBackgroundColor("parent_id",
		// Color.parseColor("#aaaaaa"), Color.parseColor("#0099cc"));
		listAdapter.cleanDate("date", scope.User().getTimezone(),
				"MMM dd, yyyy,  hh:mm a");
		listAdapter.addViewListener(new OEListViewOnCreateListener() {

			@Override
			public View listViewOnCreateListener(final int position,
					View row_view, OEListViewRows row_data) {
				final int message_id = row_data.getRow_id();
				final HashMap<String, Object> row_values = row_data
						.getRow_data();
				/* handling vote control */
				final TextView txvVote = (TextView) row_view
						.findViewById(R.id.txvmessageVotenb);
				final int vote_nb = Integer.parseInt(row_data.getRow_data()
						.get("vote_nb").toString());
				if (vote_nb == 0) {
					txvVote.setText("");
				}
				final boolean hasVoted = Boolean.parseBoolean(row_data
						.getRow_data().get("has_voted").toString());
				if (!hasVoted) {
					txvVote.setCompoundDrawablesWithIntrinsicBounds(
							getResources()
									.getDrawable(
											R.drawable.ic_thumbs_up_unselected_dark_tablet),
							null, null, null);
					// txvVote.setBackgroundResource(R.drawable.vote_background_selector_gray);
				} else {
					// txvVote.setBackgroundResource(R.drawable.vote_background_selector_blue);
					txvVote.setCompoundDrawablesWithIntrinsicBounds(
							getResources()
									.getDrawable(
											R.drawable.ic_thumbs_up_selected_dark_tablet),
							null, null, null);
				}
				txvVote.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						MessageVoteToggle voteToggle = new MessageVoteToggle(
								message_id, vote_nb, hasVoted);
						String newVote = "";
						boolean btnvoted = false;
						if (hasVoted) {
							newVote = (vote_nb - 1) + "";
							row_values.put("has_voted", "false");
						} else {
							btnvoted = true;
							newVote = (vote_nb + 1) + "";
							row_values.put("has_voted", "true");
						}
						row_values.put("vote_nb", newVote);
						listAdapter.updateRow(position, new OEListViewRows(
								message_id, row_values));
						voteToggle.execute((Void) null);
						txvVote.setText(newVote);
						if (!btnvoted) {
							txvVote.setCompoundDrawablesWithIntrinsicBounds(
									getResources()
											.getDrawable(
													R.drawable.ic_thumbs_up_unselected_dark_tablet),
									null, null, null);
							// txvVote.setBackgroundResource(R.drawable.vote_background_selector_gray);
						} else {
							// txvVote.setBackgroundResource(R.drawable.vote_background_selector_blue);
							txvVote.setCompoundDrawablesWithIntrinsicBounds(
									getResources()
											.getDrawable(
													R.drawable.ic_thumbs_up_selected_dark_tablet),
									null, null, null);
						}
					}
				});

				/* handling attachments */
				List<OEListViewRows> attachments = getAttachmentsOfMessage(row_data
						.getRow_id() + "");
				int index = 0;
				if (attachments.size() > 0) {
					LayoutInflater vi = (LayoutInflater) scope.context()
							.getApplicationContext()
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View insertPoint = row_view
							.findViewById(R.id.gridAttachments);
					((ViewGroup) insertPoint).removeAllViews();
					for (OEListViewRows row : attachments) {
						View v = vi
								.inflate(
										R.layout.fragment_message_detail_attachment_grid_item,
										null, true);
						TextView txvAttachmentName = (TextView) v
								.findViewById(R.id.txvFileName);

						txvAttachmentName.setText(row.getRow_data().get("name")
								.toString());
						TextView txvAttachmentSize = (TextView) v
								.findViewById(R.id.txvFileSize);
						long fileSize = Long.parseLong(row.getRow_data()
								.get("file_size").toString());
						String file_size = OEFileSizeHelper
								.readableFileSize(fileSize);
						txvAttachmentSize.setText((file_size.equals("0")) ? " "
								: file_size);

						TextView txvAttachmentId = (TextView) v
								.findViewById(R.id.txvAttachmentId);
						txvAttachmentId.setText(String.valueOf(row.getRow_id()));
						((ViewGroup) insertPoint).addView(v, index,
								new ViewGroup.LayoutParams(
										ViewGroup.LayoutParams.FILL_PARENT,
										ViewGroup.LayoutParams.FILL_PARENT));
						v.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								int attachment_id = Integer.parseInt(((TextView) v
										.findViewById(R.id.txvAttachmentId))
										.getText().toString());
								OEBinaryDownloadHelper binaryDownload = new OEBinaryDownloadHelper();
								binaryDownload
										.downloadBinary(attachment_id, db);
							}
						});
						index++;

					}

				} else {
					row_view.findViewById(R.id.layoutMessageAttachments)
							.setVisibility(View.GONE);
				}
				OEContactView oe_contactView = (OEContactView) row_view
						.findViewById(R.id.imgUserPicture);
				int partner_id = Integer.parseInt(row_data.getRow_data()
						.get("partner_id").toString());
				oe_contactView.assignPartnerId(partner_id);
				return row_view;
			}
		});
		listAdapter.setItemClickListener(R.id.imgBtnReply,
				new ControlClickEventListener() {

					@Override
					public OEListViewRows controlClicked(int position,
							OEListViewRows row, View view) {
						Intent composeIntent = new Intent(scope.context(),
								MessageComposeActivty.class);
						composeIntent.putExtra("message_id", message_id);
						composeIntent.putExtra("send_reply", true);
						startActivityForResult(composeIntent, MESSAGE_REPLY);

						return null;
					}
				});
		// Setting callback handler for boolean field value change.
		listAdapter.setBooleanEventOperation("starred",
				R.drawable.ic_action_starred, R.drawable.ic_action_unstarred,
				updateStarred);
		GridView lstview = (GridView) rootView
				.findViewById(R.id.lstMessageDetail);
		// Providing adapter to listview
		lstview.setAdapter(listAdapter);
		return true;
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
				LoadMessageDetails messageDetails = new LoadMessageDetails(
						message_id);
				messageDetails.execute((Void) null);
			}
		}
	}

	private boolean setupMessageDetail(int message_id) {
		messages_sorted = new ArrayList<OEListViewRows>();

		String query = "select t1.id as message_id , t1.*, t2.id as partner_id, t2.name, t2.image_small as image, t2.email from mail_message t1, res_partner t2 where (t1.id = ? or t1.parent_id = ?) and (t2.id = t1.author_id or t1.author_id = 'false') group by t1.id order by t1.date desc";
		List<HashMap<String, Object>> records = db.executeSQL(
				query,
				new String[] { String.valueOf(message_id),
						String.valueOf(message_id) });
		HashMap<String, Object> row = new HashMap<String, Object>();
		row.put("total", records.size());
		row.put("records", records);
		if ((Integer) row.get("total") > 0) {
			List<HashMap<String, Object>> rows_detail = (List<HashMap<String, Object>>) row
					.get("records");
			for (HashMap<String, Object> row_detail : rows_detail) {
				int msg_id = Integer.parseInt(row_detail.get("message_id")
						.toString());
				String key = row_detail.get("parent_id").toString();
				OEListViewRows rowObj = null;
				String[] ids = getPartnersOfMessage(row_detail
						.get("message_id").toString());
				String partners = "nobody";
				if (ids != null) {
					partners = TextUtils.join(", ", ids);
				}
				row_detail.put("partners", partners);
				if (key.equals("false")) {
					// Parent Message
					if (row_detail.get("author_id").toString().equals("false")) {
						row_detail.put("image", "false");
					}
					rowObj = new OEListViewRows(msg_id, row_detail);
					parent_row = row_detail;
					if (!row_detail.get("model").toString().equals("false")) {
						messages_sorted.add(rowObj);
					} else {
						messages_sorted.add(0, rowObj);
					}
					String sub = rowObj.getRow_data().get("subject").toString();
					if (sub.equals("false")) {
						sub = rowObj.getRow_data().get("type").toString();
					}
					TextView txvTitle = (TextView) rootView
							.findViewById(R.id.txvMessageTitle);
					txvTitle.setText(sub);
					if (row_detail.get("model").toString().equals("mail.group")) {
						if (UserGroups.menu_color.containsKey("group_"
								+ row_detail.get("res_id").toString())) {
							View tagColor = rootView
									.findViewById(R.id.groupColorLine);
							tagColor.setBackgroundColor(UserGroups.menu_color
									.get("group_"
											+ row_detail.get("res_id")
													.toString()));
						}
					}
				} else {
					rowObj = new OEListViewRows(msg_id, row_detail);
					messages_sorted.add(rowObj);
				}

			}
		}
		return setupListView(messages_sorted);
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
		List<HashMap<String, Object>> records = partners
				.executeSQL(
						"SELECT id,name,oea_name FROM res_partner where id in (select res_partner_id from mail_message_res_partner_rel where mail_message_id = ? and oea_name = ?) and oea_name = ?",
						new String[] { message_id, oea_name, oea_name });
		List<String> names = new ArrayList<String>();
		if (records.size() > 0) {
			for (HashMap<String, Object> row : records) {
				if (row.get("name")
						.toString()
						.equals(OpenERPAccountManager.currentUser(
								MainActivity.context).getPartner_id())) {
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
	public List<OEListViewRows> getAttachmentsOfMessage(String message_id) {
		List<OEListViewRows> lists = new ArrayList<OEListViewRows>();
		Ir_AttachmentDBHelper attachments = new Ir_AttachmentDBHelper(
				MainActivity.context);
		oea_name = OpenERPAccountManager.currentUser(MainActivity.context)
				.getAndroidName();
		List<HashMap<String, Object>> records = attachments
				.executeSQL(
						"SELECT * FROM ir_attachment where id in (select ir_attachment_id from mail_message_ir_attachment_rel where mail_message_id = ? and oea_name = ?) and oea_name = ?",
						new String[] { message_id, oea_name, oea_name });
		if (records.size() > 0) {
			for (HashMap<String, Object> row : records) {
				int attachment_id = Integer.parseInt(row.get("id").toString());
				OEListViewRows list_row = new OEListViewRows(attachment_id, row);
				lists.add(list_row);
			}
		}
		return lists;
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		return null;
	}

	/** The update starred. */
	BooleanColumnCallback updateStarred = new BooleanColumnCallback() {

		@Override
		public OEListViewRows updateFlagValues(OEListViewRows row, View view) {
			HashMap<String, Object> rowData = (HashMap<String, Object>) row
					.getRow_data();
			boolean flag = false;
			ImageView img = (ImageView) view;
			if (rowData.get("starred").toString().equals("false")) {
				flag = true;
				img.setImageResource(R.drawable.ic_action_starred);
			} else {
				img.setImageResource(R.drawable.ic_action_unstarred);
			}
			OEArgsHelper messageIds = new OEArgsHelper();
			messageIds.addArg(row.getRow_id());
			if (markAsTodo(messageIds, flag)) {
				rowData.put("starred", flag);
			} else {
				Log.e("Unable to mark as todo", "Operation Fail");
			}
			return row;
		}
	};

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
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		// handle item selection
		switch (item.getItemId()) {
		case R.id.menu_message_detail_read:
			markAsReadUnreadArchive(true);
			return true;
		case R.id.menu_message_detail_unread:
			markAsReadUnreadArchive(false);
			return true;
		case R.id.menu_message_compose:
			scope.context().startActivity(
					new Intent(scope.context(), MessageComposeActivty.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* Method for Make Message as Read,Unread and Archive */
	private boolean markAsReadUnreadArchive(OEArgsHelper messageIds,
			String default_model, int res_id, int parent_id, boolean markFlag) {
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
			args.addArg(messageIds.getArgs());

			// Param 2 : starred - boolean value
			args.addArg(markFlag);

			// Param 3 : create_missing - If table does not contain any value
			// for
			// this row than create new one
			args.addArg(true);

			// Param 4 : context
			args.addArg(newContext);

			// Creating Local Database Requirement Values
			ContentValues values = new ContentValues();
			String value = (markFlag) ? "false" : "true";
			values.put("starred", "false");
			values.put("to_read", value);
			flag = openerp.callServerMethod(getModel(), "set_message_read",
					args.getArgs(), values,
					JSONDataHelper.jsonArrayTointArray(messageIds.getArgs()));
			for (int uId : JSONDataHelper.jsonArrayTointArray(messageIds
					.getArgs())) {
				db.write(db, values, uId, true);
			}
		} catch (Exception e) {
		}
		return flag;
	}

	/* Method for mark multiple message as Read, Unread, Archive */
	private boolean markAsReadUnreadArchive(final boolean flag) {
		boolean res = false;
		OEArgsHelper args = new OEArgsHelper();
		int parent_id = 0;
		int res_id = 0;
		String default_model = "false";

		final int pos = 0;
		OEListViewRows rowInfo = messages_sorted.get(pos);
		if (rowInfo.getRow_data().get("parent_id").equals("false")) {
			parent_id = rowInfo.getRow_id();
			res_id = Integer.parseInt(rowInfo.getRow_data().get("res_id")
					.toString());
			default_model = rowInfo.getRow_data().get("model").toString();
		} else {
			parent_id = Integer.parseInt(rowInfo.getRow_data().get("parent_id")
					.toString());
		}
		List<HashMap<String, Object>> ids = db.executeSQL(
				db.getModelName(),
				new String[] { "id" },
				new String[] { "id = ?", "OR", "parent_id = ?" },
				new String[] { String.valueOf(parent_id),
						String.valueOf(parent_id) });
		for (HashMap<String, Object> id : ids) {
			if (parent_id != Integer.parseInt(id.get("id").toString())) {
				args.addArg(Integer.parseInt(id.get("id").toString()));
			}
		}
		args.addArg(rowInfo.getRow_id());

		if (markAsReadUnreadArchive(args, default_model, res_id, parent_id,
				flag)) {
		}
		return res;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case MESSAGE_REPLY:
			if (resultCode == Activity.RESULT_OK) {
				Bundle bundle = new Bundle();
				bundle.putInt("message_id", message_id);
				LoadMessageDetails messageDetails = new LoadMessageDetails(
						message_id);
				messageDetails.execute((Void) null);
			}
			break;
		}
	}

	public class LoadMessageDetails extends AsyncTask<Void, Void, Boolean> {
		int message_id = 0;
		boolean flag = false;

		public LoadMessageDetails(int message_id) {
			this.message_id = message_id;
		}

		@Override
		protected void onPreExecute() {
			scope.context().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					rootView.findViewById(R.id.loadingHeader).setVisibility(
							View.VISIBLE);
					rootView.findViewById(R.id.messageDetailView)
							.setVisibility(View.GONE);
				}
			});
		}

		@Override
		protected Boolean doInBackground(Void... arg0) {
			scope.context().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					flag = setupMessageDetail(message_id);
				}
			});
			return flag;

		}

		@Override
		protected void onPostExecute(final Boolean success) {
			scope.context().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					try {
						rootView.findViewById(R.id.loadingHeader)
								.setVisibility(View.GONE);
						rootView.findViewById(R.id.messageDetailView)
								.setVisibility(View.VISIBLE);
					} catch (Exception e) {
					}
					if (success) {
						MarkingAsRead read = new MarkingAsRead(message_id);
						read.execute((Void) null);
					}
				}
			});
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
				JSONObject res = oe.call_kw(db.getModelName(), "vote_toggle",
						new JSONArray("[" + args.toString() + "]"));
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

		@Override
		protected void onPostExecute(Boolean result) {
		}

	}

	private class MarkingAsRead extends AsyncTask<Void, Void, Boolean> {
		int message_id = 0;

		public MarkingAsRead(int message_id) {
			this.message_id = message_id;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			markAsReadUnreadArchive(true);
			return true;
		}

	}
}
