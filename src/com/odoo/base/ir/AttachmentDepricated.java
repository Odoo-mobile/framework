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
package com.odoo.base.ir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.odoo.R;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OModel;
import com.odoo.orm.OSyncHelper;
import com.odoo.orm.OValues;
import com.odoo.orm.OdooHelper;

/**
 * The Class AttachmentFragment.
 */
public class AttachmentDepricated implements OnClickListener {
	public static final String TAG = AttachmentDepricated.class.getSimpleName();

	private Context mContext = null;
	private Notification mNotification = null;
	private Builder mNotificationBuilder = null;
	private PendingIntent mNotificationResultIntent = null;
	private NotificationManager mNotificationManager = null;

	private List<Long> mNewAttachmentIds = new ArrayList<Long>();

	public static int NOTIFICATION_ID = 458;

	public enum Types {
		CAPTURE_IMAGE, IMAGE, IMAGE_OR_CAPTURE_IMAGE, AUDIO, FILE, OTHER
	}

	private String[] mOptions = null;
	private Types mDialogType = null;
	private IrAttachment mDb = null;

	public static final int REQUEST_CAMERA = 111;
	public static final int REQUEST_IMAGE = 112;
	public static final int REQUEST_FILE = 115;
	public static final int REQUEST_AUDIO = 113;

	public AttachmentDepricated(Context context) {
		mContext = context;
		mDb = new IrAttachment(mContext);
	}

