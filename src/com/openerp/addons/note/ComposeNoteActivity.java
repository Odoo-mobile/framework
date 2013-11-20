package com.openerp.addons.note;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.openerp.MainActivity;
import com.openerp.R;
import com.openerp.auth.OpenERPAccountManager;
import com.openerp.support.AppScope;
import com.openerp.util.tags.TagsItems;
import com.openerp.util.tags.TagsView;

public class ComposeNoteActivity extends Activity implements
		TagsView.TokenListener {

	Spinner noteStages = null;
	ArrayAdapter<String> adapter;
	AppScope scope = null;
	NoteDBHelper.NoteStages stagesobj;
	NoteDBHelper dbhelper = null;
	HashMap<String, Long> note_Stages = null;
	ArrayList<String> stages = new ArrayList<String>();
	String name, memo, open, stage_name;
	EditText noteDescription;
	JSONArray tagID = new JSONArray();
	ArrayList<String> tagName = new ArrayList<String>();
	List<TagsItems> arr = new ArrayList<TagsItems>();
	HashMap<String, TagsItems> selectedTags = new HashMap<String, TagsItems>();
	ArrayList<String> keyList = null;
	ListView modeList;
	LinkedHashMap<String, String> note_tags = null;
	String[] stringArray = null;
	TagsView noteTags;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_compose_note);
		scope = new AppScope(MainActivity.userContext,
				(MainActivity) MainActivity.context);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		noteTags = (TagsView) findViewById(R.id.txv_composeNote_Tag);
		noteTags.allowDuplicates(false);
		noteTags.setTokenListener(this);
		noteTags.showImage(false);
		fillNoteStages();
	}

	// Called when + button pressed for adding tags
	public void addTags(View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
							// TODO Auto-generated method stub
							AlertDialog d = (AlertDialog) dialog;
							ListView v = d.getListView();
							int i = 0;
							while (i < stringArray.length) {
								if (v.isItemChecked(i)) {
									Integer id = Integer.parseInt(note_tags
											.get(v.getItemAtPosition(i)
													.toString()).toString());
									if (!selectedTags.containsKey(note_tags
											.get(v.getItemAtPosition(i)
													.toString()))) {
										noteTags.addObject(new TagsItems(id,
												stringArray[i], ""));
									}
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
		} else {
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
		case android.R.id.home:
			// app icon in action bar clicked; go home
			finish();
			return true;
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
					scope.context().refreshMenu(scope.context());
				}
			}
		});
		builder.setNegativeButton("Cancel", new OnClickListener() {
			public void onClick(DialogInterface di, int i) {
			}
		});
		builder.create().show();
	}

	public void createNotetag() {

		AlertDialog.Builder builder = new Builder(this);
		final EditText tag = new EditText(this);
		builder.setTitle("Tag Name").setMessage("Enter new Tag").setView(tag);
		builder.setPositiveButton("Create", new OnClickListener() {
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

	public void writeNoteTags(String tagname) {

		ContentValues values = new ContentValues();
		values.put("name", tagname);
		dbhelper = new NoteDBHelper(scope.context());
		NoteDBHelper.NoteTags notetagObj = dbhelper.new NoteTags(
				scope.context());
		int newId = notetagObj.createRecordOnserver(notetagObj, values);
		values.put("id", newId);
		notetagObj.create(notetagObj, values);
		noteTags.addObject(new TagsItems(newId, tagname, ""));
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
		tagID = getSelectedTagId();

		try {
			ContentValues values = new ContentValues();
			JSONObject vals = new JSONObject();
			values.put("name", generateName(noteDescription.getText()
					.toString()));
			vals.put("name", values.get("name").toString());

			values.put("memo", noteDescription.getText().toString());
			vals.put("memo", values.get("memo").toString());
			values.put("open", "true");
			vals.put("open", true);
			Long stageid = note_Stages.get(noteStages.getSelectedItem()
					.toString());
			if (stageid != null) {
				values.put("stage_id", note_Stages.get(noteStages
						.getSelectedItem().toString()));
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
				int newId = dbhelper.createRecordOnserver(dbhelper, vals);
				values.put("id", newId);
				values.put("date_done", "false");
				values.put("tag_ids", tagID.toString());
				int new_id = dbhelper.create(dbhelper, values);
				Intent resultIntent = new Intent();
				resultIntent.putExtra("result", new_id);
				setResult(Activity.RESULT_OK, resultIntent);
				finish();
			} else {
				Toast.makeText(scope.context(),
						"You can't keep Stage empty..!", Toast.LENGTH_SHORT)
						.show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JSONArray getSelectedTagId() {
		JSONArray list = new JSONArray();
		for (String key : selectedTags.keySet()) {
			list.put(selectedTags.get(key).getId());
		}
		return list;
	}

	@Override
	public void onTokenAdded(Object token, View view) {
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

	public LinkedHashMap<String, String> getNoteTag() {
		String oea_name = OpenERPAccountManager.currentUser(
				MainActivity.context).getAndroidName();
		dbhelper = new NoteDBHelper(scope.context());
		List<HashMap<String, Object>> records = dbhelper.executeSQL(
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
