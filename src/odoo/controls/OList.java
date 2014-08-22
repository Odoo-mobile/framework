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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.odoo.R;
import com.odoo.orm.ODataRow;
import com.odoo.support.listview.OListAdapter;
import com.odoo.support.listview.OListAdapter.OnSearchChange;

/**
 * The Class OList.
 */
@SuppressLint("ClickableViewAccessibility")
public class OList extends ScrollView implements View.OnClickListener,
		View.OnLongClickListener, View.OnTouchListener, View.OnDragListener,
		OnSearchChange {

	/** The Constant KEY_DATA_LOADER. */
	private static final String KEY_DATA_LOADER = "data_loader_status";
	/** The Constant KEY_CUSTOM_LAYOUT. */
	public static final String KEY_CUSTOM_LAYOUT = "custome_layout";

	/** The Constant KEY_SHOW_DIVIDER. */
	public static final String KEY_SHOW_DIVIDER = "showDivider";

	/** The Constant KEY_EMPTY_LIST_MESSAGE. */
	public static final String KEY_EMPTY_LIST_MESSAGE = "emptyListMessage";

	/** The Constant KEY_EMPTY_LIST_ICON. */
	public static final String KEY_EMPTY_LIST_ICON = "emptyListIcon";

	/** The Constant KEY_SHOW_AS_CARD. */
	public static final String KEY_SHOW_AS_CARD = "showAsCard";

	/** The context. */
	private Context mContext = null;

	/** The typed array. */
	private TypedArray mTypedArray = null;

	/** The list adapter. */
	private OListAdapter mListAdapter = null;

	/** The records. */
	private List<Object> mRecords = new ArrayList<Object>();

	/** The attr. */
	private OControlAttributes mAttr = new OControlAttributes();

	/** The custom layout. */
	private Integer mCustomLayout = 0;

	/** The inner layout. */
	private LinearLayout mInnerLayout = null;

	/** The layout params. */
	private LayoutParams mLayoutParams = null;

	/** The on row click listener. */
	private OnRowClickListener mOnRowClickListener = null;

	/** The row draggable. */
	private Boolean mRowDraggable = false;

	/** The drag mode. */
	private boolean mDragMode = false;

	/** The drag drop listener. */
	private OListDragDropListener mDragDropListener = null;

	/** The shadow builder. */
	private View.DragShadowBuilder mShadowBuilder = null;

	/** The drop layouts. */
	private List<Integer> mDropLayouts = new ArrayList<Integer>();

	/** The dropped. */
	private Boolean mDropped = false;

	/** The row droppable. */
	private Boolean mRowDroppable = false;

	/** The draggable view. */
	private View mDraggableView = null;

	/** The drag started. */
	private Boolean mDragStarted = false;

	/** The drag ended. */
	private Boolean mDragEnded = false;

	/** The m view click listener. */
	private List<ViewClickListeners> mViewClickListener = new ArrayList<ViewClickListeners>();

	/** The m before list row create listener. */
	private BeforeListRowCreateListener mBeforeListRowCreateListener = null;

	/** The on list bottom reached listener. */
	private OnListBottomReachedListener mOnListBottomReachedListener = null;

	/** The record limit. */
	private Integer mRecordLimit = -1;

	/** The record offset. */
	private Integer mRecordOffset = 0;
	/** The display metrics. */
	private DisplayMetrics mMetrics = null;

	/** The scale factor. */
	private Float mScaleFactor = 0F;

	/** The m load new records. */
	private Boolean mLoadNewRecords = true;

	/** The adapter created. */
	private Boolean mAdapterCreated = false;

	/**
	 * Instantiates a new list control.
	 * 
	 * @param context
	 *            the context
	 */
	public OList(Context context) {
		super(context);
		init(context, null, 0);
	}

	/**
	 * Instantiates a new list control.
	 * 
	 * @param context
	 *            the context
	 * @param attrs
	 *            the attrs
	 */
	public OList(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	/**
	 * Instantiates a new list control.
	 * 
	 * @param context
	 *            the context
	 * @param attrs
	 *            the attrs
	 * @param defStyle
	 *            the def style
	 */
	public OList(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	/**
	 * Inits the list control.
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
		mMetrics = getResources().getDisplayMetrics();
		mScaleFactor = mMetrics.density;
		if (attrs != null) {
			mTypedArray = mContext.obtainStyledAttributes(attrs,
					R.styleable.OList);
			mAttr.put(KEY_CUSTOM_LAYOUT, mTypedArray.getResourceId(
					R.styleable.OList_custom_layout, 0));
			mAttr.put(KEY_SHOW_DIVIDER,
					mTypedArray.getBoolean(R.styleable.OList_showDivider, true));
			mAttr.put(KEY_EMPTY_LIST_MESSAGE,
					mTypedArray.getString(R.styleable.OList_emptyListMessage));
			mAttr.put(KEY_EMPTY_LIST_ICON, mTypedArray.getResourceId(
					R.styleable.OList_emptyListIcon,
					R.drawable.ic_action_exclamation_mark));
			mAttr.put(KEY_SHOW_AS_CARD,
					mTypedArray.getBoolean(R.styleable.OList_showAsCard, false));
			mCustomLayout = mAttr.getResource(KEY_CUSTOM_LAYOUT, 0);
			mTypedArray.recycle();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onFinishInflate()
	 */
	protected void onFinishInflate() {
		super.onFinishInflate();
		removeAllViews();
		createListInnerControl();
	}

	/**
	 * Creates the list inner control.
	 */
	private void createListInnerControl() {
		mInnerLayout = parentView();
	}

	/**
	 * Inits the list control.
	 * 
	 * @param records
	 *            the records
	 */
	public void initListControl(List<ODataRow> records) {
		if (mRecords.size() > 0 && mRecords.size() < records.size()) {
			List<ODataRow> appendRecords = new ArrayList<ODataRow>();
			if (records.size() > 0) {
				appendRecords.addAll(records.subList(mRecords.size(),
						records.size()));
			}
			appendRecords(appendRecords);
		} else {
			if (mRecords.size() != records.size()) {
				mRecords.clear();
				mRecords.addAll(records);
				createAdapter();
			} else {
				mLoadNewRecords = false;
				removeDataLoaderProgress();
			}
		}
		if (!mAdapterCreated) {
			createAdapter();
		}
		if (mRecords.size() <= 0) {
			showEmptyListView();
		}
	}

	private void removeDataLoaderProgress() {
		if (findViewWithTag(KEY_DATA_LOADER) != null) {
			int index = mInnerLayout
					.indexOfChild(findViewWithTag(KEY_DATA_LOADER));
			mInnerLayout.removeViewAt(index);
		}
	}

	/**
	 * Append records.
	 * 
	 * @param newRecords
	 *            the new records
	 */
	public void appendRecords(List<ODataRow> newRecords) {
		if (newRecords.size() > 0) {
			int lastPosition = mRecords.size();
			mRecords.addAll(lastPosition, newRecords);
			mListAdapter.notifiyDataChange(mRecords);
			addRecordViews(lastPosition, -1);
		} else {
			mLoadNewRecords = false;
			removeDataLoaderProgress();
		}
	}

	/**
	 * Append records at position.
	 * 
	 * @param index
	 *            the index
	 * @param newRecords
	 *            the new records
	 */
	public void appendRecords(Integer index, List<ODataRow> newRecords) {
		if (newRecords.size() > 0) {
			mRecords.addAll(index, newRecords);
			mListAdapter.notifiyDataChange(mRecords);
			int end_index = newRecords.size() + 1;
			addRecordViews(index, end_index);
		} else {
			mLoadNewRecords = false;
			removeDataLoaderProgress();
		}
	}

	/**
	 * Creates the adapter.
	 */
	@SuppressLint("NewApi")
	private void createAdapter() {
		mListAdapter = new OListAdapter(mContext, mCustomLayout, mRecords) {
			@Override
			public View getView(final int position, View convertView,
					ViewGroup parent) {
				View mView = (View) convertView;
				LayoutInflater inflater = LayoutInflater.from(mContext);
				if (mView == null) {
					mView = inflater.inflate(getResource(), parent, false);
				}
				final ODataRow record = (ODataRow) mRecords.get(position);
				final OForm form = (OForm) mView;
				for (final ViewClickListeners listener : mViewClickListener) {
					for (final String key : listener.getKeys()) {
						OForm.OnViewClickListener itemClick = new OForm.OnViewClickListener() {

							@Override
							public void onFormViewClick(View view, ODataRow row) {
								listener.getListener(key).onRowViewClick(form,
										view, position, row);
							}
						};
						if (form.findViewById(listener.getViewId(key)) instanceof OField) {
							OField field = (OField) form.findViewById(listener
									.getViewId(key));
							field.setOnItemClickListener(itemClick);
						} else {
							form.setOnViewClickListener(
									listener.getViewId(key), itemClick);
						}
					}
				}
				form.initForm(record);
				if (mBeforeListRowCreateListener != null) {
					mBeforeListRowCreateListener.beforeListRowCreate(position,
							record, mView);
				}
				return mView;
			}
		};
		mListAdapter.setOnSearchChange(this);
		mAdapterCreated = true;
		addRecordViews(0, -1);
	}

	/**
	 * Sets the empty list message.
	 * 
	 * @param message
	 *            the new empty list message
	 */
	public void setEmptyListMessage(String message) {
		mAttr.put(KEY_EMPTY_LIST_MESSAGE, message);
	}

	/**
	 * Sets the empty list icon.
	 * 
	 * @param icon
	 *            the new empty list icon
	 */
	public void setEmptyListIcon(Integer icon) {
		mAttr.put(KEY_EMPTY_LIST_ICON, icon);
	}

	/**
	 * Show empty list view.
	 */
	private void showEmptyListView() {
		mInnerLayout.removeAllViews();
		LinearLayout mEmptyListLayout = new LinearLayout(mContext);
		mEmptyListLayout.setOrientation(LinearLayout.VERTICAL);
		Integer padding = (int) (20 * mScaleFactor);
		mLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		mEmptyListLayout.setLayoutParams(mLayoutParams);
		mEmptyListLayout.setPadding(padding, padding * 4, padding, padding);
		mEmptyListLayout.setGravity(Gravity.CENTER);
		// Adding empty list icon
		ImageView imgIcon = new ImageView(mContext);
		int height = (int) (96 * mScaleFactor);
		mLayoutParams = new LayoutParams(height, height);
		imgIcon.setLayoutParams(mLayoutParams);
		imgIcon.setImageResource(mAttr.getResource(KEY_EMPTY_LIST_ICON,
				R.drawable.ic_action_exclamation_mark));
		imgIcon.setColorFilter(mContext.getResources().getColor(
				R.color.gray_light));
		mEmptyListLayout.addView(imgIcon);

		// Adding empty message
		TextView txvEmptyMessage = new TextView(mContext);
		mLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		txvEmptyMessage.setLayoutParams(mLayoutParams);
		txvEmptyMessage.setTextAppearance(mContext,
				android.R.style.TextAppearance_Large);
		txvEmptyMessage.setTypeface(OControlHelper.boldFont());
		txvEmptyMessage.setGravity(Gravity.CENTER);
		txvEmptyMessage.setTextColor(mContext.getResources().getColor(
				R.color.gray_light));
		String empty_message = mAttr.getString(KEY_EMPTY_LIST_MESSAGE,
				"No records found");
		txvEmptyMessage.setText(empty_message);
		mEmptyListLayout.addView(txvEmptyMessage);
		mInnerLayout.addView(mEmptyListLayout);
	}

	/**
	 * Adds the record views.
	 */
	private void addRecordViews(Integer start, Integer end) {
		if (start == 0 && end == -1) {
			removeAllViews();
			mInnerLayout.removeAllViews();
			addView(mInnerLayout);
		} else {
			mInnerLayout = (LinearLayout) findViewWithTag("list_parent_view");
			removeDataLoaderProgress();
		}
		if (end == -1) {
			end = mListAdapter.getCount();
		}
		int listLen = mListAdapter.getCount();
		for (int i = start; i < end; i++) {
			OForm view = (OForm) mListAdapter.getView(i, null, null);
			view.setTag(i);
			if (mOnRowClickListener != null) {
				view.setOnClickListener(this);
			}
			if (mRowDraggable) {
				view.setOnLongClickListener(this);
				view.setOnTouchListener(this);
				view.setOnDragListener(this);
			}
			if (mRowDroppable) {
				view.setOnDragListener(this);
			}
			if (mAttr.getBoolean(KEY_SHOW_AS_CARD, false)) {
				ViewGroup card = cardOuterView(i);
				card.addView(view);
				if (end == listLen)
					mInnerLayout.addView(card);
				else
					mInnerLayout.addView(card, i);
				mInnerLayout
						.setBackgroundResource(R.color.card_view_parent_background);
				setBackgroundResource(R.color.card_view_parent_background);
			} else {
				mInnerLayout.addView(view);
				if (mAttr.getBoolean(KEY_SHOW_DIVIDER, true)) {
					if (end == listLen)
						mInnerLayout.addView(divider());
					else
						mInnerLayout.addView(divider(), i);
				}
			}
		}
	}

	/**
	 * Show as card.
	 * 
	 * @param showAsCard
	 *            the show as card
	 */
	public void showAsCard(boolean showAsCard) {
		mAttr.put(KEY_SHOW_AS_CARD, showAsCard);
	}

	/**
	 * Card outer view.
	 * 
	 * @return the view
	 */
	private LinearLayout cardOuterView(int position) {
		LinearLayout cardView = new LinearLayout(mContext);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		int left_right_margin = (int) (10 * mScaleFactor);
		int top_margin = (int) (6 * mScaleFactor);
		if (position == mListAdapter.getCount() - 1) {
			params.setMargins(left_right_margin, top_margin, left_right_margin,
					left_right_margin);
		} else {
			params.setMargins(left_right_margin, top_margin, left_right_margin,
					0);
		}
		cardView.setLayoutParams(params);
		int padding = (int) (2 * mScaleFactor);
		cardView.setPadding(padding, padding, padding, padding);
		cardView.setOrientation(LinearLayout.VERTICAL);
		cardView.setBackgroundResource(R.drawable.card);
		return cardView;
	}

	/**
	 * Parent view.
	 * 
	 * @return the linear layout
	 */
	private LinearLayout parentView() {
		LinearLayout mLayout = new LinearLayout(mContext);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		mLayout.setLayoutParams(params);
		mLayout.setOrientation(LinearLayout.VERTICAL);
		mLayout.setTag("list_parent_view");
		return mLayout;
	}

	/**
	 * Divider.
	 * 
	 * @return the view
	 */
	private View divider() {
		View v = new View(mContext);
		v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 1));
		v.setBackgroundColor(mContext.getResources().getColor(
				R.color.list_divider));
		return v;
	}

	/**
	 * Sets the custom view.
	 * 
	 * @param view_resource
	 *            the new custom view
	 */
	public void setCustomView(int view_resource) {
		mAttr.put(KEY_CUSTOM_LAYOUT, view_resource);
		mCustomLayout = view_resource;
	}

	/**
	 * Sets the on row click listener.
	 * 
	 * @param listener
	 *            the new on row click listener
	 */
	public void setOnRowClickListener(OnRowClickListener listener) {
		mOnRowClickListener = listener;
	}

	/**
	 * The listener interface for receiving onRowClick events. The class that is
	 * interested in processing a onRowClick event implements this interface,
	 * and the object created with that class is registered with a component
	 * using the component's <code>addOnRowClickListener<code> method. When
	 * the onRowClick event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnRowClickEvent
	 */
	public interface OnRowClickListener {

		/**
		 * On row item click.
		 * 
		 * @param position
		 *            the position
		 * @param view
		 *            the view
		 * @param row
		 *            the row
		 */
		public void onRowItemClick(int position, View view, ODataRow row);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		if (!mDragMode) {
			int pos = (Integer) v.getTag();
			mOnRowClickListener.onRowItemClick(pos, v,
					(ODataRow) mRecords.get(pos));
		}
	}

	/**
	 * Sets the drag drop listener.
	 * 
	 * @param listener
	 *            the new drag drop listener
	 */
	public void setDragDropListener(OListDragDropListener listener) {
		mDragDropListener = listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnLongClickListener#onLongClick(android.view.View)
	 */
	@Override
	public boolean onLongClick(View v) {
		mDragMode = true;
		mDragStarted = false;
		mDragEnded = false;
		return true;
	}

	/**
	 * The Class DragShadowBuilder.
	 */
	private static class DragShadowBuilder extends View.DragShadowBuilder {

		/** The shadow. */
		private static Drawable shadow;

		/** The height. */
		int width, height;

		/**
		 * Instantiates a new drag shadow builder.
		 * 
		 * @param v
		 *            the v
		 */
		public DragShadowBuilder(View v) {
			super(v);
			shadow = new ColorDrawable(Color.LTGRAY);
			width = getView().getWidth() / 2;
			height = getView().getHeight() / 2;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.view.View.DragShadowBuilder#onProvideShadowMetrics(android
		 * .graphics.Point, android.graphics.Point)
		 */
		@Override
		public void onProvideShadowMetrics(Point size, Point touch) {
			shadow.setBounds(0, 0, width, height);
			size.set(width, height);
			touch.set(width / 2, height / 2);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.view.View.DragShadowBuilder#onDrawShadow(android.graphics
		 * .Canvas)
		 */
		@Override
		public void onDrawShadow(Canvas canvas) {
			shadow.draw(canvas);
		}

	}

	/**
	 * Sets the row draggable.
	 * 
	 * @param draggable
	 *            the new row draggable
	 */
	public void setRowDraggable(boolean draggable) {
		mRowDraggable = draggable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View,
	 * android.view.MotionEvent)
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (mDragMode && event.getAction() == MotionEvent.ACTION_MOVE) {
			mShadowBuilder = new DragShadowBuilder(v);
			v.startDrag(null, mShadowBuilder, v, 0);
			v.setVisibility(View.INVISIBLE);
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnDragListener#onDrag(android.view.View,
	 * android.view.DragEvent)
	 */
	@Override
	public boolean onDrag(final View v, DragEvent event) {
		int action = event.getAction();
		final View view = (View) event.getLocalState();
		ViewGroup parent = (ViewGroup) view.getParent();
		ViewGroup newParent = (ViewGroup) v;
		final int position = (Integer) view.getTag();
		switch (action) {
		case DragEvent.ACTION_DRAG_STARTED:
			if (mRowDraggable && mDragDropListener != null)
				onDragStart(view, position, mRecords.get(position));
			break;
		case DragEvent.ACTION_DRAG_ENTERED:
			if (mRowDroppable || isDroppable(v))
				v.setBackgroundColor(Color.GRAY);
			break;
		case DragEvent.ACTION_DRAG_EXITED:
			if (mRowDroppable || isDroppable(v))
				v.setBackgroundColor(Color.WHITE);
			break;
		case DragEvent.ACTION_DROP:
			if (mRowDroppable || isDroppable(v)) {
				parent.removeView(view);
				view.setVisibility(View.VISIBLE);
				view.setOnTouchListener(null);
				newParent.setBackgroundColor(Color.WHITE);
				if (getDraggableView() instanceof OList) {
					int drop_position = (Integer) newParent.getTag();
					OList draggableView = (OList) getDraggableView();
					draggableView.setDroppedObjectData(view, position,
							mRecords.get(drop_position));
				}
				if (getDraggableView() == null && isDroppable(v)) {
					// Adding view to layout
					newParent.addView(view);
				}
			}
			mDragMode = false;
			mDropped = true;
			break;
		case DragEvent.ACTION_DRAG_ENDED:
			if (!mDropped) {
				view.post(new Runnable() {
					@Override
					public void run() {
						view.setVisibility(View.VISIBLE);
					}
				});
			}
			if (mDragDropListener != null) {
				onDragEnnd(view, position, mRecords.get(position));
			}
			mDragMode = false;
			mDropped = false;
			break;
		}
		return true;
	}

	/**
	 * Sets the dropped object data.
	 * 
	 * @param view
	 *            the view
	 * @param position
	 *            the position
	 * @param object
	 *            the object
	 */
	public void setDroppedObjectData(View view, int position, Object object) {
		if (mDragDropListener != null) {
			mDragDropListener.onItemDrop(view, mRecords.get(position), object);
		}
	}

	/**
	 * On drag start.
	 * 
	 * @param view
	 *            the view
	 * @param position
	 *            the position
	 * @param object
	 *            the object
	 */
	private void onDragStart(View view, int position, Object object) {
		if (!mDragStarted) {
			mDragDropListener.onItemDragStart(view, position, object);
			mDragStarted = true;
		}
	}

	/**
	 * On drag ennd.
	 * 
	 * @param view
	 *            the view
	 * @param position
	 *            the position
	 * @param object
	 *            the object
	 */
	private void onDragEnnd(View view, int position, Object object) {
		if (!mDragEnded) {
			mDragDropListener.onItemDragEnd(view, position,
					mRecords.get(position));
			mDragEnded = true;
		}
	}

	/**
	 * Sets the row droppable.
	 * 
	 * @param droppable
	 *            the droppable
	 * @param draggableView
	 *            the draggable view
	 */
	public void setRowDroppable(boolean droppable, View draggableView) {
		mRowDroppable = droppable;
		mDraggableView = draggableView;
	}

	/**
	 * Adds the drop listener layout.
	 * 
	 * @param resource
	 *            the resource
	 */
	public void addDropListenerLayout(int resource) {
		mDropLayouts.add(resource);
		ViewGroup view = (ViewGroup) getParent();
		View droppable_view = view.findViewById(resource);
		droppable_view.setTag("droppable_view");
		droppable_view.setOnDragListener(this);
	}

	/**
	 * Checks if is droppable.
	 * 
	 * @param v
	 *            the v
	 * @return true, if is droppable
	 */
	public boolean isDroppable(View v) {
		if (v.getTag() != null
				&& v.getTag().toString().equals("droppable_view"))
			return true;
		return false;
	}

	/**
	 * Gets the draggable view.
	 * 
	 * @return the draggable view
	 */
	public View getDraggableView() {
		return mDraggableView;
	}

	/**
	 * Sets the on list row view click listener.
	 * 
	 * @param view_id
	 *            the view_id
	 * @param listener
	 *            the listener
	 */
	public void setOnListRowViewClickListener(Integer view_id,
			OnListRowViewClickListener listener) {
		mViewClickListener.add(new ViewClickListeners(view_id, listener));
	}

	/**
	 * Sets the before list row create listener.
	 * 
	 * @param callback
	 *            the new before list row create listener
	 */
	public void setBeforeListRowCreateListener(
			BeforeListRowCreateListener callback) {
		mBeforeListRowCreateListener = callback;
	}

	/**
	 * Sets the on list bottom reached listener.
	 * 
	 * @param listener
	 *            the new on list bottom reached listener
	 */
	public void setOnListBottomReachedListener(
			OnListBottomReachedListener listener) {
		mOnListBottomReachedListener = listener;
	}

	/**
	 * Sets the record offset.
	 * 
	 * @param offset
	 *            the offset
	 * @return the o list
	 */
	public OList setRecordOffset(Integer offset) {
		mRecordOffset = offset;
		return this;
	}

	/**
	 * Sets the record limit.
	 * 
	 * @param limit
	 *            the limit
	 * @return the o list
	 */
	public OList setRecordLimit(Integer limit) {
		mRecordLimit = limit;
		return this;
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if (mLoadNewRecords && getViewVisiblityDiff() == 0
				&& mOnListBottomReachedListener != null) {
			if (mOnListBottomReachedListener.showLoader()) {
				LinearLayout loaderLayout = new LinearLayout(mContext);
				mLayoutParams = new LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				// Adding loader progress
				ProgressBar mProgress = new ProgressBar(mContext);
				loaderLayout.setGravity(Gravity.CENTER);
				loaderLayout.addView(mProgress);
				loaderLayout.setPadding(15, 15, 15, 15);
				loaderLayout.setTag(KEY_DATA_LOADER);
				mInnerLayout.addView(loaderLayout);
				post(new Runnable() {

					@Override
					public void run() {
						fullScroll(ScrollView.FOCUS_DOWN);
					}
				});
			}
			mOnListBottomReachedListener.onBottomReached(mRecordLimit,
					mRecordOffset);
		}
	}

	/**
	 * Gets the view visiblity diff.
	 * 
	 * @return the view visiblity diff
	 */
	private Integer getViewVisiblityDiff() {
		View view = (View) getChildAt(getChildCount() - 1);
		if (view.findViewWithTag(KEY_DATA_LOADER) != null)
			return -1;
		return (view.getBottom() - (getHeight() + getScrollY()));
	}

	/**
	 * Gets the query listener.
	 * 
	 * @return the query listener
	 */
	public OnQueryTextListener getQueryListener() {
		return mQueryListener;
	}

	@Override
	public void onSearchChange(List<Object> newRecords) {
		if (findViewWithTag(KEY_DATA_LOADER) != null) {
			removeView(findViewWithTag(KEY_DATA_LOADER));
		}
		mRecords.clear();
		mRecords.addAll(newRecords);
		addRecordViews(0, -1);
		if (mRecords.size() <= 0) {
			showEmptyListView();
		}
	}

	/** The query listener. */
	private OnQueryTextListener mQueryListener = new OnQueryTextListener() {

		private boolean isSearched = false;

		@Override
		public boolean onQueryTextChange(String newText) {
			if (TextUtils.isEmpty(newText)) {
				newText = "";
				if (isSearched && mListAdapter != null) {
					mListAdapter.getFilter().filter(null);
				}
			} else {
				isSearched = true;
				mListAdapter.getFilter().filter(newText);
			}

			return false;
		}

		@Override
		public boolean onQueryTextSubmit(String query) {
			return false;
		}
	};

	/**
	 * The listener interface for receiving onListRowViewClick events. The class
	 * that is interested in processing a onListRowViewClick event implements
	 * this interface, and the object created with that class is registered with
	 * a component using the component's
	 * <code>addOnListRowViewClickListener<code> method. When
	 * the onListRowViewClick event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnListRowViewClickEvent
	 */
	public interface OnListRowViewClickListener {

		/**
		 * On row view click.
		 * 
		 * @param view_group
		 *            the view_group
		 * @param view
		 *            the view
		 * @param position
		 *            the position
		 * @param row
		 *            the row
		 */
		public void onRowViewClick(ViewGroup view_group, View view,
				int position, ODataRow row);
	}

	/**
	 * The Class ViewClickListeners.
	 */
	private class ViewClickListeners {

		/** The _listener_data. */
		private HashMap<String, OnListRowViewClickListener> _listener_data = new HashMap<String, OnListRowViewClickListener>();

		/** The _listener_view. */
		private HashMap<String, Integer> _listener_view = new HashMap<String, Integer>();

		/**
		 * Instantiates a new view click listeners.
		 * 
		 * @param view_id
		 *            the view_id
		 * @param listener
		 *            the listener
		 */
		public ViewClickListeners(Integer view_id,
				OnListRowViewClickListener listener) {
			String key = "KEY_" + view_id;
			_listener_data.put(key, listener);
			_listener_view.put(key, view_id);
		}

		/**
		 * Gets the listener.
		 * 
		 * @param key
		 *            the key
		 * @return the listener
		 */
		public OnListRowViewClickListener getListener(String key) {
			return _listener_data.get(key);
		}

		/**
		 * Gets the view id.
		 * 
		 * @param key
		 *            the key
		 * @return the view id
		 */
		public Integer getViewId(String key) {
			return _listener_view.get(key);
		}

		/**
		 * Gets the keys.
		 * 
		 * @return the keys
		 */
		public List<String> getKeys() {
			List<String> keys = new ArrayList<String>();
			keys.addAll(_listener_view.keySet());
			return keys;
		}
	}

	/**
	 * The listener interface for receiving beforeListRowCreate events. The
	 * class that is interested in processing a beforeListRowCreate event
	 * implements this interface, and the object created with that class is
	 * registered with a component using the component's
	 * <code>addBeforeListRowCreateListener<code> method. When
	 * the beforeListRowCreate event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see BeforeListRowCreateEvent
	 */
	public interface BeforeListRowCreateListener {

		/**
		 * Before list row create.
		 * 
		 * @param position
		 *            the position
		 * @param view
		 *            the view
		 */
		public void beforeListRowCreate(int position, ODataRow row, View view);
	}

	/**
	 * The listener interface for receiving onListBottomReached events. The
	 * class that is interested in processing a onListBottomReached event
	 * implements this interface, and the object created with that class is
	 * registered with a component using the component's
	 * <code>addOnListBottomReachedListener<code> method. When
	 * the onListBottomReached event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnListBottomReachedEvent
	 */
	public interface OnListBottomReachedListener {

		/**
		 * On bottom reached.
		 * 
		 * @param record_limit
		 *            the record_limit
		 * @param record_offset
		 *            the record_offset
		 */
		public void onBottomReached(Integer record_limit, Integer record_offset);

		/**
		 * Show loader.
		 * 
		 * @return the boolean
		 */
		public Boolean showLoader();
	}

}
