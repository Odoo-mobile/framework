package com.odoo.addons.unmc.patients;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.odoo.R;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.fields.OColumn;

import java.util.Calendar;

import static com.odoo.ActivityCommons.hasRecordInExtra;
import static com.odoo.addons.unmc.patients.Patient.FEMALE_VAL;
import static com.odoo.addons.unmc.patients.Patient.MALE_VAL;

public class PatientDetails extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
    private boolean mEditMode = false;
    private Button submitButton;
    private Bundle bundle;
    private Patient patient;
    private EditText surnameEditText;
    private EditText firstNameEditText;
    private RadioButton maleRadioButton;
    private RadioButton femaleRadioButton;
    private Button dateButton;
    private EditText parentEditText;
    private EditText addressEditText;
    private EditText phoneEditText;
    private EditText barcodeEditText;
    private int rowId;
    private final int PATIENT_DEL_ID = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bundle = getIntent().getExtras();
        if (hasRecordInExtra(bundle)) {
            mEditMode = true;
        }
        patient = new Patient(this, null);
        surnameEditText = (EditText) findViewById(R.id.surname);
        firstNameEditText = (EditText) findViewById(R.id.first_name);
        maleRadioButton = (RadioButton) findViewById(R.id.male_gender);
        femaleRadioButton = (RadioButton) findViewById(R.id.female_gender);
        dateButton = (Button) findViewById(R.id.date_of_birth);
        parentEditText = (EditText) findViewById(R.id.parent_name);
        addressEditText = (EditText) findViewById(R.id.address);
        phoneEditText = (EditText) findViewById(R.id.phone_number);
        barcodeEditText = (EditText) findViewById(R.id.barcode);
        submitButton = (Button) findViewById(R.id.submit_button);
        setupUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mEditMode) {
            menu.add(Menu.NONE, PATIENT_DEL_ID, 0, R.string.label_delete);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case PATIENT_DEL_ID:
                deletePatient();
        }
        return true;
    }

    private void deletePatient() {
        if (mEditMode) {
            patient.delete(rowId);
            finish();
        }
    }

    private void setupUI() {
        dateButton.setOnClickListener(this);
        submitButton.setOnClickListener(this);
        if (mEditMode) {
            setTitle(R.string.update_patient_button);
            submitButton.setText(R.string.update_patient_button);
            rowId = bundle.getInt(OColumn.ROW_ID);
            showRowData();
        } else {
            setTitle(R.string.add_new_patient);
            submitButton.setText(R.string.add_new_patient);
        }
    }

    private void updateChanges() {
        String surname = surnameEditText.getText().toString();
        String firstName = firstNameEditText.getText().toString();
        String gender;
        if (maleRadioButton.isChecked())
            gender = MALE_VAL;
        else
            gender = FEMALE_VAL;
        String date = dateButton.getText().toString();
        String parentNameOrGuardian = parentEditText.getText().toString();
        String phone = phoneEditText.getText().toString();
        String address = addressEditText.getText().toString();
        String barcodeId = barcodeEditText.getText().toString();

        OValues values = new OValues();
        values.put("surname", surname);
        values.put("first_name", firstName);
        values.put("gender", gender);
        values.put("date_of_birth", date);
        values.put("parent_name_or_guardian", parentNameOrGuardian);
        values.put("phone_number", phone);
        values.put("address", address);
        values.put("barcode_id", barcodeId);
        if (mEditMode) {
            patient.update(rowId, values);
            Toast.makeText(this, R.string.patient_updated, Toast.LENGTH_LONG).show();
        } else {
            patient.insert(values);
            Toast.makeText(this, R.string.patient_created, Toast.LENGTH_LONG).show();
        }
        finish();
    }

    private void showRowData() {
        ODataRow patientRow = patient.browse(rowId);

        String surname = patientRow.getString("surname");
        String firstName = patientRow.getString("first_name");
        String gender = patientRow.getString("gender");
        String dateOfBirth = patientRow.getString("date_of_birth");
        String parentNameOrGuardian = patientRow.getString("parent_name_or_guardian");
        String phoneNumber = patientRow.getString("phone_number");
        String address = patientRow.getString("address");
        String barcodeId = patientRow.getString("barcode_id");

        surnameEditText.setText(surname);
        firstNameEditText.setText(firstName);
        if (gender.equalsIgnoreCase(MALE_VAL))
            maleRadioButton.setChecked(true);
        else
            femaleRadioButton.setChecked(true);
        dateButton.setText(dateOfBirth);
        parentEditText.setText(parentNameOrGuardian);
        phoneEditText.setText(phoneNumber);
        addressEditText.setText(address);
        barcodeEditText.setText(barcodeId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.date_of_birth:
                showDateDialog();
                break;
            default:
                updateChanges();
        }
    }

    private void showDateDialog() {
        DialogFragment newFragment = new DatePickerFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DatePickerFragment.DATE_VAL_KEY, dateButton.getText().toString());
        newFragment.setArguments(bundle);
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        dateButton.setText(String.format("%d-%d-%d", year, month + 1, dayOfMonth));
    }

    public static class DatePickerFragment extends DialogFragment {
        public static String DATE_VAL_KEY = "date_key";

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle bundle = getArguments();
            String dateString = bundle.getString(DATE_VAL_KEY, null);
            if (dateString != null) {
                try {
                    String[] dateValues = dateString.split("-");
                    int year = Integer.parseInt(dateValues[0]);
                    int month = Integer.parseInt(dateValues[1]) - 1;
                    int day = Integer.parseInt(dateValues[2]);
                    return new DatePickerDialog(getActivity(), (DatePickerDialog.OnDateSetListener) getActivity(), year, month, day);
                } catch (Exception e) {
                    e.printStackTrace();
                    return defaultValuesDialog();
                }
            } else {
                return defaultValuesDialog();
            }
        }

        @NonNull
        private Dialog defaultValuesDialog() {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), (DatePickerDialog.OnDateSetListener) getActivity(), year, month, day);
        }
    }
}
