/**
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
 * Created on 16/1/15 3:36 PM
 */
package com.odoo.base.addons.ir.feature;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import com.odoo.core.orm.OValues;
import com.odoo.core.utils.BitmapUtils;

import java.io.File;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class OFileManager implements DialogInterface.OnClickListener {
    public static final String TAG = OFileManager.class.getSimpleName();
    public static final int REQUEST_CAMERA = 111;
    public static final int REQUEST_IMAGE = 112;
    public static final int REQUEST_AUDIO = 113;
    public static final int REQUEST_FILE = 114;
    private static final int SINGLE_ATTACHMENT_STREAM = 115;
    private static final long IMAGE_MAX_SIZE = 1000000; // 1 MB
    private Context mContext = null;
    private String[] mOptions = null;
    private RequestType requestType = null;
    private Uri newImageUri = null;

    public enum RequestType {
        CAPTURE_IMAGE, IMAGE, IMAGE_OR_CAPTURE_IMAGE, AUDIO, FILE, OTHER
    }

    public OFileManager(Context context) {
        mContext = context;
    }


    public void requestForFile(RequestType type) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        switch (type) {
            case AUDIO:
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
            case IMAGE_OR_CAPTURE_IMAGE:
                requestDialog(type);
                break;
            case FILE:
                break;
            case OTHER:
                break;
        }
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
                String path = getPath(uri);
                if (path != null) {
                    values.put("file_size", new File(path).length() + "");
                }
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
        return values;
    }

    public String getPath(Uri uri) {
        ContentResolver mCR = mContext.getContentResolver();
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
                    OValues values = getURIDetails(newImageUri);
                    values.put("datas", BitmapUtils.uriToBase64(newImageUri,
                            mContext.getContentResolver(), true));
                    return values;
                case REQUEST_IMAGE:
                    values = getURIDetails(data.getData());
                    values.put("datas", BitmapUtils.uriToBase64(data.getData(),
                            mContext.getContentResolver(), true));
                    return values;
            }
        }
        return null;
    }

    private void requestIntent(Intent intent, int requestCode) {
        try {
            ((Activity) mContext).startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            makeText(mContext, "No Activity Found to handle request",
                    LENGTH_SHORT).show();
        }
    }

    private void requestDialog(RequestType type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        switch (type) {
            case IMAGE_OR_CAPTURE_IMAGE:
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
                requestForFile((which == 0) ? RequestType.IMAGE : RequestType.CAPTURE_IMAGE);
                break;
        }
        dialog.dismiss();
    }

}
