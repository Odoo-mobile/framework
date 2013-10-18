package com.openerp.addons.note;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.openerp.MainActivity;
import com.openerp.R;
import com.openerp.support.AppScope;

public class ComposeNoteActivity extends Activity {

	Spinner noteStages = null;
	ArrayAdapter<String> adapter;
	AppScope scope = null;
	NoteDBHelper.NoteStages stagesobj;
	NoteDBHelper dbhelper = null;
	HashMap<String, Long> note_Stages = null;
	ArrayList<String> stages = new ArrayList<String>();
	String name, memo, open, stage_name;
	EditText noteDescription;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_compose_note);
		scope = new AppScope(MainActivity.userContext,
				(MainActivity) MainActivity.context);
		fillNoteStages();
	}

	public void fillNoteStages() {

		noteStages = (Spinner) findViewById(R.id.txv_composeNote_Stage);
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
					// noteStages.setAdapter(adapter);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.menu_fragment_note, menu);

		// disabling the Compose Note option cause you are already in that menu
		MenuItem item = menu.findItem(R.id.menu_note_compose);
		MenuItem item1 = menu.findItem(R.id.menu_note_search);
		item1.setVisible(false);
		item.setVisible(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.menu_note_write:
			writeNote();
			return true;
		case R.id.menu_note_cancel:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
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

	public String generateName(String longName) {
		String[] splitName = (longName).split("\\n");
		if (splitName.length == 1) {
			name = longName;
		} else {
			name = splitName[0];
		}
		return name;
	}

	private void writeNote() {

		noteDescription = (EditText) findViewById(R.id.txv_composeNote_Description);

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
			int new_id = dbhelper.create(dbhelper, values);

			Intent resultIntent = new Intent();
			resultIntent.putExtra("result", new_id);
			setResult(Activity.RESULT_OK, resultIntent);
			finish();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
