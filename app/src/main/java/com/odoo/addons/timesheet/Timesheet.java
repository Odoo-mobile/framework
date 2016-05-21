package com.odoo.addons.timesheet;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.odoo.base.addons.res.ResUsers;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import odoo.controls.OField;

/**
 * Created by Sylwek on 27/12/2015.
 */
public class Timesheet extends AppCompatActivity
        implements View.OnClickListener, OField.IOnFieldValueChangeListener {

    public static final String TAG = Timesheet.class.getSimpleName();
    public Calendar calendar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ResUsers resUsers = new ResUsers(this,null);



    }

    private void getWeekDates()
    {
        calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd.MM.yyyy");

        for (int i = 0; i < 7; i++) {
            Log.i("dateTag", sdf.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_WEEK, 1);
        }
    }
    @Override
    public void onFieldValueChange(OField field, Object value) {

    }

    @Override
    public void onClick(View v) {

    }
}
