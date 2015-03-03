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
 * Created on 7/1/15 5:10 PM
 */
package odoo.controls;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.odoo.core.utils.ODateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTimePicker {
    public static final String TAG = DateTimePicker.class.getSimpleName();

    public enum Type {
        Date, DateTime, Time
    }

    private Context mContext = null;
    private Builder mBuilder;
    private DatePicker mDatePicker;
    private TimePicker mTimePicker;

    public DateTimePicker() {

    }

    public DateTimePicker(Context context, Builder builder) {
        mContext = context;
        mBuilder = builder;
    }

    public void show() {
        if (mBuilder.getType() == Type.Time) {
            mTimePicker = new TimePicker(mContext, mBuilder.getTime());
            mTimePicker.setPickerCallback(callBack);
            mTimePicker.show();
        } else {
            mDatePicker = new DatePicker(mContext, mBuilder.getDate());
            mDatePicker.setPickerCallback(callBack);
            mDatePicker.show();
        }
    }

    PickerCallBack callBack = new PickerCallBack() {

        @Override
        public void onTimePick(String time) {
            mBuilder.getCallBack().onTimePick(time);
            mTimePicker.dismiss();
        }

        @Override
        public void onDatePick(String date) {
            mDatePicker.dismiss();
            if (mBuilder.getType() == Type.DateTime) {
                mTimePicker = new TimePicker(mContext, mBuilder.getTime());
                mTimePicker.setPickerCallback(callBack);
                mTimePicker.show();
            }
            mBuilder.getCallBack().onDatePick(date);
        }
    };

    public static class Builder {
        private Context mContext;
        private Type mType = Type.DateTime;
        private PickerCallBack mCallback;
        private String mDialogTitle = null;
        private String time = null;
        private String date = null;
        private String dateTime = null;

        public Builder(Context context) {
            mContext = context;
        }

        public Builder setDate(String date) {
            this.date = date;
            return this;
        }

        public Builder setTime(String time) {
            this.time = time;
            return this;
        }

        public Builder setDateTime(String dateTime) {
            if (dateTime != null) {
                date = ODateUtils.parseDate(dateTime, ODateUtils.DEFAULT_FORMAT,
                        ODateUtils.DEFAULT_DATE_FORMAT);
                time = ODateUtils.parseDate(dateTime, ODateUtils.DEFAULT_FORMAT,
                        ODateUtils.DEFAULT_TIME_FORMAT);
            }
            return this;
        }

        public Calendar getDate() {
            if (date != null) {
                Date dt = ODateUtils.createDateObject(date, ODateUtils.DEFAULT_DATE_FORMAT, false);
                Calendar cal = Calendar.getInstance();
                cal.setTime(dt);
                return cal;
            }
            return null;
        }

        public Calendar getTime() {
            if (time != null) {
                Date dt = ODateUtils.createDateObject(time, ODateUtils.DEFAULT_TIME_FORMAT, false);
                Calendar cal = Calendar.getInstance();
                cal.setTime(dt);
                return cal;
            }
            return null;
        }

        public Builder setType(Type type) {
            mType = type;
            return this;
        }

        public Type getType() {
            return mType;
        }

        public Builder setCallBack(PickerCallBack callback) {
            mCallback = callback;
            return this;
        }

        public PickerCallBack getCallBack() {
            return mCallback;
        }

        public Builder setTitle(String title) {
            mDialogTitle = title;
            return this;
        }

        public Builder setTitle(int res_id) {
            mDialogTitle = mContext.getResources().getString(res_id);
            return this;
        }

        public String getDialogTitle() {
            return mDialogTitle;
        }

        public DateTimePicker build() {
            DateTimePicker picker = new DateTimePicker(mContext, this);
            return picker;
        }
    }

    public class DatePicker implements DatePickerDialog.OnDateSetListener,
            DialogInterface.OnCancelListener {

        private PickerCallBack mCallback;
        private boolean called = false;
        private Dialog mDialog;

        public DatePicker(Context context, Calendar date) {
            final Calendar c = (date != null) ? date : Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            mDialog = new DatePickerDialog(context, this, year, month, day);
            mDialog.setOnCancelListener(this);
        }

        @Override
        public void onCancel(DialogInterface dialog) {

        }

        @Override
        public void onDateSet(android.widget.DatePicker view, int year,
                              int monthOfYear, int dayOfMonth) {
            if (mCallback != null && !called) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.MONTH, monthOfYear);
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                cal.set(Calendar.YEAR, year);
                Date now = cal.getTime();
                String date = new SimpleDateFormat(ODateUtils.DEFAULT_DATE_FORMAT)
                        .format(now);
                mCallback.onDatePick(date);
                called = true;
            }
        }

        public void setPickerCallback(PickerCallBack callback) {
            mCallback = callback;
        }

        public void show() {
            mDialog.show();
        }

        public void dismiss() {
            mDialog.dismiss();
        }
    }

    public class TimePicker implements TimePickerDialog.OnTimeSetListener,
            DialogInterface.OnCancelListener {

        private PickerCallBack mCallback;
        private TimePickerDialog mDialog = null;

        public TimePicker(Context context, Calendar time) {

            final Calendar c = (time != null) ? time : Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            mDialog = new TimePickerDialog(context, this, hour, minute, false);
            mDialog.setOnCancelListener(this);
        }

        @Override
        public void onTimeSet(android.widget.TimePicker view, int hourOfDay,
                              int minute) {
            if (mCallback != null) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                cal.set(Calendar.MINUTE, minute);
                cal.set(Calendar.MILLISECOND, 0);
                Date now = cal.getTime();
                String time = new SimpleDateFormat(ODateUtils.DEFAULT_TIME_FORMAT)
                        .format(now);
                mCallback.onTimePick(time);
            }
        }

        @Override
        public void onCancel(DialogInterface dialog) {
        }

        public void setPickerCallback(PickerCallBack callback) {
            mCallback = callback;
        }

        public void show() {
            mDialog.show();
        }

        public void dismiss() {
            mDialog.dismiss();
        }
    }

    public interface PickerCallBack {
        public void onDatePick(String date);

        public void onTimePick(String time);
    }
}
