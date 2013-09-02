package com.openerp.addons.note;

import android.content.Context;

import com.openerp.base.res.Res_PartnerDBHelper;
import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.Fields;
import com.openerp.orm.Types;

public class NoteDBHelper extends BaseDBHelper {

    public NoteDBHelper(Context context) {
	super(context);
	// TODO Auto-generated constructor stub
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

    }

    public class NoteStages extends BaseDBHelper {

	public NoteStages(Context context) {
	    super(context);
	    // TODO Auto-generated constructor stub
	    name = "note.stage";
	    columns.add(new Fields("name", "Name", Types.text()));
	}

    }

    public class NoteTags extends BaseDBHelper {

	public NoteTags(Context context) {
	    super(context);
	    // TODO Auto-generated constructor stub
	    name = "note.tag";
	    columns.add(new Fields("name", "Name", Types.text()));
	}

    }
}
