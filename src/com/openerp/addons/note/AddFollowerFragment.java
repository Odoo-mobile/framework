/*
 * OpenERP, Open Source Management Solution
 * Copyright (C) 2012-today OpenERP SA (<http://www.openerp.com>)
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * 
 */
package com.openerp.addons.note;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.Toast;

import com.openerp.R;
import com.openerp.base.res.Res_PartnerDBHelper;
import com.openerp.orm.Fields;
import com.openerp.orm.OEHelper;
import com.openerp.providers.note.NoteProvider;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.JSONDataHelper;
import com.openerp.support.OEDialog;
import com.openerp.support.listview.OEListViewAdapter;
import com.openerp.support.listview.OEListViewRows;
import com.openerp.util.drawer.DrawerItem;

public class AddFollowerFragment extends BaseFragment {

	View rootview;
	ListView partner_list;
	Button addFollower, morePartners;
	Res_PartnerDBHelper res_partners = null;
	OEListViewAdapter listAdapters = null;
	List<OEListViewRows> listRows = null;
	OEHelper oe = null;
	ArrayList<String> partners;
	int record_id = 0;
	String message = null;
	Boolean flag = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		scope = new AppScope(this);
		rootview = inflater.inflate(R.layout.fragment_add_follower, container,
				false);
		addFollower = (Button) rootview.findViewById(R.id.btn_note_addfollower);
		morePartners = (Button) rootview
				.findViewById(R.id.btn_note_loadpartners);
		partner_list = (ListView) rootview.findViewById(R.id.lstfollowers);
		addFollower.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getSelecetedPartners();
			}
		});

		morePartners.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LoadPartners loadpartner = new LoadPartners();
				loadpartner.execute((Void) null);
			}
		});
		return rootview;
	}

	@Override
	public Object databaseHelper(Context context) {
		return new NoteDBHelper(context);
	}

	public void onStart() {
		super.onStart();
		Bundle bundle = getArguments();
		if (bundle.containsKey("res_id")) {
			record_id = bundle.getInt("res_id");
			message = bundle.getString("message");
		}
		getPartnersFromLocal();
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		return null;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_fragment_note, menu);
		SearchView searchView = (SearchView) menu.findItem(
				R.id.menu_note_search).getActionView();
		searchView.setOnQueryTextListener(getQueryListener(listAdapters));

		searchView.setOnCloseListener(new OnCloseListener() {
			@Override
			public boolean onClose() {
				getPartnersFromLocal();
				return false;
			}
		});

		// disabling the COMPOSE NOTE,WRITE,CANCEL options
		MenuItem item_compose = menu.findItem(R.id.menu_note_compose);
		item_compose.setVisible(false);
		MenuItem item_write = menu.findItem(R.id.menu_note_write);
		item_write.setVisible(false);
		MenuItem item_cancel = menu.findItem(R.id.menu_note_cancel);
		item_cancel.setVisible(false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	private List<OEListViewRows> getListRows() {

		List<OEListViewRows> lists = new ArrayList<OEListViewRows>();
		res_partners = new Res_PartnerDBHelper(scope.context());
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
			scope.context().requestSync(NoteProvider.AUTHORITY);
		}
		return lists;
	}

	public void getPartnersFromLocal() {

		String[] from = new String[] { "image_small", "name", "email" };
		int[] to = new int[] { R.id.imgUserPicture, R.id.txvPartner,
				R.id.txvPartnerEmail };
		listRows = new ArrayList<OEListViewRows>();

		if (listRows != null && listRows.size() <= 0) {
			listRows = getListRows();
		}

		listAdapters = new OEListViewAdapter(scope.context(),
				R.layout.res_partners, listRows, from, to, db);
		listAdapters.addImageColumn("image_small");
		partner_list.setAdapter(listAdapters);
		flag = false;
	}

	public Boolean getPartnersFromServer() {

		boolean loaded = true;
		if (!flag) {
			res_partners = new Res_PartnerDBHelper(scope.context());
			oe = res_partners.getOEInstance();
			try {
				ArrayList<Fields> cols = res_partners.getServerColumns();
				JSONObject fields = new JSONObject();
				for (Fields field : cols) {
					fields.accumulate("fields", field.getName());
				}
				JSONObject domain = new JSONObject();
				JSONArray ids = JSONDataHelper.intArrayToJSONArray(oe
						.getAllIds(res_partners));

				domain.accumulate("domain", new JSONArray(
						"[[\"id\", \"not in\", " + ids.toString() + "]]"));

				JSONObject result = oe.search_read("res.partner", fields,
						domain, 0, 0, null, null);

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

					scope.context().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							listRows.add(listRow);
							listAdapters.refresh(listRows);
						}
					});
					flag = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			loaded = false;
		}
		return loaded;
	}

	public void getSelecetedPartners() {

		SparseBooleanArray checked = partner_list.getCheckedItemPositions();
		for (int i = 0; i < checked.size(); i++) {
			int key = checked.keyAt(i);
			boolean value = checked.get(key);

			if (value) {
				res_partners = new Res_PartnerDBHelper(scope.context());

				if (!res_partners.hasRecord(res_partners, listRows.get(key)
						.getRow_id())) {
					ContentValues values = new ContentValues();
					ArrayList<Fields> cols = new Res_PartnerDBHelper(
							scope.context()).getServerColumns();

					for (Fields field : cols) {
						values.put(field.getName(), listRows.get(key)
								.getRow_data().get(field.getName()).toString());
					}
					res_partners.create(res_partners, values);
				}
				addFollowers(listRows.get(key).getRow_id());
			}
		}
		flag = false;
		getActivity().getSupportFragmentManager().popBackStack();
	}

	public void addFollowers(int partnerId) {

		res_partners = new Res_PartnerDBHelper(scope.context());
		oe = res_partners.getOEInstance();
		try {
			JSONObject args = new JSONObject();
			args.put("res_model", "note.note");
			args.put("res_id", record_id);
			args.put("message", message);
			JSONArray partner_ids = new JSONArray();
			partner_ids.put(6);
			partner_ids.put(false);
			JSONArray c_ids = new JSONArray();
			c_ids.put(partnerId);
			partner_ids.put(c_ids);
			args.put("partner_ids", new JSONArray("[" + partner_ids.toString()
					+ "]"));
			JSONObject result = oe.createNew("mail.wizard.invite", args);
			int id = result.getInt("result");

			// calling mail.wizard.invite method
			JSONArray arguments = new JSONArray();
			JSONArray result_id = new JSONArray();
			result_id.put(id);
			arguments.put(result_id);

			JSONObject newValues = new JSONObject();
			newValues.put("default_res_model", "note.note");
			newValues.put("default_res_id", args.getInt("res_id"));
			JSONObject newContext = oe.updateContext(newValues);
			arguments.put(newContext);
			oe.call_kw("mail.wizard.invite", "add_followers", arguments);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class LoadPartners extends AsyncTask<Void, Void, Boolean> {

		OEDialog pdialog = null;

		public LoadPartners() {
			pdialog = new OEDialog(scope.context(), true, "Loading Partners...");
		}

		@Override
		protected void onPreExecute() {
			pdialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			return getPartnersFromServer();
		}

		@Override
		protected void onPostExecute(final Boolean success) {

			pdialog.hide();
			if (!success) {
				Toast.makeText(scope.context(), "No More Partners...",
						Toast.LENGTH_SHORT).show();
			}
		}
	}
}
