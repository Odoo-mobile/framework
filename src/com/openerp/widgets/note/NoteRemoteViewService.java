package com.openerp.widgets.note;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class NoteRemoteViewService extends RemoteViewsService {

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		NoteRemoteViewFactory mRemoteViewFactory = new NoteRemoteViewFactory(
				getApplicationContext(), intent);
		return mRemoteViewFactory;
	}

}
