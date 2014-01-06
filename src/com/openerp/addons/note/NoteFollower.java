package com.openerp.addons.note;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.GridView;

import com.openerp.R;
import com.openerp.addons.messages.ReceipientsTagsCustomAdapter;
import com.openerp.base.res.Res_PartnerDBHelper;
import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.OEColumn;
import com.openerp.orm.OEDataRow;
import com.openerp.orm.OEHelper;
import com.openerp.orm.OEM2MRecord;
import com.openerp.providers.note.NoteProvider;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.JSONDataHelper;
import com.openerp.support.OEUser;
import com.openerp.support.listview.OEListViewAdapter;
import com.openerp.support.listview.OEListViewRow;
import com.openerp.util.Base64Helper;
import com.openerp.util.contactview.OEContactView;
import com.openerp.util.controls.OETextView;
import com.openerp.util.drawer.DrawerItem;
import com.openerp.util.logger.OELog;
import com.openerp.util.tags.TagsItem;
import com.openerp.util.tags.TagsView;

public class NoteFollower extends BaseFragment implements
		TagsView.TokenListener {
	View mView = null;
	int mNoteId = 0;
	String mNoteName = "";

	List<OEListViewRow> mFollowerList = new ArrayList<OEListViewRow>();
	GridView mNoteFollowerGrid = null;
	OEListViewAdapter mNoteGridAdatper;

	List<TagsItem> mPartnerList = new ArrayList<TagsItem>();
	TagsView edtFollowerAdd = null;
	ReceipientsTagsCustomAdapter mPartnerAdapter = null;

	HashMap<String, TagsItem> mSelectedPartners = new HashMap<String, TagsItem>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		setHasOptionsMenu(true);
		scope = new AppScope(this);
		db = (BaseDBHelper) databaseHelper(scope.context());
		mView = inflater.inflate(R.layout.fragment_note_followers, container,
				false);
		getActivity().setTitle("Followers");
		mNoteFollowerGrid = (GridView) mView
				.findViewById(R.id.noteFollowersGridView);
		edtFollowerAdd = (TagsView) mView.findViewById(R.id.edtNoteFollowers);
		return mView;
	}

	@Override
	public void onStart() {
		super.onStart();
		Bundle bundle = getArguments();
		if (bundle != null && bundle.containsKey("note_id")) {
			mNoteId = bundle.getInt("note_id");
			mNoteName = bundle.getString("note_name");
		}
		List<OEDataRow> records = db.search(db, new String[] { "res_id = ? " },
				new String[] { mNoteId + "" });
		for (OEDataRow record : records) {
			int partner_id = Integer.parseInt(OEM2MRecord
					.get(record.getString("partner_id")).get("id").toString());
			Res_PartnerDBHelper partnerDb = new Res_PartnerDBHelper(
					scope.context());
			List<OEDataRow> partner = partnerDb
					.search(partnerDb, new String[] { "id = ?" },
							new String[] { partner_id + "" });
			OEDataRow row = partner.get(0);
			mFollowerList.add(new OEListViewRow(row.getInt("id"), row));
		}
		getPartnersFromServer();
		if (records.size() > 0) {
			setupGridView();
		} else {
			mView.findViewById(R.id.txvNoteFollowerStatus).setVisibility(
					View.VISIBLE);
		}

		setupQuickFollowerAdd();

		mView.findViewById(R.id.imgBtnAddFollower).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						mView.findViewById(R.id.txvNoteFollowerStatus)
								.setVisibility(View.GONE);
						setupGridView();
						JSONArray partners_id = new JSONArray();
						for (String key : mSelectedPartners.keySet()) {
							TagsItem item = (TagsItem) mSelectedPartners
									.get(key);
							partners_id.put(item.getId());
							OEDataRow row = new OEDataRow();
							row.put("id", item.getId());
							row.put("name", item.getSubject());
							row.put("email", item.getSub_subject());
							row.put("image_small", item.getImage());
							mFollowerList.add(0, new OEListViewRow(
									item.getId(), row));
							edtFollowerAdd.removeObject(item);
						}
						addFolloers(partners_id);
						mNoteGridAdatper.refresh(mFollowerList);
						scope.main().requestSync(NoteProvider.AUTHORITY);
					}
				});
	}

	private boolean unsubscribe(int partner_id) {
		Res_PartnerDBHelper res_partners = new Res_PartnerDBHelper(
				scope.context());
		OEHelper oe = res_partners.getOEInstance();
		try {
			JSONArray arguments = new JSONArray();
			JSONObject newContext = oe.updateContext(new JSONObject());
			arguments.put(new JSONArray("[" + mNoteId + "]"));
			arguments.put(new JSONArray("[" + partner_id + "]"));
			arguments.put(newContext);
			oe.call_kw("note.note", "message_unsubscribe", arguments);
			db.executeSQL(
					"delete from mail_followers where res_id = ? and partner_id = ? and oea_name = ? and res_model = 'note.note'",
					new String[] { mNoteId + "", partner_id + "",
							OEUser.current(scope.context()).getAndroidName() });
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void addFolloers(JSONArray partners_id) {
		Res_PartnerDBHelper res_partners = new Res_PartnerDBHelper(
				scope.context());
		OEHelper oe = res_partners.getOEInstance();
		try {
			JSONObject args = new JSONObject();
			args.put("res_model", "note.note");
			args.put("res_id", mNoteId);
			args.put("message", "You have been invited to follow " + mNoteName);
			JSONArray partner_ids = new JSONArray();
			partner_ids.put(6);
			partner_ids.put(false);
			partner_ids.put(partners_id);
			args.put("partner_ids", new JSONArray("[" + partner_ids.toString()
					+ "]"));
			JSONObject result = oe.createNew("mail.wizard.invite", args);
			int id = result.getInt("result");

			// calling mail.wizard.invite method
			JSONArray arguments = new JSONArray();
			JSONArray result_id = new JSONArray();
			result_id.put(id);
			arguments.put(result_id);

			JSONObject newValues = new JSONObject();
			newValues.put("default_res_model", "note.note");
			newValues.put("default_res_id", args.getInt("res_id"));
			JSONObject newContext = oe.updateContext(newValues);
			arguments.put(newContext);
			oe.call_kw("mail.wizard.invite", "add_followers", arguments);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setupQuickFollowerAdd() {
		mPartnerAdapter = new ReceipientsTagsCustomAdapter(scope.context(),
				R.layout.tags_view_partner_item_layout, mPartnerList);
		edtFollowerAdd.setAdapter(mPartnerAdapter);
		edtFollowerAdd.setTokenListener(this);
	}

	private void setupGridView() {
		mNoteGridAdatper = new OEListViewAdapter(scope.context(),
				R.layout.fragment_note_followers_grid_item_view, mFollowerList,
				null, null, db) {
			View mGridView = null;

			@Override
			public View getView(final int position, View convertView,
					ViewGroup parent) {
				mGridView = convertView;
				if (mGridView == null) {
					LayoutInflater inflater = getActivity().getLayoutInflater();
					mGridView = inflater.inflate(
							R.layout.fragment_note_followers_grid_item_view,
							parent, false);
				}
				final OEDataRow row = mFollowerList.get(position).getRow_data();

				OEContactView imgContact = (OEContactView) mGridView
						.findViewById(R.id.imgFollowerPic);

				OETextView txvName = (OETextView) mGridView
						.findViewById(R.id.txvFollowerName);
				OETextView txvEmail = (OETextView) mGridView
						.findViewById(R.id.txvFollowerEmail);
				txvName.setText(row.getString("name"));
				txvEmail.setText(row.getString("email"));

				imgContact.assignPartnerId(row.getInt("id"));
				imgContact.setImageBitmap(Base64Helper.getBitmapImage(
						scope.context(), row.getString("image_small")));
				mGridView.findViewById(R.id.imgFollowerRemove)
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								confirmRemoveFollower(row.getInt("id"),
										position);
							}
						});
				return mGridView;
			}

		};
		mNoteFollowerGrid.setAdapter(mNoteGridAdatper);
	}

	private void confirmRemoveFollower(final int partner_id, final int position) {
		AlertDialog.Builder deleteDialogConfirm = new AlertDialog.Builder(
				scope.context());
		deleteDialogConfirm.setTitle("Unsubscribe");
		deleteDialogConfirm
				.setMessage("Are you sure want to remove follower ?");
		deleteDialogConfirm.setCancelable(true);

		deleteDialogConfirm.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if (unsubscribe(partner_id)) {
							mFollowerList.remove(position);
							mNoteGridAdatper.refresh(mFollowerList);
						}
					}
				});

		deleteDialogConfirm.setNegativeButton("No", null);
		deleteDialogConfirm.show();
	}

	@Override
	public Object databaseHelper(Context context) {
		NoteDBHelper noteDb = new NoteDBHelper(context);
		return noteDb.new NoteFollowers(context);
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		return null;
	}

	private Boolean getPartnersFromServer() {
		boolean flag = true;
		Res_PartnerDBHelper res_partners = new Res_PartnerDBHelper(
				scope.context());
		OEHelper oe = res_partners.getOEInstance();
		try {
			ArrayList<OEColumn> cols = res_partners.getServerColumns();
			JSONObject fields = new JSONObject();
			for (OEColumn field : cols) {
				fields.accumulate("fields", field.getName());
			}
			JSONArray ids = JSONDataHelper.intArrayToJSONArray(oe
					.getAllIds(res_partners));
			JSONObject result = oe.search_read("res.partner", fields, null, 0,
					0, null, null);
			for (int i = 0; i < result.getInt("length"); i++) {
				JSONObject row = result.getJSONArray("records")
						.getJSONObject(i);
				int id = row.getInt("id");

				mPartnerList.add(new TagsItem(id, row.getString("name")
						.toString(), row.getString("email").toString(), row
						.getString("image_small")));
			}
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}
		return flag;

	}

	@Override
	public void onTokenAdded(Object token, View view) {
		TagsItem tag = (TagsItem) token;
		mSelectedPartners.put("_" + tag.getId(), tag);
	}

	@Override
	public void onTokenSelected(Object token, View view) {

	}

	@Override
	public void onTokenRemoved(Object token) {
		TagsItem tag = (TagsItem) token;
		mSelectedPartners.remove("_" + tag.getId());
	}
}
