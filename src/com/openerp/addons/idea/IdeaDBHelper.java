package com.openerp.addons.idea;

import android.content.Context;

import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.Fields;
import com.openerp.orm.Types;

public class IdeaDBHelper extends BaseDBHelper {

    public IdeaDBHelper(Context context) {
	super(context);
	// TODO Auto-generated constructor stub
	name = "idea.idea";

	columns.add(new Fields("name", "Name", Types.varchar(64)));
	columns.add(new Fields("description", "Description", Types.text()));
	columns.add(new Fields("categoriy_ids", "Idea Category", Types
		.many2Many(new IdeaCategory(context))));

    }

    class IdeaCategory extends BaseDBHelper {

	public IdeaCategory(Context context) {
	    super(context);
	    // TODO Auto-generated constructor stub
	    name = "idea.category";
	    columns.add(new Fields("name", "Name", Types.text()));
	}

    }

}
