/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p/>
 * Created on 16/1/15 3:36 PM
 */
package com.odoo.base.addons.ir.feature;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.odoo.App;
import com.odoo.R;
import com.odoo.base.addons.ir.IrAttachment;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.support.OdooCompatActivity;
import com.odoo.core.tools.permissions.DevicePermissionHelper;
import com.odoo.core.utils.BitmapUtils;
import com.odoo.core.utils.OAlert;
import com.odoo.core.utils.OResource;
import com.odoo.core.utils.OStorageUtils;
import com.odoo.core.utils.notification.ONotificationBuilder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class OFileManager implements DialogInterface.OnClickListener {
    public static final String TAG = OFileManager.class.getSimpleName();
    public static final int REQUEST_CAMERA = 111;
    public static final int REQUEST_HIGH_CAMERA = 112;
    public static final int REQUEST_IMAGE = 113;
    public static final int REQUEST_AUDIO = 114;
    public static final int REQUEST_FILE = 115;
    public static final int REQUEST_ALL_FILE = 116;
    private static final int SINGLE_ATTACHMENT_STREAM = 117;
    private static final long IMAGE_MAX_SIZE = 1000000; // 1 MB
    private OdooCompatActivity mActivity = null;
    private String[] mOptions = null;
    private RequestType requestType = null;
    private Uri newImageUri = null;
    private IrAttachment irAttachment = null;
    private App mApp;
    private DevicePermissionHelper devicePermissionHelper;

    public enum RequestType {
        CAPTURE_HIGH_IMAGE,
        CAPTURE_IMAGE,
        IMAGE,
        IMAGE_OR_CAPTURE_IMAGE,
        IMAGE_OR_CAPTURE_HIGH_IMAGE,
        AUDIO,
        FILE,
        ALL_FILE_TYPE
    }

    public OFileManager(OdooCompatActivity activity) {
        mActivity = activity;
        irAttachment = new IrAttachment(mActivity, null);
        mApp = (App) mActivity.getApplicationContext();
        devicePermissionHelper = new DevicePermissionHelper(activity);
    }

    public void downloadAttachment(final int attachment_id) {
        if (devicePermissionHelper.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            _downloadAttachment(attachment_id);
        } else {
            devicePermissionHelper.requestToGrantPermission(new DevicePermissionHelper
                    .PermissionGrantListener() {
                @Override
                public void onPermissionGranted() {
                    _downloadAttachment(attachment_id);
                }

                @Override
                public void onPermissionDenied() {
                    Toast.makeText(mActivity, R.string.toast_permission_download_storage,
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onPermissionRationale() {
                    Toast.makeText(mActivity, R.string.toast_permission_download_storage_help,
                            Toast.LENGTH_LONG).show();
                }
            }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private void _downloadAttachment(int id) {
        ODataRow attachment = irAttachment.browse(id);
        if (attachment != null) {
            String uri = attachment.getString("file_uri");
            if (uri.equals("false")) {
                // Downloading new file
                _download(attachment);
            } else {
                Uri file_uri = Uri.parse(uri);
                if (fileExists(file_uri)) {
                    requestIntent(file_uri);
                } else if (atLeastKitKat()) {
                    String file_path = getDocPath(file_uri);
                    if (file_path != null) {
                        file_uri = Uri.fromFile(new File(file_path));
                        requestIntent(file_uri);
                    } else if (attachment.getInt("id") != 0) {
                        // Downloading new file
                        _download(attachment);
                    } else {
                        // Failed to get file
                        OAlert.showAlert(mActivity, "Unable to find file !");
                    }
                } else if (fileExists(file_uri)) {
                    requestIntent(file_uri);
                } else {
                    // Failed to get file
                    OAlert.showAlert(mActivity, "Unable to find file !");
                }
            }
        }
    }


    private void _download(ODataRow attachment) {
        ONotificationBuilder builder = new ONotificationBuilder(mActivity,
                attachment.getInt(OColumn.ROW_ID));
        builder.setTitle("Downloading " + attachment.getString("name"));
        builder.setText("Download in progress");
        builder.setOngoing(true);
        builder.setAutoCancel(true);
        DownloadManager downloader = new DownloadManager(builder);
        downloader.execute(attachment);
    }

    private class DownloadManager extends AsyncTask<ODataRow, Void, ODataRow> {

        ONotificationBuilder notificationBuilder;

        public DownloadManager(ONotificationBuilder builder) {
            notificationBuilder = builder;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            notificationBuilder.allowVibrate(false);
            notificationBuilder.withRingTone(false);
            notificationBuilder.setProgress(0, 0, true);
            notificationBuilder.build().show();
        }

        @Override
        protected ODataRow doInBackground(ODataRow... params) {
            if (mApp.inNetwork()) {
                try {
                    Thread.sleep(500);
                    ODataRow attachment = params[0];
                    String base64 = irAttachment.getDatasFromServer(attachment.getInt(OColumn.ROW_ID));
                    if (!base64.equals("false")) {
                        String file = createFile(attachment.getString("name"),
                                Base64.decode(base64, 0)
                                , attachment.getString("file_type"));
                        Uri uri = Uri.fromFile(new File(file));
                        OValues values = new OValues();
                        values.put("file_uri", uri.toString());
                        irAttachment.update(attachment.getInt(OColumn.ROW_ID), values);
                        return irAttachment.browse(attachment.getInt(OColumn.ROW_ID));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(ODataRow row) {
            super.onPostExecute(row);
            if (row != null) {
                ONotificationBuilder.cancelNotification(mActivity, row.getInt(OColumn.ROW_ID));
                ONotificationBuilder builder = new ONotificationBuilder(mActivity,
                        row.getInt(OColumn.ROW_ID));
                builder.allowVibrate(true);
                builder.withRingTone(true);
                builder.setTitle(row.getString("name"));
                builder.setText("Download Complete");
                builder.setBigText("Download Complete");
                if (row.getString("file_type").contains("image")) {
                    Bitmap bmp = getBitmapFromURI(Uri.parse(row.getString("file_uri")));
                    builder.setBigPicture(bmp);
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(row.getString("file_uri")), row.getString("file_type"));
                builder.setResultIntent(intent);
                builder.build().show();
            } else {
                ONotificationBuilder.cancelNotification(mActivity);
            }
        }
    }

    private String createFile(String name, byte[] fileAsBytes, String file_type) {
        InputStream is = new ByteArrayInputStream(fileAsBytes);
        String filename = name.replaceAll("[-+^:=, ]", "_");
        String file_path = OStorageUtils.getDirectoryPath(file_type) + "/" + filename;
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

    private void requestIntent(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        FileNameMap mime = URLConnection.getFileNameMap();
        String mimeType = mime.getContentTypeFor(uri.getPath());
        intent.setDataAndType(uri, mimeType);
        try {
            mActivity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mActivity, OResource.string(mActivity, R.string.toast_no_activity_found_to_handle_file),
                    Toast.LENGTH_LONG).show();
        }
    }


    private boolean fileExists(Uri uri) {
        return new File(uri.getPath()).exists();
    }

    public Bitmap getBitmapFromURI(Uri uri) {
        Bitmap bitmap;
        if (!fileExists(uri) && atLeastKitKat()) {
            String path = getDocPath(uri);
            bitmap = BitmapUtils.getBitmapImage(mActivity,
                    BitmapUtils.uriToBase64(Uri.fromFile(new File(path)), mActivity.getContentResolver()));
        } else {
            bitmap = BitmapUtils.getBitmapImage(mActivity,
                    BitmapUtils.uriToBase64(uri, mActivity.getContentResolver()));
        }
        return bitmap;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public String getDocPath(Uri uri) {
        String wholeID = DocumentsContract.getDocumentId(uri);
        String id = wholeID.split(":")[1];
        String[] column = {MediaStore.Images.Media.DATA};
        String sel = MediaStore.Images.Media._ID + "=?";
        Cursor cursor = mActivity.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel,
                new String[]{id}, null);
        String filePath = null;
        int columnIndex = cursor.getColumnIndex(column[0]);
        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    public boolean atLeastKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public void requestForFile(final RequestType type) {
        if (devicePermissionHelper.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && devicePermissionHelper.hasPermission(Manifest.permission.CAMERA)) {
            _requestForFile(type);
        } else {
            Log.w(TAG, "No permission for CAMERA or WRITE_EXTERNAL_STORAGE");
            devicePermissionHelper.requestPermissions(new DevicePermissionHelper
                    .PermissionGrantListener() {
                @Override
                public void onPermissionGranted() {
                    _requestForFile(type);
                }

                @Override
                public void onPermissionDenied() {
                    Toast.makeText(mActivity, R.string.toast_permission_download_storage,
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onPermissionRationale() {
                    Toast.makeText(mActivity, R.string.toast_permission_download_storage_help,
                            Toast.LENGTH_LONG).show();
                }
            }, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA});
        }
    }

    public void _requestForFile(RequestType type) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        switch (type) {
            case AUDIO:
                intent.setType("audio/*");
                requestIntent(intent, REQUEST_AUDIO);
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
            case CAPTURE_HIGH_IMAGE:
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "Odoo Mobile Attachment");
                values.put(MediaStore.Images.Media.DESCRIPTION,
                        "Captured from Odoo Mobile App");
                newImageUri = mActivity.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, newImageUri);
                requestIntent(intent, type == RequestType.CAPTURE_IMAGE ?
                        REQUEST_CAMERA : REQUEST_HIGH_CAMERA);
                break;
            case IMAGE_OR_CAPTURE_IMAGE:
            case IMAGE_OR_CAPTURE_HIGH_IMAGE:
                requestDialog(type);
                break;
            case FILE:
                intent.setType("application/*");
                requestIntent(intent, REQUEST_FILE);
                break;
            case ALL_FILE_TYPE:
                intent.setType("*/*");
                requestIntent(intent, REQUEST_ALL_FILE);
                break;
        }
    }

    public OValues getURIDetails(Uri uri) {
        OValues values = new OValues();
        ContentResolver mCR = mActivity.getContentResolver();
        if (uri.getScheme().equals("content")) {
            Cursor cr = mCR.query(uri, null, null, null, null);
            int nameIndex = cr.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int fileSize = cr.getColumnIndex(OpenableColumns.SIZE);
            if (cr.moveToFirst()) {
                values.put("name", cr.getString(nameIndex));
                values.put("datas_fname", values.get("name"));
                values.put("file_size", Long.toString(cr.getLong(fileSize)));
                String path = getPath(uri);
                if (path != null) {
                    values.put("file_size", new File(path).length() + "");
                }
            }
            cr.close();
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
        return values;
    }

    public String getPath(Uri uri) {
        ContentResolver mCR = mActivity.getContentResolver();
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = mCR.query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s = cursor.getString(column_index);
        cursor.close();
        return s;
    }

    public OValues handleResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA:
                case REQUEST_HIGH_CAMERA:
                    OValues values = getURIDetails(newImageUri);
                    values.put("datas", BitmapUtils.uriToBase64(newImageUri,
                            mActivity.getContentResolver(), requestCode == REQUEST_CAMERA));
                    return values;
                case REQUEST_IMAGE:
                    values = getURIDetails(data.getData());
                    values.put("datas", BitmapUtils.uriToBase64(data.getData(),
                            mActivity.getContentResolver(), true));
                    return values;
                case REQUEST_ALL_FILE:
                default:
                    return getURIDetails(data.getData());
            }
        }
        return null;
    }

    private void requestIntent(Intent intent, int requestCode) {
        try {
            mActivity.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            makeText(mActivity, "No Activity Found to handle request",
                    LENGTH_SHORT).show();
        }
    }

    private void requestDialog(RequestType type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        switch (type) {
            case IMAGE_OR_CAPTURE_IMAGE:
            case IMAGE_OR_CAPTURE_HIGH_IMAGE:
                requestType = type;
                mOptions = new String[]{"Select Image", "Capture Image"};
                break;
        }
        builder.setSingleChoiceItems(mOptions, -1, this);
        builder.create().show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (requestType) {
            case IMAGE_OR_CAPTURE_IMAGE:
            case IMAGE_OR_CAPTURE_HIGH_IMAGE:
                RequestType captureType = requestType == RequestType.IMAGE_OR_CAPTURE_IMAGE
                        ? RequestType.CAPTURE_IMAGE : RequestType.CAPTURE_HIGH_IMAGE;
                requestForFile((which == 0) ? RequestType.IMAGE : captureType);
                break;
        }
        dialog.dismiss();
    }

}
