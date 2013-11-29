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
package com.openerp.support.calendar;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;

import com.openerp.addons.meeting.MeetingDBHelper;
import com.openerp.auth.OpenERPAccountManager;
import com.openerp.orm.OEHelper;
import com.openerp.support.OEUser;

// TODO: Auto-generated Javadoc
/**
 * The Class OECalendar.
 */
public class OECalendar {

	/** context : object for context */
	Context context = null;

	/** cr : object for ContentResolver. */
	ContentResolver cr;

	/** values : object for ContentValues. */
	ContentValues values;

	/**
	 * created_ calendar_id: id of the newly created OpenERP Mobile Calendar,
	 * creted_Event_id : id of the newly created Events under OpenERP Mobile
	 * Calendar
	 */
	long created_Calendar_id, creted_Event_id;
	long cal_id = 0;

	/** uri : To work with android default URI's for [Calendar/Events]. */
	Uri uri, creationUri;

	/**
	 * eventBeginTime : Begin time of the crm.meeting, eventEndTime :
	 * Date_deadline for the crm.meeting
	 */
	Calendar eventBeginTime, eventEndTime;

	/** selection query : for checking calendar existance or not in device. */
	String selectionQuery;

	/** cursor : cursor to moving with dataset. */
	Cursor cursor = null;

	/**
	 * get_ AllMettings : contain all the meetings from localdb table
	 * crm.meeting.
	 */
	HashMap<String, Object> get_AllMettings;

	/**
	 * startDate : for parsing meeting start time to
	 * DateFormat("yyyy-MM-dd HH:mm").
	 */
	Date startDate = null;

	/**
	 * endDate : used for parsing meeting end time to
	 * DateFormat("yyyy-MM-dd HH:mm").
	 */
	Date endDate = null;

	/** cal : Instance of the calendar */
	Calendar cal;

	/** db : obejct to work with MeetingDBHelper class for database operations. */
	MeetingDBHelper db;

	/**
	 * calendar_ Eventids : contain all the synced evenids from local database.
	 */
	List<String> calendar_Eventids;

	/**
	 * CALENDAR_PROJECTION : used to check OpenERP Mobile calendar existence or
	 * not, Calendars._ID : give OpenERP Mobile calendar Id,
	 * Calendars.ACCOUNT_NAME : give OpenERP Mobile calendar account name [Not
	 * Display Name], Calendars.CALENDAR_DISPLAY_NAME : give OpenERP Mobile
	 * calendar Display Name
	 */
	public static final String[] CALENDAR_PROJECTION = new String[] {
			Calendars._ID, // 0
			Calendars.ACCOUNT_NAME, // 1
			Calendars.CALENDAR_DISPLAY_NAME // 2
	};

	public OECalendar(Context context) {
		super();
		this.context = context;
	}

