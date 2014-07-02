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

public class OManyToOneWidget extends LinearLayout implements
		OnItemSelectedListener {

	public static final String KEY_MODEL_NAME = "model_name";
	public static final String KEY_COLUMN_NAME = "column_name";
	Context mContext = null;
	TypedArray mTypedArray = null;
	OControlAttributes mAttrs = new OControlAttributes();
	OModel mModel = null;

	/*
	 * Controls
	 */
	OColumn mColumn = null;
	Spinner mSpinner = null;
	LayoutParams mParams = null;
	OListAdapter mSpinnerAdapter = null;
	List<Object> mSpinnerObjects = new ArrayList<Object>();
	Integer mSelectedPosition = -1;
	Integer mCurrentId = -1;

	/*
	 * Listener
	 */
	ManyToOneItemChangeListener mManyToOneItemChangeListener = null;

	public OManyToOneWidget(Context context) {
		super(context);
		init(context, null, 0);
	}

	public OManyToOneWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public OManyToOneWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

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

	public void reInit() {
		initControls();
	}

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

	public OManyToOneWidget setModel(OModel model, String column) {
		mModel = model;
		mColumn = mModel.getColumn(column);
		return this;
	}

	public void setRecordId(Integer id) {
		mCurrentId = id;
	}

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

	public interface ManyToOneItemChangeListener {
		public void onManyToOneItemChangeListener(OColumn column, ODataRow row);
	}
}
