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
import java.util.LinkedHashMap;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.odoo.R;
import com.odoo.orm.OColumn;
import com.odoo.orm.OColumn.ColumnDomain;
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
	private Context mContext = null;

	/** The typed array. */
	private TypedArray mTypedArray = null;

	/** The attrs. */
	private OControlAttributes mAttrs = new OControlAttributes();

	/** The model. */
	private OModel mModel = null;

	/** The column. */
	private OColumn mColumn = null;

	/** The spinner. */
	private Spinner mSpinner = null;

	/** The params. */
	private LayoutParams mParams = null;

	/** The spinner adapter. */
	private OListAdapter mSpinnerAdapter = null;

	/** The spinner objects. */
	private List<Object> mSpinnerObjects = new ArrayList<Object>();

	/** The selected position. */
	private Integer mSelectedPosition = -1;

	/** The current id. */
	private Integer mCurrentId = -1;

	/** The many to one item change listener. */
	private ManyToOneItemChangeListener mManyToOneItemChangeListener = null;

	/** The custom_layout. */
	private Integer custom_layout = null;

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
	 * Sets the custom layout.
	 * 
	 * @param layout
	 *            the new custom layout
	 */
	public void setCustomLayout(Integer layout) {
		custom_layout = layout;
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
		return setModel(model, column,
				new LinkedHashMap<String, OColumn.ColumnDomain>());
	}

	public OManyToOneWidget setModel(OModel model, String column,
			LinkedHashMap<String, ColumnDomain> domains) {
		mModel = model;
		mColumn = mModel.getColumn(column);
		mColumn.cloneDomain(domains);
		return this;
	}

	public void setDomains(LinkedHashMap<String, ColumnDomain> domains) {
		mColumn.cloneDomain(domains);
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
		select_row.put(OColumn.ROW_ID, 0);
		select_row.put(mColumn.getName(), "Select " + mColumn.getLabel());
		mSpinnerObjects.add(select_row);
		StringBuffer whr = new StringBuffer();
		List<Object> args_list = new ArrayList<Object>();
		for (String key : mColumn.getDomains().keySet()) {
			ColumnDomain domain = mColumn.getDomains().get(key);
			if (domain.getConditionalOperator() != null) {
				whr.append(domain.getConditionalOperator());
			} else {
				whr.append(" ");
				whr.append(domain.getColumn());
				whr.append(" ");
				whr.append(domain.getOperator());
				whr.append(" ? ");
				args_list.add(domain.getValue());
			}
		}
		String where = null;
		Object[] args = null;
		if (args_list.size() > 0) {
			where = whr.toString();
			args = args_list.toArray(new Object[args_list.size()]);
		}
		for (ODataRow row : mModel.select(where, args)) {
			mSpinnerObjects.add(row);
			if (mCurrentId > 0 && mCurrentId == row.getInt(OColumn.ROW_ID)) {
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
		OForm form = null;
		if (custom_layout != null) {
			form = (OForm) LayoutInflater.from(mContext).inflate(custom_layout,
					null);
		} else {
			form = new OForm(mContext);
			form.setLayoutParams(mParams);
			form.setOrientation(LinearLayout.VERTICAL);
		}
		form.setModel(mModel);

		if (custom_layout == null) {
			this.mParams = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			// Creating name field
			OField field = new OField(mContext);
			field.setFieldName(mColumn.getName());
			field.showLabel(false);
			field.setLayoutParams(this.mParams);
			field.setPadding(8, 8, 8, 8);
			field.reInit();
			field.setTextAppearance(android.R.attr.textAppearanceMedium);
			form.addView(field);
		}
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
