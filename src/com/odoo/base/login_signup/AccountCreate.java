package com.odoo.base.login_signup;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import odoo.ODomain;
import odoo.OdooAccountExpireException;
import odoo.OdooInstance;

import org.json.JSONArray;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.odoo.App;
import com.odoo.R;
import com.odoo.auth.OdooAccountManager;
import com.odoo.base.ir.IrModel;
import com.odoo.orm.OdooHelper;
import com.odoo.support.OExceptionDialog;
import com.odoo.support.OUser;
import com.odoo.support.fragment.BaseFragment;
import com.odoo.support.fragment.FragmentListener;
import com.odoo.support.listview.OListAdapter;
import com.odoo.util.OControls;
import com.odoo.util.PreferenceManager;
import com.odoo.util.controls.ExpandableHeightGridView;
import com.odoo.util.drawer.DrawerItem;

public class AccountCreate extends BaseFragment implements OnItemClickListener {

	View mView = null;
	OUser mUser = null;
	AccountCreate mSelf = null;
	Boolean loadConfigWizard = true, mSelfHosted = false;
	OListAdapter mListAdapter = null;
	List<Object> mInstanceList = new ArrayList<Object>();
	ExpandableHeightGridView mGridView = null;
	App mApp = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		showActionBar(false);
		mView = inflater.inflate(
				R.layout.base_login_signup_account_create_layout, container,
				false);
		mApp = (App) getActivity().getApplicationContext();
		mSelf = this;
		initArgs();
		init();
		return mView;
	}

	private void init() {
		mListAdapter = new OListAdapter(getActivity(),
				R.layout.base_login_signup_instance_view, mInstanceList) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View mView = convertView;
				if (mView == null)
					mView = getActivity().getLayoutInflater().inflate(
							getResource(), parent, false);
				OdooInstance row = (OdooInstance) mInstanceList.get(position);
				String image_url = row.getInstanceUrl()
						+ "/web/binary/company_logo?dbname="
						+ row.getDatabaseName();
				ImageLoader imgLoader = new ImageLoader(position, image_url,
						R.id.imgInstance);
				imgLoader.execute();
				OControls.setText(mView, R.id.txvInstanceName,
						row.getCompanyName());
				OControls.setText(mView, R.id.txvInstanceUrl,
						row.getInstanceUrl());
				return mView;
			}
		};
	}

	private void initArgs() {
		if (getArguments() != null
				&& getArguments().containsKey("no_config_wizard")) {
			loadConfigWizard = !getArguments().getBoolean("no_config_wizard");
		}
		mUser = new OUser();
		mUser.setFromBundle(getArguments());
		if (loadConfigWizard) {
			GetInstances instances = new GetInstances(getArguments()
					.getBoolean("self_hosted"));
			instances.execute();
		} else {
			// Create database
			DatabaseCreate databaseCreate = new DatabaseCreate();
			databaseCreate.execute();
		}
	}

	// Step 1
	// Load Instances
	class GetInstances extends AsyncTask<Void, Void, List<OdooInstance>> {
		Boolean mSelfHosted = false;

		public GetInstances(Boolean selfHosted) {
			mSelfHosted = selfHosted;
		}

		@Override
		protected void onPreExecute() {
			OControls.setText(mView, R.id.progressStatus, getResources()
					.getString(R.string.title_getting_instances));
		}

		@Override
		protected List<OdooInstance> doInBackground(Void... params) {
			try {
				if (!mSelfHosted) {
					OdooHelper odoo = new OdooHelper(getActivity());
					return odoo.getUserInstances(mUser);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<OdooInstance> result) {
			super.onPostExecute(result);
			if (result != null) {
				if (result.size() > 1) {
					// More than one instance
					LoadInstances instances = new LoadInstances(result);
					instances.execute();
				} else {
					loginToOdoo();
				}
			} else {
				CreateAccount createAccount = new CreateAccount(null,
						mSelfHosted);
				createAccount.execute();
			}
		}

	}

	// Step 2 : only if there are more than one instance
	class LoadInstances extends AsyncTask<Void, Void, Void> {
		List<OdooInstance> mInstances = new ArrayList<OdooInstance>();

		public LoadInstances(List<OdooInstance> instances) {
			mInstances.addAll(instances);
			OControls.setGone(mView, R.id.progressLoader);
			OControls.setVisible(mView, R.id.instanceList);
			mGridView = (ExpandableHeightGridView) mView
					.findViewById(R.id.gridInstances);
			mGridView.setExpanded(true);
			mGridView.setAdapter(mListAdapter);
		}

		@Override
		protected Void doInBackground(Void... params) {
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mInstanceList.clear();
					mInstanceList.addAll(mInstances);
				}
			});
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mListAdapter.notifiyDataChange(mInstanceList);
			mGridView.setOnItemClickListener(mSelf);
		}
	}

	class ImageLoader extends AsyncTask<Void, Void, Void> {

		String image_url = "";
		int image_view = -1;
		int view_pos = -1;
		Bitmap bmp = null;

		public ImageLoader(int pos, String url, int image_view) {
			view_pos = pos;
			this.image_view = image_view;
			image_url = url;
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				URL url = new URL(image_url);
				bmp = BitmapFactory.decodeStream(url.openConnection()
						.getInputStream());
			} catch (Exception e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (bmp != null) {
				View mView = mGridView.getChildAt(view_pos);
				OControls.setImage(mView, image_view, bmp);
			}
		}
	}

	// Step 2 : only if there are any instance found or self hosted
	class CreateAccount extends AsyncTask<Void, Void, Boolean> {
		OdooInstance mOdooInstance = null;
		String mOdooException = null;
		Boolean mSelfHosted = false;

		public CreateAccount(OdooInstance instance, Boolean selfHosted) {
			mOdooInstance = instance;
			mSelfHosted = selfHosted;
			OControls.setGone(mView, R.id.instanceList);
			OControls.setVisible(mView, R.id.progressLoader);
			OControls.setText(mView, R.id.progressStatus, getActivity()
					.getResources().getString(R.string.title_account_create));
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				OdooHelper odoo = new OdooHelper(getActivity(),
						mUser.isAllowSelfSignedSSL());
				OUser user = null;
				if (mSelfHosted) {
					user = odoo.login(mUser.getUsername(), mUser.getPassword(),
							mUser.getDatabase(), mUser.getHost());
				} else {
					user = odoo.instance_login(mOdooInstance,
							mUser.getUsername(), mUser.getPassword());
				}
				if (OdooAccountManager.createAccount(getActivity(), user)) {
					mApp.createInstance();
					return true;
				}
			} catch (OdooAccountExpireException e) {
				mOdooException = e.getMessage();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				DatabaseCreate databaseCreate = new DatabaseCreate();
				databaseCreate.execute();
			} else {
				if (mOdooException != null) {
					OExceptionDialog dialog = new OExceptionDialog(
							getActivity(), true, mOdooException);
					dialog.show();
					OControls.setVisible(mView, R.id.instanceList);
					OControls.setGone(mView, R.id.progressLoader);
				}
			}
		}

	}

	class DatabaseCreate extends AsyncTask<Void, Void, Void> {
		App mApp = null;
		IrModel mIRModel = null;

		public DatabaseCreate() {
			mApp = (App) getActivity().getApplicationContext();
			mIRModel = new IrModel(getActivity().getApplicationContext());
		}

		@Override
		protected void onPreExecute() {
			OControls.setText(mView, R.id.progressStatus, getResources()
					.getString(R.string.title_database_create));
		}

		@Override
		protected Void doInBackground(Void... params) {
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					PreferenceManager pref = new PreferenceManager(
							getActivity());
					List<String> model_list = new ArrayList<String>();
					for (String m : pref.getStringSet("models"))
						model_list.add(m);
					try {
						ODomain domain = new ODomain();
						domain.add("model", "in",
								new JSONArray(model_list.toString()));
						mIRModel.getSyncHelper().syncWithServer(domain);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			});
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (mIRModel.isEmptyTable()) {
				DatabaseCreate databaseCreate = new DatabaseCreate();
				databaseCreate.execute();
			} else {
				if (loadConfigWizard) {
					SyncWizard syncWizard = new SyncWizard();
					FragmentListener mFragment = (FragmentListener) getActivity();
					mFragment.startMainFragment(syncWizard, false);
				}
			}
		}

	}

	@Override
	public Object databaseHelper(Context context) {
		return null;
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		return null;
	}

	// On instance click
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		OdooInstance instance = (OdooInstance) mInstanceList.get(position);
		String server_url = instance.getInstanceUrl();
		if (!server_url.equals(OdooHelper.ODOO_SERVER_URL)) {
			loginWithInstance(instance);
		} else {
			loginToOdoo();
		}
	}

	private void loginToOdoo() {
		mUser.setHost(OdooHelper.ODOO_SERVER_URL);
		CreateAccount createAccount = new CreateAccount(null, true);
		createAccount.execute();
	}

	private void loginWithInstance(OdooInstance instance) {
		CreateAccount account = new CreateAccount(instance, false);
		account.execute();
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().getActionBar().hide();
	}
}
