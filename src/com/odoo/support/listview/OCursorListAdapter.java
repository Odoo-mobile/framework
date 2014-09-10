package com.odoo.support.listview;

import java.util.HashMap;

import odoo.controls.OForm;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.odoo.orm.ODataRow;
import com.odoo.util.logger.OLog;

public class OCursorListAdapter extends CursorAdapter {

	private Integer mLayout = null;
	private LayoutInflater mInflater = null;
	private OnViewCreateListener mOnViewCreateListener = null;
	private HashMap<Integer, OnRowViewClickListener> mViewClickListeners = new HashMap<Integer, OnRowViewClickListener>();
	private HashMap<String, View> mViewCache = new HashMap<String, View>();
	private Boolean mCacheViews = false;
	private OnViewBindListener mOnViewBindListener = null;
	private BeforeBindUpdateData mBeforeBindUpdateData = null;
	private Context mContext = null;

	public OCursorListAdapter(Context context, Cursor c, int layout) {
		super(context, c, false);
		mLayout = layout;
		mInflater = LayoutInflater.from(context);
		mContext = context;
	}

	public OCursorListAdapter allowCacheView(Boolean cache) {
		mCacheViews = cache;
		return this;
	}

	public View getCachedView(Cursor cr) {
		int pos = cr.getPosition();
		if (mViewCache.size() > pos) {
			return mViewCache.get("view_" + pos);
		}
		return null;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final ODataRow row = new ODataRow();
		for (String col : cursor.getColumnNames()) {
			row.put(col, getValue(cursor, col));
		}
		if (mBeforeBindUpdateData != null) {
			row.addAll(mBeforeBindUpdateData.updateDataRow(cursor));
		}
		OForm form = (OForm) view;
		form.initForm(row);
		if (mOnViewBindListener != null) {
			mOnViewBindListener.onViewBind(view, cursor, row);
		}
	}

	@Override
	public View getView(final int position, View view, ViewGroup viewGroup) {
		getCursor().moveToPosition(position);
		Cursor cursor = getCursor();
		view = getCachedView(cursor);
		if (mCacheViews && view != null) {
			return view;
		}
		view = newView(mContext, cursor, (ViewGroup) view);
		final View mView = view;
		for (final int id : mViewClickListeners.keySet()) {
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					if (mView.findViewById(id) != null) {
						mView.findViewById(id).setOnClickListener(
								new View.OnClickListener() {

									@Override
									public void onClick(View v) {
										OnRowViewClickListener listener = mViewClickListeners
												.get(id);
										Cursor c = getCursor();
										c.moveToPosition(position);
										listener.onRowViewClick(position, c, v,
												mView);
									}
								});
					} else {
						OLog.log("View @id/"
								+ mContext.getResources().getResourceEntryName(
										id) + " not found");
					}
				}
			}, 100);
		}
		return super.getView(position, view, viewGroup);
	}

	private Object getValue(Cursor c, String column) {
		Object value = false;
		int index = c.getColumnIndex(column);
		switch (c.getType(index)) {
		case Cursor.FIELD_TYPE_NULL:
			value = false;
			break;
		case Cursor.FIELD_TYPE_BLOB:
		case Cursor.FIELD_TYPE_STRING:
			value = c.getString(index);
			break;
		case Cursor.FIELD_TYPE_FLOAT:
			value = c.getFloat(index);
			break;
		case Cursor.FIELD_TYPE_INTEGER:
			value = c.getInt(index);
			break;
		}
		return value;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
		View view = null;
		if (mCacheViews && getCachedView(cursor) != null) {
			view = getCachedView(cursor);
			if (!view.isDirty()) {
				return view;
			}
		}
		if (mOnViewCreateListener != null) {
			view = mOnViewCreateListener.onViewCreated(context, viewGroup,
					cursor, cursor.getPosition());
			if (view == null) {
				view = mInflater.inflate(mLayout, viewGroup, false);
			}
		} else
			view = mInflater.inflate(mLayout, viewGroup, false);
		if (mCacheViews) {
			mViewCache.put("view_" + cursor.getPosition(), view);
		}
		return view;
	}

	public View inflate(int resource, ViewGroup viewGroup) {
		return mInflater.inflate(resource, viewGroup, false);
	}

	public int getResource() {
		return mLayout;
	}

	public void setOnRowViewClickListener(int view_id,
			OnRowViewClickListener listener) {
		mViewClickListeners.put(view_id, listener);
	}

	public void setOnViewCreateListener(OnViewCreateListener viewCreateListener) {
		mOnViewCreateListener = viewCreateListener;
	}

	public void setOnViewBindListener(OnViewBindListener bindListener) {
		mOnViewBindListener = bindListener;
	}

	public void setBeforeBindUpdateData(BeforeBindUpdateData updater) {
		mBeforeBindUpdateData = updater;
	}

	public interface OnRowViewClickListener {
		public void onRowViewClick(int position, Cursor cursor, View view,
				View parent);
	}

	public interface OnViewBindListener {
		public void onViewBind(View view, Cursor cursor, ODataRow row);
	}

	public interface BeforeBindUpdateData {
		public ODataRow updateDataRow(Cursor cr);
	}

	public interface OnViewCreateListener {
		public View onViewCreated(Context context, ViewGroup view, Cursor cr,
				int position);
	}
}
