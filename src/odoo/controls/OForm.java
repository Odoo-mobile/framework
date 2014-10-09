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
import java.util.LinkedHashMap;
import java.util.List;

import odoo.controls.OField.OFieldType;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.odoo.R;
import com.odoo.orm.OColumn;
import com.odoo.orm.OColumn.ColumnDomain;
import com.odoo.orm.OColumn.RelationType;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OModel;
import com.odoo.orm.OValues;
import com.odoo.orm.types.OBlob;
import com.odoo.orm.types.OBoolean;
import com.odoo.orm.types.OHtml;

/**
 * The Class OForm.
 */
public class OForm extends LinearLayout implements View.OnClickListener {

	/** The Constant KEY_BACKGROUND_SELECTOR. */
	public static final String KEY_BACKGROUND_SELECTOR = "background_selector";

	/** The Constant KEY_BACKGROUND_SELECTOR_BOOLEAN_FIELD. */
	public static final String KEY_BACKGROUND_SELECTOR_BOOLEAN_FIELD = "background_selector_boolean_field";

	/** The Constant KEY_TRUE_BACKGROUND_SELECTOR. */
	public static final String KEY_TRUE_BACKGROUND_SELECTOR = "true_background_selector";

	/** The Constant KEY_FALSE_BACKGROUND_SELECTOR. */
	public static final String KEY_FALSE_BACKGROUND_SELECTOR = "false_background_selector";

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

	/** The m form field controls. */
	HashMap<String, OField> mFormFieldControls = new HashMap<String, OField>();

	/** The m on view click listener. */
	OnViewClickListener mOnViewClickListener = null;

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
			mAttrs.put(
					KEY_BACKGROUND_SELECTOR_BOOLEAN_FIELD,
					mTypedArray
							.getString(R.styleable.OForm_background_selector_boolean_field));
			mAttrs.put(KEY_TRUE_BACKGROUND_SELECTOR, mTypedArray.getResourceId(
					R.styleable.OForm_true_background_selector, -1));
			mAttrs.put(KEY_FALSE_BACKGROUND_SELECTOR, mTypedArray
					.getResourceId(R.styleable.OForm_false_background_selector,
							-1));
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
		mFormFieldControls.clear();
		findAllFields(this);

