package odoo.controls;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.odoo.orm.OColumn;
import com.odoo.orm.ODataRow;
import com.odoo.support.listview.OListAdapter;
import com.odoo.util.logger.OLog;

public class OCollectionView extends ListView {

	private Context mContext = null;
	/** The list adapter. */
	private OListAdapter mListAdapter = null;

	/** The records. */
	private List<Object> mRecords = new ArrayList<Object>();

	private int mCustomLayout = -1;
	private Boolean mAdapterCreated = false;

	public OCollectionView(Context context) {
		this(context, null);
	}

	public OCollectionView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public OCollectionView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		setDivider(null);
		setDividerHeight(0);
		setItemsCanFocus(false);
		setChoiceMode(ListView.CHOICE_MODE_NONE);
		setSelector(android.R.color.transparent);
		setSmoothScrollbarEnabled(true);
	}

	public void initListControl(List<ODataRow> records, int customLayout) {
		mCustomLayout = customLayout;
		// if (mRecords.size() > 0 && mRecords.size() < records.size()) {
		// List<ODataRow> appendRecords = new ArrayList<ODataRow>();
		// if (records.size() > 0) {
		// appendRecords.addAll(records.subList(mRecords.size(),
		// records.size()));
		// }
		// // appendRecords(appendRecords);
		// } else {
		// if (mRecords.size() != records.size()) {
		// mRecords.clear();
		// mRecords.addAll(records);
		// createAdapter();
		// } else {
		// // mLoadNewRecords = false;
		// // removeDataLoaderProgress();
		// }
		// }
		if (!mAdapterCreated) {
			createAdapter();
			mAdapterCreated = true;
		}
		if (mRecords.size() <= 0) {
			// showEmptyListView();
		}
		OLog.log(mRecords.size() + "<<");
		mRecords.addAll(records);
		mListAdapter.notifiyDataChange(mRecords);
	}

	private void createAdapter() {
		mListAdapter = new OListAdapter(mContext, mCustomLayout, mRecords) {
			@Override
			public View getView(final int position, View convertView,
					ViewGroup parent) {
				View mView = (View) convertView;
				LayoutInflater inflater = LayoutInflater.from(mContext);
				final ODataRow record = (ODataRow) mRecords.get(position);
				if (mView == null) {
					mView = inflater.inflate(getResource(), parent, false);
					final OForm form = (OForm) mView;
					form.initForm(record);
					mView.setTag(record.getString(OColumn.ROW_ID));
				} else if (!mView.getTag().equals(
						record.getString(OColumn.ROW_ID))) {
					final OForm form = (OForm) mView;
					form.initForm(record);
				}
				// for (final ViewClickListeners listener : mViewClickListener)
				// {
				// for (final String key : listener.getKeys()) {
				// form.setOnViewClickListener(listener.getViewId(key),
				// new OForm.OnViewClickListener() {
				//
				// @Override
				// public void onFormViewClick(View view,
				// ODataRow row) {
				// listener.getListener(key)
				// .onRowViewClick(form, view,
				// position, record);
				// }
				// });
				// }
				// }
				// if (mBeforeListRowCreateListener != null) {
				// mBeforeListRowCreateListener.beforeListRowCreate(position,
				// record, mView);
				// }
				return mView;
			}
		};
		setAdapter(mListAdapter);
	}

}
