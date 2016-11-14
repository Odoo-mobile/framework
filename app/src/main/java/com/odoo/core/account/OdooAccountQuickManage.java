/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p/>
 * Created on 16/2/15 12:52 PM
 */
package com.odoo.core.account;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.odoo.App;
import com.odoo.OdooActivity;
import com.odoo.R;
import com.odoo.core.auth.OdooAccountManager;
import com.odoo.core.rpc.Odoo;
import com.odoo.core.service.OSyncAdapter;
import com.odoo.core.support.OUser;
import com.odoo.core.utils.BitmapUtils;
import com.odoo.core.utils.OResource;

public class OdooAccountQuickManage extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = OdooAccountQuickManage.class.getSimpleName();
    private OUser user = null;
    private ImageView userAvatar;
    private TextView txvName;
    private LoginProcess loginProcess = null;
    private EditText edtPassword;
    private String action;
    private App mApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_account_quick_manage);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getSupportActionBar().hide();
        action = getIntent().getAction();
        mApp = (App) getApplicationContext();
        user = OdooAccountManager.getDetails(this, getIntent().getStringExtra("android_name"));
        if (action.equals("remove_account")) {
            findViewById(R.id.layoutSavePassword).setVisibility(View.GONE);
            removeAccount();
        } else if (action.equals("reset_password")) {
            updatePassword();
            findViewById(R.id.cancel).setOnClickListener(this);
            findViewById(R.id.save_password).setOnClickListener(this);
        }
    }

    private void updatePassword() {
        userAvatar = (ImageView) findViewById(R.id.userAvatar);
        Bitmap userImage = BitmapUtils.getAlphabetImage(this, user.getName());
        if (!user.getAvatar().equals("false")) {
            userImage = BitmapUtils.getBitmapImage(this, user.getAvatar());
        }
        userAvatar.setImageBitmap(userImage);
        txvName = (TextView) findViewById(R.id.userName);
        txvName.setText(user.getName());
    }

    private void removeAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_confirm);
        builder.setMessage(R.string.toast_are_you_sure_delete_account);
        builder.setPositiveButton(R.string.label_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (OdooAccountManager.removeAccount(
                        OdooAccountQuickManage.this, user.getAndroidName())) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent loginActivity = new Intent(OdooAccountQuickManage.this,
                                    OdooLogin.class);
                            loginActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(loginActivity);
                            finish();
                        }
                    }, 500);
                }
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                finish();
            }
        });
        builder.setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                finish();
                break;
            case R.id.save_password:
                savePassword();
                break;
        }
    }

    private void savePassword() {
        edtPassword = (EditText) findViewById(R.id.newPassword);
        edtPassword.setError(null);
        if (TextUtils.isEmpty(edtPassword.getText())) {
            edtPassword.setError("Password required");
            edtPassword.requestFocus();
        }
        user.setPassword(edtPassword.getText().toString());
        loginProcess = new LoginProcess();
        loginProcess.execute(user.getDatabase(), user.getHost());
    }

    private class LoginProcess extends AsyncTask<String, Void, OUser> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(OdooAccountQuickManage.this);
            progressDialog.setTitle(R.string.title_working);
            progressDialog.setMessage(OResource.string(OdooAccountQuickManage.this,
                    R.string.toast_updating_password));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected OUser doInBackground(String... params) {
            Odoo odoo = mApp.getOdoo(user);
            if (odoo == null) {
                odoo = OSyncAdapter.createOdooInstance(OdooAccountQuickManage.this,
                        (com.odoo.core.support.OUser) user);
            }
            return odoo.authenticate(user.getUsername(), user.getPassword(), user.getDatabase());
        }

        @Override
        protected void onPostExecute(OUser oUser) {
            super.onPostExecute(oUser);
            progressDialog.dismiss();
            if (oUser != null) {
                OdooAccountManager.updateUserData(OdooAccountQuickManage.this,
                        (com.odoo.core.support.OUser) user);
                mApp.setOdoo(null, user);
                finish();
                Intent intent = new Intent(OdooAccountQuickManage.this, OdooActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
            } else {
                edtPassword.setText("");
                edtPassword.setError("Password required");
            }
        }
    }
}
