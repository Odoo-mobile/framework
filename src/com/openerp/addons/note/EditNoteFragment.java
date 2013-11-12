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
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Spinner;

import com.openerp.MainActivity;
import com.openerp.R;
import com.openerp.orm.OEHelper;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.menu.OEMenu;
import com.openerp.util.OnBackButtonPressedListener;

public class EditNoteFragment extends BaseFragment {

	View rootview;
	ImageView addTags;
	Spinner noteStages;
	ArrayAdapter<String> stageAdapter = null;
	HashMap<String, String> stages = new HashMap<String, String>();;
	EditText noteName, noteMemo, noteTag;
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
	String originalMemo, originialStage, originialtag;
	private static final int NOTE_ID = 0;
	JSONArray tagID = new JSONArray();
	ArrayList<String> tagName = new ArrayList<String>();
	private static OEHelper oe_obj = null;
	boolean flag = false;

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
				// TODO Auto-generated method stub
				Intent EditNoteTags = new Intent(scope.context(), AddTags.class);
				startActivityForResult(EditNoteTags, NOTE_ID);
			}
		});

		scope.context().setOnBackPressed(new OnBackButtonPressedListener() {

			@Override
			public boolean onBackPressed() {
				if (isContentChanged(noteMemo.getText().toString(), noteTag
						.getText().toString())) {
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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {

		case NOTE_ID:
			if (resultCode == Activity.RESULT_OK) {
				try {
					tagID = new JSONArray(data.getExtras().get("result")
							.toString());
				} catch (Exception e) {
				}
				tagName = data.getExtras().getStringArrayList("result1");
				noteTag.setText(tagName.toString().replace("[", "")
						.replace("]", ""));
			}
			break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {

		case R.id.menu_note_edit_save:
			updateNote(row_id);
			flag = true;
			getActivity().getSupportFragmentManager().popBackStack();
			return true;

		case R.id.menu_note_edit_cancel:
			if (isContentChanged(noteMemo.getText().toString(), noteTag
					.getText().toString())) {
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
			noteTag = (EditText) rootview.findViewById(R.id.txv_editNote_Tag);
			row_id = bundle.getInt("row_id");
			stageid = bundle.getString("stage_id");
			tagid = bundle.getString("tag_id");
			originialtag = tagid;
			noteTag.setText(tagid);
			setNoteStages(scope.context());
			originalMemo = bundle.getString("row_details");
			noteMemo.setText(bundle.getString("row_details"));
		}
	}

	@Override
	public OEMenu menuHelper(Context context) {
		// TODO Auto-generated method stub
		return null;
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

	public boolean isContentChanged(String memo, String tag) {
		if ((originalMemo.length() != memo.length())
				|| (!originialStage.equalsIgnoreCase(noteStages
						.getSelectedItem().toString()))
				|| (originialtag.length() != tag.length())) {
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
}
