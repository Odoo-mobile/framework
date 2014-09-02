package com.odoo.support.service;

import android.content.SyncResult;

public interface OSyncFinishListener {
	public OSyncAdapter performSync(SyncResult syncResult);
}
