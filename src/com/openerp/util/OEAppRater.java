package com.openerp.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.openerp.R;

public class OEAppRater {
	private static String app_title = "APP-NAME";
	private static String app_pname = "PACKAGE-NAME";

	private final static int DAYS_UNTIL_PROMPT = 3;
	private final static int LAUNCHES_UNTIL_PROMPT = 7;

	public static void app_launched(Context mContext) {
		app_title = mContext.getResources().getString(R.string.app_name);
		app_pname = mContext.getPackageName();
		SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
		if (prefs.getBoolean("dontshowagain", false)) {
			return;
		}

		SharedPreferences.Editor editor = prefs.edit();
		long launch_count = prefs.getLong("launch_count", 0) + 1;
		editor.putLong("launch_count", launch_count);
		Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
		if (date_firstLaunch == 0) {
			date_firstLaunch = System.currentTimeMillis();
			editor.putLong("date_firstlaunch", date_firstLaunch);
		}
		if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
			if (System.currentTimeMillis() >= date_firstLaunch
					+ (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
				showRateDialog(mContext, editor);
			}
		}
		editor.commit();
	}

	private static Dialog dialog = null;

	public static void showRateDialog(final Context mContext,
			final SharedPreferences.Editor editor) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

		builder.setTitle("Rate " + app_title);
		builder.setIcon(R.drawable.ic_action_starred);
		builder.setMessage("If you enjoy using " + app_title
				+ ", please take a moment to rate it. Thanks for your support!");
		builder.setNegativeButton("Rate " + app_title,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						Intent intent = new Intent(Intent.ACTION_VIEW, Uri
								.parse("market://details?id=" + app_pname));
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						mContext.startActivity(intent);
						if (editor != null) {
							editor.putBoolean("dontshowagain", true);
							editor.commit();
						}
						dialog.dismiss();
					}
				});
		builder.setNeutralButton("Remind me later",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.setPositiveButton("No, thanks",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (editor != null) {
							editor.putBoolean("dontshowagain", true);
							editor.commit();
						}
						dialog.dismiss();
					}
				});
		dialog = builder.create();
		dialog.show();
	}
}
