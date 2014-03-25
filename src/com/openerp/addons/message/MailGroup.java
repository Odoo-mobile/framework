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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import openerp.OEArguments;

import org.json.JSONArray;
import org.json.JSONObject;

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
import com.openerp.OETouchListener.OnPullListener;
import com.openerp.R;
import com.openerp.addons.message.providers.groups.MailGroupProvider;
import com.openerp.base.mail.MailFollowers;
import com.openerp.orm.OEDataRow;
import com.openerp.orm.OEHelper;
import com.openerp.receivers.SyncFinishReceiver;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.OEUser;
import com.openerp.support.listview.OEListAdapter;
import com.openerp.util.Base64Helper;
import com.openerp.util.drawer.DrawerItem;
import com.openerp.util.drawer.DrawerListener;

public class MailGroup extends BaseFragment implements OnPullListener {

	public static final String TAG = "com.openerp.addons.message.MailGroup";

	/**
	 * OETouchListener
	 */
	private OETouchListener mTouchAttacher;

	View mView = null;
	Boolean hasSynced = false;
	GridView mGroupGridView = null;
	List<Object> mGroupGridListItems = new ArrayList<Object>();
	OEListAdapter mGroupGridViewAdapter = null;

	/**
	 * Tag Colors
	 */
	public static HashMap<String, Object> mMenuGroups = new HashMap<String, Object>();
	String mTagColors[] = new String[] { "#218559", "#192823", "#FF8800",
			"#CC0000", "#59A2BE", "#808080", "#9933CC", "#0099CC", "#669900",
			"#EBB035" };
	/**
	 * Loaders
	 */
	GroupsLoader mGroupsLoader = null;
	JoinUnfollowGroup mJoinUnFollowGroup = null;

