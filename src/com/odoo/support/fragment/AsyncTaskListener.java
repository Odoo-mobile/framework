package com.odoo.support.fragment;

public interface AsyncTaskListener {
	public Object onPerformTask();

	public void onFinish(Object result);
}