	public void requestAttachment(Types type) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_GET_CONTENT);
		switch (type) {
		case IMAGE_OR_CAPTURE_IMAGE:
			// createDialog(type);
			// break;
		case IMAGE:
			intent.setType("image/*");
			requestIntent(intent, REQUEST_IMAGE);
			break;
		case CAPTURE_IMAGE:
			intent = new Intent(
					android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
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

	// private void createDialog(Types type) {
	// mDialogType = type;
	// AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
	// switch (type) {
	// case IMAGE_OR_CAPTURE_IMAGE:
	// builder.setTitle("Image");
	// mOptions = new String[] { "Select Image", "Capture Image" };
	// break;
	// default:
	// break;
	// }
	// builder.setSingleChoiceItems(mOptions, -1, this);
	// builder.create().show();
	// }

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (mDialogType) {
		case IMAGE_OR_CAPTURE_IMAGE:
			requestAttachment((which == 0) ? Types.IMAGE : Types.CAPTURE_IMAGE);
			break;
		default:
		}
		dialog.cancel();
	}

	private void requestIntent(Intent intent, int requestCode) {
		((Activity) mContext).startActivityForResult(intent, requestCode);
	}

	public ODataRow handleResult(Intent data) {
		return handleResult(-1, data);
	}

	public List<ODataRow> handleMultipleResult(Intent data) {
		List<ODataRow> attachments = new ArrayList<ODataRow>();
		ArrayList<Uri> fileUris = data
				.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
		for (Uri uri : fileUris) {
			attachments.add(uriToDataRow(uri, null));
		}
		return attachments;
	}

	public ODataRow handleResult(int requestCode, Intent data) {
		Uri uri = null;
		Bitmap bitmap = null;
		switch (requestCode) {
		case REQUEST_AUDIO:
			uri = data.getData();
			break;
		case REQUEST_CAMERA:
			bitmap = (Bitmap) data.getExtras().get("data");
			uri = data.getData();
			break;
		case REQUEST_IMAGE:
			uri = data.getData();
			break;
		case REQUEST_FILE:
			uri = data.getData();
			break;
		default: // Single Attachment (share)
			uri = data.getParcelableExtra(Intent.EXTRA_STREAM);
			break;
		}

		return uriToDataRow(uri, bitmap);
	}

	private ODataRow uriToDataRow(Uri uri, Bitmap bitmap) {
		ODataRow attachment = new ODataRow();
		String filename = "";
		String file_type = "";
		if (uri != null) {
			String[] file_info = getFileName(uri);
			filename = file_info[0];
			file_type = file_info[1];
		}
		attachment.put("name", filename);
		attachment.put("file_type", file_type);
		attachment.put("file_uri", (uri != null) ? uri.toString() : false);
		attachment.put("bitmap", (bitmap != null) ? bitmap : false);
		return attachment;
	}

	private String[] getFileName(Uri uri) {
		String[] file_info = null;
		String filename = "";
		String file_type = "";
		if (uri.getScheme().toString().compareTo("content") == 0) {
			Cursor cursor = mContext.getContentResolver().query(uri, null,
					null, null, null);
			if (cursor.moveToFirst()) {
				int column_index = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				filename = cursor.getString(column_index);

				File fl = new File(filename);
				filename = fl.getName();
			}
			file_type = mContext.getContentResolver().getType(uri);
		} else if (uri.getScheme().compareTo("file") == 0) {
			filename = uri.getLastPathSegment().toString();
			file_type = "file";
		} else {
			filename = filename + "_" + uri.getLastPathSegment();
			file_type = "file";
		}
		file_info = new String[] { filename, file_type };
		return file_info;
	}

	public List<ODataRow> select(String model, int id) {
		return mDb.select("res_model = ? AND res_id = ?", new Object[] { model,
				id });
	}

	public void removeAttachment(int attachment_id) {
		RemoveAttachment remove = new RemoveAttachment(mDb, attachment_id);
		remove.execute();
	}

	class RemoveAttachment extends AsyncTask<Void, Void, Void> {

		int mId = 0;
		IrAttachment mModel = null;

		public RemoveAttachment(OModel db, int id) {
			mId = id;
			mModel = (IrAttachment) db;
		}

		@Override
		protected Void doInBackground(Void... params) {
			return null;
		}

	}

	public void updateAttachments(String model, int id,
			List<ODataRow> attachments) {
		updateAttachments(model, id, attachments, true);
	}

	public void updateAttachments(String model, int id,
			List<ODataRow> attachments, boolean asBackgroundTask) {
		List<OValues> values = new ArrayList<OValues>();
		// for (ODataRow row : attachments) {
		// if (row.getInt("id") == 0) {
		// String name = "";
		// String base64 = "";
		// String file_type = "";
		// int company_id = Integer.parseInt(OUser.current(mContext)
		// .getCompany_id());
		// base64 = Base64Helper.fileUriToBase64(
		// Uri.parse(row.getString("file_uri")),
		// mContext.getContentResolver());
		// name = row.getString("name");
		// file_type = row.getString("file_type");
		// OValues value = new OValues();
		// value.put("name", name);
		// value.put("datas_fname", name);
		// value.put("db_datas", base64);
		// value.put("file_type", file_type);
		// value.put("res_model", model);
		// value.put("res_id", id);
		// value.put("company_id", company_id);
		// value.put("type", "binary");
		// value.put("size", 0);
		// value.put("file_uri", row.getString("file_uri"));
		// values.add(value);
		// }
		// }
		// if (values.size() > 0) {
		if (asBackgroundTask) {
			CreateAttachment attachment = new CreateAttachment(
					mDb.getSyncHelper(), values);
			attachment.execute();
		} else {
			OSyncHelper helper = mDb.getSyncHelper();
			mNewAttachmentIds.clear();
			for (OValues value : values) {
				long a_id = 0;// helper.create(data_row);
				Log.i(TAG, "Attachment created #" + a_id);
				mNewAttachmentIds.add(a_id);
			}

			// }
		}
	}

	class CreateAttachment extends AsyncTask<Void, Void, Void> {

		boolean mConnection = false;
		OSyncHelper mOdoo = null;
		List<OValues> mAttachments = null;

		public CreateAttachment(OSyncHelper odoo, List<OValues> row) {
			if (odoo != null) {
				mConnection = true;
				mOdoo = odoo;
				mAttachments = row;
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (mConnection) {
				for (OValues values : mAttachments) {
					long id = 0;// FIXME: mOdoo.create(values);
					Log.i(TAG, "Attachment created #" + id);
				}
			}
			return null;
		}
	}

	public List<Long> newAttachmentIds() {
		Log.d(TAG, "newAttachmentIds()");
		Log.v(TAG, "Attachment ids #" + mNewAttachmentIds.toString());
		return mNewAttachmentIds;
	}

	private void initNotificationManager() {
		mNotificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationBuilder = new NotificationCompat.Builder(mContext);
		mNotificationBuilder.setContentTitle("Downloading attachment");
		mNotificationBuilder.setContentText("Download in progress");
		mNotificationBuilder.setSmallIcon(R.drawable.ic_odoo_o);
		mNotificationBuilder.setVibrate(new long[] { 1000, 1000 });
		mNotificationBuilder.setAutoCancel(true);
		mNotificationBuilder.setOngoing(true);
	}

	@SuppressWarnings("deprecation")
	private Notification setFileIntent(Uri uri) {
		Log.v(TAG, "setFileIntent()");
		Intent intent = new Intent(Intent.ACTION_VIEW);
		FileNameMap mime = URLConnection.getFileNameMap();
		String mimeType = mime.getContentTypeFor(uri.getPath());
		intent.setDataAndType(uri, mimeType);
		mNotificationResultIntent = PendingIntent.getActivity(mContext, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mNotificationBuilder.addAction(R.drawable.ic_odoo_o,
				"Download attachment", mNotificationResultIntent);
		mNotificationBuilder.setOngoing(false);
		mNotificationBuilder.setAutoCancel(true);
		mNotificationBuilder.setContentTitle("Attachment downloaded");
		mNotificationBuilder.setContentText("Download Complete");
		mNotificationBuilder.setProgress(0, 0, false);
		mNotification = mNotificationBuilder.build();
		mNotification.setLatestEventInfo(mContext, "Attachment downloaded",
				"Download complete", mNotificationResultIntent);
		return mNotification;

	}

	private void requestIntent(Uri uri) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		FileNameMap mime = URLConnection.getFileNameMap();
		String mimeType = mime.getContentTypeFor(uri.getPath());
		intent.setDataAndType(uri, mimeType);
		mContext.startActivity(intent);
	}

	boolean error = false;

	private void showNotification(final int attachment_id) {
		AttachmentDownloader attachmentLoader = new AttachmentDownloader(
				attachment_id);
		attachmentLoader.execute();
		if (error)
			Toast.makeText(mContext,
					"Someting gone wrong. No file data found.",
					Toast.LENGTH_LONG).show();
	}

	class AttachmentDownloader extends AsyncTask<Void, Void, Void> {

		ODataRow mAttachmentInfo = null;
		OdooHelper mOdoo = null;
		int mID = 0;

		public AttachmentDownloader(int attachment_id) {
			initNotificationManager();
			mID = NOTIFICATION_ID++;
			// mAttachmentInfo = mDb.select(attachment_id);
			if (mAttachmentInfo.getString("file_uri").equals("false")) {
				mNotificationBuilder.setProgress(0, 0, true);
				mNotification = mNotificationBuilder.build();
				mNotificationManager.notify(mID, mNotification);
			}
			// mOE = mDb.getOEInstance();
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (!mAttachmentInfo.getString("file_uri").equals("false")) {
				return null;
			} else {
				 
					
					 
						  
//						  String file_path = createFile( row.getString("name"),
//						  row.getString("datas"), row.getString("file_type")); 
//						  
//						  Uri uri
//						  = Uri.fromFile(new File(file_path)); mNotification =
//						  setFileIntent(uri); OEValues values = new OEValues();
//						  values.put("file_uri", uri.toString()); // mDb.update(values,
//						  mAttachmentInfo.getInt("id")); } else { error = true; } 
						   				
				 
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (!mAttachmentInfo.getString("file_uri").equals("false")) {
				Uri uri = Uri.parse(mAttachmentInfo.getString("file_uri"));
				requestIntent(uri);
			} else {
				mNotificationManager.cancel(mID);
				mNotificationManager.notify(mID, mNotification);
			}
		}

	}

	public void downloadFile(int attachment_id) {
		Log.v(TAG, "Downloading attachment #" + attachment_id);
		showNotification(attachment_id);
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

}