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
 * Created on 18/12/14 12:00 PM
 */
package com.odoo.core.support;

import android.content.Context;
import android.text.TextUtils;

import com.odoo.core.utils.JSONUtils;

import org.json.JSONArray;

import java.util.List;

import javax.net.ssl.SSLPeerUnverifiedException;

import odoo.OVersionException;
import odoo.Odoo;

public class OdooServerTester {
    private Context mContext;
    private Boolean mForceConnect = false;
    private Odoo mOdoo;
    private JSONArray mDatabases = null;

    public OdooServerTester(Context context) {
        mContext = context;
    }

    public boolean testConnection(String serverURL, Boolean forceConnect) throws SSLPeerUnverifiedException, OVersionException {
        mForceConnect = forceConnect;
        if (!TextUtils.isEmpty(serverURL)) {
            try {
                mOdoo = new Odoo(mContext, serverURL, forceConnect);
                mDatabases = mOdoo.getDatabaseList();
                if (mDatabases == null) {
                    mDatabases = new JSONArray();
                    if (mOdoo.getDatabaseName() != null) {
                        mDatabases.put(mOdoo.getDatabaseName());
                    }
                }
                if (mDatabases.length() > 0)
                    return true;
            } catch (SSLPeerUnverifiedException peer) {
                throw new SSLPeerUnverifiedException(peer.getMessage());
            } catch (OVersionException version) {
                throw new OVersionException(version.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public List<String> getDatabases() {
        return JSONUtils.toList(mDatabases);
    }
}
