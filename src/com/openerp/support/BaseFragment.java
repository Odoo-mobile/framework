package com.openerp.support;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView.OnQueryTextListener;

import com.openerp.auth.OpenERPAccountManager;
import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.OEHelper;
import com.openerp.support.listview.OEListViewAdapter;
import com.openerp.support.menu.OEMenu;

public abstract class BaseFragment extends Fragment implements FragmentHelper {

    public AppScope scope;
    public BaseDBHelper db;
    private OEListViewAdapter listSearchAdapter;

    public OEHelper getOEInstance() {
	OEHelper openerp = null;
	try {
	    openerp = new OEHelper(scope.context(),
		    OpenERPAccountManager.currentUser(scope.context()));
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    // e.printStackTrace();
	}
	return openerp;
    }

    public BaseDBHelper getModel() {
	return (BaseDBHelper) databaseHelper(scope.context());
    }

    public OnQueryTextListener getQueryListener(OEListViewAdapter listAdapter) {
	listSearchAdapter = listAdapter;
	return queryListener;
    }

    /* list search handler */
    private String grid_currentQuery = null; // holds the current query...

    final public OnQueryTextListener queryListener = new OnQueryTextListener() {

	private boolean isSearched = false;

	@Override
	public boolean onQueryTextChange(String newText) {

	    if (TextUtils.isEmpty(newText)) {
		grid_currentQuery = null;
		newText = "";
		if (isSearched && listSearchAdapter != null) {
		    listSearchAdapter.getFilter().filter(null);
		}

	    } else {
		isSearched = true;
		grid_currentQuery = newText;
		listSearchAdapter.getFilter().filter(newText);
	    }

	    return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
	    return false;
	}
    };

}

interface FragmentHelper {
    public Object databaseHelper(Context context);

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState);

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater);

    public void handleArguments(Bundle bundle);

    public OEMenu menuHelper(Context context);

}
