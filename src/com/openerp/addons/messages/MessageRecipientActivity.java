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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.openerp.MainActivity;
import com.openerp.R;
import com.openerp.base.res.Res_PartnerDBHelper;
import com.openerp.orm.Fields;
import com.openerp.orm.OEHelper;
import com.openerp.support.JSONDataHelper;
import com.openerp.support.OEDialog;
import com.openerp.support.listview.OEListViewAdapter;
import com.openerp.support.listview.OEListViewRows;

public class MessageRecipientActivity extends Activity {
	public static final int RECEIPIENT_RESULT = 2;

	ListView partner_list;
	ArrayList<String> partners;
	OEListViewAdapter listAdapters = null;
	List<OEListViewRows> listRows = null;
	Res_PartnerDBHelper res_partners = null;
	OEHelper oe = null;
	Context context = null;

	HashMap<String, Object> selectedPartners = new HashMap<String, Object>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_message_recipient);
		context = this;
		res_partners = new Res_PartnerDBHelper(this);
		oe = res_partners.getOEInstance();
		setTitle("Partners");
		partner_list = (ListView) findViewById(R.id.lstRecipients);

		localPartners();
	}

	private void localPartners() {
		String[] from = new String[] { "image_small", "name", "email" };
		int[] to = new int[] { R.id.imgUserPicture, R.id.txvPartner,
				R.id.txvPartnerEmail };
		listRows = new ArrayList<OEListViewRows>();
		listRows = getListRows();
		listAdapters = new OEListViewAdapter(MainActivity.context,
				R.layout.res_partners, listRows, from, to, res_partners);
		listAdapters.addImageColumn("image_small");
		partner_list.setAdapter(listAdapters);

		checkForSelectedPartners(getIntent());
	}

	private void checkForSelectedPartners(Intent intent) {
		// TODO Auto-generated method stub
		HashMap<String, Object> selected = new HashMap<String, Object>();
		if (intent.getExtras().containsKey("selected_ids")) {
			selected = (HashMap<String, Object>) intent.getExtras().get(
					"selected_ids");
			for (String key : selected.keySet()) {
				ContentValues val = (ContentValues) selected.get(key);
				int pos = val.getAsInteger("pos");
				partner_list.setItemChecked(pos, true);
			}
		}

	}

	private List<OEListViewRows> getListRows() {
		List<OEListViewRows> lists = new ArrayList<OEListViewRows>();

		HashMap<String, Object> partners = res_partners.search(res_partners);
		int total = Integer.parseInt(partners.get("total").toString());
		if (total > 0) {
			@SuppressWarnings("unchecked")
			List<HashMap<String, Object>> rows = (List<HashMap<String, Object>>) partners
					.get("records");
			for (HashMap<String, Object> row_data : rows) {
				OEListViewRows row = new OEListViewRows(
						Integer.parseInt(row_data.get("id").toString()),
						row_data);
				lists.add(row);
			}
		} else {
			finish();
		}
		return lists;
	}

	public Boolean getPartnersFromServer() {
		boolean flag = true;

		try {
			ArrayList<Fields> cols = res_partners.getServerColumns();
			JSONObject fields = new JSONObject();
			for (Fields field : cols) {
				fields.accumulate("fields", field.getName());
			}
			JSONObject domain = new JSONObject();
			JSONArray ids = JSONDataHelper.intArrayToJSONArray(oe
					.getAllIds(res_partners));

			domain.accumulate("domain", new JSONArray("[[\"id\", \"not in\", "
					+ ids.toString() + "]]"));

			// oe.debugMode(true);
			JSONObject result = oe.search_read("res.partner", fields, domain,
					0, 0, null, null);

			for (int i = 0; i < result.getInt("length"); i++) {
				JSONObject row = result.getJSONArray("records")
						.getJSONObject(i);

				HashMap<String, Object> rowHash = new HashMap<String, Object>();
				@SuppressWarnings("unchecked")
				Iterator<String> keys = row.keys();
				while (keys.hasNext()) {
					String key = keys.next();
					rowHash.put(key, row.get(key));
				}

				final OEListViewRows listRow = new OEListViewRows(
						row.getInt("id"), rowHash);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						listRows.add(listRow);
						listAdapters.refresh(listRows);

					}
				});

			}
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}
		return flag;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_message_recipient_activty, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case android.R.id.home:
			// app icon in action bar clicked; go home
			finish();
			return true;
		case R.id.menu_add_from_server:
			GetFromServer request = new GetFromServer();
			request.execute((Void) null);
			return true;
		case R.id.menu_add_recipients:
			Intent resultIntent = new Intent();
			resultIntent.putExtra("result", getSelecetedPartners());
			// TODO Add extras or a data URI to this intent as appropriate.
			setResult(Activity.RESULT_OK, resultIntent);
			finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	class GetFromServer extends AsyncTask<Void, Void, Boolean> {
		OEDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new OEDialog(context, false, "Loading partners...");
			dialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return getPartnersFromServer();
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			dialog.cancel();
			if (!success) {
				Toast.makeText(context,
						"Unable to fetch partners from server !",
						Toast.LENGTH_LONG).show();
			}
			this.cancel(true);

		}

	}

	public HashMap<String, Object> getSelecetedPartners() {
		selectedPartners = new HashMap<String, Object>();
		SparseBooleanArray checked = partner_list.getCheckedItemPositions();
		for (int i = 0; i < checked.size(); i++) {
			int key = checked.keyAt(i);
			boolean value = checked.get(key);
			if (value) {
				if (!res_partners.hasRecord(res_partners, listRows.get(key)
						.getRow_id())) {
					ContentValues values = new ContentValues();
					ArrayList<Fields> cols = res_partners.getServerColumns();
					for (Fields field : cols) {
						values.put(field.getName(), listRows.get(key)
								.getRow_data().get(field.getName()).toString());
					}
					res_partners.create(res_partners, values);
				}
				// Creating list for selected partners
				HashMap<String, Object> row_data = listRows.get(key)
						.getRow_data();
				String display_name = row_data.get("name").toString();

				ContentValues partner_vals = new ContentValues();
				partner_vals.put("pos", key);
				partner_vals.put("id", row_data.get("id").toString());
				partner_vals.put("email", row_data.get("email").toString());
				selectedPartners.put(display_name, partner_vals);
				// addFollowers(listRows.get(key).getRow_id());
			}
		}
		return selectedPartners;

	}

	@Override
	public void onPause() {
		super.onPause();
		res_partners = null;
	}

}
