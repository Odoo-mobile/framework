package com.openerp.util;

import android.text.Html;
import android.text.Spanned;

public class HTMLHelper {
    public static String htmlToString(String html) {

	return Html.fromHtml(
		html.replaceAll("\\<.*?\\>", "").replaceAll("\n", ""))
		.toString();
    }

    public static Spanned stringToHtml(String string) {
	return Html.fromHtml(string);
    }
}
