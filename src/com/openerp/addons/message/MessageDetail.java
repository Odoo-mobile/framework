package com.openerp.addons.message;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import openerp.OEArguments;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.openerp.R;
import com.openerp.orm.OEDataRow;
import com.openerp.orm.OEHelper;
import com.openerp.orm.OEValues;
import com.openerp.support.BaseFragment;
import com.openerp.support.OEUser;
import com.openerp.support.listview.OEListAdapter;
import com.openerp.util.Base64Helper;
import com.openerp.util.OEBinaryDownloadHelper;
import com.openerp.util.OEDate;
import com.openerp.util.OEFileSizeHelper;
import com.openerp.util.contactview.OEContactView;
import com.openerp.util.drawer.DrawerItem;
import com.openerp.util.drawer.DrawerListener;

public class MessageDetail extends BaseFragment implements OnClickListener {

	public static final String TAg = "com.openerp.addons.message.MessageDetail";
	private static final int MESSAGE_REPLY = 3;
	View mView = null;

	Integer mMessageId = null;
	OEDataRow mMessageData = null;
	ListView mMessageListView = null;
	OEListAdapter mMessageListAdapter = null;
	List<Object> mMessageObjects = new ArrayList<Object>();
	MessageLoader mMessageLoader = null;
	StarredOperation mStarredOperation = null;
	ReadUnreadOperation mReadUnreadOperation = null;
	Integer[] mStarredDrawables = new Integer[] { R.drawable.ic_action_starred,
			R.drawable.ic_action_unstarred };

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		mView = inflater.inflate(R.layout.fragment_message_detail_view,
				container, false);
		init();
		return mView;
	}

	private void init() {
		Log.d(TAg, "MessageDetail->init()");
		Bundle bundle = getArguments();
		if (bundle != null) {
			mMessageId = bundle.getInt("message_id");
			mMessageData = db().select(mMessageId);
			initControls();
			mMessageLoader = new MessageLoader(mMessageId);
			mMessageLoader.execute();
		}
	}

	private void initControls() {
		mMessageListView = (ListView) mView.findViewById(R.id.lstMessageDetail);
		mMessageListAdapter = new OEListAdapter(getActivity(),
				R.layout.fragment_message_detail_listview_items,
				mMessageObjects) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View mView = convertView;
				if (mView == null)
					mView = getActivity().getLayoutInflater().inflate(
							getResource(), parent, false);
				mView = createListViewRow(mView, position);
				return mView;
			}
		};
		mMessageListView.setAdapter(mMessageListAdapter);
	}

	@SuppressLint("CutPasteId")
	private View createListViewRow(View mView, final int position) {

		final OEDataRow row = (OEDataRow) mMessageObjects.get(position);
		TextView txvAuthor, txvEmail, txvTime, txvTo;
		final TextView txvVoteNumber;
		txvAuthor = (TextView) mView.findViewById(R.id.txvMessageAuthor);
		txvEmail = (TextView) mView.findViewById(R.id.txvAuthorEmail);
		txvTime = (TextView) mView.findViewById(R.id.txvTime);
		txvTo = (TextView) mView.findViewById(R.id.txvTo);
		txvVoteNumber = (TextView) mView.findViewById(R.id.txvmessageVotenb);

		String author = row.getString("email_from");
		String email = author;
		OEDataRow author_id = null;
		if (author.equals("false")) {
			author_id = row.getM2ORecord("author_id").browse();
			if (author_id != null) {
				author = author_id.getString("name");
				email = author_id.getString("email");
			}
		}
		txvAuthor.setText(author);
		txvEmail.setText(email);

		txvTime.setText(OEDate.getDate(row.getString("date"), TimeZone
				.getDefault().getID(), "MMM dd, yyyy,  hh:mm a"));

		List<String> partners = new ArrayList<String>();
		String partnersName = "none";
		for (OEDataRow partner : row.getM2MRecord("partner_ids").browseEach()) {
			if (partner.getInt("id") == OEUser.current(getActivity())
					.getPartner_id())
				partners.add("me");
			else
				partners.add(partner.getString("name"));
		}
		if (partners.size() > 0)
			partnersName = TextUtils.join(", ",
					partners.toArray(new String[partners.size()]));
		txvTo.setText(partnersName);

		/* Handling vote control */
		txvVoteNumber.setText(row.getString("vote_nb"));
		int vote_nb = 0;
		if (!row.getString("vote_nb").equals("false"))
			vote_nb = row.getInt("vote_nb");
		if (vote_nb == 0) {
			txvVoteNumber.setText("");
		}
		boolean hasVoted = row.getBoolean("has_voted");
		if (!hasVoted) {
			txvVoteNumber.setCompoundDrawablesWithIntrinsicBounds(
					getResources().getDrawable(
							R.drawable.ic_thumbs_up_unselected_dark_tablet),
					null, null, null);
		} else {
			txvVoteNumber.setCompoundDrawablesWithIntrinsicBounds(
					getResources().getDrawable(
							R.drawable.ic_thumbs_up_selected_dark_tablet),
					null, null, null);
		}
		txvVoteNumber.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				handleVoteToggle(position, txvVoteNumber, row);
			}
		});

		WebView webView = (WebView) mView.findViewById(R.id.webViewMessageBody);
		webView.loadData(row.getString("body"), "text/html", "UTF-8");

		// Handling attachment for each message
		showAttachments(row.getM2MRecord("attachment_ids").browseEach(), mView);

		ImageView imgUserPicture, imgBtnStar;
		imgUserPicture = (ImageView) mView.findViewById(R.id.imgUserPicture);
		imgBtnStar = (ImageView) mView.findViewById(R.id.imgBtnStar);

		// Handling starred event
		final boolean starred = row.getBoolean("starred");
		imgBtnStar.setImageResource((starred) ? mStarredDrawables[0]
				: mStarredDrawables[1]);
		imgBtnStar.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Handling Starred click event
				mStarredOperation = new StarredOperation(position,
						(starred) ? false : true);
				mStarredOperation.execute();
			}
		});

		if (author_id != null
				&& !author_id.getString("image_small").equals("false")) {
			imgUserPicture.setImageBitmap(Base64Helper.getBitmapImage(
					getActivity(), author_id.getString("image_small")));
		}

		// Handling reply button click event
		mView.findViewById(R.id.imgBtnReply).setOnClickListener(this);

		// handling contact view
		OEContactView oe_contactView = (OEContactView) mView
				.findViewById(R.id.imgUserPicture);
		int partner_id = 0;
		if (author_id != null)
			partner_id = author_id.getInt("id");
		oe_contactView.assignPartnerId(partner_id);

		return mView;
	}

	private void showAttachments(List<OEDataRow> attachments, View mView) {
		if (attachments != null && attachments.size() > 0) {
			ViewGroup attachmentViewGroup = (ViewGroup) mView
					.findViewById(R.id.gridAttachments);
			LayoutInflater inflater = getActivity().getLayoutInflater();
			attachmentViewGroup.removeAllViews();
			int index = 0;
			for (OEDataRow attachment : attachments) {
				View attachmentView = inflater.inflate(
						R.layout.fragment_message_detail_attachment_grid_item,
						null, false);
				TextView txvAttachmentName = (TextView) attachmentView
						.findViewById(R.id.txvFileName);

				txvAttachmentName.setText(attachment.get("name").toString());
				TextView txvAttachmentSize = (TextView) attachmentView
						.findViewById(R.id.txvFileSize);
				long fileSize = Long.parseLong(attachment
						.getString("file_size"));
				String file_size = OEFileSizeHelper.readableFileSize(fileSize);
				txvAttachmentSize.setText((file_size.equals("0")) ? " "
						: file_size);

				TextView txvAttachmentId = (TextView) attachmentView
						.findViewById(R.id.txvAttachmentId);
				txvAttachmentId.setText(attachment.getString("id"));

				attachmentViewGroup.addView(attachmentView, index,
						new ViewGroup.LayoutParams(
								ViewGroup.LayoutParams.MATCH_PARENT,
								ViewGroup.LayoutParams.MATCH_PARENT));
				attachmentView.setOnClickListener(this);
				index++;
			}
		} else {
			mView.findViewById(R.id.layoutMessageAttachments).setVisibility(
					View.GONE);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_fragment_message_detail, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection
		switch (item.getItemId()) {
		case R.id.menu_message_detail_read:
			mReadUnreadOperation = new ReadUnreadOperation(false);
			mReadUnreadOperation.execute();
			return true;
		case R.id.menu_message_detail_unread:
			mReadUnreadOperation = new ReadUnreadOperation(true);
			mReadUnreadOperation.execute();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public Object databaseHelper(Context context) {
		return new MessageDB(context);
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		return null;
	}

	class MessageLoader extends AsyncTask<Void, Void, Void> {
		int mMessageId = 0;

		public MessageLoader(int message_id) {
			mMessageId = message_id;
			mView.findViewById(R.id.loadingProgress)
					.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... params) {
			mMessageObjects.clear();
			List<OEDataRow> childs = db().select("parent_id = ?",
					new String[] { mMessageId + "" }, null, null, "date DESC");
			mMessageObjects.add(mMessageData);
			mMessageObjects.addAll(childs);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mView.findViewById(R.id.loadingProgress).setVisibility(View.GONE);
			mMessageListAdapter.notifiyDataChange(mMessageObjects);
			TextView txvTitle = (TextView) mView
					.findViewById(R.id.txvMessageTitle);
			String title = mMessageData.getString("subject");
			if (title.equals("false")) {
				title = mMessageData.getString("type");
			}
			if (!mMessageData.getString("record_name").equals("false"))
				title = mMessageData.getString("record_name");

			txvTitle.setText(title);
		}

	}

	/**
	 * When user press Reply button or download attachment.
	 */
	@Override
	public void onClick(View v) {
		if (v.findViewById(R.id.txvAttachmentId) != null) {
			// when click on attachment download
			int attachment_id = Integer.parseInt(((TextView) v
					.findViewById(R.id.txvAttachmentId)).getText().toString());
			OEBinaryDownloadHelper binaryDownload = new OEBinaryDownloadHelper();
			binaryDownload.downloadBinary(attachment_id, db(), getActivity());
		} else {
			// when click on reply message
			Intent composeIntent = new Intent(getActivity(),
					MessageComposeActivity.class);
			composeIntent.putExtra("message_id", mMessageId);
			composeIntent.putExtra("send_reply", true);
			startActivityForResult(composeIntent, MESSAGE_REPLY);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == MESSAGE_REPLY && resultCode == Activity.RESULT_OK) {
			int newId = data.getIntExtra("new_message_id", 0);
			mMessageObjects.add(1, db().select(newId));
			mMessageListAdapter.notifiyDataChange(mMessageObjects);
		}
	}

	/**
	 * Marking each row starred/unstarred in background
	 */
	public class StarredOperation extends AsyncTask<Void, Void, Boolean> {

		boolean mStarred = false;
		boolean isConnection = true;
		OEHelper mOE = null;
		ProgressDialog mProgressDialog = null;
		int mPosition = -1;

		public StarredOperation(int position, boolean starred) {
			mStarred = starred;
			mOE = db().getOEInstance();
			mPosition = position;
			if (mOE == null)
				isConnection = false;
			mProgressDialog = new ProgressDialog(getActivity());
			mProgressDialog.setMessage("Working...");
			if (isConnection) {
				mProgressDialog.show();
			}
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			if (!isConnection) {
				return false;
			}
			JSONArray mIds = new JSONArray();

			OEDataRow row = (OEDataRow) mMessageObjects.get(mPosition);
			mIds.put(row.getInt("id"));

			OEArguments args = new OEArguments();
			// Param 1 : message_ids list
			args.add(mIds);

			// Param 2 : starred - boolean value
			args.add(mStarred);

			// Param 3 : create_missing - If table does not contain any value
			// for
			// this row than create new one
			args.add(true);

			// Creating Local Database Requirement Values
			OEValues values = new OEValues();
			String value = (mStarred) ? "true" : "false";
			values.put("starred", value);

			boolean response = (Boolean) mOE.call_kw("set_message_starred",
					args, null);
			response = (!mStarred && !response) ? true : response;
			if (response) {
				try {
					for (int i = 0; i < mIds.length(); i++)
						db().update(values, mIds.getInt(i));
				} catch (Exception e) {
				}
			}
			return response;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {

				OEDataRow row = (OEDataRow) mMessageObjects.get(mPosition);
				row.put("starred", mStarred);
				mMessageListAdapter.notifiyDataChange(mMessageObjects);
				DrawerListener drawer = (DrawerListener) getActivity();
				drawer.refreshDrawer(Message.TAG);
			} else {
				Toast.makeText(getActivity(), "No connection",
						Toast.LENGTH_LONG).show();
			}
			mProgressDialog.dismiss();
		}

	}

	/**
	 * Making message read or unread or Archive
	 */
	public class ReadUnreadOperation extends AsyncTask<Void, Void, Boolean> {

		ProgressDialog mProgressDialog = null;
		boolean mToRead = false;
		boolean isConnection = true;
		OEHelper mOE = null;

		public ReadUnreadOperation(boolean toRead) {
			mOE = db().getOEInstance();
			if (mOE == null)
				isConnection = false;
			mToRead = toRead;
			mProgressDialog = new ProgressDialog(getActivity());
			mProgressDialog.setMessage("Working...");
			if (isConnection) {
				mProgressDialog.show();
			}
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			if (!isConnection)
				return false;

			boolean flag = false;

			String default_model = "false";
			JSONArray ids = new JSONArray();
			int parent_id = 0, res_id = 0;

			parent_id = mMessageData.getInt("id");
			res_id = mMessageData.getInt("res_id");
			default_model = mMessageData.getString("model");

			ids.put(parent_id);
			for (OEDataRow child : db().select("parent_id = ? ",
					new String[] { parent_id + "" })) {
				ids.put(child.getInt("id"));
			}
			if (toggleReadUnread(mOE, ids, default_model, res_id, parent_id,
					mToRead)) {
				flag = true;
			}

			return flag;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				DrawerListener drawer = (DrawerListener) getActivity();
				drawer.refreshDrawer(Message.TAG);
			} else {
				Toast.makeText(getActivity(), "No connection",
						Toast.LENGTH_LONG).show();
			}
			mProgressDialog.dismiss();
		}

	}

	/* Method for Make Message as Read,Unread and Archive */
	private boolean toggleReadUnread(OEHelper oe, JSONArray ids,
			String default_model, int res_id, int parent_id, boolean to_read) {
		boolean flag = false;

		JSONObject newContext = new JSONObject();
		OEArguments args = new OEArguments();
		try {
			if (default_model.equals("false")) {
				newContext.put("default_model", false);
			} else {
				newContext.put("default_model", default_model);
			}
			newContext.put("default_res_id", res_id);
			newContext.put("default_parent_id", parent_id);

			// Param 1 : message_ids list
			args.add(ids);

			// Param 2 : to_read - boolean value
			args.add((to_read) ? false : true);

			// Param 3 : create_missing - If table does not contain any value
			// for
			// this row than create new one
			args.add(true);

			// Param 4 : context
			args.add(newContext);

			// Creating Local Database Requirement Values
			OEValues values = new OEValues();
			String value = (to_read) ? "true" : "false";
			values.put("starred", false);
			values.put("to_read", value);
			int result = (Integer) oe.call_kw("set_message_read", args, null);
			if (result > 0) {
				for (int i = 0; i < ids.length(); i++) {
					int id = ids.getInt(i);
					db().update(values, id);
				}
				flag = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}

	private void handleVoteToggle(int position, TextView view, Object obj) {
		OEDataRow row = (OEDataRow) obj;
		int vote_nb = row.getInt("vote_nb");
		boolean hasVoted = row.getBoolean("has_voted");
		MessageVoteToggle voteToggle = new MessageVoteToggle(row.getInt("id"),
				vote_nb, hasVoted);
		String newVote = "";
		boolean btnvoted = false;
		if (hasVoted) {
			newVote = (vote_nb - 1) + "";
			row.put("has_voted", "false");
		} else {
			btnvoted = true;
			newVote = (vote_nb + 1) + "";
			row.put("has_voted", "true");
		}
		row.put("vote_nb", newVote);
		mMessageListAdapter.replaceObjectAtPosition(position, row);
		voteToggle.execute((Void) null);
		view.setText(newVote);
		if (!btnvoted) {
			view.setCompoundDrawablesWithIntrinsicBounds(
					getResources().getDrawable(
							R.drawable.ic_thumbs_up_unselected_dark_tablet),
					null, null, null);
		} else {
			view.setCompoundDrawablesWithIntrinsicBounds(getResources()
					.getDrawable(R.drawable.ic_thumbs_up_selected_dark_tablet),
					null, null, null);
		}
		if (newVote.equals("0")) {
			view.setText("");
		}
	}

	private class MessageVoteToggle extends AsyncTask<Void, Void, Boolean> {
		int message_id = 0;
		int vote_nb = 0;
		boolean has_voted = false;

		public MessageVoteToggle(int message_id, int current_vote_nb,
				boolean has_voted) {
			this.message_id = message_id;
			this.vote_nb = current_vote_nb;
			this.has_voted = has_voted;
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			OEHelper oe = db().getOEInstance();
			if (oe == null)
				return false;
			try {
				OEArguments args = new OEArguments();
				args.add(new JSONArray("[" + message_id + "]"));
				oe.call_kw("vote_toggle", args);
				OEValues values = new OEValues();
				String vote = "false";
				if (!this.has_voted) {
					vote = "true";
					vote_nb = vote_nb + 1;
				} else {
					vote_nb = vote_nb - 1;
				}

				values.put("has_voted", vote);
				values.put("vote_nb", vote_nb);
				if (db().update(values, message_id) > 0)
					return true;

			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
	}
}
