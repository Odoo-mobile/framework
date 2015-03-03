package com.odoo.core.utils;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class OControls {

    public static void setText(View parent_view, int textview_id, Object value) {
        TextView textView = (TextView) parent_view.findViewById(textview_id);
        if (value instanceof String || value instanceof CharSequence)
            textView.setText(value.toString());
        if (value instanceof Integer)
            textView.setText(Integer.parseInt(value.toString()));
    }

    public static String getText(View parent_view, int textview_id) {
        TextView textView = (TextView) parent_view.findViewById(textview_id);
        return textView.getText().toString();
    }

    public static void toggleViewVisibility(View parent_view, int view_id,
                                            Boolean visible) {
        int view_visibility = (visible) ? View.VISIBLE : View.GONE;
        parent_view.findViewById(view_id).setVisibility(view_visibility);

    }

    public static void setImage(View parent_view, int imageview_id,
                                Bitmap bitmap) {
        ImageView imgView = (ImageView) parent_view.findViewById(imageview_id);
        imgView.setImageBitmap(bitmap);
    }

    public static void setImage(View parent_view, int imageview_id,
                                int drawable_id) {
        ImageView imgView = (ImageView) parent_view.findViewById(imageview_id);
        imgView.setImageResource(drawable_id);
    }

    public static void setVisible(View parent_view, int resource_id) {
        View view = parent_view.findViewById(resource_id);
        view.setVisibility(View.VISIBLE);
    }


    public static void setInvisible(View parent_view, int resource_id) {
        parent_view.findViewById(resource_id).setVisibility(View.INVISIBLE);
    }

    public static void setGone(View parent_view, int resource_id) {
        View view = parent_view.findViewById(resource_id);
        view.setVisibility(View.GONE);
    }

    public static void setTextViewStrikeThrough(View parent, int res_id) {
        TextView tv = (TextView) parent.findViewById(res_id);
        tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }


    public static void setTextColor(View parent, int txv_id, int color) {
        TextView tv = (TextView) parent.findViewById(txv_id);
        tv.setTextColor(color);
    }

}
