package com.openerp.addons.note;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.openerp.base.res.Res_PartnerDBHelper;
import com.openerp.orm.OEColumn;
import com.openerp.orm.OEDatabase;
import com.openerp.orm.OEFields;

public class NoteDB extends OEDatabase {
	Context mContext = null;

	public NoteDB(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public String getModelName() {
		return "note.note";
	}

	@Override
	public List<OEColumn> getModelColumns() {
		List<OEColumn> cols = new ArrayList<OEColumn>();
		cols.add(new OEColumn("name", "Name", OEFields.varchar(64)));
		cols.add(new OEColumn("memo", "Memo", OEFields.varchar(64)));
		cols.add(new OEColumn("open", "Open", OEFields.varchar(64)));
		cols.add(new OEColumn("date_done", "Date_Done", OEFields.varchar(64)));
		cols.add(new OEColumn("stage_id", "NoteStages", OEFields
				.manyToOne(new NoteStages(mContext))));
		cols.add(new OEColumn("tag_ids", "NoteTags", OEFields
				.manyToMany(new NoteTags(mContext))));
		cols.add(new OEColumn("current_partner_id", "Res_Partner", OEFields
				.manyToOne(new Res_PartnerDBHelper(mContext))));
		cols.add(new OEColumn("note_pad_url", "URL", OEFields.text()));
		cols.add(new OEColumn("message_follower_ids", "Followers", OEFields
				.manyToMany(new Res_PartnerDBHelper(mContext))));
		return cols;
	}

	public class NoteStages extends OEDatabase {

		public NoteStages(Context context) {
			super(context);
		}

		@Override
		public String getModelName() {
			return "note.stage";
		}

		@Override
		public List<OEColumn> getModelColumns() {
			List<OEColumn> cols = new ArrayList<OEColumn>();
			cols.add(new OEColumn("name", "Name", OEFields.text()));
			return cols;
		}

	}

	public class NoteTags extends OEDatabase {

		public NoteTags(Context context) {
			super(context);
		}

		@Override
		public String getModelName() {
			return "note.tag";
		}

		@Override
		public List<OEColumn> getModelColumns() {
			List<OEColumn> cols = new ArrayList<OEColumn>();
			cols.add(new OEColumn("name", "Name", OEFields.text()));
			return cols;
		}
	}

}
