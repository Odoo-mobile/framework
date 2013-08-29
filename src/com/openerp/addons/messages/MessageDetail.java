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

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.openerp.MainActivity;
import com.openerp.R;
import com.openerp.orm.OEHelper;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.listview.BooleanColumnCallback;
import com.openerp.support.listview.ControlClickEventListener;
import com.openerp.support.listview.OEListViewAdapter;
import com.openerp.support.listview.OEListViewRows;
import com.openerp.support.menu.OEMenu;
import com.openerp.util.OEDate;

// TODO: Auto-generated Javadoc
/**
 * The Class MessageDetail.
 */
public class MessageDetail extends BaseFragment {

	/** The root view. */
	View rootView = null;

	/** The list. */
	List<OEListViewRows> list = new ArrayList<OEListViewRows>();

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

		rootView.findViewById(R.id.imgBtnSendMessage).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						EditText edtReply = (EditText) rootView
								.findViewById(R.id.edtReplyMessage);
						edtReply.setError(null);

						if (TextUtils.isEmpty(edtReply.getText())) {
							edtReply.setError("Provide message reply !");
						} else {
							Toast.makeText(scope.context(),
									"Sending message...", Toast.LENGTH_LONG)
									.show();
							if (sendMessageReply(edtReply.getText().toString())) {
								Toast.makeText(scope.context(),
										"Message Sent successfully.",
										Toast.LENGTH_LONG).show();
								edtReply.setText("");
								rootView.findViewById(R.id.layoutMessageReply)
										.setVisibility(View.GONE);

							} else {
								Toast.makeText(scope.context(),
										"Message Sending fail !",
										Toast.LENGTH_LONG).show();
							}
						}
					}

				});

		return rootView;
	}

	/**
	 * Send message reply.
	 * 
	 * @param body
	 *            the body
	 * @return true, if successful
	 */
	private boolean sendMessageReply(String body) {
		boolean flag = false;
		try {
			OEHelper oe = db.getOEInstance();
			ContentValues values = new ContentValues();

			JSONArray arguments = new JSONArray("[false]");
			String crate_date = OEDate.getDate();
			JSONObject kwargs = new JSONObject();
			kwargs.put("body", body);
			values.put("body", body);

			kwargs.put("subject", false);
			values.put("subject", "false");
			kwargs.put("date", crate_date);
			kwargs.put("parent_id", message_id);
			values.put("parent_id", message_id);

			kwargs.put("attachment_ids", new JSONArray());
			kwargs.put("partner_ids", new JSONArray());

			JSONObject oecontext = new JSONObject();
			String model = parent_row.get("model").toString();

			oecontext
					.put("default_model",
							(!model.equals("mail.thread") ? (model
									.equals("false") ? false : model) : false));
			oecontext
					.put("default_res_id", (parent_row.get("res_id").toString()
							.equals("0") ? false : parent_row.get("res_id")));
			oecontext.put("default_parent_id", message_id);
			oecontext.put("mail_post_autofollow", true);
			oecontext.put("mail_post_autofollow_partner_ids", new JSONArray());

			kwargs.put("context", oecontext);

			kwargs.put("type", "comment");
			values.put("type", "comment");

			kwargs.put("content_subtype", "plaintext");
			kwargs.put("subtype", "mail.mt_comment");

			values.put("email_from", "false");
			values.put("record_name", "false");
			values.put("to_read", "false");
			values.put("author_id", scope.User().getPartner_id());
			values.put("model", oecontext.getString("default_model"));
			values.put("res_id", oecontext.getString("default_res_id"));
			values.put("date", crate_date);
			values.put("starred", "false");
			values.put("partner_ids", parent_row.get("partners").toString());
			oe.updateKWargs(kwargs);
			JSONObject result = oe.call_kw("mail.thread", "message_post",
					arguments);
			values.put("id", result.getString("result"));
			int newid = db.create(db, values);

			String query = "select t1.id as message_id , t1.*, t2.name, t2.image, t2.email from mail_message t1, res_partner t2 where (t1.id = ? or t1.parent_id = ?) and (t2.id = t1.author_id or t1.author_id = 'false') group by t1.id order by t1.id desc";
			List<HashMap<String, Object>> records = db
					.executeSQL(query, new String[] { String.valueOf(newid),
							String.valueOf(newid) });

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
					String[] ids = getPartnersOfMessage(row_detail.get(
							"message_id").toString());
					String partners = "nobody";
					if (ids != null) {
						partners = TextUtils.join(", ", ids);
					}
					row_detail.put("partners", partners);

					rowObj = new OEListViewRows(msg_id, row_detail);
					messages_sorted.add(1, rowObj);

				}
			}
			listAdapter.refresh(messages_sorted);
			flag = true;
		} catch (Exception e) {
			flag = false;
		}
		return flag;
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
				"email_from|email", "parent_id", "body", "date", "partners" };
		int[] to = new int[] { R.id.imgUserPicture, R.id.txvMessageAuthor,
				R.id.txvAuthorEmail, R.id.layoutMessageDetailHeader,
				R.id.txvBody, R.id.txvTime, R.id.txvTo };

		// Creating instance for listAdapter
		listAdapter = new OEListViewAdapter(scope.context(),
				R.layout.message_detail_listview_items, list, from, to, db);
		listAdapter.toHTML("body");
		listAdapter.addImageColumn("image");
		listAdapter.layoutBackgroundColor("parent_id",
				Color.parseColor("#aaaaaa"), Color.parseColor("#0099cc"));
		listAdapter.cleanDate("date", scope.User().getTimezone());

		listAdapter.setItemClickListener(R.id.layoutMessageDetailHeader,
				new ControlClickEventListener() {

					@Override
					public OEListViewRows controlClicked(int position,
							OEListViewRows row, View view) {
						// TODO Auto-generated method stub

						return null;
					}
				});

		listAdapter.setItemClickListener(R.id.imgBtnReply,
				new ControlClickEventListener() {

					@Override
					public OEListViewRows controlClicked(int position,
							OEListViewRows row, View view) {
						// TODO Auto-generated method stub
						rootView.findViewById(R.id.layoutMessageReply)
								.setVisibility(View.VISIBLE);
						rootView.findViewById(R.id.edtReplyMessage)
								.requestFocus();

						return null;
					}
				});
		// Setting callback handler for boolean field value change.
		listAdapter.setBooleanEventOperation("starred",
				R.drawable.ic_action_starred, R.drawable.ic_action_unstarred,
				updateStarred);
		ListView lstview = (ListView) rootView
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
	private String[] getPartnersOfMessage(String message_id) {
		String[] str = null;

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
			// TODO Auto-generated method stub
			return null;
		}
	};

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
			Log.i("Menu Clicked", "mark as read");
			return true;
		case R.id.menu_message_detail_unread:
			Log.i("Menu Clicked", "mark as unread");
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
