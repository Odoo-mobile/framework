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
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.odoo.R;
import com.odoo.orm.OColumn;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OModel;
import com.odoo.support.listview.OListAdapter;

/**
 * The Class OManyToOneWidget.
 */
public class OManyToOneWidget extends LinearLayout implements
		OnItemSelectedListener {

	/** The Constant KEY_MODEL_NAME. */
	public static final String KEY_MODEL_NAME = "model_name";

	/** The Constant KEY_COLUMN_NAME. */
	public static final String KEY_COLUMN_NAME = "column_name";

	/** The context. */
	Context mContext = null;

	/** The typed array. */
	TypedArray mTypedArray = null;

	/** The attrs. */
	OControlAttributes mAttrs = new OControlAttributes();

	/** The model. */
	OModel mModel = null;

	/** The column. */
	OColumn mColumn = null;

	/** The spinner. */
	Spinner mSpinner = null;

	/** The params. */
	LayoutParams mParams = null;

	/** The spinner adapter. */
	OListAdapter mSpinnerAdapter = null;

	/** The spinner objects. */
	List<Object> mSpinnerObjects = new ArrayList<Object>();

	/** The selected position. */
	Integer mSelectedPosition = -1;

	/** The current id. */
	Integer mCurrentId = -1;

	/** The many to one item change listener. */
	ManyToOneItemChangeListener mManyToOneItemChangeListener = null;

	/**
	 * Instantiates a new many to one widget.
	 * 
	 * @param context
	 *            the context
	 */
	public OManyToOneWidget(Context context) {
		super(context);
		init(context, null, 0);
	}

	/**
	 * Instantiates a new many to one widget.
	 * 
	 * @param context
	 *            the context
	 * @param attrs
	 *            the attrs
	 */
	public OManyToOneWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	/**
	 * Instantiates a new many to one widget.
	 * 
	 * @param context
	 *            the context
	 * @param attrs
	 *            the attrs
	 * @param defStyle
	 *            the def style
	 */
	public OManyToOneWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	/**
	 * Inits the control.
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
					R.styleable.OManyToOneWidget);
			mAttrs.put(KEY_MODEL_NAME, mTypedArray
					.getString(R.styleable.OManyToOneWidget_model_name));
			mAttrs.put(KEY_COLUMN_NAME, mTypedArray
					.getString(R.styleable.OManyToOneWidget_column_name));
			mModel = OModel.get(mContext,
					mAttrs.getString(KEY_MODEL_NAME, null));
			mColumn = mModel.getColumn(mAttrs.getString(KEY_COLUMN_NAME, null));
			mTypedArray.recycle();
		}
		initControls();
	}

	/**
	 * Re init the control.
	 */
	public void reInit() {
		initControls();
	}

	/**
	 * Inits the controls.
	 */
	private void initControls() {
		removeAllViews();
		mSpinner = new Spinner(mContext);
		mParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		mSpinner.setLayoutParams(mParams);
		if (mModel != null) {
			mSpinner.setTag("many_to_one_" + mModel.getTableName());
			addView(mSpinner);
			setAdapter();
		}
	}

	/**
	 * Sets the model.
	 * 
	 * @param model
	 *            the model
	 * @param column
	 *            the column
	 * @return the o many to one widget
	 */
	public OManyToOneWidget setModel(OModel model, String column) {
		mModel = model;
		mColumn = mModel.getColumn(column);
		return this;
	}

	/**
	 * Sets the record id.
	 * 
	 * @param id
	 *            the new record id
	 */
	public void setRecordId(Integer id) {
		mCurrentId = id;
	}

	/**
	 * Sets the adapter.
	 */
	private void setAdapter() {
		mSpinnerAdapter = new OListAdapter(mContext, 0, mSpinnerObjects) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ODataRow row = (ODataRow) mSpinnerObjects.get(position);
				return getRowForm(row);
			}

			@Override
			public View getDropDownView(int position, View convertView,
					ViewGroup parent) {
				ODataRow row = (ODataRow) mSpinnerObjects.get(position);
				return getRowForm(row);
			}
		};
		mSpinner.setAdapter(mSpinnerAdapter);
		fillRecords();
	}

	/**
	 * Fill records.
	 */
	private void fillRecords() {
		mSpinnerObjects.clear();
		ODataRow select_row = new ODataRow();
		select_row.put("id", 0);
		select_row.put(mColumn.getName(), "Select " + mColumn.getLabel());
		mSpinnerObjects.add(select_row);
		for (ODataRow row : mModel.select()) {
			mSpinnerObjects.add(row);
			if (mCurrentId > 0 && mCurrentId == row.getInt("id")) {
				mSelectedPosition = mSpinnerObjects.indexOf(row);
			}
		}
		mSpinnerAdapter.notifiyDataChange(mSpinnerObjects);
		mSpinner.setSelection(mSelectedPosition);
	}

	/**
	 * Gets the row form.
	 * 
	 * @param row
	 *            the row
	 * @return the row form
	 */
	private OForm getRowForm(ODataRow row) {
		AbsListView.LayoutParams mParams = new AbsListView.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		OForm form = new OForm(mContext);
		form.setOrientation(LinearLayout.VERTICAL);
		form.setLayoutParams(mParams);
		form.setModel(mModel);

		this.mParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		// Creating name field
		OField field = new OField(mContext);
		field.setFieldName(mColumn.getName());
		field.showLabel(false);
		field.setLayoutParams(this.mParams);
		field.setPadding(8, 8, 8, 8);
		field.reInit();
		field.setTextStyle(10);
		form.addView(field);
		form.initForm(row);

		return form;
	}

	/**
	 * Sets the on many to one item change listener.
	 * 
	 * @param listener
	 *            the new on many to one item change listener
	 */
	public void setOnManyToOneItemChangeListener(
			ManyToOneItemChangeListener listener) {
		Log.v("", "setOnManyToOneItemChangeListener()");
		mManyToOneItemChangeListener = listener;
		mSpinner.setOnItemSelectedListener(this);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		ODataRow row = (ODataRow) mSpinnerObjects.get(position);
		mManyToOneItemChangeListener
				.onManyToOneItemChangeListener(mColumn, row);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	/**
	 * The listener interface for receiving manyToOneItemChange events. The
	 * class that is interested in processing a manyToOneItemChange event
	 * implements this interface, and the object created with that class is
	 * registered with a component using the component's
	 * <code>addManyToOneItemChangeListener<code> method. When
	 * the manyToOneItemChange event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see ManyToOneItemChangeEvent
	 */
	public interface ManyToOneItemChangeListener {

		/**
		 * On many to one item change listener.
		 * 
		 * @param column
		 *            the column
		 * @param row
		 *            the row
		 */
		public void onManyToOneItemChangeListener(OColumn column, ODataRow row);
	}
}
