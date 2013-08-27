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

public class Base64Helper {
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

    public static Bitmap getBitmapImage(Context context, String base64) {

	String imagestring = base64;
	byte[] imageAsBytes = Base64.decode(imagestring.getBytes(), 5);
	return BitmapFactory.decodeByteArray(imageAsBytes, 0,
		imageAsBytes.length);

    }

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
