package odoo.controls;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import odoo.ODomain;

import org.json.JSONArray;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.odoo.R;
import com.odoo.orm.OColumn;
import com.odoo.orm.OColumn.ColumnDomain;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OFieldsHelper;
import com.odoo.orm.OModel;
import com.odoo.orm.OSyncHelper;
import com.odoo.support.listview.OListAdapter;
import com.odoo.util.OControls;

public class OSearchableMany2One extends LinearLayout implements
		View.OnClickListener, TextWatcher, OnItemClickListener {

	public static final String TAG = OSearchableMany2One.class.getSimpleName();
	public static final String KEY_DATA_MODEL = "dataModel";
	public static final String KEY_TITLE = "title";
	public static final String KEY_DISPLAY_LAYOUT = "displayLayout";
	public static final Integer ID_DEFAULT_VIEW = 0x123456;
	public static final Integer ID_DIALOG_PARENT_VIEW = 0x1234567;
	public static final Integer ID_DIALOG_SEARCH_BOX = 0x1234568;
	public static final Integer ID_DIALOG_LISTVIEW = 0x1234569;
	public static final Integer ID_DIALOG_LOADING = 0x1234570;
	public static final Integer ID_DIALOG_EMPTY_LIST = 0x1234571;
	private Context mContext;
	private OModel mModel;
	private TypedArray mTypedArray = null;
	private DisplayMetrics mMetrics = null;
	private OControlAttributes mAttr = new OControlAttributes();
	private Float mScaleFactor = 0F;
	private ODataRow mRecord = null;
	private AlertDialog.Builder mDialogBuilder = null;
	private AlertDialog mDialog = null;
	private EditText mSearchBox = null;
	private Boolean mVisibile = false;
	private ListView mListView;
	private List<Object> mObjects = new ArrayList<Object>();
	private OListAdapter mAdapter;
	private DialogListRowViewListener mDialogListRowViewListener = null;
	private RecordsLoader mRecordsLoader = null;
	private LinearLayout mParentView;
	private ODomain mDefaultDomain = new ODomain();
	private Boolean mEditMode = false;
	private LinkedHashMap<String, ColumnDomain> columnDomains;
	private OnChangeCallback mOnChangeCallback;
	private OnDomainFilterCallbacks mOnDomainFilterCallbacks;
	private ColumnDomain mColumnDomain;

	public OSearchableMany2One(Context context) {
		super(context);
		init(context, null, 0);
	}

	public OSearchableMany2One(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public OSearchableMany2One(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	private void init(Context context, AttributeSet attrs, int defStyle) {
		mContext = context;
		mMetrics = getResources().getDisplayMetrics();
		mScaleFactor = mMetrics.density;
		if (attrs != null) {
			mTypedArray = mContext.obtainStyledAttributes(attrs,
					R.styleable.OSearchableMany2One);
			mAttr.put(KEY_DATA_MODEL, mTypedArray
					.getString(R.styleable.OSearchableMany2One_dataModel));
			mAttr.put(KEY_TITLE, mTypedArray
					.getString(R.styleable.OSearchableMany2One_widget_title));
			mAttr.put(KEY_DISPLAY_LAYOUT, mTypedArray.getResourceId(
					R.styleable.OSearchableMany2One_displayLayout, -1));
			mTypedArray.recycle();
		}
		initControl();
	}

	public void setRecord(ODataRow record) {
		mRecord = record;
	}

	public void reInit(boolean editMode) {
		mEditMode = editMode;
		removeAllViews();
		initControl();
	}

	public void setDefaultDomain(LinkedHashMap<String, ColumnDomain> domains) {
		columnDomains = domains;
		ODomain domain = new ODomain();
		if (domain != null && domains.size() > 0) {
			for (String key : domains.keySet()) {
				ColumnDomain cDomain = domains.get(key);
				domain.add(cDomain.getColumn(), cDomain.getOperator(),
						cDomain.getValue());
			}
			mDefaultDomain.append(domain);
		}
	}

	public void setModel(OModel model) {
		mModel = model;
	}

	public OModel getModel() {
		if (mModel == null && mAttr.getString(KEY_DATA_MODEL, null) != null) {
			mModel = OModel
					.get(mContext, mAttr.getString(KEY_DATA_MODEL, null));
		}
		return mModel;
	}

	private void initDialogBuilder() {
		mDialogBuilder = new Builder(mContext);
		mDialogBuilder.setCancelable(false);
		mDialogBuilder.setView(getDialogView());
		mDialogBuilder.setNegativeButton(
				mContext.getString(R.string.label_cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						mVisibile = false;
					}
				});
	}

	private void initControl() {
		Log.v(TAG, "initControl()");
		setOrientation(LinearLayout.VERTICAL);
		if (hasDisplayLayout()) {
			View layout = LayoutInflater.from(mContext).inflate(
					mAttr.getResource(KEY_DISPLAY_LAYOUT, -1), this, false);
			if (mEditMode) {
				layout.setTag(ID_DEFAULT_VIEW);
				layout.setOnClickListener(this);
				layout.setBackgroundResource(R.drawable.drawer_item_selector);
			}
			if (mDialogListRowViewListener != null) {
				mDialogListRowViewListener.bindDisplayLayoutLoad(mRecord,
						layout);
			}
			addView(layout);
		} else {
			TextView layout = (TextView) getDefaltLayout();
			layout.setText(getTitle());
			if (mDialogListRowViewListener != null) {
				mDialogListRowViewListener.bindDisplayLayoutLoad(mRecord,
						layout);
			}
			addView(layout);
		}
	}

	public void setTitle(String title) {
		mAttr.put(KEY_TITLE, title);
	}

	public String getTitle() {
		return mAttr.getString(KEY_TITLE, null);
	}

	private View getDefaltLayout() {
		TextView txvDefault = new TextView(mContext);
		setPadd(10, txvDefault);
		txvDefault.setPadding((int) (5 * mScaleFactor),
				txvDefault.getPaddingTop(), txvDefault.getPaddingRight(),
				txvDefault.getPaddingBottom());
		txvDefault.setTextAppearance(mContext,
				android.R.attr.textAppearanceLarge);
		txvDefault.setTypeface(OControlHelper.boldFont());
		txvDefault.setTag(ID_DEFAULT_VIEW);
		if (mEditMode) {
			txvDefault.setOnClickListener(this);
			txvDefault.setBackgroundResource(R.drawable.drawer_item_selector);
		}
		return txvDefault;
	}

	public void setDisplayLayout(int res) {
		mAttr.put(KEY_DISPLAY_LAYOUT, res);
	}

	public boolean hasDisplayLayout() {
		return (mAttr.getResource(KEY_DISPLAY_LAYOUT, -1) != -1);
	}

	private void setPadd(int padd, View view) {
		int padding = (int) (mScaleFactor * padd);
		view.setPadding(padding, padding, padding, padding);
	}

	@Override
	public void onClick(View v) {
		if (v.getTag() == ID_DEFAULT_VIEW) {
			if (!mVisibile) {
				initDialogBuilder();
				mDialog = mDialogBuilder.create();
				mDialog.show();
				mVisibile = true;
			}
		}
	}

	private View getDialogView() {
		// Parent View
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		mParentView = new LinearLayout(mContext);
		mParentView.setTag(ID_DIALOG_PARENT_VIEW);
		mParentView.setOrientation(LinearLayout.VERTICAL);
		mParentView.setLayoutParams(params);
		int padd = (int) (mScaleFactor * 10);
		mParentView.setPadding(padd, padd, padd, padd);
		// EditText Search box
		initSearchBox();
		mParentView.addView(mSearchBox);
		// Separator
		mParentView.addView(getSeparator());

		// Preparing listview and data
		prepareListControl();
		mParentView.addView(mListView);
		return mParentView;
	}

	private View getSeparator() {
		LayoutParams param = new LayoutParams(LayoutParams.MATCH_PARENT, 2);
		View separator = new View(mContext);
		separator.setLayoutParams(param);
		separator.setBackgroundColor(Color.LTGRAY);
		return separator;
	}

	private void initSearchBox() {
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		mSearchBox = new EditText(mContext);
		mSearchBox.setSingleLine();
		mSearchBox.setLayoutParams(params);
		mSearchBox.setId(ID_DIALOG_SEARCH_BOX);
		mSearchBox.setTextAppearance(mContext,
				android.R.attr.textAppearanceMedium);
		mSearchBox.setTypeface(OControlHelper.lightFont());
		mSearchBox.setBackgroundColor(Color.TRANSPARENT);
		mSearchBox.setHint(getTitle());
		mSearchBox.addTextChangedListener(this);
	}

	private void prepareListControl() {
		AbsListView.LayoutParams params = new AbsListView.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		mListView = new ListView(mContext);
		mListView.setLayoutParams(params);
		mListView.setTag(ID_DIALOG_LISTVIEW);
		mListView.setOnItemClickListener(this);
		prepareAdapter();
	}

	private View dataLoadingView() {
		View v = LayoutInflater.from(mContext).inflate(
				R.layout.listview_data_loading_progress, this, false);
		v.setTag(ID_DIALOG_LOADING);
		return v;
	}

	private View emptyList() {
		TextView txvEmpty = new TextView(mContext);
		setPadd(30, txvEmpty);
		txvEmpty.setTextAppearance(mContext, android.R.attr.textAppearanceLarge);
		txvEmpty.setTypeface(OControlHelper.boldFont());
		txvEmpty.setTag(ID_DIALOG_EMPTY_LIST);
		txvEmpty.setText(mContext.getString(R.string.label_no_records_found));
		txvEmpty.setGravity(Gravity.CENTER);
		return txvEmpty;
	}

	private void prepareAdapter() {
		mObjects.clear();
		mAdapter = new OListAdapter(mContext,
				android.R.layout.simple_list_item_1, mObjects) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if (mDialogListRowViewListener != null) {
					View v = mDialogListRowViewListener.onDialogListRowGetView(
							(ODataRow) mObjects.get(position), position,
							convertView, parent);
					if (v != null)
						return v;
				}
				if (convertView == null) {
					convertView = LayoutInflater.from(mContext).inflate(
							android.R.layout.simple_list_item_1, parent, false);
				}
				ODataRow row = (ODataRow) mObjects.get(position);
				OControls.setText(
						convertView,
						android.R.id.text1,
						(row.contains("name")) ? row.getString("name") : row
								.toString());
				return convertView;
			}
		};
		mListView.setAdapter(mAdapter);

		// Getting default records
		if (mRecordsLoader != null)
			mRecordsLoader.cancel(true);
		mRecordsLoader = new RecordsLoader(mParentView, getModel());
		mRecordsLoader.execute();
	}

	public void setDialogListRowViewListener(DialogListRowViewListener listener) {
		mDialogListRowViewListener = listener;
	}

	private Object[] getWhere(ODomain domain) {
		StringBuffer whr = new StringBuffer();
		List<String> args = new ArrayList<String>();
		for (String key : columnDomains.keySet()) {
			ColumnDomain cDomain = columnDomains.get(key);
			if (cDomain.getConditionalOperator() != null) {
				whr.append(cDomain.getConditionalOperator());
			} else {
				whr.append(" ");
				whr.append(cDomain.getColumn());
				whr.append(" ");
				whr.append(cDomain.getOperator());
				whr.append(" ? ");
				args.add(cDomain.getValue().toString());
			}
		}
		JSONArray domains = domain.getArray();
		for (int i = 0; i < domains.length(); i++) {
			try {
				JSONArray dmn = domains.getJSONArray(i);
				if (whr.toString().trim().length() != 0) {
					whr.append(" AND ");
				}
				whr.append(" ");
				whr.append(dmn.getString(0));
				whr.append(" ");
				whr.append(validOperator(dmn.getString(1)));
				whr.append(" ? ");
				if (validOperator(dmn.getString(1)).contains("like"))
					args.add("%" + dmn.getString(2) + "%");
				else
					args.add(dmn.getString(2));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new Object[] { whr.toString(), args };
	}

	private String validOperator(String ope) {
		if (ope.equals("ilike") || ope.equals("=ilike")) {
			return " like ";
		}
		return " = ";
	}

	private class RecordsLoader extends AsyncTask<String, Void, List<Object>> {

		private OModel mModel;
		private OSyncHelper mOdoo;
		private LinearLayout mParentView;

		public RecordsLoader(LinearLayout parentView, OModel model) {
			mModel = model;
			mOdoo = (model != null) ? model.getSyncHelper() : null;
			mParentView = parentView;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			removeStatusView();
			mParentView.addView(dataLoadingView());
		}

		@Override
		protected List<Object> doInBackground(String... searchFor) {
			List<Object> items = new ArrayList<Object>();
			try {
				if (mOdoo != null) {
					ODomain domain = new ODomain();
					domain.append(mDefaultDomain);
					domain.add("id", "not in", mModel.ids());
					ODomain localDomain = new ODomain();
					if (searchFor.length > 0) {
						if (mDialogListRowViewListener != null) {
							ODomain filter = mDialogListRowViewListener
									.onDialogSearchChange(searchFor[0]);
							domain.append(filter);
							localDomain.append(filter);
						}
					}
					OFieldsHelper fields = new OFieldsHelper(mModel.fields());
					items.addAll(mOdoo.dataHelper().searchRecords(fields,
							domain, 20));
					Object[] params = getWhere(localDomain);
					@SuppressWarnings("unchecked")
					List<String> args = (List<String>) params[1];
					if (params[0].toString().length() > 0) {
						items.addAll(mModel.select(params[0].toString(),
								args.toArray(new String[args.size()])));
					} else {
						items.addAll(mModel.select());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return items;
		}

		@Override
		protected void onPostExecute(List<Object> result) {
			super.onPostExecute(result);
			removeStatusView();
			mObjects.clear();
			if (result.size() > 0) {
				mObjects.addAll(result);
			} else {
				mParentView.addView(emptyList());
			}
			mAdapter.notifiyDataChange(mObjects);
		}

		private void removeStatusView() {
			int loading_view = mParentView.indexOfChild(mParentView
					.findViewWithTag(ID_DIALOG_LOADING));
			if (loading_view > -1)
				mParentView.removeViewAt(loading_view);
			int empty_item = mParentView.indexOfChild(mParentView
					.findViewWithTag(ID_DIALOG_EMPTY_LIST));
			if (empty_item > -1)
				mParentView.removeViewAt(empty_item);
		}

	}

	public interface DialogListRowViewListener {
		public View onDialogListRowGetView(ODataRow data, int position,
				View view, ViewGroup parent);

		public ODomain onDialogSearchChange(String filter);

		public void bindDisplayLayoutLoad(ODataRow data, View layout);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (s.length() > 3) {
			if (mRecordsLoader != null) {
				mRecordsLoader.cancel(true);
			}
			mRecordsLoader = new RecordsLoader(mParentView, mModel);
			mRecordsLoader.execute(s.toString());
		}
		if (s.length() == 0) {
			if (mRecordsLoader != null) {
				mRecordsLoader.cancel(true);
			}
			mRecordsLoader = new RecordsLoader(mParentView, mModel);
			mRecordsLoader.execute();
		}
	}

	@Override
	public void afterTextChanged(Editable s) {

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mVisibile = false;
		mRecord = (ODataRow) mObjects.get(position);
		mDialog.dismiss();
		reInit(mEditMode);
		if (!mRecord.contains(OColumn.ROW_ID)) {
			CreatingRecord creating = new CreatingRecord(mRecord);
			creating.execute();
		} else {
			if (mOnChangeCallback != null) {
				mOnChangeCallback.onValueChange(mRecord);
			}
			if (mOnDomainFilterCallbacks != null) {
				mColumnDomain.setValue(mRecord.get(OColumn.ROW_ID));
				mOnDomainFilterCallbacks.onFieldValueChanged(mColumnDomain);
			}
		}
	}

	private class CreatingRecord extends AsyncTask<Void, Void, Void> {
		private ProgressDialog dialog;
		private ODataRow record;

		public CreatingRecord(ODataRow row) {
			record = row;
			dialog = new ProgressDialog(mContext);
			dialog.setMessage(mContext.getString(R.string.title_working));
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog.setCancelable(false);
			dialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(300);
			} catch (Exception e) {
				e.printStackTrace();
			}
			mModel.getSyncHelper().dataHelper().quickCreateLocalRecord(record);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			dialog.dismiss();
			mRecord.put(OColumn.ROW_ID,
					mModel.selectRowId(mRecord.getInt("id")));
			if (mOnChangeCallback != null) {
				mOnChangeCallback.onValueChange(mRecord);
			}
			if (mOnDomainFilterCallbacks != null) {
				mColumnDomain.setValue(mRecord.get(OColumn.ROW_ID));
				mOnDomainFilterCallbacks.onFieldValueChanged(mColumnDomain);
			}
		}
	}

	public ODataRow getValue() {
		return mRecord;
	}

	public void setOnChangeCallback(OnChangeCallback callback) {
		mOnChangeCallback = callback;
	}

	public void setOnFilterDomainCallBack(ColumnDomain domain,
			OnDomainFilterCallbacks callback) {
		mColumnDomain = domain;
		mOnDomainFilterCallbacks = callback;
	}
}
