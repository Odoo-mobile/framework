/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http:www.openerp.com>)
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
package com.openerp.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.openerp.R;
import com.openerp.orm.OEDatabase;
import com.openerp.orm.OEHelper;
import com.openerp.support.OpenERPServerConnection;

public class OEBinaryDownloadHelper {
	public static final String TAG = "com.openerp.util.OEBinaryDownloadHelper";
	ProgressDialog mProgressDialog;
	DownloadTask downloadTask = null;
	Context mContext = null;

	public void downloadBinary(int id, OEDatabase db, Context context) {
		Log.d(TAG, "OEBinaryDownloadHelper->downloadBinary()");
		mContext = context;
		try {
			if (OpenERPServerConnection.isNetworkAvailable(mContext)) {
				mProgressDialog = new ProgressDialog(mContext);
				mProgressDialog.setMessage("Downloading...");
				mProgressDialog.setIndeterminate(true);
				mProgressDialog
						.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				mProgressDialog.setCancelable(true);

				downloadTask = new DownloadTask(db);
				downloadTask.execute(id);
			} else {
				Toast.makeText(mContext, "Unable to connect server !",
						Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class DownloadTask extends AsyncTask<Integer, Integer, Boolean> {

		int attachment_id = 0;
		OEDatabase db = null;
		String downloadPath = "";
		String downloadFileName = "";

		public DownloadTask(OEDatabase db) {
			this.db = db;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog.show();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.setMax(100);
			mProgressDialog.setProgress(values[0]);
		}

		@Override
		protected Boolean doInBackground(Integer... arg) {
			this.attachment_id = arg[0];
			OEHelper oe = db.getOEInstance();
			try {
				JSONObject fields = new JSONObject();
				fields.accumulate("fields", "db_datas");
				fields.accumulate("fields", "datas_fname");
				JSONObject res = oe.openERP().search_read(
						"ir.attachment",
						fields,
						new JSONObject().accumulate("domain",
								new JSONArray("[[\"id\", \"=\", "
										+ this.attachment_id + "]]")), 0, 1,
						null, null);
				String base64Data = res.getJSONArray("records")
						.getJSONObject(0).getString("db_datas");
				String filename = res.getJSONArray("records").getJSONObject(0)
						.getString("datas_fname");
				int count;

				byte[] fileAsBytes = Base64.decode(base64Data, 0);
				int lenghtOfFile = fileAsBytes.length;
				InputStream is = new ByteArrayInputStream(fileAsBytes);
				filename = filename.replaceAll("[-+^:, ]", "_");
				String path = "/sdcard/Download/" + filename;
				downloadPath = path;
				downloadFileName = filename;
				FileOutputStream fos = new FileOutputStream(path);
				byte data[] = new byte[1024];
				long total = 0;
				int progress = 0;
				while ((count = is.read(data)) != -1) {
					total += count;
					int progress_temp = (int) total * 100 / lenghtOfFile;
					publishProgress(progress_temp);
					if (progress_temp % 10 == 0 && progress != progress_temp) {
						progress = progress_temp;
					}
					fos.write(data, 0, count);

				}
				is.close();
				fos.close();

			} catch (Exception e) {
				return false;
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			mProgressDialog.dismiss();
			if (!result) {
				Toast.makeText(mContext,
						"Server not responding. Try again later.",
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(mContext, "File downloaded", Toast.LENGTH_SHORT)
						.show();
				OENotificationHelper notification = new OENotificationHelper();
				Intent intent = new Intent(Intent.ACTION_VIEW);
				File fl = new File(downloadPath);
				FileNameMap mime = URLConnection.getFileNameMap();
				Uri uri = Uri.fromFile(fl);
				String mimeType = mime.getContentTypeFor(uri.getPath());
				intent.setDataAndType(uri, mimeType);
				notification.setResultIntent(intent, mContext);
				notification.showNotification(mContext, downloadFileName
						+ " Download complete", downloadPath, "",
						R.drawable.ic_stat_av_download);

			}
		}
	}
}