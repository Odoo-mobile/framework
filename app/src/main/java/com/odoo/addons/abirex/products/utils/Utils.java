package com.odoo.addons.abirex.products.utils;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.DrawableRes;

import com.odoo.App;

public class Utils {
    public static Bitmap convertDrawableResToBitmap(@DrawableRes int drawableId, Integer width, Integer height) {
        Drawable d = App.getContext().getResources().getDrawable(drawableId);

        if (d instanceof BitmapDrawable) {
            return ((BitmapDrawable) d).getBitmap();
        }

        if (d instanceof GradientDrawable) {
            GradientDrawable g = (GradientDrawable) d;

            int w = d.getIntrinsicWidth() > 0 ? d.getIntrinsicWidth() : width;
            int h = d.getIntrinsicHeight() > 0 ? d.getIntrinsicHeight() : height;

            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            g.setBounds(0, 0, w, h);
            g.setStroke(1, Color.BLACK);
            g.setFilterBitmap(true);
            g.draw(canvas);
            return bitmap;
        }

        Bitmap bit = BitmapFactory.decodeResource(App.getContext().getResources(), drawableId);
        return bit.copy(Bitmap.Config.ARGB_8888, true);
    }


}
