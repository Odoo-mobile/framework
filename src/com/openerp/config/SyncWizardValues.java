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
package com.openerp.config;

import java.util.ArrayList;
import java.util.List;

import android.provider.CalendarContract;
import android.provider.ContactsContract;

import com.openerp.addons.message.providers.groups.MailGroupProvider;
import com.openerp.addons.message.providers.message.MessageProvider;
import com.openerp.addons.note.providers.note.NoteProvider;
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
		list.add(new SyncValue("Groups", MailGroupProvider.AUTHORITY,
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
