/*
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
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
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * 
 */
package odoo.controls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.odoo.R;
import com.odoo.orm.OColumn;
import com.odoo.orm.OColumn.RelationType;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OValues;
import com.odoo.orm.OModel;

/**
 * The Class OForm.
 */
public class OForm extends LinearLayout {

	/** The Constant KEY_BACKGROUND_SELECTOR. */
	public static final String KEY_BACKGROUND_SELECTOR = "background_selector";

	/** The Constant KEY_MODEL. */
	public static final String KEY_MODEL = "model";

	/** The context. */
	Context mContext = null;

	/** The typed array. */
	TypedArray mTypedArray = null;

	/** The current record. */
	ODataRow mRecord = null;

	/** The control attributes. */
	OControlAttributes mAttrs = new OControlAttributes();

	/** The current data model. */
	OModel mModel = null;

	/** The fields. */
	List<String> mFields = new ArrayList<String>();

	/** The field columns. */
	HashMap<String, OColumn> mFieldColumns = new HashMap<String, OColumn>();

	/**
	 * Instantiates a new form.
	 * 
	 * @param context
	 *            the context
	 */
	public OForm(Context context) {
		super(context);
		init(context, null, 0);
	}

	/**
	 * Instantiates a new form.
	 * 
	 * @param context
	 *            the context
	 * @param attrs
	 *            the attrs
	 */
	public OForm(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	/**
	 * Instantiates a new form.
	 * 
	 * @param context
	 *            the context
	 * @param attrs
	 *            the attrs
	 * @param defStyle
	 *            the def style
	 */
	public OForm(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	/**
	 * Inits the form control.
	 * 
	 * @param context
	 *            the context
	 * @param attrs
	 *            the attrs
	 * @param defStyle
	 *            the def style
	 */
	private void init(Context context, AttributeSet attrs, int defStyle) {
		mContext = context;
		if (attrs != null) {
			mTypedArray = mContext.obtainStyledAttributes(attrs,
					R.styleable.OForm);
			mAttrs.put(KEY_BACKGROUND_SELECTOR, mTypedArray.getResourceId(
					R.styleable.OForm_background_selector, 0));
			mAttrs.put(KEY_MODEL,
					mTypedArray.getString(R.styleable.OForm_model));
			mModel = OModel.get(mContext, mAttrs.getString(KEY_MODEL, null));
			mTypedArray.recycle();
		}
	}

	/**
	 * _init form.
	 * 
	 * @param editable
	 *            the editable
	 */
	private void _initForm(boolean editable) {
		mFieldColumns.clear();
		if (mAttrs.getResource(KEY_BACKGROUND_SELECTOR, 0) != 0) {
			setBackgroundResource(mAttrs
					.getResource(KEY_BACKGROUND_SELECTOR, 0));
			setClickable(true);
		}
		int childs = getChildCount();
		for (int i = 0; i < childs; i++) {
			View v = getChildAt(i);
			if (v instanceof OField) {
				OField field = (OField) getChildAt(i);
				OColumn column = mModel.getColumn(field.getFieldName());
				Boolean widget = false;
				String label = field.getFieldName();
				if (column != null) {
					mFieldColumns.put(field.getFieldName(), column);
					mFields.add(field.getTag().toString());
					label = column.getLabel();
					if (column.getRelationType() != null
							&& column.getRelationType() == RelationType.ManyToOne)
						widget = true;
				}
				field.setEditable(editable);
				if (widget) {
					field.showAsManyToOne(column, mRecord);
				} else {
					if (mRecord != null)
						field.setText(mRecord.getString(field.getFieldName()));
				}
				field.setLabel(label);

				switch (field.getType()) {
				case EDITABLE:
					break;
				case READONLY:
					break;
				}
			}
		}
	}

	/**
	 * Sets the model.
	 * 
	 * @param model
	 *            the new model
	 */
	public void setModel(OModel model) {
		mModel = model;
	}

	/**
	 * Inits the form.
	 * 
	 * @param record
	 *            the record
	 */
	public void initForm(ODataRow record) {
		initForm(record, false);
	}

	/**
	 * Gets the form values.
	 * 
	 * @return the form values, null if validation failed
	 */
	public OValues getFormValues() {
		OValues values = null;
		if (validateForm()) {
			values = new OValues();
			for (String key : mFields) {
				OField field = (OField) findViewWithTag(key);
				values.put(field.getFieldName(), field.getValue());
			}
			if (mRecord != null) {
				values.put("local_record",
						Boolean.parseBoolean(mRecord.getString("local_record")));
				if (values.getBoolean("local_record")) {
					values.put("local_id", mRecord.getInt("local_id"));
					values.put("is_dirty", true);
				}
			}
		}
		return values;
	}

	/**
	 * Validate form.
	 * 
	 * @return true, if successful
	 */
	private boolean validateForm() {
		for (String key : mFields) {
			OField field = (OField) findViewWithTag(key);
			OColumn col = mFieldColumns.get(field.getFieldName());
			field.setError(null);
			if (col.isRequired() && field.isEmpty()) {
				field.setError(col.getLabel() + " is required");
				return false;
			}
		}
		return true;
	}

	/**
	 * Inits the form width data and editable mode.
	 * 
	 * @param record
	 *            the record
	 * @param editable
	 *            the editable
	 */
	public void initForm(ODataRow record, boolean editable) {
		mRecord = record;
		_initForm(editable);
	}

	/**
	 * Sets the editable.
	 * 
	 * @param mEditMode
	 *            the new editable
	 */
	public void setEditable(Boolean mEditMode) {
		_initForm(mEditMode);
	}

}
