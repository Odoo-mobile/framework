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
import android.widget.TextView;
import android.widget.Toast;

import com.openerp.PullToRefreshAttacher;
import com.openerp.R;
import com.openerp.auth.OpenERPAccountManager;
import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.OEHelper;
import com.openerp.providers.groups.UserGroupsProvider;
import com.openerp.receivers.SyncFinishReceiver;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.OpenERPServerConnection;
import com.openerp.support.listview.OEListViewAdapter;
import com.openerp.support.listview.OEListViewOnCreateListener;
import com.openerp.support.listview.OEListViewRows;
import com.openerp.util.drawer.DrawerItem;

public class UserGroups extends BaseFragment implements
		PullToRefreshAttacher.OnRefreshListener {
	public static final String TAG = "com.openerp.addons.UserGroups";
	private PullToRefreshAttacher mPullToRefreshAttacher;
	View rootView = null;
	GridView lstGroups = null;
	String tag_colors[] = new String[] { "#218559", "#192823", "#FF8800",
			"#CC0000", "#59A2BE", "#808080", "#9933CC", "#0099CC", "#669900",
			"#EBB035" };
	public static HashMap<String, Integer> menu_color = new HashMap<String, Integer>();
	public static HashMap<String, String> group_names = new HashMap<String, String>();
	LoadGroups groups_loader = null;
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
		return rootView;
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
		scope.context().setTitle("Join a Group");
		lstGroups = (GridView) rootView.findViewById(R.id.listGroups);
		groups_loader = new LoadGroups();
		groups_loader.execute((Void) null);

	}

	private Boolean setupView() {
		List<OEListViewRows> groups = getGroups();
		String[] from = new String[] { "name", "image_medium", "description" };
		int[] to = new int[] { R.id.txvGroupName, R.id.imgGroupPic,
				R.id.txvGroupDescription };
		final OEListViewAdapter adapter = new OEListViewAdapter(
				scope.context(), R.layout.fragment_message_groups_list_item,
				groups, from, to, db);
		adapter.addViewListener(new OEListViewOnCreateListener() {

			@Override
			public View listViewOnCreateListener(int position, View row_view,
					OEListViewRows row_data) {
				final int group_id = row_data.getRow_id();
				final Button btnJoin = (Button) row_view
						.findViewById(R.id.btnJoinGroup);
				final Button btnUnJoin = (Button) row_view
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
				return row_view;
			}
		});
		adapter.addImageColumn("image_medium");
		scope.context().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				lstGroups.setAdapter(adapter);
			}
		});

		// Getting Pull To Refresh Attacher from Main Activity
		mPullToRefreshAttacher = scope.context().getPullToRefreshAttacher();

		// Set the Refreshable View to be the ListView and the refresh listener
		// to be this.
		if (mPullToRefreshAttacher != null & lstGroups != null) {
			mPullToRefreshAttacher.setRefreshableView(lstGroups, this);
		}
		return true;

	}

	private List<OEListViewRows> getGroups() {
		List<OEListViewRows> groups = new ArrayList<OEListViewRows>();

		if (!db.isEmptyTable(db)) {
			scope.context().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					rootView.findViewById(R.id.groupSyncWaiter).setVisibility(
							View.GONE);
					rootView.findViewById(R.id.listGroups).setVisibility(
							View.VISIBLE);
				}
			});

			HashMap<String, Object> group_data = db.search(db);
			if (Integer.parseInt(group_data.get("total").toString()) > 0) {
				List<HashMap<String, Object>> datas = (List<HashMap<String, Object>>) group_data
						.get("records");
				for (HashMap<String, Object> row : datas) {
					int id = Integer.parseInt(row.get("id").toString());
					OEListViewRows row_data = new OEListViewRows(id, row);
					groups.add(row_data);
				}
			}

		} else {
			scope.context().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					rootView.findViewById(R.id.groupSyncWaiter).setVisibility(
							View.VISIBLE);
					TextView txvSyncDetail = (TextView) rootView
							.findViewById(R.id.txvMessageHeaderSubtitle);
					txvSyncDetail.setText("Your groups will appear shortly");
					rootView.findViewById(R.id.listGroups).setVisibility(
							View.GONE);
					Log.d(TAG, "Requesting for sync groups");
					scope.context().requestSync(UserGroupsProvider.AUTHORITY);
				}
			});
		}
		return groups;
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
			HashMap<String, Object> user_groups = followers.search(followers,
					new String[] { "res_model = ?", "AND", "partner_id = ? " },
					new String[] {
							db.getModelName(),
							OpenERPAccountManager.currentUser(context)
									.getPartner_id() });
			int total = Integer.parseInt(user_groups.get("total").toString());
			if (total > 0) {
				int i = 0;
				List<HashMap<String, Object>> records = (List<HashMap<String, Object>>) user_groups
						.get("records");
				for (HashMap<String, Object> row : records) {
					if (i > tag_colors.length - 1) {
						i = 0;
					}

					List<HashMap<String, Object>> group_rec = (List<HashMap<String, Object>>) db
							.search(db,
									new String[] { "id = ?" },
									new String[] { row.get("res_id").toString() })
							.get("records");
					String group_name = group_rec.get(0).get("name").toString();
					int key = Integer.parseInt(group_rec.get(0).get("id")
							.toString());
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

	// PullToRefresh
	// Allow Activity to pass us it's PullToRefreshAttacher
	void setPullToRefreshAttacher(PullToRefreshAttacher attacher) {
		mPullToRefreshAttacher = attacher;
	}

	private SyncFinishReceiver syncFinishReceiver = new SyncFinishReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			mPullToRefreshAttacher.setRefreshComplete();
			setupView();
			scope.context().refreshDrawer(TAG, context);
		}

	};

	@Override
	public void onRefreshStarted(View arg0) {
		try {
			if (OpenERPServerConnection.isNetworkAvailable(getActivity())) {
				Log.i("UserGroupsFragment", "requesting for sync");
				scope.context().requestSync(UserGroupsProvider.AUTHORITY);
			} else {
				Toast.makeText(getActivity(), "Unable to connect server !",
						Toast.LENGTH_LONG).show();
				mPullToRefreshAttacher.setRefreshComplete();
			}
		} catch (Exception e) {
		}
	}

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
				scope.context().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						try {
							if (result.getBoolean("result")) {
								Toast.makeText(scope.context(), toast_message,
										Toast.LENGTH_LONG).show();
							}
						} catch (Exception e) {
						}
						scope.context().refreshDrawer(TAG, scope.context());
					}
				});
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	public class LoadGroups extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			scope.context().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					rootView.findViewById(R.id.loadingHeader).setVisibility(
							View.VISIBLE);
					rootView.findViewById(R.id.listGroups).setVisibility(
							View.GONE);
				}
			});
		}

		@Override
		protected Boolean doInBackground(Void... arg0) {
			return setupView();
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			scope.context().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					try {
						rootView.findViewById(R.id.loadingHeader)
								.setVisibility(View.GONE);
						rootView.findViewById(R.id.listGroups).setVisibility(
								View.VISIBLE);
						groups_loader = null;
					} catch (Exception e) {
					}
				}
			});
		}

	}
}
