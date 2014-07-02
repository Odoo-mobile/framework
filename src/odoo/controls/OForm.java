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

public class OForm extends LinearLayout {

	public static final String KEY_BACKGROUND_SELECTOR = "background_selector";
	public static final String KEY_MODEL = "model";

	Context mContext = null;
	TypedArray mTypedArray = null;
	ODataRow mRecord = null;
	OControlAttributes mAttrs = new OControlAttributes();
	OModel mModel = null;
	List<String> mFields = new ArrayList<String>();
	HashMap<String, OColumn> mFieldColumns = new HashMap<String, OColumn>();

	public OForm(Context context) {
		super(context);
		init(context, null, 0);
	}

	public OForm(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public OForm(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

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

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
	}

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

	public void setModel(OModel model) {
		mModel = model;
	}

	public void initForm(ODataRow record) {
		initForm(record, false);
	}

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

	public void initForm(ODataRow record, boolean editable) {
		mRecord = record;
		_initForm(editable);
	}

	public void setEditable(Boolean mEditMode) {
		_initForm(mEditMode);
	}

}