	/**
	 * Database Objects
	 */
	MailFollowers mMailFollowerDB = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_message_groups_list,
				container, false);
		init();
		return mView;
	}

	private void init() {
		scope = new AppScope(getActivity());
		mMailFollowerDB = new MailFollowers(getActivity());
		initControls();
		mGroupsLoader = new GroupsLoader();
		mGroupsLoader.execute();
	}

	private void initControls() {
		mGroupGridView = (GridView) mView.findViewById(R.id.listGroups);
		mGroupGridViewAdapter = new OEListAdapter(getActivity(),
				R.layout.fragment_message_groups_list_item, mGroupGridListItems) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View mView = convertView;
				if (mView == null)
					mView = getActivity().getLayoutInflater().inflate(
							getResource(), parent, false);
				generateView(position, mView);
				return mView;
			}
		};
		mGroupGridView.setAdapter(mGroupGridViewAdapter);
		mTouchAttacher = scope.main().getTouchAttacher();
		mTouchAttacher.setPullableView(mGroupGridView, this);
	}

	private void generateView(int position, View mView) {
		OEDataRow row = (OEDataRow) mGroupGridListItems.get(position);
		TextView txvName = (TextView) mView.findViewById(R.id.txvGroupName);
		TextView txvDesc = (TextView) mView
				.findViewById(R.id.txvGroupDescription);
		ImageView imgGroupPic = (ImageView) mView
				.findViewById(R.id.imgGroupPic);
		imgGroupPic.setImageBitmap(Base64Helper.getBitmapImage(getActivity(),
				row.getString("image_medium")));
		txvName.setText(row.getString("name"));
		txvDesc.setText(row.getString("description"));

		final int group_id = row.getInt("id");
		final Button btnJoin = (Button) mView.findViewById(R.id.btnJoinGroup);
		final Button btnUnJoin = (Button) mView
				.findViewById(R.id.btnUnJoinGroup);
		int total = mMailFollowerDB.count(
				"res_model = ? AND res_id = ? AND partner_id = ?",
				new String[] { db().getModelName(), group_id + "",
						OEUser.current(getActivity()).getPartner_id() + "" });
		btnUnJoin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mJoinUnFollowGroup = new JoinUnfollowGroup(group_id, false);
				mJoinUnFollowGroup.execute();
				btnJoin.setVisibility(View.VISIBLE);
				btnUnJoin.setVisibility(View.GONE);
			}
		});
		btnJoin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mJoinUnFollowGroup = new JoinUnfollowGroup(group_id, true);
				mJoinUnFollowGroup.execute();
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
	}

	@Override
	public Object databaseHelper(Context context) {
		return new MailGroupDB(context);
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {

		MailGroupDB db = new MailGroupDB(context);
		mMailFollowerDB = new MailFollowers(context);

		List<DrawerItem> menu = new ArrayList<DrawerItem>();

		MailGroup group = new MailGroup();
		Bundle bundle = new Bundle();

		if (db.isInstalledOnServer()) {
			menu.add(new DrawerItem(TAG, "My Groups", true));

			// Join Group
			group.setArguments(bundle);
			menu.add(new DrawerItem(TAG, "Join Group", 0,
					R.drawable.ic_action_social_group, group));

			// Dynamic Groups
			List<OEDataRow> groups = mMailFollowerDB.select(
					"res_model = ? AND partner_id = ?",
					new String[] { db.getModelName(),
							OEUser.current(context).getPartner_id() + "" });
			int index = 0;
			MessageDB messageDB = new MessageDB(context);
			for (OEDataRow row : groups) {
				if (mTagColors.length - 1 < index)
					index = 0;
				OEDataRow grp = db.select(row.getInt("res_id"));

				Message message = new Message();
				bundle = new Bundle();
				bundle.putInt("group_id", grp.getInt("id"));
				message.setArguments(bundle);

				int count = messageDB.count(
						"to_read = ? AND model = ? AND res_id = ?",
						new String[] { "true", db().getModelName(),
								row.getString("id") });
				menu.add(new DrawerItem(TAG, grp.getString("name"), count,
						mTagColors[index], message));
				grp.put("tag_color", Color.parseColor(mTagColors[index]));
				mMenuGroups.put("group_" + grp.getInt("id"), grp);
				index++;
			}
		}
		return menu;
	}

	@Override
	public void onPullStarted(View arg0) {
		Log.d(TAG, "MailGroup->OETouchListener->onPullStarted()");
		scope.main().requestSync(MailGroupProvider.AUTHORITY);

	}

	class GroupsLoader extends AsyncTask<Void, Void, Void> {

		public GroupsLoader() {
			mView.findViewById(R.id.loadingProgress)
					.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... params) {
			mGroupGridListItems.clear();
			mGroupGridListItems.addAll(db().select());
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mView.findViewById(R.id.loadingProgress).setVisibility(View.GONE);
			mGroupGridViewAdapter.notifiyDataChange(mGroupGridListItems);
			mGroupsLoader = null;
			checkStatus();
		}
	}

	private void checkStatus() {
		if (!db().isEmptyTable()) {
			mView.findViewById(R.id.groupSyncWaiter).setVisibility(View.GONE);
		} else {
			mView.findViewById(R.id.groupSyncWaiter)
					.setVisibility(View.VISIBLE);
			TextView txvSyncDetail = (TextView) mView
					.findViewById(R.id.txvMessageHeaderSubtitle);
			txvSyncDetail.setText("Your groups will appear shortly");
			if (!hasSynced) {
				hasSynced = true;
				scope.main().requestSync(MailGroupProvider.AUTHORITY);
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().registerReceiver(syncFinishReceiver,
				new IntentFilter(SyncFinishReceiver.SYNC_FINISH));
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(syncFinishReceiver);
	}

	private SyncFinishReceiver syncFinishReceiver = new SyncFinishReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			mTouchAttacher.setPullComplete();
			mGroupsLoader = new GroupsLoader();
			mGroupsLoader.execute();
			DrawerListener drawer = (DrawerListener) getActivity();
			drawer.refreshDrawer(TAG);
			drawer.refreshDrawer(Message.TAG);
		}

	};

	public class JoinUnfollowGroup extends AsyncTask<Void, Void, Boolean> {
		int mGroupId = 0;
		boolean mJoin = false;
		String mToast = "";
		JSONObject result = new JSONObject();

		public JoinUnfollowGroup(int group_id, boolean join) {
			mGroupId = group_id;
			mJoin = join;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				if (mMailFollowerDB == null)
					mMailFollowerDB = new MailFollowers(getActivity());
				int partner_id = OEUser.current(getActivity()).getPartner_id();
				OEHelper oe = db().getOEInstance();
				if (oe == null) {
					mToast = "No Connection";
					return false;
				}

				OEArguments arguments = new OEArguments();
				arguments.add(new JSONArray().put(mGroupId));
				arguments.add(oe.updateContext(new JSONObject()));
				if (mJoin) {
					oe.call_kw("action_follow", arguments, null);
					mToast = "Group joined";
					oe.syncWithServer(true);
				} else {
					oe.call_kw("action_unfollow", arguments, null);
					mToast = "Unfollowed from group";
					mMailFollowerDB.delete(
							"res_id = ? AND partner_id = ? AND res_model = ? ",
							new String[] { mGroupId + "", partner_id + "",
									db().getModelName() });

				}
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			Toast.makeText(getActivity(), mToast, Toast.LENGTH_LONG).show();
			DrawerListener drawer = (DrawerListener) getActivity();
			drawer.refreshDrawer(TAG);
			drawer.refreshDrawer(Message.TAG);
		}
	}
}
