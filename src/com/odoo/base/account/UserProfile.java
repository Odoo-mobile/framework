package com.odoo.base.account;

import odoo.OdooInstance;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.odoo.R;
import com.odoo.auth.OdooAccountManager;
import com.odoo.orm.OdooHelper;
import com.odoo.support.AppScope;
import com.odoo.support.OUser;
import com.odoo.util.Base64Helper;
import com.odoo.util.OControls;
import com.odoo.util.dialog.MaterialDialog;

public class UserProfile extends ActionBarActivity {
	private EditText password = null;
	private MaterialDialog builder = null;
	private Dialog dialog = null;
	private View rootview = null;
	private AppScope scope = null;
	private ActionBar actionBar;
	private Context mContext = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_profile);
		mContext = this;
		actionBar = getSupportActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color
				.parseColor("#00000000")));
		actionBar.setTitle("");
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		scope = new AppScope(mContext);
		rootview = findViewById(R.id.profile_parent_view);
		setupView();
	}

	private void setupView() {
		Bitmap userPic = null;
		if (!scope.User().getAvatar().equals("false"))
			userPic = Base64Helper.getBitmapImage(scope.context(), scope.User()
					.getAvatar());
		else
			userPic = BitmapFactory.decodeResource(mContext.getResources(),
					R.drawable.avatar);
		OControls.setImage(rootview, R.id.imgUserProfilePic, userPic);
		OControls.setText(rootview, R.id.userFullName, scope.User().getName());
		OControls.setText(rootview, R.id.txvUserName, scope.User()
				.getUsername());
		OControls.setText(rootview, R.id.txvServerUrl, (scope.User()
				.isOAauthLogin()) ? scope.User().getInstanceUrl() : scope
				.User().getHost());
		OControls.setText(rootview, R.id.txvDatabase, (scope.User()
				.isOAauthLogin()) ? scope.User().getInstanceDatabase() : scope
				.User().getDatabase());
		String timezone = scope.User().getTimezone();
		OControls.setText(rootview, R.id.txvTimeZone,
				(timezone.equals("false")) ? "GMT" : timezone);
		OControls.setText(rootview, R.id.txvOdooVersion, scope.User()
				.getVersion_serie());

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_user_profile, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.menu_account_user_profile_sync:
			dialog = inputPasswordDialog();
			dialog.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private Dialog inputPasswordDialog() {
		builder = new MaterialDialog(scope.context());
		password = new EditText(scope.context());
		password.setTransformationMethod(PasswordTransformationMethod
				.getInstance());
		builder.setTitle(R.string.title_enter_password);
		builder.setCustomView(password);
		builder.setupPositiveButton(R.string.label_update_info,
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						OUser userData = null;
						try {
							OdooHelper odoo = null;
							if (scope.User().isOAauthLogin()) {
								odoo = new OdooHelper(mContext);
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
								odoo = new OdooHelper(mContext, scope.User()
										.isAllowSelfSignedSSL());
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
								Toast.makeText(mContext, "Infomation Updated.",
										Toast.LENGTH_LONG).show();
							}
						} else {
							Toast.makeText(
									mContext,
									getResources().getString(
											R.string.toast_invalid_password),
									Toast.LENGTH_LONG).show();
						}
						setupView();
						dialog.dismiss();
					}
				});
		builder.setupNegativeButton(R.string.label_cancel,
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
		return builder;

	}
}
