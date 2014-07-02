package com.odoo.base.ir;

import java.util.ArrayList;
import java.util.List;

import odoo.OEDomain;

import org.json.JSONArray;

import android.content.Context;

import com.odoo.orm.OColumn;
import com.odoo.orm.OColumn.RelationType;
import com.odoo.orm.OModel;
import com.odoo.orm.types.OBoolean;
import com.odoo.orm.types.OVarchar;
import com.odoo.util.PreferenceManager;

public class IrModelFields extends OModel {

	Context mContext = null;
	List<String> mDefaultColumns = new ArrayList<String>();

	OColumn name = new OColumn("Field name", OVarchar.class, 100);
	OColumn ttype = new OColumn("Field Type", OVarchar.class, 100);
	OColumn required = new OColumn("Required", OBoolean.class);
	OColumn readonly = new OColumn("Readonly", OBoolean.class);
	OColumn model_id = new OColumn("Model", IrModel.class,
			RelationType.ManyToOne);

	public IrModelFields(Context context) {
		super(context, "ir.model.fields");
		mContext = context;
		PreferenceManager pfManager = new PreferenceManager(mContext);
		List<String> models = pfManager.getStringSet("models");
		for (String model : models) {
			List<String> cols = pfManager.getStringSet(model + ".server");
			mDefaultColumns.addAll(cols);
		}
	}

	@Override
	public OEDomain defaultDomain() {
		OEDomain domain = new OEDomain();
		try {
			domain.add("name", "in", new JSONArray(mDefaultColumns.toString()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return domain;
	}
}
