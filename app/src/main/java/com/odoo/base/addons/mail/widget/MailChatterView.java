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
 * Created on 25/2/15 12:07 PM
 */
package com.odoo.base.addons.mail.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.odoo.App;
import com.odoo.R;
import com.odoo.base.addons.mail.MailMessage;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.utils.BitmapUtils;
import com.odoo.core.utils.IntentUtils;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.OCursorUtils;
import com.odoo.core.utils.ODateUtils;
import com.odoo.core.utils.OResource;
import com.odoo.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import odoo.controls.ExpandableListControl;
import com.odoo.core.rpc.helper.ODomain;

public class MailChatterView extends LinearLayout implements
        ExpandableListControl.ExpandableListAdapterGetViewListener, View.OnClickListener {
    public static final String TAG = MailChatterView.class.getSimpleName();
    private Context mContext;
    private String modelName = null;
    private int record_server_id = 0;
    private View mChatterCardView;
    private OModel mModel;
    private ExpandableListControl mChatterListView;
    private ExpandableListControl.ExpandableListAdapter mListAdapter;
    private List<Object> chatterItems = new ArrayList<>();
    private MailMessage mailMessage;
    private ChatterMessagesLoader messagesLoader;
    private App app;
    private Boolean loadAllMessages = false;
    private boolean isExecuting = false;

    public MailChatterView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public MailChatterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public MailChatterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mContext = context;
        app = (App) mContext.getApplicationContext();
        if (attrs != null) {
            TypedArray types = mContext.obtainStyledAttributes(attrs,
                    R.styleable.MailChatterView);
            modelName = types.getString(R.styleable.MailChatterView_resModelName);
            types.recycle();
        }
        setOrientation(VERTICAL);
        mailMessage = new MailMessage(context, null);
        mContext.registerReceiver(dataChangeReceiver, new IntentFilter("mail.message.update"));
    }

    public void generateView() {
        Log.v(TAG, "Generating View for Mail Chatter");
        removeAllViews();
        mChatterCardView = LayoutInflater.from(mContext).inflate(R.layout.base_mail_chatter, this, false);
        addView(mChatterCardView);
        findViewById(R.id.chatterSendMessage).setOnClickListener(this);
        findViewById(R.id.chatterLogInternalNote).setOnClickListener(this);
        if (modelName != null) {
            mModel = OModel.get(mContext, modelName, null);
            if (!mModel.hasMailChatter()) {
                removeAllViews();
            } else {
                if (record_server_id > 0) {
                    getMessages();
                }
            }
        } else {
            removeAllViews();
        }
    }

    private void getMessages() {
        mChatterListView = (ExpandableListControl) findViewById(R.id.chatterMessages);
        mListAdapter = mChatterListView.getAdapter(
                R.layout.base_mail_chatter_item, chatterItems, this);
        mListAdapter.notifyDataSetChanged(chatterItems);

        // Check for server updated messages
        if (app.inNetwork()) {
            messagesLoader = new ChatterMessagesLoader();
            messagesLoader.execute();
        }
        // Updating chatter messages
        updateChatterList();
    }

    private void updateChatterList() {
        // Getting local messages
        if (modelName != null) {
            chatterItems.clear();
            Cursor cr = mContext.getContentResolver().query(mailMessage.uri(),
                    null, "model = ? and res_id = ?",
                    new String[]{modelName, record_server_id + ""}, "date desc");
            if (cr.moveToFirst()) {
                int limit = (loadAllMessages) ? cr.getCount()
                        : (cr.getCount() > 3) ? 3 : cr.getCount();
                for (int i = 0; i < limit; i++) {
                    ODataRow row = OCursorUtils.toDatarow(cr);
                    chatterItems.add(row);
                    cr.moveToNext();
                }
            }
            TextView loadMore = (TextView) findViewById(R.id.chatterLoadMoreMessages);
            if (cr.getCount() > 3 && !loadAllMessages) {
                loadMore.setVisibility(View.VISIBLE);
                loadMore.setOnClickListener(this);
            } else {
                loadMore.setVisibility(View.GONE);
            }
            mListAdapter.notifyDataSetChanged(chatterItems);
            if (chatterItems.isEmpty()) {
                loadMore.setVisibility(View.VISIBLE);
                loadMore.setText("No messages !");
            }
        }
    }

    public void setModelName(String model) {
        modelName = model;
    }

    public void setRecordServerId(int record_server_id) {
        this.record_server_id = record_server_id;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ODataRow row = (ODataRow) chatterItems.get(position);
        if (row.getString("subtype_id").equals("false")) {
            view.setBackgroundResource(R.color.base_chatter_view_note_background);
        } else {
            view.setBackgroundColor(Color.WHITE);
        }
        view.findViewById(R.id.imgAttachments).setVisibility(
                (row.getBoolean("has_attachments")) ?
                        View.VISIBLE :
                        View.GONE
        );

        if (row.getString("subject").equals("false")) {
            OControls.setGone(view, R.id.chatterSubject);
        } else {
            OControls.setVisible(view, R.id.chatterSubject);
            OControls.setText(view, R.id.chatterSubject, row.getString("subject"));
        }
        String date = ODateUtils.convertToDefault(row.getString("date"),
                ODateUtils.DEFAULT_FORMAT, "MMM dd hh:mm a");
        OControls.setText(view, R.id.chatterDate, date);
        OControls.setText(view, R.id.chatterBody, StringUtils.htmlToString(row.getString("body")));
        OControls.setText(view, R.id.chatterAuthor, row.getString("author_name"));
        String author_image = mailMessage.getAuthorImage(row.getInt(OColumn.ROW_ID));
        if (!author_image.equals("false")) {
            Bitmap author = BitmapUtils.getBitmapImage(mContext, author_image);
            OControls.setImage(view, R.id.authorImage, author);
        } else {
            OControls.setImage(view, R.id.authorImage, R.drawable.avatar);
        }

        view.setTag(row);
        view.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        Bundle extra = new Bundle();
        extra.putString("model", mModel.getModelName());
        extra.putInt("server_id", record_server_id);
        switch (v.getId()) {
            case R.id.chatterSendMessage:
                if (app.inNetwork()) {
                    extra.putString("type", MailChatterCompose.MessageType.Message.toString());
                    IntentUtils.startActivity(mContext, MailChatterCompose.class, extra);
                } else {
                    Toast.makeText(mContext, OResource.string(mContext,
                            R.string.toast_network_required), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.chatterLogInternalNote:
                if (app.inNetwork()) {
                    extra.putString("type", MailChatterCompose.MessageType.InternalNote.toString());
                    IntentUtils.startActivity(mContext, MailChatterCompose.class, extra);
                } else {
                    Toast.makeText(mContext, OResource.string(mContext,
                            R.string.toast_network_required), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.chatterLoadMoreMessages:
                loadAllMessages = true;
                updateChatterList();
                break;
            default:
                ODataRow row = (ODataRow) v.getTag();
                extra.putAll(row.getPrimaryBundleData());
                if (row != null) {
                    IntentUtils.startActivity(mContext, MailDetailDialog.class, extra);
                }
                break;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (messagesLoader != null)
            messagesLoader.cancel(true);
        mContext.unregisterReceiver(dataChangeReceiver);
    }

    private class ChatterMessagesLoader extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.chatterProgress).setVisibility(View.VISIBLE);
            findViewById(R.id.chatterOr).setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(500);
                ODomain domain = new ODomain();
                domain.add("model", "=", modelName);
                domain.add("res_id", "=", record_server_id);
                List<Integer> serverIds = mailMessage.getServerIds(modelName, record_server_id);
                if (serverIds.size() > 0) {
                    domain.add("id", "not in", serverIds);
                }
                mailMessage.quickSyncRecords(domain);
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            findViewById(R.id.chatterProgress).setVisibility(View.GONE);
            findViewById(R.id.chatterOr).setVisibility(View.VISIBLE);
            updateChatterList();
            isExecuting = false;
        }
    }

    private BroadcastReceiver dataChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isExecuting) {
                if (messagesLoader != null)
                    messagesLoader.cancel(true);
                messagesLoader = new ChatterMessagesLoader();
                messagesLoader.execute();
                isExecuting = true;
            }
        }
    };
}