	/**
	 * As sync adapter.
	 * 
	 * @param uri
	 *            : uri of the calendar
	 * @param account
	 *            : with OpenERP Mobile Calendar created
	 * @param accountType
	 *            : with OpenERP Mobile Calendar created
	 * @return uri : with uri + account + accounttype
	 */
	private Uri asSyncAdapter(Uri uri, String account, String accountType) {
		return uri
				.buildUpon()
				.appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER,
						"true")
				.appendQueryParameter(Calendars.ACCOUNT_NAME, account)
				.appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType)
				.build();
	}

	/**
	 * Mannually created events into Mobile/Device will be Sync with OpenERP
	 * Server and in localdb too.
	 * 
	 * @param account
	 *            : OpenERP account.
	 * @param db
	 *            : databae object.
	 */
	public void sync_Event_TOServer(Account account, MeetingDBHelper db) {

		try {
			List<HashMap<String, Object>> local_meetings = getAllMeetings(db);
			for (int i = 0; i < local_meetings.size(); i++) {
				cal_id = isCalInDevice(account);

				if (cal_id == 0) {
					cal_id = createCalendar(account);
					syncEvent(
							cal_id,
							Integer.parseInt(local_meetings.get(i).get("id")
									.toString()),
							local_meetings.get(i).get("name").toString(),
							local_meetings.get(i).get("date").toString(),
							local_meetings.get(i).get("date_deadline")
									.toString(),
							local_meetings.get(i).get("description").toString());
				} else {

					if (!isEventInCal(
							context,
							isObjectNull(local_meetings.get(i).get(
									"calendar_event_id")))) {
						syncEvent(
								cal_id,
								Integer.parseInt(local_meetings.get(i)
										.get("id").toString()), local_meetings
										.get(i).get("name").toString(),
								local_meetings.get(i).get("date").toString(),
								local_meetings.get(i).get("date_deadline")
										.toString(),
								local_meetings.get(i).get("description")
										.toString());
					}
				}
			}
			syncToServer(context);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Registering only those Events which are exist in Device[PHONE] but not in
	 * OpenERP server.
	 * 
	 * @param context
	 *            : context value
	 * @return boolean : true, if successful
	 */
	public boolean syncToServer(Context context) {

		boolean flag = false;
		get_AllMettings = new HashMap<String, Object>();
		cursor = context.getContentResolver().query(
				Uri.parse("content://com.android.calendar/events"),
				new String[] { "calendar_id", "title", "description",
						"dtstart", "dtend", "eventLocation", "_id", "allDay",
						"duration" }, null, null, null);
		calendar_Eventids = new ArrayList<String>();
		calendar_Eventids = get_cal_event_Ids(context);

		if (cursor.moveToFirst()) {
			do {
				// retrieve id from Mobile/Device
				String meeting_id = cursor.getString(cursor
						.getColumnIndex("_id"));

				// retrieve calendar_id from Mobile/Device
				long calendar_id = cursor.getLong(cursor
						.getColumnIndex(Events.CALENDAR_ID));

				// checking whether meeting id exist in localdb or not
				if (!calendar_Eventids.contains(meeting_id)) {
					// retrieve all the details from Mobile/Device
					// checking for only OpenERP calendar's EVENTS
					if (cal_id == calendar_id) {
						String name = cursor.getString(cursor
								.getColumnIndex("title"));

						String date = convertDateTimeZone(cursor.getLong(cursor
								.getColumnIndex("dtstart")));

						String enddate = convertDateTimeZone(cursor
								.getLong(cursor.getColumnIndex("dtend")));

						String duration = getDuration(cursor.getLong(cursor
								.getColumnIndex("dtstart")),
								cursor.getLong(cursor.getColumnIndex("dtend")));

						boolean allDay = (cursor.getInt(cursor
								.getColumnIndex("allDay")) == 1) ? true : false;

						String categ_id = "[]";

						String location = cursor.getString(cursor
								.getColumnIndex("eventLocation"));

						String description = cursor.getString(cursor
								.getColumnIndex("description"));

						int newId = writeMeeting(name, date, enddate, duration,
								allDay, categ_id, location, description);

						// updating the values in crm.meeting table in localdb
						// for
						// synced events.
						update_SyncedEvent(newId, String.valueOf(calendar_id),
								meeting_id, "true");
						flag = true;
					}
				}
			} while (cursor.moveToNext());
		}
		cursor.close();
		return flag;
	}

	/**
	 * Update the status of synced events in crm.meeting table.
	 * 
	 * @param meeting_id
	 *            : record id of the crm.meeting.
	 * @param calendar_id
	 *            : under which meeting synced.
	 * @param event_id
	 *            : creted event id under calendar.
	 * @param sync
	 *            : true, if synced in OpenERP Mobile calendar.
	 */
	public void update_SyncedEvent(int meeting_id, String calendar_id,
			String event_id, String sync) {
		db = new MeetingDBHelper(context);
		SQLiteDatabase database = db.getWritableDatabase();
		String query = "update " + db.getTableName() + " set in_cal_sync = '"
				+ sync + "',calendar_event_id  = '" + event_id
				+ "', calendar_id  = '" + calendar_id + "' where  id = "
				+ meeting_id;
		database.execSQL(query);
		database.close();
	}

	/**
	 * Check whether Calendar exist In Device or not.
	 * 
	 * @param account
	 *            : calendar account name.
	 * @return int : if Yes then return 1 else 0.
	 */
	public int isCalInDevice(Account account) {
		// ContentResolver cr = context.getContentResolver();
		// Uri uri = Calendars.CONTENT_URI;
		cr = context.getContentResolver();
		uri = Calendars.CONTENT_URI;
		selectionQuery = "((" + Calendars.ACCOUNT_NAME + " = '" + account.name
				+ "'))";
		cursor = cr.query(uri, CALENDAR_PROJECTION, selectionQuery, null, null);
		int out = 0;
		while (cursor.moveToNext()) {
			out = cursor.getInt(0);
		}
		cursor.close();
		return out;
	}

	/**
	 * Creating own OpenERP Mobile calendar upon Mobile device
	 * 
	 * @param account
	 *            : for which OpenERP calendar created
	 * @return long : the OpenERP Mobile calendar_id
	 */
	public long createCalendar(Account account) {
		cr = context.getContentResolver();
		values = new ContentValues();
		values.put(Calendars.ACCOUNT_NAME, account.name);
		values.put(CalendarContract.Calendars.ACCOUNT_TYPE, account.type);
		values.put(Calendars.NAME, account.name);
		values.put(Calendars.CALENDAR_DISPLAY_NAME, account.name);
		values.put(Calendars.CALENDAR_COLOR, Color.parseColor("#cc0000"));
		values.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);
		values.put(Calendars.OWNER_ACCOUNT, account.name);
		values.put(Calendars.SYNC_EVENTS, 1);
		values.put(Calendars.VISIBLE, 1);
		values.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, TimeZone
				.getDefault().getID());
		creationUri = asSyncAdapter(Calendars.CONTENT_URI, account.name,
				account.type);
		uri = cr.insert(creationUri, values);
		created_Calendar_id = Long.parseLong(uri.getLastPathSegment());
		return created_Calendar_id;
	}

	/**
	 * Check whether Event exist In OpenERP Mobile calendar in Device or not.
	 * 
	 * @param context
	 *            : context value
	 * @param cal_meeting_id
	 *            : id of the event
	 * @return true, if exist
	 */
	public boolean isEventInCal(Context context, String cal_meeting_id) {
		Cursor cursor = context.getContentResolver().query(
				// cursor = context.getContentResolver().query(
				Uri.parse("content://com.android.calendar/events"),
				new String[] { "_id" }, " _id = ? ",
				new String[] { cal_meeting_id }, null);

		boolean flag = false;
		if (cursor.moveToFirst()) {
			// Event exist
			flag = true;
		}
		cursor.close();
		return flag;
	}

	/**
	 * Calculating duration between Start date and End date.
	 * 
	 * @param startDate
	 *            : begin date of the meeting.
	 * @param endDate
	 *            : date_deadline of the meeting.
	 * @return String : duration.
	 */
	private String getDuration(long startDate, long endDate) {
		long offset = endDate - startDate;
		long datehours = offset / (60 * 60 * 1000);
		long dateminutes = offset / (60 * 1000);
		long duration = dateminutes - (datehours * 60);
		String hour = String.valueOf(datehours);
		String minute = String.valueOf(duration);
		StringBuffer durationString = new StringBuffer();
		durationString.append(hour);
		durationString.append(".");
		durationString.append(minute);
		return durationString.toString();
	}

	/**
	 * Converting TimeZone into GMT to save in localDb to avoid Controversy Over
	 * Server
	 * 
	 * @param originalDate
	 *            : the date retrieve from android device the original date
	 * @return String : Converted date in GMT time
	 */
	private String convertDateTimeZone(long originalDate) {
		String newDate = "";
		Date date = new Date(originalDate);
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
		Date parsed = null;
		try {
			parsed = formatter.parse(formatter.format(date).toString());
			TimeZone tz = TimeZone.getTimeZone("UTC");
			SimpleDateFormat destFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			destFormat.setTimeZone(tz);
			newDate = destFormat.format(parsed);
		} catch (Exception e) {
		}
		return newDate;
	}

	/**
	 * Registering the crm.meetings in OpenERP Mobile calendar
	 * 
	 * @param calendar_id
	 *            : of the OpenERP Mobile calendar
	 * @param meeting_id
	 *            : id of crm.meeting table for updating the record
	 * @param EventName
	 *            : meeting name in the crm.meeting table
	 * @param Stime
	 *            : begin Time of meeting
	 * @param Etime
	 *            : date_deadline of meeting
	 * @param Description
	 *            : of the meeting
	 */
	public void syncEvent(long calendar_id, int meeting_id, String EventName,
			String Stime, String Etime, String Description) {
		cal = Calendar.getInstance();
		try {
			// parsing localdb time in required format

			SimpleDateFormat tempFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm");
			tempFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

			startDate = tempFormat.parse(Stime);
			endDate = tempFormat.parse(Etime);

			// creating a calendar instance for the BeginTime
			eventBeginTime = Calendar.getInstance();
			cal.setTime(startDate);

			// setting formate in beginTime.set(2013, 7, 25, 7, 30);
			// endTime.set(year, month, day, hourOfDay, minute);
			eventBeginTime.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
					cal.get(Calendar.DATE), cal.get(Calendar.HOUR_OF_DAY),
					cal.get(Calendar.MINUTE));

			// creating a calendar instance for the EndTime
			eventEndTime = Calendar.getInstance();
			cal.setTime(endDate);

			// setting formate in endTime.set(2013, 7, 25, 14, 30);
			// endTime.set(year, month, day, hourOfDay, minute);
			eventEndTime.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
					cal.get(Calendar.DATE), cal.get(Calendar.HOUR_OF_DAY),
					cal.get(Calendar.MINUTE));

			OEUser user = OpenERPAccountManager.currentUser(context);
			cr = context.getContentResolver();
			values = new ContentValues();
			values.put(Events.DTSTART, eventBeginTime.getTimeInMillis());
			values.put(Events.DTEND, eventEndTime.getTimeInMillis());
			values.put(Events.TITLE, EventName);
			values.put(Events.DESCRIPTION, Description);
			values.put(Events.CALENDAR_ID, calendar_id);
			// values.put(Events._ID, meeting_id);
			values.put(Events.EVENT_TIMEZONE, user.getTimezone());

			// inserting event into OpenERP Mobile calendar
			uri = cr.insert(Events.CONTENT_URI, values);

			// contain id of newly created event in OpenERP Mobile calendar
			creted_Event_id = Long.parseLong(uri.getLastPathSegment());

			update_SyncedEvent(meeting_id, String.valueOf(calendar_id),
					String.valueOf(creted_Event_id), "true");
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retrieve all the meetings/records from crm.meeting table of localdb.
	 * 
	 * @param db
	 *            : database object.
	 * @return list: of all the records.
	 */
	@SuppressWarnings("unchecked")
	public List<HashMap<String, Object>> getAllMeetings(MeetingDBHelper db) {

		HashMap<String, Object> res = db.search(db);
		if (Integer.parseInt(res.get("total").toString()) > 0) {
			return (List<HashMap<String, Object>>) db.search(db).get("records");
		}
		return null;
	}

	/**
	 * Retrieve all the calendar_event_id from crm.meeting table od localdb.
	 * 
	 * @param context
	 *            :value.
	 * @return list: event ids which are synced in OpenERP Mobile Calendar.
	 */
	public List<String> get_cal_event_Ids(Context context) {

		db = new MeetingDBHelper(context);
		List<String> mtnglist = new ArrayList<String>();
		get_AllMettings = db.search(db);

		@SuppressWarnings("unchecked")
		List<HashMap<String, Object>> idsList = (List<HashMap<String, Object>>) get_AllMettings
				.get("records");
		for (int i = 0; i < idsList.size(); i++) {
			mtnglist.add(idsList.get(i).get("calendar_event_id").toString());
		}
		return mtnglist;
	}

	/**
	 * Creting new Meeting over OpenERP server and return new record id.
	 * 
	 * @param name
	 *            : meeting name
	 * @param date
	 *            : meeting begin date.
	 * @param endDate
	 *            : : meeting date_deadline.
	 * @param Duration
	 *            : meeting duration.
	 * @param allDay
	 *            : if allday ? or not.
	 * @param categ_id
	 *            : meeting category_ids.
	 * @param location
	 *            the location
	 * @param description
	 *            : meeting description.
	 * 
	 * @return newid : record id which is creted over OpenERP.
	 */
	public int writeMeeting(String name, String date, String endDate,
			String Duration, boolean allDay, String categ_id, String location,
			String description) {

		String new_generated_id = null;

		try {
			JSONObject response = new JSONObject();
			OEHelper openerp = db.getOEInstance();

			JSONObject args = new JSONObject();
			args.put("name", name);
			args.put("date", date);
			float duration = Float.parseFloat(Duration);
			args.put("duration", duration);
			JSONArray Ctag_ids = new JSONArray();
			Ctag_ids.put(6);
			Ctag_ids.put(false);
			Ctag_ids.put(new JSONArray(categ_id));
			args.put("categ_ids",
					new JSONArray("[" + Ctag_ids.toString() + "]"));
			JSONArray Partner_ids = new JSONArray();
			Partner_ids.put(6);
			Partner_ids.put(false);
			JSONArray c_ids = new JSONArray();
			Partner_ids.put(c_ids);
			args.put("partner_ids", new JSONArray("[" + Partner_ids.toString()
					+ "]"));
			args.put("allday", allDay); // IMP Field REQ
			args.put("date_deadline", endDate); // IMP Field REQ
			args.put("location", location);
			args.put("description", description);

			response = openerp.createNew("crm.meeting", args);
			new_generated_id = response.getString("result");
			addMeetings_TOlocaldb(Integer.parseInt(new_generated_id), name,
					date, endDate, Duration, location, description);

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		return Integer.parseInt(new_generated_id);
	}

	/**
	 * Check whether variable's value is null ? or not.
	 * 
	 * @param value
	 *            : to be checked.
	 * @return String : if yes then "false" else same value.
	 */
	public String isNull(String value) {

		if (value != null && !value.isEmpty()) {
			return value;
		}
		return "false";
	}

	/**
	 * Check whether object is null ? or not.
	 * 
	 * @param str
	 *            : object to be checked.
	 * @return String : if yes then "false" else [parsed into string]String
	 *         value.
	 */
	public String isObjectNull(Object str) {

		if (str != null && !str.toString().isEmpty()) {
			return str.toString();
		}
		return "false";

	}

	/**
	 * Add meettings into crm.meeting table of localdb.
	 * 
	 * @param id
	 *            : of the meeting.
	 * @param name
	 *            : of the meeting.
	 * @param date
	 *            : of the meeting.
	 * @param endDate
	 *            : of the meeting.
	 * @param duration
	 *            : of the meeting.
	 * @param location
	 *            : of the meeting.
	 * @param description
	 *            : of the meeting.
	 */
	public void addMeetings_TOlocaldb(int id, String name, String date,
			String endDate, String duration, String location, String description) {

		db = new MeetingDBHelper(context);
		values = new ContentValues();

		values.put("id", id);
		values.put("name", name);
		values.put("date", date);
		values.put("duration", duration);
		values.put("description", isNull(description));
		values.put("location", isNull(location));
		values.put("date_deadline", endDate);
		values.put("in_cal_sync", "false");
		values.put("calendar_event_id", "false");
		values.put("calendar_id", "false");
		db.create(db, values);
	}

	/**
	 * Delete events from OpenERP Mobile Calendar of MobilePhone/Device.
	 * 
	 * @param calendar_Event_Id
	 *            : event id which'll be deleted.
	 */
	public void delete_CalendarEvent(int calendar_Event_Id) {

		long eventID = calendar_Event_Id;
		Uri deleteUri = null;
		deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, eventID);
		int rows = context.getContentResolver().delete(deleteUri, null, null);
	}
}
