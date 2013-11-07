package com.openerp.addons.note;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONArray;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.openerp.MainActivity;
import com.openerp.R;
import com.openerp.auth.OpenERPAccountManager;
import com.openerp.support.AppScope;

public class AddTags extends Activity {

	Button addTags, morePartners, createNew;
	int record_id = 0;
	String message = null;

	ListView partner_list;
	AppScope scope = null;
	NoteDBHelper db = null;
	ArrayAdapter<String> adapter = null;
	LinkedHashMap<String, String> note_tags = null;
	ArrayList<String> keyList = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_compose_note_select_tags);

		addTags = (Button) findViewById(R.id.btn_addtags);
		createNew = (Button) findViewById(R.id.btn_createtags);
		scope = new AppScope(MainActivity.userContext,
				(MainActivity) MainActivity.context);

		db = new NoteDBHelper(scope.context());
		partner_list = (ListView) findViewById(R.id.lsttags);
		note_tags = getNoteTags();
		keyList = new ArrayList<String>(note_tags.keySet());

		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_multiple_choice, keyList);
		partner_list.setAdapter(adapter);

		addTags.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getSelecetedTags();
			}
		});

		createNew.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				createNoteStage();
			}
		});

	}

	public void createNoteStage() {

		AlertDialog.Builder builder = new Builder(this);
		final EditText stage = new EditText(this);
		builder.setTitle("Stage Name").setMessage("Enter new Stage")
				.setView(stage);
		builder.setPositiveButton("Create",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						// do something with onClick
						if ((stage.getText().toString())
								.equalsIgnoreCase("Add New")
								|| (stage.getText().toString())
										.equalsIgnoreCase("AddNew")) {
							Toast.makeText(scope.context(),
									"CHOOSE ANOTHER NAME", Toast.LENGTH_SHORT)
									.show();
						} else {

							writeNoteTags(stage.getText().toString());
							keyList.add(keyList.size(), stage.getText()
									.toString());
							adapter.notifyDataSetChanged();
							scope.context().refreshMenu(scope.context());
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
		note_tags.put(tagname, String.valueOf(newId));
		values.put("id", newId);
		notetagObj.create(notetagObj, values);
	}

	public void getSelecetedTags() {
		JSONArray ids = new JSONArray();
		ArrayList<String> val = new ArrayList<String>();

		SparseBooleanArray checked = partner_list.getCheckedItemPositions();
		for (int i = 0; i < checked.size(); i++) {
			int key = checked.keyAt(i);
			boolean value = checked.get(key);
			if (value) {
				ids.put(Integer.parseInt(note_tags.get(
						partner_list.getItemAtPosition(key).toString())
						.toString()));
				val.add(partner_list.getItemAtPosition(key).toString());
			}
		}

		Intent resultIntent = new Intent();
		resultIntent.putExtra("result", ids.toString());
		resultIntent.putExtra("result1", val);
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}

	public LinkedHashMap<String, String> getNoteTags() {
		String oea_name = OpenERPAccountManager.currentUser(
				MainActivity.context).getAndroidName();
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
}
