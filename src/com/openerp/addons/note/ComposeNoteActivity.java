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
package com.openerp.addons.note;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.openerp.MainActivity;
import com.openerp.R;
import com.openerp.orm.OEDataRow;
import com.openerp.orm.OEHelper;
import com.openerp.support.AppScope;
import com.openerp.support.OEUser;
import com.openerp.util.HTMLHelper;
import com.openerp.util.controls.OEEditText;
import com.openerp.util.controls.OETextView;
import com.openerp.util.tags.TagsItem;
import com.openerp.util.tags.TagsView;
import com.openerp.widget.Mobile_Widget;

public class ComposeNoteActivity extends Activity implements
		TagsView.TokenListener, OnNavigationListener {

	OEEditText noteDescription;
	WebView webViewpad;
	TagsView noteTags;
	AppScope scope = null;
	NoteDBHelper.NoteStages mNoteStagesDB;
	NoteDBHelper dbhelper = null;

	/* actionbar navigation */
	ActionBar mActionbar;
	ArrayAdapter<SpinnerNavItem> mActionbarAdapter;
	List<SpinnerNavItem> mSpinnerItems = new ArrayList<SpinnerNavItem>();
	HashMap<String, Integer> mSpinnerItemsPositions = new HashMap<String, Integer>();
	Integer mStageId = null;

	HashMap<String, TagsItem> selectedTags = new HashMap<String, TagsItem>();
	String[] stringArray = null;
	String padURL = null;
	private static OEHelper oe = null;
	Intent update_widget = null;

	boolean inEditMode = false;
	int mEditNoteId = 0;
	String mEditNoteMemo = null;
	int mEditNoteStageId;

	List<TagsItem> mNoteTagsList = new ArrayList<TagsItem>();
	ArrayAdapter<TagsItem> mNoteTagAdapter = null;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_note_compose);
		scope = new AppScope((MainActivity) MainActivity.context);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		noteDescription = (OEEditText) findViewById(R.id.edtNoteComposeDescription);
		dbhelper = new NoteDBHelper(scope.context());
		mActionbar = getActionBar();
		mActionbar.setDisplayShowTitleEnabled(false);
		mActionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		fillNoteStages();
		Intent intent = getIntent();
		if (intent.hasExtra("note_title")) {
			OEEditText edtNoteTitleInput = (OEEditText) findViewById(R.id.edtNoteTitleInput);
			edtNoteTitleInput.setText(intent.getStringExtra("note_title")
					.toString());
		}
		webViewpad = (WebView) findViewById(R.id.webNoteComposeWebViewPad);
		noteTags = (TagsView) findViewById(R.id.edtComposeNoteTags);
		noteTags.setCustomTagView(new TagsView.CustomTagViewListener() {

			@Override
			public View getViewForTags(LayoutInflater layoutInflater,
					Object object, ViewGroup tagsViewGroup) {
				View view = (View) layoutInflater.inflate(
						R.layout.custom_note_tagsview_item, tagsViewGroup,
						false);
				TagsItem item = (TagsItem) object;
				OETextView txvTitle = (OETextView) view
						.findViewById(R.id.txvCustomNoteTagsViewItem);
				txvTitle.setText(item.getSubject());
				txvTitle.setBackgroundColor(Color.parseColor("#cccccc"));
				txvTitle.setTextColor(Color.parseColor("#414141"));
				return view;
			}
		});
		Bundle editModeBundle = intent.getExtras();
		boolean padExists = dbhelper.isPadExist();
		if (editModeBundle != null) {
			if (editModeBundle.containsKey("note_id")) {
				inEditMode = true;
				mEditNoteId = editModeBundle.getInt("note_id");
				String row_details = editModeBundle.getString("row_details");
				mEditNoteMemo = row_details;
				findViewById(R.id.edtNoteTitleInput).setVisibility(View.GONE);
				Note note = new Note();
				List<TagsItem> tags = note.getNoteTags(mEditNoteId + "", this);
				for (TagsItem item : tags) {
					noteTags.addObject(item);
					selectedTags.put(item.getId() + "", item);
				}
				if (padExists) {
					padURL = editModeBundle.getString("padurl");
				} else {
					((OEEditText) findViewById(R.id.edtNoteComposeDescription))
							.setText(row_details);
				}
				try {
					JSONArray stage_id = new JSONArray(
							editModeBundle.getString("stage_id"));
					mEditNoteStageId = stage_id.getJSONArray(0).getInt(0);
					mStageId = mEditNoteStageId;
					mActionbar.setSelectedNavigationItem(mSpinnerItemsPositions
							.get("key_" + mStageId));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (padExists) {
			oe = dbhelper.getOEInstance();
			noteDescription.setVisibility(View.GONE);
			webViewpad.setVisibility(View.VISIBLE);
			webViewpad.getSettings().setJavaScriptEnabled(true);
			webViewpad.getSettings().setJavaScriptCanOpenWindowsAutomatically(
					true);
			if (padURL == null) {
				padURL = dbhelper.getURL(oe);
			}
			webViewpad.loadUrl(padURL + "?showChat=false&userName="
					+ OEUser.current(scope.context()).getUsername());
		}
		update_widget = new Intent();
		update_widget.setAction(Mobile_Widget.TAG);

	}

	@Override
	public void onStart() {
		super.onStart();
		noteTags.allowDuplicates(false);
		noteTags.setTokenListener(this);
		mNoteTagsList = getTags();
		mNoteTagAdapter = new ArrayAdapter<TagsItem>(this,
				R.layout.custom_note_tags_adapter_view_item, mNoteTagsList);
		noteTags.setNewTokenCreateListener(new TagsView.NewTokenCreateListener() {

			@Override
			public TagsItem newTokenAddListener(String token) {
				TagsItem item = null;
				LinkedHashMap<String, String> newTag = dbhelper
						.writeNoteTags(token);
				item = new TagsItem(Integer.parseInt(newTag.get("newID")),
						newTag.get("tagName"), null);
				noteTags.addObject(item);
				mNoteTagsList.add(item);
				return item;
			}
		});
		noteTags.setAdapter(mNoteTagAdapter);
	}

	public List<TagsItem> getTags() {
		List<TagsItem> items = new ArrayList<TagsItem>();
		LinkedHashMap<Integer, String> noteTags = dbhelper.getAllNoteTags();
		for (Integer key : noteTags.keySet()) {
			items.add(new TagsItem(key, noteTags.get(key).toString(), null));
		}
		return items;
	}

	public void fillNoteStages() {

		mNoteStagesDB = dbhelper.new NoteStages(scope.context());
		List<OEDataRow> data = mNoteStagesDB.search(mNoteStagesDB);
		int total = data.size();

		if (total > 0) {
			mSpinnerItems.add(new SpinnerNavItem(0, "Stages"));
			int i = 1;
			for (OEDataRow row_data : data) {
				mSpinnerItems.add(new SpinnerNavItem(row_data.getInt("id"),
						row_data.getString("name")));
				mSpinnerItemsPositions.put("key_" + row_data.getInt("id"), i);
				i++;
			}
			mSpinnerItems.add(new SpinnerNavItem(-1, "Add New"));
		} else {
			mSpinnerItems.add(new SpinnerNavItem(-1, "Add New"));
		}

		mActionbarAdapter = new ArrayAdapter<SpinnerNavItem>(this,
				R.layout.spinner_custom_layout, mSpinnerItems) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View mView = convertView;
				if (mView == null) {
					LayoutInflater inflater = getLayoutInflater();
					mView = inflater.inflate(R.layout.spinner_custom_layout,
							parent, false);
				}
				OETextView txvTitle = (OETextView) mView
						.findViewById(R.id.txvCustomSpinnerItemText);
				SpinnerNavItem item = mSpinnerItems.get(position);
				txvTitle.setText(item.get_title());
				return mView;
			}

			@Override
			public View getDropDownView(int position, View convertView,
					ViewGroup parent) {
				View mView = convertView;
				if (mView == null) {
					LayoutInflater inflater = getLayoutInflater();
					mView = inflater.inflate(R.layout.spinner_custom_layout,
							parent, false);
				}
				OETextView txvTitle = (OETextView) mView
						.findViewById(R.id.txvCustomSpinnerItemText);
				SpinnerNavItem item = mSpinnerItems.get(position);
				txvTitle.setText(item.get_title());
				return mView;
			}

		};
		mActionbar.setListNavigationCallbacks(mActionbarAdapter, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.menu_fragment_note_new_edit, menu);
		MenuItem note_write = menu.findItem(R.id.menu_note_write);
		if (inEditMode) {
			note_write.setTitle("Update");
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
			if (inEditMode) {
				Toast.makeText(this, "Updating...", Toast.LENGTH_LONG).show();
				writeNote(mEditNoteId);
			} else {
				writeNote(null);
			}
			return true;

		case R.id.menu_note_cancel:
			if (inEditMode) {
				if (isContentChanged()) {
					openConfirmDiscard("Discard ?",
							"Your changes will be discarded. Are you sure?",
							"Discard", "Cancel");
				} else {
					finish();
				}
			} else {
				finish();
			}
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void openConfirmDiscard(String title, String message,
			String positivebtnText, String negativebtnText) {

		AlertDialog.Builder deleteDialogConfirm = new AlertDialog.Builder(
				scope.context());
		deleteDialogConfirm.setTitle(title);
		deleteDialogConfirm.setMessage(message);
		deleteDialogConfirm.setCancelable(true);

		deleteDialogConfirm.setPositiveButton(positivebtnText,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});

		deleteDialogConfirm.setNegativeButton(negativebtnText, null);

		deleteDialogConfirm.show();
	}

	public boolean isContentChanged() {
		String memo = ((OEEditText) findViewById(R.id.edtNoteComposeDescription))
				.getText().toString();

		if ((mEditNoteMemo.length() != memo.length())
				|| (mEditNoteStageId != mStageId)) {
			return true;
		}

		return false;
	}

	public void createNoteStage() {

		AlertDialog.Builder builder = new Builder(this);
		final EditText stage = new EditText(this);
		builder.setTitle("Stage Name").setMessage("Enter new Stage")
				.setView(stage);

		builder.setPositiveButton("Create", new OnClickListener() {
			public void onClick(DialogInterface di, int i) {
				// do something with onClick
				if ((stage.getText().toString()).equalsIgnoreCase("Add New")
						|| (stage.getText().toString())
								.equalsIgnoreCase("AddNew")) {
					Toast.makeText(scope.context(), "CHOOSE ANOTHER NAME",
							Toast.LENGTH_SHORT).show();
				} else {
					int newId = writeNoteStages(stage.getText().toString());
					mSpinnerItems.add(mSpinnerItems.size() - 1,
							new SpinnerNavItem(newId, stage.getText()
									.toString()));
					mActionbarAdapter.notifyDataSetChanged();
					mStageId = newId;
					scope.main().refreshDrawer(Note.TAG, scope.context());
				}
			}
		});

		builder.setNegativeButton("Cancel", new OnClickListener() {
			public void onClick(DialogInterface di, int i) {
			}
		});

		builder.create().show();
	}

	public int writeNoteStages(String stageName) {

		ContentValues values = new ContentValues();
		values.put("name", stageName);
		dbhelper = new NoteDBHelper(scope.context());
		NoteDBHelper.NoteStages notestageObj = dbhelper.new NoteStages(
				scope.context());
		int newId = notestageObj.createRecordOnserver(notestageObj, values);
		values.put("id", newId);
		return notestageObj.create(notestageObj, values);
	}

	private void writeNote(Integer note_id) {

		JSONArray tagID = dbhelper.getSelectedTagId(selectedTags);
		try {
			ContentValues values = new ContentValues();
			JSONObject vals = new JSONObject();

			// If Pad Installed Over Server
			if (dbhelper.isPadExist()) {
				OEHelper oe = dbhelper.getOEInstance();
				JSONArray url = new JSONArray();
				url.put(padURL);
				JSONObject obj = oe.call_kw("pad.common", "pad_get_content",
						url);
				String link = HTMLHelper.htmlToString(obj.getString("result"));
				values.put("name", dbhelper.generateName(link));
				values.put("memo", link);
				values.put("note_pad_url", padURL);
				vals.put("note_pad_url", values.get("note_pad_url").toString());
			} // If Pad Not Installed Over Server
			else {
				String name = dbhelper.generateName(noteDescription.getText()
						.toString());
				if (!inEditMode) {
					EditText edtTitle = (EditText) findViewById(R.id.edtNoteTitleInput);
					if (!TextUtils.isEmpty(edtTitle.getText())) {
						name = edtTitle.getText().toString();
					}
				}
				values.put("name", name);
				vals.put("name", values.get("name").toString());
				if (!inEditMode) {
					values.put("memo", name + "<br/><br/>"
							+ noteDescription.getText().toString());
				} else {
					values.put("memo", noteDescription.getText().toString());
				}

				vals.put("memo", values.get("memo").toString());
			}

			values.put("open", "true");
			vals.put("open", true);

			if (mStageId != null) {
				values.put("stage_id", mStageId + "");
				vals.put("stage_id",
						Integer.parseInt(values.get("stage_id").toString()));
				JSONArray tag_ids = new JSONArray();
				tag_ids.put(6);
				tag_ids.put(false);
				JSONArray c_ids = new JSONArray(tagID.toString());
				tag_ids.put(c_ids);
				values.put("current_partner_id",
						Integer.parseInt(scope.User().getUser_id().toString()));
				vals.put("current_partner_id", values.get("current_partner_id")
						.toString());
				vals.put("tag_ids", new JSONArray("[" + tag_ids.toString()
						+ "]"));
				dbhelper = new NoteDBHelper(scope.context());

				values.put("date_done", "false");
				values.put("tag_ids", tagID.toString());
				int write_id = 0;
				Intent resultIntent = new Intent();
				String toast_msg = "Note created";
				if (note_id == null) {
					int newId = dbhelper.createRecordOnserver(dbhelper, vals);
					values.put("id", newId);
					write_id = dbhelper.create(dbhelper, values);
				} else {
					deleteLocalEntriesForTags(note_id);
					dbhelper.write(dbhelper, values, note_id);
					resultIntent.putExtra("updated", true);
					Bundle editNoteValues = new Bundle();
					editNoteValues.putString("memo", noteDescription.getText()
							.toString());
					editNoteValues.putInt("stage_id", mStageId);
					resultIntent.putExtras(editNoteValues);
					toast_msg = "Note updated";
				}
				Toast.makeText(this, toast_msg, Toast.LENGTH_LONG).show();
				resultIntent.putExtra("result", write_id);
				setResult(Activity.RESULT_OK, resultIntent);
				finish();
			} else {
				Toast.makeText(scope.context(), "Please select stage",
						Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			scope.context().sendBroadcast(update_widget);
		}
	}

	private void deleteLocalEntriesForTags(int note_id) {
		// Removing all many to many records for note for tags.
		dbhelper.executeSQL(
				"delete from note_note_note_tag_rel where note_note_id = ? and oea_name = ?",
				new String[] { note_id + "",
						OEUser.current(this).getAndroidName() });
	}

	@Override
	public void onTokenAdded(Object token, View view) {
		TagsItem item = (TagsItem) token;
		selectedTags.put("" + item.getId(), item);
	}

	@Override
	public void onTokenSelected(Object token, View view) {
	}

	@Override
	public void onTokenRemoved(Object token) {
		TagsItem item = (TagsItem) token;
		selectedTags.remove("" + item.getId());
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
		SpinnerNavItem item = mSpinnerItems.get(itemPosition);
		if (item.get_id() == 0) {
			return false;
		}
		if (item.get_id() == -1) {
			createNoteStage();
		}
		mStageId = item.get_id();
		return true;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		Bundle bundle = new Bundle();
		for (String key : selectedTags.keySet()) {
			TagsItem item = selectedTags.get(key);
			Bundle item_bundle = new Bundle();
			item_bundle.putString("name", item.getSubject());
			item_bundle.putInt("id", item.getId());
			bundle.putBundle(key, item_bundle);
		}
		savedInstanceState.putBundle("selected_tags", bundle);
		savedInstanceState.putString("padURL", padURL);
		savedInstanceState.putInt("stage_id", mStageId);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Bundle bundle = savedInstanceState.getBundle("selected_tags");
		selectedTags = new HashMap<String, TagsItem>();
		for (String key : bundle.keySet()) {
			Bundle item_bundle = bundle.getBundle(key);
			TagsItem item = new TagsItem(item_bundle.getInt("id"),
					item_bundle.getString("name"), null);
			selectedTags.put(key, item);
			noteTags.addObject(item);
		}
		padURL = savedInstanceState.getString("padURL");
		mStageId = savedInstanceState.getInt("stage_id");
	}
}
