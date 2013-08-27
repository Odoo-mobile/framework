/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http:www.openerp.com>)
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
 */
package com.openerp.addons.messages;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView.Tokenizer;
import android.widget.Toast;

import com.openerp.MainActivity;
import com.openerp.R;
import com.openerp.base.res.Res_PartnerDBHelper;
import com.openerp.support.listview.OEListViewAdapter;
import com.openerp.support.listview.OEListViewRows;
import com.openerp.util.ChipsMultiAutoCompleteTextview;

public class MessageComposeActivty extends Activity {
    private static final int PICKFILE_RESULT_CODE = 1;
    ChipsMultiAutoCompleteTextview multiComplete;
    List<Uri> file_uris = new ArrayList<Uri>();
    ListView lstAttachments = null;
    List<OEListViewRows> attachments = new ArrayList<OEListViewRows>();
    OEListViewAdapter lstAttachmentAdapter = null;
    List<OEListViewRows> partners_list = new ArrayList<OEListViewRows>();

    enum ATTACHMENT_TYPE {
	IMAGE, TEXT_FILE
    }

    EnumMap<ATTACHMENT_TYPE, String> attachments_type = new EnumMap<MessageComposeActivty.ATTACHMENT_TYPE, String>(
	    ATTACHMENT_TYPE.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_message_compose);
	getActionBar().setHomeButtonEnabled(true);
	getActionBar().setTitle("Compose");
	getActionBar().setDisplayHomeAsUpEnabled(true);

	lstAttachments = (ListView) findViewById(R.id.lstAttachments);
	String[] from = new String[] { "name" };
	int[] to = new int[] { R.id.txvFileName };
	lstAttachmentAdapter = new OEListViewAdapter(MainActivity.context,
		R.layout.message_attachment_listview_item, attachments, from,
		to, null);
	lstAttachments.setAdapter(lstAttachmentAdapter);

	multiComplete = (ChipsMultiAutoCompleteTextview) findViewById(R.id.edtMessageTo);

	Res_PartnerDBHelper partners = new Res_PartnerDBHelper(this);
	HashMap<String, Object> data = partners.search(partners);
	if ((Integer) data.get("total") > 0) {
	    for (HashMap<String, Object> row : (List<HashMap<String, Object>>) data
		    .get("records")) {
		OEListViewRows newRow = new OEListViewRows(Integer.parseInt(row
			.get("id").toString()), row);
		partners_list.add(newRow);
	    }
	}

	from = new String[] { "id", "name", "email", "image" };
	to = new int[] { R.id.txvMultiId, R.id.txvMultiName,
		R.id.txvMultiEmail, R.id.imgUserPic };

	// multiComplete.setAdapter(new MultiAutoCompleteTextViewCustomeAdapter(
	// this, partners_list,
	// R.layout.multi_select_textview_custom_layout, from, to));
	OEListViewAdapter adapter = new OEListViewAdapter(MainActivity.context,
		R.layout.multi_select_textview_custom_layout, partners_list,
		from, to, partners);
	multiComplete.setAdapter(adapter);
	adapter.addImageColumn("image");
	multiComplete.setTokenizer(new Tokenizer() {

	    @Override
	    public CharSequence terminateToken(CharSequence text) {
		// TODO Auto-generated method stub
		int i = text.length();
		while (i > 0 && text.charAt(i - 1) == ' ') {
		    i--;
		}

		if (i > 0 && text.charAt(i - 1) == ' ') {
		    return text;
		} else {
		    if (text instanceof Spanned) {
			SpannableString sp = new SpannableString(text + " ");
			TextUtils.copySpansFrom((Spanned) text, 0,
				text.length(), Object.class, sp, 0);
			return sp;
		    } else {
			return text + " ";
		    }
		}
	    }

	    @Override
	    public int findTokenStart(CharSequence text, int cursor) {
		// TODO Auto-generated method stub
		int i = cursor;

		while (i > 0 && text.charAt(i - 1) != ' ') {
		    i--;
		}
		while (i < cursor && text.charAt(i) == ' ') {
		    i++;
		}

		return i;
	    }

	    @Override
	    public int findTokenEnd(CharSequence text, int cursor) {
		// TODO Auto-generated method stub
		int i = cursor;
		int len = text.length();

		while (i < len) {
		    if (text.charAt(i) == ' ') {
			return i;
		    } else {
			i++;
		    }
		}

		return len;
	    }
	});
	handleIntentFilter(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.menu_message_compose_activty, menu);
	return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {

	case android.R.id.home:
	    // app icon in action bar clicked; go home
	    finish();
	    return true;
	case R.id.menu_message_compose_add_attachment_images:
	    requestForAttachmentIntent(ATTACHMENT_TYPE.IMAGE);
	    return true;
	case R.id.menu_message_compose_add_attachment_files:
	    requestForAttachmentIntent(ATTACHMENT_TYPE.TEXT_FILE);
	    return true;
	case R.id.menu_message_compose_send:
	    Log.e(">>SElected partners ", multiComplete.getSelectedIds()
		    .toString());
	    Toast.makeText(this, "Sending message...", Toast.LENGTH_LONG)
		    .show();
	    return true;
	default:
	    return super.onOptionsItemSelected(item);
	}
    }

    /**
     * Handle message intent filter for attachments
     * 
     * @param intent
     */
    private void handleIntentFilter(Intent intent) {
	attachments_type.put(ATTACHMENT_TYPE.IMAGE, "image/*");
	attachments_type.put(ATTACHMENT_TYPE.TEXT_FILE, "application/*");

	String action = intent.getAction();
	String type = intent.getType();

	// Single attachment
	if (Intent.ACTION_SEND.equals(action) && type != null) {
	    Uri fileUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
	    file_uris.add(fileUri);
	    handleReceivedFile();
	}

	// Multiple Attachments
	if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
	    ArrayList<Uri> fileUris = intent
		    .getParcelableArrayListExtra(Intent.EXTRA_STREAM);
	    file_uris.addAll(fileUris);
	    handleReceivedFile();

	}

    }

    /**
     * getting real path from attachment URI.
     * 
     * @param contentUri
     * @return
     */
    private String getRealPathFromURI(Uri contentUri) {
	String[] proj = { MediaStore.Images.Media.DATA };
	CursorLoader loader = new CursorLoader(this, contentUri, proj, null,
		null, null);
	Cursor cursor = loader.loadInBackground();
	int column_index = cursor
		.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	cursor.moveToFirst();
	return cursor.getString(column_index);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onActivityResult(int, int,
     * android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	switch (requestCode) {
	case PICKFILE_RESULT_CODE:
	    if (resultCode == RESULT_OK) {
		String FilePath = data.getDataString();
		Uri fileUri = Uri.parse(FilePath);
		file_uris.add(fileUri);
		handleReceivedFile();
	    }
	    break;
	}
    }

    /**
     * requesting for file browse for attachment in message
     * 
     * @param type
     */
    private void requestForAttachmentIntent(ATTACHMENT_TYPE type) {
	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
	intent.setType(attachments_type.get(type));
	startActivityForResult(intent, PICKFILE_RESULT_CODE);
    }

    private void handleReceivedFile() {
	attachments.clear();
	for (Uri uri : file_uris) {
	    int row_id = Integer.parseInt(uri.getLastPathSegment().toString());
	    File file = new File(getRealPathFromURI(uri));
	    HashMap<String, Object> data = new HashMap<String, Object>();
	    data.put("name", file.getName());
	    OEListViewRows row = new OEListViewRows(row_id, data);
	    attachments.add(row);
	    lstAttachmentAdapter.refresh(attachments);
	}
    }

}
