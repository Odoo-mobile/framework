/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http://www.openerp.com>)
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * 
 */
package com.openerp.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.util.Base64;

// TODO: Auto-generated Javadoc
/**
 * The Class Base64Helper.
 */
public class Base64Helper {

	/**
	 * File uri to base64.
	 * 
	 * @param uri
	 *            the uri
	 * @param resolver
	 *            the resolver
	 * @return the string
	 */
	public static String fileUriToBase64(Uri uri, ContentResolver resolver) {
		String encodedBase64 = "";
		try {
			byte[] bytes = readBytes(uri, resolver);
			encodedBase64 = Base64.encodeToString(bytes, 0);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return encodedBase64;
	}

	/**
	 * Read bytes.
	 * 
	 * @param uri
	 *            the uri
	 * @param resolver
	 *            the resolver
	 * @return the byte[]
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private static byte[] readBytes(Uri uri, ContentResolver resolver)
			throws IOException {
		// this dynamically extends to take the bytes you read
		InputStream inputStream = resolver.openInputStream(uri);
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

		// this is storage overwritten on each iteration with bytes
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];

		// we need to know how may bytes were read to write them to the
		// byteBuffer
		int len = 0;
		while ((len = inputStream.read(buffer)) != -1) {
			byteBuffer.write(buffer, 0, len);
		}

		// and then we can return your byte array.
		return byteBuffer.toByteArray();
	}

	/**
	 * Gets the bitmap image.
	 * 
	 * @param context
	 *            the context
	 * @param base64
	 *            the base64
	 * @return the bitmap image
	 */
	public static Bitmap getBitmapImage(Context context, String base64) {

		String imagestring = base64;
		byte[] imageAsBytes = Base64.decode(imagestring.getBytes(), 5);
		return BitmapFactory.decodeByteArray(imageAsBytes, 0,
				imageAsBytes.length);

	}

	/**
	 * Gets the rounded corner bitmap.
	 * 
	 * @param bitmap
	 *            the bitmap
	 * @return the rounded corner bitmap
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = 12;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}
}
