package com.openerp.support.listview;

import android.view.View;

public interface OEListViewOnCreateListener {
	public View listViewOnCreateListener(int position, View row_view,
			OEListViewRows row_data);
}
