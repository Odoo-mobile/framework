package com.openerp.addons.note;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import openerp.OEArguments;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

import com.openerp.R;
import com.openerp.addons.note.NoteDB.NoteStages;
import com.openerp.addons.note.NoteDB.NoteTags;
import com.openerp.orm.OEDataRow;
import com.openerp.orm.OEHelper;
import com.openerp.orm.OEM2MIds;
import com.openerp.orm.OEM2MIds.Operation;
import com.openerp.orm.OEValues;
import com.openerp.support.OEUser;
import com.openerp.support.listview.OEListAdapter;
import com.openerp.util.HTMLHelper;
import com.openerp.util.controls.OEEditText;
import com.openerp.util.controls.OETextView;
import com.openerp.util.tags.MultiTagsTextView.TokenListener;
import com.openerp.util.tags.TagsItem;
import com.openerp.util.tags.TagsView;
import com.openerp.util.tags.TagsView.NewTokenCreateListener;

public class NoteComposeActivity extends Activity implements
		OnNavigationListener, NewTokenCreateListener, TokenListener {
	public static final String TAG = "com.openerp.addons.note.NoteComposeActivity";

	Context mContext = null;

	/**
	 * Note db, OpenERP Instance
	 */
	NoteDB mDb = null;
	OEHelper mOpenERP = null;
	/**
	 * Database Objects
	 */
	NoteDB mNoteDB = null;
	NoteTags mTagsDb = null;
	NoteStages mNoteStageDB = null;
	Integer mStageId = null;
	List<Object> mNoteTags = new ArrayList<Object>();
	OEListAdapter mNoteStageAdapter = null;
	OEListAdapter mNoteTagsAdapter = null;
	OEDataRow mNoteRow = null;
	Integer mNoteId = null;
	Boolean mEditMode = false;
	HashMap<String, Integer> mSelectedTagsIds = new HashMap<String, Integer>();
	String mPadURL = "";
	/**
	 * Actionbar
	 */
	ActionBar mActionbar;
	List<Object> mActionbarSpinnerItems = new ArrayList<Object>();
	HashMap<String, Integer> mActionbarSpinnerItemsPositions = new HashMap<String, Integer>();

	/**
	 * Note pad status
	 */
	boolean mPadInstalled = false;

	WebView mWebViewPad = null;
	OEEditText edtNoteTitle = null;
	OEEditText edtNoteDescription = null;
	TagsView mNoteTagsView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_note_compose);
		mContext = this;
		mDb = new NoteDB(mContext);
		mTagsDb = mDb.new NoteTags(mContext);
		mOpenERP = mDb.getOEInstance();
		init();
	}

	private void init() {
		mNoteDB = new NoteDB(mContext);
		mNoteStageDB = mNoteDB.new NoteStages(mContext);
		initActionBar();
		initNoteTags();
		checkForPad();
		initNote();
	}

	public void checkForPad() {
		Log.d(TAG, "NoteComposeActivity->checkForPad()");
		if (mOpenERP != null) {
			mPadInstalled = mOpenERP.moduleExists("note_pad");
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void initNote() {
		edtNoteTitle = (OEEditText) findViewById(R.id.edtNoteTitleInput);
		edtNoteDescription = (OEEditText) findViewById(R.id.edtNoteComposeDescription);
		mWebViewPad = (WebView) findViewById(R.id.webNoteComposeWebViewPad);
		Intent intent = getIntent();
		if (intent.hasExtra("note_id")) {
			mEditMode = true;
			mNoteId = intent.getIntExtra("note_id", 0);
			mNoteRow = mDb.select(mNoteId);
			OEDataRow stage = mNoteRow.getM2ORecord("stage_id").browse();
			if (stage != null) {
				mStageId = stage.getInt("id");
				mActionbar
						.setSelectedNavigationItem(mActionbarSpinnerItemsPositions
								.get("key_" + mStageId));
			}

		}
		if (intent.hasExtra("note_title")) {
			edtNoteTitle.setText(intent.getStringExtra("note_title"));
		}
		if (mPadInstalled) {
			edtNoteDescription.setVisibility(View.GONE);
			mWebViewPad.setVisibility(View.VISIBLE);
			if (mEditMode) {
				mPadURL = mNoteRow.getString("note_pad_url");
				if (mPadURL.equals("false")) {
					mPadURL = getPadURL(mNoteRow.getInt("id"));
				}
			} else {
				mPadURL = getPadURL(null);
			}
			mWebViewPad.getSettings().setJavaScriptEnabled(true);
			mWebViewPad.getSettings().setJavaScriptCanOpenWindowsAutomatically(
					true);
			mWebViewPad.loadUrl(mPadURL + "?showChat=false&userName="
					+ OEUser.current(mContext).getUsername());
		} else {
			edtNoteDescription.setVisibility(View.VISIBLE);
			mWebViewPad.setVisibility(View.GONE);
			if (mEditMode) {
				edtNoteDescription
						.setMovementMethod(new ScrollingMovementMethod());
				edtNoteDescription.setText(HTMLHelper.stringToHtml(mNoteRow
						.getString("memo")));
			}
		}
		if (mEditMode) {
			edtNoteTitle.setText(mNoteRow.getString("name"));
			List<OEDataRow> tags = mNoteRow.getM2MRecord("tag_ids")
					.browseEach();
			if (tags != null) {
				for (OEDataRow row : tags) {
					TagsItem item = new TagsItem(row.getInt("id"),
							row.getString("name"), null);
					mNoteTagsView.addObject(item);
				}
			}
		}

	}

	private String getPadURL(Integer note_id) {
		if (mOpenERP != null) {
			JSONObject newContext = new JSONObject();
			try {
				boolean flag = false;
				if (note_id != null) {
					List<Object> ids = new ArrayList<Object>();
					ids.add(note_id);
					if (mOpenERP.syncWithServer(false, null, ids)) {
						mNoteRow = mDb.select(note_id);
						if (!mNoteRow.getString("note_pad_url").equals("false")) {
							mPadURL = mNoteRow.getString("note_pad_url");
							flag = true;
						}
					}
				}
				if (!flag) {
					newContext.put("model", "note.note");
					newContext.put("field_name", "note_pad_url");
					JSONObject kwargs = new JSONObject();
					if (note_id != null)
						newContext.put("object_id", note_id);
					kwargs.accumulate("context", newContext);
					mOpenERP.updateKWargs(kwargs);
					OEArguments arguments = new OEArguments();
					JSONObject result = (JSONObject) mOpenERP.call_kw(
							"pad_generate_url", arguments, null);
					mPadURL = result.getString("url");
				}

			} catch (Exception e) {
			}
		}
		return mPadURL;
	}

	private void initNoteTags() {
		mNoteTagsView = (TagsView) findViewById(R.id.edtComposeNoteTags);
		for (OEDataRow tag : mTagsDb.select()) {
			TagsItem tag_item = new TagsItem(tag.getInt("id"),
					tag.getString("name"), null);
			mNoteTags.add(tag_item);
		}
		mNoteTagsAdapter = new OEListAdapter(mContext,
				R.layout.custom_note_tags_adapter_view_item, mNoteTags) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View mView = convertView;
				if (mView == null) {
					mView = getLayoutInflater().inflate(getResource(), parent,
							false);
				}
				TagsItem row = (TagsItem) mNoteTags.get(position);
				OETextView txvName = (OETextView) mView
						.findViewById(R.id.txvCustomNoteTagsAdapterViewItem);
				txvName.setText(row.getSubject());
				return mView;
			}
		};
		mNoteTagsView.setAdapter(mNoteTagsAdapter);
		mNoteTagsView.showImage(false);
		mNoteTagsView.setNewTokenCreateListener(this);
		mNoteTagsView.setTokenListener(this);
	}

	private void initActionBar() {
		mActionbar = getActionBar();
		mActionbar.setHomeButtonEnabled(true);
		mActionbar.setDisplayHomeAsUpEnabled(true);
		mActionbar.setDisplayShowTitleEnabled(false);
		mActionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		setupActionbarSpinner();
	}

	private void setupActionbarSpinner() {
		mActionbarSpinnerItems.add(new SpinnerNavItem(0, "Stages"));
		int i = 1;
		for (OEDataRow stage : mNoteStageDB.select()) {
			mActionbarSpinnerItems.add(new SpinnerNavItem(stage.getInt("id"),
					stage.getString("name")));
			mActionbarSpinnerItemsPositions.put("key_" + stage.getInt("id"), i);
			i++;
		}
		mActionbarSpinnerItems.add(new SpinnerNavItem(-1, "Add New"));
		mNoteStageAdapter = new OEListAdapter(mContext,
				R.layout.spinner_custom_layout, mActionbarSpinnerItems) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View mView = convertView;
				if (mView == null) {
					mView = getLayoutInflater().inflate(getResource(), parent,
							false);
				}
				OETextView txvTitle = (OETextView) mView
						.findViewById(R.id.txvCustomSpinnerItemText);
				SpinnerNavItem item = (SpinnerNavItem) mActionbarSpinnerItems
						.get(position);
				txvTitle.setText(item.get_title());
				return mView;
			}

			@Override
			public View getDropDownView(int position, View convertView,
					ViewGroup parent) {
				View mView = convertView;
				if (mView == null) {
					mView = getLayoutInflater().inflate(getResource(), parent,
							false);
				}
				OETextView txvTitle = (OETextView) mView
						.findViewById(R.id.txvCustomSpinnerItemText);
				SpinnerNavItem item = (SpinnerNavItem) mActionbarSpinnerItems
						.get(position);
				txvTitle.setText(item.get_title());
				return mView;
			}
		};

		mActionbar.setListNavigationCallbacks(mNoteStageAdapter, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.menu_fragment_note_new_edit, menu);
		MenuItem menu_note_write = menu.findItem(R.id.menu_note_write);
		if (mEditMode) {
			menu_note_write.setTitle("Update");
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;

		case R.id.menu_note_write:
			String mToast = "Creating note...";
			if (mEditMode) {
				saveNote(mNoteId);
			} else {
				saveNote(null);
			}
			Toast.makeText(this, mToast, Toast.LENGTH_LONG).show();
			return true;

		case R.id.menu_note_cancel:
			if (mEditMode) {
				// if (isContentChanged()) {
				// openConfirmDiscard("Discard ?",
				// "Your changes will be discarded. Are you sure?",
				// "Discard", "Cancel");
				// } else {
				// finish();
				// }
				// } else {
				// finish();
			} else {
				finish();
			}
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void saveNote(Integer mNoteId) {
		if (mOpenERP != null) {
			OEValues values = new OEValues();
			String name = "";
			String memo = "";
			if (mStageId == null) {
				Toast.makeText(mContext, "Please select stage",
						Toast.LENGTH_LONG).show();
				return;
			}
			if (mPadInstalled) {
				try {
					JSONArray args = new JSONArray();
					args.put(mPadURL);
					JSONObject content = mOpenERP.call_kw("pad.common",
							"pad_get_content", args);
					memo = content.getString("result");
					values.put("note_pad_url", mPadURL);
					memo = name + " <br> " + memo;
				} catch (Exception e) {
				}
			} else {
				memo = edtNoteDescription.getText().toString();
			}
			name = noteName(memo);
			List<Integer> tag_ids = new ArrayList<Integer>();
			for (String key : mSelectedTagsIds.keySet())
				tag_ids.add(mSelectedTagsIds.get(key));
			OEM2MIds m2mIds = new OEM2MIds(Operation.ADD, tag_ids);
			values.put("name", name);
			values.put("memo", memo);
			values.put("open", true);
			values.put("date_done", false);
			values.put("stage_id", mStageId);
			values.put("tag_ids", m2mIds);
			values.put("current_partner_id", OEUser.current(mContext)
					.getPartner_id());
			String mToast = "Note Created";
			if (mNoteId != null) {
				// Updating
				mToast = "Note Updated";
				mOpenERP.update(values, mNoteId);
			} else {
				// Creating
				mOpenERP.create(values);
			}
			Toast.makeText(mContext, mToast, Toast.LENGTH_LONG).show();
			finish();
		} else {
			Toast.makeText(mContext, "No Connection", Toast.LENGTH_LONG).show();
		}
	}

	private String noteName(String memo) {
		String name = "";
		String[] parts = memo.split("\\n");
		if (parts.length == 1) {
			parts = memo.split("\\</br>");
			if (parts.length == 1)
				parts = memo.split("\\<br>");
		}
		name = parts[0];
		return name;
	}

	class SpinnerNavItem {
		int _id;
		String _title;

		public SpinnerNavItem(int _id, String _title) {
			this._id = _id;
			this._title = _title;
		}

		public int get_id() {
			return _id;
		}

		public void set_id(int _id) {
			this._id = _id;
		}

		public String get_title() {
			return _title;
		}

		public void set_title(String _title) {
			this._title = _title;
		}

	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		SpinnerNavItem item = (SpinnerNavItem) mActionbarSpinnerItems
				.get(itemPosition);
		if (item.get_id() == 0) {
			return false;
		}
		if (item.get_id() == -1) {
			createNoteStage();
		}
		mStageId = item.get_id();
		return true;
	}

	public void createNoteStage() {

		AlertDialog.Builder builder = new Builder(this);
		final EditText stage = new EditText(this);
		builder.setTitle("Stage Name").setMessage("Enter new Stage")
				.setView(stage);

		builder.setPositiveButton("Create", new OnClickListener() {
			public void onClick(DialogInterface di, int i) {
				// do something with onClick
				String mToast = "No Connection ";
				if ((stage.getText().toString()).equalsIgnoreCase("Add New")
						|| (stage.getText().toString())
								.equalsIgnoreCase("AddNew")) {
					mToast = "You can't take " + stage.getText().toString();
				} else {
					OEHelper oe = mNoteStageDB.getOEInstance();
					if (oe != null) {
						String stageName = stage.getText().toString();
						OEValues values = new OEValues();
						values.put("name", stageName);
						int newId = oe.create(values);
						mActionbarSpinnerItems.add(
								mActionbarSpinnerItems.size() - 1,
								new SpinnerNavItem(newId, stageName));
						mActionbarSpinnerItemsPositions.put("key_" + newId,
								mActionbarSpinnerItems.size() - 2);
						mActionbar
								.setSelectedNavigationItem(mActionbarSpinnerItemsPositions
										.get("key_" + newId));
						mStageId = newId;
						mNoteStageAdapter
								.notifiyDataChange(mActionbarSpinnerItems);
						mToast = "Stage created";
					}
				}
				Toast.makeText(mContext, mToast, Toast.LENGTH_SHORT).show();
			}
		});

		builder.setNegativeButton("Cancel", null);
		builder.create().show();
	}

	@Override
	public TagsItem newTokenAddListener(String token) {
		TagsItem item = null;
		OEHelper oe = mTagsDb.getOEInstance();
		if (oe != null) {
			OEValues values = new OEValues();
			values.put("name", token);
			int id = oe.create(values);
			item = new TagsItem(id, token, null);
			mNoteTags.add(item);
			mNoteTagsAdapter.notifiyDataChange(mNoteTags);
		}
		return item;
	}

	@Override
	public void onTokenAdded(Object token, View view) {
		TagsItem item = (TagsItem) token;
		mSelectedTagsIds.put("key_" + item.getId(), item.getId());
	}

	@Override
	public void onTokenSelected(Object token, View view) {

	}

	@Override
	public void onTokenRemoved(Object token) {
		TagsItem item = (TagsItem) token;
		mSelectedTagsIds.remove("key_" + item.getId());
	}
}
