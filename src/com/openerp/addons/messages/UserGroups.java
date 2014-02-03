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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.openerp.OETouchListener;
import com.openerp.R;
import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.OEDataRow;
import com.openerp.orm.OEHelper;
import com.openerp.providers.groups.UserGroupsProvider;
import com.openerp.receivers.SyncFinishReceiver;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.OEUser;
import com.openerp.support.OpenERPServerConnection;
import com.openerp.support.listview.OEListAdapter;
import com.openerp.support.listview.OEListViewRow;
import com.openerp.util.Base64Helper;
import com.openerp.util.drawer.DrawerItem;

public class UserGroups extends BaseFragment implements
		OETouchListener.OnPullListener {
	public static final String TAG = "com.openerp.addons.UserGroups";
	private OETouchListener mTouchAttacher;
	View rootView = null;
	String tag_colors[] = new String[] { "#218559", "#192823", "#FF8800",
			"#CC0000", "#59A2BE", "#808080", "#9933CC", "#0099CC", "#669900",
			"#EBB035" };
	public static HashMap<String, Integer> menu_color = new HashMap<String, Integer>();
	public static HashMap<String, String> group_names = new HashMap<String, String>();

	GridView mGridGroups = null;
	GroupsLoader mGroupLoader = null;
	List<Object> mGroupsList = new ArrayList<Object>();
	OEListAdapter mGroupsAdapter = null;

	JoinUnfollowGroup joinUnfollow = null;
	MailFollowerDb follower = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		scope = new AppScope(this);
		db = (BaseDBHelper) databaseHelper(scope.context());
		rootView = inflater.inflate(R.layout.fragment_message_groups_list,
				container, false);
		setupGridView();
		return rootView;

	}

	private void setupGridView() {
		mGridGroups = (GridView) rootView.findViewById(R.id.listGroups);
		mGroupsAdapter = new OEListAdapter(getActivity(),
				R.layout.fragment_message_groups_list_item, mGroupsList) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View mView = convertView;
				if (mView == null) {
					mView = getActivity().getLayoutInflater().inflate(
							getResource(), parent, false);
				}
				OEListViewRow row = (OEListViewRow) mGroupsList.get(position);
				TextView txvName = (TextView) mView
						.findViewById(R.id.txvGroupName);
				TextView txvDesc = (TextView) mView
						.findViewById(R.id.txvGroupDescription);
				ImageView imgGroupPic = (ImageView) mView
						.findViewById(R.id.imgGroupPic);
				imgGroupPic.setImageBitmap(Base64Helper.getBitmapImage(
						getActivity(),
						row.getRow_data().getString("image_medium")));
				txvName.setText(row.getRow_data().getString("name"));
				txvDesc.setText(row.getRow_data().getString("description"));

				final int group_id = row.getRow_id();
				final Button btnJoin = (Button) mView
						.findViewById(R.id.btnJoinGroup);
				final Button btnUnJoin = (Button) mView
						.findViewById(R.id.btnUnJoinGroup);
				int total = follower.count(follower, new String[] {
						"res_model = ?", "AND", "res_id = ?", "AND",
						"partner_id = ?" }, new String[] { "mail.group",
						group_id + "", scope.User().getPartner_id() });

				btnUnJoin.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						joinUnfollow = new JoinUnfollowGroup(group_id, false);
						joinUnfollow.execute((Void) null);
						btnJoin.setVisibility(View.VISIBLE);
						btnUnJoin.setVisibility(View.GONE);
					}
				});
				btnJoin.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						joinUnfollow = new JoinUnfollowGroup(group_id, true);
						joinUnfollow.execute((Void) null);
						btnJoin.setVisibility(View.GONE);
						btnUnJoin.setVisibility(View.VISIBLE);
					}
				});
				if (total > 0) {
					btnJoin.setVisibility(View.GONE);
					btnUnJoin.setVisibility(View.VISIBLE);

				} else {
					btnJoin.setVisibility(View.VISIBLE);
					btnUnJoin.setVisibility(View.GONE);
				}
				return mView;
			}
		};
		mGridGroups.setAdapter(mGroupsAdapter);
		// Getting Pull To Refresh Attacher from Main Activity
		mTouchAttacher = scope.main().getTouchAttacher();
		// Set the Refreshable View to be the ListView and the refresh listener
		// to be this.
		mTouchAttacher.setPullableView(mGridGroups, this);
		mGroupLoader = new GroupsLoader();
		mGroupLoader.execute((Void) null);
	}

	@Override
	public Object databaseHelper(Context context) {
		MailFollowerDb follower = new MailFollowerDb(context);
		follower.createTable(follower.createStatement(follower));
		return new UserGroupsDb(context);
	}

	@Override
	public void onStart() {
		super.onStart();
		follower = new MailFollowerDb(scope.context());
		scope.main().setTitle("Join a Group");
	}

	private void checkStatus() {

		if (!db.isEmptyTable(db)) {
			scope.main().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					rootView.findViewById(R.id.groupSyncWaiter).setVisibility(
							View.GONE);
				}
			});
		} else {
			scope.main().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					rootView.findViewById(R.id.groupSyncWaiter).setVisibility(
							View.VISIBLE);
					TextView txvSyncDetail = (TextView) rootView
							.findViewById(R.id.txvMessageHeaderSubtitle);
					txvSyncDetail.setText("Your groups will appear shortly");
					Log.d(TAG, "Requesting for sync groups");
					scope.main().requestSync(UserGroupsProvider.AUTHORITY);
				}
			});
		}
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		db = (BaseDBHelper) databaseHelper(context);
		List<DrawerItem> drawer_items = new ArrayList<DrawerItem>();
		if (db.getOEInstance().isInstalled("mail.group")) {
			drawer_items.add(new DrawerItem(TAG, "My Groups", true));

			// default join group menu
			UserGroups grp = new UserGroups();
			grp.setArguments(new Bundle());
			drawer_items.add(new DrawerItem(TAG, "Join a Group", 0,
					R.drawable.ic_action_social_group, grp));

			// Add dynamic groups
			MailFollowerDb followers = new MailFollowerDb(context);
			List<OEDataRow> user_groups = followers.search(followers,
					new String[] { "res_model = ?", "AND", "partner_id = ? " },
					new String[] { db.getModelName(),
							OEUser.current(context).getPartner_id() });
			int total = user_groups.size();
			if (total > 0) {
				int i = 0;
				for (OEDataRow row : user_groups) {
					if (i > tag_colors.length - 1) {
						i = 0;
					}
					OEDataRow group_rec = db.search(db,
							new String[] { "id = ?" },
							new String[] { row.get("res_id").toString() }).get(
							0);
					String group_name = group_rec.getString("name");
					int key = group_rec.getInt("id");
					drawer_items.add(new DrawerItem(TAG, group_name,
							getGroupCount(context, key), tag_colors[i],
							getGroupInstance(key)));
					menu_color.put("group_" + key,
							Color.parseColor(tag_colors[i]));
					group_names.put("group_" + key, group_name);

					i++;
				}
			}
			return drawer_items;
		} else {
			return null;
		}
	}

	private Message getGroupInstance(int key) {
		Message groups = new Message();
		Bundle bundle = new Bundle();
		bundle.putString("group_id", key + "");
		groups.setArguments(bundle);
		return groups;
	}

	private int getGroupCount(Context context, int group_id) {
		MessageDBHelper messages = new MessageDBHelper(context);
		return messages.count(messages, new String[] { "to_read = ?", "AND",
				"model = ?", "AND", "res_id = ?" }, new String[] { "true",
				"mail.group", String.valueOf(group_id) });
	}

	@Override
	public void onPause() {
		super.onPause();
		scope.context().unregisterReceiver(syncFinishReceiver);
	}

	@Override
	public void onResume() {
		super.onResume();
		scope.context().registerReceiver(syncFinishReceiver,
				new IntentFilter(SyncFinishReceiver.SYNC_FINISH));
	}

	// sync Pull
	// Allow Activity to pass us it's OETouchListener
	void setTouchAttacher(OETouchListener attacher) {
		mTouchAttacher = attacher;
	}

	private SyncFinishReceiver syncFinishReceiver = new SyncFinishReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			mTouchAttacher.setPullComplete();
			mGroupLoader = new GroupsLoader();
			mGroupLoader.execute();
			scope.main().refreshDrawer(TAG, context);
		}

	};

	public class JoinUnfollowGroup extends AsyncTask<Void, Void, Boolean> {
		int group_id = 0;
		boolean join = false;
		String toast_message = "";
		JSONObject result = new JSONObject();

		public JoinUnfollowGroup(int group_id, boolean join) {
			this.group_id = group_id;
			this.join = join;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				follower = new MailFollowerDb(scope.context());
				int partner_id = Integer.parseInt(scope.User().getPartner_id());
				OEHelper oe = db.getOEInstance();
				JSONArray args = new JSONArray();
				args.put(new JSONArray("[" + this.group_id + "]"));
				args.put(oe.updateContext(new JSONObject()));
				if (this.join) {
					result = oe.call_kw("mail.group", "action_follow", args);
					JSONObject fields = new JSONObject();
					fields.accumulate("fields", "res_model");
					fields.accumulate("fields", "res_id");
					fields.accumulate("fields", "partner_id");
					JSONObject domain = new JSONObject();
					domain.accumulate("domain", new JSONArray(
							"[[\"res_model\", \"=\", \"mail.group\"], [\"res_id\",\"=\","
									+ this.group_id
									+ "] ,[\"partner_id\",\"=\", " + partner_id
									+ "]]"));
					JSONObject result_data = oe.search_read("mail.followers",
							null, domain, 0, 0, null, null);
					ContentValues data_vals = new ContentValues();
					data_vals.put("id", result_data.getJSONArray("records")
							.getJSONObject(0).getString("id"));
					data_vals.put("res_model", "mail.group");
					data_vals.put("res_id", this.group_id);
					data_vals.put("partner_id",
							Integer.parseInt(scope.User().getPartner_id()));
					@SuppressWarnings("unused")
					int newId = follower.create(follower, data_vals);
					toast_message = "Group joined";
				} else {
					result = oe.call_kw("mail.group", "action_unfollow", args);

					List<HashMap<String, Object>> mail_follower_ids = follower
							.executeSQL(follower.getModelName(),
									new String[] { "id" }, new String[] {
											"res_model = ?", "AND",
											"res_id = ?", "AND",
											"partner_id = ?" }, new String[] {
											"mail.group", group_id + "",
											scope.User().getPartner_id() });
					if (mail_follower_ids.size() > 0) {
						int id = Integer.parseInt(mail_follower_ids.get(0)
								.get("id").toString());
						follower.delete(follower, id, true);
					}
					toast_message = "Unfollowed from group";
				}
				scope.main().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						try {
							if (result.getBoolean("result")) {
								Toast.makeText(scope.context(), toast_message,
										Toast.LENGTH_LONG).show();
							}
						} catch (Exception e) {
						}
						scope.main().refreshDrawer(TAG, scope.context());
					}
				});
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	public class GroupsLoader extends AsyncTask<Void, Void, Boolean> {

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
		protected Boolean doInBackground(Void... arg0) {
			mGroupsList.clear();
			List<OEDataRow> group_data = db.search(db);
			if (group_data.size() > 0) {
				for (OEDataRow row : group_data) {
					int id = row.getInt("id");
					OEListViewRow row_data = new OEListViewRow(id, row);
					mGroupsList.add(row_data);
				}
			}
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			scope.main().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					try {
						rootView.findViewById(R.id.loadingProgress)
								.setVisibility(View.GONE);
						mGroupsAdapter.notifiyDataChange(mGroupsList);
						mGroupLoader = null;
						checkStatus();
					} catch (Exception e) {
					}
				}
			});
		}

	}

	@Override
	public void onPullStarted(View arg0) {
		try {
			if (OpenERPServerConnection.isNetworkAvailable(getActivity())) {
				Log.i("UserGroupsFragment", "requesting for sync");
				scope.main().requestSync(UserGroupsProvider.AUTHORITY);
			} else {
				Toast.makeText(getActivity(), "Unable to connect server !",
						Toast.LENGTH_LONG).show();
				mTouchAttacher.setPullComplete();
			}
		} catch (Exception e) {
		}
	}
}