		if (mAttrs.get(KEY_BACKGROUND_SELECTOR_BOOLEAN_FIELD) != null) {
			Boolean val = mRecord.getBoolean(mAttrs.getString(
					KEY_BACKGROUND_SELECTOR_BOOLEAN_FIELD, ""));
			if (val) {
				setBackgroundResource(mAttrs.getResource(
						KEY_TRUE_BACKGROUND_SELECTOR, -1));
			} else {
				setBackgroundResource(mAttrs.getResource(
						KEY_FALSE_BACKGROUND_SELECTOR, -1));
			}
			setClickable(true);
		} else {
			if (mAttrs.getResource(KEY_BACKGROUND_SELECTOR, 0) != 0) {
				setBackgroundResource(mAttrs.getResource(
						KEY_BACKGROUND_SELECTOR, 0));
				setClickable(true);
			}
		}
		for (String key : mFormFieldControls.keySet()) {
			View v = mFormFieldControls.get(key);
			if (v instanceof OField) {
				OField field = (OField) v;
				field.reInit();
				OColumn column = mModel.getColumn(field.getFieldName());
				OModel ref_model = null;
				if (column != null) {
					if (column.getRelationType() != null) {
						ref_model = mModel.createInstance(column.getType());
					}
					OColumn ref_column = null;
					if (field.getRefColumn() != null) {
						ref_column = ref_model.getColumn(field.getRefColumn());
					}
					field.setColumn(column);
					/**
					 * Check for onChange of columns in EditMode
					 */
					if (column.hasOnChange() && editable) {
						setOnChangeForColumn(field, column);
					}

					/**
					 * Check for domain filter column.
					 */
					if (column.hasDomainFilterColumn() && editable) {
						setOnDomainFilterCallBack(field, column);
					}
					OFieldType widget = null;
					String label = field.getFieldName();
					mFieldColumns.put(field.getFieldName(), column);
					mFields.add(field.getTag().toString());
					label = column.getLabel();
					if (column.getRelationType() != null
							&& column.getRelationType() == RelationType.ManyToOne) {
						widget = OFieldType.MANY_TO_ONE;
					}
					if (column.getRelationType() != null
							&& (column.getRelationType() == RelationType.ManyToMany || column
									.getRelationType() == RelationType.OneToMany)) {
						widget = OFieldType.MANY_TO_MANY_TAGS;
					}
					if (column.isFunctionalColumn()) {
						Object value = mRecord.get(column.getName());
						if (column.getType() == null)
							column.setType(value.getClass());
						field.setColumn(column);
					}
					if (column.getType().isAssignableFrom(OBlob.class)
							|| (ref_column != null && ref_column.getType()
									.isAssignableFrom(OBlob.class))) {
						widget = OFieldType.BINARY;
					}
					if (column.getType().isAssignableFrom(OBoolean.class)) {
						widget = OFieldType.BOOLEAN_WIDGET;
					}
					if (column.getType().isAssignableFrom(OHtml.class)) {
						widget = OFieldType.WEB_VIEW;
					}
					if (widget != null) {
						if (mRecord != null
								&& mRecord.contains(column.getName())) {
							field.createControl(widget, column, mRecord);
							field.setEditable(editable);
						} else {
							if (mRecord != null
									&& !mRecord.contains(column.getName()))
								field.setVisibility(View.GONE);
							else {
								field.createControl(widget, column, mRecord);
								field.setEditable(editable);
							}
						}
					} else {
						field.setEditable(editable);
						if (mRecord != null)
							field.setText(mRecord.getString(field
									.getFieldName()));
						else
							field.setText("");
					}
					field.setLabel(label);
				} else {
					if (mRecord != null) {
						if (field.getWidget() != null) {
							OColumn col = new OColumn("");
							col.setName(field.getFieldName());
							field.createControl(field.getWidget(), col, mRecord);
						} else
							field.setText(mRecord.getString(field
									.getFieldName()));
					}
				}
			}
		}
	}

	private void setOnDomainFilterCallBack(final OField field, final OColumn col) {
		LinkedHashMap<String, ColumnDomain> filter_domain = col
				.getFilterDomains();
		for (String k : filter_domain.keySet()) {
			ColumnDomain dmn = filter_domain.get(k);
			if (dmn.getColumn() != null) {
				OField fld = mFormFieldControls.get(dmn.getColumn());
				if (fld != null) {
					fld.setOnFilterDomainCallBack(dmn,
							new OnDomainFilterCallbacks() {

								@Override
								public void onFieldValueChanged(
										ColumnDomain domain) {
									col.addDomain(domain.getColumn(),
											domain.getOperator(),
											domain.getValue());
									if (field.getWidget() != null
											&& field.getWidget() == OFieldType.MANY_TO_ONE) {
										col.setHasDomainFilterColumn(false);
										field.setColumn(col);
										field.reInit();
									}
								}
							});
				}
			}
		}
	}

	private void setOnChangeForColumn(OField field, final OColumn col) {
		field.setOnChangeCallBack(new OnChangeCallback() {

			@Override
			public void onValueChange(ODataRow row) {
				if (!col.isOnChangeBGProcess()) {
					ODataRow vals = mModel.getOnChangeValue(col, row);
					for (String key : vals.keys()) {
						if (mFormFieldControls.containsKey(key)) {
							OField field = mFormFieldControls.get(key);
							if (field.getWidget() != null) {
								// Relation field
								field.selectManyToOneRecord(vals.getInt(key));
							} else {
								field.setText(vals.getString(key));
							}
						}
					}
				} else {
					OnChangeBackgroundProcess bgProcess = new OnChangeBackgroundProcess(
							mContext, mModel, col, row);
					bgProcess.execute();
				}
			}
		});
	}

	private class OnChangeBackgroundProcess extends
			AsyncTask<Void, Void, ODataRow> {
		private OModel mModel;
		private OColumn mCol;
		private ODataRow mRow;
		private ProgressDialog mDialog;

		public OnChangeBackgroundProcess(Context context, OModel model,
				OColumn col, ODataRow row) {
			mModel = model;
			mCol = col;
			mRow = row;
			mDialog = new ProgressDialog(context);
			mDialog.setTitle(context.getString(R.string.title_working));
			mDialog.setMessage(context.getString(R.string.title_please_wait));
			mDialog.setCancelable(false);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mDialog.show();
		}

		@Override
		protected ODataRow doInBackground(Void... params) {
			try {
				Thread.sleep(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return mModel.getOnChangeValue(mCol, mRow);
		}

		@Override
		protected void onPostExecute(ODataRow vals) {
			super.onPostExecute(vals);
			for (String key : vals.keys()) {
				if (mFormFieldControls.containsKey(key)) {
					OField field = mFormFieldControls.get(key);
					if (field.getWidget() != null) {
						// Relation field
						field.selectManyToOneRecord(vals.getInt(key));
					} else {
						field.setText(vals.getString(key));
					}
				}
			}
			mDialog.dismiss();
		}
	}

	/**
	 * Find all fields.
	 * 
	 * @param view
	 *            the view
	 */
	private void findAllFields(ViewGroup view) {
		int childs = view.getChildCount();
		for (int i = 0; i < childs; i++) {
			View v = view.getChildAt(i);
			if (v instanceof LinearLayout || v instanceof RelativeLayout) {
				findAllFields((ViewGroup) v);
			}
			if (v instanceof OField) {
				OField field = (OField) v;
				mFormFieldControls.put(field.getFieldName(), field);
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
				if (field != null) {
					if (field.getValue() != null) {
						values.put(field.getFieldName(), field.getValue());
					}
				}
			}
			if (mRecord != null) {
				values.put("id", mRecord.getInt("id"));
				values.put("local_id", mRecord.getInt(OColumn.ROW_ID));
				values.put("is_dirty", true);
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
			if (field != null) {
				OColumn col = mFieldColumns.get(field.getFieldName());
				field.setError(null);
				if (col.isRequired() && field.isEmpty()) {
					field.setError(col.getLabel() + " is required");
					return false;
				}
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

	/**
	 * Sets the on view click listener.
	 * 
	 * @param view_id
	 *            the view_id
	 * @param listener
	 *            the listener
	 */
	public void setOnViewClickListener(int view_id, OnViewClickListener listener) {
		findViewById(view_id).setOnClickListener(this);
		mOnViewClickListener = listener;
	}

	/**
	 * The listener interface for receiving onViewClick events. The class that
	 * is interested in processing a onViewClick event implements this
	 * interface, and the object created with that class is registered with a
	 * component using the component's
	 * <code>addOnViewClickListener<code> method. When
	 * the onViewClick event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnViewClickEvent
	 */
	public interface OnViewClickListener {

		/**
		 * On form view click.
		 * 
		 * @param view
		 *            the view
		 * @param row
		 *            the row
		 */
		public void onFormViewClick(View view, ODataRow row);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		if (mOnViewClickListener != null) {
			mOnViewClickListener.onFormViewClick(v, mRecord);
		}
	}

}
