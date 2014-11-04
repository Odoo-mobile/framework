package com.odoo.util;

import android.graphics.Bitmap;

public class BitmapUtils {
	public static Bitmap replaceColor(Bitmap src, int fromColor, int targetColor) {
		if (src == null) {
			return null;
		}
		// Source image size
		int width = src.getWidth();
		int height = src.getHeight();
		int[] pixels = new int[width * height];
		// get pixels
		src.getPixels(pixels, 0, width, 0, 0, width, height);

		for (int x = 0; x < pixels.length; ++x) {
			pixels[x] = (pixels[x] == fromColor) ? targetColor : pixels[x];
		}
		// create result bitmap output
		Bitmap result = Bitmap.createBitmap(width, height, src.getConfig());
		// set pixels
		result.setPixels(pixels, 0, width, 0, 0, width, height);

		return result;
	}
}
