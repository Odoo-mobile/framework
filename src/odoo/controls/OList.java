package odoo.controls;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.odoo.R;
import com.odoo.orm.ODataRow;
import com.odoo.support.listview.OListAdapter;

@SuppressLint("ClickableViewAccessibility")
public class OList extends ScrollView implements View.OnClickListener,
		View.OnLongClickListener, View.OnTouchListener, View.OnDragListener {

	public static final String KEY_CUSTOM_LAYOUT = "custome_layout";
	Context mContext = null;
	TypedArray mTypedArray = null;
	OListAdapter mListAdapter = null;
	List<Object> mRecords = new ArrayList<Object>();
	OControlAttributes mAttr = new OControlAttributes();
	/*
	 * required controls
	 */
	Integer mCustomLayout = 0;
	LinearLayout mInnerLayout = null;
	LayoutParams mLayoutParams = null;
	OnRowClickListener mOnRowClickListener = null;

	/*
	 * DragDrop Members
	 */
	private Boolean mRowDraggable = false;
	private boolean mDragMode = false;
	private OListDragDropListener mDragDropListener = null;
	private View.DragShadowBuilder mShadowBuilder = null;
	private List<Integer> mDropLayouts = new ArrayList<Integer>();
	private Boolean mDropped = false;
	private Boolean mRowDroppable = false;
	private View mDraggableView = null;
	private Boolean mDragStarted = false;
	private Boolean mDragEnded = false;

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
		if (attrs != null) {
			mTypedArray = mContext.obtainStyledAttributes(attrs,
					R.styleable.OList);
			mAttr.put(KEY_CUSTOM_LAYOUT, mTypedArray.getResourceId(
					R.styleable.OList_custom_layout, 0));
			mCustomLayout = mAttr.getResource(KEY_CUSTOM_LAYOUT, 0);
			mTypedArray.recycle();
		}
	}

	protected void onFinishInflate() {
		super.onFinishInflate();
		removeAllViews();
		createListInnerControl();
	}

	private void createListInnerControl() {
		mInnerLayout = parentView();
	}

	public void initListControl(List<ODataRow> records) {
		mRecords.clear();
		mRecords.addAll(records);
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

	public void setCustomView(int view_resource) {
		mAttr.put(KEY_CUSTOM_LAYOUT, view_resource);
		mCustomLayout = view_resource;
	}

	public void setOnRowClickListener(OnRowClickListener listener) {
		mOnRowClickListener = listener;
	}

	public interface OnRowClickListener {
		public void onRowItemClick(int position, View view, ODataRow row);
	}

	@Override
	public void onClick(View v) {
		if (!mDragMode) {
			int pos = (Integer) v.getTag();
			mOnRowClickListener.onRowItemClick(pos, v,
					(ODataRow) mRecords.get(pos));
		}
	}

	/*
	 * Drag drop methods
	 */
	public void setDragDropListener(OListDragDropListener listener) {
		mDragDropListener = listener;
	}

	@Override
	public boolean onLongClick(View v) {
		mDragMode = true;
		mDragStarted = false;
		mDragEnded = false;
		return true;
	}

	private static class DragShadowBuilder extends View.DragShadowBuilder {

		private static Drawable shadow;
		int width, height;

		public DragShadowBuilder(View v) {
			super(v);
			shadow = new ColorDrawable(Color.LTGRAY);
			width = getView().getWidth() / 2;
			height = getView().getHeight() / 2;
		}

		@Override
		public void onProvideShadowMetrics(Point size, Point touch) {
			shadow.setBounds(0, 0, width, height);
			size.set(width, height);
			touch.set(width / 2, height / 2);
		}

		@Override
		public void onDrawShadow(Canvas canvas) {
			shadow.draw(canvas);
		}

	}

	public void setRowDraggable(boolean draggable) {
		mRowDraggable = draggable;
	}

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

	@Override
	public boolean onDrag(final View v, DragEvent event) {
		int action = event.getAction();
		final View view = (View) event.getLocalState();
		ViewGroup parent = (ViewGroup) view.getParent();
		LinearLayout newParent = (LinearLayout) v;
		final int position = (Integer) view.getTag();
		switch (action) {
		case DragEvent.ACTION_DRAG_STARTED:
			if (mRowDraggable && mDragDropListener != null)
				onDragStart(view, position, mRecords.get(position));
			break;
		case DragEvent.ACTION_DRAG_ENTERED:
			if (mRowDroppable)
				v.setBackgroundColor(Color.GRAY);
			break;
		case DragEvent.ACTION_DRAG_EXITED:
			if (mRowDroppable)
				v.setBackgroundColor(Color.WHITE);
			break;
		case DragEvent.ACTION_DROP:
			if (mRowDroppable) {
				parent.removeView(view);
				// ViewGroup p = (ViewGroup) newParent.getParent();
				// p.addView(view);
				view.setVisibility(View.VISIBLE);
				view.setOnTouchListener(null);
				newParent.setBackgroundColor(Color.WHITE);
				if (getDraggableView() instanceof OList) {
					int drop_position = (Integer) newParent.getTag();
					OList draggableView = (OList) getDraggableView();
					draggableView.setDroppedObjectData(view, position,
							mRecords.get(drop_position));
				}
			}
			mDragMode = false;
			mDropped = true;
			break;
		case DragEvent.ACTION_DRAG_ENDED:
			if (!mDropped && mRowDroppable) {
				view.post(new Runnable() {
					@Override
					public void run() {
						view.setVisibility(View.VISIBLE);
						// toggleDropAreas(false);
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

	public void setDroppedObjectData(View view, int position, Object object) {
		if (mDragDropListener != null) {
			mDragDropListener.onItemDrop(view, mRecords.get(position), object);
		}
	}

	private void onDragStart(View view, int position, Object object) {
		if (!mDragStarted) {
			mDragDropListener.onItemDragStart(view, position, object);
			mDragStarted = true;
		}
	}

	private void onDragEnnd(View view, int position, Object object) {
		if (!mDragEnded) {
			mDragDropListener.onItemDragEnd(view, position,
					mRecords.get(position));
			mDragEnded = true;
		}
	}

	public void setRowDroppable(boolean droppable, View draggableView) {
		mRowDroppable = droppable;
		mDraggableView = draggableView;
	}

	public void addDropListenerLayout(int resource) {
		mDropLayouts.add(resource);
	}

	public View getDraggableView() {
		return mDraggableView;
	}
}
