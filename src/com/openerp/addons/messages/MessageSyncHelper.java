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

import openerp.OEVersionException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.OEHelper;
import com.openerp.receivers.DataSetChangeReceiver;
import com.openerp.support.JSONDataHelper;
import com.openerp.support.OEArgsHelper;
import com.openerp.support.OEUser;
import com.openerp.support.SyncHelper;

// TODO: Auto-generated Javadoc
/**
 * The Class MessageSyncHelper.
 */
public class MessageSyncHelper extends OEHelper implements SyncHelper {

	/** The m context. */
	Context mContext = null;
	private static OEHelper oe = null;
	OEUser mUser = null;

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
	 * @throws OEVersionException
	 */
	public MessageSyncHelper(Context context) throws ClientProtocolException,
			JSONException, IOException, OEVersionException {
		super(context, OEUser.current(context));
		mContext = context;
		mUser = OEUser.current(context);
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
		HashMap<String, Object> messageSyncOutcome = new HashMap<String, Object>();
		// Updating old messages (starred and to_read columsn from
		// mail.notification
		if (oe == null) {
			oe = db.getOEInstance();
		}
		JSONArray updatedIds = new JSONArray();
		JSONArray localIds = JSONDataHelper.intArrayToJSONArray(getAllIds(db));
		// Fetching new message
		JSONArray newCreated = new JSONArray();
		try {
			JSONObject serverData = oe.call_kw(db.getModelName(),
					"message_read", args);
			if (serverData.has("result")) {
				for (int i = serverData.getJSONArray("result").length() - 1; i >= 0; i--) {
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

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Updating old messages
		try {
			JSONObject fields = new JSONObject();

			fields.accumulate("fields", "read");
			fields.accumulate("fields", "starred");
			fields.accumulate("fields", "partner_id");
			fields.accumulate("fields", "message_id");

			OEArgsHelper argsObj1 = new OEArgsHelper();
			argsObj1.addArgCondition("message_id", "in", localIds);
			OEArgsHelper argsObj2 = new OEArgsHelper();
			argsObj2.addArgCondition("partner_id", "=",
					Integer.parseInt(OEUser.current(mContext).getPartner_id()));

			OEArgsHelper argsObj = new OEArgsHelper();
			argsObj.addArg(argsObj1.getArgs());
			argsObj.addArg(argsObj2.getArgs());

			JSONObject domainRplies = new JSONObject();
			domainRplies.accumulate("domain", new JSONArray(argsObj.getArgs()
					.toString()));

			JSONObject msgReplies = oe.search_read("mail.notification", fields,
					domainRplies, 0, 1000, null, null);
			if (msgReplies.has("records")) {
				updatedIds = updateMessageStatus(msgReplies, db);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Updating Message Votes
		try {
			JSONObject vote_fields = new JSONObject();
			vote_fields.accumulate("fields", "vote_user_ids");

			OEArgsHelper vote_args = new OEArgsHelper();
			vote_args.addArgCondition("id", "in", localIds);

			OEArgsHelper argsObj = new OEArgsHelper();
			argsObj.addArg(vote_args.getArgs());

			JSONObject domain_votes = new JSONObject();
			domain_votes.accumulate("domain", new JSONArray(argsObj.getArgs()
					.toString()));
			JSONObject vote_detail = oe.search_read("mail.message",
					vote_fields, domain_votes, 0, 0, null, null);
			if (vote_detail.has("records")) {
				updateMessageVotes(vote_detail, db);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		int total = newCreated.length() + updatedIds.length();
		messageSyncOutcome.put("total", total);
		messageSyncOutcome.put("new_ids", newCreated);
		messageSyncOutcome.put("update_ids", updatedIds);

		return messageSyncOutcome;
	}

	private void updateMessageVotes(JSONObject vote_detail, BaseDBHelper db) {
		try {
			for (int j = 0; j < vote_detail.getJSONArray("records").length(); j++) {
				JSONObject obj_vote = vote_detail.getJSONArray("records")
						.getJSONObject(j);
				JSONArray voted_user_ids = obj_vote
						.getJSONArray("vote_user_ids");
				ContentValues values = new ContentValues();
				for (int i = 0; i < voted_user_ids.length(); i++) {
					if (voted_user_ids.getString(i).equals(mUser.getUser_id())) {
						values.put("has_voted", "true");
						break;
					} else {
						values.put("has_voted", "false");
					}
				}
				int total_votes = voted_user_ids.length();
				int message_id = obj_vote.getInt("id");
				values.put("vote_nb", total_votes);
				db.write(db, values, message_id, true);
			}
		} catch (Exception e) {
		}
	}

	private JSONArray updateMessageStatus(JSONObject msgReplies, BaseDBHelper db) {
		JSONArray updatedIds = new JSONArray();
		try {
			for (int j = 0; j < msgReplies.getJSONArray("records").length(); j++) {
				JSONObject objRes = msgReplies.getJSONArray("records")
						.getJSONObject(j);
				String message_id = objRes.getJSONArray("message_id")
						.getString(0);
				String read = (objRes.getString("read").equals("true")) ? "false"
						: "true";
				String starred = objRes.getString("starred");
				ContentValues values = new ContentValues();
				values.put("starred", starred);
				values.put("to_read", read);
				if (db.write(db, values, Integer.parseInt(message_id), true)) {
					updatedIds.put(Integer.parseInt(message_id));
				}

			}
		} catch (Exception e) {
		}
		return updatedIds;
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

}
