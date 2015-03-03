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
 * Created on 31/12/14 11:36 AM
 */
package com.odoo.core.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class ODateUtils {
    public final static String TAG = ODateUtils.class.getSimpleName();
    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";


    /**
     * Return Current date string in "yyyy-MM-dd HH:mm:ss" format
     *
     * @return current date string (Default timezone)
     */
    public static String getDate() {
        return getDate(new Date(), DEFAULT_FORMAT);
    }

    /**
     * Returns current date string in given format
     *
     * @param format, date format
     * @return current date string (Default timezone)
     */
    public static String getDate(String format) {
        return getDate(new Date(), format);
    }

    /**
     * Returns current date string in given format
     *
     * @param date,          date object
     * @param defaultFormat, date format
     * @return current date string (default timezone)
     */
    public static String getDate(Date date, String defaultFormat) {
        return createDate(date, defaultFormat, false);
    }

    /**
     * Returns UTC date string in "yyyy-MM-dd HH:mm:ss" format.
     *
     * @return string, UTC Date
     */
    public static String getUTCDate() {
        return getUTCDate(new Date(), DEFAULT_FORMAT);
    }

    /**
     * Return UTC date in given format
     *
     * @param format, date format
     * @return UTC date string
     */
    public static String getUTCDate(String format) {
        return getUTCDate(new Date(), format);
    }

    /**
     * Returns UTC Date string in given date format
     *
     * @param date,          Date object
     * @param defaultFormat, Date pattern format
     * @return UTC date string
     */
    public static String getUTCDate(Date date, String defaultFormat) {
        return createDate(date, defaultFormat, true);
    }


    /**
     * Convert UTC date to default timezone date
     *
     * @param date       date in string
     * @param dateFormat default date format
     * @return string converted date string
     */
    public static String convertToDefault(String date, String dateFormat) {
        return convertToDefault(date, dateFormat, dateFormat);
    }

    /**
     * Convert UTC date to default timezone
     *
     * @param date       UTC date string
     * @param dateFormat default date format
     * @param toFormat   converting date format
     * @return string converted date string
     */
    public static String convertToDefault(String date, String dateFormat, String toFormat) {
        return createDate(createDateObject(date, dateFormat, false), toFormat, false);
    }

    /**
     * Convert to UTC date
     *
     * @param date       date in string
     * @param dateFormat default date format
     * @return string date string in UTC timezone
     */
    public static String convertToUTC(String date, String dateFormat) {
        return convertToUTC(date, dateFormat, dateFormat);
    }

    /**
     * Convert default timezone date to UTC timezone
     *
     * @param date,      date in string
     * @param dateFormat default date format
     * @param toFormat   display format
     * @return string, returns string converted to UTC
     */
    public static String convertToUTC(String date, String dateFormat, String toFormat) {
        return createDate(createDateObject(date, dateFormat, true), toFormat, true);
    }

    public static String parseDate(String date, String dateFormat, String toFormat) {
        return createDate(createDateObject(date, dateFormat, false), toFormat, true);
    }

    /**
     * Create Date instance from given date string.
     *
     * @param date               date in string
     * @param dateFormat,        original date format
     * @param hasDefaultTimezone if date is in default timezone than true, otherwise false
     * @return Date, returns Date object with given date
     */
    public static Date createDateObject(String date, String dateFormat, Boolean hasDefaultTimezone) {
        Date dateObj = null;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
            if (!hasDefaultTimezone) {
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            }
            dateObj = simpleDateFormat.parse(date);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return dateObj;
    }

    /**
     * Returns date before given days
     *
     * @param days days to before
     * @return string date string before days
     */
    public static String getDateBefore(int days) {
        Date today = new Date();
        Calendar cal = new GregorianCalendar();
        cal.setTime(today);
        cal.add(Calendar.DAY_OF_MONTH, days * -1);
        Date date = cal.getTime();
        SimpleDateFormat gmtFormat = new SimpleDateFormat();
        gmtFormat.applyPattern("yyyy-MM-dd 00:00:00");
        TimeZone gmtTime = TimeZone.getTimeZone("GMT");
        gmtFormat.setTimeZone(gmtTime);
        return gmtFormat.format(date);
    }

    public static Date setDateTime(Date originalDate, int hour, int minute, int second) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(originalDate);
        cal.set(Calendar.HOUR, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static String getDateDayBeforeAfterUTC(String utcDate, int days) {
        Date dt = createDateObject(utcDate, DEFAULT_FORMAT, false);
        Calendar cal = new GregorianCalendar();
        cal.setTime(dt);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return createDate(cal.getTime(), DEFAULT_FORMAT, true);
    }

    public static Date getDateDayBefore(Date originalDate, int days) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(originalDate);
        cal.add(Calendar.DAY_OF_MONTH, days * -1);
        return cal.getTime();
    }

    public static String getCurrentDateWithHour(int addHour) {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR);
        cal.set(Calendar.HOUR, hour + addHour);
        Date date = cal.getTime();
        return ODateUtils.createDate(date, ODateUtils.DEFAULT_FORMAT, true);
    }

    public static Date getDateMinuteBefore(Date originalDate, int minutes) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(originalDate);
        cal.add(Calendar.MINUTE, minutes * -1);
        return cal.getTime();
    }

    private static String createDate(Date date, String defaultFormat, Boolean utc) {
        SimpleDateFormat gmtFormat = new SimpleDateFormat();
        gmtFormat.applyPattern(defaultFormat);
        TimeZone gmtTime = (utc) ? TimeZone.getTimeZone("GMT") : TimeZone.getDefault();
        gmtFormat.setTimeZone(gmtTime);
        return gmtFormat.format(date);
    }

    public static String floatToDuration(String duration_in_float) {
        duration_in_float = String.format("%2.2f", Float.parseFloat(duration_in_float));
        String[] parts = duration_in_float.split("\\.");
        long minute = Long.parseLong(parts[0]);
        long seconds = (60 * Long.parseLong(parts[1])) / 100;
        return String.format("%02d:%02d", minute, seconds);
    }

    public static String durationToFloat(String duration) {
        String[] parts = duration.split("\\:");
        if (parts.length == 2) {
            long minute = Long.parseLong(parts[0]);
            long seconds = Long.parseLong(parts[1]);
            if (seconds == 60) {
                minute = minute + 1;
                seconds = 0;
            } else {
                seconds = (100 * seconds) / 60;
            }
            return String.format("%d.%d", minute, seconds);
        }
        return "false";
    }

    public static String durationToFloat(long milliseconds) {
        long minute = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                TimeUnit.MINUTES.toSeconds(minute);
        if (seconds == 60) {
            minute = minute + 1;
            seconds = 0;
        } else {
            seconds = (100 * seconds) / 60;
        }
        return String.format("%d.%d", minute, seconds);
    }
}
