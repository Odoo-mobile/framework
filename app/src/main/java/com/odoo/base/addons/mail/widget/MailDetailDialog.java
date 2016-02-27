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
 * Created on 25/2/15 6:26 PM
 */
package com.odoo.base.addons.mail.widget;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.odoo.R;
import com.odoo.base.addons.ir.feature.OFileManager;
import com.odoo.base.addons.mail.MailMessage;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.support.OdooCompatActivity;
import com.odoo.core.utils.BitmapUtils;
import com.odoo.core.utils.IntentUtils;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.ODateUtils;
import com.odoo.core.utils.OStringColorUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MailDetailDialog extends OdooCompatActivity implements View.OnClickListener {
    public static final String TAG = MailDetailDialog.class.getSimpleName();
    private Bundle extra;
    private MailMessage mailMessage;
    private OModel baseModel;
    private TextView recordName;
    private View parent;
    private List<ODataRow> attachments = new ArrayList<>();
    private LoadAttachments loadAttachments = null;
    private LinearLayout horizontalScrollView;
    private OFileManager fileManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.base_mail_chatter_message_detail);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fileManager = new OFileManager(this);
        mailMessage = new MailMessage(this, null);
        extra = getIntent().getExtras();
        findViewById(R.id.btnClose).setOnClickListener(this);
        findViewById(R.id.btnReply).setOnClickListener(this);
        init();
    }

    private void init() {
        recordName = (TextView) findViewById(R.id.recordName);
        parent = (View) recordName.getParent();
        ODataRow row = mailMessage.browse(extra.getInt(OColumn.ROW_ID));
        attachments.addAll(row.getM2MRecord("attachment_ids").browseEach());
        if (attachments.size() > 0) {
            loadAttachments = new LoadAttachments();
            loadAttachments.execute();
        }
        horizontalScrollView = (LinearLayout) findViewById(R.id.attachmentsList);
        baseModel = OModel.get(this, row.getString("model"), mailMessage.getUser().getAndroidName());
        ODataRow record = baseModel.browse(baseModel.selectRowId(row.getInt("res_id")));
        String name = record.getString(baseModel.getDefaultNameColumn());
        recordName.setText(name);
        recordName.setBackgroundColor(OStringColorUtil.getStringColor(this, name));

        if (!row.getString("subject").equals("false"))
            OControls.setText(parent, R.id.messageSubject, row.getString("subject"));
        else
            OControls.setGone(parent, R.id.messageSubject);

        WebView messageBody = (WebView) findViewById(R.id.messageBody);
        messageBody.setBackgroundColor(Color.TRANSPARENT);
        messageBody.loadData(row.getString("body"), "text/html; charset=UTF-8", "UTF-8");

        Bitmap author_image = BitmapUtils.getAlphabetImage(this, row.getString("author_name"));
        String author_img = mailMessage.getAuthorImage(row.getInt(OColumn.ROW_ID));
        if (!author_img.equals("false")) {
            author_image = BitmapUtils.getBitmapImage(this, author_img);
        }
        OControls.setImage(parent, R.id.author_image, author_image);
        OControls.setText(parent, R.id.authorName, row.getString("author_name"));
        String date = ODateUtils.convertToDefault(row.getString("date"),
                ODateUtils.DEFAULT_FORMAT, "MMM dd, yyyy hh:mm a");
        OControls.setText(parent, R.id.messageDate, date);
    }

    private class LoadAttachments extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (ODataRow row : attachments) {
                        addAttachment(row);
                    }
                }
            });
            return null;
        }
    }

    private void addAttachment(ODataRow values) {
        View attachmentView = LayoutInflater.from(this)
                .inflate(R.layout.base_attachment_item, horizontalScrollView, false);
        String fileName = values.getString("name");
        String type = values.getString("file_type");
        ImageView imgPreview = (ImageView) attachmentView.findViewById(R.id.attachmentPreview);
        if (type.contains("image")) {
            if (!values.getString("file_uri").equals("false")) {
                Uri uri = Uri.parse(new File(values.getString("file_uri")).toString());
                imgPreview.setImageBitmap(fileManager.getBitmapFromURI(uri));
            } else
                imgPreview.setImageResource(R.drawable.image);
        } else if (type.contains("audio")) {
            imgPreview.setImageResource(R.drawable.audio);
        } else if (type.contains("video")) {
            imgPreview.setImageResource(R.drawable.video);
        } else {
            imgPreview.setImageResource(R.drawable.file);
        }
        OControls.setText(attachmentView, R.id.attachmentFileName, fileName);
        attachmentView.setTag(values);
        attachmentView.findViewById(R.id.btnRemoveAttachment).setVisibility(View.GONE);
        attachmentView.setOnClickListener(this);
        horizontalScrollView.addView(attachmentView);
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() != null) {
            ODataRow attachment = (ODataRow) v.getTag();
            fileManager.downloadAttachment(attachment.getInt(OColumn.ROW_ID));
        } else {
            switch (v.getId()) {
                case R.id.btnClose:
                    finish();
                    break;
                case R.id.btnReply:
                    extra.putString("type", MailChatterCompose.MessageType.Message.toString());
                    IntentUtils.startActivity(this, MailChatterCompose.class, extra);
                    finish();
                    break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (loadAttachments != null) {
            loadAttachments.cancel(true);
        }
    }
}
