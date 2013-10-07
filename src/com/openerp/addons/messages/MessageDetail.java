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
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
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
import com.openerp.base.ir.Ir_AttachmentDBHelper;
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
import com.openerp.support.menu.OEMenu;
import com.openerp.util.HTMLHelper;
import com.openerp.util.OEBinaryDownloadHelper;
import com.openerp.util.OEFileSizeHelper;

// TODO: Auto-generated Javadoc
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

		scope = new AppScope(MainActivity.userContext,
				(MainActivity) getActivity());

		db = (MessageDBHelper) getModel();

		rootView = inflater.inflate(R.layout.fragment_message_detail_view,
				container, false);

		handleArguments((Bundle) getArguments());

		return rootView;
	}

	/**
	 * Sets the up list view.
	 * 
	 * @param list
	 *            the new up list view
	 */
	private void setupListView(List<OEListViewRows> list) {
		// Handling List View controls and keys
		String[] from = new String[] { "image", "email_from|name",
				"email_from|email", "body", "date", "partners", "starred" };
		int[] to = new int[] { R.id.imgUserPicture, R.id.txvMessageAuthor,
				R.id.txvAuthorEmail, R.id.txvBody, R.id.txvTime, R.id.txvTo,
				R.id.imgBtnStar };

		// Creating instance for listAdapter
		listAdapter = new OEListViewAdapter(scope.context(),
				R.layout.message_detail_listview_items, list, from, to, db);
		// listAdapter.toHTML("body");
		listAdapter.addImageColumn("image");
		// listAdapter.layoutBackgroundColor("parent_id",
		// Color.parseColor("#aaaaaa"), Color.parseColor("#0099cc"));
		listAdapter.cleanDate("date", scope.User().getTimezone());
		listAdapter.addViewListener(new OEListViewOnCreateListener() {

			@Override
			public View listViewOnCreateListener(int position, View row_view,
					OEListViewRows row_data) {
				TextView txvBody = (TextView) row_view
						.findViewById(R.id.txvBody);
				txvBody.setMovementMethod(LinkMovementMethod.getInstance());

				txvBody.setText(HTMLHelper.stringToHtml(row_data.getRow_data()
						.get("body").toString()));

				/* handling attachments */
				List<OEListViewRows> attachments = getAttachmentsOfMessage(row_data
						.getRow_id() + "");
				int index = 0;
				if (attachments.size() > 0) {
					LayoutInflater vi = (LayoutInflater) scope.context()
							.getApplicationContext()
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View v = vi
							.inflate(
									R.layout.fragment_message_detail_attachment_grid_item,
									null, false);
					View insertPoint = row_view
							.findViewById(R.id.gridAttachments);
					((ViewGroup) insertPoint).removeAllViews();
					for (OEListViewRows row : attachments) {

						TextView txvAttachmentName = (TextView) v
								.findViewById(R.id.txvFileName);

						txvAttachmentName.setText(row.getRow_data().get("name")
								.toString());
						TextView txvAttachmentSize = (TextView) v
								.findViewById(R.id.txvFileSize);
						long fileSize = Long.parseLong(row.getRow_data()
								.get("file_size").toString());
						txvAttachmentSize.setText(OEFileSizeHelper
								.readableFileSize(fileSize));

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
				return row_view;
			}
		});
		listAdapter.setItemClickListener(R.id.imgBtnReply,
				new ControlClickEventListener() {

					@Override
					public OEListViewRows controlClicked(int position,
							OEListViewRows row, View view) {
						// TODO Auto-generated method stub
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
				R.drawable.ic_action_rating_important,
				R.drawable.ic_action_rating_not_important, updateStarred);
		GridView lstview = (GridView) rootView
				.findViewById(R.id.lstMessageDetail);
		// Providing adapter to listview
		lstview.setAdapter(listAdapter);
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
		// TODO Auto-generated method stub
		return new MessageDBHelper(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openerp.support.FragmentHelper#handleArguments(android.os.Bundle)
	 */
	@Override
	public void handleArguments(Bundle bundle) {
		// TODO Auto-generated method stub
		if (bundle != null) {
			if (bundle.containsKey("message_id")) {
				messages_sorted = new ArrayList<OEListViewRows>();
				message_id = bundle.getInt("message_id");

				String query = "select t1.id as message_id , t1.*, t2.name, t2.image, t2.email from mail_message t1, res_partner t2 where (t1.id = ? or t1.parent_id = ?) and (t2.id = t1.author_id or t1.author_id = 'false') group by t1.id order by t1.id desc";
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

						int msg_id = Integer.parseInt(row_detail.get(
								"message_id").toString());
						String key = row_detail.get("parent_id").toString();
						OEListViewRows rowObj = null;
						String[] ids = getPartnersOfMessage(row_detail.get(
								"message_id").toString());
						String partners = "nobody";
						if (ids != null) {
							partners = TextUtils.join(", ", ids);
						}
						row_detail.put("partners", partners);
						if (key.equals("false")) {

							// Parent Message
							if (row_detail.get("author_id").toString()
									.equals("false")) {
								row_detail.put("image", "false");
							}
							rowObj = new OEListViewRows(msg_id, row_detail);
							parent_row = row_detail;
							messages_sorted.add(0, rowObj);
							String sub = rowObj.getRow_data().get("subject")
									.toString();
							if (sub.equals("false")) {
								sub = rowObj.getRow_data().get("type")
										.toString();
							}
							TextView txvTitle = (TextView) rootView
									.findViewById(R.id.txvMessageTitle);
							txvTitle.setText(sub);
						} else {
							rowObj = new OEListViewRows(msg_id, row_detail);
							messages_sorted.add(rowObj);
						}

					}
				}
				setupListView(messages_sorted);
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
		String[] str = null;
		db = new MessageDBHelper(MainActivity.context);
		HashMap<String, Object> data = db.search(db, new String[] { "id = ?" },
				new String[] { message_id });
		if (Integer.parseInt(data.get("total").toString()) > 0) {
			List<HashMap<String, Object>> rows = (List<HashMap<String, Object>>) data
					.get("records");
			for (HashMap<String, Object> row : rows) {
				try {
					JSONArray arr = new JSONArray(row.get("partner_ids")
							.toString());
					str = new String[arr.length()];
					for (int i = 0; i < arr.length(); i++) {
						if (arr.getJSONArray(i).length() == 2) {

							if (arr.getJSONArray(i).getString(0)
									.equals(scope.User().getPartner_id())) {
								str[i] = "me";
							} else {
								str[i] = arr.getJSONArray(i).getString(1);
							}
						}
					}
				} catch (Exception e) {

				}
			}
		}
		return str;
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
		db = new MessageDBHelper(MainActivity.context);
		HashMap<String, Object> data = db.search(db, new String[] { "id = ?" },
				new String[] { message_id });
		if (Integer.parseInt(data.get("total").toString()) > 0) {
			List<HashMap<String, Object>> rows = (List<HashMap<String, Object>>) data
					.get("records");
			Ir_AttachmentDBHelper attachments = new Ir_AttachmentDBHelper(
					MainActivity.context);

			for (HashMap<String, Object> row : rows) {
				try {
					JSONArray arr = new JSONArray(row.get("attachment_ids")
							.toString());
					for (int i = 0; i < arr.length(); i++) {
						int attachment_id = arr.getJSONArray(i).getInt(0);
						HashMap<String, Object> rowData = attachments.search(
								attachments, new String[] { "id = ? " },
								new String[] { attachment_id + "" });
						List<HashMap<String, Object>> lists_data = (List<HashMap<String, Object>>) rowData
								.get("records");
						OEListViewRows list_row = new OEListViewRows(
								attachment_id, lists_data.get(0));
						lists.add(list_row);
					}
				} catch (Exception e) {
				}
			}

		}
		return lists;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openerp.support.FragmentHelper#menuHelper(android.content.Context)
	 */
	@Override
	public OEMenu menuHelper(Context context) {
		// TODO Auto-generated method stub
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
				img.setImageResource(R.drawable.ic_action_rating_important);
			} else {
				img.setImageResource(R.drawable.ic_action_rating_not_important);
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
				handleArguments(bundle);
			}
			break;
		}
	}

}
