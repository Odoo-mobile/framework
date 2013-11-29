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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;

import com.openerp.base.res.Res_PartnerDBHelper;
import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.Fields;
import com.openerp.orm.OEHelper;
import com.openerp.orm.Types;
import com.openerp.support.OEUser;
import com.openerp.util.tags.TagsItems;

public class NoteDBHelper extends BaseDBHelper {

	Context mContext = null;

	public NoteDBHelper(Context context) {
		super(context);
		mContext = context;
		name = "note.note";

		columns.add(new Fields("name", "Name", Types.varchar(64)));
		columns.add(new Fields("memo", "Memo", Types.varchar(64)));
		columns.add(new Fields("open", "Open", Types.varchar(64)));
		columns.add(new Fields("date_done", "Date_Done", Types.varchar(64)));
		columns.add(new Fields("stage_id", "NoteStages", Types
				.many2One(new NoteStages(context))));
		columns.add(new Fields("tag_ids", "NoteTags", Types
				.many2Many(new NoteTags(context))));
		columns.add(new Fields("current_partner_id", "Res_Partner", Types
				.many2One(new Res_PartnerDBHelper(context))));
		columns.add(new Fields("note_pad_url", "URL", Types.text()));
	}

	// This method Will generate Name for the Notes
	public String generateName(String longName) {

		String[] splitName = (longName).split("\\n");

		if (splitName.length == 1) {
			name = longName;
		} else {
			name = splitName[0];
		}
		return name;
	}

	// This method will retrieve all the Tags from database
	public LinkedHashMap<String, String> getAllNoteTags() {

		String oea_name = OEUser.current(mContext).getAndroidName();
		LinkedHashMap<String, String> note_tag = new LinkedHashMap<String, String>();
		List<HashMap<String, Object>> records = executeSQL(
				"SELECT id,name,oea_name FROM note_tag where oea_name = ?",
				new String[] { oea_name });

		if (records.size() > 0) {
			for (HashMap<String, Object> row : records) {
				note_tag.put(row.get("name").toString(), row.get("id")
						.toString());
			}
		}
		return note_tag;
	}

	// This method will retrieve IDS of selected Tags from EditNotes or
	// Compose Notes
	public JSONArray getSelectedTagId(HashMap<String, TagsItems> selectedTags) {

		JSONArray list = new JSONArray();

		for (String key : selectedTags.keySet()) {
			list.put(selectedTags.get(key).getId());
		}
		return list;
	}

	// This Method will create tags in Notes
	public LinkedHashMap<String, String> writeNoteTags(String tagname) {

		LinkedHashMap<String, String> noteTags = new LinkedHashMap<String, String>();
		ContentValues values = new ContentValues();

		values.put("name", tagname);
		NoteDBHelper.NoteTags notetagObj = new NoteTags(mContext);
		int newId = notetagObj.createRecordOnserver(notetagObj, values);
		values.put("id", newId);
		notetagObj.create(notetagObj, values);
		noteTags.put("newID", String.valueOf(newId));
		noteTags.put("tagName", tagname);
		return noteTags;
	}

	// This Method will check wheather Pad Inastlled or Not
	public boolean isPadExist() {

		boolean state = false;
		OEHelper oe = getOEInstance();
		JSONObject domain = new JSONObject();

		try {
			domain.accumulate("domain", new JSONArray(
					"[[\"name\",\"ilike\",\"note_pad\"]]"));
			JSONObject fields = new JSONObject();
			fields.accumulate("fields", "state");
			JSONObject result = oe.search_read("ir.module.module", fields,
					domain, 0, 0, null, null);
			JSONArray ids = result.getJSONArray("records");
			if (ids.getJSONObject(0).getString("state")
					.equalsIgnoreCase("installed")) {
				state = true;
			}
		} catch (Exception e) {
		}
		return state;
	}

	// This Method will Generate The PADURL For Normal Notes in Edit Menu
	public String getURL(OEHelper oe, Integer noteid) {

		String urllink = null;

		try {
			JSONObject newValues = new JSONObject();
			JSONObject newContext = new JSONObject();
			newContext.put("model", "note.note");
			newContext.put("field_name", "note_pad_url");
			newValues.put("context", newContext);

			if (noteid != null) {
				newContext.put("object_id", noteid);
			}

			oe.updateKWargs(newValues);
			JSONObject obj = oe.call_kw("note.note", "pad_generate_url",
					new JSONArray());
			urllink = obj.getJSONObject("result").getString("url");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return urllink;
	}

	// This Method will Generate The PADURL In Compose Menu
	public String getURL(OEHelper oe) {
		return getURL(oe, null);
	}

	public class NoteStages extends BaseDBHelper {

		public NoteStages(Context context) {
			super(context);
			name = "note.stage";
			columns.add(new Fields("name", "Name", Types.text()));
		}

	}

	public class NoteTags extends BaseDBHelper {

		public NoteTags(Context context) {
			super(context);
			name = "note.tag";
			columns.add(new Fields("name", "Name", Types.text()));
		}
	}
}
