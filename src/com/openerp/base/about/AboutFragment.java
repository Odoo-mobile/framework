package com.openerp.base.about;

import android.content.Context;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.openerp.MainActivity;
import com.openerp.R;
import com.openerp.support.AppScope;
import com.openerp.support.BaseFragment;
import com.openerp.support.menu.OEMenu;

public class AboutFragment extends BaseFragment {

	View rootView = null;
	TextView versionName = null;
	TextView aboutLine2 = null;
	TextView aboutLine3 = null;
	TextView aboutLine4 = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		scope = new AppScope(MainActivity.userContext,
				(MainActivity) getActivity());
		getActivity().getActionBar().setHomeButtonEnabled(true);
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		rootView = inflater.inflate(R.layout.fragment_about_company, container,
				false);
		versionName = (TextView) rootView.findViewById(R.id.txvVersionName);

		try {
			// setting version name from manifest file
			String version = getActivity().getPackageManager().getPackageInfo(
					getActivity().getPackageName(), 0).versionName;
			versionName.setText("Version " + version);

			// setting link in textview
			aboutLine2 = (TextView) rootView.findViewById(R.id.line2);
			if (aboutLine2 != null) {
				aboutLine2.setMovementMethod(LinkMovementMethod.getInstance());
			}
			aboutLine3 = (TextView) rootView.findViewById(R.id.line3);
			if (aboutLine3 != null) {
				aboutLine3.setMovementMethod(LinkMovementMethod.getInstance());
			}
			aboutLine4 = (TextView) rootView.findViewById(R.id.line4);
			if (aboutLine4 != null) {
				aboutLine4.setMovementMethod(LinkMovementMethod.getInstance());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		getActivity().setTitle("About");
		return rootView;
	}

	@Override
	public Object databaseHelper(Context context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleArguments(Bundle bundle) {
		// TODO Auto-generated method stub
	}

	@Override
	public OEMenu menuHelper(Context context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go home
			getActivity().getSupportFragmentManager().popBackStack();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		scope.context().getActionBar().setDisplayHomeAsUpEnabled(true);
		scope.context().getActionBar().setHomeButtonEnabled(true);
	}

}
