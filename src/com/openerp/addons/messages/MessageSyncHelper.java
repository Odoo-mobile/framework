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

import java.io.IOException;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.openerp.MainActivity;
import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.OEHelper;
import com.openerp.receivers.DataSetChangeReceiver;
import com.openerp.support.OEArgsHelper;
import com.openerp.support.SyncHelper;

// TODO: Auto-generated Javadoc
/**
 * The Class MessageSyncHelper.
 */
public class MessageSyncHelper extends OEHelper implements SyncHelper {

	/** The m context. */
	Context mContext = null;

	/**
	 * Instantiates a new message sync helper.
	 * 
	 * @param context
	 *            the context
	 * @throws ClientProtocolException
	 *             the client protocol exception
	 * @throws JSONException
	 *             the jSON exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public MessageSyncHelper(Context context) throws ClientProtocolException,
			JSONException, IOException {
		super(context, MainActivity.userContext);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.openerp.support.SyncHelper#syncWithServer(com.openerp.orm.BaseDBHelper
	 * , org.json.JSONArray)
	 */
	@Override
	public HashMap<String, Object> syncWithServer(BaseDBHelper db,
			JSONArray args) {
		// TODO Auto-generated method stub
		HashMap<String, Object> messageSyncOutcome = new HashMap<String, Object>();
		JSONArray newCreated = new JSONArray();
		try {
			JSONObject serverData = call_kw(db.getModelName(), "message_read",
					args);
			if (serverData.has("result")) {
				for (int i = 0; i < serverData.getJSONArray("result").length(); i++) {
					JSONObject row = serverData.getJSONArray("result")
							.getJSONObject(i);

					// If row is expandable
					if (row.getString("type").equals("expandable")) {
						this.handleMessageExpandable(db, row);
					} else {
						int newId = this.handleMessageResponse(db, row);
						newCreated.put(newId);
					}
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		messageSyncOutcome.put("total", newCreated.length());
		messageSyncOutcome.put("new_ids", newCreated);

		// testing();
		return messageSyncOutcome;
	}

	/**
	 * Handle message expandable.
	 * 
	 * @param db
	 *            the db
	 * @param row
	 *            the row
	 */
	private void handleMessageExpandable(BaseDBHelper db, JSONObject row) {
		try {

			String domain = row.getString("domain");
			JSONObject enewValues = new JSONObject();
			enewValues.put("default_model", false);
			enewValues.put("default_res_id", false);
			enewValues.put("default_parent_id", row.getString("parent_id"));
			JSONObject ejcontext = this.updateContext(enewValues);
			JSONArray eargs = new JSONArray();

			eargs.put(null);

			eargs.put(new JSONArray(domain));
			eargs.put(new JSONArray("[" + row.getString("parent_id") + "]"));
			eargs.put(false);
			eargs.put(ejcontext);
			eargs.put(row.getString("parent_id"));
			eargs.put(100);

			syncWithServer(db, eargs);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handle message response.
	 * 
	 * @param db
	 *            the db
	 * @param response
	 *            the response
	 * @return the int
	 */
	private int handleMessageResponse(BaseDBHelper db, JSONObject response) {
		String[] columns = db.columnListToStringArray(db.getServerColumns());
		ContentValues values = new ContentValues();
		int newId = 0;

		try {
			for (String column : columns) {
				if (response.has(column)) {
					values.put(column, response.getString(column));
				}
			}

			if (values.get("subject").toString().equals("false")) {
				values.put("subject", response.getString("record_name"));
			}

			String author = "false";
			String email_from = "false";
			if (response.getJSONArray("author_id").getString(0).equals("0")) {
				email_from = response.getJSONArray("author_id").getString(1)
						.toString();
			} else {
				author = response.getString("author_id");
			}
			values.put("author_id", author);
			values.put("email_from", email_from);

			String parent_id = "false";
			if (!response.getString("parent_id").equals("false")) {
				if (response.get("parent_id") instanceof JSONArray) {
					parent_id = response.getJSONArray("parent_id").getString(0);
				} else {
					parent_id = response.getString("parent_id");
				}
			}
			values.put("parent_id", parent_id);

			String starred = "false";
			if (response.has("is_favorite")) {
				if (response.getString("is_favorite").equals("true")) {
					starred = "true";
				}
			}
			values.put("starred", starred);

			if (!db.hasRecord(db, response.getInt("id"))) {
				// Sending Broadcast message for data set change.
				Intent intent = new Intent();
				intent.setAction(DataSetChangeReceiver.DATA_CHANGED);
				newId = db.create(db, values);
				intent.putExtra("id", String.valueOf(newId));
				intent.putExtra("parent_id", parent_id);
				mContext.sendBroadcast(intent);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return newId;
	}

	/**
	 * Testing.
	 */
	public void testing() {
		try {
			JSONObject fields = new JSONObject();
			fields.accumulate("fields", "notified_partner_ids");
			fields.accumulate("fields", "author_id");
			fields.accumulate("fields", "parent_id");

			// filtering notification partner ids
			OEArgsHelper args1 = new OEArgsHelper();
			args1.addArgCondition("notification_ids.partner_id.user_ids", "in",
					new JSONArray("[1]"));

			// Filtering partner_ids to userid
			OEArgsHelper args2 = new OEArgsHelper();
			args2.addArgCondition("partner_ids.user_ids", "in", new JSONArray(
					"[1]"));

			// filtering author
			OEArgsHelper args3 = new OEArgsHelper();
			args3.addArgCondition("author_id.user_ids", "in", new JSONArray(
					"[1]"));

			// // fetching only parents
			// OEArgsHelper args4 = new OEArgsHelper();
			// args4.addArgCondition("parent_id", "=", false);

			OEArgsHelper args = new OEArgsHelper();

			args.addArg("|");
			args.addArg("|");
			args.addArg(args1.getArgs());
			args.addArg(args2.getArgs());
			args.addArg(args3.getArgs());
			// args.addArg(args4.getArgs());

			JSONObject domain = new JSONObject();
			domain.accumulate("domain", args.getArgs());
			JSONObject data = search_read("mail.message", fields, domain, 0, 0,
					null, null);

		} catch (Exception e) {
			// TODO: handle exception
		}

	}
}
