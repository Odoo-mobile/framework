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
 * Created on 15/1/15 4:52 PM
 */
package com.odoo.addons.customers.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.odoo.core.orm.ODataRow;
import com.odoo.core.utils.OStorageUtils;

import java.io.File;
import java.io.FileWriter;

public class ShareUtil {
    public static final String TAG = ShareUtil.class.getSimpleName();

    public static void shareContact(Context context, ODataRow row, Boolean view) {
        try {
            File vcfFile = new File(OStorageUtils.getDirectoryPath("file"), row.getString("name") + ".vcf");
            FileWriter fw = new FileWriter(vcfFile);
            fw.write("BEGIN:VCARD\r\n");
            fw.write("VERSION:3.0\r\n");
            fw.write("N:" + row.getString("name") + ";\r\n");
            fw.write("FN:" + row.getString("name") + "\r\n");
            if (row.get("parent_id") instanceof Integer) {
                fw.write("ORG:" + row.getM2ORecord("parent_id").browse().getString("name") + "\r\n");
            }
            if (!row.getString("phone").equals("false"))
                fw.write("TEL;TYPE=WORK,VOICE:" + row.getString("phone") + "\r\n");
            if (!row.getString("mobile").equals("false"))
                fw.write("TEL;TYPE=HOME,VOICE:" + row.getString("mobile") + "\r\n");
            String country = "";
            if (row.get("country_id") instanceof Integer) {
                country = row.getM2ORecord("country_id").browse().getString("name");
            }
//            if (!row.getString("street").equals("false") && !row.getString("street").equals("")) {
//            fw.write("ADR;TYPE=WORK:;;" + row.getString("street"));
            fw.write("ADR;TYPE=WORK:;;" + row.getString("street") + " " + row.getString("street2") + ";" +
                    row.getString("city") + ";" + row.getString("zip") + ";" + country + "\r\n");
//            }
//            if (!row.getString("street2").equals("false") && !row.getString("street2").equals(""))
//                fw.write("ADR;TYPE=WORK:;;" + " " + row.getString("street2"));
//            if (!row.getString("city").equals("false") && !row.getString("city").equals(""))
//                fw.write("ADR;TYPE=WORK:;;" + " " + row.getString("city"));
//            if (!row.getString("zip").equals("false") && !row.getString("zip").equals(""))
//                fw.write("ADR;TYPE=WORK:;;" + " " + row.getString("zip") + ";" + country);

//            if (!row.getString("email").equals("false") && !row.getString("email").equals(""))
            fw.write("EMAIL;TYPE=PREF,INTERNET:" + row.getString("email") + "\r\n");
            fw.write("END:VCARD\r\n");
            fw.close();

            Intent i = new Intent();
            if (view) {
                i.setAction(Intent.ACTION_SEND);
                i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(vcfFile));
                i.setType("text/x-vcard");
            } else {
                i.setAction(Intent.ACTION_VIEW);
                i.setDataAndType(Uri.fromFile(vcfFile), "text/x-vcard");
            }
            context.startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
