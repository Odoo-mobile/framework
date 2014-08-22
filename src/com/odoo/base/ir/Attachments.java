package com.odoo.base.ir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.odoo.App;
import com.odoo.R;
import com.odoo.orm.OColumn;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OValues;
import com.odoo.util.Base64Helper;

public class Attachments {
	public static final String TAG = Attachments.class.getSimpleName();

	private String KEY_FILE_URI = "file_uri";
	private String KEY_FILE_NAME = "datas_fname";
	private String KEY_FILE_TYPE = "file_type";
	private Context mContext = null;
	private App mApp = null;
	private IrAttachment mAttachment = null;
	private Notification mNotification = null;
	private Builder mNotificationBuilder = null;
	private PendingIntent mNotificationResultIntent = null;
	private NotificationManager mNotificationManager = null;
	private static Integer notification_id = 1;

	public Attachments(Context context) {
		mContext = context;
		mApp = (App) mContext.getApplicationContext();
		mAttachment = new IrAttachment(mContext);
	}

	public void downloadAttachment(int attachment_id) {
		ODataRow attachment = mAttachment.select(attachment_id);
		if (attachment != null) {
			String uri = attachment.getString(KEY_FILE_URI);
			if (uri.equals("false")) {
				_download(attachment);
			} else {
				Uri file_uri = Uri.parse(uri);
				if (fileExists(file_uri)) {
					_open(file_uri);
				} else if (attachment.getInt("id") != 0) {
					_download(attachment);
				} else {
					noFileFound();
				}
			}
		} else {
			noFileFound();
		}
	}

	private void _open(Uri uri) {
		requestIntent(uri);
	}

	private void requestIntent(Uri uri) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		FileNameMap mime = URLConnection.getFileNameMap();
		String mimeType = mime.getContentTypeFor(uri.getPath());
		intent.setDataAndType(uri, mimeType);
		try {
			mContext.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(mContext, "No activity found to handle file",
					Toast.LENGTH_LONG).show();
		}
	}

	private void _download(ODataRow attachment) {
		initNotificationManager(attachment.getString(KEY_FILE_NAME));
		DownloadManager downloader = new DownloadManager(attachment,
				notification_id++);
		downloader.execute();
	}

	private class DownloadManager extends AsyncTask<Void, Void, Void> {
		private int notification_id = -1;
		private int server_id = -1;
		private ODataRow attachment = null;
		private Uri uri = null;

		public DownloadManager(ODataRow row, int id) {
			notification_id = id;
			attachment = row;
			server_id = row.getInt("id");
			mNotificationBuilder.setProgress(0, 0, true);
			mNotification = mNotificationBuilder.build();
			mNotificationManager.notify(notification_id, mNotification);
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (mApp.inNetwork()) {
				String base64Data = mAttachment.getBase64Data(server_id, mApp);
				String file = createFile(attachment.getString(KEY_FILE_NAME),
						base64Data, attachment.getString(KEY_FILE_TYPE));
				uri = Uri.fromFile(new File(file));
				OValues values = new OValues();
				values.put(KEY_FILE_URI, uri.toString());
				mAttachment.update(values, attachment.getInt(OColumn.ROW_ID));
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mNotificationManager.cancel(notification_id);
			if (mApp.inNetwork()) {
				setSoundForNotification();
				setVibrateForNotification();
				setFileIntent(attachment, uri);
				mNotification = mNotificationBuilder.build();
				mNotificationManager.notify(notification_id, mNotification);
			} else {
				Toast.makeText(mContext, "No network !", Toast.LENGTH_LONG)
						.show();
			}
		}

	}

	private String createFile(String name, String base64, String file_type) {
		byte[] fileAsBytes = Base64.decode(base64, 0);

		InputStream is = new ByteArrayInputStream(fileAsBytes);
		String filename = name.replaceAll("[-+^:=, ]", "_");
		String file_path = getDirectoryPath(file_type) + "/" + filename;
		try {
			FileOutputStream fos = new FileOutputStream(file_path);
			byte data[] = new byte[1024];
			int count = 0;
			while ((count = is.read(data)) != -1) {
				fos.write(data, 0, count);
			}
			is.close();
			fos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return file_path;
	}

	private String getDirectoryPath(String file_type) {
		String path = getAppDirectoryPath();

		if (file_type.contains("image")) {
			path += "/Images";
		} else if (file_type.contains("audio")) {
			path += "/Audio";
		} else {
			path += "/Files";
		}
		File fileDir = new File(path);
		if (!fileDir.isDirectory()) {
			fileDir.mkdir();
		}
		return path;
	}

	private String getAppDirectoryPath() {
		File externalStorage = Environment.getExternalStorageDirectory();
		String basePath = externalStorage.getAbsolutePath() + "/Odoo";
		File baseDir = new File(basePath);
		if (!baseDir.isDirectory()) {
			baseDir.mkdir();
		}
		return basePath;
	}

	private void initNotificationManager(String filename) {
		mNotificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationBuilder = new NotificationCompat.Builder(mContext);
		mNotificationBuilder.setContentTitle("Downloading " + filename);
		mNotificationBuilder.setContentText("Download in progress");
		mNotificationBuilder.setSmallIcon(R.drawable.ic_odoo_o);
		mNotificationBuilder.setAutoCancel(true);
		mNotificationBuilder.setOngoing(true);
	}

	private void setSoundForNotification() {
		mNotificationBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000,
				1000 });
	}

	private void setVibrateForNotification() {
		Uri uri = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		mNotificationBuilder.setSound(uri);
	}

	private void setFileIntent(ODataRow attachment, Uri uri) {
		Log.v(TAG, "setFileIntent()");
		String filename = attachment.getString(KEY_FILE_NAME);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		FileNameMap mime = URLConnection.getFileNameMap();
		String mimeType = mime.getContentTypeFor(uri.getPath());
		intent.setDataAndType(uri, mimeType);
		mNotificationResultIntent = PendingIntent.getActivity(mContext, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mNotificationBuilder.addAction(R.drawable.ic_odoo_o, filename,
				mNotificationResultIntent);

		if (attachment.getString(KEY_FILE_TYPE).contains("image")) {
			Bitmap bitmap = Base64Helper.getBitmapImage(
					mContext,
					Base64Helper.fileUriToBase64(uri,
							mContext.getContentResolver()));
			mNotificationBuilder
					.setStyle(new NotificationCompat.BigPictureStyle()
							.bigPicture(bitmap));
		}
		mNotificationBuilder.setOngoing(false);
		mNotificationBuilder.setAutoCancel(true);
		mNotificationBuilder.setContentTitle(filename);
		mNotificationBuilder.setContentText("Download Complete");
		mNotificationBuilder.setProgress(0, 0, false);

		mNotificationBuilder.setContentIntent(mNotificationResultIntent);
		mNotificationBuilder.setContentTitle("File downloaded");
		mNotificationBuilder.setContentText("Download complete");

	}

	private void noFileFound() {
		Toast.makeText(mContext, "Unable to find attachment !",
				Toast.LENGTH_LONG).show();
	}

	private boolean fileExists(Uri uri) {
		return new File(uri.getPath()).exists();
	}

}
