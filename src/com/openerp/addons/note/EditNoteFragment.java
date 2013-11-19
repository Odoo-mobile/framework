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
package com.openerp.addons.note;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.openerp.MainActivity;
import com.openerp.R;
import com.openerp.auth.OpenERPAccountManager;
import com.openerp.orm.OEHelper;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.menu.OEMenu;
import com.openerp.util.OnBackButtonPressedListener;
import com.openerp.util.tags.TagsItems;
import com.openerp.util.tags.TagsView;

public class EditNoteFragment extends BaseFragment implements
		TagsView.TokenListener {

	View rootview;
	ImageView addTags;
	Spinner noteStages;
	ArrayAdapter<String> stageAdapter = null;
	HashMap<String, String> stages = new HashMap<String, String>();;
	EditText noteName, noteMemo;
	int row_id = 0;
	ArrayList<String> stagelist = null;
	String row_status = null;
	String stageid = null;
	String tagid = null;
	String memo = null;
	String name = null;
	NoteDBHelper db = null;
	ComposeNoteActivity composeNote = null;
	JSONArray stage_name = null;
	String originalMemo, originialStage;
	JSONArray tagID = new JSONArray();
	ArrayList<String> tagName = new ArrayList<String>();
	private static OEHelper oe_obj = null;
	boolean flag = false;

	LinkedHashMap<String, String> note_tags = null;
	ArrayList<String> keyList = null;
	String[] stringArray = null;
	TagsView noteTags;
	HashMap<String, TagsItems> selectedTags = new HashMap<String, TagsItems>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		setHasOptionsMenu(true);
		scope = new AppScope(MainActivity.userContext,
				(MainActivity) getActivity());
		db = (NoteDBHelper) getModel();
		rootview = inflater.inflate(R.layout.fragment_edit_note, container,
				false);
		handleArguments((Bundle) getArguments());

		addTags = (ImageView) rootview.findViewById(R.id.imgBtnEditTags);
		addTags.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// opening tag list in dialogview
				openTagList();
			}
		});

		scope.context().setOnBackPressed(new OnBackButtonPressedListener() {

			@Override
			public boolean onBackPressed() {
				if (isContentChanged(noteMemo.getText().toString())) {
					if (flag != true) {
						openDailogview(
								"Are you sure..?",
								"Your changes will be discarded. Are you sure?",
								"Discard", "Cancel");
						return false;
					}
				}
				return true;
			}
		});
		if (oe_obj == null) {
			oe_obj = getOEInstance();
		}
		return rootview;
	}

	public void openTagList() {
		AlertDialog.Builder builder = new AlertDialog.Builder(scope.context());
		note_tags = getNoteTag();
		keyList = new ArrayList<String>(note_tags.keySet());
		if (keyList.size() > 0) {
			stringArray = new String[keyList.size() - 1];
			stringArray = keyList.toArray(stringArray);

			builder.setTitle("Select Tags");
			builder.setMultiChoiceItems(stringArray, null,
					new DialogInterface.OnMultiChoiceClickListener() {
						public void onClick(DialogInterface dialog, int item,
								boolean isChecked) {
						}
					});

			builder.setPositiveButton("Select",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							AlertDialog d = (AlertDialog) dialog;
							ListView v = d.getListView();
							int i = 0;
							while (i < stringArray.length) {
								if (v.isItemChecked(i)) {
									Integer id = Integer.parseInt(note_tags
											.get(v.getItemAtPosition(i)
													.toString()).toString());
									noteTags.addObject(new TagsItems(id,
											stringArray[i], ""));
								}
								i++;
							}

						}
					});

		} else {
			builder.setTitle("You don't have any tags \ncreate tag");
		}
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface di, int i) {
					}
				});

		builder.setNeutralButton("Create",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						createNotetag();
					}
				});
		final Dialog dialog = builder.create();
		dialog.show();
	}

	public void createNotetag() {

		AlertDialog.Builder builder = new Builder(scope.context());
		final EditText tag = new EditText(scope.context());
		builder.setTitle("Tag Name").setMessage("Enter new Tag").setView(tag);

		builder.setPositiveButton("Create",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface di, int i) {
						// do something with onClick
						if (tag.getText().length() > 0) {
							writeNoteTags(tag.getText().toString());
						} else {
							Toast.makeText(scope.context(), "Enter Tag First",
									Toast.LENGTH_LONG).show();
						}
					}
				});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface di, int i) {
					}
				});
		builder.create().show();
	}

	public void writeNoteTags(String tagname) {

		ContentValues values = new ContentValues();
		values.put("name", tagname);
		db = new NoteDBHelper(scope.context());
		NoteDBHelper.NoteTags notetagObj = db.new NoteTags(scope.context());
		int newId = notetagObj.createRecordOnserver(notetagObj, values);
		values.put("id", newId);
		notetagObj.create(notetagObj, values);
		noteTags.addObject(new TagsItems(newId, tagname, ""));
	}

	private JSONArray getSelectedTagId() {
		JSONArray list = new JSONArray();
		for (String key : selectedTags.keySet()) {
			list.put(selectedTags.get(key).getId());
		}
		return list;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_note_edit_save:
			updateNote(row_id);
			flag = true;
			getActivity().getSupportFragmentManager().popBackStack();
			return true;

		case R.id.menu_note_edit_cancel:
			if (isContentChanged(noteMemo.getText().toString())) {
				openDailogview("Are you sure..?",
						"Your changes will be discarded. Are you sure?",
						"Discard", "Cancel");
			} else {
				getActivity().getSupportFragmentManager().popBackStack();
			}
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		inflater.inflate(R.menu.menu_fragment_note_detail, menu);

		// disabling the MORE [:] menu
		MenuItem item_operation = menu.findItem(R.id.menu_note_operation);
		item_operation.setVisible(false);

	}

	@Override
	public Object databaseHelper(Context context) {
		return null;
	}

	@Override
	public void handleArguments(Bundle bundle) {

		if (bundle.containsKey("row_id")) {
			noteMemo = (EditText) rootview
					.findViewById(R.id.txv_editNote_Description);
			row_id = bundle.getInt("row_id");
			stageid = bundle.getString("stage_id");
			tagid = bundle.getString("tag_id");

			setNoteTags(tagid);
			setNoteStages(scope.context());
			originalMemo = bundle.getString("row_details");
			noteMemo.setText(bundle.getString("row_details"));
		}
	}

	@Override
	public OEMenu menuHelper(Context context) {
		return null;
	}

	public void setNoteTags(String tagid) {
		noteTags = (TagsView) rootview.findViewById(R.id.txv_editNote_Tag);
		noteTags.allowDuplicates(false);
		noteTags.setTokenListener(this);
		noteTags.showImage(false);
		// noteTags.allowDuplicates(false);
		@SuppressWarnings("unused")
		String[] note_tags_items = getNoteTags(String.valueOf(tagid),
				scope.context());
	}

	public String[] getNoteTags(String note_note_id, Context context) {

		String oea_name = OpenERPAccountManager.currentUser(
				MainActivity.context).getAndroidName();
		db = new NoteDBHelper(context);
		List<String> note_tags = new ArrayList<String>();
		List<HashMap<String, Object>> records = db
				.executeSQL(
						"SELECT id,name,oea_name FROM note_tag where id in (select note_tag_id from note_note_note_tag_rel where note_note_id = ? and oea_name = ?) and oea_name = ?",
						new String[] { note_note_id, oea_name, oea_name });
		if (records.size() > 0) {
			for (HashMap<String, Object> row : records) {
				note_tags.add(row.get("name").toString());
				noteTags.addObject(new TagsItems(Integer.parseInt(row.get("id")
						.toString()), row.get("name").toString(), ""));
			}
		}
		return note_tags.toArray(new String[note_tags.size()]);
	}

	public void setNoteStages(Context context) {

		stages = new HashMap<String, String>();
		stagelist = new ArrayList<String>();

		db = new NoteDBHelper(context);
		NoteDBHelper.NoteStages stagesobj = db.new NoteStages(context);
		HashMap<String, Object> data = stagesobj.search(stagesobj);

		int total = Integer.parseInt(data.get("total").toString());
		if (total > 0) {
			@SuppressWarnings("unchecked")
			List<HashMap<String, Object>> rows = (List<HashMap<String, Object>>) data
					.get("records");
			for (HashMap<String, Object> row_data : rows) {
				String row_id = row_data.get("id").toString();
				String name = row_data.get("name").toString();
				stages.put(name, row_id);
				stagelist.add(name);
			}
		}

		noteStages = (Spinner) rootview
				.findViewById(R.id.spinner_editNote_Stage);
		stageAdapter = new ArrayAdapter<String>(scope.context(),
				android.R.layout.simple_spinner_dropdown_item, stagelist);
		noteStages.setAdapter(stageAdapter);

		int spinnerPosition = stageAdapter.getPosition(handleStages(stageid)
				.toString());
		// setting the Orginal Value
		noteStages.setSelection(spinnerPosition);
	}

	public String handleStages(String stageid) {

		try {
			// Format stage_id = [[6,"Today"]]
			if (!stageid.equalsIgnoreCase("false")) {
				stage_name = new JSONArray(stageid);
				originialStage = stage_name.getJSONArray(0).getString(1)
						.toString();
				return stage_name.getJSONArray(0).getString(1).toString();
			} else {
				originialStage = "New";
				return "New";
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void updateNote(int row_id) {
		try {

			tagID = getSelectedTagId();

			// For using generateName() of composeNote
			composeNote = new ComposeNoteActivity();
			long stage_id = Long.parseLong(stages.get(noteStages
					.getSelectedItem().toString()));

			ContentValues values = new ContentValues();
			values.put("stage_id", stage_id);
			values.put("name",
					composeNote.generateName(noteMemo.getText().toString()));
			values.put("memo", Html.toHtml(noteMemo.getText()));

			db = new NoteDBHelper(scope.context());

			JSONObject vals = new JSONObject();
			vals.put("stage_id",
					Integer.parseInt(values.get("stage_id").toString()));
			vals.put("memo", values.get("memo").toString());
			vals.put("name", values.get("name").toString());
			JSONArray tag_ids = new JSONArray();
			tag_ids.put(6);
			tag_ids.put(false);
			JSONArray c_ids = new JSONArray(tagID.toString());
			tag_ids.put(c_ids);
			vals.put("tag_ids", new JSONArray("[" + tag_ids.toString() + "]"));

			if (oe_obj.updateValues(db.getModelName(), vals, row_id)) {
				db.executeSQL(
						"delete from note_note_note_tag_rel where note_note_id = ? and oea_name = ?",
						new String[] { row_id + "",
								scope.User().getAndroidName() });
				for (int i = 0; i < tagID.length(); i++) {
					ContentValues rel_vals = new ContentValues();
					rel_vals.put("note_note_id", row_id);
					rel_vals.put("note_tag_id", tagID.getInt(i));
					rel_vals.put("oea_name", scope.User().getAndroidName());
					SQLiteDatabase insertDb = db.getWritableDatabase();
					insertDb.insert("note_note_note_tag_rel", null, rel_vals);
					insertDb.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isContentChanged(String memo) {
		if ((originalMemo.length() != memo.length())
				|| (!originialStage.equalsIgnoreCase(noteStages
						.getSelectedItem().toString()))) {
			return true;
		}
		return false;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	public void openDailogview(String title, String message,
			String positivebtnText, String negativebtnText) {
		AlertDialog.Builder deleteDialogConfirm = new AlertDialog.Builder(
				scope.context());
		deleteDialogConfirm.setTitle(title);
		deleteDialogConfirm.setMessage(message);
		deleteDialogConfirm.setCancelable(true);

		deleteDialogConfirm.setPositiveButton(positivebtnText,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						getActivity().getSupportFragmentManager()
								.popBackStack();
					}
				});
		deleteDialogConfirm.setNegativeButton(negativebtnText, null);
		deleteDialogConfirm.show();
	}

	public LinkedHashMap<String, String> getNoteTag() {
		String oea_name = OpenERPAccountManager.currentUser(
				MainActivity.context).getAndroidName();
		db = new NoteDBHelper(scope.context());
		List<HashMap<String, Object>> records = db.executeSQL(
				"SELECT id,name,oea_name FROM note_tag where oea_name = ?",
				new String[] { oea_name });
		LinkedHashMap<String, String> note_tag = new LinkedHashMap<String, String>();
		if (records.size() > 0) {
			for (HashMap<String, Object> row : records) {
				note_tag.put(row.get("name").toString(), row.get("id")
						.toString());
			}
		}
		return note_tag;
	}

	@Override
	public void onTokenAdded(Object token, View view) {
		// TODO Auto-generated method stub
		TagsItems item = (TagsItems) token;
		selectedTags.put("" + item.getId(), item);
	}

	@Override
	public void onTokenSelected(Object token, View view) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onTokenRemoved(Object token) {
		// TODO Auto-generated method stub
		TagsItems item = (TagsItems) token;
		selectedTags.remove("" + item.getId());
	}
}
