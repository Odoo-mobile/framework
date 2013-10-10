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

import com.openerp.MainActivity;
import com.openerp.R;
import com.openerp.auth.OpenERPAccountManager;
import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.OEHelper;
import com.openerp.providers.groups.UserGroupsProvider;
import com.openerp.receivers.SyncFinishReceiver;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.listview.OEListViewAdapter;
import com.openerp.support.listview.OEListViewOnCreateListener;
import com.openerp.support.listview.OEListViewRows;
import com.openerp.support.menu.OEMenu;
import com.openerp.support.menu.OEMenuItems;

public class UserGroups extends BaseFragment {
	public static final String TAG = "UserGroups";
	View rootView = null;
	GridView lstGroups = null;
	String tag_colors[] = new String[] { "#218559", "#192823", "#FF8800",
			"#CC0000", "#59A2BE", "#808080", "#9933CC", "#0099CC", "#669900",
			"#EBB035" };
	HashMap<String, Integer> menu_color = new HashMap<String, Integer>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		scope = new AppScope(MainActivity.userContext,
				(MainActivity) getActivity());
		db = (BaseDBHelper) databaseHelper(scope.context());
		rootView = inflater.inflate(R.layout.fragment_message_groups_list,
				container, false);
		handleArguments(getArguments());
		return rootView;
	}

	@Override
	public Object databaseHelper(Context context) {
		MailFollowerDb follower = new MailFollowerDb(context);
		follower.createTable(follower.createStatement(follower));
		return new UserGroupsDb(context);
	}

	@Override
	public void handleArguments(Bundle bundle) {
		scope.context().setTitle("Join a Group");
		lstGroups = (GridView) rootView.findViewById(R.id.listGroups);
		setupView();
	}

	private void setupView() {
		List<OEListViewRows> groups = getGroups();
		String[] from = new String[] { "name", "image_medium", "description" };
		int[] to = new int[] { R.id.txvGroupName, R.id.imgGroupPic,
				R.id.txvGroupDescription };
		OEListViewAdapter adapter = new OEListViewAdapter(scope.context(),
				R.layout.fragment_message_groups_list_item, groups, from, to,
				db);
		final MailFollowerDb follower = new MailFollowerDb(scope.context());
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
				final OEHelper oe = db.getOEInstance();
				final JSONArray args = new JSONArray();

				final List<HashMap<String, Object>> mail_follower_ids = follower
						.executeSQL(follower.getModelName(),
								new String[] { "id" }, new String[] {
										"res_model = ?", "AND", "res_id = ?",
										"AND", "partner_id = ?" },
								new String[] { "mail.group", group_id + "",
										scope.User().getPartner_id() });
				try {
					args.put(new JSONArray("[" + group_id + "]"));
					args.put(oe.updateContext(new JSONObject()));
				} catch (Exception e) {
				}
				btnUnJoin.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						try {
							JSONObject result = oe.call_kw("mail.group",
									"action_unfollow", args);
							if (result.getBoolean("result")) {
								if (mail_follower_ids.size() > 0) {
									follower.delete(
											follower,
											Integer.parseInt(mail_follower_ids
													.get(0).get("id")
													.toString()));
									Toast.makeText(scope.context(),
											"Unfollowed from group",
											Toast.LENGTH_LONG).show();
									btnJoin.setVisibility(View.VISIBLE);
									btnUnJoin.setVisibility(View.GONE);
									scope.context().refreshMenu(scope.context());
								}

							}

						} catch (Exception e) {
						}
					}
				});
				btnJoin.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						try {
							ContentValues data_vals = new ContentValues();
							data_vals.put("res_model", "mail.group");
							data_vals.put("res_id", group_id);
							data_vals.put("partner_id", Integer.parseInt(scope
									.User().getPartner_id()));
							follower.create(follower, data_vals);
							Toast.makeText(scope.context(), "Group joined",
									Toast.LENGTH_LONG).show();
							btnJoin.setVisibility(View.GONE);
							btnUnJoin.setVisibility(View.VISIBLE);
							scope.context().refreshMenu(scope.context());
						} catch (Exception e) {
						}
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
		lstGroups.setAdapter(adapter);

	}

	private List<OEListViewRows> getGroups() {
		List<OEListViewRows> groups = new ArrayList<OEListViewRows>();

		if (!db.isEmptyTable(db)) {
			rootView.findViewById(R.id.groupSyncWaiter)
					.setVisibility(View.GONE);
			rootView.findViewById(R.id.listGroups).setVisibility(View.VISIBLE);

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
			rootView.findViewById(R.id.groupSyncWaiter).setVisibility(
					View.VISIBLE);
			TextView txvSyncDetail = (TextView) rootView
					.findViewById(R.id.txvMessageHeaderSubtitle);
			txvSyncDetail.setText("Your groups will appear shortly");
			rootView.findViewById(R.id.listGroups).setVisibility(View.GONE);
			Log.d(TAG, "Requesting for sync groups");
			scope.context().requestSync(UserGroupsProvider.AUTHORITY);
		}

		return groups;
	}

	@Override
	public OEMenu menuHelper(Context context) {
		db = (BaseDBHelper) databaseHelper(context);
		OEMenu group_menu = new OEMenu();
		group_menu.setMenuTitle("My Groups");
		group_menu.setId(0);

		List<OEMenuItems> group_menu_items = new ArrayList<OEMenuItems>();
		// default join group menu
		group_menu_items.add(new OEMenuItems("Join a Group", new UserGroups(),
				0));

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
						.search(db, new String[] { "id = ?" },
								new String[] { row.get("res_id").toString() })
						.get("records");
				String group_name = group_rec.get(0).get("name").toString();
				int key = Integer.parseInt(group_rec.get(0).get("id")
						.toString());
				OEMenuItems group_menu_item = new OEMenuItems(group_name,
						getGroupInstance(key), getGroupCount(context, key));
				group_menu_item.setAutoMenuTagColor(true);
				group_menu_item
						.setMenuTagColor(Color.parseColor(tag_colors[i]));
				menu_color.put("group_" + key,
						group_menu_item.getMenuTagColor());
				group_menu_items.add(group_menu_item);
				i++;
			}
		}
		group_menu.setMenuItems(group_menu_items);
		return group_menu;
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

	private SyncFinishReceiver syncFinishReceiver = new SyncFinishReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			setupView();
		}

	};

}
