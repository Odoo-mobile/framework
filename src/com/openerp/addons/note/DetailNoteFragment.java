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

import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.openerp.MainActivity;
import com.openerp.R;
import com.openerp.addons.messages.MessageComposeActivty;
import com.openerp.orm.OEHelper;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.util.HTMLHelper;
import com.openerp.util.drawer.DrawerItem;
import com.openerp.util.tags.TagsItems;
import com.openerp.util.tags.TagsView;

public class DetailNoteFragment extends BaseFragment {

	View rootview = null;
	TextView noteMemo;
	Note note = null;
	NoteDBHelper db = null;
	int row_id = 0;
	String message;
	String padurl = "false";
	String row_status = null;
	String stageid = null;
	String noteid = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		setHasOptionsMenu(true);
		scope = new AppScope(this);
		db = (NoteDBHelper) getModel();
		note = new Note();
		rootview = inflater.inflate(R.layout.fragment_detail_note, container,
				false);
		return rootview;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		inflater.inflate(R.menu.menu_fragment_note_detail, menu);
		// disabling the Compose Note option cause you are already in that menu
		MenuItem item_save = menu.findItem(R.id.menu_note_edit_save);
		MenuItem item_cancel = menu.findItem(R.id.menu_note_edit_cancel);
		item_save.setVisible(false);
		item_cancel.setVisible(false);

		// handling Menubutton[Marks As Done or Open] depending upon note status
		if (getArguments() != null) {
			row_status = getArguments().getString("row_status");

			if (row_status.equalsIgnoreCase("true")) {
				MenuItem mark_asopen = menu
						.findItem(R.id.menu_note_mark_asopen);
				mark_asopen.setVisible(false);
			} else {
				MenuItem mark_asdone = menu
						.findItem(R.id.menu_note_mark_asdone);
				mark_asdone.setVisible(false);
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_note_invite_people:

			AddFollowerFragment addfollower_fragement = new AddFollowerFragment();
			Bundle selectedNoteID = new Bundle();
			selectedNoteID.putInt("res_id", row_id);
			selectedNoteID.putString("message", message);
			addfollower_fragement.setArguments(selectedNoteID);
			scope.context().fragmentHandler.setBackStack(true, null);
			scope.context().fragmentHandler
					.replaceFragmnet(addfollower_fragement);
			return true;

		case R.id.menu_note_forward_asmail:

			Intent sendAsMail = new Intent(MainActivity.context,
					MessageComposeActivty.class);
			sendAsMail.putExtra("note_body", message);
			scope.context().startActivity(sendAsMail);
			return true;

		case R.id.menu_note_mark_asdone:

			note.strikeNote(row_id, row_status, scope.context());
			getActivity().getSupportFragmentManager().popBackStack();
			return true;

		case R.id.menu_note_mark_asopen:

			note.strikeNote(row_id, row_status, scope.context());
			getActivity().getSupportFragmentManager().popBackStack();
			return true;

		case R.id.menu_note_edit:

			EditNoteFragment editnote_fragment = new EditNoteFragment();
			Bundle editNoteID = new Bundle();
			editNoteID.putInt("row_id", row_id);
			if (Note.isStateExist.equalsIgnoreCase("true")) {
				if (!padurl.equalsIgnoreCase("false")) {
					editNoteID.putString("padurl", padurl);
				} else {
					// If Pad Installed And Notes is without pad then will
					// converted into Pad.
					OEHelper oe = getOEInstance();
					padurl = db.getURL(oe, row_id);
					editNoteID.putString("padurl", padurl);
				}
			} else {
				editNoteID.putString("padurl", padurl);
				editNoteID.putString("row_details", noteMemo.getText()
						.toString());
			}

			editNoteID.putString("stage_id", stageid);
			editNoteID.putString("tag_id", noteid);
			editnote_fragment.setArguments(editNoteID);
			scope.context().fragmentHandler.setBackStack(true, null);
			scope.context().fragmentHandler.replaceFragmnet(editnote_fragment);
			return true;

		case R.id.menu_note_delete:

			// Opening dailogbox for confirmation to delete
			openDailogview(row_id);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public Object databaseHelper(Context context) {
		return null;
	}

	public void onStart() {
		super.onStart();
		Bundle bundle = getArguments();
		if (bundle.containsKey("row_id")) {
			row_id = bundle.getInt("row_id");
			row_status = bundle.getString("row_status");
			stageid = bundle.getString("stage_id");

			if (bundle.containsKey("stage_color")) {
				View vStageColor = (View) rootview
						.findViewById(R.id.viewNoteStageColor);
				vStageColor.setBackgroundColor(bundle.getInt("stage_color"));
			}
			showNoteDetails(bundle.getInt("row_id"));
		}
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		return null;
	}

	private void showNoteDetails(int note_id) {

		noteMemo = (TextView) rootview.findViewById(R.id.txv_detailNote_Memo);
		TagsView noteTags = (TagsView) rootview
				.findViewById(R.id.txv_detailNote_Tags);

		noteTags.allowDuplicates(false);
		noteMemo.setMovementMethod(new ScrollingMovementMethod());
		db = new NoteDBHelper(scope.context());

		HashMap<String, Object> result = db.search(db, new String[] { "id=?" },
				new String[] { String.valueOf(note_id) });
		int total = Integer.parseInt(result.get("total").toString());
		if (total > 0) {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> row = ((List<HashMap<String, Object>>) result
					.get("records")).get(0);

			if (row.get("note_pad_url") != null) {
				padurl = row.get("note_pad_url").toString();
			}

			// paassing to next followerfragment
			message = row.get("memo").toString();
			try {
				noteid = String.valueOf(note_id);
				String[] note_tags_items = note.getNoteTags(
						String.valueOf(noteid), scope.context());
				noteTags.showImage(false);
				for (String tag : note_tags_items) {
					noteTags.addObject(new TagsItems(0, tag, ""));
				}
				if (note_tags_items.length <= 0) {
					noteTags.setVisibility(View.GONE);
				}
				noteMemo.setText(HTMLHelper.stringToHtml(row.get("memo")
						.toString()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void openDailogview(final int deleteID) {
		AlertDialog.Builder deleteDialogConfirm = new AlertDialog.Builder(
				scope.context());
		deleteDialogConfirm.setTitle("Delete");
		deleteDialogConfirm.setMessage("Are you sure want to delete ?");
		deleteDialogConfirm.setCancelable(true);

		deleteDialogConfirm.setPositiveButton("Delete",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						db = new NoteDBHelper(scope.context());
						db.delete(db, deleteID);
						getActivity().getSupportFragmentManager()
								.popBackStack();
					}
				});

		deleteDialogConfirm.setNegativeButton("Cancel", null);
		deleteDialogConfirm.show();
	}

}
