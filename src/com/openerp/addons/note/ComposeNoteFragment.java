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
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.openerp.MainActivity;
import com.openerp.R;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.menu.OEMenu;

public class ComposeNoteFragment extends BaseFragment {

	ArrayAdapter<String> adapter;
	Spinner noteStages = null;
	View rootView = null;
	ArrayList<String> stages = new ArrayList<String>();
	HashMap<String, Long> note_Stages = null;
	EditText noteDescription;
	String[] Name;
	long stage_id;
	String name, memo, open, stage_name;
	NoteDBHelper.NoteStages stagesobj;
	NoteDBHelper dbhelper = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		setHasOptionsMenu(true);
		scope = new AppScope(MainActivity.userContext,
				(MainActivity) getActivity());

		// Inflate the layout for this fragment
		rootView = inflater.inflate(R.layout.fragment_compose_note, container,
				false);
		handleArguments((Bundle) getArguments());
		return rootView;
	}

	public void fillNoteStages() {

		noteStages = (Spinner) rootView
				.findViewById(R.id.txv_composeNote_Stage);
		note_Stages = new HashMap<String, Long>();
		dbhelper = new NoteDBHelper(scope.context());
		stagesobj = dbhelper.new NoteStages(scope.context());
		HashMap<String, Object> data = stagesobj.search(stagesobj);
		int total = Integer.parseInt(data.get("total").toString());

		if (total > 0) {
			@SuppressWarnings("unchecked")
			List<HashMap<String, Object>> rows = (List<HashMap<String, Object>>) data
					.get("records");
			for (HashMap<String, Object> row_data : rows) {
				stages.add(row_data.get("name").toString());
				note_Stages.put(row_data.get("name").toString(),
						Long.parseLong(row_data.get("id").toString()));
			}
			stages.add("Add New");
		}

		adapter = new ArrayAdapter<String>(scope.context(),
				android.R.layout.simple_spinner_dropdown_item, stages);
		noteStages.setAdapter(adapter);

		noteStages.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub
				String selectedStageName = noteStages.getItemAtPosition(
						position).toString();
				if (selectedStageName.equalsIgnoreCase("Add New")) {
					createNoteStage();
					noteStages.setAdapter(adapter);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});
	}

	@Override
	public Object databaseHelper(Context context) {
		// TODO Auto-generated method stub
		return new NoteDBHelper(context);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.menu_fragment_note, menu);

		// disabling the Compose Note option cause you are already in that menu
		MenuItem item = menu.findItem(R.id.menu_note_compose);
		MenuItem item1 = menu.findItem(R.id.menu_note_search);
		item1.setVisible(false);
		item.setVisible(false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.menu_note_write:
			writeNote();
		case R.id.menu_note_cancel:
			getActivity().getSupportFragmentManager().popBackStack();

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public OEMenu menuHelper(Context context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleArguments(Bundle bundle) {
		// TODO Auto-generated method stub
		fillNoteStages();
	}

	private void writeNote() {

		noteDescription = (EditText) rootView
				.findViewById(R.id.txv_composeNote_Description);

		try {
			ContentValues values = new ContentValues();
			values.put("name", generateName(noteDescription.getText()
					.toString()));
			values.put("memo", noteDescription.getText().toString());
			values.put("open", "true");
			values.put("stage_id",
					note_Stages.get(noteStages.getSelectedItem().toString()));
			JSONArray tag_ids = new JSONArray();
			tag_ids.put(6);
			tag_ids.put(false);
			JSONArray c_ids = new JSONArray();
			tag_ids.put(c_ids);
			values.put("current_partner_id",
					Integer.parseInt(scope.User().getUser_id().toString()));
			values.put("tag_ids",
					new JSONArray("[" + tag_ids.toString() + "]").toString());

			dbhelper = new NoteDBHelper(scope.context());
			int newId = dbhelper.createRecordOnserver(dbhelper, values);
			values.put("id", newId);
			values.put("date_done", "false");
			dbhelper.create(dbhelper, values);
			getActivity().getSupportFragmentManager().popBackStack();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String generateName(String longName) {
		String[] splitName = (longName).split("\\n");
		if (splitName.length == 1) {
			name = longName;
		} else {
			name = splitName[0];
		}
		return name;
	}

	public void createNoteStage() {

		AlertDialog.Builder builder = new Builder(scope.context());
		final EditText stage = new EditText(scope.context());

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
					writeNoteStages(stage.getText().toString());
					stages.add(stages.size() - 1, stage.getText().toString());
					adapter.notifyDataSetChanged();
					noteStages.setSelection(stages.size() - 2);
				}
			}
		});
		builder.setNegativeButton("Cancel", new OnClickListener() {
			public void onClick(DialogInterface di, int i) {
			}
		});
		builder.create().show();
	}

	public void writeNoteStages(String stagename) {

		ContentValues values = new ContentValues();
		values.put("name", stagename);
		dbhelper = new NoteDBHelper(scope.context());
		NoteDBHelper.NoteStages notestageObj = dbhelper.new NoteStages(
				scope.context());
		int newId = notestageObj.createRecordOnserver(notestageObj, values);
		note_Stages.put(stagename, Long.parseLong(String.valueOf(newId)));
		values.put("id", newId);
		notestageObj.create(notestageObj, values);
	}

}
