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

import java.util.List;

import android.app.Activity;
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

import com.openerp.MainActivity;
import com.openerp.R;
import com.openerp.addons.messages.MessageComposeActivty;
import com.openerp.orm.OEDataRow;
import com.openerp.orm.OEHelper;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.util.HTMLHelper;
import com.openerp.util.controls.OETextView;
import com.openerp.util.drawer.DrawerItem;
import com.openerp.util.tags.MultiTagsTextView;
import com.openerp.util.tags.TagsItem;
import com.openerp.util.tags.TagsView;

public class DetailNoteFragment extends BaseFragment implements
		MultiTagsTextView.TokenListener {

	View rootview = null;
	OETextView mNoteDetailTitle;
	OETextView mNoteDetailMemo;
	Note note = null;
	NoteDBHelper db = null;
	int note_id = 0;
	String message;
	String padurl = "false";
	String row_status = null;
	String stageId;
	int noteId;
	int stageColor;
	TagsView noteTags = null;

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
			selectedNoteID.putInt("res_id", note_id);
			selectedNoteID.putString("message", message);
			addfollower_fragement.setArguments(selectedNoteID);
			scope.main().fragmentHandler.setBackStack(true, null);
			scope.main().fragmentHandler.replaceFragmnet(addfollower_fragement);
			return true;

		case R.id.menu_note_forward_asmail:

			Intent sendAsMail = new Intent(MainActivity.context,
					MessageComposeActivty.class);
			sendAsMail.putExtra("note_body", message);
			scope.context().startActivity(sendAsMail);
			return true;

		case R.id.menu_note_mark_asdone:

			note.strikeNote(note_id, row_status, scope);
			getActivity().getSupportFragmentManager().popBackStack();
			return true;

		case R.id.menu_note_mark_asopen:

			note.strikeNote(note_id, row_status, scope);
			getActivity().getSupportFragmentManager().popBackStack();
			return true;

		case R.id.menu_note_edit:
			NoteDBHelper noteDb = new NoteDBHelper(scope.context());
			Bundle editNoteID = new Bundle();
			editNoteID.putInt("note_id", note_id);
			if (noteDb.isPadExist()) {
				if (!padurl.equalsIgnoreCase("false")) {
					editNoteID.putString("padurl", padurl);
				} else {
					// If Pad Installed And Notes is without pad then will
					// converted into Pad.
					OEHelper oe = getOEInstance();
					padurl = db.getURL(oe, note_id);
					editNoteID.putString("padurl", padurl);
				}
			} else {
				editNoteID.putString("padurl", padurl);
				editNoteID.putString("row_details", mNoteDetailMemo.getText()
						.toString());
			}

			editNoteID.putString("stage_id", stageId);
			editNoteID.putInt("tag_id", noteId);
			Intent editNote = new Intent(scope.context(),
					ComposeNoteActivity.class);
			editNote.putExtras(editNoteID);
			startActivityForResult(editNote, 123);
			return true;

		case R.id.menu_note_delete:

			// Opening dailogbox for confirmation to delete
			openDailogview(note_id);
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
		if (bundle.containsKey("note_id")) {
			note_id = bundle.getInt("note_id");
			row_status = bundle.getString("row_status");
			stageId = bundle.getString("stage_id");

			if (bundle.containsKey("stage_color")) {
				View vStageColor = (View) rootview
						.findViewById(R.id.viewNoteStageColor);
				vStageColor.setBackgroundColor(bundle.getInt("stage_color"));
				stageColor = bundle.getInt("stage_color");
			}
			showNoteDetails(bundle.getInt("note_id"));
		}
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		return null;
	}

	private void showNoteDetails(int note_id) {
		mNoteDetailTitle = (OETextView) rootview
				.findViewById(R.id.txvNoteDetailTitle);
		mNoteDetailMemo = (OETextView) rootview
				.findViewById(R.id.txvNoteDetailMemo);
		noteTags = (TagsView) rootview.findViewById(R.id.edtNoteTagsView);
		noteTags.setTokenListener(this);
		noteTags.setCustomTagView(new TagsView.CustomTagViewListener() {

			@Override
			public View getViewForTags(LayoutInflater layoutInflater,
					Object object, ViewGroup tagsViewGroup) {
				View view = (View) layoutInflater.inflate(
						R.layout.custom_note_tagsview_item, tagsViewGroup,
						false);
				TagsItem item = (TagsItem) object;
				OETextView txvTitle = (OETextView) view
						.findViewById(R.id.txvCustomNoteTagsViewItem);
				txvTitle.setText(item.getSubject());
				txvTitle.setBackgroundColor(stageColor);
				return view;
			}
		});
		for (Object tag : noteTags.getObjects()) {
			noteTags.removeObject(tag);
		}
		noteTags.allowDuplicates(false);
		mNoteDetailMemo.setMovementMethod(new ScrollingMovementMethod());
		db = new NoteDBHelper(scope.context());

		List<OEDataRow> result = db.search(db, new String[] { "id=?" },
				new String[] { String.valueOf(note_id) });
		int total = result.size();
		if (total > 0) {
			OEDataRow row = result.get(0);

			if (row.get("note_pad_url") != null) {
				padurl = row.get("note_pad_url").toString();
			}
			message = row.get("memo").toString();
			try {
				noteId = note_id;
				List<TagsItem> note_tags_items = note.getNoteTags(
						String.valueOf(noteId), scope.context());
				noteTags.showImage(false);
				for (TagsItem tag : note_tags_items) {
					noteTags.addObject(new TagsItem(tag.getId(), tag
							.getSubject(), null));
				}
				if (note_tags_items.size() <= 0) {
					noteTags.setVisibility(View.GONE);
				}
				mNoteDetailTitle.setText(HTMLHelper.htmlToString(db
						.generateName(row.getString("memo"))));
				mNoteDetailMemo.setText(HTMLHelper.stringToHtml(row
						.getString("memo")));
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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case 123:
				if (data.hasExtra("updated")) {
					Bundle notedata = data.getExtras();
					String memo = notedata.getString("memo");
					mNoteDetailTitle.setText(HTMLHelper.htmlToString(db
							.generateName(memo)));
					mNoteDetailMemo.setText(HTMLHelper.stringToHtml(memo));
				}
				break;
			}
		}
	}

	@Override
	public void onTokenAdded(Object token, View view) {

	}

	@Override
	public void onTokenSelected(Object token, View view) {

	}

	@Override
	public void onTokenRemoved(Object token) {
		noteTags.addObject(token);
	}
}
