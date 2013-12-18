package com.openerp.config;

import java.util.ArrayList;
import java.util.List;

import android.provider.CalendarContract;
import android.provider.ContactsContract;

import com.openerp.providers.groups.UserGroupsProvider;
import com.openerp.providers.message.MessageProvider;
import com.openerp.providers.note.NoteProvider;
import com.openerp.support.SyncValue;
import com.openerp.support.SyncWizardHelper;

public class SyncWizardValues implements SyncWizardHelper {

	@Override
	public List<SyncValue> syncValues() {
		List<SyncValue> list = new ArrayList<SyncValue>();

		/* Social */
		list.add(new SyncValue("Social"));
		list.add(new SyncValue("Messages", MessageProvider.AUTHORITY,
				SyncValue.Type.CHECKBOX));
		list.add(new SyncValue("Groups", UserGroupsProvider.AUTHORITY,
				SyncValue.Type.CHECKBOX));

		/* Contacts */
		list.add(new SyncValue("Contacts"));
		List<SyncValue> radioGroups = new ArrayList<SyncValue>();
		radioGroups.add(new SyncValue("All Contacts",
				ContactsContract.AUTHORITY, SyncValue.Type.RADIO));
		radioGroups.add(new SyncValue("Local Contacts",
				ContactsContract.AUTHORITY, SyncValue.Type.RADIO));
		list.add(new SyncValue(radioGroups));

		/* Notes */
		list.add(new SyncValue("Notes"));
		list.add(new SyncValue("Notes", NoteProvider.AUTHORITY,
				SyncValue.Type.CHECKBOX));

		/* Meetings */
		list.add(new SyncValue("Calendar"));
		list.add(new SyncValue("Meetings", CalendarContract.AUTHORITY,
				SyncValue.Type.CHECKBOX));
		return list;
	}
}
