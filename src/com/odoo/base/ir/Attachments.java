package com.odoo.base.ir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.odoo.App;
import com.odoo.R;
import com.odoo.orm.OColumn;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OValues;
import com.odoo.util.Base64Helper;
import com.odoo.util.ODate;

public class Attachments implements OnClickListener {
	public static final String TAG = Attachments.class.getSimpleName();
	public static final String KEY_DB_DATAS = "db_datas";
	public static final String KEY_TYPE = "type";
	public static final int REQUEST_CAMERA = 111;
	public static final int REQUEST_IMAGE = 112;
	public static final int REQUEST_AUDIO = 113;
	public static final int REQUEST_FILE = 114;
	private static final int SINGLE_ATTACHMENT_STREAM = 115;
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
	private String[] mOptions = null;
	private Uri newImageUri;

	public enum Types {
		CAPTURE_IMAGE, IMAGE, IMAGE_OR_CAPTURE_IMAGE, AUDIO, FILE, OTHER
	}

	private Types mDialogType = null;

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
				} else if (isKitKat()) {
					String kitkatDoc = getKitKatDocPath(file_uri);
					if (kitkatDoc != null) {
						file_uri = Uri.fromFile(new File(kitkatDoc));
						_open(file_uri);
					} else if (attachment.getInt("id") != 0)
						_download(attachment);
					else
						noFileFound();
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

	private class DownloadManager extends AsyncTask<Void, Void, Object> {
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
		protected Object doInBackground(Void... params) {
			if (mApp.inNetwork()) {
				String base64Data = mAttachment.getBase64Data(server_id, mApp);
				if (!base64Data.equals("false")) {
					String file = createFile(
							attachment.getString(KEY_FILE_NAME),
							Base64.decode(base64Data, 0),
							attachment.getString(KEY_FILE_TYPE));
					uri = Uri.fromFile(new File(file));
					OValues values = new OValues();
					values.put(KEY_FILE_URI, uri.toString());
					mAttachment.update(values,
							attachment.getInt(OColumn.ROW_ID));
					return true;
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if (result != null) {
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
			} else {
				mNotificationManager.cancel(notification_id);
				Toast.makeText(mContext, "Unable to download file !",
						Toast.LENGTH_LONG).show();
			}
		}

	}

	private String createFile(String name, byte[] fileAsBytes, String file_type) {

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

	@SuppressLint("NewApi")
	private String getKitKatDocPath(Uri uri) {
		String wholeID = DocumentsContract.getDocumentId(uri);
		String id = wholeID.split(":")[1];
		String[] column = { MediaStore.Images.Media.DATA };
		String sel = MediaStore.Images.Media._ID + "=?";
		Cursor cursor = mContext.getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel,
				new String[] { id }, null);
		String filePath = null;
		int columnIndex = cursor.getColumnIndex(column[0]);
		if (cursor.moveToFirst()) {
			filePath = cursor.getString(columnIndex);
		}
		cursor.close();
		return filePath;
	}

	private boolean isKitKat() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
	}

	@SuppressLint("InlinedApi")
	public void newAttachment(Types type) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_GET_CONTENT);
		switch (type) {
		case IMAGE_OR_CAPTURE_IMAGE:
			createDialog(type);
			break;
		case IMAGE:
			if (Build.VERSION.SDK_INT < 19) {
				intent = new Intent();
				intent.setAction(Intent.ACTION_GET_CONTENT);
			} else {
				intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
			}
			intent.setType("image/*");
			requestIntent(intent, REQUEST_IMAGE);
			break;
		case CAPTURE_IMAGE:
			ContentValues values = new ContentValues();
			values.put(MediaStore.Images.Media.TITLE, "Odoo Mobile Attachment");
			values.put(MediaStore.Images.Media.DESCRIPTION,
					"Captured from Odoo Mobile App");
			newImageUri = mContext.getContentResolver().insert(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, newImageUri);
			requestIntent(intent, REQUEST_CAMERA);
			break;
		case AUDIO:
			intent.setType("audio/*");
			requestIntent(intent, REQUEST_AUDIO);
			break;
		case FILE:
			intent.setType("application/file");
			requestIntent(intent, REQUEST_FILE);
			break;
		default:
			break;
		}
	}

	private void createDialog(Types type) {
		mDialogType = type;
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		switch (type) {
		case IMAGE_OR_CAPTURE_IMAGE:
			builder.setTitle("Image");
			mOptions = new String[] { "Select Image", "Capture Image" };
			break;
		default:
			break;
		}
		builder.setSingleChoiceItems(mOptions, -1, this);
		builder.create().show();
	}

	private void requestIntent(Intent intent, int requestCode) {
		try {
			((Activity) mContext).startActivityForResult(intent, requestCode);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(mContext, "No Activity Found to handle request",
					Toast.LENGTH_SHORT).show();
		}
	}

	public OValues handleResult(int requestCode, Intent data) {
		OValues attachment = new OValues();
		switch (requestCode) {
		case REQUEST_AUDIO:
		case REQUEST_FILE:
		case REQUEST_IMAGE:
			attachment = getURIDetails(data.getData());
			break;
		case REQUEST_CAMERA:
			attachment = getURIDetails(newImageUri);
			break;
		case SINGLE_ATTACHMENT_STREAM:
			Uri uri = data.getParcelableExtra(Intent.EXTRA_STREAM);
			attachment = getURIDetails(uri);
			break;
		default:
			return null;
		}
		return attachment;
	}

	public OValues getURIDetails(Uri uri) {
		OValues values = new OValues();
		ContentResolver mCR = mContext.getContentResolver();
		if (uri.getScheme().equals("content")) {
			Cursor cr = mCR.query(uri, null, null, null, null);
			int nameIndex = cr.getColumnIndex(OpenableColumns.DISPLAY_NAME);
			int fileSize = cr.getColumnIndex(OpenableColumns.SIZE);
			if (cr.moveToFirst()) {
				values.put("name", cr.getString(nameIndex));
				values.put("datas_fname", values.get("name"));
				values.put("file_size", Long.toString(cr.getLong(fileSize)));
			}
		}
		if (uri.getScheme().equals("file")) {
			File file = new File(uri.toString());
			values.put("name", file.getName());
			values.put("datas_fname", values.get("name"));
			values.put("file_size", Long.toString(file.length()));
		}
		values.put("file_uri", uri.toString());
		values.put("scheme", uri.getScheme());
		MimeTypeMap mime = MimeTypeMap.getSingleton();
		String type = mime.getMimeTypeFromExtension(mime
				.getExtensionFromMimeType(mCR.getType(uri)));
		values.put("file_type", (type == null) ? uri.getScheme() : type);
		values.put("type", type);
		if (mAttachment.getColumn("write_date") != null)
			values.put("write_date", ODate.getUTCDate(ODate.DEFAULT_FORMAT));
		return values;
	}

	public int pushToServer(ODataRow row) {
		int attachment_id = 0;
		try {
			IrAttachment attachment = new IrAttachment(mContext);
			String base64 = Base64Helper.fileUriToBase64(
					Uri.parse(row.getString("file_uri")),
					mContext.getContentResolver());
			row.put(KEY_DB_DATAS, base64);
			row.put(KEY_TYPE, "binary");
			attachment_id = attachment.getSyncHelper().create(attachment, row);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return attachment_id;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (mDialogType) {
		case IMAGE_OR_CAPTURE_IMAGE:
			newAttachment((which == 0) ? Types.IMAGE : Types.CAPTURE_IMAGE);
			break;
		default:
		}
		dialog.cancel();
	}

	public List<OValues> handleIntentRequest(Intent intent) {
		List<OValues> attachments = new ArrayList<OValues>();
		String action = intent.getAction();
		// Handling single attachment request
		if (Intent.ACTION_SEND.equals(action)) {
			attachments.add(handleResult(SINGLE_ATTACHMENT_STREAM, intent));
		}

		// Handling multiple attachments request
		if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
			for (Parcelable attach : intent
					.getParcelableArrayListExtra(Intent.EXTRA_STREAM)) {
				Uri uri = (Uri) attach;
				attachments.add(getURIDetails(uri));
			}
		}
		return attachments;
	}

}
