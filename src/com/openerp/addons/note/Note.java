package com.openerp.addons.note;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import openerp.OEDomain;

import org.json.JSONArray;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.openerp.OESwipeListener.SwipeCallbacks;
import com.openerp.OETouchListener;
import com.openerp.R;
import com.openerp.addons.note.NoteDB.NoteStages;
import com.openerp.orm.OEDataRow;
import com.openerp.orm.OEHelper;
import com.openerp.orm.OEValues;
import com.openerp.providers.note.NoteProvider;
import com.openerp.receivers.SyncFinishReceiver;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.OEUser;
import com.openerp.support.fragment.FragmentListener;
import com.openerp.support.listview.OEListAdapter;
import com.openerp.util.HTMLHelper;
import com.openerp.util.TextViewTags;
import com.openerp.util.controls.OEEditText;
import com.openerp.util.controls.OETextView;
import com.openerp.util.drawer.DrawerColorTagListener;
import com.openerp.util.drawer.DrawerItem;
import com.openerp.util.drawer.DrawerListener;

public class Note extends BaseFragment implements
		OETouchListener.OnPullListener, SwipeCallbacks, OnClickListener,
		OnItemClickListener, DrawerColorTagListener {

	public static final String TAG = "com.openerp.addons.note.Note";
	public static final int KEY_NOTE = 1;

	View mView = null;
	String mTagColors[] = new String[] { "#9933CC", "#669900", "#FF8800",
			"#CC0000", "#59A2BE", "#808080", "#192823", "#0099CC", "#218559",
			"#EBB035" };
	static HashMap<String, Integer> mStageTagColors = new HashMap<String, Integer>();

	GridView mNoteGridView = null;
	List<Object> mNotesList = new ArrayList<Object>();
	OEListAdapter mNoteListAdapter = null;
	SearchView mSearchView = null;
	Integer mStageId = 0;
	NoteLoader mNoteLoader = null;
	OETouchListener mTouchListener = null;
	Boolean mSynced = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		mView = inflater.inflate(R.layout.fragment_note, container, false);
		return mView;
	}

	@Override
	public void onStart() {
		super.onStart();
		Bundle bundle = getArguments();
		if (bundle != null) {
			mStageId = bundle.getInt("stage_id");
		}
		init();
	}

	private void init() {
		Log.d(TAG, "Note->init()");
		scope = new AppScope(getActivity());
		mTouchListener = scope.main().getTouchAttacher();
		initControls();
	}

	private void initControls() {
		Log.d(TAG, "Note->initControls()");
		mNoteGridView = (GridView) mView.findViewById(R.id.noteGridView);
		mView.findViewById(R.id.imgBtnCreateQuickNote).setOnClickListener(this);
		mNoteListAdapter = new OEListAdapter(getActivity(),
				R.layout.fragment_note_grid_custom_layout, mNotesList) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View mView = convertView;
				if (mView == null) {
					mView = getActivity().getLayoutInflater().inflate(
							getResource(), parent, false);
				}
				OEDataRow row = (OEDataRow) mNotesList.get(position);
				TextView txvTitle, txvDesc, txvStage, txvTags;
				txvTitle = (TextView) mView.findViewById(R.id.txvNoteTitle);
				txvDesc = (TextView) mView
						.findViewById(R.id.txvNoteDescription);
				txvStage = (TextView) mView.findViewById(R.id.txvNoteStage);
				txvTags = (TextView) mView.findViewById(R.id.txvNoteTags);
				txvTitle.setText(row.getString("name"));
				txvDesc.setText(HTMLHelper.htmlToString(row.getString("memo")));
				OEDataRow stage = row.getM2ORecord("stage_id").browse();
				txvStage.setText("New");
				int color = Color.parseColor("#ffffff");
				if (stage != null) {
					txvStage.setText(stage.getString("name"));
					Integer tagColor = getTagColor("key_"
							+ stage.getString("id"));
					if (tagColor != null) {
						color = tagColor;
					}
				}
				List<String> tags = new ArrayList<String>();
				List<OEDataRow> notetags = row.getM2MRecord("tag_ids")
						.browseEach();
				for (OEDataRow tag : notetags) {
					tags.add(tag.getString("name"));
				}
				txvTags.setText(new TextViewTags(getActivity(), tags,
						"#ebebeb", "#414141", 25).generate());
				mView.findViewById(R.id.noteGridClildView).setBackgroundColor(
						color);
				txvStage.setTextColor(color);
				return mView;
			}
		};
		mNoteGridView.setAdapter(mNoteListAdapter);
		mNoteGridView.setOnItemClickListener(this);
		mTouchListener.setPullableView(mNoteGridView, this);
		mTouchListener.setSwipeableView(mNoteGridView, this);
		mNoteGridView.setOnScrollListener(mTouchListener.makeScrollListener());

		mNoteLoader = new NoteLoader(mStageId);
		mNoteLoader.execute();
	}

	class NoteLoader extends AsyncTask<Void, Void, Void> {

		Integer mStageId = null;

		public NoteLoader(Integer stageId) {
			mStageId = stageId;
			Log.d(TAG, "Note->NoteLoader->constructor()");
		}

		@Override
		protected Void doInBackground(Void... params) {
			mNotesList.clear();
			String where = "";
			String[] whereArgs = null;
			switch (mStageId) {
			case -1:
				where = "open = ?";
				whereArgs = new String[] { "true" };
				break;
			case -2:
				where = "open = ?";
				whereArgs = new String[] { "false" };
				break;
			default:
				where = "open = ? AND stage_id = ?";
				whereArgs = new String[] { "true", mStageId + "" };
				break;
			}
			List<OEDataRow> records = db().select(where, whereArgs);
			mNotesList.addAll(records);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Log.d(TAG, "Note->NoteLoader->onPostExecute()");
			mNoteListAdapter.notifiyDataChange(mNotesList);
			mSearchView
					.setOnQueryTextListener(getQueryListener(mNoteListAdapter));
			mNoteLoader = null;
			mView.findViewById(R.id.loadingProgress).setVisibility(View.GONE);
			checkStatus();
		}

	}

	private void checkStatus() {
		Log.d(TAG, "Note->checkStatus()");
		if (mNotesList.size() == 0) {
			if (db().isEmptyTable() && !mSynced) {
				scope.main().requestSync(NoteProvider.AUTHORITY);
				mView.findViewById(R.id.waitingForSyncToStart).setVisibility(
						View.VISIBLE);
				OETextView txvSubMessage = (OETextView) mView
						.findViewById(R.id.txvMessageHeaderSubtitle);
				txvSubMessage.setText("Your notes will appear shortly");
			} else {
				OETextView txvMsg = (OETextView) mView
						.findViewById(R.id.txvNoteAllArchive);
				txvMsg.setVisibility(View.VISIBLE);
			}
		} else {
			mView.findViewById(R.id.waitingForSyncToStart).setVisibility(
					View.GONE);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_fragment_note, menu);
		mSearchView = (SearchView) menu.findItem(R.id.menu_note_search)
				.getActionView();
	}

	@Override
	public Object databaseHelper(Context context) {
		return new NoteDB(context);
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		List<DrawerItem> items = null;
		NoteDB note = new NoteDB(context);
		NoteStages stages = note.new NoteStages(context);
		if (stages.isEmptyTable()) {
			OEDomain domain = new OEDomain();
			domain.add("user_id", "=", OEUser.current(context).getUser_id());
			OEHelper oe = stages.getOEInstance();
			if (oe != null) {
				oe.syncWithServer(domain);
			}
		}
		if (note.isInstalledOnServer()) {
			items = new ArrayList<DrawerItem>();
			items.add(new DrawerItem(TAG, "Notes", true));
			items.add(new DrawerItem(TAG, "Notes", count("-1", context),
					R.drawable.ic_action_notes, fragmentObject(-1)));
			items.add(new DrawerItem(TAG, "Archive", 0,
					R.drawable.ic_action_archive, fragmentObject(-2)));
			if (stages.count() > 0) {
				int index = 0;
				for (OEDataRow stage : stages.select()) {
					if (index > mTagColors.length - 1) {
						index = 0;
					}
					DrawerItem stageItem = new DrawerItem(TAG,
							stage.getString("name"), count(
									stage.getString("id"), context),
							mTagColors[index],
							fragmentObject(stage.getInt("id")));
					mStageTagColors.put("key_" + stage.getString("id"),
							Color.parseColor(mTagColors[index]));
					items.add(stageItem);
					index++;
				}
			}
		}
		return items;
	}

	public int count(String stage_id, Context context) {
		int count = 0;
		NoteDB note = new NoteDB(context);
		String where = null;
		String[] whereArgs = null;

		if (stage_id.equals("-1")) {
			where = "open = ?";
			whereArgs = new String[] { "true" };
		} else {
			where = "open = ? AND stage_id = ? ";
			whereArgs = new String[] { "true", stage_id };
		}
		count = note.count(where, whereArgs);
		return count;
	}

	private Fragment fragmentObject(int value) {
		Note note = new Note();
		Bundle bundle = new Bundle();
		bundle.putInt("stage_id", value);
		note.setArguments(bundle);
		return note;
	}

	@Override
	public boolean canSwipe(int position) {
		return true;
	}

	@Override
	public void onSwipe(View view, int[] positions) {
		for (int position : positions) {
			OEDataRow row = (OEDataRow) mNotesList.get(position);
			NoteToggleStatus mNoteToggle = new NoteToggleStatus(
					row.getInt("id"), row.getBoolean("open"), getActivity());
			mNoteToggle.execute();
			mNotesList.remove(position);
			mNoteListAdapter.notifiyDataChange(mNotesList);
		}
	}

	public class NoteToggleStatus extends AsyncTask<Void, Void, Void> {
		int mId = 0;
		boolean mStatus = false;
		String mToast = "";
		FragmentActivity mActivity = null;

		public NoteToggleStatus(int id, boolean status,
				FragmentActivity activity) {
			mId = id;
			mStatus = status;
			mActivity = activity;
		}

		@Override
		protected Void doInBackground(Void... params) {
			NoteDB note = new NoteDB(mActivity);
			OEHelper oe = note.getOEInstance();
			if (oe != null) {
				try {
					JSONArray args = new JSONArray("[" + mId + "]");
					String method = "onclick_note_is_done";
					mToast = "Moved to archive";

					if (!mStatus) {
						method = "onclick_note_not_done";
						mToast = "Moved to active notes";
						mStatus = true;
					} else {
						mStatus = false;
					}
					oe.call_kw(note.getModelName(), method, args);
					OEValues values = new OEValues();
					values.put("open", mStatus);
					int count = note.update(values, mId);
					Log.i(TAG, "Note->NoteToggleStatus() : " + count
							+ " row updated");
				} catch (Exception e) {
					mToast = "No Connection !";
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			DrawerListener drawer = (DrawerListener) mActivity;
			drawer.refreshDrawer(TAG);
			Toast.makeText(mActivity, mToast, Toast.LENGTH_LONG).show();
		}

	}

	@Override
	public void onPullStarted(View view) {
		scope.main().requestSync(NoteProvider.AUTHORITY);
	}

	@Override
	public void onResume() {
		super.onResume();
		scope.context().registerReceiver(syncFinishReceiver,
				new IntentFilter(SyncFinishReceiver.SYNC_FINISH));
	}

	@Override
	public void onPause() {
		super.onPause();
		scope.context().unregisterReceiver(syncFinishReceiver);
	}

	private SyncFinishReceiver syncFinishReceiver = new SyncFinishReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mTouchListener.setPullComplete();
			DrawerListener drawer = (DrawerListener) getActivity();
			drawer.refreshDrawer(TAG);
			mNoteLoader = new NoteLoader(mStageId);
			mNoteLoader.execute();
			mSynced = true;
		}
	};

	/**
	 * On QuickNote Create button click listener
	 */
	@Override
	public void onClick(View v) {
		Log.d(TAG, "[QuickNote create] Note->onClick()");
		Intent composeNote = new Intent(scope.context(),
				NoteComposeActivity.class);
		OEEditText edtTitle = (OEEditText) mView
				.findViewById(R.id.edtNoteQuickTitle);
		composeNote.putExtra("note_title", edtTitle.getText().toString());
		startActivityForResult(composeNote, KEY_NOTE);
		edtTitle.setText(null);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case KEY_NOTE:
			if (resultCode == Activity.RESULT_OK) {
				int new_id = data.getExtras().getInt("result");
				OEDataRow row = db().select(new_id);
				mNotesList.add(row);
				mNoteListAdapter.notifiyDataChange(mNotesList);
			}
			break;
		}
	}

	/**
	 * On Note GridView item click listener
	 */
	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position,
			long id) {
		Log.d(TAG, "Note->onItemClick()");
		OEDataRow row = (OEDataRow) mNotesList.get(position);

		Bundle bundle = new Bundle();
		bundle.putInt("note_id", row.getInt("id"));
		bundle.putBoolean("row_status", row.getBoolean("open"));
		OEDataRow stage = row.getM2ORecord("stage_id").browse();
		if (stage != null) {
			bundle.putString("stage_id", stage.getString("id"));
			bundle.putInt("stage_color",
					getTagColor("key_" + bundle.getString("stage_id")));
		}
		NoteDetail note = new NoteDetail();
		note.setArguments(bundle);
		FragmentListener mFragment = (FragmentListener) getActivity();
		mFragment.startDetailFragment(note);
	}

	@Override
	public Integer getTagColor(String key) {
		if (mStageTagColors.containsKey(key)) {
			return mStageTagColors.get(key);
		}
		return null;
	}
}
