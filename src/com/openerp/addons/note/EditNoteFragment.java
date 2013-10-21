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

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.openerp.MainActivity;
import com.openerp.R;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.menu.OEMenu;

public class EditNoteFragment extends BaseFragment {

	View rootview;
	Spinner noteStages;
	ArrayAdapter<String> stageAdapter = null;
	HashMap<String, String> stages = new HashMap<String, String>();;
	EditText noteName, noteMemo;
	int row_id = 0;
	ArrayList<String> stagelist = null;
	String row_status = null;
	String stageid = null;
	String memo = null;
	String name = null;
	NoteDBHelper db = null;
	ComposeNoteActivity composeNote = null;

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
		return rootview;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {

		case R.id.menu_note_edit_save:
			updateNote(row_id);
			getActivity().getSupportFragmentManager().popBackStack();
			return true;

		case R.id.menu_note_edit_cancel:
			getActivity().getSupportFragmentManager().popBackStack();
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
			setNoteStages(scope.context());
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
				JSONArray tem = new JSONArray(stageid);
				return tem.getJSONArray(0).getString(1).toString();
			} else {
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
			db.write(db, values, row_id);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
