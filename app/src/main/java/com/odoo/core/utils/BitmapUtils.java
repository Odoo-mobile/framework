/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p>
 * Created on 19/12/14 11:42 AM
 */
package com.odoo.core.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.text.TextPaint;
import android.util.Base64;

import com.odoo.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import odoo.controls.OControlHelper;

public class BitmapUtils {
    public static final int THUMBNAIL_SIZE = 500;

    /**
     * Read bytes.
     *
     * @param uri      the uri
     * @param resolver the resolver
     * @return the byte[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static byte[] readBytes(Uri uri, ContentResolver resolver, boolean thumbnail)
            throws IOException {
        // this dynamically extends to take the bytes you read
        InputStream inputStream = resolver.openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        if (!thumbnail) {
            // this is storage overwritten on each iteration with bytes
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            // we need to know how may bytes were read to write them to the
            // byteBuffer
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
        } else {
            Bitmap imageBitmap = BitmapFactory.decodeStream(inputStream);
            int thumb_width = imageBitmap.getWidth() / 2;
            int thumb_height = imageBitmap.getHeight() / 2;
            if (thumb_width > THUMBNAIL_SIZE) {
                thumb_width = THUMBNAIL_SIZE;
            }
            if (thumb_width == THUMBNAIL_SIZE) {
                thumb_height = ((imageBitmap.getHeight() / 2) * THUMBNAIL_SIZE)
                        / (imageBitmap.getWidth() / 2);
            }
            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, thumb_width, thumb_height, false);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteBuffer);
        }
        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }

    public static String uriToBase64(Uri uri, ContentResolver resolver) {
        return uriToBase64(uri, resolver, false);
    }

    public static String uriToBase64(Uri uri, ContentResolver resolver, boolean thumbnail) {
        String encodedBase64 = "";
        try {
            byte[] bytes = readBytes(uri, resolver, thumbnail);
            encodedBase64 = Base64.encodeToString(bytes, 0);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return encodedBase64;
    }

    /**
     * Gets the bitmap image.
     *
     * @param context the context
     * @param base64  the base64
     * @return the bitmap image
     */
    public static Bitmap getBitmapImage(Context context, String base64) {
        byte[] imageAsBytes = Base64.decode(base64.getBytes(), 5);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0,
                imageAsBytes.length);

    }

    public static Bitmap getAlphabetImage(Context context, String content) {
        Resources res = context.getResources();
        Bitmap mDefaultBitmap = BitmapFactory.decodeResource(res, android.R.drawable.sym_def_app_icon);
        int width = mDefaultBitmap.getWidth();
        int height = mDefaultBitmap.getHeight();
        TextPaint mPaint = new TextPaint();
        mPaint.setTypeface(OControlHelper.boldFont());
        mPaint.setColor(Color.WHITE);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setAntiAlias(true);
        int textSize = res.getDimensionPixelSize(R.dimen.text_size_xxlarge);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        Rect mBounds = new Rect();
        canvas.setBitmap(bitmap);
        canvas.drawColor(OStringColorUtil.getStringColor(context, content));
        if (content == null || content.trim().length() == 0) {
            content = "?";
        }
        char[] alphabet = {Character.toUpperCase(content.trim().charAt(0))};
        mPaint.setTextSize(textSize);
        mPaint.getTextBounds(alphabet, 0, 1, mBounds);
        canvas.drawText(alphabet, 0, 1, width / 2,
                height / 2 + (mBounds.bottom - mBounds.top) / 2, mPaint);
        return bitmap;
    }
}
