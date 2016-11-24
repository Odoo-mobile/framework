/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p>
 * Created on 19/12/14 2:30 PM
 */
package com.odoo.core.account;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.odoo.OdooActivity;
import com.odoo.R;
import com.odoo.core.auth.OdooAccountManager;
import com.odoo.core.support.OUser;
import com.odoo.core.utils.BitmapUtils;
import com.odoo.core.utils.OAppBarUtils;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.OResource;

import java.util.ArrayList;
import java.util.List;

import odoo.controls.ExpandableListControl;

public class ManageAccounts extends AppCompatActivity implements View.OnClickListener,
        ExpandableListControl.ExpandableListAdapterGetViewListener {

    private List<Object> accounts = new ArrayList<>();
    private ExpandableListControl mList = null;
    private ExpandableListControl.ExpandableListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_manage_accounts);
        setTitle(R.string.label_accounts);
        OAppBarUtils.setAppBar(this, true);
        setResult(RESULT_CANCELED);
        accounts.clear();
        accounts.addAll(OdooAccountManager.getAllAccounts(this));
        mList = (ExpandableListControl) findViewById(R.id.accountList);
        mAdapter = mList.getAdapter(R.layout.base_account_item, accounts, this);
        mAdapter.notifyDataSetChanged(accounts);
    }

    private void generateView(View view, OUser user) {
        OControls.setText(view, R.id.accountName, user.getName());
        OControls.setText(view, R.id.accountURL, user.getHost());
        OControls.setImage(view, R.id.profile_image, R.drawable.avatar);
        if (!user.getAvatar().equals("false")) {
            Bitmap bmp = BitmapUtils.getBitmapImage(this, user.getAvatar());
            if (bmp != null)
                OControls.setImage(view, R.id.profile_image, bmp);
        }
        if (user.isActive()) {
            OControls.setVisible(view, R.id.btnLogout);
            OControls.setGone(view, R.id.btnLogin);
        } else {
            OControls.setGone(view, R.id.btnLogout);
            OControls.setVisible(view, R.id.btnLogin);
        }
        view.findViewById(R.id.btnLogin).setTag(user);
        view.findViewById(R.id.btnLogout).setTag(user);
        view.findViewById(R.id.btnRemoveAccount).setTag(user);
        view.findViewById(R.id.btnLogout).setOnClickListener(this);
        view.findViewById(R.id.btnLogin).setOnClickListener(this);
        view.findViewById(R.id.btnRemoveAccount).setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.btnLogin:
                OUser user = (OUser) v.getTag();
                OdooAccountManager.login(this, user.getAndroidName());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ManageAccounts.this, OResource.string(ManageAccounts.this,
                                R.string.status_login_success), Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK);
                        finish();
                    }
                }, OdooActivity.DRAWER_ITEM_LAUNCH_DELAY);
                break;
            case R.id.btnLogout:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.title_confirm);
                builder.setMessage(R.string.toast_are_you_sure_logout);
                builder.setPositiveButton(R.string.label_logout, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OUser user = (OUser) v.getTag();
                        OdooAccountManager.logout(ManageAccounts.this, user.getAndroidName());

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ManageAccounts.this, OResource.string(ManageAccounts.this,
                                        R.string.status_logout_success), Toast.LENGTH_LONG).show();
                                setResult(RESULT_OK);
                                finish();
                            }
                        }, OdooActivity.DRAWER_ITEM_LAUNCH_DELAY);

                    }
                });
                builder.setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
                break;
            case R.id.btnRemoveAccount:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.title_confirm);
                builder.setMessage(R.string.toast_are_you_sure_delete_account);
                builder.setPositiveButton(R.string.label_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OUser user = (OUser) v.getTag();
                        new AccountDeleteTask().execute(user);
                        setResult(RESULT_OK);
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
                break;
        }
    }

    private class AccountDeleteTask extends AsyncTask<OUser, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(OUser... odooUsers) {
            return OdooAccountManager.removeAccount(ManageAccounts.this, odooUsers[0].getAndroidName());
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                Toast.makeText(ManageAccounts.this, R.string.toast_account_removed,
                        Toast.LENGTH_LONG).show();
                accounts.clear();
                accounts.addAll(OdooAccountManager.getAllAccounts(ManageAccounts.this));
                mAdapter.notifyDataSetChanged(accounts);
            }
        }
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        generateView(view, (OUser) mAdapter.getItem(position));
        return view;
    }
}
