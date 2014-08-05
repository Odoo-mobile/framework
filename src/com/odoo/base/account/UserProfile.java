/*
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
 */
package com.odoo.base.account;

import java.util.List;

import odoo.OdooInstance;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.odoo.R;
import com.odoo.auth.OdooAccountManager;
import com.odoo.orm.OdooHelper;
import com.odoo.support.AppScope;
import com.odoo.support.OUser;
import com.odoo.support.fragment.BaseFragment;
import com.odoo.util.Base64Helper;
import com.odoo.util.OControls;
import com.odoo.util.drawer.DrawerItem;

public class UserProfile extends BaseFragment {
	View rootView = null;
	EditText password = null;
	AlertDialog.Builder builder = null;
	Dialog dialog = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		rootView = inflater.inflate(R.layout.base_account_user_profile,
				container, false);
		scope = new AppScope(this);
		scope.main().setTitle(R.string.title_user_profile);
		setupView();
		return rootView;
	}

	private void setupView() {
		Bitmap userPic = null;
		if (!scope.User().getAvatar().equals("false"))
			userPic = Base64Helper.getRoundedCornerBitmap(getActivity(),
					Base64Helper.getBitmapImage(scope.context(), scope.User()
							.getAvatar()), true);
		else
			userPic = Base64Helper.getRoundedCornerBitmap(getActivity(),
					BitmapFactory.decodeResource(getActivity().getResources(),
							R.drawable.avatar), true);
		OControls.setImage(rootView, R.id.imgUserProfilePic, userPic);
		OControls.setText(rootView, R.id.userFullName, scope.User().getName());
		OControls.setText(rootView, R.id.txvUserName, scope.User()
				.getUsername());
		OControls.setText(rootView, R.id.txvServerUrl, (scope.User()
				.isOAauthLogin()) ? scope.User().getInstanceUrl() : scope
				.User().getHost());
		OControls.setText(rootView, R.id.txvDatabase, (scope.User()
				.isOAauthLogin()) ? scope.User().getInstanceDatabase() : scope
				.User().getDatabase());
		String timezone = scope.User().getTimezone();
		OControls.setText(rootView, R.id.txvTimeZone,
				(timezone.equals("false")) ? "GMT" : timezone);
		OControls.setText(rootView, R.id.txvOdooVersion, scope.User()
				.getVersion_serie());

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_fragment_account_user_profile, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_account_user_profile_sync:
			dialog = inputPasswordDialog();
			dialog.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	private Dialog inputPasswordDialog() {
		builder = new Builder(scope.context());
		password = new EditText(scope.context());
		password.setTransformationMethod(PasswordTransformationMethod
				.getInstance());
		builder.setTitle(R.string.title_enter_password)
				.setMessage(R.string.toast_provide_password).setView(password);
		builder.setPositiveButton(R.string.label_update_info,
				new OnClickListener() {
					public void onClick(DialogInterface di, int i) {
						OUser userData = null;
						try {
							OdooHelper odoo = null;
							if (scope.User().isOAauthLogin()) {
								odoo = new OdooHelper(getActivity());
								OdooInstance instance = new OdooInstance();
								instance.setInstanceUrl(scope.User()
										.getInstanceUrl());
								instance.setDatabaseName(scope.User()
										.getInstanceDatabase());
								instance.setClientId(scope.User().getClientId());
								userData = odoo.instance_login(instance, scope
										.User().getUsername(), password
										.getText().toString());
							} else {
								odoo = new OdooHelper(getActivity(), scope
										.User().isAllowSelfSignedSSL());
								userData = odoo.login(scope.User()
										.getUsername(), password.getText()
										.toString(),
										scope.User().getDatabase(), scope
												.User().getHost());
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (userData != null) {
							if (OdooAccountManager.updateAccountDetails(
									scope.context(), userData)) {
								Toast.makeText(getActivity(),
										"Infomation Updated.",
										Toast.LENGTH_LONG).show();
							}
						} else {
							Toast.makeText(
									getActivity(),
									getResources().getString(
											R.string.toast_invalid_password),
									Toast.LENGTH_LONG).show();
						}
						setupView();
						dialog.cancel();
						dialog = null;
					}
				});
		builder.setNegativeButton(R.string.label_cancel, new OnClickListener() {
			public void onClick(DialogInterface di, int i) {
				dialog.cancel();
				dialog = null;
			}
		});
		return builder.create();

	}

	@Override
	public Object databaseHelper(Context context) {
		return null;
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		return null;
	}
}
