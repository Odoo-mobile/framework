package odoo.controls;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.odoo.R;
import com.odoo.orm.ODataRow;
import com.odoo.support.listview.OListAdapter;

public class OList extends ScrollView {

	Context mContext = null;
	TypedArray mTypedArray = null;
	OListAdapter mListAdapter = null;
	List<Object> mRecords = new ArrayList<Object>();

	/*
	 * required controls
	 */
	Integer mCustomLayout = 0;
	LinearLayout mInnerLayout = null;
	LayoutParams mLayoutParams = null;

	public OList(Context context) {
		super(context);
		init(context, null, 0);
	}

	public OList(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public OList(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	private void init(Context context, AttributeSet attrs, int defStyle) {
		mContext = context;
	}

	protected void onFinishInflate() {
		super.onFinishInflate();
		removeAllViews();
		createListInnerControl();
	}

	private void createListInnerControl() {
		Log.v("OList", "OList ready for add child views");
		mInnerLayout = parentView();
	}

	public void initListControl(List<ODataRow> records, int custom_layout) {
		mRecords.clear();
		mRecords.addAll(records);
		mCustomLayout = custom_layout;
		Log.v("OList", "Got " + mRecords.size() + " records");
		createAdapter();
	}

	private void createAdapter() {
		mListAdapter = new OListAdapter(mContext, mCustomLayout, mRecords) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View mView = (View) convertView;
				LayoutInflater inflater = LayoutInflater.from(mContext);
				if (mView == null) {
					mView = inflater.inflate(getResource(), parent, false);
				}
				ODataRow record = (ODataRow) mRecords.get(position);
				((OForm) mView).initForm(record);
				return mView;
			}
		};
		addRecordViews();
	}

	private void addRecordViews() {
		removeAllViews();
		mInnerLayout.removeAllViews();
		for (int i = 0; i < mListAdapter.getCount(); i++) {
			OForm view = (OForm) mListAdapter.getView(i, null, null);
			view.setTag(i);
			mInnerLayout.addView(view);
			mInnerLayout.addView(divider());
		}
		addView(mInnerLayout);
	}

	private LinearLayout parentView() {
		LinearLayout mLayout = new LinearLayout(mContext);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		mLayout.setLayoutParams(params);
		mLayout.setOrientation(LinearLayout.VERTICAL);
		return mLayout;
	}

	private View divider() {
		View v = new View(mContext);
		v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 1));
		v.setBackgroundColor(mContext.getResources().getColor(
				R.color.list_divider));
		return v;
	}
}
